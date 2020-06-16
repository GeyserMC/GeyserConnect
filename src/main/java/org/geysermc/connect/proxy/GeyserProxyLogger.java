package org.geysermc.connect.proxy;

import lombok.extern.log4j.Log4j2;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Logger;

@Log4j2
public class GeyserProxyLogger extends Logger {
    /**
     * Disable debug messages depending on config
     */
    public void debug(String message) {
        if (MasterServer.getInstance().getGeyserConnectConfig().getGeyser().isDebugMode())
            super.debug(message);
    }
}
