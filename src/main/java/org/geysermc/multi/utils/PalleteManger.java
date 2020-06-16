package org.geysermc.multi.utils;

import com.nukkitx.nbt.CompoundTagBuilder;
import com.nukkitx.nbt.NbtUtils;
import com.nukkitx.nbt.stream.NBTInputStream;
import com.nukkitx.nbt.stream.NBTOutputStream;
import com.nukkitx.nbt.tag.CompoundTag;
import com.nukkitx.nbt.tag.ListTag;
import org.geysermc.connector.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is mostly copied from core Geyser
 */
public class PalleteManger {
    public static final ListTag<CompoundTag> BLOCK_PALLETE;
    public static final CompoundTag BIOMES_PALLETE;
    public static final byte[] EMPTY_LEVEL_CHUNK_DATA;

    private static final com.nukkitx.nbt.tag.CompoundTag EMPTY_TAG = CompoundTagBuilder.builder().buildRootTag();

    static {
        /* Load block palette */
        InputStream stream = FileUtils.getResource("runtime_block_states.dat");

        try (NBTInputStream nbtInputStream = NbtUtils.createNetworkReader(stream)) {
            BLOCK_PALLETE = (ListTag<CompoundTag>) nbtInputStream.readTag();
        } catch (Exception e) {
            throw new AssertionError("Unable to get blocks from runtime block states", e);
        }

        /* Load biomes */
        stream = FileUtils.getResource("biome_definitions.dat");

        try (NBTInputStream nbtInputStream = NbtUtils.createNetworkReader(stream)){
            BIOMES_PALLETE = (CompoundTag) nbtInputStream.readTag();
        } catch (Exception e) {
            throw new AssertionError("Failed to get biomes from biome definitions", e);
        }

        /* Create empty chunk data */
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(new byte[258]); // Biomes + Border Size + Extra Data Size

            try (NBTOutputStream nbtOutputStream = NbtUtils.createNetworkWriter(outputStream)) {
                nbtOutputStream.write(EMPTY_TAG);
            }

            EMPTY_LEVEL_CHUNK_DATA = outputStream.toByteArray();
        }catch (IOException e) {
            throw new AssertionError("Unable to generate empty level chunk data");
        }
    }

    public static void init() {
        // no-op
    }
}
