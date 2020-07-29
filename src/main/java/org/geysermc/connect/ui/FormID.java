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

package org.geysermc.connect.ui;

import lombok.Getter;

@Getter
public enum FormID {

    MAIN,
    DIRECT_CONNECT(true),
    EDIT_SERVERS(true),
    SERVER_OPTIONS(true),
    ADD_SERVER(true),
    REMOVE_SERVER,
    EDIT_SERVER(true),
    CONNECTING,
    ERROR;

    private boolean handlesNull;

    private static final FormID[] VALUES = values();

    FormID() {
        this(false);
    }

    FormID(boolean handlesNull) {
        this.handlesNull = handlesNull;
    }

    public static FormID fromId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : ERROR;
    }
}