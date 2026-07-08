package com.kmdtravel.event;

import com.kmdtravel.config.KMDConfig;
import com.kmdtravel.eventconfig.EventProfile;
import com.kmdtravel.eventconfig.EventProfileSavedData;
import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.travel.FastTravelManager;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public final class KMDCommands {
    private KMDCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kmd")
                .then(Commands.literal("finishencounter")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(context -> finishEncounterTargets(context, EntityArgument.getEntities(context, "targets"))))
                        .then(Commands.argument("target", StringArgumentType.word())
                                .executes(KMDCommands::finishEncounterTextTarget))));
        dispatcher.register(Commands.literal("kmdtravel")
                .then(Commands.literal("help")
                        .executes(context -> {
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                KMDNetwork.openHelp(player);
                                return 1;
                            }
                            context.getSource().sendSuccess(() -> Component.literal("KMD help is available in-game as a player with /kmdtravel help."), false);
                            return 1;
                        }))
                .then(Commands.literal("events")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    FastTravelManager.setEventsEnabled(enabled);
                                    context.getSource().sendSuccess(() -> Component.translatable("command.kmdtravel.events", enabled), true);
                                    return 1;
                                }))
                        .executes(context -> {
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                KMDNetwork.openEventProfileEditor(player);
                                return 1;
                            }
                            context.getSource().sendSuccess(() -> Component.translatable("command.kmdtravel.events", KMDConfig.ENABLE_EVENTS.get()), false);
                            return 1;
                        }))
                .then(Commands.literal("eventprofiles")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("list")
                                .executes(context -> {
                                    EventProfileSavedData data = EventProfileSavedData.get(context.getSource().getLevel());
                                    context.getSource().sendSuccess(() -> Component.literal("KMD event profiles:"), false);
                                    for (EventProfile profile : data.profiles()) {
                                        boolean active = profile.id().equals(data.globalProfile());
                                        context.getSource().sendSuccess(() -> Component.literal((active ? "* " : "- ") + profile.id() + " (" + profile.name() + ") - " + profile.events().size() + " events"), false);
                                    }
                                    return data.profiles().size();
                                }))
                        .then(Commands.literal("global")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> {
                                            EventProfileSavedData data = EventProfileSavedData.get(context.getSource().getLevel());
                                            String profileId = StringArgumentType.getString(context, "id");
                                            if (data.profile(profileId).isEmpty()) {
                                                context.getSource().sendFailure(Component.literal("Unknown KMD event profile: " + profileId));
                                                return 0;
                                            }
                                            data.setGlobalProfile(profileId);
                                            context.getSource().sendSuccess(() -> Component.literal("Global KMD event profile set to: " + profileId), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("id", StringArgumentType.word())
                                                .executes(context -> {
                                                    EventProfileSavedData data = EventProfileSavedData.get(context.getSource().getLevel());
                                                    String profileId = StringArgumentType.getString(context, "id");
                                                    if (data.profile(profileId).isEmpty()) {
                                                        context.getSource().sendFailure(Component.literal("Unknown KMD event profile: " + profileId));
                                                        return 0;
                                                    }
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    data.setPlayerProfile(player.getUUID(), profileId);
                                                    context.getSource().sendSuccess(() -> Component.literal("KMD event profile for " + player.getGameProfile().name() + " set to: " + profileId), true);
                                                    return 1;
                                                }))))
                        .then(Commands.literal("self")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> {
                                            EventProfileSavedData data = EventProfileSavedData.get(context.getSource().getLevel());
                                            String profileId = StringArgumentType.getString(context, "id");
                                            if (data.profile(profileId).isEmpty()) {
                                                context.getSource().sendFailure(Component.literal("Unknown KMD event profile: " + profileId));
                                                return 0;
                                            }
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            data.setPlayerProfile(player.getUUID(), profileId);
                                            context.getSource().sendSuccess(() -> Component.literal("Your KMD event profile is now: " + profileId), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("current")
                                .executes(context -> {
                                    EventProfileSavedData data = EventProfileSavedData.get(context.getSource().getLevel());
                                    context.getSource().sendSuccess(() -> Component.literal("Global KMD event profile: " + data.globalProfile()), false);
                                    return 1;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            EventProfileSavedData data = EventProfileSavedData.get(context.getSource().getLevel());
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            context.getSource().sendSuccess(() -> Component.literal("KMD event profile for " + player.getGameProfile().name() + ": " + data.playerProfile(player)), false);
                                            return 1;
                                        }))))
                .then(Commands.literal("eventwait")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(5, 600))
                                .executes(context -> {
                                    int seconds = IntegerArgumentType.getInteger(context, "seconds");
                                    FastTravelManager.setEventInvestigationSeconds(seconds);
                                    context.getSource().sendSuccess(() -> Component.translatable("command.kmdtravel.eventwait", seconds), true);
                                    return 1;
                                }))
                        .executes(context -> {
                            context.getSource().sendSuccess(() -> Component.translatable("command.kmdtravel.eventwait", KMDConfig.EVENT_INVESTIGATION_SECONDS.get()), false);
                            return 1;
                        }))
                .then(Commands.literal("basechance")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("chance", DoubleArgumentType.doubleArg(0.0D, 1.0D))
                                .executes(context -> setDouble(context, KMDConfig.BASE_EVENT_CHANCE, "command.kmdtravel.basechance", "chance"))))
                .then(Commands.literal("nightmultiplier")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.0D, 10.0D))
                                .executes(context -> setDouble(context, KMDConfig.NIGHT_EVENT_MULTIPLIER, "command.kmdtravel.nightmultiplier", "multiplier"))))
                .then(Commands.literal("armor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("perpoint")
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D, 0.1D))
                                        .executes(context -> setDouble(context, KMDConfig.ARMOR_SAFETY_PER_POINT, "command.kmdtravel.armor_per_point", "amount"))))
                        .then(Commands.literal("maxreduction")
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D, 0.95D))
                                        .executes(context -> setDouble(context, KMDConfig.MAX_ARMOR_EVENT_REDUCTION, "command.kmdtravel.armor_max", "amount")))))
                .then(Commands.literal("hunger")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("low")
                                .then(Commands.argument("bonus", DoubleArgumentType.doubleArg(0.0D, 1.0D))
                                        .executes(context -> setDouble(context, KMDConfig.HUNGER_LOW_EVENT_BONUS, "command.kmdtravel.hunger_low", "bonus"))))
                        .then(Commands.literal("medium")
                                .then(Commands.argument("bonus", DoubleArgumentType.doubleArg(0.0D, 1.0D))
                                        .executes(context -> setDouble(context, KMDConfig.HUNGER_MEDIUM_EVENT_BONUS, "command.kmdtravel.hunger_medium", "bonus")))))
                .then(Commands.literal("distancechance")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("chancePer1000", DoubleArgumentType.doubleArg(0.0D, 1.0D))
                                .executes(context -> setDouble(context, KMDConfig.DISTANCE_EVENT_CHANCE_PER_1000_BLOCKS, "command.kmdtravel.distancechance", "chancePer1000"))))
                .then(Commands.literal("maxchance")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("chance", DoubleArgumentType.doubleArg(0.0D, 1.0D))
                                .executes(context -> setDouble(context, KMDConfig.MAX_EVENT_CHANCE, "command.kmdtravel.maxchance", "chance"))))
                .then(Commands.literal("finishencounter")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(context -> finishEncounterTargets(context, EntityArgument.getEntities(context, "targets"))))
                        .then(Commands.argument("target", StringArgumentType.word())
                                .executes(KMDCommands::finishEncounterTextTarget)))
                .then(Commands.literal("sharedpostcrafting")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    KMDConfig.ENABLE_SHARED_POST_CRAFTING.set(enabled);
                                    KMDConfig.save();
                                    context.getSource().sendSuccess(() -> Component.translatable("command.kmdtravel.sharedpostcrafting", enabled), true);
                                    return 1;
                                }))
                        .executes(context -> {
                            context.getSource().sendSuccess(() -> Component.translatable("command.kmdtravel.sharedpostcrafting", KMDConfig.ENABLE_SHARED_POST_CRAFTING.get()), false);
                            return 1;
                        })));
    }

    private static int finishEncounterTargets(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets) {
        int players = 0;
        int finished = 0;
        for (Entity entity : targets) {
            if (entity instanceof ServerPlayer player) {
                players++;
                if (FastTravelManager.finishEncounter(player)) {
                    finished++;
                }
            }
        }
        if (players == 0) {
            context.getSource().sendFailure(Component.literal("No players matched that KMD encounter target."));
            return 0;
        }
        if (finished == 0) {
            context.getSource().sendFailure(Component.literal("No active KMD encounter found for the selected player(s)."));
            return 0;
        }
        int count = finished;
        context.getSource().sendSuccess(() -> Component.literal("Finished KMD encounter for " + count + " player(s)."), true);
        return finished;
    }

    private static int finishEncounterTextTarget(CommandContext<CommandSourceStack> context) {
        String target = StringArgumentType.getString(context, "target");
        if ("{player}".equals(target)) {
            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                return finishEncounterTargets(context, java.util.List.of(player));
            }
            context.getSource().sendFailure(Component.literal("{player} only resolves inside KMD event commands or when a player runs the command."));
            return 0;
        }
        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(target);
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Unknown player or KMD placeholder: " + target));
            return 0;
        }
        return finishEncounterTargets(context, java.util.List.of(player));
    }

    private static int setDouble(CommandContext<CommandSourceStack> context,
                                 KMDConfig.DoubleValue value,
                                 String translationKey,
                                 String argumentName) {
        double amount = DoubleArgumentType.getDouble(context, argumentName);
        value.set(amount);
        KMDConfig.save();
        context.getSource().sendSuccess(() -> Component.translatable(translationKey, amount), true);
        return 1;
    }
}
