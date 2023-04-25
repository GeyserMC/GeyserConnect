/*
 * Copyright (c) 2019-2023 GeyserMC. http://geysermc.org
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

package org.geysermc.connect.extension.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.geysermc.geyser.api.extension.Extension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;

public class ConfigLoader {
    public static <T> T load(Extension extension, Class<?> extensionClass, Class<T> configClass) {
        File configFile = extension.dataFolder().resolve("config.yml").toFile();

        // Ensure the data folder exists
        if (!extension.dataFolder().toFile().exists()) {
            if (!extension.dataFolder().toFile().mkdirs()) {
                extension.logger().error("Failed to create data folder");
                return null;
            }
        }

        // Create the config file if it doesn't exist
        if (!configFile.exists()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(new File(extensionClass.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath(), Collections.emptyMap())) {
                    try (InputStream input = Files.newInputStream(fileSystem.getPath("config.yml"))) {
                        byte[] bytes = new byte[input.available()];

                        input.read(bytes);

                        writer.write(new String(bytes).toCharArray());

                        writer.flush();
                    }
                }
            } catch (IOException | URISyntaxException e) {
                extension.logger().error("Failed to create config", e);
                return null;
            }
        }

        // Load the config file
        try {
            return new ObjectMapper(new YAMLFactory())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(configFile, configClass);
        } catch (IOException e) {
            extension.logger().error("Failed to load config", e);
            return null;
        }
    }
}
