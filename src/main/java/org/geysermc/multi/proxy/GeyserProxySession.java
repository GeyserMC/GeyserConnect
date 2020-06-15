package org.geysermc.multi.proxy;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.multi.MasterServer;
import org.geysermc.multi.utils.Player;

public class GeyserProxySession extends GeyserSession {

    private final GeyserConnector connector;
    private final BedrockServerSession bedrockServerSession;

    public GeyserProxySession(GeyserConnector connector, BedrockServerSession bedrockServerSession) {
        super(connector, bedrockServerSession);
        this.connector = connector;
        this.bedrockServerSession = bedrockServerSession;
    }

    public void authenticate(String username, String password) {
        // Get the player based on the connection address
        Player player = MasterServer.getInstance().getPlayers().get(bedrockServerSession.getAddress());
        if (player != null && player.getCurrentServer() != null) {
            // Set the remote server info for the player
            connector.getRemoteServer().setAddress(player.getCurrentServer().getAddress());
            connector.getRemoteServer().setPort(player.getCurrentServer().getPort());
            super.authenticate(username, password);
        }else{
            // Disconnect the player if they haven't picked a server on the master server list
            bedrockServerSession.disconnect("Please connect to the master server and pick a server first!");
        }
    }
}
