package com.kmdtravel.client.render;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.registry.KMDItems;
import com.kmdtravel.client.ClientMapCache;
import com.kmdtravel.network.HeldMapDataRequestPacket;
import com.kmdtravel.network.KMDNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class TravelMapHandRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KMDTravel.MOD_ID, "textures/item/travel_scroll_texture.png");
    private static final float ANIMATION_TICKS = TravelMapModel.ANIMATION_LENGTH * 20.0F;

    private static TravelMapModel model;
    private static float previousAnimationTicks;
    private static float animationTicks;
    private static boolean wasHeld;
    private static int lastMapDataRequestTick = Integer.MIN_VALUE;

    private TravelMapHandRenderer() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean held = minecraft.player != null && minecraft.player.getMainHandItem().is(KMDItems.TRAVEL_MAP.get());

        if (!held) {
            wasHeld = false;
            previousAnimationTicks = 0.0F;
            animationTicks = 0.0F;
            lastMapDataRequestTick = Integer.MIN_VALUE;
            return;
        }

        if (minecraft.level != null
                && (!ClientMapCache.hasActiveMap()
                || !ClientMapCache.matchesDimension(minecraft.level.dimension().location())
                || lastMapDataRequestTick == Integer.MIN_VALUE
                || minecraft.player.tickCount - lastMapDataRequestTick >= 40)) {
            KMDNetwork.sendToServer(HeldMapDataRequestPacket.INSTANCE);
            lastMapDataRequestTick = minecraft.player.tickCount;
        }

        if (!wasHeld) {
            previousAnimationTicks = 0.0F;
            animationTicks = 0.0F;
            wasHeld = true;
            return;
        }

        if (minecraft.level == null
                || !ClientMapCache.hasActiveMap()
                || !ClientMapCache.matchesDimension(minecraft.level.dimension().location())
                || !ClientMapCache.hasSampleAt(minecraft.player.getBlockX(), minecraft.player.getBlockZ())) {
            previousAnimationTicks = 0.0F;
            animationTicks = 0.0F;
            return;
        }

        previousAnimationTicks = animationTicks;
        animationTicks = Math.min(animationTicks + 1.0F, ANIMATION_TICKS);
    }

    public static boolean renderHeld(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
            AbstractClientPlayer player, float partialTick) {
        if (!player.getMainHandItem().is(KMDItems.TRAVEL_MAP.get())) {
            return false;
        }

        float animationSeconds = Mth.lerp(partialTick, previousAnimationTicks, animationTicks) / 20.0F;
        float progress = Mth.clamp(animationSeconds / TravelMapModel.ANIMATION_LENGTH, 0.0F, 1.0F);
        float easedProgress = progress * progress * (3.0F - 2.0F * progress);

        float mapTilt = calculateMapTilt(player.getViewXRot(partialTick));
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.04F - mapTilt * 0.5F, -0.72F);
        poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(mapTilt * -85.0F)));
        renderHands(poseStack, collector, packedLight, player, easedProgress);
        renderTravelMap(poseStack, collector, packedLight, animationSeconds, easedProgress);
        poseStack.popPose();
        return true;
    }

    private static void renderHands(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            AbstractClientPlayer player,
            float progress
    ) {
        if (player.isInvisible()) {
            return;
        }

        AvatarRenderer playerRenderer = (AvatarRenderer) Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .getRenderer(player);

        // The right hand anchors the closed roller throughout the animation.
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(90.0F)));
        renderMapHand(poseStack, collector, packedLight, player, playerRenderer, true);
        poseStack.popPose();

        if (progress > 0.01F) {
            float pull = Mth.clamp((progress - 0.01F) / 0.94F, 0.0F, 1.0F);
            pull = pull * pull * (3.0F - 2.0F * pull);
            poseStack.pushPose();
            // Apply this before rotating the arm so it travels across the screen
            // instead of moving toward the camera and appearing to change size.
            poseStack.translate(Mth.lerp(pull, 0.92F, 0.0F), Mth.lerp(pull, 0.12F, 0.0F), 0.0F);
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(90.0F)));
            renderMapHand(poseStack, collector, packedLight, player, playerRenderer, false);
            poseStack.popPose();
        }
    }

    private static void renderMapHand(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            AbstractClientPlayer player,
            AvatarRenderer renderer,
            boolean right
    ) {
        poseStack.pushPose();
        float side = right ? 1.0F : -1.0F;
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(92.0F)));
        poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(45.0F)));
        poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(side * -41.0F)));
        poseStack.translate(side * 0.3F, -1.1F, 0.45F);
        if (right) {
            renderer.renderRightHand(poseStack, collector, packedLight, player.getSkin().body().texturePath(), false);
        } else {
            renderer.renderLeftHand(poseStack, collector, packedLight, player.getSkin().body().texturePath(), false);
        }
        poseStack.popPose();
    }

    private static void renderTravelMap(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            float animationSeconds,
            float progress
    ) {
        if (model == null) {
            model = new TravelMapModel(Minecraft.getInstance().getEntityModels().bakeLayer(TravelMapModel.LAYER));
        }

        model.setupAnimation(animationSeconds);
        poseStack.pushPose();

        float closedOffset = 0.44F * (1.0F - progress);
        poseStack.translate(closedOffset, -0.02F, -0.08F);
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.PI));
        poseStack.mulPose(new Quaternionf().rotationZ((float) Math.PI));
        poseStack.mulPose(new Quaternionf().rotationX(-0.04F));
        poseStack.scale(0.84F, 0.84F, 0.84F);
        poseStack.translate(-0.5F, 0.5F, -0.5F);

        PoseStack modelPose = snapshotPoseStack(poseStack);
        collector.submitCustomGeometry(poseStack, RenderType.entityCutoutNoCull(TEXTURE),
                (pose, consumer) -> model.render(modelPose, consumer, packedLight));
        HeldTravelMapRenderer.render(poseStack, collector, packedLight, Minecraft.getInstance().player, progress);
        poseStack.popPose();
    }

    private static float calculateMapTilt(float pitch) {
        float value = Mth.clamp(1.0F - pitch / 45.0F + 0.1F, 0.0F, 1.0F);
        return -Mth.cos(value * (float) Math.PI) * 0.5F + 0.5F;
    }

    private static PoseStack snapshotPoseStack(PoseStack source) {
        PoseStack snapshot = new PoseStack();
        snapshot.mulPose(new Matrix4f(source.last().pose()));
        return snapshot;
    }
}
