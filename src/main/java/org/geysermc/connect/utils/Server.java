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

package org.geysermc.connect.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.api.network.AuthType;
import org.geysermc.geyser.api.network.RemoteServer;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Server implements RemoteServer {

    private String address;
    private int port = -1;
    private boolean online = true;
    private boolean bedrock = false;
    private String name = null;
    private String imageUrl = null;
    private ServerCategory category = null;

    public Server(String address) {
        this(address, -1);
    }

    public Server(String address, int port) {
        this(address, port, true);
    }

    public Server(String address, int port, boolean online) {
        this(address, port, online, false);
    }

    public Server(String address, int port, boolean online, boolean bedrock) {
        this(address, port, online, bedrock, null);
    }

    public Server(String address, int port, boolean online, boolean bedrock, String name) {
        this(address, port, online, bedrock, name, null);
    }

    public Server(String address, int port, boolean online, boolean bedrock, String name, String imageUrl) {
        this(address.replaceAll(" ", ""), port, online, bedrock, name, imageUrl, ServerCategory.CUSTOM);
    }

    private int defaultPort() { return bedrock ? 19132 : 25565; }

    public int getPort() { return port < 0 ? defaultPort() : port; }

    @Override
    public String toString() {
        return name != null ? name : address + (getPort() != defaultPort() ? ":" + getPort() : "");
    }

    @JsonIgnore
    public FormImage getFormImage() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return FormImage.of(FormImage.Type.URL, imageUrl);
        } else {
            return FormImage.of(FormImage.Type.URL, "https://eu.mc-api.net/v3/server/favicon/" + address + ":" + port + ".png?use-fallback-icon=true");
        }
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public @NonNull AuthType authType() {
        return this.online ? AuthType.ONLINE : AuthType.OFFLINE;
    }

    @Override
    public String minecraftVersion() {
        return null;
    }

    @Override
    public int protocolVersion() {
        return 0;
    }
}
