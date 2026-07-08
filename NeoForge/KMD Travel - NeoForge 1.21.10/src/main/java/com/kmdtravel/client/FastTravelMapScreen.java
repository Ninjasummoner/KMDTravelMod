package com.kmdtravel.client;

import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.network.OpenTravelScreenPacket;
import com.kmdtravel.network.TravelRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

public class FastTravelMapScreen extends KMDScreen {
    private final OpenTravelScreenPacket packet;
    private final List<OpenTravelScreenPacket.Entry> locations;
    private double panX;
    private double panY;
    private boolean dragging;
    private double lastMouseX;
    private double lastMouseY;
    private double zoom = 1.0D;
    private int locationScroll;
    private boolean locationsOpen;
    private int selectedListIndex = -1;
    private boolean openingSoundPlayed;

    public FastTravelMapScreen(OpenTravelScreenPacket packet) {
        super(Component.translatable("screen.kmdtravel.fast_travel"));
        this.packet = packet;
        this.locations = packet.locations();
        ParchmentMap.rememberSamples(packet.worldSeed(), packet.dimension(), packet.samples());
    }

    @Override
    protected void init() {
        if (!openingSoundPlayed && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.75F, 0.85F);
            openingSoundPlayed = true;
        }
        ParchmentMap.Bounds bounds = ParchmentMap.bounds(width, height, locationsOpen);
        OpenTravelScreenPacket.Entry source = sourceEntry();
        if (source != null) {
            zoom = initialZoom();
            focusOn(bounds, source);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ParchmentMap.drawFrame(graphics, font, title, width, height, locationsOpen);
        ParchmentMap.Bounds bounds = ParchmentMap.bounds(width, height, locationsOpen);
        if (locationsOpen) {
            ParchmentMap.Bounds listBounds = ParchmentMap.listBounds(width, height);
            ParchmentMap.drawLocationList(graphics, font, listBounds, locations, packet.sourceId(), locationScroll, mouseX, mouseY);
        }
        ParchmentMap.drawMap(graphics, font, bounds, packet.worldSeed(), packet.dimension(), locations, packet.sourceId(), null, panX, panY, zoom, mouseX, mouseY);
        ParchmentMap.drawLocationToggle(graphics, font, ParchmentMap.listToggleBounds(width, height, locationsOpen), locationsOpen);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0) {
            ParchmentMap.Bounds bounds = ParchmentMap.bounds(width, height, locationsOpen);
            ParchmentMap.Bounds toggleBounds = ParchmentMap.listToggleBounds(width, height, locationsOpen);
            if (toggleBounds.contains(mouseX, mouseY)) {
                locationsOpen = !locationsOpen;
                return true;
            }

            if (locationsOpen) {
                ParchmentMap.Bounds listBounds = ParchmentMap.listBounds(width, height);
                int listIndex = ParchmentMap.locationListIndex(listBounds, locations, locationScroll, mouseX, mouseY);
                if (listIndex >= 0) {
                    OpenTravelScreenPacket.Entry selected = locations.get(listIndex);
                    selectedListIndex = listIndex;
                    zoom = Math.max(zoom, 8.0D);
                    focusOn(bounds, selected);
                    return true;
                }
            }

            OpenTravelScreenPacket.Entry hovered = ParchmentMap.hovered(bounds, locations, packet.sourceId(), panX, panY, zoom, mouseX, mouseY);
            if (hovered != null && !hovered.id().equals(packet.sourceId())) {
                KMDNetwork.sendToServer(new TravelRequestPacket(packet.sourceId(), hovered.id()));
                onClose();
                return true;
            }
            if (bounds.contains(mouseX, mouseY)) {
                dragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (dragging) {
            panX += mouseX - lastMouseX;
            panY += mouseY - lastMouseY;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ParchmentMap.Bounds listBounds = ParchmentMap.listBounds(width, height);
        if (locationsOpen && listBounds.contains(mouseX, mouseY)) {
                int visible = ParchmentMap.visibleLocationRows(listBounds);
                locationScroll = Math.max(0, Math.min(Math.max(0, locations.size() - visible), locationScroll - (int) Math.signum(scrollY)));
                return true;
            }

        ParchmentMap.Bounds bounds = ParchmentMap.bounds(width, height, locationsOpen);
        if (bounds.contains(mouseX, mouseY)) {
            double oldZoom = zoom;
            zoom = ParchmentMap.clampZoom(zoom * (scrollY > 0 ? 1.18D : 0.85D));
            panX = mouseX - (mouseX - panX - bounds.x() - bounds.w() / 2.0D) * (zoom / oldZoom) - bounds.x() - bounds.w() / 2.0D;
            panY = mouseY - (mouseY - panY - bounds.y() - bounds.h() / 2.0D) * (zoom / oldZoom) - bounds.y() - bounds.h() / 2.0D;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void focusOn(ParchmentMap.Bounds bounds, OpenTravelScreenPacket.Entry selected) {
        panX = ParchmentMap.panXFor(bounds, locations, packet.sourceId(), zoom, selected);
        panY = ParchmentMap.panYFor(bounds, locations, packet.sourceId(), zoom, selected);
    }

    private OpenTravelScreenPacket.Entry sourceEntry() {
        for (OpenTravelScreenPacket.Entry location : locations) {
            if (location.id().equals(packet.sourceId())) {
                return location;
            }
        }
        return locations.isEmpty() ? null : locations.getFirst();
    }

    private double initialZoom() {
        OpenTravelScreenPacket.Entry source = sourceEntry();
        if (source == null) {
            return 1.0D;
        }
        int farthest = 0;
        for (OpenTravelScreenPacket.Entry location : locations) {
            farthest = Math.max(farthest, Math.max(Math.abs(location.x() - source.x()), Math.abs(location.z() - source.z())));
        }
        return farthest > 3000 ? 12.0D : farthest > 1200 ? 8.0D : 3.0D;
    }

}





