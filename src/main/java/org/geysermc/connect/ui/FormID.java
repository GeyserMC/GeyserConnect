package org.geysermc.connect.ui;

import lombok.Getter;

@Getter
public enum FormID {

    MAIN,
    DIRECT_CONNECT(true),
    EDIT_SERVERS(true),
    ADD_SERVER,
    REMOVE_SERVER,
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