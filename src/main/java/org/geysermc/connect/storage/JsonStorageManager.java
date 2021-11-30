/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserConnect
 */

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
            mapper.writeValue(dataFolder.resolve(player.getAuthData().xuid() + ".json").toFile(), player.getServers());
        } catch (IOException ignored) { }
    }

    @Override
    public List<Server> loadServers(Player player) {
        List<Server> servers = new ArrayList<>();

        try {
            List<Server> loadedServers = mapper.readValue(dataFolder.resolve(player.getAuthData().xuid() + ".json").toFile(), new TypeReference<>(){});
            servers.addAll(loadedServers);
        } catch (IOException ignored) { }

        return servers;
    }
}
