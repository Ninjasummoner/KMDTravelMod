package com.kmdtravel.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record TravelLocation(UUID id, String name, ResourceLocation dimension, BlockPos pos, boolean shared, int markerColor, int markerPattern) {
    public TravelLocation(UUID id, String name, ResourceLocation dimension, BlockPos pos) {
        this(id, name, dimension, pos, false, 0x2F5D46, 0);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
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
                tag.getUUID("Id"),
                tag.getString("Name"),
                ResourceLocation.parse(tag.getString("Dimension")),
                new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z")),
                tag.getBoolean("Shared"),
                tag.contains("MarkerColor") ? tag.getInt("MarkerColor") : 0x2F5D46,
                tag.contains("MarkerPattern") ? tag.getInt("MarkerPattern") : 0);
    }
}
