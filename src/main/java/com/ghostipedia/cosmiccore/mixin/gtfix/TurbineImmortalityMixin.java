package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.TurbineRotorImmortality;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.behavior.TurbineRotorBehaviour;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = TurbineRotorBehaviour.class, remap = false)
public abstract class TurbineImmortalityMixin {

    @Inject(method = "getPartMaxDurability", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$removeMaximumDurability(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        TurbineRotorImmortality.normalize(stack);
        cir.setReturnValue(0);
    }

    @Inject(method = "getRotorDurabilityPercent", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$reportFullRotorDurability(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        TurbineRotorImmortality.normalize(stack);
        cir.setReturnValue(100);
    }

    @Inject(method = "applyRotorDamage", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$preventRotorDamage(ItemStack stack, int damageApplied, CallbackInfo ci) {
        TurbineRotorImmortality.normalize(stack);
        ci.cancel();
    }

    @Inject(method = "getRotorPower", at = @At("HEAD"))
    private void cosmiccore$normalizeLegacyRotorForPower(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        TurbineRotorImmortality.normalize(stack);
    }

    @Inject(method = "getRotorEfficiency", at = @At("HEAD"))
    private void cosmiccore$normalizeLegacyRotorForEfficiency(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        TurbineRotorImmortality.normalize(stack);
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$replaceRotorTooltip(ItemStack stack, Item.TooltipContext context,
                                                List<Component> tooltipComponents, TooltipFlag flag,
                                                CallbackInfo ci) {
        TurbineRotorImmortality.normalize(stack);
        TurbineRotorBehaviour rotor = (TurbineRotorBehaviour) (Object) this;
        tooltipComponents.add(Component.translatable(
                "metaitem.tool.tooltip.primary_material", rotor.getPartMaterial(stack).getLocalizedName()));
        tooltipComponents.add(Component.translatable(
                "metaitem.tool.tooltip.rotor.efficiency", rotor.getRotorEfficiency(stack)));
        tooltipComponents.add(Component.translatable(
                "metaitem.tool.tooltip.rotor.power", rotor.getRotorPower(stack)));
        ci.cancel();
    }

    public void setPartMaterial(ItemStack stack, Material material) {
        if (!material.hasProperty(PropertyKey.INGOT)) {
            throw new IllegalArgumentException("Part material must have an Ingot!");
        }
        stack.set(GTDataComponents.ITEM_MATERIAL, material);
        TurbineRotorImmortality.normalize(stack);
    }

    public int getPartDamage(ItemStack stack) {
        TurbineRotorImmortality.normalize(stack);
        return 0;
    }

    public void setPartDamage(ItemStack stack, int damage) {
        TurbineRotorImmortality.normalize(stack);
    }

    public boolean isBarVisible(ItemStack stack) {
        TurbineRotorImmortality.normalize(stack);
        return false;
    }

    public boolean showFullBar(ItemStack stack) {
        TurbineRotorImmortality.normalize(stack);
        return false;
    }

    public float getDurabilityForDisplay(ItemStack stack) {
        TurbineRotorImmortality.normalize(stack);
        return 1;
    }
}
