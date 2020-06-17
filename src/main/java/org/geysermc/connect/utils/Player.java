/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 *  @author GeyserMC
 *  @link https://github.com/GeyserMC/GeyserConnect
 *
 */

package org.geysermc.connect.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.GamePublishSetting;
import com.nukkitx.protocol.bedrock.data.GameRuleData;
import com.nukkitx.protocol.bedrock.data.PlayerPermission;
import com.nukkitx.protocol.bedrock.packet.*;
import lombok.Getter;
import lombok.Setter;
import org.geysermc.common.window.FormWindow;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.ui.FormID;
import org.geysermc.connect.ui.UIHandler;

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
        if (MasterServer.getInstance().getGeyserConnectConfig().getCustomServers().isEnabled()) {
            servers.addAll(MasterServer.getInstance().getStorageManager().loadServers(this));
        }
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
        transferPacket.setAddress(MasterServer.getInstance().getGeyserConnectConfig().getRemoteAddress());
        transferPacket.setPort(MasterServer.getInstance().getGeyserConnectConfig().getGeyser().getPort());
        session.sendPacket(transferPacket);
    }

    public void sendToServer(Server server) {
        // Tell the user we are connecting them
        // this wont show up in alot of cases as the client connects quite quickly
        sendWindow(FormID.CONNECTING, UIHandler.getWaitingScreen(server));

        // Create the Geyser instance if its not already running
        MasterServer.getInstance().createGeyserProxy();

        // Send the user over to the server
        setCurrentServer(server);
        connectToProxy();
    }
}
