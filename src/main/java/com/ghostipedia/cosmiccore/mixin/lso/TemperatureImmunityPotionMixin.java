package com.ghostipedia.cosmiccore.mixin.lso;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sfiomn.legendarysurvivaloverhaul.registry.MobEffectRegistry;

@Mixin(MobEffectRegistry.class)
public abstract class TemperatureImmunityPotionMixin {

    @Redirect(method = "registerBrewingRecipes",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/alchemy/PotionBrewing$Builder;addMix(Lnet/minecraft/core/Holder;Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)V"))
    private static void cosmiccore$removeImmunityBrewingRecipes(PotionBrewing.Builder builder, Holder<Potion> input,
                                                                Item ingredient, Holder<Potion> result) {
        if (result == MobEffectRegistry.HEAT_IMMUNITY_POTION ||
                result == MobEffectRegistry.HEAT_IMMUNITY_POTION_LONG ||
                result == MobEffectRegistry.COLD_IMMUNITY_POTION ||
                result == MobEffectRegistry.COLD_IMMUNITY_POTION_LONG ||
                result == MobEffectRegistry.TEMPERATURE_IMMUNITY_POTION ||
                result == MobEffectRegistry.TEMPERATURE_IMMUNITY_POTION_LONG)
            return;
        builder.addMix(input, ingredient, result);
    }
}
