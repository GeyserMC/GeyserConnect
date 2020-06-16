package org.geysermc.multi;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeyserMultiConfig {
    private GeyserConfigSection geyser;

    private int port;

    @JsonProperty("max-players")
    private int maxPlayers;

    private String motd;

    @JsonProperty("debug-mode")
    private boolean debugMode;

    @Getter
    public static class GeyserConfigSection {
        private int port;

        @JsonProperty("debug-mode")
        private boolean debugMode;
    }
}
