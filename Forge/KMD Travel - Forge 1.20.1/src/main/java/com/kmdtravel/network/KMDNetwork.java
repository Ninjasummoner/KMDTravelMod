package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.eventconfig.EventProfileSavedData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Comparator;
import java.util.List;

public final class KMDNetwork {
    private static final String PROTOCOL = "1";
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(KMDTravel.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private KMDNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, OpenTravelScreenPacket.class,
                OpenTravelScreenPacket::encode,
                OpenTravelScreenPacket::decode,
                OpenTravelScreenPacket::handle);
        CHANNEL.registerMessage(packetId++, TravelRequestPacket.class,
                TravelRequestPacket::encode,
                TravelRequestPacket::decode,
                TravelRequestPacket::handle);
        CHANNEL.registerMessage(packetId++, BeginTravelPacket.class,
                BeginTravelPacket::encode,
                BeginTravelPacket::decode,
                BeginTravelPacket::handle);
        CHANNEL.registerMessage(packetId++, EndTravelOverlayPacket.class,
                EndTravelOverlayPacket::encode,
                EndTravelOverlayPacket::decode,
                EndTravelOverlayPacket::handle);
        CHANNEL.registerMessage(packetId++, TravelEventPromptPacket.class,
                TravelEventPromptPacket::encode,
                TravelEventPromptPacket::decode,
                TravelEventPromptPacket::handle);
        CHANNEL.registerMessage(packetId++, TravelEventChoicePacket.class,
                TravelEventChoicePacket::encode,
                TravelEventChoicePacket::decode,
                TravelEventChoicePacket::handle);
        CHANNEL.registerMessage(packetId++, OpenRenamePostPacket.class,
                OpenRenamePostPacket::encode,
                OpenRenamePostPacket::decode,
                OpenRenamePostPacket::handle);
        CHANNEL.registerMessage(packetId++, RenamePostPacket.class,
                RenamePostPacket::encode,
                RenamePostPacket::decode,
                RenamePostPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenKMDHelpPacket.class,
                OpenKMDHelpPacket::encode,
                OpenKMDHelpPacket::decode,
                OpenKMDHelpPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenEventProfileEditorPacket.class,
                OpenEventProfileEditorPacket::encode,
                OpenEventProfileEditorPacket::decode,
                OpenEventProfileEditorPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveEventProfilePacket.class,
                SaveEventProfilePacket::encode,
                SaveEventProfilePacket::decode,
                SaveEventProfilePacket::handle);
        CHANNEL.registerMessage(packetId++, SetDefaultEventProfilePacket.class,
                SetDefaultEventProfilePacket::encode,
                SetDefaultEventProfilePacket::decode,
                SetDefaultEventProfilePacket::handle);
        CHANNEL.registerMessage(packetId++, HeldMapDataPacket.class,
                HeldMapDataPacket::encode, HeldMapDataPacket::decode, HeldMapDataPacket::handle);
        CHANNEL.registerMessage(packetId++, HeldMapDataRequestPacket.class,
                HeldMapDataRequestPacket::encode, HeldMapDataRequestPacket::decode, HeldMapDataRequestPacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
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
        List<String> biomes = player.getServer().registryAccess().registryOrThrow(Registries.BIOME).keySet().stream()
                .map(ResourceLocation::toString)
                .sorted(Comparator.naturalOrder())
                .toList();
        sendToPlayer(player, new OpenEventProfileEditorPacket(EventProfileSavedData.get(player.serverLevel()).saveClientTag(), mobs, dimensions, biomes));
    }
}
