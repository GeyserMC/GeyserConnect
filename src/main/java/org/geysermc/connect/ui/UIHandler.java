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

package org.geysermc.connect.ui;

import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;
import org.geysermc.connect.utils.ServerCategory;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.Form;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.component.InputComponent;
import org.geysermc.cumulus.component.LabelComponent;
import org.geysermc.cumulus.component.ToggleComponent;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.cumulus.util.FormImage;

import java.util.List;

public class UIHandler {

    /**
     * Create a list of servers for the client based on the passed servers list
     *
     * @return A {@link SimpleForm} object
     */
    public static Form getMainMenu() {
        SimpleForm.Builder window = SimpleForm.builder().title("Main Menu");

        window.button("Official Servers");
        window.button("Geyser Servers");

        // Add a buttons for custom servers
        if (MasterServer.getInstance().getGeyserConnectConfig().getCustomServers().isEnabled()) {
            window.button("Custom Servers");
            window.button("Direct connect");
        }

        window.button("Disconnect");

        return window.build();
    }

    /**
     * Create a list of servers for the client based on the passed servers list
     *
     * @param servers A list of {@link Server} objects
     * @param category The category of the current list
     * @return A {@link SimpleForm} object
     */
    public static Form getServerList(List<Server> servers, ServerCategory category) {
        SimpleForm.Builder window = SimpleForm.builder().title(category.getTitle() + " Servers");

        // Add a button for each global server
        for (Server server : servers) {
            // These images would be better if there was a default to fall back on
            // But that would require a web api as bedrock doesn't support doing that
            window.button(server.toString(), server.getFormImage());
        }

        // Add a button for editing
        if (category == ServerCategory.CUSTOM) {
            window.button("Edit servers");
        }

        window.button("Back");

        return window.build();
    }

    /**
     * Create a direct connect form
     *
     * @return A {@link CustomForm} object
     */
    public static Form getDirectConnect() {
        return CustomForm.builder().title("Direct Connect")
                .component(InputComponent.of("IP", "play.cubecraft.net"))
                .component(InputComponent.of("Port", "25565", "25565"))
                .component(ToggleComponent.of("Online mode", true))
                .component(ToggleComponent.of("Bedrock/Geyser server", false))
                .build();
    }

    /**
     * Create a list of servers for the client to edit
     *
     * @param servers A list of {@link Server} objects
     * @return A {@link SimpleForm} object
     */
    public static Form getEditServerList(List<Server> servers) {
        SimpleForm.Builder window = SimpleForm.builder().title("Edit Servers").content("Select a server to edit");

        // Add a button for each personal server
        for (Server server : servers) {
            window.button(server.toString(), FormImage.of(FormImage.Type.URL,
                    "https://eu.mc-api.net/v3/server/favicon/" + server.getAddress() + ":" + server.getPort() + ".png"));
        }

        window.button("Add server");
        window.button("Back");

        return window.build();
    }

    /**
     * Create a add server form
     *
     * @return A {@link CustomForm} object
     */
    public static Form getAddServer() {
        return CustomForm.builder().title("Add Server")
                .component(InputComponent.of("IP", "play.cubecraft.net"))
                .component(InputComponent.of("Port", "25565", "25565"))
                .component(ToggleComponent.of("Online mode", true))
                .component(ToggleComponent.of("Bedrock/Geyser server", false))
                .build();
    }

    /**
     * Create a server options form
     *
     * @param server A {@link Server} object to show options for
     * @return A {@link SimpleForm} object
     */
    public static Form getServerOptions(Server server) {
        SimpleForm.Builder window = SimpleForm.builder().title("Server Options").content(server.toString());

        window.button("Edit");
        window.button("Remove");
        window.button("Back");

        return window.build();
    }

    /**
     * Create a remove server form
     *
     * @param server A {@link Server} object to remove
     * @return A {@link SimpleForm} object
     */
    public static Form getRemoveServer(Server server) {
        return SimpleForm.builder()
                .title("Remove Server")
                .content("Are you sure you want to remove server: " + server)
                .button("Remove")
                .button("Cancel")
                .build();
    }

    /**
     * Create a edit server form
     *
     * @param server A {@link Server} object to edit
     * @return A {@link CustomForm} object
     */
    public static Form getEditServer(int serverIndex, Server server) {
        String port = String.valueOf(server.getPort());
        return CustomForm.builder()
                .component(LabelComponent.of("Server at index: " + serverIndex))
                .component(InputComponent.of("IP", server.getAddress(), server.getAddress()))
                .component(InputComponent.of("Port", port, port))
                .component(ToggleComponent.of("Online mode", server.isOnline()))
                .component(ToggleComponent.of("Bedrock/Geyser server", server.isBedrock()))
                .build();
    }

    /**
     * Show a basic form window with a message
     *
     * @param message The message to display
     * @return A {@link CustomForm} object
     */
    public static Form getMessageWindow(String message) {
        return CustomForm.builder()
                .title("Notice")
                .component(LabelComponent.of(message))
                .build();
    }

    /**
     * Handle the main menu response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleMainMenuResponse(Player player, SimpleFormResponse data) {
        switch (data.getClickedButtonId()) {
            case 0:
                player.setServerCategory(ServerCategory.OFFICIAL);
                break;

            case 1:
                player.setServerCategory(ServerCategory.GEYSER);
                break;

            default:
                // If we have custom servers enabled there are a few extra buttons
                if (MasterServer.getInstance().getGeyserConnectConfig().getCustomServers().isEnabled()) {
                    switch (data.getClickedButtonId()) {
                        case 2:
                            player.setServerCategory(ServerCategory.CUSTOM);
                            break;
                        case 3:
                            player.sendWindow(FormID.DIRECT_CONNECT, getDirectConnect());
                            return;

                        default:
                            player.getSession().disconnect("disconnectionScreen.disconnected");
                            return;
                    }
                } else {
                    player.getSession().disconnect("disconnectionScreen.disconnected");
                    return;
                }
                break;
        }

        // Send the server list
        player.sendWindow(FormID.LIST_SERVERS, getServerList(player.getCurrentServers(), player.getServerCategory()));
    }

    /**
     * Handle the server list response
     *
     * @param player The player that submitted the response
     * @param data The form response data
     */
    public static void handleServerListResponse(Player player, SimpleFormResponse data) {
        List<Server> servers = player.getCurrentServers();

        if (player.getServerCategory() == ServerCategory.CUSTOM) {
            if (!data.isCorrect() || data.getClickedButtonId() == servers.size() + 1) {
                player.sendWindow(FormID.MAIN, UIHandler.getMainMenu());
            } else if (data.getClickedButtonId() == servers.size()) {
                player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getCurrentServers()));
            } else {
                // Get the server
                Server server = servers.get(data.getClickedButtonId());

                player.sendToServer(server);
            }
        } else {
            if (!data.isCorrect() || data.getClickedButtonId() == servers.size()) {
                player.sendWindow(FormID.MAIN, UIHandler.getMainMenu());
            } else {
                // Get the server
                Server server = servers.get(data.getClickedButtonId());

                player.sendToServer(server);
            }
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
        if (!data.isCorrect()) {
            player.sendWindow(FormID.MAIN, getMainMenu());
            return;
        }

        try {
            String address = data.getInput(0);
            int port = Integer.parseInt(data.getInput(1));
            boolean online = data.getToggle(2);
            boolean bedrock = data.getToggle(3);

            // Make sure we got an address
            if (address == null || "".equals(address)) {
                player.sendWindow(FormID.MAIN, getMainMenu());
                return;
            }

            // Make sure we got a valid port
            if (port <= 0 || port >= 65535) {
                player.resendWindow();
                return;
            }

            player.sendToServer(new Server(address, port, online, bedrock));
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
        List<Server> servers = player.getCurrentServers();

        // Take them back to the main menu if they close the edit server list window
        if (!data.isCorrect()) {
            player.sendWindow(FormID.LIST_SERVERS, getServerList(servers, player.getServerCategory()));
            return;
        }

        if (data.getClickedButtonId() == servers.size()) {
            player.sendWindow(FormID.ADD_SERVER, getAddServer());
        } else if (data.getClickedButtonId() == servers.size() + 1) {
            player.sendWindow(FormID.LIST_SERVERS, getServerList(servers, player.getServerCategory()));
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
        if (!data.isCorrect()) {
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
            return;
        }

        try {
            String address = data.getInput(0);
            int port = Integer.parseInt(data.getInput(1));
            boolean online = data.getToggle(2);
            boolean bedrock = data.getToggle(3);

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
        if (!data.isCorrect()) {
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
            return;
        }

        SimpleForm window = (SimpleForm) player.getCurrentWindow();
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
        SimpleForm window = (SimpleForm) player.getCurrentWindow();
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
        if (!data.isCorrect()) {
            player.sendWindow(FormID.EDIT_SERVERS, getEditServerList(player.getServers()));
            return;
        }

        try {
            int serverIndex = Integer.parseInt(((CustomForm)player.getCurrentWindow()).getContent().get(0).getText().split(":")[1].trim());

            String address = data.getInput(1);
            int port = Integer.parseInt(data.getInput(2));
            boolean online = data.getToggle(3);
            boolean bedrock = data.getToggle(4);

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
