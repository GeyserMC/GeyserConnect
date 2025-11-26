/*
 * Copyright (c) 2019-2024 GeyserMC. http://geysermc.org
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

package org.geysermc.extension.connect.storage;

import com.google.gson.reflect.TypeToken;
import org.geysermc.api.connection.Connection;
import org.geysermc.extension.connect.GeyserConnect;
import org.geysermc.extension.connect.utils.Server;
import org.geysermc.extension.connect.utils.ServerManager;
import org.geysermc.extension.connect.utils.Utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonStorageManager extends AbstractStorageManager {
    private Path dataFolder;

    @Override
    public void setupStorage() {
        dataFolder = GeyserConnect.instance().dataFolder().resolve("players/");
        if (!dataFolder.toFile().exists()) {
            dataFolder.toFile().mkdirs();
        }
    }

    @Override
    public void saveServers(Connection session) {
        try (FileWriter writer = new FileWriter(dataFolder.resolve(session.xuid() + ".json").toFile())) {
            writer.write(Utils.GSON.toJson(ServerManager.getServers(session)));
        } catch (IOException ignored) {
        }
    }

    @Override
    public List<Server> loadServers(Connection session) {
        List<Server> servers = new ArrayList<>();

        try (FileReader reader = new FileReader(dataFolder.resolve(session.xuid() + ".json").toFile())) {
            List<Server> loadedServers = Utils.GSON.fromJson(reader, new TypeToken<>() {
            });
            if (loadedServers != null) {
                servers.addAll(loadedServers);
            }
        } catch (IOException ignored) {
        }

        return servers;
    }
}
