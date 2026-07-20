package com.kmdtravel.eventconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;

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

    public boolean matches(Identifier currentDimension, Identifier currentBiome, boolean day) {
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
        ListTag mobList = tag.getListOrEmpty("Mobs");
        for (int i = 0; i < mobList.size(); i++) {
            mobList.getString(i).ifPresent(mobs::add);
        }
        List<EventCommandStep> commands = new ArrayList<>();
        ListTag commandList = tag.getListOrEmpty("Commands");
        for (int i = 0; i < commandList.size(); i++) {
            commandList.getCompound(i).ifPresent(commandTag -> commands.add(EventCommandStep.load(commandTag)));
        }
        return new EditableTravelEvent(
                tag.getStringOr("Id", "custom_event"),
                tag.getBooleanOr("Enabled", true),
                tag.getStringOr("Title", "New Event"),
                tag.getBooleanOr("Passive", false),
                tag.getIntOr("PassiveDurationSeconds", 60),
                tag.getStringOr("Description", "A custom event happens."),
                tag.getStringOr("Dimension", ""),
                tag.getStringOr("Biome", ""),
                EventTimeOfDay.byName(tag.getStringOr("TimeOfDay", EventTimeOfDay.BOTH.name())),
                AggressiveCompletion.byName(tag.getStringOr("AggressiveCompletion", AggressiveCompletion.KILL_MOBS.name())),
                tag.getDoubleOr("Weight", 1.0D),
                tag.getDoubleOr("AvoidChance", 0.0D),
                List.copyOf(mobs),
                List.copyOf(commands));
    }

    public EditableTravelEvent withTitle(String title) {
        return new EditableTravelEvent(id, enabled, title, passive, passiveDurationSeconds, description, dimension, biome, timeOfDay, aggressiveCompletion, weight, avoidChance, mobs, commands);
    }
}
