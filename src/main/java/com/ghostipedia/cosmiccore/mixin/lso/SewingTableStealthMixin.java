package com.ghostipedia.cosmiccore.mixin.lso;

import com.ghostipedia.cosmiccore.common.item.StealthCoatingItem;
import com.ghostipedia.cosmiccore.common.murkbloom.StealthCoating;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sfiomn.legendarysurvivaloverhaul.common.containers.SewingTableContainer;

@Mixin(value = SewingTableContainer.class)
public abstract class SewingTableStealthMixin extends ItemCombinerMenu {

    protected SewingTableStealthMixin(MenuType<?> type, int containerId, Inventory inventory,
                                      ContainerLevelAccess access) {
        super(type, containerId, inventory, access);
    }

    @Inject(method = "isItemCoat", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$acceptStealthCoat(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof StealthCoatingItem) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isItemArmor", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$acceptCoatableGear(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (StealthCoating.canCoat(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$stealthResult(CallbackInfo ci) {
        ItemStack addition = this.inputSlots.getItem(1);
        if (!(addition.getItem() instanceof StealthCoatingItem coating)) return;
        ci.cancel();

        ItemStack base = this.inputSlots.getItem(0);
        ItemStack result = ItemStack.EMPTY;
        if (StealthCoating.canCoat(base) && StealthCoating.tier(base) < coating.getTier()) {
            result = base.copyWithCount(1);
            StealthCoating.setTier(result, coating.getTier());
        }
        this.resultSlots.setItem(0, result);
    }
}
