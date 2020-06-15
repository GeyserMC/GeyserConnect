package org.geysermc.multi.proxy;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.ConnectorServerEventHandler;
import org.geysermc.connector.network.UpstreamPacketHandler;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.multi.MasterServer;
import org.geysermc.multi.utils.Player;

public class ProxyConnectorServerEventHandler extends ConnectorServerEventHandler {
    private final GeyserConnector connector;

    public ProxyConnectorServerEventHandler(GeyserConnector connector) {
        super(connector);
        this.connector = connector;
        MasterServer.getInstance().getLogger().debug("Registered custom ConnectorServerEventHandler");
    }

    @Override
    public void onSessionCreation(BedrockServerSession bedrockServerSession) {
        Player player = MasterServer.getInstance().getPlayers().get(bedrockServerSession.getAddress());
        if (player == null) {
            bedrockServerSession.disconnect("Please connect to the master server and pick a server first!");
            return;
        }

        super.onSessionCreation(bedrockServerSession);

        bedrockServerSession.setPacketHandler(new UpstreamPacketHandler(connector, new GeyserProxySession(connector, bedrockServerSession)));
        bedrockServerSession.addDisconnectHandler(disconnectReason -> {
            MasterServer.getInstance().getLogger().debug("Player disconnected from geyser proxy: " + player.getDisplayName() + " (" + disconnectReason + ")");
            MasterServer.getInstance().getPlayers().remove(bedrockServerSession.getAddress());
        });
    }
}
