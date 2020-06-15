package org.geysermc.multi.ui;

import com.nukkitx.protocol.bedrock.packet.TransferPacket;
import org.geysermc.common.window.FormWindow;
import org.geysermc.common.window.SimpleFormWindow;
import org.geysermc.common.window.button.FormButton;
import org.geysermc.common.window.button.FormImage;
import org.geysermc.common.window.response.SimpleFormResponse;
import org.geysermc.multi.MasterServer;
import org.geysermc.multi.utils.Player;
import org.geysermc.multi.utils.Server;

import java.util.List;

public class UIHandler {
    public static FormWindow getServerListFormPacket(List<Server> servers) {
        SimpleFormWindow window = new SimpleFormWindow("Servers", "");

        for (Server server : servers) {
            window.getButtons().add(new FormButton(server.getAddress(), new FormImage(FormImage.FormImageType.URL, "https://eu.mc-api.net/v3/server/favicon/" + server.getAddress() + ":" + server.getPort() + ".png")));
        }

        return window;
    }

    public static FormWindow getWaitingScreen(Server server) {
        SimpleFormWindow window = new SimpleFormWindow("Servers", "Please wait while we connect you to " + server.toString());
        return window;
    }

    public static void handleServerListResponse(Player player, SimpleFormResponse data) {
        MasterServer.getInstance().getLogger().debug(data.getClickedButton().getText());

        // Get the server
        Server server = player.getServers().get(data.getClickedButtonId());

        // Tell the user we are connecting them
        player.sendWindow(FormID.CONNECTING, getWaitingScreen(server));

        // Create the geyser instance if its not already running
        MasterServer.getInstance().createGeyserProxy();

        // Send the user over to the serverty
        player.setCurrentServer(server);
        player.connectToProxy();
    }
}
