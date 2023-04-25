package org.geysermc.connect.extension.config;

//import com.fasterxml.jackson.annotation.JsonProperty;
//import org.geysermc.connect.extension.storage.AbstractStorageManager;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.geysermc.connect.extension.storage.AbstractStorageManager;

public record CustomServersSection(
    boolean enabled,
    int max,
    @JsonProperty("storage-type") AbstractStorageManager.StorageType storageType,
    MySQLConnectionSection mysql) {
}
