package com.kmdtravel.client;

import com.kmdtravel.network.OpenTravelScreenPacket;
import com.kmdtravel.network.BeginTravelPacket;
import com.kmdtravel.network.OpenEventProfileEditorPacket;
import com.kmdtravel.network.OpenRenamePostPacket;
import com.kmdtravel.network.TravelEventPromptPacket;
import net.minecraft.client.Minecraft;

public final class ClientTravelScreens {
    private static TravelEventPromptPacket activePrompt;

    private ClientTravelScreens() {
    }

    public static void open(OpenTravelScreenPacket packet) {
        activePrompt = null;
        ClientMapCache.useMap(packet.worldSeed(), packet.dimension());
        Minecraft.getInstance().setScreen(new FastTravelMapScreen(packet));
    }

    public static void openTravelProgress(BeginTravelPacket packet) {
        activePrompt = null;
        ClientMapCache.useMap(packet.worldSeed(), packet.dimension());
        Minecraft.getInstance().setScreen(new TravelProgressScreen(packet));
    }

    public static void closeTravelProgress() {
        activePrompt = null;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof TravelProgressScreen) {
            minecraft.setScreen(null);
        }
    }

    public static void openRenamePost(OpenRenamePostPacket packet) {
        Minecraft.getInstance().setScreen(new RenamePostScreen(packet));
    }

    public static void openEventProfileEditor(OpenEventProfileEditorPacket packet) {
        Minecraft.getInstance().setScreen(new EventProfileEditorScreen(packet));
    }

    public static void openHelp() {
        Minecraft.getInstance().setScreen(new KMDHelpScreen());
    }

    public static void showEventPrompt(TravelEventPromptPacket prompt) {
        activePrompt = prompt;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof TravelProgressScreen progressScreen) {
            progressScreen.setPrompt(prompt);
        }
    }

    public static TravelEventPromptPacket activePrompt() {
        return activePrompt;
    }

    public static void clearPrompt() {
        activePrompt = null;
    }
}
