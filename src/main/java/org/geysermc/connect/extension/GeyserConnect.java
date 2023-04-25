package org.geysermc.connect.extension;

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.geysermc.connect.extension.config.Config;
import org.geysermc.connect.extension.config.ConfigLoader;
import org.geysermc.connect.extension.storage.AbstractStorageManager;
import org.geysermc.connect.extension.storage.DisabledStorageManager;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.bedrock.SessionInitializeEvent;
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
        BedrockPacketHandler packetHandler = session.getUpstream().getSession().getPacketHandler();
        session.getUpstream().getSession().setPacketHandler(new PacketHandler(this, session, packetHandler));
    }
}