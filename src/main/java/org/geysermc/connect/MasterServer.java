package org.geysermc.connect;

import com.nukkitx.protocol.bedrock.*;
import com.nukkitx.protocol.bedrock.v390.Bedrock_v390;
import lombok.Getter;
import org.geysermc.connector.utils.FileUtils;
import org.geysermc.connect.proxy.GeyserProxyBootstrap;
import org.geysermc.connect.storage.AbstractStorageManager;
import org.geysermc.connect.utils.Logger;
import org.geysermc.connect.utils.Player;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MasterServer {

    public static final BedrockPacketCodec CODEC = Bedrock_v390.V390_CODEC;

    private final Timer timer;
    private BedrockServer bdServer;
    private BedrockPong bdPong;

    @Getter
    private boolean shuttingDown = false;

    @Getter
    private static MasterServer instance;

    @Getter
    private final Logger logger;

    @Getter
    private final ScheduledExecutorService generalThreadPool;

    @Getter
    private final Map<InetSocketAddress, Player> players = new HashMap<>();

    @Getter
    private GeyserProxyBootstrap geyserProxy;

    @Getter
    private GeyserConnectConfig geyserConnectConfig;

    @Getter
    private AbstractStorageManager storageManager;

    public MasterServer() {
        this.instance = this;

        logger = new Logger();

        try {
            File configFile = FileUtils.fileOrCopiedFromResource(new File("config.yml"), "config.yml", (x) -> x);
            this.geyserConnectConfig = FileUtils.loadConfig(configFile, GeyserConnectConfig.class);
        } catch (IOException ex) {
            logger.severe("Failed to read/create config.yml! Make sure it's up to date and/or readable+writable!", ex);
            ex.printStackTrace();
        }

        logger.setDebug(geyserConnectConfig.isDebugMode());

        geyserConnectConfig.checkRemoteIP();

        this.generalThreadPool = Executors.newScheduledThreadPool(32);

        // Start a timer to keep the thread running
        timer = new Timer();
        TimerTask task = new TimerTask() { public void run() { } };
        timer.scheduleAtFixedRate(task, 0L, 1000L);

        try {
            storageManager = geyserConnectConfig.getCustomServers().getStorageType().getStorageManager().newInstance();
        } catch (Exception e) {
            logger.severe("Invalid storage manager class!", e);
            return;
        }

        storageManager.setupStorage();

        start(geyserConnectConfig.getPort());

        logger.start();
    }

    private void start(int port) {
        logger.info("Starting...");

        InetSocketAddress bindAddress = new InetSocketAddress(geyserConnectConfig.getAddress(), port);
        bdServer = new BedrockServer(bindAddress);

        bdPong = new BedrockPong();
        bdPong.setEdition("MCPE");
        bdPong.setMotd(geyserConnectConfig.getMotd());
        bdPong.setPlayerCount(0);
        bdPong.setMaximumPlayerCount(geyserConnectConfig.getMaxPlayers());
        bdPong.setGameType("Survival");
        bdPong.setIpv4Port(port);
        bdPong.setProtocolVersion(MasterServer.CODEC.getProtocolVersion());
        bdPong.setVersion(null); // Server tries to connect either way and it looks better

        bdServer.setHandler(new BedrockServerEventHandler() {
            @Override
            public boolean onConnectionRequest(InetSocketAddress address) {
                return true; // Connection will be accepted
            }

            @Override
            public BedrockPong onQuery(InetSocketAddress address) {
                return bdPong;
            }

            @Override
            public void onSessionCreation(BedrockServerSession session) {
                session.setPacketHandler(new PacketHandler(session, instance));
            }
        });

        // Start server up
        bdServer.bind().join();
        logger.info("Server started on " + geyserConnectConfig.getAddress() + ":" + port);
    }

    public void shutdown() {
        shuttingDown = true;
        generalThreadPool.shutdown();
        storageManager.closeStorage();

        if (geyserProxy != null) {
            geyserProxy.onDisable();
        }
    }

    public void createGeyserProxy() {
        if (geyserProxy == null) {
            this.geyserProxy = new GeyserProxyBootstrap();
            geyserProxy.onEnable();
        }
    }
}
