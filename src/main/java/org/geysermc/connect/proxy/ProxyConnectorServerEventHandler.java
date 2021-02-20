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

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.ConnectorServerEventHandler;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Player;

import java.util.concurrent.TimeUnit;

public class ProxyConnectorServerEventHandler extends ConnectorServerEventHandler {

    private final GeyserConnector connector;

    public ProxyConnectorServerEventHandler(GeyserConnector connector) {
        super(connector);
        this.connector = connector;
        MasterServer.getInstance().getLogger().debug("Registered custom ConnectorServerEventHandler");
    }

    @Override
    public void onSessionCreation(BedrockServerSession bedrockServerSession) {
        super.onSessionCreation(bedrockServerSession);

        // This doesn't clean up the old packet handler, so may cause a memory leak?
        GeyserProxySession session = new GeyserProxySession(connector, bedrockServerSession);
        bedrockServerSession.setPacketHandler(new GeyserProxyUpstreamPacketHandler(connector, session));

        // Add another disconnect handler to remove the player on final disconnect
        bedrockServerSession.addDisconnectHandler(disconnectReason -> {
            // Make sure nothing is null before locating the player
            if (MasterServer.getInstance() == null
                    || session.getAuthData() == null
                    || session.getAuthData().getXboxUUID() == null) {
                return;
            }

            Player player = session.getPlayer();
            if (player != null) {
                MasterServer.getInstance().getLogger().debug("Player disconnected from Geyser proxy: " + player.getDisplayName() + " (" + disconnectReason + ")");

                // Set the last disconnect time
                MasterServer.getInstance().setLastDisconnectTime(System.currentTimeMillis());

                int shutdownTime = MasterServer.getInstance().getGeyserConnectConfig().getGeyser().getShutdownTime();

                if (shutdownTime != -1) {
                    MasterServer.getInstance().getGeneralThreadPool().schedule(() -> {
                        if (System.currentTimeMillis() - MasterServer.getInstance().getLastDisconnectTime() > shutdownTime * 1000L
                                && connector != null
                                && connector.getPlayers().size() <= 0) {
                            MasterServer.getInstance().shutdownGeyserProxy();
                        }
                    }, shutdownTime, TimeUnit.SECONDS);
                }
            }
        });
    }
}
