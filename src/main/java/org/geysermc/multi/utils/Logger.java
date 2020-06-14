package org.geysermc.multi.utils;

import lombok.extern.log4j.Log4j2;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.core.config.Configurator;
import org.geysermc.common.ChatColor;
import org.geysermc.multi.MasterServer;

@Log4j2
public class Logger extends SimpleTerminalConsole {

    private boolean colored = true;

    @Override
    protected boolean isRunning() {
        return !MasterServer.getInstance().isShuttingDown();
    }

    @Override
    protected void runCommand(String line) {
        // Dont do anything rn
    }

    @Override
    protected void shutdown() {
        MasterServer.getInstance().shutdown();
    }

    public void severe(String message) {
        log.fatal(printConsole(ChatColor.DARK_RED + message, colored));
    }

    public void severe(String message, Throwable error) {
        log.fatal(printConsole(ChatColor.DARK_RED + message, colored), error);
    }

    public void error(String message) {
        log.error(printConsole(ChatColor.RED + message, colored));
    }

    public void error(String message, Throwable error) {
        log.error(printConsole(ChatColor.RED + message, colored), error);
    }

    public void warning(String message) {
        log.warn(printConsole(ChatColor.YELLOW + message, colored));
    }

    public void info(String message) {
        log.info(printConsole(ChatColor.WHITE + message, colored));
    }

    public void debug(String message) {
        log.debug(printConsole(ChatColor.GRAY + message, colored));
    }

    public static String printConsole(String message, boolean colors) {
        return colors ? ChatColor.toANSI(message + ChatColor.RESET) : ChatColor.stripColors(message + ChatColor.RESET);
    }

    public void setDebug(boolean debug) {
        Configurator.setLevel(log.getName(), debug ? org.apache.logging.log4j.Level.DEBUG : log.getLevel());
    }

    public String getName() {
        return "CONSOLE";
    }

    public void sendMessage(String message) {
        info(message);
    }

    public boolean isConsole() {
        return true;
    }
}
