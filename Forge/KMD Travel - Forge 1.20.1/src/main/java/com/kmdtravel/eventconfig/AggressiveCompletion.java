package com.kmdtravel.eventconfig;

public enum AggressiveCompletion {
    KILL_MOBS,
    TIMED;

    public static AggressiveCompletion byName(String name) {
        for (AggressiveCompletion completion : values()) {
            if (completion.name().equalsIgnoreCase(name)) {
                return completion;
            }
        }
        return KILL_MOBS;
    }
}

