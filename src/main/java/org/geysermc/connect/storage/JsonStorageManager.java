package org.geysermc.connect.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonStorageManager extends AbstractStorageManager {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path dataFolder = Paths.get("players/");

    @Override
    public void setupStorage() {
        if (!dataFolder.toFile().exists()) {
            dataFolder.toFile().mkdirs();
        }
    }

    @Override
    public void saveServers(Player player) {
        try {
            mapper.writeValue(dataFolder.resolve(player.getXuid() + ".json").toFile(), player.getServers());
        } catch (IOException e) { }
    }

    @Override
    public List<Server> loadServers(Player player) {
        List<Server> servers = new ArrayList<>();

        try {
            List<Server> loadedServers = mapper.readValue(dataFolder.resolve(player.getXuid() + ".json").toFile(), new TypeReference<List<Server>>(){});
            servers.addAll(loadedServers);
        } catch (IOException e) { }

        return servers;
    }
}
