package org.geysermc.multi.proxy;

import lombok.extern.log4j.Log4j2;
import org.geysermc.multi.utils.Logger;

@Log4j2
public class GeyserProxyLogger extends Logger {
    /**
     * Disable debug messages
     */
    public void debug(String message) { }
}
