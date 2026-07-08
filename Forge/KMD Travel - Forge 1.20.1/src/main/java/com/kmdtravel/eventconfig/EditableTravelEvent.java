package com.kmdtravel.eventconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record EditableTravelEvent(
        String id,
        boolean enabled,
        String title,
        boolean passive,
        int passiveDurationSeconds,
        String description,
        String dimension,
        String biome,
        EventTimeOfDay timeOfDay,
        AggressiveCompletion aggressiveCompletion,
        double weight,
        double avoidChance,
        List<String> mobs,
        List<EventCommandStep> commands) {

    public boolean matches(ResourceLocation currentDimension, ResourceLocation currentBiome, boolean day) {
        if (!enabled || !timeOfDay.matches(day)) {
            return false;
        }
        if (!dimension.isBlank() && !dimension.equals(currentDimension.toString())) {
            return false;
        }
        return biome.isBlank() || biome.equals(currentBiome.toString());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id);
        tag.putBoolean("Enabled", enabled);
        tag.putString("Title", title);
        tag.putBoolean("Passive", passive);
        tag.putInt("PassiveDurationSeconds", Math.max(0, passiveDurationSeconds));
        tag.putString("Description", description);
        tag.putString("Dimension", dimension);
        tag.putString("Biome", biome);
        tag.putString("TimeOfDay", timeOfDay.name());
        tag.putString("AggressiveCompletion", aggressiveCompletion.name());
        tag.putDouble("Weight", Math.max(0.0D, weight));
        tag.putDouble("AvoidChance", Math.max(0.0D, Math.min(1.0D, avoidChance)));
        ListTag mobList = new ListTag();
        for (String mob : mobs) {
            mobList.add(StringTag.valueOf(mob));
        }
        tag.put("Mobs", mobList);
        ListTag commandList = new ListTag();
        for (EventCommandStep command : commands) {
            commandList.add(command.save());
        }
        tag.put("Commands", commandList);
        return tag;
    }

    public static EditableTravelEvent load(CompoundTag tag) {
        List<String> mobs = new ArrayList<>();
        ListTag mobList = tag.getList("Mobs", 8);
        for (int i = 0; i < mobList.size(); i++) {
            mobs.add(mobList.getString(i));
        }
        List<EventCommandStep> commands = new ArrayList<>();
        ListTag commandList = tag.getList("Commands", 10);
        for (int i = 0; i < commandList.size(); i++) {
            commands.add(EventCommandStep.load(commandList.getCompound(i)));
        }
        return new EditableTravelEvent(
                tag.getString("Id"),
                !tag.contains("Enabled") || tag.getBoolean("Enabled"),
                tag.getString("Title"),
                tag.getBoolean("Passive"),
                tag.getInt("PassiveDurationSeconds"),
                tag.getString("Description"),
                tag.getString("Dimension"),
                tag.getString("Biome"),
                EventTimeOfDay.byName(tag.getString("TimeOfDay")),
                AggressiveCompletion.byName(tag.getString("AggressiveCompletion")),
                tag.contains("Weight") ? tag.getDouble("Weight") : 1.0D,
                tag.contains("AvoidChance") ? tag.getDouble("AvoidChance") : 0.0D,
                List.copyOf(mobs),
                List.copyOf(commands));
    }

    public EditableTravelEvent withTitle(String title) {
        return new EditableTravelEvent(id, enabled, title, passive, passiveDurationSeconds, description, dimension, biome, timeOfDay, aggressiveCompletion, weight, avoidChance, mobs, commands);
    }
}

