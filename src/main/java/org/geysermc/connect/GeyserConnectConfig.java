package org.geysermc.connect;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.geysermc.connector.utils.WebUtils;
import org.geysermc.connect.storage.AbstractStorageManager;
import org.geysermc.connect.utils.Server;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeyserConnectConfig {

    private String address;

    @JsonProperty("remote-address")
    private String remoteAddress;

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

    public void checkRemoteIP() {
        if ("auto".equals(remoteAddress)) {
            remoteAddress = WebUtils.getBody("https://icanhazip.com/").trim();
            MasterServer.getInstance().getLogger().debug("Auto set remote IP to: " + remoteAddress);
        }
    }

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
        private AbstractStorageManager.StorageType storageType;

        private MySQLConnectionSection mysql;
    }

    @Getter
    public static class MySQLConnectionSection {

        private String user;
        private String pass;
        private String database;
        private String host;
        private int port;
    }
}
