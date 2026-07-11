package com.kmdtravel.mixin;

import com.kmdtravel.config.KMDConfig;
import com.kmdtravel.registry.KMDItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void kmdtravel$blockDisabledSharedPostCraft(int amount, CallbackInfoReturnable<ItemStack> callback) {
        ItemStack stack = ((Slot) (Object) this).getItem();
        if (!KMDConfig.ENABLE_SHARED_POST_CRAFTING.get() && stack.is(KMDItems.SHARED_FAST_TRAVEL_POST.get())) {
            callback.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void kmdtravel$cancelDisabledSharedPostTake(Player player, ItemStack stack, org.spongepowered.asm.mixin.injection.callback.CallbackInfo callback) {
        if (!KMDConfig.ENABLE_SHARED_POST_CRAFTING.get() && stack.is(KMDItems.SHARED_FAST_TRAVEL_POST.get())) {
            player.sendOverlayMessage(Component.translatable("message.kmdtravel.shared_post_crafting_disabled"));
            callback.cancel();
        }
    }
}
