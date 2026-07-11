package com.kmdtravel.travel;

import com.kmdtravel.util.NbtCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record TravelLocation(UUID id, String name, Identifier dimension, BlockPos pos, boolean shared, int markerColor, int markerPattern) {
    public TravelLocation(UUID id, String name, Identifier dimension, BlockPos pos) {
        this(id, name, dimension, pos, false, 0x2F5D46, 0);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        NbtCompat.putUuid(tag, "Id", id);
        tag.putString("Name", name);
        tag.putString("Dimension", dimension.toString());
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        tag.putBoolean("Shared", shared);
        tag.putInt("MarkerColor", markerColor);
        tag.putInt("MarkerPattern", markerPattern);
        return tag;
    }

    public static TravelLocation load(CompoundTag tag) {
        return new TravelLocation(
                NbtCompat.getUuid(tag, "Id", UUID.randomUUID()),
                tag.getStringOr("Name", "Travel Post"),
                Identifier.parse(tag.getStringOr("Dimension", "minecraft:overworld")),
                new BlockPos(tag.getIntOr("X", 0), tag.getIntOr("Y", 64), tag.getIntOr("Z", 0)),
                tag.getBooleanOr("Shared", false),
                tag.getIntOr("MarkerColor", 0x2F5D46),
                tag.getIntOr("MarkerPattern", 0));
    }
}
