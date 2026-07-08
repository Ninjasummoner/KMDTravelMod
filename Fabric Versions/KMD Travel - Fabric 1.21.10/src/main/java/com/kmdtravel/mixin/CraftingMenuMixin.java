package com.kmdtravel.mixin;

import com.kmdtravel.config.KMDConfig;
import com.kmdtravel.registry.KMDItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void kmdtravel$blockDisabledSharedPostShiftClick(Player player, int index, CallbackInfoReturnable<ItemStack> callback) {
        if (index != 0 || KMDConfig.ENABLE_SHARED_POST_CRAFTING.get()) {
            return;
        }
        ItemStack result = ((CraftingMenu) (Object) this).getSlot(0).getItem();
        if (result.is(KMDItems.SHARED_FAST_TRAVEL_POST.get())) {
            player.displayClientMessage(Component.translatable("message.kmdtravel.shared_post_crafting_disabled"), true);
            callback.setReturnValue(ItemStack.EMPTY);
        }
    }
}
