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

package org.geysermc.connect.extension.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.cloudburstmc.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import org.cloudburstmc.protocol.bedrock.packet.TransferPacket;
import org.geysermc.connect.extension.GeyserConnect;
import org.geysermc.geyser.session.GeyserSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static List<Server> getServers(ServerCategory category) {
        return GeyserConnect.instance().config().servers().stream().filter(server -> server.category() == category).toList();
    }

    public static File fileOrCopiedFromResource(String fileName, String name) throws IOException {
        File file = GeyserConnect.instance().dataFolder().resolve(fileName).toFile();

        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(new File(GeyserConnect.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath(), Collections.emptyMap())) {
                    try (InputStream input = Files.newInputStream(fileSystem.getPath(name))) {
                        byte[] bytes = new byte[input.available()];

                        input.read(bytes);

                        writer.write(new String(bytes).toCharArray());

                        writer.flush();
                    }
                }
            } catch (URISyntaxException ignored) {
            }
        }

        return file;
    }

    public static String displayName(GeyserSession session) {
        return session.bedrockUsername() + " (" + session.xuid() + ")";
    }

    public static void sendToServer(GeyserSession session, BedrockPacketHandler originalPacketHandler, Server server) {
        GeyserConnect.instance().logger().info("Sending " + Utils.displayName(session) + " to " + server.title());
        GeyserConnect.instance().logger().debug(server.toString());

        if (server.bedrock()) {
            // Send them to the bedrock server
            TransferPacket transferPacket = new TransferPacket();
            transferPacket.setAddress(server.address());
            transferPacket.setPort(server.port());
            session.sendUpstreamPacket(transferPacket);
        } else {
            // Save the players servers since we are changing packet handlers
            ServerManager.unloadServers(session);

            // Restore the original packet handler
            session.getUpstream().getSession().setPacketHandler(originalPacketHandler);

            // Set the remote server and un-initialize the session
            session.remoteServer(server);
            session.getUpstream().setInitialized(false);

            // Hand back to core geyser
            SetLocalPlayerAsInitializedPacket initializedPacket = new SetLocalPlayerAsInitializedPacket();
            initializedPacket.setRuntimeEntityId(session.getPlayerEntity().getGeyserId());
            originalPacketHandler.handle(initializedPacket);
        }
    }
}
