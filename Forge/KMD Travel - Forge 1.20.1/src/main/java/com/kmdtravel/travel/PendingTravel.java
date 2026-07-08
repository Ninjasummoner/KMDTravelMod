package com.kmdtravel.travel;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.UUID;

public record PendingTravel(
        UUID playerId,
        TravelLocation source,
        TravelLocation destination,
        BlockPos interruptedAt,
        List<UUID> mobIds,
        int ticksWaited,
        int durationTicks,
        int eventTick,
        boolean waitingForEvent,
        boolean awaitingEventChoice,
        boolean passiveEventWait,
        RuntimeTravelEvent pendingEvent,
        int promptStartTick,
        String eventTag,
        int commandIndex,
        int nextCommandTick) {
    public PendingTravel withTicksWaited(int ticks) {
        return new PendingTravel(playerId, source, destination, interruptedAt, mobIds, ticks, durationTicks, eventTick, waitingForEvent, awaitingEventChoice, passiveEventWait, pendingEvent, promptStartTick, eventTag, commandIndex, nextCommandTick);
    }

    public PendingTravel withTrackedMobs(List<UUID> mobs, int commandIndex, int nextCommandTick) {
        return new PendingTravel(playerId, source, destination, interruptedAt, mobs, ticksWaited, durationTicks, eventTick, waitingForEvent, awaitingEventChoice, passiveEventWait, pendingEvent, promptStartTick, eventTag, commandIndex, nextCommandTick);
    }

    public PendingTravel asInterrupted(BlockPos pos, List<UUID> mobs, RuntimeTravelEvent event, String eventTag, int commandIndex, int nextCommandTick) {
        return new PendingTravel(playerId, source, destination, pos, mobs, 0, durationTicks, eventTick, true, false, event != null && event.passive(), event, -1, eventTag, commandIndex, nextCommandTick);
    }

    public PendingTravel awaitingChoice(RuntimeTravelEvent event, int tick) {
        return new PendingTravel(playerId, source, destination, interruptedAt, mobIds, ticksWaited, durationTicks, eventTick, false, true, false, event, tick, "", 0, 0);
    }

    public PendingTravel awaitingChoice(RuntimeTravelEvent event, int tick, BlockPos encounterPos) {
        return new PendingTravel(playerId, source, destination, encounterPos, mobIds, ticksWaited, durationTicks, eventTick, false, true, false, event, tick, "", 0, 0);
    }

    public PendingTravel clearChoice() {
        return new PendingTravel(playerId, source, destination, interruptedAt, mobIds, ticksWaited, durationTicks, -1, false, false, false, null, -1, "", 0, 0);
    }
}
