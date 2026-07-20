package com.kmdtravel.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class FastTravelPostRenderer implements BlockEntityRenderer<FastTravelPostBlockEntity, FastTravelPostRenderer.State> {
    public FastTravelPostRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FastTravelPostBlockEntity post, State state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(post, state, partialTick, cameraPos, crumblingOverlay);
        state.name = post.getPostName().getString();
        state.textColor = post.getTextColor();
        state.shared = post.isShared();
        state.blockState = post.getBlockState();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.shared || state.name.isBlank() || state.blockState == null) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        String[] lines = wrapName(font, state.name, 58, 3);
        if (lines.length == 0) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.35D, 0.5D);
        float facing = state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing));
        poseStack.translate(-0.15625D, 0.0D, -0.052D);
        poseStack.scale(-0.007F, -0.007F, 0.007F);
        float startY = lines.length == 1 ? -4.0F : lines.length == 2 ? -8.0F : -11.0F;
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            collector.submitText(
                    poseStack,
                    -font.width(line) / 2.0F,
                    startY + index * 7.0F,
                    FormattedCharSequence.forward(line, Style.EMPTY),
                    false,
            Font.DisplayMode.POLYGON_OFFSET,
            state.lightCoords,
            0xFF000000 | (state.textColor & 0xFFFFFF),
            0,
            0);
        }
        poseStack.popPose();
    }

    private static String[] wrapName(Font font, String name, int maxWidth, int maxLines) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return new String[0];
        }
        String[] lines = new String[maxLines];
        int count = 0;
        while (!trimmed.isEmpty() && count < maxLines) {
            String line = font.plainSubstrByWidth(trimmed, maxWidth).trim();
            if (line.isEmpty()) {
                line = trimmed.substring(0, 1);
            }
            lines[count++] = line;
            trimmed = trimmed.substring(Math.min(trimmed.length(), line.length())).trim();
        }
        String[] result = new String[count];
        System.arraycopy(lines, 0, result, 0, count);
        return result;
    }

    public static class State extends BlockEntityRenderState {
        String name = "";
        int textColor = 0x111111;
        boolean shared;
        BlockState blockState;
    }
}
