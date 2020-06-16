package org.geysermc.connect.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonStorageManager extends AbstractStorageManager {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setupStorage() {
        File playersFolder = new File("players/");
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }

    @Override
    public void saveServers(Player player) {
        File dataFile = new File("players/" + player.getXuid() + ".json");
        try {
            mapper.writeValue(dataFile, player.getServers());
        } catch (IOException e) { }
    }

    @Override
    public List<Server> loadServers(Player player) {
        File dataFile = new File("players/" + player.getXuid() + ".json");
        List<Server> servers = new ArrayList<>();

        if (dataFile.exists()) {
            try {
                List<Server> loadedServers = mapper.readValue(dataFile, new TypeReference<List<Server>>(){});
                servers.addAll(loadedServers);
            } catch (IOException e) { }
        }

        return servers;
    }
}
