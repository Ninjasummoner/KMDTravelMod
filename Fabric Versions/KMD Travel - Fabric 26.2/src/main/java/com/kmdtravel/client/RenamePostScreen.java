package com.kmdtravel.client;

import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.network.OpenRenamePostPacket;
import com.kmdtravel.network.RenamePostPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class RenamePostScreen extends KMDScreen {
    private final OpenRenamePostPacket packet;
    private EditBox nameInput;

    public RenamePostScreen(OpenRenamePostPacket packet) {
        super(Component.translatable("screen.kmdtravel.rename_post"));
        this.packet = packet;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;
        nameInput = new EditBox(font, centerX - 100, centerY - 10, 200, 20, Component.translatable("screen.kmdtravel.rename_post_hint"));
        nameInput.setMaxLength(32);
        nameInput.setValue(packet.currentName());
        nameInput.setFocused(true);
        setInitialFocus(nameInput);
        addRenderableWidget(nameInput);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> applyName())
                .bounds(centerX - 102, centerY + 20, 98, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(centerX + 4, centerY + 20, 98, 20)
                .build());
    }

    private void applyName() {
        KMDNetwork.sendToServer(new RenamePostPacket(packet.pos(), nameInput.getValue()));
        onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xCC000000);
        graphics.centeredText(font, title, width / 2, height / 2 - 36, 0xFFFFFFFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (nameInput.keyPressed(event) || nameInput.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return nameInput.charTyped(event) || super.charTyped(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}




