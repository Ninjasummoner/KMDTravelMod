package com.kmdtravel.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.kmdtravel.eventconfig.EventProfileSavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Comparator;
import java.util.List;

public final class KMDNetwork {
    private static final String PROTOCOL = "1";

    private KMDNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToClient(OpenTravelScreenPacket.TYPE, OpenTravelScreenPacket.STREAM_CODEC, OpenTravelScreenPacket::handle);
        registrar.playToServer(TravelRequestPacket.TYPE, TravelRequestPacket.STREAM_CODEC, TravelRequestPacket::handle);
        registrar.playToClient(BeginTravelPacket.TYPE, BeginTravelPacket.STREAM_CODEC, BeginTravelPacket::handle);
        registrar.playToClient(EndTravelOverlayPacket.TYPE, EndTravelOverlayPacket.STREAM_CODEC, EndTravelOverlayPacket::handle);
        registrar.playToClient(TravelEventPromptPacket.TYPE, TravelEventPromptPacket.STREAM_CODEC, TravelEventPromptPacket::handle);
        registrar.playToServer(TravelEventChoicePacket.TYPE, TravelEventChoicePacket.STREAM_CODEC, TravelEventChoicePacket::handle);
        registrar.playToClient(OpenRenamePostPacket.TYPE, OpenRenamePostPacket.STREAM_CODEC, OpenRenamePostPacket::handle);
        registrar.playToServer(RenamePostPacket.TYPE, RenamePostPacket.STREAM_CODEC, RenamePostPacket::handle);
        registrar.playToClient(OpenKMDHelpPacket.TYPE, OpenKMDHelpPacket.STREAM_CODEC, OpenKMDHelpPacket::handle);
        registrar.playToClient(OpenEventProfileEditorPacket.TYPE, OpenEventProfileEditorPacket.STREAM_CODEC, OpenEventProfileEditorPacket::handle);
        registrar.playToServer(SaveEventProfilePacket.TYPE, SaveEventProfilePacket.STREAM_CODEC, SaveEventProfilePacket::handle);
        registrar.playToServer(SetDefaultEventProfilePacket.TYPE, SetDefaultEventProfilePacket.STREAM_CODEC, SetDefaultEventProfilePacket::handle);
        registrar.playToServer(HeldMapDataRequestPacket.TYPE, HeldMapDataRequestPacket.STREAM_CODEC, HeldMapDataRequestPacket::handle);
        registrar.playToClient(HeldMapDataPacket.TYPE, HeldMapDataPacket.STREAM_CODEC, HeldMapDataPacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
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
