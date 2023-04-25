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

package org.geysermc.connect.extension;

import org.cloudburstmc.protocol.bedrock.data.AttributeData;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAttributesPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.geysermc.connect.extension.ui.UIHandler;
import org.geysermc.connect.extension.utils.ServerManager;
import org.geysermc.connect.extension.utils.Utils;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.entity.attribute.GeyserAttributeType;
import org.geysermc.geyser.network.UpstreamPacketHandler;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.DimensionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PacketHandler extends UpstreamPacketHandler {

    private final GeyserSession session;
    private final GeyserConnect geyserConnect;
    private final BedrockPacketHandler originalPacketHandler;

    public PacketHandler(GeyserConnect geyserConnect, GeyserSession session, BedrockPacketHandler packetHandler) {
        super(GeyserImpl.getInstance(), session);

        this.session = session;
        this.geyserConnect = geyserConnect;
        this.originalPacketHandler = packetHandler;

        // Spawn the player in the end (it just looks better)
        session.setDimension(DimensionUtils.THE_END);
        DimensionUtils.setBedrockDimension(session, DimensionUtils.THE_END);
    }

    @Override
    public void onDisconnect(String reason) {
        geyserConnect.logger().info(Utils.displayName(session) + " has disconnected (" + reason + ")");
        ServerManager.unloadServers(session);
    }

    @Override
    public PacketSignal handle(SetLocalPlayerAsInitializedPacket packet) {
        if (session.getPlayerEntity().getGeyserId() == packet.getRuntimeEntityId()) {
            if (!session.getUpstream().isInitialized()) {
                session.getUpstream().setInitialized(true);

                // Load the players servers
                ServerManager.loadServers(session);

                geyserConnect.logger().debug("Player initialized: " + Utils.displayName(session));

                UIHandler uiHandler = new UIHandler(session, originalPacketHandler);
                uiHandler.initialiseSession();
            }
        }

        // Handle the virtual host if specified
//        GeyserConnectConfig.VirtualHostSection vhost = MasterServer.getInstance().getGeyserConnectConfig().getVhost();
//        if (vhost.isEnabled()) {
//            String domain = player.getClientData().getServerAddress().split(":")[0];
//            if (!domain.equals(vhost.getBaseDomain()) && domain.endsWith("." + vhost.getBaseDomain())) {
//                String address = "";
//                int port = 25565;
//                boolean online = true;
//
//                // Parse the address used
//                String[] domainParts = domain.replaceFirst("\\." + vhost.getBaseDomain() + "$", "").split("\\._");
//                for (int i = 0; i < domainParts.length; i++) {
//                    String part = domainParts[i];
//                    if (i == 0) {
//                        address = part;
//                    } else if (part.startsWith("p")) {
//                        port = Integer.parseInt(part.substring(1));
//                    } else if (part.startsWith("o")) {
//                        online = false;
//                    }
//                }
//
//                // They didn't specify an address so disconnect them
//                if (address.startsWith("_")) {
//                    session.disconnect("disconnectionScreen.invalidIP");
//                    return false;
//                }
//
//                // Log the virtual host usage
//                masterServer.getLogger().info(player.getAuthData().name() + " is using virtualhost: " + address + ":" + port + (!online ? " (offline)" : ""));
//
//                // Send the player to the wanted server
//                player.sendToServer(new Server(address, port, online, false));
//
//                return false;
//            }
//        }

        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(NetworkStackLatencyPacket packet) {
        // This is to fix a bug in the client where it doesn't load form images
        UpdateAttributesPacket updateAttributesPacket = new UpdateAttributesPacket();
        updateAttributesPacket.setRuntimeEntityId(1);
        List<AttributeData> attributes = Collections.singletonList(GeyserAttributeType.EXPERIENCE_LEVEL.getAttribute(0f));
        updateAttributesPacket.setAttributes(attributes);

        // Doesn't work 100% of the time but fixes it most of the time
        GeyserImpl.getInstance().getScheduledThread().schedule(() -> session.sendUpstreamPacket(updateAttributesPacket), 500, TimeUnit.MILLISECONDS);

        return super.handle(packet);
    }
}

