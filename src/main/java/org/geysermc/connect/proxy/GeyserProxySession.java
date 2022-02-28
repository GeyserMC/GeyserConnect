/*
 * Copyright (c) 2019-2021 GeyserMC. http://geysermc.org
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
import io.netty.channel.EventLoop;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.PendingMicrosoftAuthentication.*;

public class GeyserProxySession extends GeyserSession {
    private final Player player;

    public GeyserProxySession(GeyserImpl geyser, BedrockServerSession bedrockServerSession, EventLoop eventLoop, Player player, boolean initialized) {
        super(geyser, bedrockServerSession, eventLoop);
        sentSpawnPacket = initialized;
        this.player = player;
    }

    @Override
    protected void disableSrvResolving() {
        // Do nothing
    }

    @Override
    public void disconnect(String reason) {
        ProxyAuthenticationTask task = (ProxyAuthenticationTask) getGeyser().getPendingMicrosoftAuthentication().getTask(this.xuid());
        if (task != null) {
            Server server = player.getCurrentServer();
            task.setServer(server.getAddress());
            task.setPort(server.getPort());
        }
        super.disconnect(reason);
    }
}
