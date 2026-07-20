package com.kmdtravel.mixin;

import com.kmdtravel.client.render.TravelMapHandRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(
            method = "renderHandsWithItems",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kmdtravel$renderTravelMap(
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            LocalPlayer player,
            int packedLight,
            CallbackInfo callback
    ) {
        if (TravelMapHandRenderer.renderHeld(
                poseStack,
                collector,
                packedLight,
                player,
                partialTick
        )) {
            Minecraft minecraft = Minecraft.getInstance();

            minecraft.gameRenderer
                    .getFeatureRenderDispatcher()
                    .renderAllFeatures();


            minecraft.renderBuffers()
                    .bufferSource()
                    .endBatch();

            callback.cancel();
        }
    }
}