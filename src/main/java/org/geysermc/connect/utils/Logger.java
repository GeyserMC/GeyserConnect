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

import lombok.extern.log4j.Log4j2;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.core.config.Configurator;
import org.geysermc.connect.MasterServer;
import org.geysermc.geyser.GeyserLogger;
import org.geysermc.geyser.text.ChatColor;

@Log4j2
public class Logger extends SimpleTerminalConsole implements GeyserLogger {

    private final boolean colored = true;

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

    public boolean isDebug() {
        return log.isDebugEnabled();
    }
}
