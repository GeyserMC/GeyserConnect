package org.geysermc.multi.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Server {
    private String address;
    private int port = 25565;

    // Added so we can load from config
    public Server() {
        super();
    }

    public Server(String address) {
        this(address, 25565);
    }

    @Override
    public String toString() {
        return address + (port != 25565 ? ":" + port : "");
    }
}
