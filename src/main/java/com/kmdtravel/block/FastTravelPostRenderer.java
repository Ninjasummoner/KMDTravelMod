package com.kmdtravel.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FastTravelPostRenderer implements BlockEntityRenderer<FastTravelPostBlockEntity> {
    public FastTravelPostRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FastTravelPostBlockEntity post) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public void render(FastTravelPostBlockEntity post, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (post.isShared()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        String name = post.getPostName().getString();
        if (name.isBlank()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.35D, 0.5D);
        float facing = post.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing));
        poseStack.translate(-0.15625D, 0.0D, -0.052D);
        poseStack.scale(-0.007F, -0.007F, 0.007F);
        int light = Math.max(packedLight, 15728880);
        int color = post.getTextColor();
        String[] lines = wrapName(font, name, 58, 3);
        float startY = lines.length == 1 ? -4.0F : lines.length == 2 ? -8.0F : -11.0F;
        for (int index = 0; index < lines.length; index++) {
            drawCentered(font, lines[index], startY + index * 7.0F, color, poseStack, buffer, light);
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

    private static void drawCentered(Font font, String text, float y, int color, PoseStack poseStack, MultiBufferSource buffer, int light) {
        int width = font.width(text);
        font.drawInBatch(text, -width / 2.0F, y, color, false, poseStack.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, light);
    }
}
