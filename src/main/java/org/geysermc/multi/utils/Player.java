package org.geysermc.multi.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.*;
import com.nukkitx.protocol.bedrock.packet.*;
import lombok.Getter;
import lombok.Setter;
import org.geysermc.common.window.FormWindow;
import org.geysermc.multi.MasterServer;
import org.geysermc.multi.ui.FormID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Player {

    private String xuid;
    private UUID identity;
    private String displayName;

    private BedrockServerSession session;

    private final List<Server> servers = new ArrayList<>();

    private FormWindow currentWindow;
    private FormID currentWindowId;

    @Setter
    private Server currentServer;

    public Player(JsonNode extraData, BedrockServerSession session) {
        this.xuid = extraData.get("XUID").asText();
        this.identity = UUID.fromString(extraData.get("identity").asText());
        this.displayName = extraData.get("displayName").asText();

        this.session = session;

        // Should fetch the servers from some form of db
        servers.add(new Server("play.cubecraft.net"));
        servers.add(new Server("81.174.164.211", 25580));
    }

    /**
     * Send a few different packets to get the client to load in
     */
    public void sendStartGame() {
        // A lot of this likely doesn't need to be changed
        StartGamePacket startGamePacket = new StartGamePacket();
        startGamePacket.setUniqueEntityId(1);
        startGamePacket.setRuntimeEntityId(1);
        startGamePacket.setPlayerGamemode(0);
        startGamePacket.setPlayerPosition(Vector3f.from(0, 64 + 2, 0));
        startGamePacket.setRotation(Vector2f.ONE);

        startGamePacket.setSeed(-1);
        startGamePacket.setDimensionId(2);
        startGamePacket.setGeneratorId(1);
        startGamePacket.setLevelGamemode(1);
        startGamePacket.setDifficulty(0);
        startGamePacket.setDefaultSpawn(Vector3i.ZERO);
        startGamePacket.setAchievementsDisabled(true);
        startGamePacket.setTime(-1);
        startGamePacket.setEduEditionOffers(0);
        startGamePacket.setEduFeaturesEnabled(false);
        startGamePacket.setRainLevel(0);
        startGamePacket.setLightningLevel(0);
        startGamePacket.setMultiplayerGame(true);
        startGamePacket.setBroadcastingToLan(true);
        startGamePacket.getGamerules().add(new GameRuleData<>("showcoordinates", true));
        startGamePacket.setPlatformBroadcastMode(GamePublishSetting.PUBLIC);
        startGamePacket.setXblBroadcastMode(GamePublishSetting.PUBLIC);
        startGamePacket.setCommandsEnabled(true);
        startGamePacket.setTexturePacksRequired(false);
        startGamePacket.setBonusChestEnabled(false);
        startGamePacket.setStartingWithMap(false);
        startGamePacket.setTrustingPlayers(true);
        startGamePacket.setDefaultPlayerPermission(PlayerPermission.VISITOR);
        startGamePacket.setServerChunkTickRange(4);
        startGamePacket.setBehaviorPackLocked(false);
        startGamePacket.setResourcePackLocked(false);
        startGamePacket.setFromLockedWorldTemplate(false);
        startGamePacket.setUsingMsaGamertagsOnly(false);
        startGamePacket.setFromWorldTemplate(false);
        startGamePacket.setWorldTemplateOptionLocked(false);

        startGamePacket.setLevelId("");
        startGamePacket.setWorldName("GeyserMulti");
        startGamePacket.setPremiumWorldTemplateId("");
        startGamePacket.setCurrentTick(0);
        startGamePacket.setEnchantmentSeed(0);
        startGamePacket.setMultiplayerCorrelationId("");
        startGamePacket.setBlockPalette(PalleteManger.BLOCK_PALLETE);
        startGamePacket.setVanillaVersion("*");
        session.sendPacket(startGamePacket);

        // Send an empty chunk
        LevelChunkPacket data = new LevelChunkPacket();
        data.setChunkX(0);
        data.setChunkZ(0);
        data.setSubChunksLength(0);
        data.setData(PalleteManger.EMPTY_LEVEL_CHUNK_DATA);
        data.setCachingEnabled(false);
        session.sendPacket(data);

        // Send the biomes
        BiomeDefinitionListPacket biomeDefinitionListPacket = new BiomeDefinitionListPacket();
        biomeDefinitionListPacket.setTag(PalleteManger.BIOMES_PALLETE);
        session.sendPacket(biomeDefinitionListPacket);

        // Let the client know the player can spawn
        PlayStatusPacket playStatusPacket = new PlayStatusPacket();
        playStatusPacket.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
        session.sendPacket(playStatusPacket);

        // Freeze the player
        SetEntityMotionPacket setEntityMotionPacket = new SetEntityMotionPacket();
        setEntityMotionPacket.setRuntimeEntityId(1);
        setEntityMotionPacket.setMotion(Vector3f.ZERO);
        session.sendPacket(setEntityMotionPacket);
    }

    /**
     * Send a window with the specified id and content
     * Also cache it against the player for later use
     *
     * @param id The {@link FormID} to use for the form
     * @param window The {@link FormWindow} to turn into json and send
     */
    public void sendWindow(FormID id, FormWindow window) {
        this.currentWindow = window;
        this.currentWindowId = id;

        ModalFormRequestPacket modalFormRequestPacket = new ModalFormRequestPacket();
        modalFormRequestPacket.setFormId(id.ordinal());
        modalFormRequestPacket.setFormData(window.getJSONData());
        session.sendPacketImmediately(modalFormRequestPacket);
    }

    public void resendWindow() {
        sendWindow(currentWindowId, currentWindow);
    }

    /**
     * Send the player to the Geyser proxy server
     */
    public void connectToProxy() {
        TransferPacket transferPacket = new TransferPacket();
        transferPacket.setAddress("127.0.0.1"); // Need to find a good way of getting this
        transferPacket.setPort(MasterServer.getInstance().getGeyserProxy().getGeyserConfig().getBedrock().getPort());
        session.sendPacket(transferPacket);
    }
}
