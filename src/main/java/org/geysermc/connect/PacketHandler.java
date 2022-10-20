/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserConnect
 */

package org.geysermc.connect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.AttributeData;
import com.nukkitx.protocol.bedrock.data.ExperimentData;
import com.nukkitx.protocol.bedrock.data.PacketCompressionAlgorithm;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.*;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import com.nukkitx.protocol.bedrock.v471.Bedrock_v471;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.connect.proxy.GeyserProxySession;
import org.geysermc.connect.ui.FormID;
import org.geysermc.connect.ui.UIHandler;
import org.geysermc.connect.utils.GeyserConnectFileUtils;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;
import org.geysermc.cumulus.Form;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.FormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.network.AuthType;
import org.geysermc.geyser.api.network.RemoteServer;
import org.geysermc.geyser.entity.attribute.GeyserAttributeType;
import org.geysermc.geyser.network.GameProtocol;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.session.PendingMicrosoftAuthentication.*;
import org.geysermc.geyser.session.auth.AuthData;
import org.geysermc.geyser.session.auth.BedrockClientData;
import org.geysermc.geyser.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PacketHandler implements BedrockPacketHandler {

    private final BedrockServerSession session;
    private final MasterServer masterServer;

    private Player player;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public PacketHandler(BedrockServerSession session, MasterServer masterServer) {
        this.session = session;
        this.masterServer = masterServer;

        session.addDisconnectHandler(this::disconnect);
    }

    public void disconnect(DisconnectReason reason) {
        if (player != null) {
            masterServer.getLogger().info(player.getAuthData().name() + " has disconnected from the master server (" + reason + ")");
            masterServer.getStorageManager().saveServers(player);

            masterServer.getPlayers().remove(player);
        }
    }

    private boolean checkedProtocol = false;

    @Override
    public boolean handle(RequestNetworkSettingsPacket packet) {
        if (checkProtocol(packet.getProtocolVersion())) {
            PacketCompressionAlgorithm algorithm = PacketCompressionAlgorithm.ZLIB;

            NetworkSettingsPacket responsePacket = new NetworkSettingsPacket();
            responsePacket.setCompressionAlgorithm(algorithm);
            responsePacket.setCompressionThreshold(512);
            session.sendPacketImmediately(responsePacket);

            session.setCompression(algorithm);
        }
        return true;
    }

    private boolean checkProtocol(int protocolVersion) {
        BedrockPacketCodec packetCodec = GameProtocol.getBedrockCodec(protocolVersion);
        if (packetCodec == null) {
            session.setPacketCodec(GameProtocol.DEFAULT_BEDROCK_CODEC);

            String message = "disconnectionScreen.internalError.cantConnect";
            PlayStatusPacket status = new PlayStatusPacket();
            if (protocolVersion > GameProtocol.DEFAULT_BEDROCK_CODEC.getProtocolVersion()) {
                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD);
                message = "disconnectionScreen.outdatedServer";
            } else if (protocolVersion < GameProtocol.DEFAULT_BEDROCK_CODEC.getProtocolVersion()) {
                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
                message = "disconnectionScreen.outdatedClient";
            }
            session.sendPacket(status);
            session.disconnect(message);

            return false;
        }

        // Set the session codec
        session.setPacketCodec(packetCodec);
        return true;
    }

    @Override
    public boolean handle(LoginPacket packet) {
        masterServer.getLogger().debug("Login: " + packet.toString());

        if (!checkedProtocol) {
            if (!checkProtocol(packet.getProtocolVersion())) {
                return false;
            }
            checkedProtocol = true;
        }

        // Read the raw chain data
        JsonNode rawChainData;
        try {
            rawChainData = OBJECT_MAPPER.readTree(packet.getChainData().toByteArray());
        } catch (IOException e) {
            throw new AssertionError("Unable to read chain data!");
        }

        // Get the parsed chain data
        JsonNode chainData = rawChainData.get("chain");
        if (chainData.getNodeType() != JsonNodeType.ARRAY) {
            throw new AssertionError("Invalid chain data!");
        }

        try {
            // Convert the chainData to a JSONArray
            ObjectReader reader = OBJECT_MAPPER.readerFor(new TypeReference<List<String>>() { });
            JSONArray array = new JSONArray();
            array.addAll(reader.readValue(chainData));

            // Verify the chain data
            if (!EncryptionUtils.verifyChain(array)) {
                // Disconnect the client
                session.disconnect("disconnectionScreen.internalError.cantConnect");
                throw new AssertionError("Failed to login, due to invalid chain data!");
            }

            // Parse the signed jws object
            JWSObject jwsObject;
            jwsObject = JWSObject.parse(chainData.get(chainData.size() - 1).asText());

            // Read the JWS payload
            JsonNode payload = OBJECT_MAPPER.readTree(jwsObject.getPayload().toBytes());

            // Check the identityPublicKey is there
            if (payload.get("identityPublicKey").getNodeType() != JsonNodeType.STRING) {
                throw new AssertionError("Missing identity public key!");
            }

            // Create an ECPublicKey from the identityPublicKey
            ECPublicKey identityPublicKey = EncryptionUtils.generateKey(payload.get("identityPublicKey").textValue());

            // Get the skin data to validate the JWS token
            JWSObject skinData = JWSObject.parse(packet.getSkinData().toString());
            if (skinData.verify(new DefaultJWSVerifierFactory().createJWSVerifier(skinData.getHeader(), identityPublicKey))) {
                // Make sure the client sent over the username, xuid and other info
                if (payload.get("extraData").getNodeType() != JsonNodeType.OBJECT) {
                    throw new AssertionError("Missing client data");
                }

                // Fetch the client data
                JsonNode extraData = payload.get("extraData");

                AuthData authData = new AuthData(
                        extraData.get("displayName").asText(),
                        UUID.fromString(extraData.get("identity").asText()),
                        extraData.get("XUID").asText()
                );

                // Create a new player and add it to the players list
                player = new Player(authData, session);
                masterServer.getPlayers().add(player);

                player.setChainData(chainData);

                // Store the full client data
                player.setClientData(OBJECT_MAPPER.convertValue(OBJECT_MAPPER.readTree(skinData.getPayload().toBytes()), BedrockClientData.class));
                player.getClientData().setOriginalString(packet.getSkinData().toString());

                // Tell the client we have logged in successfully
                PlayStatusPacket playStatusPacket = new PlayStatusPacket();
                playStatusPacket.setStatus(PlayStatusPacket.Status.LOGIN_SUCCESS);
                session.sendPacket(playStatusPacket);

                // Tell the client there are no resourcepacks
                ResourcePacksInfoPacket resourcePacksInfo = new ResourcePacksInfoPacket();
                session.sendPacket(resourcePacksInfo);
            } else {
                throw new AssertionError("Invalid identity public key!");
            }
        } catch (Exception e) {
            // Disconnect the client
            session.disconnect("disconnectionScreen.internalError.cantConnect");
            throw new AssertionError("Failed to login", e);
        }

        return false;
    }

    @Override
    public boolean handle(ResourcePackClientResponsePacket packet) {
        switch (packet.getStatus()) {
            case COMPLETED:
                masterServer.getLogger().info("Logged in " + player.getAuthData().name() + " (" + player.getAuthData().xuid() + ", " + player.getAuthData().uuid() + ")");

                ProxyAuthenticationTask task = (ProxyAuthenticationTask) GeyserImpl.getInstance()
                        .getPendingMicrosoftAuthentication().getTask(player.getAuthData().xuid());
                if (task != null && task.getAuthentication().isDone()) {
                    String address = task.getServer();
                    int port = task.getPort();
                    player.setCurrentServer(new Server(address, port, true, false));
                    GeyserProxySession session = player.createGeyserSession(false);
                    session.remoteServer(player.getCurrentServer());

                    session.onMicrosoftLoginComplete(task);
                } else {
                    player.sendStartGame();
                }
                break;
            case HAVE_ALL_PACKS:
                ResourcePackStackPacket stack = new ResourcePackStackPacket();
                stack.setExperimentsPreviouslyToggled(false);
                stack.setForcedToAccept(false);
                stack.setGameVersion("*");

                if (!Registries.ITEMS.forVersion(session.getPacketCodec().getProtocolVersion()).getComponentItemData().isEmpty()) {
                    // Allow custom items to work
                    stack.getExperiments().add(new ExperimentData("data_driven_items", true));
                }

                if (session.getPacketCodec().getProtocolVersion() <= Bedrock_v471.V471_CODEC.getProtocolVersion()) {
                    // Allow extended world height in the overworld to work for pre-1.18 clients
                    stack.getExperiments().add(new ExperimentData("caves_and_cliffs", true));
                }

                session.sendPacket(stack);
                break;
            default:
                session.disconnect("disconnectionScreen.resourcePack");
                break;
        }

        return true;
    }

    @Override
    public boolean handle(SetLocalPlayerAsInitializedPacket packet) {
        masterServer.getLogger().debug("Player initialized: " + player.getAuthData().name());

        if (player.getCurrentServer() != null) {
            // Player is already logged in via delayed Microsoft authentication
            return false;
        }

        // Handle the virtual host if specified
        GeyserConnectConfig.VirtualHostSection vhost = MasterServer.getInstance().getGeyserConnectConfig().getVhost();
        if (vhost.isEnabled()) {
            String domain = player.getClientData().getServerAddress().split(":")[0];
            if (!domain.equals(vhost.getBaseDomain()) && domain.endsWith("." + vhost.getBaseDomain())) {
                String address = "";
                int port = 25565;
                boolean online = true;

                // Parse the address used
                String[] domainParts = domain.replaceFirst("\\." + vhost.getBaseDomain() + "$", "").split("\\._");
                for (int i = 0; i < domainParts.length; i++) {
                    String part = domainParts[i];
                    if (i == 0) {
                        address = part;
                    } else if (part.startsWith("p")) {
                        port = Integer.parseInt(part.substring(1));
                    } else if (part.startsWith("o")) {
                        online = false;
                    }
                }

                // They didn't specify an address so disconnect them
                if (address.startsWith("_")) {
                    session.disconnect("disconnectionScreen.invalidIP");
                    return false;
                }

                // Log the virtual host usage
                masterServer.getLogger().info(player.getAuthData().name() + " is using virtualhost: " + address + ":" + port + (!online ? " (offline)" : ""));

                // Send the player to the wanted server
                player.sendToServer(new Server(address, port, online, false));

                return false;
            }
        }

        String message = "";
        try {
            File messageFile = GeyserConnectFileUtils.fileOrCopiedFromResource(new File(MasterServer.getInstance().getGeyserConnectConfig().getWelcomeFile()), "welcome.txt", (x) -> x);
            message = new String(FileUtils.readAllBytes(messageFile));
        } catch (IOException ignored) { }

        if (!message.trim().isEmpty()) {
            player.sendWindow(FormID.WELCOME, UIHandler.getMessageWindow(message));
        } else {
            player.sendWindow(FormID.MAIN, UIHandler.getMainMenu());
        }

        return false;
    }

    @Override
    public boolean handle(ModalFormResponsePacket packet) {
        // Make sure the form is valid
        FormID id = FormID.fromId(packet.getFormId());
        if (id != player.getCurrentWindowId())
            return false;

        // Fetch the form and parse the response
        Form window = player.getCurrentWindow();
        FormResponse response = window.parseResponse(packet.getFormData() == null ? null : packet.getFormData().trim());

        // Resend the form if they closed it
        if (!response.isCorrect() && !id.isHandlesNull()) {
            player.resendWindow();
        } else {
            // Send the response to the correct response function
            switch (id) {
                case WELCOME:
                    player.sendWindow(FormID.MAIN, UIHandler.getMainMenu());
                    break;

                case MAIN:
                    UIHandler.handleMainMenuResponse(player, (SimpleFormResponse) response);
                    break;

                case LIST_SERVERS:
                    UIHandler.handleServerListResponse(player, (SimpleFormResponse) response);
                    break;

                case DIRECT_CONNECT:
                    UIHandler.handleDirectConnectResponse(player, (CustomFormResponse) response);
                    break;

                case EDIT_SERVERS:
                    UIHandler.handleEditServerListResponse(player, (SimpleFormResponse) response);
                    break;

                case ADD_SERVER:
                    UIHandler.handleAddServerResponse(player, (CustomFormResponse) response);
                    break;

                case SERVER_OPTIONS:
                    UIHandler.handleServerOptionsResponse(player, (SimpleFormResponse) response);
                    break;

                case REMOVE_SERVER:
                    UIHandler.handleServerRemoveResponse(player, (SimpleFormResponse) response);
                    break;

                case EDIT_SERVER:
                    UIHandler.handleEditServerResponse(player, (CustomFormResponse) response);
                    break;

                default:
                    player.resendWindow();
                    break;
            }
        }

        return true;
    }

    @Override
    public boolean handle(NetworkStackLatencyPacket packet) {
        // This is to fix a bug in the client where it doesn't load form images
        UpdateAttributesPacket updateAttributesPacket = new UpdateAttributesPacket();
        updateAttributesPacket.setRuntimeEntityId(1);
        List<AttributeData> attributes = Collections.singletonList(GeyserAttributeType.EXPERIENCE_LEVEL.getAttribute(0f));
        updateAttributesPacket.setAttributes(attributes);

        // Doesn't work 100% of the time but fixes it most of the time
        MasterServer.getInstance().getGeneralThreadPool().schedule(() -> session.sendPacket(updateAttributesPacket), 500, TimeUnit.MILLISECONDS);

        return false;
    }
}
