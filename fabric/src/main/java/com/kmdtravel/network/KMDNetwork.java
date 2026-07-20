package com.kmdtravel.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.kmdtravel.eventconfig.EventProfileSavedData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.Comparator;
import java.util.List;

public final class KMDNetwork {
    private KMDNetwork() {
    }

    public static void registerServer() {
        PayloadTypeRegistry.playS2C().register(OpenTravelScreenPacket.TYPE, OpenTravelScreenPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(BeginTravelPacket.TYPE, BeginTravelPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(EndTravelOverlayPacket.TYPE, EndTravelOverlayPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(TravelEventPromptPacket.TYPE, TravelEventPromptPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenRenamePostPacket.TYPE, OpenRenamePostPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenKMDHelpPacket.TYPE, OpenKMDHelpPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenEventProfileEditorPacket.TYPE, OpenEventProfileEditorPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(HeldMapDataPacket.TYPE, HeldMapDataPacket.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(TravelRequestPacket.TYPE, TravelRequestPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(TravelEventChoicePacket.TYPE, TravelEventChoicePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RenamePostPacket.TYPE, RenamePostPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SaveEventProfilePacket.TYPE, SaveEventProfilePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SetDefaultEventProfilePacket.TYPE, SetDefaultEventProfilePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(HeldMapDataRequestPacket.TYPE, HeldMapDataRequestPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TravelRequestPacket.TYPE, TravelRequestPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(TravelEventChoicePacket.TYPE, TravelEventChoicePacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(RenamePostPacket.TYPE, RenamePostPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(SaveEventProfilePacket.TYPE, SaveEventProfilePacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(SetDefaultEventProfilePacket.TYPE, SetDefaultEventProfilePacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(HeldMapDataRequestPacket.TYPE, HeldMapDataRequestPacket::handle);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(OpenTravelScreenPacket.TYPE, OpenTravelScreenPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(BeginTravelPacket.TYPE, BeginTravelPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(EndTravelOverlayPacket.TYPE, EndTravelOverlayPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(TravelEventPromptPacket.TYPE, TravelEventPromptPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(OpenRenamePostPacket.TYPE, OpenRenamePostPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(OpenKMDHelpPacket.TYPE, OpenKMDHelpPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(OpenEventProfileEditorPacket.TYPE, OpenEventProfileEditorPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(HeldMapDataPacket.TYPE, HeldMapDataPacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        ClientPlayNetworking.send(packet);
    }

    public static void openHelp(ServerPlayer player) {
        sendToPlayer(player, OpenKMDHelpPacket.INSTANCE);
    }

    public static void openEventProfileEditor(ServerPlayer player) {
        List<String> mobs = BuiltInRegistries.ENTITY_TYPE.keySet().stream()
                .map(ResourceLocation::toString)
                .sorted()
                .toList();
        List<String> dimensions = player.getServer().levelKeys().stream()
                .map(key -> key.location().toString())
                .sorted()
                .toList();
        List<String> biomes = player.registryAccess().registryOrThrow(Registries.BIOME).keySet().stream()
                .map(ResourceLocation::toString)
                .sorted(Comparator.naturalOrder())
                .toList();
        sendToPlayer(player, new OpenEventProfileEditorPacket(EventProfileSavedData.get(player.serverLevel()).saveClientTag(), mobs, dimensions, biomes));
    }
}
