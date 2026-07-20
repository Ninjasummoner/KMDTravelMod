package com.kmdtravel.mixin;

import com.kmdtravel.config.KMDConfig;
import com.kmdtravel.registry.KMDItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void kmdtravel$blockDisabledSharedPostPickup(Player player, CallbackInfoReturnable<Boolean> callback) {
        Slot slot = (Slot) (Object) this;
        if (slot instanceof ResultSlot
                && !KMDConfig.ENABLE_SHARED_POST_CRAFTING.get()
                && slot.getItem().is(KMDItems.SHARED_FAST_TRAVEL_POST.get())) {
            player.sendOverlayMessage(Component.translatable("message.kmdtravel.shared_post_crafting_disabled"));
            callback.setReturnValue(false);
        }
    }
}
