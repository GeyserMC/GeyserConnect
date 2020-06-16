package org.geysermc.multi.ui;

import org.geysermc.common.window.CustomFormBuilder;
import org.geysermc.common.window.CustomFormWindow;
import org.geysermc.common.window.FormWindow;
import org.geysermc.common.window.SimpleFormWindow;
import org.geysermc.common.window.button.FormButton;
import org.geysermc.common.window.button.FormImage;
import org.geysermc.common.window.component.InputComponent;
import org.geysermc.common.window.response.CustomFormResponse;
import org.geysermc.common.window.response.SimpleFormResponse;
import org.geysermc.multi.MasterServer;
import org.geysermc.multi.utils.Player;
import org.geysermc.multi.utils.Server;

import java.util.ArrayList;
import java.util.List;

public class UIHandler {
    /**
     * Create a list of servers for the client based on the passed servers list
     *
     * @param servers A list of {@link Server} objects
     * @return A {@link SimpleFormWindow} object
     */
    public static FormWindow getServerList(List<Server> servers) {
        SimpleFormWindow window = new SimpleFormWindow("Servers", "");

        window.getButtons().add(new FormButton("Direct connect"));
        window.getButtons().add(new FormButton("Edit servers"));

        // Add a button for each global server
        for (Server server : MasterServer.getInstance().getGeyserMultiConfig().getServers()) {
            window.getButtons().add(new FormButton(server.toString(), new FormImage(FormImage.FormImageType.URL, "https://eu.mc-api.net/v3/server/favicon/" + server.getAddress() + ":" + server.getPort() + ".png")));
        }

        // Add a button for each personal server
        if (MasterServer.getInstance().getGeyserMultiConfig().getCustomServers().isEnabled()) {
            for (Server server : servers) {
                window.getButtons().add(new FormButton(server.toString(), new FormImage(FormImage.FormImageType.URL, "https://eu.mc-api.net/v3/server/favicon/" + server.getAddress() + ":" + server.getPort() + ".png")));
            }
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
     * Create a direct connect form
     *
     * @return A {@link SimpleFormWindow} object
     */
    public static FormWindow getDirectConnect() {
        CustomFormWindow window = new CustomFormBuilder("Direct Connect")
                .addComponent(new InputComponent("IP", "play.cubecraft.net", ""))
                .addComponent(new InputComponent("Port", "25565", "25565"))
                .build();
        return window;
    }

    /**
     * Handle the server list response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleServerListResponse(Player player, SimpleFormResponse data) {
        switch (data.getClickedButtonId()) {
            case 0:
                player.sendWindow(FormID.DIRECT_CONNECT, getDirectConnect());
                break;
            case 1:
                break;
            default:
                // Get the server
                List<Server> servers = new ArrayList<>(MasterServer.getInstance().getGeyserMultiConfig().getServers());
                servers.addAll(player.getServers());
                Server server = servers.get(data.getClickedButtonId() - 2);

                player.sendToServer(server);
                break;
        }
    }

    public static void handleDirectConnectResponse(Player player, CustomFormResponse data) {
        // Take them back to the main menu if they close the direct connect window
        if (data == null) {
            player.sendWindow(FormID.MAIN, getServerList(player.getServers()));;
            return;
        }

        player.sendToServer(new Server(data.getInputResponses().get(0), Integer.valueOf(data.getInputResponses().get(1))));
    }
}
