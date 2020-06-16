package org.geysermc.multi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.*;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import org.geysermc.common.window.FormWindow;
import org.geysermc.common.window.response.CustomFormResponse;
import org.geysermc.common.window.response.SimpleFormResponse;
import org.geysermc.multi.ui.FormID;
import org.geysermc.multi.ui.UIHandler;
import org.geysermc.multi.utils.Player;

import java.io.IOException;
import java.security.interfaces.ECPublicKey;

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
                masterServer.getPlayers().put(session.getAddress(), player);

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
        if (window.getResponse() == null && id != FormID.DIRECT_CONNECT) {
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

                default:
                    player.resendWindow();
                    break;
            }
        }

        return true;
    }
}
