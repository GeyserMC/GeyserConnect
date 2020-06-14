package org.geysermc.multi.UI;

import com.nukkitx.protocol.bedrock.packet.ModalFormRequestPacket;
import org.geysermc.common.window.FormWindow;
import org.geysermc.common.window.SimpleFormWindow;
import org.geysermc.common.window.button.FormButton;
import org.geysermc.common.window.button.FormImage;
import org.geysermc.multi.Server;

import java.util.List;

public class UIHandler {
    public static ModalFormRequestPacket getServerListFormPacket(List<Server> servers) {
        SimpleFormWindow window = new SimpleFormWindow("Servers", "");

        for (Server server : servers) {
            window.getButtons().add(new FormButton(server.getAddress(), new FormImage(FormImage.FormImageType.URL, "https://eu.mc-api.net/v3/server/favicon/" + server.getAddress() + ":" + server.getPort() + ".png")));
        }

        return generatePacket(FormID.MAIN, window);
    }

    private static ModalFormRequestPacket generatePacket(FormID id, FormWindow form) {
        ModalFormRequestPacket modalFormRequestPacket = new ModalFormRequestPacket();
        modalFormRequestPacket.setFormId(id.ordinal());
        modalFormRequestPacket.setFormData(form.getJSONData()); // This fixes a bug in Geyser
        return modalFormRequestPacket;
    }
}
