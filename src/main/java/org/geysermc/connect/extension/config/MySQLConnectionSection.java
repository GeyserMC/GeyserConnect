package org.geysermc.connect.extension.config;

public record MySQLConnectionSection(
    String user,
    String pass,
    String database,
    String host,
    int port) {
}
