package com.kmdtravel.block;

import com.kmdtravel.registry.KMDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FastTravelPostBlockEntity extends BlockEntity {
    private UUID locationId = UUID.randomUUID();
    private Component postName = Component.literal("Travel Post");
    private int textColor = 0x000000;
    private boolean shared;
    private int markerColor = 0x2F5D46;
    private int markerPattern;

    public FastTravelPostBlockEntity(BlockPos pos, BlockState state) {
        super(KMDBlockEntities.FAST_TRAVEL_POST.get(), pos, state);
    }

    public UUID getLocationId() {
        return locationId;
    }

    public Component getPostName() {
        return postName;
    }

    public void setPostName(Component postName) {
        this.postName = postName;
        setChangedAndUpdate();
    }

    public int getTextColor() {
        return textColor;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
        setChangedAndUpdate();
    }

    public int getMarkerColor() {
        return markerColor;
    }

    public int getMarkerPattern() {
        return markerPattern;
    }

    public void setMarker(int markerColor, int markerPattern) {
        this.markerColor = markerColor & 0xFFFFFF;
        this.markerPattern = markerPattern;
        setChangedAndUpdate();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor & 0xFFFFFF;
        setChangedAndUpdate();
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("LocationId", locationId.toString());
        output.putString("PostName", postName.getString());
        output.putInt("TextColor", textColor);
        output.putBoolean("Shared", shared);
        output.putInt("MarkerColor", markerColor);
        output.putInt("MarkerPattern", markerPattern);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        loadValues(
                input.getStringOr("LocationId", ""),
                input.getStringOr("PostName", "Travel Post"),
                input.getIntOr("TextColor", textColor),
                input.getBooleanOr("Shared", false),
                input.getIntOr("MarkerColor", markerColor),
                input.getIntOr("MarkerPattern", markerPattern));
    }

    private void loadValues(String id, String name, int textColor, boolean shared, int markerColor, int markerPattern) {
        try {
            if (!id.isBlank()) {
                locationId = UUID.fromString(id);
            }
        } catch (IllegalArgumentException ignored) {
            locationId = UUID.randomUUID();
        }
        String loaded = name.trim();
        postName = loaded.isEmpty() ? Component.literal("Travel Post") : Component.literal(loaded);
        this.textColor = textColor & 0xFFFFFF;
        this.shared = shared;
        this.markerColor = markerColor & 0xFFFFFF;
        this.markerPattern = markerPattern;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
