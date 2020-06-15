package org.geysermc.connector.network.remote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * This is super hacky but I guess it works
 */
@Getter
@Setter
@AllArgsConstructor
public class RemoteServer {

    private String address;
    private int port;
}