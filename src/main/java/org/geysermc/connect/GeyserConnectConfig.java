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

package org.geysermc.connect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.geysermc.connect.storage.AbstractStorageManager;
import org.geysermc.connect.utils.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeyserConnectConfig {

    private String address;

    private int port;

    @JsonProperty("max-players")
    private int maxPlayers;

    private String motd;

    private String submotd = "GeyserConnect";

    @JsonProperty("welcome-file")
    private String welcomeFile = "welcome.txt";

    @JsonProperty("debug-mode")
    private boolean debugMode;

    private GeyserConfigSection geyser;

    private List<Server> servers = new ArrayList<>();

    @JsonProperty("custom-servers")
    private CustomServersSection customServers;

    private VirtualHostSection vhost;

    @Getter
    public static class GeyserConfigSection {

        @JsonProperty("allow-password-authentication")
        private boolean allowPasswordAuthentication = false;

        @JsonProperty("debug-mode")
        private boolean debugMode;

        @JsonProperty("saved-user-logins")
        private List<String> savedUserLogins = Collections.emptyList();
    }

    @Getter
    public static class CustomServersSection {

        private boolean enabled;
        private int max;

        @JsonProperty("storage-type")
        private AbstractStorageManager.StorageType storageType;

        private MySQLConnectionSection mysql;
    }

    @Getter
    public static class MySQLConnectionSection {

        private String user;
        private String pass;
        private String database;
        private String host;
        private int port;
    }

    @Getter
    public static class VirtualHostSection {

        private boolean enabled;
        @JsonProperty("base-domain")
        private String baseDomain;
    }
}
