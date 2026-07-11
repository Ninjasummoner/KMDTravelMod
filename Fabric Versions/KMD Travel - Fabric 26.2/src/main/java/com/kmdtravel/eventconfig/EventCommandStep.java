package com.kmdtravel.eventconfig;

import net.minecraft.nbt.CompoundTag;

public record EventCommandStep(String command, int delaySeconds) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Command", command);
        tag.putInt("DelaySeconds", Math.max(0, delaySeconds));
        return tag;
    }

    public static EventCommandStep load(CompoundTag tag) {
        return new EventCommandStep(tag.getStringOr("Command", ""), Math.max(0, tag.getIntOr("DelaySeconds", 0)));
    }
}
