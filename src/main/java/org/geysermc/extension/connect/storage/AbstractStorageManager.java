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

import com.fasterxml.jackson.annotation.JsonValue;
import org.geysermc.api.connection.Connection;
import org.geysermc.extension.connect.utils.Server;

import java.util.ArrayList;
import java.util.List;

public class AbstractStorageManager {

    public void setupStorage() {
    }

    public void closeStorage() {
    }

    public void saveServers(Connection session) {
    }

    public List<Server> loadServers(Connection session) {
        return new ArrayList<>();
    }

    public enum StorageType {
        JSON("json", JsonStorageManager.class),
        SQLITE("sqlite", SQLiteStorageManager.class),
        MYSQL("mysql", MySQLStorageManager.class);

        @JsonValue
        private final String configName;

        private final Class<? extends AbstractStorageManager> storageManager;

        StorageType(String configName, Class<? extends AbstractStorageManager> storageManager) {
            this.configName = configName;
            this.storageManager = storageManager;
        }

        public String configName() {
            return configName;
        }

        public Class<? extends AbstractStorageManager> storageManager() {
            return storageManager;
        }
    }
}
