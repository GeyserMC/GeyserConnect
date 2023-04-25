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
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.SessionInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.session.GeyserSession;

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
    }

    @Subscribe
    public void onSessionInitialize(SessionInitializeEvent event) {
        GeyserSession session = (GeyserSession) event.connection();

        // Remove all saved logins to prevent issues connecting
        // Maybe worth adding support for this later
        session.getGeyser().getConfig().getSavedUserLogins().clear();

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
    }
}