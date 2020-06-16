package org.geysermc.connect.storage;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;

import java.util.ArrayList;
import java.util.List;

public class AbstractStorageManager {

    public void setupStorage() { }

    public void closeStorage() { }

    public void saveServers(Player player) { }

    public List<Server> loadServers(Player player) {
        return new ArrayList<>();
    }

    @Getter
    public enum StorageType {
        JSON("json", JsonStorageManager.class),
        SQLITE("sqlite", SqliteStorageManager.class);

        @JsonValue
        private String name;

        private Class<? extends AbstractStorageManager> storageManager;

        StorageType(String name, Class<? extends AbstractStorageManager> storageManager) {
            this.name = name;
            this.storageManager = storageManager;
        }
    }
}
