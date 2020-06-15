package org.geysermc.multi.ui;

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
    /**
     * Create a list of servers for the client based on the passed servers list
     *
     * @param servers A list of {@link Server} objects
     * @return A {@link SimpleFormWindow} object
     */
    public static FormWindow getServerListFormPacket(List<Server> servers) {
        SimpleFormWindow window = new SimpleFormWindow("Servers", "");

        // Add a button for each server with the server icon as the image
        for (Server server : servers) {
            window.getButtons().add(new FormButton(server.toString(), new FormImage(FormImage.FormImageType.URL, "https://eu.mc-api.net/v3/server/favicon/" + server.getAddress() + ":" + server.getPort() + ".png")));
        }

        return window;
    }

    /**
     * Create a simple connecting message form
     *
     * @param server The server info to display
     * @return A {@link SimpleFormWindow} object
     */
    public static FormWindow getWaitingScreen(Server server) {
        SimpleFormWindow window = new SimpleFormWindow("Connecting", "Please wait while we connect you to " + server.toString());
        return window;
    }

    /**
     * Handle the server list response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleServerListResponse(Player player, SimpleFormResponse data) {
        // Get the server
        Server server = player.getServers().get(data.getClickedButtonId());

        // Tell the user we are connecting them
        // this wont show up in alot of cases as the client connects quite quickly
        player.sendWindow(FormID.CONNECTING, getWaitingScreen(server));

        // Create the Geyser instance if its not already running
        MasterServer.getInstance().createGeyserProxy();

        // Send the user over to the server
        player.setCurrentServer(server);
        player.connectToProxy();
    }
}
