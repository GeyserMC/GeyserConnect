package org.geysermc.multi.ui;

public enum FormID {
    MAIN,
    DIRECT_CONNECT,
    ADD_SERVER,
    REMOVE_SERVER,
    ERROR;

    private static final FormID[] VALUES = values();

    public static FormID fromId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : ERROR;
    }
}