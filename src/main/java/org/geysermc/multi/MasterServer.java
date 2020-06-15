package org.geysermc.multi;

import com.nukkitx.protocol.bedrock.BedrockPong;
import com.nukkitx.protocol.bedrock.BedrockServer;
import com.nukkitx.protocol.bedrock.BedrockServerEventHandler;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import lombok.Getter;
import org.geysermc.multi.proxy.GeyserProxyBootstrap;
import org.geysermc.multi.utils.Logger;
import org.geysermc.multi.utils.Player;
import org.geysermc.multi.utils.Server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MasterServer {

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

    public MasterServer() {
        logger = new Logger();
        logger.setDebug(true);

        this.instance = this;
        this.generalThreadPool = Executors.newScheduledThreadPool(32);

        // Start a timer to keep the thread running
        timer = new Timer();
        TimerTask task = new TimerTask() { public void run() { } };
        timer.scheduleAtFixedRate(task, 0L, 1000L);

        start(19132);

        logger.start();
    }

    private void start(int port) {
        logger.info("Starting...");

        InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", port);
        bdServer = new BedrockServer(bindAddress);

        bdPong = new BedrockPong();
        bdPong.setEdition("MCPE");
        bdPong.setMotd("My Server");
        bdPong.setPlayerCount(0);
        bdPong.setMaximumPlayerCount(1337);
        bdPong.setGameType("Survival");
        bdPong.setIpv4Port(port);
        bdPong.setProtocolVersion(GeyserMulti.CODEC.getProtocolVersion());
        bdPong.setVersion(GeyserMulti.CODEC.getMinecraftVersion());

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
        logger.info("Server started on 0.0.0.0:" + port);
    }

    public void shutdown() {
        shuttingDown = true;
        generalThreadPool.shutdown();

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
