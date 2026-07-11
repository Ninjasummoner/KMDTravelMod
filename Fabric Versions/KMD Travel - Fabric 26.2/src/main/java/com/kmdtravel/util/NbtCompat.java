package com.kmdtravel.util;

import net.minecraft.nbt.CompoundTag;

import java.util.Optional;
import java.util.UUID;

public final class NbtCompat {
    private NbtCompat() {
    }

    public static void putUuid(CompoundTag tag, String key, UUID uuid) {
        tag.putString(key, uuid.toString());
    }

    public static UUID getUuid(CompoundTag tag, String key, UUID fallback) {
        Optional<String> value = tag.getString(key);
        if (value.isEmpty()) {
            return fallback;
        }
        try {
            return UUID.fromString(value.get());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
