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

package org.geysermc.connect.extension.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.api.network.AuthType;
import org.geysermc.geyser.api.network.RemoteServer;

public record Server(
    String address,
    int port,
    boolean online,
    boolean bedrock,
    String name,
    String imageUrl,
    ServerCategory category
) implements RemoteServer {
    public Server(String address, int port) {
        this(address, port, true, false, null, null, ServerCategory.CUSTOM);
    }

    private int defaultPort() { return bedrock ? 19132 : 25565; }

    @Override
    public int port() {
        return port < 0 ? defaultPort() : port;
    }

    @Override
    public @NonNull AuthType authType() {
        return this.online ? AuthType.ONLINE : AuthType.OFFLINE;
    }

    @JsonIgnore
    public FormImage formImage() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return FormImage.of(FormImage.Type.URL, imageUrl);
        } else {
            return FormImage.of(FormImage.Type.URL, "https://eu.mc-api.net/v3/server/favicon/" + address + ":" + port + ".png?use-fallback-icon=true");
        }
    }

    @Override
    public String minecraftVersion() {
        return null;
    }

    @Override
    public int protocolVersion() {
        return 0;
    }

    public String title() {
        return name != null ? name : address + (port() != defaultPort() ? ":" + port() : "");
    }
}
