package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.armor.AbyssModule;
import com.ghostipedia.cosmiccore.common.armor.LegacyArmorModuleRecovery;
import com.ghostipedia.cosmiccore.common.armor.OxygenTankModule;
import com.ghostipedia.cosmiccore.common.armor.UtilityArmorModule;

import com.gregtechceu.gtceu.api.item.capability.ModularItemStack;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModularItemData;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModularItemStack.class, remap = false)
public abstract class LegacyArmorModuleRecoveryMixin {

    @Shadow
    @Final
    private ItemStack stack;

    @Inject(method = "getData", at = @At("RETURN"), cancellable = true)
    private void cosmiccore$recoverLegacyContents(CallbackInfoReturnable<ModularItemData> cir) {
        cir.setReturnValue(
                LegacyArmorModuleRecovery.recover(stack, cir.getReturnValue()));
    }

    @Inject(method = "attach(Lcom/gregtechceu/gtceu/api/item/module/ItemModule;Lnet/minecraft/world/item/ItemStack;IZ)Lcom/gregtechceu/gtceu/api/item/module/ModuleContext;",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$preserveOccupiedAbyssSlot(ItemModule module,
                                                      ItemStack item, int slot, boolean simulate,
                                                      CallbackInfoReturnable<ModuleContext> cir) {
        if (!(module instanceof AbyssModule ||
                module instanceof UtilityArmorModule ||
                module instanceof OxygenTankModule))
            return;
        var data = stack.get(GTDataComponents.MODULAR_ITEM_DATA);
        if (slot < 0 || data != null && data.getModuleDataForSlot(slot) != null) cir.setReturnValue(null);
    }
}
