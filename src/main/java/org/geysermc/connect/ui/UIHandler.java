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

package org.geysermc.connect.ui;

import org.geysermc.common.window.CustomFormBuilder;
import org.geysermc.common.window.CustomFormWindow;
import org.geysermc.common.window.FormWindow;
import org.geysermc.common.window.SimpleFormWindow;
import org.geysermc.common.window.button.FormButton;
import org.geysermc.common.window.button.FormImage;
import org.geysermc.common.window.component.InputComponent;
import org.geysermc.common.window.response.CustomFormResponse;
import org.geysermc.common.window.response.SimpleFormResponse;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;

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

        // Add a button for each global server
        for (Server server : MasterServer.getInstance().getGeyserConnectConfig().getServers()) {
            // These images would be better if there was a default to fall back on
            // But that would require a web api as bedrock doesn't support doing that
            window.getButtons().add(new FormButton(server.toString(), new FormImage(FormImage.FormImageType.URL, "https://eu.mc-api.net/v3/server/favicon/" + server.getAddress() + ":" + server.getPort() + ".png")));
        }

        // Add a button for each personal server
        if (MasterServer.getInstance().getGeyserConnectConfig().getCustomServers().isEnabled()) {
            for (Server server : servers) {
                window.getButtons().add(new FormButton(server.toString(), new FormImage(FormImage.FormImageType.URL, "https://eu.mc-api.net/v3/server/favicon/" + server.getAddress() + ":" + server.getPort() + ".png")));
            }

            window.getButtons().add(new FormButton("Edit servers"));
            window.getButtons().add(new FormButton("Direct connect"));
        }

        window.getButtons().add(new FormButton("Disconnect"));

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
     * Create a list of servers for the client to edit
     *
     * @param servers A list of {@link Server} objects
     * @return A {@link SimpleFormWindow} object
     */
    public static FormWindow getEditServerList(List<Server> servers) {
        SimpleFormWindow window = new SimpleFormWindow("Edit Servers", "Select a server to edit");

        // Add a button for each personal server
        for (Server server : servers) {
            window.getButtons().add(new FormButton(server.toString(), new FormImage(FormImage.FormImageType.URL, "https://eu.mc-api.net/v3/server/favicon/" + server.getAddress() + ":" + server.getPort() + ".png")));
        }

        return window;
    }

    /**
     * Handle the server list response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleServerListResponse(Player player, SimpleFormResponse data) {
        List<Server> servers = new ArrayList<>(MasterServer.getInstance().getGeyserConnectConfig().getServers());
        servers.addAll(player.getServers());

        // Cant be done in a switch as we need to calculate the last 2 buttons

        if ((!MasterServer.getInstance().getGeyserConnectConfig().getCustomServers().isEnabled() && data.getClickedButtonId() == servers.size()) || data.getClickedButtonId() == servers.size() + 2) {
            player.getSession().disconnect("disconnect.disconnected");
        } else if (data.getClickedButtonId() == servers.size()) {
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
        } else if (data.getClickedButtonId() == servers.size() + 1) {
            player.sendWindow(FormID.DIRECT_CONNECT, getDirectConnect());
        } else {
            // Get the server
            Server server = servers.get(data.getClickedButtonId());

            player.sendToServer(server);
        }
    }

    /**
     * Handle the direct connect response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleDirectConnectResponse(Player player, CustomFormResponse data) {
        // Take them back to the main menu if they close the direct connect window
        if (data == null) {
            player.sendWindow(FormID.MAIN, getServerList(player.getServers()));;
            return;
        }

        try {
            String address = data.getInputResponses().get(0);
            int port = Integer.valueOf(data.getInputResponses().get(1));

            // Make sure we got an address and port
            if (address == null || "".equals(address) || port <= 0 || port >= 65535) {
                player.resendWindow();
                return;
            }

            player.sendToServer(new Server(address, port));
        } catch (NumberFormatException e) {
            player.resendWindow();
        }
    }

    /**
     * Handle the edit server list response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleEditServerListResponse(Player player, SimpleFormResponse data) {
        // Take them back to the main menu if they close the edit server list window
        if (data == null) {
            player.sendWindow(FormID.MAIN, getServerList(player.getServers()));;
            return;
        }

        // Just redisplay the form for now
        player.resendWindow();
    }
}
