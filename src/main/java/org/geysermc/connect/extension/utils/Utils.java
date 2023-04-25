package org.geysermc.connect.extension.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geysermc.connect.extension.GeyserConnect;
import org.geysermc.geyser.session.GeyserSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static List<Server> getServers(ServerCategory category) {
        return GeyserConnect.instance().config().servers().stream().filter(server -> server.category() == category).toList();
    }

    public static File fileOrCopiedFromResource(String fileName, String name) throws IOException {
        File file = GeyserConnect.instance().dataFolder().resolve(fileName).toFile();

        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(new File(GeyserConnect.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath(), Collections.emptyMap())) {
                    try (InputStream input = Files.newInputStream(fileSystem.getPath(name))) {
                        byte[] bytes = new byte[input.available()];

                        input.read(bytes);

                        writer.write(new String(bytes).toCharArray());

                        writer.flush();
                    }
                }
            } catch (URISyntaxException ignored) { }
        }

        return file;
    }

    public static String displayName(GeyserSession session) {
        return session.bedrockUsername() + " (" + session.xuid() + ")";
    }
}
