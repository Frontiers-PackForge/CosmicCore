package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.GameData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = GameData.class, remap = false)
public abstract class RecipeTypeRegistrationOrderFixMixin {

    @Inject(method = "getRegistrationOrder", at = @At("RETURN"), require = 1, allow = 1)
    private static void cosmiccore$removeSyntheticRecipeTypeEvent(
                                                                  CallbackInfoReturnable<Set<ResourceLocation>> cir) {
        cir.getReturnValue().remove(GTRegistries.Keys.RECIPE_TYPE.location());
    }
}
