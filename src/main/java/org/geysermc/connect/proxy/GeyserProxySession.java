/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 *  @author GeyserMC
 *  @link https://github.com/GeyserMC/GeyserConnect
 *
 */

package org.geysermc.connect.proxy;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import org.geysermc.common.AuthType;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Player;
import org.geysermc.connector.network.session.auth.AuthData;

public class GeyserProxySession extends GeyserSession {

    private final GeyserConnector connector;
    private final BedrockServerSession bedrockServerSession;

    public GeyserProxySession(GeyserConnector connector, BedrockServerSession bedrockServerSession) {
        super(connector, bedrockServerSession);
        this.connector = connector;
        this.bedrockServerSession = bedrockServerSession;
    }

    @Override
    public void authenticate(String username, String password) {
        // Get the player based on the connection address
        Player player = MasterServer.getInstance().getPlayers().get(getAuthData().getXboxUUID());
        if (player != null && player.getCurrentServer() != null) {
            // Set the remote server info for the player
            connector.getRemoteServer().setAddress(player.getCurrentServer().getAddress());
            connector.getRemoteServer().setPort(player.getCurrentServer().getPort());

            connector.setAuthType(player.getCurrentServer().isOnline() ? AuthType.ONLINE : AuthType.OFFLINE);

            super.authenticate(username, password);
        }else{
            // Disconnect the player if they haven't picked a server on the master server list
            bedrockServerSession.disconnect("Please connect to the master server and pick a server first!");
        }
    }

    @Override
    public void login() {
        Player player = MasterServer.getInstance().getPlayers().get(getAuthData().getXboxUUID());
        connector.setAuthType(player.getCurrentServer().isOnline() ? AuthType.ONLINE : AuthType.OFFLINE);

        super.login();
    }

    @Override
    public void setAuthenticationData(AuthData authData) {
        super.setAuthenticationData(authData);

        Player player = MasterServer.getInstance().getPlayers().get(authData.getXboxUUID());
        if (player == null) {
            bedrockServerSession.disconnect("Please connect to the master server and pick a server first!");
            return;
        }
    }
}
