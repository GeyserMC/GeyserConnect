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

package org.geysermc.connect.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.geysermc.common.PlatformType;
import org.geysermc.connect.GeyserConnectConfig;
import org.geysermc.connect.MasterServer;
import org.geysermc.geyser.GeyserBootstrap;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.command.CommandManager;
import org.geysermc.geyser.configuration.GeyserConfiguration;
import org.geysermc.geyser.dump.BootstrapDumpInfo;
import org.geysermc.geyser.ping.GeyserLegacyPingPassthrough;
import org.geysermc.geyser.ping.IGeyserPingPassthrough;
import org.geysermc.geyser.text.GeyserLocale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class GeyserProxyBootstrap implements GeyserBootstrap {

    private GeyserProxyCommandManager geyserCommandManager;
    private GeyserProxyConfiguration geyserConfig;
    private GeyserProxyLogger geyserLogger;
    private IGeyserPingPassthrough geyserPingPassthrough;

    private GeyserImpl geyser;

    @Override
    public void onEnable() {
        GeyserLocale.init(this);

        // Setup a logger
        geyserLogger = new GeyserProxyLogger();

        // Read the static config from resources
        try {
            InputStream configFile = GeyserProxyBootstrap.class.getClassLoader().getResourceAsStream("proxy_config.yml");

            // Grab the config as text and replace static strings to the main config variables
            String text = new BufferedReader(new InputStreamReader(configFile, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            GeyserConnectConfig multiConfig = MasterServer.getInstance().getGeyserConnectConfig();
            text = text.replaceAll("%MOTD%", multiConfig.getMotd());
            text = text.replace("%PLAYERS%", String.valueOf(multiConfig.getMaxPlayers()));
            text = text.replace("%ALLOWPASSWORDAUTHENTICATION%", String.valueOf(multiConfig.getGeyser().isAllowPasswordAuthentication()));

            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            geyserConfig = objectMapper.readValue(text, GeyserProxyConfiguration.class);

            geyserConfig.getSavedUserLogins().clear();
            for (String savedUserLogin : MasterServer.getInstance().getGeyserConnectConfig().getGeyser().getSavedUserLogins()) {
                geyserConfig.getSavedUserLogins().add(savedUserLogin);
            }

        } catch (IOException ex) {
            geyserLogger.severe("Failed to read proxy_config.yml! Make sure it's up to date and/or readable+writable!", ex);
            return;
        }

        // Not sure there is a point in doing this as its a static config
        GeyserConfiguration.checkGeyserConfiguration(geyserConfig, geyserLogger);

        // Create the connector and command manager
        geyser = GeyserImpl.start(PlatformType.STANDALONE, this);
        geyserCommandManager = new GeyserProxyCommandManager(geyser);

        // Start the ping passthrough thread, again don't think there is a point
        geyserPingPassthrough = GeyserLegacyPingPassthrough.init(geyser);

        // Swap the normal handler to our custom handler so we can change some
        geyser.getBedrockServer().setHandler(new ProxyConnectorServerEventHandler(geyser));
    }

    @Override
    public void onDisable() {
        geyser.shutdown();
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

    @Override
    public Path getConfigFolder() {
        return Paths.get(System.getProperty("user.dir"));
    }

    @Override
    public BootstrapDumpInfo getDumpInfo() {
        return new BootstrapDumpInfo();
    }
}

