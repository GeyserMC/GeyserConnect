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

import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.*;
import com.nukkitx.protocol.bedrock.packet.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.geysermc.connect.GeyserConnect;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.proxy.GeyserProxySession;
import org.geysermc.connect.ui.FormID;
import org.geysermc.connect.ui.UIHandler;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.common.AuthType;
import org.geysermc.connector.network.UpstreamPacketHandler;
import org.geysermc.connector.network.session.auth.AuthData;
import org.geysermc.connector.network.session.auth.BedrockClientData;
import org.geysermc.connector.registry.BlockRegistries;
import org.geysermc.connector.registry.Registries;
import org.geysermc.connector.registry.type.ItemMappings;
import org.geysermc.connector.utils.DimensionUtils;
import org.geysermc.cumulus.Form;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Player {

    private final AuthData authData;

    private final BedrockServerSession session;

    private final List<Server> servers = new ArrayList<>();
    private final Long2ObjectMap<ModalFormRequestPacket> forms = new Long2ObjectOpenHashMap<>();

    private Form currentWindow;
    private FormID currentWindowId;

    @Setter
    private Server currentServer;

    @Setter
    private BedrockClientData clientData;

    @Setter
    private ServerCategory serverCategory;

    public Player(AuthData authData, BedrockServerSession session) {
        this.authData = authData;
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
        ItemMappings itemMappings = Registries.ITEMS.forVersion(session.getPacketCodec().getProtocolVersion());

        // A lot of this likely doesn't need to be changed
        StartGamePacket startGamePacket = new StartGamePacket();
        startGamePacket.setUniqueEntityId(1);
        startGamePacket.setRuntimeEntityId(1);
        startGamePacket.setPlayerGameType(GameType.CREATIVE);
        startGamePacket.setPlayerPosition(Vector3f.from(0, 64 + 2, 0));
        startGamePacket.setRotation(Vector2f.ONE);

        startGamePacket.setSeed(-1);
        startGamePacket.setDimensionId(2);
        startGamePacket.setGeneratorId(1);
        startGamePacket.setLevelGameType(GameType.CREATIVE);
        startGamePacket.setDifficulty(0);
        startGamePacket.setDefaultSpawn(Vector3i.ZERO);
        startGamePacket.setAchievementsDisabled(true);
        startGamePacket.setCurrentTick(-1);
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
        startGamePacket.setLevelName("GeyserConnect");
        startGamePacket.setPremiumWorldTemplateId("");
        startGamePacket.setCurrentTick(0);
        startGamePacket.setEnchantmentSeed(0);
        startGamePacket.setMultiplayerCorrelationId("");
        startGamePacket.setItemEntries(itemMappings.getItemEntries());
        startGamePacket.setInventoriesServerAuthoritative(true);
        startGamePacket.setServerEngine("");

        SyncedPlayerMovementSettings settings = new SyncedPlayerMovementSettings();
        settings.setMovementMode(AuthoritativeMovementMode.CLIENT);
        settings.setRewindHistorySize(0);
        settings.setServerAuthoritativeBlockBreaking(false);
        startGamePacket.setPlayerMovementSettings(settings);
        
        startGamePacket.setVanillaVersion("*");
        session.sendPacket(startGamePacket);

        if (itemMappings.getFurnaceMinecartData() != null) {
            ItemComponentPacket itemComponentPacket = new ItemComponentPacket();
            itemComponentPacket.getItems().add(itemMappings.getFurnaceMinecartData());
            session.sendPacket(itemComponentPacket);
        }

        // Send an empty chunk
        LevelChunkPacket data = new LevelChunkPacket();
        data.setChunkX(0);
        data.setChunkZ(0);
        data.setSubChunksLength(0);
        data.setData(PaletteManger.EMPTY_LEVEL_CHUNK_DATA);
        data.setCachingEnabled(false);
        session.sendPacket(data);

        // Send the biomes
        BiomeDefinitionListPacket biomeDefinitionListPacket = new BiomeDefinitionListPacket();
        biomeDefinitionListPacket.setDefinitions(Registries.BIOMES_NBT.get());
        session.sendPacket(biomeDefinitionListPacket);

        AvailableEntityIdentifiersPacket entityPacket = new AvailableEntityIdentifiersPacket();
        entityPacket.setIdentifiers(Registries.ENTITY_IDENTIFIERS.get());
        session.sendPacket(entityPacket);

        // Send a CreativeContentPacket - required for 1.16.100
        CreativeContentPacket creativeContentPacket = new CreativeContentPacket();
        creativeContentPacket.setContents(itemMappings.getCreativeItems());
        session.sendPacket(creativeContentPacket);

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
     * @param window The {@link Form} to turn into json and send
     */
    public void sendWindow(FormID id, Form window) {
        this.currentWindow = window;
        this.currentWindowId = id;

        ModalFormRequestPacket modalFormRequestPacket = new ModalFormRequestPacket();
        modalFormRequestPacket.setFormId(id.ordinal());
        modalFormRequestPacket.setFormData(window.getJsonData());
        session.sendPacket(modalFormRequestPacket);

        // This packet is used to fix the image loading bug
        NetworkStackLatencyPacket networkStackLatencyPacket = new NetworkStackLatencyPacket();
        networkStackLatencyPacket.setFromServer(true);
        networkStackLatencyPacket.setTimestamp(System.currentTimeMillis());
        session.sendPacket(networkStackLatencyPacket);
    }

    public void resendWindow() {
        sendWindow(currentWindowId, currentWindow);
    }

    /**
     * Send the player to the Geyser proxy server or straight to the bedrock server if it is
     */
    public void connectToProxy() {
        if (currentServer.isBedrock()) {
            TransferPacket transferPacket = new TransferPacket();
            transferPacket.setAddress(currentServer.getAddress());
            transferPacket.setPort(currentServer.getPort());
            session.sendPacket(transferPacket);
        } else {
            GeyserProxySession geyserSession = new GeyserProxySession(GeyserConnector.getInstance(), session, MasterServer.getInstance().getEventLoopGroup().next());
            session.setPacketHandler(new UpstreamPacketHandler(GeyserConnector.getInstance(), geyserSession));

            geyserSession.getUpstream().getSession().setPacketCodec(session.getPacketCodec());

            // Set the block translation based off of version
            geyserSession.setBlockMappings(BlockRegistries.BLOCKS.forVersion(session.getPacketCodec().getProtocolVersion()));
            geyserSession.setItemMappings(Registries.ITEMS.forVersion(session.getPacketCodec().getProtocolVersion()));

            geyserSession.setAuthData(authData);
            geyserSession.setClientData(clientData);

            geyserSession.setDimension(DimensionUtils.THE_END);

            geyserSession.setRemoteAddress(currentServer.getAddress());
            geyserSession.setRemotePort(currentServer.getPort());
            geyserSession.setRemoteAuthType(currentServer.isOnline() ? AuthType.ONLINE : AuthType.OFFLINE);

            // Tell Geyser to handle the login
            SetLocalPlayerAsInitializedPacket initializedPacket = new SetLocalPlayerAsInitializedPacket();
            initializedPacket.setRuntimeEntityId(geyserSession.getPlayerEntity().getGeyserId());
            session.getPacketHandler().handle(initializedPacket);
        }
    }

    public void sendToServer(Server server) {
        // Tell the user we are connecting them
        // This wont show up in a lot of cases as the client connects quite quickly
        if (!server.isOnline()) {
            sendWindow(FormID.CONNECTING, UIHandler.getWaitingScreen(server));
        }

        // Send the user over to the server
        setCurrentServer(server);
        connectToProxy();
    }

    public List<Server> getCurrentServers() {
        if (serverCategory == ServerCategory.CUSTOM) {
            return servers;
        }

        return MasterServer.getInstance().getServers(serverCategory);
    }
}
