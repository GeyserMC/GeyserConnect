package org.geysermc.multi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class Server {
    private String address;
    private int port;

    public Server(String address) {
        this(address, 25565);
    }
}
