package com.kmdtravel.client;

import com.kmdtravel.network.BeginTravelPacket;
import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.network.TravelEventChoicePacket;
import com.kmdtravel.network.TravelEventPromptPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class TravelProgressScreen extends KMDScreen {
    private static final double TRAVEL_FOLLOW_ZOOM = 6.0D;
    private static final double CAMERA_EASE = 0.18D;
    private final BeginTravelPacket packet;
    private int ticksOpen;
    private TravelEventPromptPacket prompt;
    private double cameraPanX;
    private double cameraPanY;
    private boolean cameraReady;

    public TravelProgressScreen(BeginTravelPacket packet) {
        super(Component.translatable("screen.kmdtravel.fast_travel"));
        this.packet = packet;
        ParchmentMap.rememberSamples(packet.worldSeed(), packet.dimension(), packet.samples());
    }

    public void setPrompt(TravelEventPromptPacket prompt) {
        this.prompt = prompt;
        rebuildPromptWidgets();
    }

    @Override
    protected void init() {
        rebuildPromptWidgets();
    }

    private void rebuildPromptWidgets() {
        clearWidgets();
        if (prompt == null) {
            return;
        }

        int centerX = width / 2;
        int centerY = height / 2 + 40;
        addRenderableWidget(Button.builder(Component.translatable("screen.kmdtravel.view_event"), button -> choose(true))
                .bounds(centerX - 154, centerY, 150, 20)
                .build());
        Component skipLabel = prompt.passive()
                ? Component.translatable("screen.kmdtravel.skip_event_peaceful")
                : Component.translatable("screen.kmdtravel.skip_event", prompt.skipChancePercent());
        addRenderableWidget(Button.builder(skipLabel, button -> choose(false))
                .bounds(centerX + 4, centerY, 150, 20)
                .build());
    }

    private void choose(boolean viewEvent) {
        KMDNetwork.sendToServer(new TravelEventChoicePacket(viewEvent));
        ClientTravelScreens.clearPrompt();
        prompt = null;
        rebuildPromptWidgets();
    }

    @Override
    public void tick() {
        if (prompt == null) {
            ticksOpen++;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        double progress = Math.min(1.0D, (ticksOpen + partialTick) / Math.max(1.0D, packet.durationTicks()));
        int alpha = (int) (Math.min(0.92D, progress < 0.18D ? progress / 0.18D * 0.92D : 0.92D) * 255.0D);
        graphics.fill(0, 0, width, height, alpha << 24);

        ParchmentMap.drawFrame(graphics, font, title, width, height, false);
        ParchmentMap.Bounds bounds = ParchmentMap.bounds(width, height, false);
        ParchmentMap.Progress mapProgress = new ParchmentMap.Progress(packet.startX(), packet.startZ(), packet.endX(), packet.endZ(), progress);
        double currentX = packet.startX() + (packet.endX() - packet.startX()) * progress;
        double currentZ = packet.startZ() + (packet.endZ() - packet.startZ()) * progress;
        double targetPanX = ParchmentMap.panXFor(bounds, packet.locations(), packet.sourceId(), TRAVEL_FOLLOW_ZOOM, currentX);
        double targetPanY = ParchmentMap.panYFor(bounds, packet.locations(), packet.sourceId(), TRAVEL_FOLLOW_ZOOM, currentZ);
        if (!cameraReady) {
            cameraPanX = targetPanX;
            cameraPanY = targetPanY;
            cameraReady = true;
        } else {
            cameraPanX += (targetPanX - cameraPanX) * CAMERA_EASE;
            cameraPanY += (targetPanY - cameraPanY) * CAMERA_EASE;
        }
        double drawPanX = Math.round(cameraPanX);
        double drawPanY = Math.round(cameraPanY);
        if (prompt == null) {
            ParchmentMap.drawMapFast(graphics, font, bounds, packet.worldSeed(), packet.dimension(), packet.locations(), packet.sourceId(),
                    mapProgress, drawPanX, drawPanY, TRAVEL_FOLLOW_ZOOM, -1000, -1000);
        } else {
            ParchmentMap.drawMapFastWithoutLabels(graphics, font, bounds, packet.worldSeed(), packet.dimension(), packet.locations(), packet.sourceId(),
                    mapProgress, drawPanX, drawPanY, TRAVEL_FOLLOW_ZOOM, -1000, -1000);
        }

        if (prompt != null) {
            drawPrompt(graphics, bounds);
        }

        for (net.minecraft.client.gui.components.Renderable renderable : renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void drawPrompt(GuiGraphics graphics, ParchmentMap.Bounds bounds) {
        int panelW = Math.min(360, bounds.w() - 40);
        int panelX = bounds.x() + bounds.w() / 2 - panelW / 2;
        int panelY = bounds.y() + bounds.h() / 2 - 46;
        int panelH = 92;
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xDD000000);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 2, 0xAAFFFFFF);
        drawCenteredShadow(graphics, prompt.titleComponent(), bounds.x() + bounds.w() / 2, panelY + 10, 0xFFFFFFFF);
        int detailY = drawWrappedCentered(graphics, prompt.descriptionComponent(), bounds.x() + bounds.w() / 2, panelY + 30, panelW - 24, 0xFFFFFFFF, 2);
        if (prompt.passive()) {
            drawWrappedCentered(graphics, Component.translatable("screen.kmdtravel.event_prompt_peaceful"), bounds.x() + bounds.w() / 2, detailY + 8, panelW - 24, 0xFFE0E0E0, 2);
        } else {
            drawCenteredShadow(graphics, Component.translatable("screen.kmdtravel.event_prompt_detail", prompt.ambushChancePercent(), prompt.skipChancePercent()),
                    bounds.x() + bounds.w() / 2, detailY + 10, 0xFFFFD36A);
        }
    }

    private void drawCenteredShadow(GuiGraphics graphics, Component component, int centerX, int y, int color) {
        graphics.drawString(font, component, centerX - font.width(component) / 2, y, color, true);
    }

    private int drawWrappedCentered(GuiGraphics graphics, Component component, int centerX, int y, int maxWidth, int color, int maxLines) {
        List<FormattedCharSequence> lines = font.split(component, maxWidth);
        int drawn = Math.min(maxLines, lines.size());
        for (int i = 0; i < drawn; i++) {
            FormattedCharSequence line = lines.get(i);
            graphics.drawString(font, line, centerX - font.width(line) / 2, y + i * 12, color, true);
        }
        return y + drawn * 12;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}



