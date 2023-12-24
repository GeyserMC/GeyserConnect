/*
 * Copyright (c) 2019-2023 GeyserMC. http://geysermc.org
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
import org.cloudburstmc.protocol.bedrock.packet.LoginPacket;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAttributesPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.geysermc.connect.extension.config.VirtualHostSection;
import org.geysermc.connect.extension.ui.UIHandler;
import org.geysermc.connect.extension.utils.Server;
import org.geysermc.connect.extension.utils.ServerManager;
import org.geysermc.connect.extension.utils.Utils;
import org.geysermc.geyser.entity.attribute.GeyserAttributeType;
import org.geysermc.geyser.network.UpstreamPacketHandler;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.DimensionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PacketHandler extends UpstreamPacketHandler {

    private final GeyserSession session;
    private final GeyserConnect geyserConnect;
    private final BedrockPacketHandler originalPacketHandler;

    public PacketHandler(GeyserConnect geyserConnect, GeyserSession session, BedrockPacketHandler packetHandler) {
        super(session.getGeyser(), session);

        this.session = session;
        this.geyserConnect = geyserConnect;
        this.originalPacketHandler = packetHandler;

        // Spawn the player in the end (it just looks better)
        session.setDimension(DimensionUtils.THE_END);
        DimensionUtils.setBedrockDimension(session, DimensionUtils.THE_END);
    }

    @Override
    public void onDisconnect(String reason) {
        if (session.getAuthData() != null) {
            geyserConnect.logger().info(Utils.displayName(session) + " has disconnected (" + reason + ")");
            ServerManager.unloadServers(session);
        }
    }

    @Override
    public PacketSignal handle(LoginPacket loginPacket) {
        // Check to see if the server is full and we have a hard player cap
        if (geyserConnect.config().hardPlayerLimit()) {
            if (session.getGeyser().getSessionManager().size() >= session.getGeyser().getConfig().getMaxPlayers()) {
                session.disconnect("disconnectionScreen.serverFull");
                return PacketSignal.HANDLED;
            }
        }

        return super.handle(loginPacket);
    }

    @Override
    public PacketSignal handle(SetLocalPlayerAsInitializedPacket packet) {
        geyserConnect.logger().debug("Player initialized: " + Utils.displayName(session));

        // Handle the virtual host if specified
        VirtualHostSection vhost = geyserConnect.config().vhost();
        if (vhost.enabled()) {
            String domain = session.getClientData().getServerAddress();

            // Build the regex matcher for the vhosts
            Pattern regex = Pattern.compile("\\.?(" + vhost.domains().stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")(:[0-9]+)?$");

            if (regex.matcher(domain).find()) {
                String target = domain.replaceAll(regex.pattern(), "").strip();
                if (!target.isEmpty()) {
                    String address = "";
                    int port = 25565;
                    boolean online = true;

                    // Parse the address used
                    String[] domainParts = target.split("\\._");
                    for (int i = 0; i < domainParts.length; i++) {
                        String part = domainParts[i];
                        if (i == 0) {
                            address = part;
                        } else if (part.startsWith("p")) {
                            port = Integer.parseInt(part.substring(1));
                        } else if (part.startsWith("o")) {
                            online = false;
                        }
                    }

                    // They didn't specify an address so disconnect them
                    if (address.startsWith("_")) {
                        session.disconnect("disconnectionScreen.invalidIP");
                        return PacketSignal.HANDLED;
                    }

                    // Log the virtual host usage
                    geyserConnect.logger().info(Utils.displayName(session) + " is using virtualhost: " + address + ":" + port + (!online ? " (offline)" : ""));

                    // Send the player to the wanted server
                    Utils.sendToServer(session, originalPacketHandler, new Server(address, port, online, false, null, null, null));

                    return PacketSignal.HANDLED;
                }
            }
        }

        // Handle normal connections
        if (session.getPlayerEntity().getGeyserId() == packet.getRuntimeEntityId()) {
            if (!session.getUpstream().isInitialized()) {
                session.getUpstream().setInitialized(true);

                // Load the players servers
                ServerManager.loadServers(session);

                UIHandler uiHandler = new UIHandler(session, originalPacketHandler);
                uiHandler.initialiseSession();
            }
        }

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
        session.getGeyser().getScheduledThread().schedule(() -> session.sendUpstreamPacket(updateAttributesPacket), 500, TimeUnit.MILLISECONDS);

        return super.handle(packet);
    }
}

