package org.geysermc.multi.utils;

import com.fasterxml.jackson.annotation.JsonValue;
import org.geysermc.multi.MasterServer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerStorageManager {

    public static void setupStorage() {
        if (!MasterServer.getInstance().getGeyserMultiConfig().getCustomServers().isEnabled()) {
            return;
        }

        switch (MasterServer.getInstance().getGeyserMultiConfig().getCustomServers().getStorageType()) {
            case JSON:
                File playersFolder = new File("players/");
                if (!playersFolder.exists()) {
                    playersFolder.mkdirs();
                }
                break;
            case SQLITE:
                throw new NotImplementedException();
        }
    }

    public static void saveServers(Player player, List<Server> servers) {

    }

    public static List<Server> loadServers(Player player) {
        List<Server> servers = new ArrayList<>();
        servers.add(new Server("81.174.164.211", 25580));
        return servers;
    }

    public enum StorageType {
        JSON("json"),
        SQLITE("sqlite");

        @JsonValue
        private String name;

        StorageType(String name) {
            this.name = name;
        }
    }
}
