package com.kmdtravel.travel;

import com.kmdtravel.api.KMDTravelEvents;
import com.kmdtravel.config.KMDConfig;
import com.kmdtravel.data.PlayerTravelData;
import com.kmdtravel.data.TravelSavedData;
import com.kmdtravel.eventconfig.AggressiveCompletion;
import com.kmdtravel.eventconfig.EventCommandStep;
import com.kmdtravel.eventconfig.EventProfileSavedData;
import com.kmdtravel.network.BeginTravelPacket;
import com.kmdtravel.network.EndTravelOverlayPacket;
import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.network.OpenTravelScreenPacket;
import com.kmdtravel.network.TravelEventPromptPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class FastTravelManager {
    private static final Map<UUID, PendingTravel> PENDING = new HashMap<>();
    private static final int ENCOUNTER_COMPLETION_GRACE_TICKS = 40;

    private FastTravelManager() {
    }

    public static boolean isTraveling(ServerPlayer player) {
        return player != null && PENDING.containsKey(player.getUUID());
    }

public static void requestTravel(ServerPlayer player, UUID sourceId, UUID destinationId) {
        if (PENDING.containsKey(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.kmdtravel.already_traveling"), true);
            return;
        }

        TravelSavedData savedData = TravelSavedData.get(((ServerLevel) player.level()));
        Optional<TravelLocation> source = savedData.get(sourceId);
        Optional<TravelLocation> destination = savedData.get(destinationId);
        if (source.isEmpty() || destination.isEmpty()
                || (!destination.get().shared() && !PlayerTravelData.hasDiscovered(player, destinationId))) {
            player.displayClientMessage(Component.translatable("message.kmdtravel.not_discovered"), true);
            return;
        }
        if (!source.get().dimension().equals(destination.get().dimension())) {
            player.displayClientMessage(Component.translatable("message.kmdtravel.same_dimension_only"), true);
            return;
        }

        ServerLevel level = levelFor(player, destination.get().dimension());
        if (level == null) {
            return;
        }

        double distance = Math.sqrt(source.get().pos().distSqr(destination.get().pos()));
        consumeHunger(player, distance);
        int durationTicks = travelDuration(distance);
        int eventTick = shouldScheduleEvent(player, level, source.get(), destination.get()) ? randomEventTick(player, durationTicks) : -1;
        player.setInvulnerable(true);
        hideTravelingPlayer(player, true);

        PENDING.put(player.getUUID(), new PendingTravel(
                player.getUUID(),
                source.get(),
                destination.get(),
                null,
                List.of(),
                0,
                durationTicks,
                eventTick,
                false,
                false,
                false,
                null,
                -1,
                "",
                0,
                0));
        KMDTravelEvents.notifyTravelStarted(player, source.get(), destination.get());
        OpenTravelScreenPacket mapPacket = OpenTravelScreenPacket.from(player, sourceId);
        KMDNetwork.sendToPlayer(player, new BeginTravelPacket(
                sourceId,
                destinationId,
                level.getSeed(),
                destination.get().dimension(),
                source.get().pos().getX(),
                source.get().pos().getZ(),
                destination.get().pos().getX(),
                destination.get().pos().getZ(),
                durationTicks,
                mapPacket.locations(),
                mapPacket.samples()));
    }

    public static void handleEventChoice(ServerPlayer player, boolean viewEvent) {
        PendingTravel pending = PENDING.get(player.getUUID());
        if (pending == null || !pending.awaitingEventChoice()) {
            return;
        }

        ServerLevel level = ((ServerLevel) player.level());
        if (viewEvent) {
            PENDING.put(player.getUUID(), interruptTravel(player, level, pending, pending.pendingEvent()));
            return;
        }

        RuntimeTravelEvent event = pending.pendingEvent();
        if (event != null && event.passive()) {
            player.displayClientMessage(Component.translatable("message.kmdtravel.event_skipped"), true);
            PENDING.put(player.getUUID(), pending.clearChoice());
            return;
        }

        int skipChance = skipChancePercent(player, event);
        if (player.getRandom().nextInt(100) < skipChance) {
            player.displayClientMessage(Component.translatable("message.kmdtravel.event_skipped"), true);
            PENDING.put(player.getUUID(), pending.clearChoice());
            return;
        }

        player.displayClientMessage(Component.translatable("message.kmdtravel.event_skip_failed"), true);
        PENDING.put(player.getUUID(), interruptTravel(player, level, pending, pending.pendingEvent()));
    }

    public static void tick(ServerLevel level) {
        Iterator<Map.Entry<UUID, PendingTravel>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingTravel> entry = iterator.next();
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            if (((ServerLevel) player.level()) != level) {
                continue;
            }


            PendingTravel pending = entry.getValue().withTicksWaited(entry.getValue().ticksWaited() + 1);
            entry.setValue(pending);

            if (pending.awaitingEventChoice()) {
                if (pending.ticksWaited() - pending.promptStartTick() >= 600) {
                    handleEventChoice(player, false);
                }
                continue;
            }

            if (!pending.waitingForEvent()) {
                if (pending.eventTick() > 0 && pending.ticksWaited() >= pending.eventTick()) {
                    entry.setValue(promptTravelEvent(player, level, pending));
                    continue;
                }
                if (pending.ticksWaited() >= pending.durationTicks()) {
                    finishTravel(player, level, pending.destination());
                    iterator.remove();
                }
                continue;
            }

            PendingTravel commandUpdated = runDueEventCommands(level, player, pending);
            if (commandUpdated != pending) {
                pending = commandUpdated;
                entry.setValue(pending);
                if (!pending.waitingForEvent()) {
                    continue;
                }
            }

            if (!pending.passiveEventWait() || pending.ticksWaited() >= 60) {
                boolean anyAlive = false;
                for (UUID mobId : pending.mobIds()) {
                    Entity entity = level.getEntity(mobId);
                    if (entity instanceof Mob mob && mob.isAlive()) {
                        reapplyCustomVisualFlags(pending.pendingEvent(), mob);
                        if (!pending.passiveEventWait()) {
                            if (mob.getVehicle() instanceof Boat) {
                                updateBoatPirate(mob, player);
                            } else if (!mob.isNoAi()) {
                                makeMobHostile(mob, player);
                                reapplyCustomVisualFlags(pending.pendingEvent(), mob);
                            }
                        }
                        anyAlive = true;
                    }
                }

                if (pending.passiveEventWait() && pending.ticksWaited() % 20 == 0) {
                    int durationSeconds = pending.pendingEvent() != null && pending.pendingEvent().durationSeconds() > 0 ? pending.pendingEvent().durationSeconds() : KMDConfig.EVENT_INVESTIGATION_SECONDS.get();
                    int remainingSeconds = Math.max(0, durationSeconds - pending.ticksWaited() / 20);
                    player.displayClientMessage(Component.translatable("message.kmdtravel.event_countdown", remainingSeconds), true);
                }

                int durationSeconds = pending.pendingEvent() != null && pending.pendingEvent().durationSeconds() > 0 ? pending.pendingEvent().durationSeconds() : KMDConfig.EVENT_INVESTIGATION_SECONDS.get();
                boolean passiveEventDone = pending.passiveEventWait() && pending.ticksWaited() >= durationSeconds * 20;
                boolean timedAggressiveDone = !pending.passiveEventWait()
                        && pending.pendingEvent() != null
                        && pending.pendingEvent().aggressiveCompletion() == AggressiveCompletion.TIMED
                        && pending.ticksWaited() >= Math.max(ENCOUNTER_COMPLETION_GRACE_TICKS, durationSeconds * 20);
                boolean killAggressiveDone = !pending.passiveEventWait()
                        && pending.pendingEvent() != null
                        && pending.pendingEvent().aggressiveCompletion() == AggressiveCompletion.KILL_MOBS
                        && pending.ticksWaited() >= ENCOUNTER_COMPLETION_GRACE_TICKS
                        && !pending.mobIds().isEmpty()
                        && !anyAlive;
                boolean killAggressiveNoMobsFallback = !pending.passiveEventWait()
                        && pending.pendingEvent() != null
                        && pending.pendingEvent().aggressiveCompletion() == AggressiveCompletion.KILL_MOBS
                        && pending.mobIds().isEmpty()
                        && pending.pendingEvent().mobs().isEmpty()
                        && pending.pendingEvent().commands().isEmpty()
                        && pending.ticksWaited() >= Math.max(ENCOUNTER_COMPLETION_GRACE_TICKS, durationSeconds * 20);
                boolean ambushDone = timedAggressiveDone || killAggressiveDone || killAggressiveNoMobsFallback;
                if (ambushDone || passiveEventDone) {
                    ServerLevel destinationLevel = levelFor(player, pending.destination().dimension());
                    if (destinationLevel != null) {
                        entry.setValue(completeEncounter(player, level, destinationLevel, pending, Math.max(0, pending.commandIndex())));
                    }
                }
            }
        }
    }

    public static boolean setEventsEnabled(boolean enabled) {
        KMDConfig.ENABLE_EVENTS.set(enabled);
        KMDConfig.save();
        return KMDConfig.ENABLE_EVENTS.get();
    }

    public static void cancelTravel(ServerPlayer player) {
        cancelTravel(player, false);
    }

    public static void cancelTravelToStart(ServerPlayer player) {
        cancelTravel(player, true);
    }

    private static void cancelTravel(ServerPlayer player, boolean returnToStart) {
        PendingTravel pending = PENDING.remove(player.getUUID());
        if (pending == null) {
            return;
        }
        if (returnToStart) {
            ServerLevel sourceLevel = levelFor(player, pending.source().dimension());
            if (sourceLevel != null) {
                BlockPos sourcePos = pending.source().pos();
                BlockPos safePos = findSafeGroundNear(sourceLevel, sourcePos, 6, 4).orElse(sourcePos);
                teleportPlayer(player, sourceLevel, safePos.getX() + 0.5D, safePos.getY() + 1.0D, safePos.getZ() + 0.5D);
            }
        }
        player.setInvulnerable(false);
        hideTravelingPlayer(player, false);
        cleanupEncounterEntities(((ServerLevel) player.level()), pending.mobIds());
        KMDNetwork.sendToPlayer(player, new EndTravelOverlayPacket(false));
    }

    public static int setEventInvestigationSeconds(int seconds) {
        KMDConfig.EVENT_INVESTIGATION_SECONDS.set(seconds);
        KMDConfig.save();
        return KMDConfig.EVENT_INVESTIGATION_SECONDS.get();
    }

    public static int previewAmbushChance(ServerPlayer player, TravelLocation source, TravelLocation destination) {
        if (!source.dimension().equals(destination.dimension()) || !KMDConfig.ENABLE_EVENTS.get()) {
            return 0;
        }
        return (int) Math.round(interruptChance(player, ((ServerLevel) player.level()), source, destination) * 100.0D);
    }

    private static PendingTravel promptTravelEvent(ServerPlayer player, ServerLevel level, PendingTravel pending) {
        TravelLocation source = pending.source();
        TravelLocation destination = pending.destination();
        Optional<BlockPos> encounterPos = randomSafeRouteEncounterPos(player, level, pending);
        if (encounterPos.isEmpty()) {
            return pending.clearChoice();
        }
        BlockPos safePos = encounterPos.get();
        List<ResourceLocation> routeBiomes = new ArrayList<>();
        addBiomeId(level, routeBiomes, player.blockPosition());
        addBiomeId(level, routeBiomes, source.pos());
        addBiomeId(level, routeBiomes, destination.pos());
        addBiomeId(level, routeBiomes, safePos);
        Optional<RuntimeTravelEvent> selectedEvent = EventProfileSavedData.get(level)
                .pickEvent(player, level.dimension().location(), routeBiomes, isDay(level), player.getRandom().nextDouble())
                .map(custom -> RuntimeTravelEvent.custom(custom, KMDConfig.EVENT_INVESTIGATION_SECONDS.get()));
        if (selectedEvent.isEmpty()) {
            return pending.clearChoice();
        }
        RuntimeTravelEvent event = selectedEvent.get();
        int ambushChance = previewAmbushChance(player, source, destination);
        int skipChance = event.passive() ? 100 : skipChancePercent(player, event);
        KMDTravelEvents.notifyEncounterPrompted(player, event, safePos);
        KMDNetwork.sendToPlayer(player, new TravelEventPromptPacket(event.id(), event.title().getString(), event.description().getString(), event.passive(), skipChance, ambushChance, event.durationSeconds()));
        return pending.awaitingChoice(event, pending.ticksWaited(), safePos);
    }

    private static void addBiomeId(ServerLevel level, List<ResourceLocation> biomes, BlockPos pos) {
        ResourceLocation biomeId = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getKey(level.getBiome(pos).value());
        if (biomeId != null && !biomes.contains(biomeId)) {
            biomes.add(biomeId);
        }
    }

    private static void consumeHunger(ServerPlayer player, double distance) {
        int hunger = (int) Math.ceil((distance / 100.0D) * KMDConfig.HUNGER_PER_100_BLOCKS.get());
        FoodData food = player.getFoodData();
        food.setFoodLevel(Math.max(0, food.getFoodLevel() - hunger));
        food.addExhaustion((float) Math.min(40.0D, distance / 250.0D));
    }

    private static boolean shouldScheduleEvent(ServerPlayer player, ServerLevel level, TravelLocation source, TravelLocation destination) {
        if (!KMDConfig.ENABLE_EVENTS.get()) {
            return false;
        }
        return player.getRandom().nextDouble() < interruptChance(player, level, source, destination);
    }

    private static double interruptChance(ServerPlayer player, ServerLevel level, TravelLocation source, TravelLocation destination) {
        double chance = KMDConfig.BASE_EVENT_CHANCE.get();
        if (!isDay(level)) {
            chance *= KMDConfig.NIGHT_EVENT_MULTIPLIER.get();
        }
        chance *= 1.0D - ArmorProtection.eventReduction(player);
        int hunger = player.getFoodData().getFoodLevel();
        if (hunger <= 6) {
            chance += KMDConfig.HUNGER_LOW_EVENT_BONUS.get();
        } else if (hunger <= 12) {
            chance += KMDConfig.HUNGER_MEDIUM_EVENT_BONUS.get();
        }
        double distance = Math.sqrt(source.pos().distSqr(destination.pos()));
        chance += distance / 1000.0D * KMDConfig.DISTANCE_EVENT_CHANCE_PER_1000_BLOCKS.get();
        return Math.min(KMDConfig.MAX_EVENT_CHANCE.get(), Math.max(0.0D, chance));
    }

    private static int travelDuration(double distance) {
        return Math.max(100, Math.min(420, (int) (80 + distance / 8.0D)));
    }

    private static int randomEventTick(ServerPlayer player, int durationTicks) {
        int minTick = Math.max(60, (int) (durationTicks * 0.20D));
        int maxTick = Math.max(minTick + 1, (int) (durationTicks * 0.80D));
        return minTick + player.getRandom().nextInt(Math.max(1, maxTick - minTick));
    }

    private static Optional<BlockPos> randomSafeRouteEncounterPos(ServerPlayer player, ServerLevel level, PendingTravel pending) {
        TravelLocation source = pending.source();
        TravelLocation destination = pending.destination();
        double baseProgress = pending.eventTick() > 0
                ? Math.max(0.15D, Math.min(0.85D, pending.eventTick() / (double) Math.max(1, pending.durationTicks())))
                : 0.50D;
        double routeX = destination.pos().getX() - source.pos().getX();
        double routeZ = destination.pos().getZ() - source.pos().getZ();
        double routeLength = Math.sqrt(routeX * routeX + routeZ * routeZ);
        double perpendicularX = routeLength <= 0.001D ? 1.0D : -routeZ / routeLength;
        double perpendicularZ = routeLength <= 0.001D ? 0.0D : routeX / routeLength;
        double jitterLimit = Math.max(8.0D, Math.min(96.0D, routeLength * 0.12D));

        for (int attempt = 0; attempt < 16; attempt++) {
            double progress = Math.max(0.10D, Math.min(0.90D, baseProgress + (player.getRandom().nextDouble() - 0.5D) * 0.34D));
            double side = (player.getRandom().nextDouble() * 2.0D - 1.0D) * jitterLimit;
            int x = (int) Math.round(source.pos().getX() + routeX * progress + perpendicularX * side);
            int z = (int) Math.round(source.pos().getZ() + routeZ * progress + perpendicularZ * side);
            int y = (int) Math.round(source.pos().getY() + (destination.pos().getY() - source.pos().getY()) * progress);
            BlockPos center = new BlockPos(x, Math.max(level.getMinY() + 2, y), z);
            Optional<BlockPos> safe = findSafeEncounterPos(level, center);
            if (safe.isPresent()) {
                return safe;
            }
        }

        BlockPos routeCenter = new BlockPos(
                (source.pos().getX() + destination.pos().getX()) / 2,
                Math.max(level.getMinY() + 2, (source.pos().getY() + destination.pos().getY()) / 2),
                (source.pos().getZ() + destination.pos().getZ()) / 2);
        return findSafeEncounterPos(level, routeCenter);
    }

    private static PendingTravel interruptTravel(ServerPlayer player, ServerLevel level, PendingTravel pending, RuntimeTravelEvent event) {
        if (event == null) {
            return pending.clearChoice();
        }
        Optional<BlockPos> encounterPos = pending.interruptedAt() != null
                ? Optional.of(pending.interruptedAt())
                : randomSafeRouteEncounterPos(player, level, pending);
        if (encounterPos.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.kmdtravel.event_skipped"), true);
            return pending.clearChoice();
        }
        BlockPos safePos = encounterPos.get();

        player.setInvulnerable(false);
        hideTravelingPlayer(player, false);
        KMDNetwork.sendToPlayer(player, new EndTravelOverlayPacket(true));
        teleportPlayer(player, level, safePos.getX() + 0.5D, safePos.getY() + 1.0D, safePos.getZ() + 0.5D);
        player.displayClientMessage(event.description(), false);
        KMDTravelEvents.notifyEncounterStarted(player, event, safePos);

        List<UUID> spawned = new ArrayList<>();
        String eventTag = "kmd_event_" + player.getUUID().toString().replace("-", "").substring(0, 8) + "_" + level.getGameTime();
        if (!event.custom() && event.mobs().isEmpty() && event.commands().isEmpty()) {
            Mob npc = (event.id().equals("roadside_merchant") || event.id().equals("floating_merchant") ? EntityType.WANDERING_TRADER : EntityType.VILLAGER).create(level, EntitySpawnReason.EVENT);
            if (npc != null) {
                BlockPos npcPos = findSafeGroundNear(level, safePos.offset(2, 0, 2), 4, 2).orElse(safePos);
                moveEntity(npc, npcPos.getX() + 0.5D, npcPos.getY() + 1.0D, npcPos.getZ() + 0.5D, player.getYRot(), 0.0F);
                npc.setCustomName(event.title());
                npc.setCustomNameVisible(true);
                npc.setPersistenceRequired();
                level.addFreshEntity(npc);
                if (event.seaEvent()) {
                    Boat boat = EntityType.OAK_BOAT.create(level, EntitySpawnReason.EVENT);
                    if (boat != null) {
                        moveEntity(boat, safePos.getX() + 2.5D, safePos.getY() + 0.1D, safePos.getZ() + 2.5D, player.getYRot(), 0.0F);
                        level.addFreshEntity(boat);
                        npc.startRiding(boat, true, true);
                        spawned.add(boat.getUUID());
                    }
                }
                spawned.add(npc.getUUID());
            }
            return pending.asInterrupted(safePos, spawned, event, eventTag, 0, firstCommandDelay(event));
        }

        int spawnedMobCount = 0;
        for (int i = 0; i < event.mobs().size(); i++) {
            RuntimeTravelEvent.MobSpawnSpec spec = event.mobs().get(i);
            Entity created = spec.type().create(level, EntitySpawnReason.EVENT);
            if (!(created instanceof Mob mob)) {
                if (created != null) {
                    created.discard();
                }
                continue;
            }
            double angle = (Math.PI * 2.0D / Math.max(1, event.mobs().size())) * i;
            double range = Math.max(4.0D, spec.range());
            Vec3 offset = new Vec3(Math.cos(angle) * range, 0.0D, Math.sin(angle) * range);
            BlockPos targetPos = BlockPos.containing(safePos.getCenter().add(offset));
            BlockPos waterPos = event.seaEvent() ? waterSurfacePos(level, targetPos) : null;
            if (event.seaEvent() && waterPos == null && waterSurfacePos(level, safePos) != null) {
                waterPos = nearbyWaterSurfacePos(level, safePos, i);
            }
            BlockPos spawnPos = waterPos == null
                    ? findSafeMobSpawnPos(level, targetPos, Math.max(6, (int) range / 2), 96).orElse(targetPos)
                    : waterPos;
            moveEntity(mob, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D, player.getYRot(), 0.0F);
            applyCustomMobData(mob, spec);
            boolean playerProvidedNbt = !spec.nbt().isBlank();
            if (!playerProvidedNbt) {
                customizeEncounterMob(mob, event, player);
            }
            String lowerNbt = spec.nbt().toLowerCase();
            boolean hasNoAi = lowerNbt.contains("noai");
            boolean keepNoAi = hasNoAi
                    && (lowerNbt.contains("noai:1") || lowerNbt.contains("noai:1b") || lowerNbt.contains("noai:true"));
            if (hasNoAi) {
                mob.setNoAi(keepNoAi);
            }
            mob.addTag(eventTag);
            if (!keepNoAi) {
                mob.setNoAi(false);
            }
            if (!level.addFreshEntity(mob)) {
                continue;
            }
            spawnedMobCount++;
            if (playerProvidedNbt) {
                applyCustomMobData(mob, spec);
                mob.addTag(eventTag);
                if (hasNoAi) {
                    mob.setNoAi(keepNoAi);
                }
            }
            if (!keepNoAi) {
                makeMobHostile(mob, player);
            }
            reapplyCustomVisualFlags(mob, spec);
            if (waterPos != null && !playerProvidedNbt && shouldUseBoat(event, spec)) {
                Boat boat = EntityType.OAK_BOAT.create(level, EntitySpawnReason.EVENT);
                if (boat != null) {
                    moveEntity(boat, waterPos.getX() + 0.5D, waterPos.getY() + 0.1D, waterPos.getZ() + 0.5D, player.getYRot(), 0.0F);
                    boat.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(boat);
                    equipPirateMob(mob);
                    mob.setNoAi(true);
                    mob.startRiding(boat, true, true);
                    spawned.add(boat.getUUID());
                }
            }
            spawned.add(mob.getUUID());
        }

        if (!event.mobs().isEmpty() && event.aggressiveCompletion() == AggressiveCompletion.KILL_MOBS && spawnedMobCount == 0) {
            player.displayClientMessage(Component.translatable("message.kmdtravel.event_skipped"), true);
            return pending.clearChoice();
        }

        PendingTravel interrupted = pending.asInterrupted(safePos, spawned, event, eventTag, 0, firstCommandDelay(event));
        return runDueEventCommands(level, player, interrupted);
    }

    private static boolean shouldUseBoat(RuntimeTravelEvent event, RuntimeTravelEvent.MobSpawnSpec spec) {
        if (event.custom()) {
            return false;
        }
        String eventText = (event.id() + " " + event.title().getString() + " " + spec.name() + " " + spec.nbt()).toLowerCase();
        return eventText.contains("pirate") || eventText.contains("boat");
    }

    private static int firstCommandDelay(RuntimeTravelEvent event) {
        return event.commands().isEmpty() ? 0 : Math.max(0, event.commands().get(0).delaySeconds()) * 20;
    }

    private static void applyCustomMobData(Mob mob, RuntimeTravelEvent.MobSpawnSpec spec) {
        if (!spec.nbt().isBlank()) {
            try {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, mob.registryAccess());
                mob.saveWithoutId(output);
                CompoundTag customTag = parseMobNbt(spec.nbt());
                CompoundTag tag = output.buildResult();
                tag.merge(customTag);
                double x = mob.getX();
                double y = mob.getY();
                double z = mob.getZ();
                float yRot = mob.getYRot();
                float xRot = mob.getXRot();
                mob.load(TagValueInput.create(ProblemReporter.DISCARDING, mob.registryAccess(), tag));
                applyEquipmentFromNbtTag(mob, customTag);
                moveEntity(mob, x, y, z, yRot, xRot);
            } catch (Exception ignored) {
            }
        }
        if (!spec.name().isBlank()) {
            mob.setCustomName(Component.literal(spec.name()));
            mob.setCustomNameVisible(true);
        }
    }

    private static void applyEquipmentFromNbtTag(Mob mob, CompoundTag customTag) {
        ListTag handItems = customTag.getListOrEmpty("HandItems");
        setEquipmentFromNbtItem(mob, EquipmentSlot.MAINHAND, handItems.getCompound(0).orElse(null));
        setEquipmentFromNbtItem(mob, EquipmentSlot.OFFHAND, handItems.getCompound(1).orElse(null));

        ListTag armorItems = customTag.getListOrEmpty("ArmorItems");
        setEquipmentFromNbtItem(mob, EquipmentSlot.FEET, armorItems.getCompound(0).orElse(null));
        setEquipmentFromNbtItem(mob, EquipmentSlot.LEGS, armorItems.getCompound(1).orElse(null));
        setEquipmentFromNbtItem(mob, EquipmentSlot.CHEST, armorItems.getCompound(2).orElse(null));
        setEquipmentFromNbtItem(mob, EquipmentSlot.HEAD, armorItems.getCompound(3).orElse(null));
    }

    private static void setEquipmentFromNbtItem(Mob mob, EquipmentSlot slot, CompoundTag itemTag) {
        if (itemTag == null || itemTag.isEmpty()) {
            return;
        }
        var itemOps = mob.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        ItemStack stack = ItemStack.CODEC.parse(itemOps, itemTag).result().orElseGet(() -> fallbackItemStack(mob, itemTag));
        mob.setItemSlot(slot, stack);
    }

    private static ItemStack fallbackItemStack(Mob mob, CompoundTag itemTag) {
        String id = itemTag.getStringOr("id", "minecraft:air");
        int count = itemTag.getIntOr("count", itemTag.getIntOr("Count", 1));
        if (id.isBlank() || "minecraft:air".equals(id) || count <= 0) {
            return ItemStack.EMPTY;
        }

        CompoundTag basicItemTag = new CompoundTag();
        basicItemTag.putString("id", id);
        basicItemTag.putInt("count", count);
        var itemOps = mob.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        ItemStack stack = ItemStack.CODEC.parse(itemOps, basicItemTag).result().orElse(ItemStack.EMPTY);
        applyDyedColor(stack, itemTag);
        return stack;
    }

    private static void applyDyedColor(ItemStack stack, CompoundTag itemTag) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag components = itemTag.getCompoundOrEmpty("components");
        CompoundTag dyedColor = components.getCompoundOrEmpty("minecraft:dyed_color");
        if (dyedColor.contains("rgb")) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(dyedColor.getIntOr("rgb", 0xA06540)));
            return;
        }
        CompoundTag tag = itemTag.getCompoundOrEmpty("tag");
        CompoundTag display = tag.getCompoundOrEmpty("display");
        if (display.contains("color")) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(display.getIntOr("color", 0xA06540)));
        }
    }    private static void reapplyCustomVisualFlags(Mob mob, RuntimeTravelEvent.MobSpawnSpec spec) {
        String lowerNbt = spec.nbt().toLowerCase();
        if (lowerNbt.contains("invisible:1") || lowerNbt.contains("invisible:true")) {
            mob.setInvisible(true);
        }
        if (lowerNbt.contains("silent:1") || lowerNbt.contains("silent:true")) {
            mob.setSilent(true);
        }
        if (lowerNbt.contains("customnamevisible:0") || lowerNbt.contains("customnamevisible:false")) {
            mob.setCustomNameVisible(false);
        }
        if (spec.name().isBlank() && (lowerNbt.contains("customnamevisible:0") || lowerNbt.contains("customnamevisible:false"))) {
            mob.setCustomName(null);
        }
    }

    private static void reapplyCustomVisualFlags(RuntimeTravelEvent event, Mob mob) {
        if (event == null) {
            return;
        }
        for (RuntimeTravelEvent.MobSpawnSpec spec : event.mobs()) {
            if (spec.type() == mob.getType()) {
                reapplyCustomVisualFlags(mob, spec);
            }
        }
    }

    private static CompoundTag parseMobNbt(String nbt) throws Exception {
        return TagParser.parseCompoundFully(prepareMobNbt(nbt));
    }

    private static String prepareMobNbt(String nbt) {
        String trimmed = nbt.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }
        if (trimmed.startsWith("\"text\"") || trimmed.startsWith("text") || trimmed.startsWith("{\"text\"") || trimmed.startsWith("{text")) {
            return "{CustomName:'" + trimmed + "'}";
        }
        return "{" + trimmed + "}";
    }


    private static PendingTravel resumeTravelAfterEvent(ServerPlayer player, ServerLevel level, PendingTravel pending) {
        cleanupEncounterEntities(level, pending.mobIds());
        int remainingTicks = Math.max(160, pending.durationTicks() - pending.eventTick());
        PendingTravel resumed = new PendingTravel(
                player.getUUID(),
                new TravelLocation(UUID.randomUUID(), "Interrupted Route", pending.destination().dimension(), pending.interruptedAt()),
                pending.destination(),
                null,
                List.of(),
                0,
                remainingTicks,
                -1,
                false,
                false,
                false,
                null,
                -1,
                "",
                0,
                0);
        OpenTravelScreenPacket mapPacket = OpenTravelScreenPacket.from(player, pending.source().id());
        KMDNetwork.sendToPlayer(player, new BeginTravelPacket(
                pending.source().id(),
                pending.destination().id(),
                level.getSeed(),
                pending.destination().dimension(),
                pending.interruptedAt().getX(),
                pending.interruptedAt().getZ(),
                pending.destination().pos().getX(),
                pending.destination().pos().getZ(),
                remainingTicks,
                mapPacket.locations(),
                mapPacket.samples()));
        player.setInvulnerable(true);
        hideTravelingPlayer(player, true);
        return resumed;
    }

    public static boolean finishEncounter(ServerPlayer player) {
        PendingTravel pending = PENDING.get(player.getUUID());
        if (pending == null || pending.pendingEvent() == null) {
            return false;
        }
        ServerLevel level = levelFor(player, pending.destination().dimension());
        if (level == null) {
            return false;
        }
        completeEncounter(player, ((ServerLevel) player.level()), level, pending, Math.max(0, pending.commandIndex()));
        return true;
    }

    private static PendingTravel completeEncounter(ServerPlayer player, ServerLevel commandLevel, ServerLevel destinationLevel, PendingTravel pending, int commandStart) {
        cleanupEncounterEntities(commandLevel, pending.mobIds());
        KMDTravelEvents.notifyEncounterFinished(player, pending.pendingEvent(), pending.interruptedAt());
        PendingTravel resumed = resumeTravelAfterEvent(player, destinationLevel, pending);
        PENDING.put(player.getUUID(), resumed);
        flushRemainingEventCommands(commandLevel, player, pending, commandStart);
        return resumed;
    }

    private static void finishTravel(ServerPlayer player, ServerLevel level, TravelLocation destination) {
        BlockPos pos = destination.pos().relative(player.getDirection().getOpposite(), 2);
        BlockPos safePos = findSafeGroundNear(level, pos, 6, 4)
                .orElseGet(() -> findSafeGroundNear(level, destination.pos(), 3, 4).orElse(destination.pos()));
        teleportPlayer(player, level, safePos.getX() + 0.5D, safePos.getY() + 1.0D, safePos.getZ() + 0.5D);
        player.displayClientMessage(Component.translatable("message.kmdtravel.arrived", destination.name()), true);
        KMDTravelEvents.notifyTravelFinished(player, destination);
        player.setInvulnerable(false);
        hideTravelingPlayer(player, false);
        KMDNetwork.sendToPlayer(player, new EndTravelOverlayPacket(false));
    }

    private static void hideTravelingPlayer(ServerPlayer player, boolean hidden) {
        player.setInvisible(hidden);
        if (hidden) {
            List<Pair<EquipmentSlot, ItemStack>> emptyEquipment = List.of(
                    Pair.of(EquipmentSlot.HEAD, ItemStack.EMPTY),
                    Pair.of(EquipmentSlot.CHEST, ItemStack.EMPTY),
                    Pair.of(EquipmentSlot.LEGS, ItemStack.EMPTY),
                    Pair.of(EquipmentSlot.FEET, ItemStack.EMPTY),
                    Pair.of(EquipmentSlot.MAINHAND, ItemStack.EMPTY),
                    Pair.of(EquipmentSlot.OFFHAND, ItemStack.EMPTY));
            ((ServerLevel) player.level()).getChunkSource().sendToTrackingPlayersAndSelf(player, new ClientboundSetEquipmentPacket(player.getId(), emptyEquipment));
        } else {
            List<Pair<EquipmentSlot, ItemStack>> currentEquipment = List.of(
                    Pair.of(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD)),
                    Pair.of(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST)),
                    Pair.of(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS)),
                    Pair.of(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET)),
                    Pair.of(EquipmentSlot.MAINHAND, player.getItemBySlot(EquipmentSlot.MAINHAND)),
                    Pair.of(EquipmentSlot.OFFHAND, player.getItemBySlot(EquipmentSlot.OFFHAND)));
            ((ServerLevel) player.level()).getChunkSource().sendToTrackingPlayersAndSelf(player, new ClientboundSetEquipmentPacket(player.getId(), currentEquipment));
        }
    }

    private static void customizeEncounterMob(Mob mob, RuntimeTravelEvent event, ServerPlayer target) {
        mob.setPersistenceRequired();
        if (!event.custom()) {
            mob.setCustomName(event.title());
            mob.setCustomNameVisible(true);
        }
        switch (event.id()) {
            case "bandit_ambush", "roadblock", "forest_ambush" -> {
                mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
                mob.setItemSlot(EquipmentSlot.MAINHAND, mob.getType() == EntityType.PILLAGER ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.IRON_SWORD));
            }
            case "illager_patrol" -> {
                mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                mob.setItemSlot(EquipmentSlot.MAINHAND, mob.getType() == EntityType.PILLAGER ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.IRON_AXE));
            }
            case "desert_raiders" -> {
                mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            }
            case "night_attack", "swamp_trouble" -> mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            default -> {
            }
        }
        makeMobHostile(mob, target);
    }

    private static int skipChancePercent(ServerPlayer player, RuntimeTravelEvent event) {
        if (event != null && event.avoidChance() > 0.0D) {
            return (int) Math.round(Math.max(0.0D, Math.min(1.0D, event.avoidChance())) * 100.0D);
        }
        return ArmorProtection.skipChancePercent(player);
    }

    private static PendingTravel runDueEventCommands(ServerLevel level, ServerPlayer player, PendingTravel pending) {
        RuntimeTravelEvent event = pending.pendingEvent();
        if (event == null || event.commands().isEmpty() || pending.commandIndex() >= event.commands().size()) {
            return pending;
        }
        if (pending.ticksWaited() < pending.nextCommandTick()) {
            return pending;
        }
        EventCommandStep step = event.commands().get(pending.commandIndex());
        if (!isFinishEncounterCommand(step.command())) {
            executeEventCommand(level, player, pending, step.command());
        }
        List<UUID> tracked = trackTaggedEventEntities(level, pending);
        int nextIndex = pending.commandIndex() + 1;
        int nextTick = nextIndex < event.commands().size()
                ? pending.ticksWaited() + Math.max(0, event.commands().get(nextIndex).delaySeconds()) * 20
                : pending.ticksWaited();
        return pending.withTrackedMobs(tracked, nextIndex, nextTick);
    }

    private static void flushRemainingEventCommands(ServerLevel level, ServerPlayer player, PendingTravel pending) {
        flushRemainingEventCommands(level, player, pending, Math.max(0, pending.commandIndex()));
    }

    private static void flushRemainingEventCommands(ServerLevel level, ServerPlayer player, PendingTravel pending, int startIndex) {
        RuntimeTravelEvent event = pending.pendingEvent();
        if (event == null || event.commands().isEmpty()) {
            return;
        }
        for (int i = Math.max(0, startIndex); i < event.commands().size(); i++) {
            String command = event.commands().get(i).command();
            if (!isFinishEncounterCommand(command)) {
                executeEventCommand(level, player, pending, command);
            }
        }
    }

    private static boolean isFinishEncounterCommand(String rawCommand) {
        if (rawCommand == null) {
            return false;
        }
        String command = rawCommand.trim().toLowerCase();
        while (command.startsWith("/")) {
            command = command.substring(1).trim();
        }
        command = command.replaceAll("\\s+", " ");
        return command.matches("(^|.*\\s)(kmdtravel|kmd) finishencounter($|\\s.*)");
    }

    private static void executeEventCommand(ServerLevel level, ServerPlayer player, PendingTravel pending, String rawCommand) {
        if (rawCommand == null || rawCommand.isBlank()) {
            return;
        }
        String command = rawCommand.trim();
        command = command.startsWith("/") ? command.substring(1).trim() : command;
        if (command.isBlank()) {
            return;
        }
        if (isFinishEncounterCommand(command)) {
            return;
        }
        BlockPos pos = pending.interruptedAt() == null ? player.blockPosition() : pending.interruptedAt();
        command = command.replace("{player}", player.getGameProfile().name())
                .replace("{uuid}", player.getUUID().toString())
                .replace("{x}", Integer.toString(pos.getX()))
                .replace("{y}", Integer.toString(pos.getY()))
                .replace("{z}", Integer.toString(pos.getZ()))
                .replace("{event_tag}", pending.eventTag());
        MinecraftServer server = level.getServer();
        CommandSourceStack source = server.createCommandSourceStack()
                .withPermission(4)
                .withPosition(Vec3.atCenterOf(pos))
                .withLevel(level)
                .withSuppressedOutput();
        try {
            com.mojang.brigadier.ParseResults<CommandSourceStack> parsed = server.getCommands().getDispatcher().parse(command, source);
            if (parsed.getReader().canRead()) {
                return;
            }
            server.getCommands().performCommand(parsed, command);
        } catch (RuntimeException ignored) {
        }
    }

    private static List<UUID> trackTaggedEventEntities(ServerLevel level, PendingTravel pending) {
        if (pending.eventTag().isBlank()) {
            return pending.mobIds();
        }
        List<UUID> tracked = new ArrayList<>(pending.mobIds());
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Mob && entity.getTags().contains(pending.eventTag()) && !tracked.contains(entity.getUUID())) {
                tracked.add(entity.getUUID());
            }
        }
        return List.copyOf(tracked);
    }

    private static void makeMobHostile(Mob mob, ServerPlayer target) {
        mob.setNoAi(false);
        mob.setTarget(target);
        mob.setAggressive(true);
        if (mob.getNavigation().isDone() && mob.distanceToSqr(target) > 9.0D) {
            double speed = mob.getAttribute(Attributes.MOVEMENT_SPEED) == null
                    ? 1.15D
                    : Math.max(0.05D, mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            mob.getNavigation().moveTo(target, speed);
        }
    }

    private static void equipPirateMob(Mob mob) {
        mob.setCustomName(Component.literal("Pirate"));
        mob.setItemSlot(EquipmentSlot.HEAD, blackLeather(Items.LEATHER_HELMET));
        mob.setItemSlot(EquipmentSlot.CHEST, blackLeather(Items.LEATHER_CHESTPLATE));
        mob.setItemSlot(EquipmentSlot.LEGS, blackLeather(Items.LEATHER_LEGGINGS));
        mob.setItemSlot(EquipmentSlot.FEET, blackLeather(Items.LEATHER_BOOTS));
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }

    private static ItemStack blackLeather(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x111111));
        return stack;
    }

    private static void steerBoatTowardPlayer(Mob mob, ServerPlayer player) {
        if (mob.getVehicle() instanceof Boat boat) {
            Vec3 direction = player.position().subtract(boat.position());
            Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
            if (horizontal.lengthSqr() > 1.0D) {
                Vec3 push = horizontal.normalize().scale(0.035D);
                mob.setNoAi(true);
                mob.startRiding(boat, true, true);
                Vec3 movement = boat.getDeltaMovement().multiply(0.55D, 1.0D, 0.55D).add(push.x, 0.0D, push.z);
                Vec3 horizontalMovement = new Vec3(movement.x, 0.0D, movement.z);
                if (horizontalMovement.lengthSqr() > 0.0144D) {
                    horizontalMovement = horizontalMovement.normalize().scale(0.12D);
                    movement = new Vec3(horizontalMovement.x, movement.y, horizontalMovement.z);
                }
                boat.setDeltaMovement(movement);
                boat.fallDistance = 0.0F;
                boat.setYRot((float) (Math.atan2(push.z, push.x) * (180.0D / Math.PI)) - 90.0F);
                boat.hurtMarked = true;
            }
        }
    }

    private static void updateBoatPirate(Mob mob, ServerPlayer player) {
        if (mob.getVehicle() instanceof Boat) {
            steerBoatTowardPlayer(mob, player);
            if (mob.distanceToSqr(player) <= 12.25D && (mob.tickCount + mob.getId()) % 20 == 0) {
                player.hurt(mob.damageSources().mobAttack(mob), 3.0F);
            }
            return;
        }
        if (mob.isNoAi()) {
            mob.setNoAi(false);
        }
    }

    private static boolean isDay(ServerLevel level) {
        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= 0L && dayTime < 13000L;
    }

    private static void teleportPlayer(ServerPlayer player, ServerLevel level, double x, double y, double z) {
        player.teleportTo(level, x, y, z, java.util.Set.<Relative>of(), player.getYRot(), player.getXRot(), false);
    }

    private static void moveEntity(Entity entity, double x, double y, double z, float yRot, float xRot) {
        entity.setPos(x, y, z);
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.setOldPosAndRot();
    }

    private static ServerLevel levelFor(ServerPlayer player, ResourceLocation dimension) {
        ResourceKey<Level> key = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimension);
        return player.level().getServer().getLevel(key);
    }

    private static void cleanupEncounterEntities(ServerLevel level, List<UUID> entityIds) {
        for (UUID entityId : entityIds) {
            Entity entity = level.getEntity(entityId);
            if (entity != null) {
                entity.discard();
            }
        }
    }

    private static Optional<BlockPos> findSafeEncounterPos(ServerLevel level, BlockPos center) {
        BlockPos water = waterSurfacePos(level, center);
        if (water != null) {
            return Optional.of(water);
        }
        int verticalRadius = level.dimension() == Level.NETHER ? 48 : 24;
        if (!level.dimensionType().hasCeiling()) {
            return findSafeSurfaceNear(level, center, 24, verticalRadius);
        }
        return findSafeGroundNear(level, center, 24, verticalRadius);
    }

    private static Optional<BlockPos> findSafeMobSpawnPos(ServerLevel level, BlockPos center, int horizontalRadius, int verticalRadius) {
        if (!level.dimensionType().hasCeiling()) {
            return findSafeSurfaceNear(level, center, horizontalRadius, verticalRadius);
        }
        return findSafeGroundNear(level, center, horizontalRadius, verticalRadius);
    }

    private static Optional<BlockPos> findSafeSurfaceNear(ServerLevel level, BlockPos center, int horizontalRadius, int verticalRadius) {
        for (int radius = 0; radius <= horizontalRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    Optional<BlockPos> surfacePos = safeSurfaceColumn(level, center.getX() + dx, center.getZ() + dz);
                    if (surfacePos.isPresent() && Math.abs(surfacePos.get().getY() - center.getY()) <= verticalRadius) {
                        return surfacePos;
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<BlockPos> findSafeGroundNear(ServerLevel level, BlockPos center, int horizontalRadius, int verticalRadius) {
        for (int radius = 0; radius <= horizontalRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    Optional<BlockPos> surfacePos = safeSurfaceColumn(level, center.getX() + dx, center.getZ() + dz);
                    if (surfacePos.isPresent() && Math.abs(surfacePos.get().getY() - center.getY()) <= verticalRadius) {
                        return surfacePos;
                    }
                    Optional<BlockPos> layeredPos = safeLayeredColumn(level, center.getX() + dx, center.getZ() + dz, center.getY(), verticalRadius);
                    if (layeredPos.isPresent()) {
                        return layeredPos;
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<BlockPos> safeSurfaceColumn(ServerLevel level, int x, int z) {
        BlockPos column = new BlockPos(x, level.getMinY(), z);
        level.getChunk(column);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
        return isSafeStandPosition(level, surface) ? Optional.of(surface) : Optional.empty();
    }

    private static Optional<BlockPos> safeLayeredColumn(ServerLevel level, int x, int z, int preferredY, int verticalRadius) {
        int minY = Math.max(level.getMinY() + 2, preferredY - verticalRadius);
        int maxY = Math.min(level.getMaxY() - 3, preferredY + verticalRadius);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, preferredY, z);
        level.getChunk(cursor);
        for (int offset = 0; offset <= verticalRadius; offset++) {
            int downY = preferredY - offset;
            if (downY >= minY) {
                cursor.set(x, downY, z);
                if (isSafeStandPosition(level, cursor)) {
                    return Optional.of(cursor.immutable());
                }
            }
            int upY = preferredY + offset;
            if (offset > 0 && upY <= maxY) {
                cursor.set(x, upY, z);
                if (isSafeStandPosition(level, cursor)) {
                    return Optional.of(cursor.immutable());
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isSafeStandPosition(ServerLevel level, BlockPos pos) {
        if (pos.getY() <= level.getMinY() + 1 || pos.getY() >= level.getMaxY() - 2) {
            return false;
        }
        BlockPos floorPos = pos.below();
        BlockState floor = level.getBlockState(floorPos);
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        return floor.isFaceSturdy(level, floorPos, Direction.UP)
                && !floor.is(Blocks.BEDROCK)
                && !isDangerousBlock(floor)
                && !isDangerousBlock(feet)
                && !isDangerousBlock(head)
                && floor.getFluidState().isEmpty()
                && feet.getFluidState().isEmpty()
                && head.getFluidState().isEmpty()
                && feet.getCollisionShape(level, pos).isEmpty()
                && head.getCollisionShape(level, pos.above()).isEmpty();
    }

    private static boolean isDangerousBlock(BlockState state) {
        return state.is(Blocks.LAVA)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.POWDER_SNOW)
                || state.getFluidState().is(FluidTags.LAVA);
    }

    private static BlockPos safeSurfacePos(ServerLevel level, BlockPos pos) {
        if (level.dimension() == Level.NETHER) {
            return safeNetherPos(level, pos);
        }
        level.getChunk(pos);
        BlockPos safePos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        if (safePos.getY() <= level.getMinY() + 1) {
            safePos = new BlockPos(pos.getX(), Math.max(level.getSeaLevel() + 1, level.getMinY() + 2), pos.getZ());
        }
        return safePos;
    }

    private static BlockPos safeNetherPos(ServerLevel level, BlockPos pos) {
        int startY = Math.max(level.getMinY() + 3, Math.min(level.getMaxY() - 8, pos.getY()));
        for (int radius = 0; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    int x = pos.getX() + dx;
                    int z = pos.getZ() + dz;
                    level.getChunk(new BlockPos(x, startY, z));
                    BlockPos found = findNetherFloor(level, x, z, startY);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return new BlockPos(pos.getX(), Math.max(level.getMinY() + 4, Math.min(80, startY)), pos.getZ());
    }

    private static BlockPos findNetherFloor(ServerLevel level, int x, int z, int startY) {
        int minY = level.getMinY() + 2;
        int maxY = Math.min(level.getMaxY() - 8, 120);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, 0, z);
        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y - 1, z);
            BlockState floor = level.getBlockState(cursor);
            cursor.setY(y);
            BlockState feet = level.getBlockState(cursor);
            cursor.setY(y + 1);
            BlockState head = level.getBlockState(cursor);
            if (!floor.isAir() && !floor.is(Blocks.BEDROCK) && floor.getFluidState().isEmpty()
                    && feet.isAir() && head.isAir() && y < maxY) {
                return new BlockPos(x, y, z);
            }
        }
        for (int y = startY + 1; y < maxY; y++) {
            cursor.set(x, y - 1, z);
            BlockState floor = level.getBlockState(cursor);
            cursor.setY(y);
            BlockState feet = level.getBlockState(cursor);
            cursor.setY(y + 1);
            BlockState head = level.getBlockState(cursor);
            if (!floor.isAir() && !floor.is(Blocks.BEDROCK) && floor.getFluidState().isEmpty()
                    && feet.isAir() && head.isAir()) {
                return new BlockPos(x, y, z);
            }
        }
        return null;
    }

    private static BlockPos waterSurfacePos(ServerLevel level, BlockPos pos) {
        level.getChunk(pos);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);
        if (level.getFluidState(surface).is(FluidTags.WATER)) {
            return surface.above();
        }
        if (level.getFluidState(surface.below()).is(FluidTags.WATER)) {
            return surface;
        }
        return null;
    }

    private static BlockPos nearbyWaterSurfacePos(ServerLevel level, BlockPos center, int index) {
        for (int radius = 4; radius <= 14; radius += 2) {
            for (int attempt = 0; attempt < 8; attempt++) {
                double angle = (Math.PI * 2.0D / 8.0D) * (attempt + index * 0.5D);
                BlockPos candidate = center.offset((int) Math.round(Math.cos(angle) * radius), 0, (int) Math.round(Math.sin(angle) * radius));
                BlockPos water = waterSurfacePos(level, candidate);
                if (water != null) {
                    return water;
                }
            }
        }
        return null;
    }
}
