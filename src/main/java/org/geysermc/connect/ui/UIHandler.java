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
import org.geysermc.common.window.component.LabelComponent;
import org.geysermc.common.window.component.SliderComponent;
import org.geysermc.common.window.component.ToggleComponent;
import org.geysermc.common.window.response.CustomFormResponse;
import org.geysermc.common.window.response.SimpleFormResponse;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Logger;
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
     * @return A {@link CustomFormWindow} object
     */
    public static FormWindow getDirectConnect() {
        CustomFormWindow window = new CustomFormBuilder("Direct Connect")
                .addComponent(new InputComponent("IP", "play.cubecraft.net", ""))
                .addComponent(new InputComponent("Port", "25565", "25565"))
                .addComponent(new ToggleComponent("Online mode", true))
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

        window.getButtons().add(new FormButton("Add server"));
        window.getButtons().add(new FormButton("Back"));

        return window;
    }

    /**
     * Create a add server form
     *
     * @return A {@link CustomFormWindow} object
     */
    public static FormWindow getAddServer() {
        CustomFormWindow window = new CustomFormBuilder("Add Server")
                .addComponent(new InputComponent("IP", "play.cubecraft.net", ""))
                .addComponent(new InputComponent("Port", "25565", "25565"))
                .addComponent(new ToggleComponent("Online mode", true))
                .addComponent(new ToggleComponent("Bedrock server", false))
                .build();
        return window;
    }

    /**
     * Create a server options form
     *
     * @param server A {@link Server} object to show options for
     * @return A {@link SimpleFormWindow} object
     */
    public static FormWindow getServerOptions(Server server) {
        SimpleFormWindow window = new SimpleFormWindow("Server Options", server.toString());

        window.getButtons().add(new FormButton("Edit"));
        window.getButtons().add(new FormButton("Remove"));
        window.getButtons().add(new FormButton("Back"));

        return window;
    }

    /**
     * Create a remove server form
     *
     * @param server A {@link Server} object to remove
     * @return A {@link SimpleFormWindow} object
     */
    public static FormWindow getRemoveServer(Server server) {
        SimpleFormWindow window = new SimpleFormWindow("Remove Server", "Are you sure you want to remove server: " + server.toString());

        window.getButtons().add(new FormButton("Remove"));
        window.getButtons().add(new FormButton("Cancel"));

        return window;
    }

    /**
     * Create a edit server form
     *
     * @param server A {@link Server} object to edit
     * @return A {@link CustomFormWindow} object
     */
    public static FormWindow getEditServer(int serverIndex, Server server) {
        String port = String.valueOf(server.getPort());
        CustomFormWindow window = new CustomFormBuilder("Edit Server")
                .addComponent(new LabelComponent("Server at index: " + serverIndex))
                .addComponent(new InputComponent("IP", server.getAddress(), server.getAddress()))
                .addComponent(new InputComponent("Port", port, port))
                .addComponent(new ToggleComponent("Online mode", server.isOnline()))
                .addComponent(new ToggleComponent("Bedrock server", server.isBedrock()))
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
        List<Server> servers = new ArrayList<>(MasterServer.getInstance().getGeyserConnectConfig().getServers());
        servers.addAll(player.getServers());

        // Cant be done in a switch as we need to calculate the last 2 buttons

        if ((!MasterServer.getInstance().getGeyserConnectConfig().getCustomServers().isEnabled() && data.getClickedButtonId() == servers.size()) || data.getClickedButtonId() == servers.size() + 2) {
            player.getSession().disconnect("disconnectionScreen.disconnected");
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
            player.sendWindow(FormID.MAIN, getServerList(player.getServers()));
            return;
        }

        try {
            String address = data.getInputResponses().get(0);
            int port = Integer.valueOf(data.getInputResponses().get(1));
            boolean online = data.getToggleResponses().get(2);

            // Make sure we got an address
            if (address == null || "".equals(address)) {
                player.sendWindow(FormID.MAIN, getServerList(player.getServers()));
                return;
            }

            // Make sure we got a valid port
            if (port <= 0 || port >= 65535) {
                player.resendWindow();
                return;
            }

            player.sendToServer(new Server(address, port, online));
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
            player.sendWindow(FormID.MAIN, getServerList(player.getServers()));
            return;
        }

        List<Server> servers = player.getServers();
        if (data.getClickedButtonId() == servers.size()) {
            player.sendWindow(FormID.ADD_SERVER, getAddServer());
        } else if (data.getClickedButtonId() == servers.size() + 1) {
            player.sendWindow(FormID.MAIN, getServerList(player.getServers()));
        } else {
            Server server = player.getServers().get(data.getClickedButtonId());
            player.sendWindow(FormID.SERVER_OPTIONS, getServerOptions(server));
        }
    }

    /**
     * Handle the add server response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleAddServerResponse(Player player, CustomFormResponse data) {
        // Take them back to the edit server list menu if they close the add server window
        if (data == null) {
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
            return;
        }

        try {
            String address = data.getInputResponses().get(0);
            int port = Integer.valueOf(data.getInputResponses().get(1));
            boolean online = data.getToggleResponses().get(2);
            boolean bedrock = data.getToggleResponses().get(3);

            // Make sure we got an address
            if (address == null || "".equals(address)) {
                player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
                return;
            }

            // Make sure we got a valid port
            if (port <= 0 || port >= 65535) {
                player.resendWindow();
                return;
            }

            player.getServers().add(new Server(address, port, online, bedrock));

            // Send them back to the edit screen
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
        } catch (NumberFormatException e) {
            player.resendWindow();
        }
    }

    /**
     * Handle the server options response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleServerOptionsResponse(Player player, SimpleFormResponse data) {
        // Take them back to the main menu if they close the edit server list window
        if (data == null) {
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
            return;
        }

        SimpleFormWindow window = (SimpleFormWindow) player.getCurrentWindow();
        Server selectedServer = null;
        for (Server server : player.getServers()) {
            if (server.toString().equals(window.getContent())) {
                selectedServer = server;
                break;
            }
        }

        if (selectedServer == null) {
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
            return;
        }

        switch (data.getClickedButtonId()) {
            case 0:
                player.sendWindow(FormID.EDIT_SERVER, getEditServer(player.getServers().indexOf(selectedServer), selectedServer));
                break;

            case 1:
                player.sendWindow(FormID.REMOVE_SERVER, getRemoveServer(selectedServer));
                break;

            default:
                player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
                break;
        }
    }

    /**
     * Handle the server remove response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleServerRemoveResponse(Player player, SimpleFormResponse data) {
        SimpleFormWindow window = (SimpleFormWindow) player.getCurrentWindow();
        String serverName = window.getContent().split(":")[1].trim();
        Server selectedServer = null;
        for (Server server : player.getServers()) {
            if (server.toString().equals(serverName)) {
                selectedServer = server;
                break;
            }
        }

        if (selectedServer == null) {
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
            return;
        }

        if (data.getClickedButtonId() == 0) {
            player.getServers().remove(selectedServer);
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
        } else {
            player.sendWindow(FormID.SERVER_OPTIONS, getServerOptions(selectedServer));
        }
    }

    /**
     * Handle the edit server response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleEditServerResponse(Player player, CustomFormResponse data) {
        // Take them back to the edit server list menu if they close the add server window
        if (data == null) {
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
            return;
        }

        try {
            int serverIndex = Integer.valueOf(data.getLabelResponses().get(0).split(":")[1].trim());

            String address = data.getInputResponses().get(1);
            int port = Integer.valueOf(data.getInputResponses().get(2));
            boolean online = data.getToggleResponses().get(3);
            boolean bedrock = data.getToggleResponses().get(4);

            // Make sure we got an address
            if (address == null || "".equals(address)) {
                player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
                return;
            }

            // Make sure we got a valid port
            if (port <= 0 || port >= 65535) {
                player.resendWindow();
                return;
            }

            player.getServers().set(serverIndex, new Server(address, port, online, bedrock));

            // Send them back to the edit screen
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
        } catch (NumberFormatException e) {
            player.resendWindow();
        }
    }
}
