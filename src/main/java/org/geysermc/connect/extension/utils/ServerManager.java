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

import org.geysermc.connect.extension.GeyserConnect;
import org.geysermc.geyser.session.GeyserSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerManager {
    private static final Map<String, List<Server>> servers = new HashMap<>();

    public static void loadServers(GeyserSession session) {
        GeyserConnect.instance().logger().debug("Loading servers for " + Utils.displayName(session));
        servers.put(session.xuid(), GeyserConnect.instance().storageManager().loadServers(session));
    }

    public static void unloadServers(GeyserSession session) {
        GeyserConnect.instance().logger().debug("Saving and unloading servers for " + Utils.displayName(session));
        GeyserConnect.instance().storageManager().saveServers(session);
        servers.remove(session.xuid());
    }

    public static List<Server> getServers(GeyserSession session) {
        return servers.get(session.xuid());
    }

    public static void addServer(GeyserSession session, Server server) {
        servers.get(session.xuid()).add(server);
    }

    public static void removeServer(GeyserSession session, Server server) {
        getServers(session).remove(server);
    }

    public static int getServerIndex(GeyserSession session, Server server) {
        return getServers(session).indexOf(server);
    }

    public static void updateServer(GeyserSession session, int serverIndex, Server server) {
        getServers(session).set(serverIndex, server);
    }
}
