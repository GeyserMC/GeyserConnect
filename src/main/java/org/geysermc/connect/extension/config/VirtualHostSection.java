package org.geysermc.connect.extension.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VirtualHostSection(
    boolean enabled,
    @JsonProperty("base-domain") String baseDomain) {
}
