package org.geysermc.connect.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Player;
import org.geysermc.connect.utils.Server;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteStorageManager extends AbstractStorageManager {

    private final ObjectMapper mapper = new ObjectMapper();

    private Connection connection;

    @Override
    public void setupStorage() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:players.db");

            Statement createPlayersTable = connection.createStatement();
            createPlayersTable.executeUpdate("CREATE TABLE IF NOT EXISTS players (xuid TEXT, servers TEXT, PRIMARY KEY(xuid));");
            createPlayersTable.close();
        } catch (ClassNotFoundException | SQLException e) {
            MasterServer.getInstance().getLogger().severe("Unable to load sqlite database!", e);
        }
    }

    @Override
    public void closeStorage() {
        try {
            connection.close();
        } catch (SQLException e) { }
    }

    @Override
    public void saveServers(Player player) {
        try {
            Statement updatePlayersServers = connection.createStatement();
            updatePlayersServers.executeUpdate("INSERT OR REPLACE INTO players(xuid, servers) VALUES('" + player.getXuid() + "', '" + mapper.writeValueAsString(player.getServers()) + "');");
            updatePlayersServers.close();
        } catch (IOException | SQLException e) { }
    }

    @Override
    public List<Server> loadServers(Player player) {
        List<Server> servers = new ArrayList<>();

        try {
            Statement getPlayersServers = connection.createStatement();
            ResultSet rs = getPlayersServers.executeQuery("SELECT servers FROM players WHERE xuid='" + player.getXuid() + "';");

            List<Server> loadedServers = mapper.readValue(rs.getString("servers"), new TypeReference<List<Server>>(){});
            servers.addAll(loadedServers);

            getPlayersServers.close();
        } catch (IOException | SQLException e) { }

        return servers;
    }
}
