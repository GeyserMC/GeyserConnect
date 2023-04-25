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
