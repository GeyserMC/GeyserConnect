package org.geysermc.multi.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.geysermc.common.PlatformType;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.bootstrap.GeyserBootstrap;
import org.geysermc.connector.configuration.GeyserConfiguration;
import org.geysermc.connector.command.CommandManager;
import org.geysermc.connector.ping.IGeyserPingPassthrough;
import org.geysermc.connector.ping.GeyserLegacyPingPassthrough;
import org.geysermc.connector.utils.FileUtils;
import org.geysermc.multi.MasterServer;
import org.geysermc.multi.utils.Logger;
import org.geysermc.multi.utils.Server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class GeyserProxyBootstrap implements GeyserBootstrap {

    private GeyserProxyCommandManager geyserCommandManager;
    private GeyserProxyConfiguration geyserConfig;
    private GeyserProxyLogger geyserLogger;
    private IGeyserPingPassthrough geyserPingPassthrough;

    private GeyserConnector connector;

    @Override
    public void onEnable() {
        // Setup a logger
        geyserLogger = new GeyserProxyLogger();

        // Read the static config from resources
        try {
            InputStream configFile = GeyserProxyBootstrap.class.getClassLoader().getResourceAsStream("proxy_config.yml");
            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            geyserConfig = objectMapper.readValue(configFile, GeyserProxyConfiguration.class);
        } catch (IOException ex) {
            geyserLogger.severe("Failed to read proxy_config.yml! Make sure it's up to date and/or readable+writable!", ex);
            return;
        }

        // Not sure there is a point in doing this as its a static config
        GeyserConfiguration.checkGeyserConfiguration(geyserConfig, geyserLogger);

        // Create the connector and command manager
        connector = GeyserConnector.start(PlatformType.STANDALONE, this);
        geyserCommandManager = new GeyserProxyCommandManager(connector);

        // Start the ping passthrough thread, again don't think there is a point
        geyserPingPassthrough = GeyserLegacyPingPassthrough.init(connector);

        // Swap the normal handler to our custom handler so we can change some
        connector.getBedrockServer().setHandler(new ProxyConnectorServerEventHandler(connector));
    }

    @Override
    public void onDisable() {
        connector.shutdown();
    }

    @Override
    public GeyserConfiguration getGeyserConfig() {
        return geyserConfig;
    }

    @Override
    public GeyserProxyLogger getGeyserLogger() {
        return geyserLogger;
    }

    @Override
    public CommandManager getGeyserCommandManager() {
        return geyserCommandManager;
    }

    @Override
    public IGeyserPingPassthrough getGeyserPingPassthrough() {
        return geyserPingPassthrough;
    }
}

