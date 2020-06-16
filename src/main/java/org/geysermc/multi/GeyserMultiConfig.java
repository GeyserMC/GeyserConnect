package org.geysermc.multi;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.geysermc.multi.utils.PlayerStorageManager;
import org.geysermc.multi.utils.Server;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeyserMultiConfig {

    private int port;

    @JsonProperty("max-players")
    private int maxPlayers;

    private String motd;

    @JsonProperty("debug-mode")
    private boolean debugMode;

    private GeyserConfigSection geyser;

    private List<Server> servers;

    @JsonProperty("custom-servers")
    private CustomServersSection customServers;

    @Getter
    public static class GeyserConfigSection {

        private int port;

        @JsonProperty("debug-mode")
        private boolean debugMode;
    }

    @Getter
    public static class CustomServersSection {

        private boolean enabled;
        private int max;

        @JsonProperty("storage-type")
        private PlayerStorageManager.StorageType storageType;
    }
}
