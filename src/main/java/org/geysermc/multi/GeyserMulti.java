package org.geysermc.multi;

import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.v390.Bedrock_v390;

public class GeyserMulti {

    public static final BedrockPacketCodec CODEC = Bedrock_v390.V390_CODEC;

    private static MasterServer masterServer;


    public static void main(String[] args) {
        new MasterServer();
    }
}
