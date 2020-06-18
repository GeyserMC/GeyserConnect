/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 *  @author GeyserMC
 *  @link https://github.com/GeyserMC/GeyserConnect
 *
 */

package org.geysermc.connect;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.Attribute;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.*;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import org.geysermc.common.window.FormWindow;
import org.geysermc.common.window.response.CustomFormResponse;
import org.geysermc.common.window.response.SimpleFormResponse;
import org.geysermc.connect.ui.FormID;
import org.geysermc.connect.ui.UIHandler;
import org.geysermc.connect.utils.Player;
import org.geysermc.connector.entity.attribute.AttributeType;
import org.geysermc.connector.utils.AttributeUtils;

import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PacketHandler implements BedrockPacketHandler {

    private BedrockServerSession session;
    private MasterServer masterServer;

    private Player player;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);;

    public PacketHandler(BedrockServerSession session, MasterServer masterServer) {
        this.session = session;
        this.masterServer = masterServer;

        session.addDisconnectHandler((reason) -> disconnect(reason));
    }

    public void disconnect(DisconnectReason reason) {
        if (player != null) {
            masterServer.getLogger().info(player.getDisplayName() + " has disconnected from the master server (" + reason + ")");
            masterServer.getStorageManager().saveServers(player);

            if (player.getCurrentServer() != null && player.getCurrentServer().isBedrock()) {
                masterServer.getPlayers().remove(player);
            }
        }
    }

    @Override
    public boolean handle(LoginPacket packet) {
        masterServer.getLogger().debug("Login: " + packet.toString());

        // Check the protocol version is correct
        int protocol = packet.getProtocolVersion();
        if (protocol != MasterServer.CODEC.getProtocolVersion()) {
            PlayStatusPacket status = new PlayStatusPacket();
            if (protocol > MasterServer.CODEC.getProtocolVersion()) {
                status.setStatus(PlayStatusPacket.Status.FAILED_SERVER);
            } else {
                status.setStatus(PlayStatusPacket.Status.FAILED_CLIENT);
            }
            session.sendPacket(status);
        }

        // Set the session codec
        session.setPacketCodec(MasterServer.CODEC);

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

                // Create a new player and add it to the players list
                player = new Player(extraData, session);
                masterServer.getPlayers().put(player.getXuid(), player);

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
                masterServer.getLogger().info("Logged in " + player.getDisplayName() + " (" + player.getXuid() + ", " + player.getIdentity() + ")");
                player.sendStartGame();
                break;
            case HAVE_ALL_PACKS:
                ResourcePackStackPacket stack = new ResourcePackStackPacket();
                stack.setExperimental(false);
                stack.setForcedToAccept(false);
                stack.setGameVersion("*");
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
        masterServer.getLogger().debug("Player initialized: " + player.getDisplayName());

        player.sendWindow(FormID.MAIN, UIHandler.getServerList(player.getServers()));;

        return false;
    }

    @Override
    public boolean handle(ModalFormResponsePacket packet) {
        // Make sure the form is valid
        FormID id = FormID.fromId(packet.getFormId());
        if (id != player.getCurrentWindowId())
            return false;

        // Fetch the form and parse the response
        FormWindow window = player.getCurrentWindow();
        window.setResponse(packet.getFormData().trim());

        // Resend the form if they closed it
        if (window.getResponse() == null && !id.isHandlesNull()) {
            player.resendWindow();
        } else {
            // Send the response to the correct response function
            switch (id) {
                case MAIN:
                    UIHandler.handleServerListResponse(player, (SimpleFormResponse) window.getResponse());
                    break;

                case DIRECT_CONNECT:
                    UIHandler.handleDirectConnectResponse(player, (CustomFormResponse) window.getResponse());
                    break;

                case EDIT_SERVERS:
                    UIHandler.handleEditServerListResponse(player, (SimpleFormResponse) window.getResponse());
                    break;

                case ADD_SERVER:
                    UIHandler.handleAddServerResponse(player, (CustomFormResponse) window.getResponse());
                    break;

                case SERVER_OPTIONS:
                    UIHandler.handleServerOptionsResponse(player, (SimpleFormResponse) window.getResponse());
                    break;

                case REMOVE_SERVER:
                    UIHandler.handleServerRemoveResponse(player, (SimpleFormResponse) window.getResponse());
                    break;

                case EDIT_SERVER:
                    UIHandler.handleEditServerResponse(player, (CustomFormResponse) window.getResponse());
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
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(AttributeUtils.getBedrockAttribute(AttributeType.EXPERIENCE_LEVEL.getAttribute(0f)));
        updateAttributesPacket.setAttributes(attributes);

        // Doesn't work 100% of the time but fixes it most of the time
        MasterServer.getInstance().getGeneralThreadPool().schedule(() -> session.sendPacket(updateAttributesPacket), 500, TimeUnit.MILLISECONDS);

        return false;
    }
}
