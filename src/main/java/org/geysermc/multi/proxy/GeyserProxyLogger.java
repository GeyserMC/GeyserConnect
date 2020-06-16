package org.geysermc.multi.proxy;

import lombok.extern.log4j.Log4j2;
import org.geysermc.multi.MasterServer;
import org.geysermc.multi.utils.Logger;

@Log4j2
public class GeyserProxyLogger extends Logger {
    /**
     * Disable debug messages depending on config
     */
    public void debug(String message) {
        if (MasterServer.getInstance().getGeyserMultiConfig().getGeyser().isDebugMode())
            super.debug(message);
    }
}
