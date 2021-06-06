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

package org.geysermc.connect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nukkitx.protocol.bedrock.*;
import lombok.Getter;
import lombok.Setter;
import org.geysermc.connect.storage.DisabledStorageManager;
import org.geysermc.connect.utils.Server;
import org.geysermc.connect.utils.ServerCategory;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.BedrockProtocol;
import org.geysermc.connector.utils.FileUtils;
import org.geysermc.connect.proxy.GeyserProxyBootstrap;
import org.geysermc.connect.storage.AbstractStorageManager;
import org.geysermc.connect.utils.Logger;
import org.geysermc.connect.utils.Player;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MasterServer {

    private BedrockServer bdServer;

    @Getter
    private boolean shuttingDown = false;

    @Getter
    private static MasterServer instance;

    @Getter
    private final Logger logger;

    @Getter
    private final ScheduledExecutorService generalThreadPool;

    @Getter
    private final Map<String, Player> players = new HashMap<>();

    /**
     * Players that are transferring and are expected to rejoin on the Geyser side.
     */
    @Getter
    private final Cache<String, Player> transferringPlayers = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Getter
    private GeyserProxyBootstrap geyserProxy;

    @Getter
    private GeyserConnectConfig geyserConnectConfig;

    @Getter
    private AbstractStorageManager storageManager;

    @Setter
    @Getter
    private long lastDisconnectTime = 0L;

    public MasterServer() {
        instance = this;

        logger = new Logger();

        try {
            File configFile = FileUtils.fileOrCopiedFromResource(new File("config.yml"), "config.yml", (x) -> x);
            this.geyserConnectConfig = FileUtils.loadConfig(configFile, GeyserConnectConfig.class);
        } catch (IOException ex) {
            logger.severe("Failed to read/create config.yml! Make sure it's up to date and/or readable+writable!", ex);
            ex.printStackTrace();
        }

        logger.setDebug(geyserConnectConfig.isDebugMode());

        this.generalThreadPool = Executors.newScheduledThreadPool(32);

        // Start a timer to keep the thread running
        Timer timer = new Timer();
        TimerTask task = new TimerTask() { public void run() { } };
        timer.scheduleAtFixedRate(task, 0L, 1000L);

        if (!geyserConnectConfig.getCustomServers().isEnabled()) {
            // Force the storage manager if we have it disabled
            storageManager = new DisabledStorageManager();
            logger.info("Disabled custom player servers");
        } else {
            try {
                storageManager = geyserConnectConfig.getCustomServers().getStorageType().getStorageManager().newInstance();
            } catch (Exception e) {
                logger.severe("Invalid storage manager class!", e);
                return;
            }
        }

        storageManager.setupStorage();

        // Create the base welcome.txt file
        try {
            FileUtils.fileOrCopiedFromResource(new File(getGeyserConnectConfig().getWelcomeFile()), "welcome.txt", (x) -> x);
        } catch (IOException ignored) { }

        start(geyserConnectConfig.getPort());

        logger.start();
    }

    private void start(int port) {
        logger.info("Starting...");

        InetSocketAddress bindAddress = new InetSocketAddress(geyserConnectConfig.getAddress(), port);
        bdServer = new BedrockServer(bindAddress);

        bdServer.setHandler(new BedrockServerEventHandler() {
            @Override
            public boolean onConnectionRequest(InetSocketAddress address) {
                return true; // Connection will be accepted
            }

            @Override
            public BedrockPong onQuery(InetSocketAddress address) {
                int playerCount = players.size();
                if (GeyserConnector.getInstance() != null) {
                    playerCount += GeyserConnector.getInstance().getPlayers().size();
                }

                String subMotd = geyserConnectConfig.getSubmotd();
                if (subMotd == null || subMotd.isEmpty()) {
                    subMotd = "GeyserConnect";
                }

                BedrockPong bdPong = new BedrockPong();
                bdPong.setEdition("MCPE");
                bdPong.setMotd(geyserConnectConfig.getMotd());
                bdPong.setSubMotd(subMotd);
                bdPong.setPlayerCount(playerCount);
                bdPong.setMaximumPlayerCount(geyserConnectConfig.getMaxPlayers());
                bdPong.setGameType("Survival");
                bdPong.setIpv4Port(port);
                bdPong.setProtocolVersion(BedrockProtocol.DEFAULT_BEDROCK_CODEC.getProtocolVersion());
                bdPong.setVersion(BedrockProtocol.DEFAULT_BEDROCK_CODEC.getMinecraftVersion());
                return bdPong;
            }

            @Override
            public void onSessionCreation(BedrockServerSession session) {
                session.setPacketHandler(new PacketHandler(session, instance));
            }
        });

        // Start server up
        bdServer.bind().join();

        // Create the Geyser instance
        createGeyserProxy();

        logger.info("Server started on " + geyserConnectConfig.getAddress() + ":" + port);
    }

    public void shutdown() {
        shuttingDown = true;
        bdServer.close();

        shutdownGeyserProxy();

        generalThreadPool.shutdown();
        storageManager.closeStorage();
        System.exit(0);
    }

    public void createGeyserProxy() {
        if (geyserProxy == null) {
            // Make sure Geyser doesn't start the listener
            GeyserConnector.setShouldStartListener(false);

            this.geyserProxy = new GeyserProxyBootstrap();
            geyserProxy.onEnable();
        }
    }

    public void shutdownGeyserProxy() {
        if (geyserProxy != null) {
            geyserProxy.onDisable();
            geyserProxy = null;
        }
    }

    public List<Server> getServers(ServerCategory serverCategory) {
        return getGeyserConnectConfig().getServers().stream().filter(server -> server.getCategory() == serverCategory).collect(Collectors.toList());
    }
}
