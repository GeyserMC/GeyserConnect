/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSQLStorageManager extends AbstractStorageManager {
    private final ObjectMapper mapper = new ObjectMapper();

    protected Connection connection;

    @Override
    public void setupStorage() {
        try {
            connectToDatabase();

            try (Statement createPlayersTable = connection.createStatement()) {
                createPlayersTable.executeUpdate("CREATE TABLE IF NOT EXISTS players (xuid VARCHAR(32), servers TEXT, PRIMARY KEY(xuid));");
            }
        } catch (ClassNotFoundException | SQLException e) {
            MasterServer.getInstance().getLogger().severe("Unable to connect to MySQL database!", e);
        }
    }

    protected abstract void connectToDatabase() throws ClassNotFoundException, SQLException;

    @Override
    public void closeStorage() {
        try {
            connection.close();
        } catch (SQLException exception) {
            MasterServer.getInstance().getLogger().error("Failed to close SQL connection", exception);
        }
    }

    @Override
    public void saveServers(Player player) {
        // replace into works on MySQL and SQLite
        try (PreparedStatement updatePlayersServers = connection.prepareStatement("REPLACE INTO players(xuid, servers) VALUES(?, ?)")) {
            updatePlayersServers.setString(1, player.getAuthData().xuid());
            updatePlayersServers.setString(2, mapper.writeValueAsString(player.getServers()));
            updatePlayersServers.executeUpdate();
        } catch (IOException | SQLException exception) {
            MasterServer.getInstance().getLogger().error("Couldn't save servers for " + player.getAuthData().name(), exception);
        }
    }

    @Override
    public List<Server> loadServers(Player player) {
        List<Server> servers = new ArrayList<>();

        try (PreparedStatement getPlayersServers = connection.prepareStatement("SELECT servers FROM players WHERE xuid=?")) {
            getPlayersServers.setString(1, player.getAuthData().xuid());
            ResultSet rs = getPlayersServers.executeQuery();

            while (rs.next()) {
                List<Server> loadedServers = mapper.readValue(rs.getString("servers"), new TypeReference<>() {
                });
                servers.addAll(loadedServers);
            }
        } catch (IOException | SQLException exception) {
            MasterServer.getInstance().getLogger().error("Couldn't load servers for " + player.getAuthData().name(), exception);
        }

        return servers;
    }
}
