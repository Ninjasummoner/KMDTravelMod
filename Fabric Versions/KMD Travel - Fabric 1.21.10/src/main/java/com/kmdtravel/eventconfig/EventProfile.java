package com.kmdtravel.eventconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;

public record EventProfile(String id, String name, List<EditableTravelEvent> events) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id);
        tag.putString("Name", name);
        ListTag list = new ListTag();
        for (EditableTravelEvent event : events) {
            list.add(event.save());
        }
        tag.put("Events", list);
        return tag;
    }

    public static EventProfile load(CompoundTag tag) {
        List<EditableTravelEvent> events = new ArrayList<>();
        ListTag list = tag.getListOrEmpty("Events");
        for (int i = 0; i < list.size(); i++) {
            list.getCompound(i).ifPresent(eventTag -> events.add(EditableTravelEvent.load(eventTag)));
        }
        String id = tag.getStringOr("Id", "default");
        String name = tag.getStringOr("Name", id);
        return new EventProfile(id.isBlank() ? "default" : id, name.isBlank() ? id : name, List.copyOf(events));
    }

    public EventProfile withName(String name) {
        return new EventProfile(id, name, events);
    }

    public EventProfile withEvents(List<EditableTravelEvent> events) {
        return new EventProfile(id, name, List.copyOf(events));
    }
}
