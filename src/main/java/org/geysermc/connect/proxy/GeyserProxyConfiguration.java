package org.geysermc.connect.proxy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.geysermc.connector.configuration.GeyserJacksonConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeyserProxyConfiguration extends GeyserJacksonConfiguration {

    @JsonProperty("floodgate-key-file")
    private String floodgateKeyFile;

    @Override
    public Path getFloodgateKeyFile() {
        return Paths.get(floodgateKeyFile);
    }
}
