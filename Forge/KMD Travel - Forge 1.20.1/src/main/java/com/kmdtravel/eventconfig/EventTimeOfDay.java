package com.kmdtravel.eventconfig;

public enum EventTimeOfDay {
    BOTH,
    DAY,
    NIGHT;

    public boolean matches(boolean day) {
        return this == BOTH || (this == DAY && day) || (this == NIGHT && !day);
    }

    public static EventTimeOfDay byName(String name) {
        for (EventTimeOfDay value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return BOTH;
    }
}

