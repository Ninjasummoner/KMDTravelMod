package com.kmdtravel.block;

import com.kmdtravel.registry.KMDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("LocationId", locationId);
        tag.putString("PostName", postName.getString());
        tag.putInt("TextColor", textColor);
        tag.putBoolean("Shared", shared);
        tag.putInt("MarkerColor", markerColor);
        tag.putInt("MarkerPattern", markerPattern);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("LocationId")) {
            locationId = tag.getUUID("LocationId");
        }
        if (tag.contains("PostName")) {
            String loaded = tag.getString("PostName").trim();
            postName = loaded.isEmpty() ? Component.literal("Travel Post") : Component.literal(loaded);
        }
        if (tag.contains("TextColor")) {
            textColor = tag.getInt("TextColor") & 0xFFFFFF;
        }
        shared = tag.getBoolean("Shared");
        if (tag.contains("MarkerColor")) {
            markerColor = tag.getInt("MarkerColor") & 0xFFFFFF;
        }
        if (tag.contains("MarkerPattern")) {
            markerPattern = tag.getInt("MarkerPattern");
        }
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

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        loadAdditional(pkt.getTag(), registries);
    }
}
