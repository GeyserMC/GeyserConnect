/*
 * Copyright (c) 2019-2023 GeyserMC. http://geysermc.org
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

package org.geysermc.connect.extension;

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.cloudburstmc.protocol.bedrock.packet.TransferPacket;
import org.geysermc.connect.extension.config.Config;
import org.geysermc.connect.extension.config.ConfigLoader;
import org.geysermc.connect.extension.storage.AbstractStorageManager;
import org.geysermc.connect.extension.storage.DisabledStorageManager;
import org.geysermc.connect.extension.utils.Utils;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.SessionInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.network.AuthType;
import org.geysermc.geyser.api.util.PlatformType;
import org.geysermc.geyser.session.GeyserSession;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GeyserConnect implements Extension {
    private static GeyserConnect instance;
    private Config config;
    private AbstractStorageManager storageManager;

    public GeyserConnect() {
        instance = this;
    }

    public static GeyserConnect instance() {
        return instance;
    }

    public Config config() {
        return config;
    }

    public AbstractStorageManager storageManager() {
        return storageManager;
    }

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        if (this.geyserApi().platformType() != PlatformType.STANDALONE) {
            this.logger().severe("GeyserConnect is only supported on standalone Geyser instances!");
            this.disable();
        }
    }

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        config = ConfigLoader.load(this, GeyserConnect.class, Config.class);

        if (!config.customServers().enabled()) {
            // Force the storage manager if we have it disabled
            storageManager = new DisabledStorageManager();
            this.logger().info("Disabled custom player servers");
        } else {
            try {
                storageManager = config.customServers().storageType().storageManager().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                this.logger().severe("Invalid storage manager class!", e);
                return;
            }
        }

        storageManager.setupStorage();

        GeyserImpl geyserInstance = (GeyserImpl) this.geyserApi();

        // Remove all saved logins to prevent issues connecting
        // Maybe worth adding support for this later
        geyserInstance.getConfig().getSavedUserLogins().clear();

        if (geyserInstance.getConfig().isPassthroughMotd() || geyserInstance.getConfig().isPassthroughPlayerCounts()) {
            this.logger().warning("Either `passthrough-motd` or `passthrough-player-counts` is enabled in the config, this will likely produce errors");
        }
    }

    @Subscribe
    public void onSessionInitialize(SessionInitializeEvent event) {
        GeyserSession session = (GeyserSession) event.connection();

        // Change the packet handler to our own
        BedrockPacketHandler packetHandler = session.getUpstream().getSession().getPacketHandler();
        session.getUpstream().getSession().setPacketHandler(new PacketHandler(this, session, packetHandler));
    }

    @Subscribe
    public void onCommandDefine(GeyserDefineCommandsEvent event) {
        event.register(Command.builder(this)
            .source(GeyserConnection.class)
            .name("menu")
            .description("Take you back to the GeyserConnect menu.")
            .executor((source, command, args) -> {
                GeyserSession session = (GeyserSession) source;
                String serverAddress = session.getClientData().getServerAddress();
                String ip = serverAddress.split(":")[0];
                int port = 19132;
                try {
                    port = Integer.parseInt(serverAddress.split(":")[1]);
                } catch (NumberFormatException ignored) {
                }

                TransferPacket transferPacket = new TransferPacket();
                transferPacket.setAddress(ip);
                transferPacket.setPort(port);
                session.sendUpstreamPacket(transferPacket);
            })
            .build());

        event.register(Command.builder(this)
            .source(CommandSource.class)
            .name("messageall")
            .description("Send a message to everyone connected to this GeyserConnect server.")
            .executor((source, command, args) -> {
                if (!source.isConsole()) {
                    source.sendMessage("This command can only be ran from the console.");
                    return;
                }

                String type = args[0].toLowerCase();
                String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

                if (message.isEmpty()) {
                    source.sendMessage("You must specify a message.");
                    return;
                }

                GeyserSession[] sessions = getGeyserSessions();

                switch (type) {
                    case "chat":
                        for (GeyserSession session : sessions) {
                            session.sendMessage(message);
                        }
                        break;
                    case "gui":
                        for (GeyserSession session : sessions) {
                            session.sendForm(CustomForm.builder()
                                .title("Notice")
                                .label(message)
                                .build());
                        }
                        break;
                    default:
                        source.sendMessage("Invalid message type. Valid types: chat, gui");
                        return;
                }
            })
            .build());


        event.register(Command.builder(this)
            .source(CommandSource.class)
            .name("transferall")
            .description("Transfer everyone connected to this GeyserConnect server to another.")
            .executor((source, command, args) -> {
                if (!source.isConsole()) {
                    source.sendMessage("This command can only be ran from the console.");
                    return;
                }

                String ip = args[0].toLowerCase();
                int port = 19132;
                boolean passAsVhost = args.length > 1 && Boolean.parseBoolean(args[1]);

                // Split the ip and port if needed
                String[] parts = ip.split(":");
                ip = parts[0];
                if (parts.length > 1) {
                    try {
                        port = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }

                for (GeyserSession session : getGeyserSessions()) {
                    String sessionIp = ip;

                    // If we are passing with a vhost construct the vhost
                    if (passAsVhost) {
                        sessionIp = session.remoteServer().address();
                        sessionIp += "._p" + session.remoteServer().port();
                        if (session.remoteServer().authType() == AuthType.OFFLINE) {
                            sessionIp += "._o";
                        }
                        sessionIp += "." + ip;
                    }

                    GeyserConnect.instance().logger().info("Sending " + Utils.displayName(session) + " to " + sessionIp + (port != 19132 ? ":" + port : ""));

                    TransferPacket transferPacket = new TransferPacket();
                    transferPacket.setAddress(sessionIp);
                    transferPacket.setPort(port);
                    session.sendUpstreamPacket(transferPacket);
                }
            })
            .build());
    }

    private GeyserSession[] getGeyserSessions() {
        return this.geyserApi().onlineConnections().stream().map(connection -> (GeyserSession) connection).toList()
    }
}