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

import com.nukkitx.nbt.*;
import org.geysermc.connector.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is mostly copied from core Geyser
 */
public class PaletteManger {

    public static final NbtList<NbtMap> BLOCK_PALETTE;
    public static final NbtMap BIOMES_PALETTE;
    public static final byte[] EMPTY_LEVEL_CHUNK_DATA;

    private static final NbtMap EMPTY_TAG = NbtMap.EMPTY;

    static {
        /* Load block palette */
        // Build the air block entry
        NbtMapBuilder mainBuilder = NbtMap.builder();
        mainBuilder.putShort("id", (short) 0);

        NbtMapBuilder blockBuilder = NbtMap.builder();
        blockBuilder.putString("name", "minecraft:air");
        blockBuilder.putInt("version", 17825806);
        blockBuilder.put("states", NbtMap.EMPTY);

        mainBuilder.put("block", blockBuilder.build());

        // Build the block list with the entry
        List<NbtMap> blocks = new ArrayList<>();
        blocks.add(mainBuilder.build());

        BLOCK_PALETTE = new NbtList<>(NbtType.COMPOUND, blocks);

        /* Load biomes */
        // Build a fake plains biome entry
        NbtMapBuilder plainsBuilder = NbtMap.builder();
        plainsBuilder.putFloat("blue_spores", 0f);
        plainsBuilder.putFloat("white_ash", 0f);
        plainsBuilder.putFloat("ash", 0f);
        plainsBuilder.putFloat("temperature", 0f);
        plainsBuilder.putFloat("red_spores", 0f);
        plainsBuilder.putFloat("downfall", 0f);

        plainsBuilder.put("minecraft:overworld_generation_rules", NbtMap.EMPTY);
        plainsBuilder.put("minecraft:climate", NbtMap.EMPTY);
        plainsBuilder.put("tags", NbtList.EMPTY);

        // Add the fake plains to the map
        NbtMapBuilder biomesBuilder = NbtMap.builder();
        biomesBuilder.put("plains", plainsBuilder.build());

        // Build the biomes palette
        BIOMES_PALETTE = biomesBuilder.build();

        /* Create empty chunk data */
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(new byte[258]); // Biomes + Border Size + Extra Data Size

            try (NBTOutputStream nbtOutputStream = NbtUtils.createNetworkWriter(outputStream)) {
                nbtOutputStream.writeTag(EMPTY_TAG);
            }

            EMPTY_LEVEL_CHUNK_DATA = outputStream.toByteArray();
        } catch (IOException e) {
            throw new AssertionError("Unable to generate empty level chunk data");
        }
    }

    public static void init() {
        // no-op
    }
}
