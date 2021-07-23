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
import org.geysermc.connect.GeyserConnectConfig;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLStorageManager extends AbstractStorageManager {

    private final ObjectMapper mapper = new ObjectMapper();

    private Connection connection;

    @Override
    public void setupStorage() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            GeyserConnectConfig.MySQLConnectionSection connectionInformation = MasterServer.getInstance().getGeyserConnectConfig().getCustomServers().getMysql();
            connection = DriverManager.getConnection("jdbc:mysql://" + connectionInformation.getHost() + ":" + connectionInformation.getPort() + "/" + connectionInformation.getDatabase(), connectionInformation.getUser(), connectionInformation.getPass());

            Statement createPlayersTable = connection.createStatement();
            createPlayersTable.executeUpdate("CREATE TABLE IF NOT EXISTS players (xuid VARCHAR(32), servers TEXT, PRIMARY KEY(xuid));");
            createPlayersTable.close();
        } catch (ClassNotFoundException | SQLException e) {
            MasterServer.getInstance().getLogger().severe("Unable to connect to MySQL database!", e);
        }
    }

    @Override
    public void closeStorage() {
        try {
            connection.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public void saveServers(Player player) {
        try {
            Statement updatePlayersServers = connection.createStatement();
            updatePlayersServers.executeUpdate("REPLACE INTO players(xuid, servers) VALUES('" + player.getAuthData().getXboxUUID() + "', '" + mapper.writeValueAsString(player.getServers()) + "');");
            updatePlayersServers.close();
        } catch (IOException | SQLException ignored) { }
    }

    @Override
    public List<Server> loadServers(Player player) {
        List<Server> servers = new ArrayList<>();

        try {
            Statement getPlayersServers = connection.createStatement();
            ResultSet rs = getPlayersServers.executeQuery("SELECT servers FROM players WHERE xuid='" + player.getAuthData().getXboxUUID() + "';");

            while (rs.next()) {
                List<Server> loadedServers = mapper.readValue(rs.getString("servers"), new TypeReference<List<Server>>(){});
                servers.addAll(loadedServers);
            }

            getPlayersServers.close();
        } catch (IOException | SQLException ignored) { }

        return servers;
    }
}
