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

import com.fasterxml.jackson.core.type.TypeReference;
import org.geysermc.extension.connect.GeyserConnect;
import org.geysermc.extension.connect.utils.Server;
import org.geysermc.extension.connect.utils.ServerManager;
import org.geysermc.extension.connect.utils.Utils;
import org.geysermc.geyser.session.GeyserSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSQLStorageManager extends AbstractStorageManager {
    protected Connection connection;

    @Override
    public void setupStorage() {
        try {
            connectToDatabase();

            try (Statement createPlayersTable = connection.createStatement()) {
                createPlayersTable.executeUpdate("CREATE TABLE IF NOT EXISTS players (xuid VARCHAR(32), servers TEXT, PRIMARY KEY(xuid));");
            }
        } catch (ClassNotFoundException | SQLException e) {
            GeyserConnect.instance().logger().severe("Unable to connect to MySQL database!", e);
        }
    }

    protected abstract void connectToDatabase() throws ClassNotFoundException, SQLException;

    @Override
    public void closeStorage() {
        try {
            connection.close();
        } catch (SQLException exception) {
            GeyserConnect.instance().logger().error("Failed to close SQL connection", exception);
        }
    }

    @Override
    public void saveServers(GeyserSession session) {
        // replace into works on MySQL and SQLite
        try (PreparedStatement updatePlayersServers = connection.prepareStatement("REPLACE INTO players(xuid, servers) VALUES(?, ?)")) {
            updatePlayersServers.setString(1, session.getAuthData().xuid());
            updatePlayersServers.setString(2, Utils.OBJECT_MAPPER.writeValueAsString(ServerManager.getServers(session)));
            updatePlayersServers.executeUpdate();
        } catch (IOException | SQLException exception) {
            GeyserConnect.instance().logger().error("Couldn't save servers for " + session.getAuthData().name(), exception);
        }
    }

    @Override
    public List<Server> loadServers(GeyserSession session) {
        List<Server> servers = new ArrayList<>();

        try (PreparedStatement getPlayersServers = connection.prepareStatement("SELECT servers FROM players WHERE xuid=?")) {
            getPlayersServers.setString(1, session.getAuthData().xuid());
            ResultSet rs = getPlayersServers.executeQuery();

            while (rs.next()) {
                List<Server> loadedServers = Utils.OBJECT_MAPPER.readValue(rs.getString("servers"), new TypeReference<>() {
                });
                if (loadedServers != null) {
                    servers.addAll(loadedServers);
                }
            }
        } catch (IOException | SQLException exception) {
            GeyserConnect.instance().logger().error("Couldn't load servers for " + session.getAuthData().name(), exception);
        }

        return servers;
    }
}
