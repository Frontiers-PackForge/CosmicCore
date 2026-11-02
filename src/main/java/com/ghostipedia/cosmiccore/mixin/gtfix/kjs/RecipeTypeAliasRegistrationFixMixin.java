package com.ghostipedia.cosmiccore.mixin.gtfix.kjs;

import com.ghostipedia.cosmiccore.common.compat.gtceu.kjs.RecipeTypeAliasRegistration;

import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameData.class, remap = false)
public abstract class RecipeTypeAliasRegistrationFixMixin {

    @Inject(method = "postRegisterEvents",
            at = @At(value = "INVOKE",
                     target = "Lnet/neoforged/fml/ModLoader;postEventWrapContainerInModOrder(Lnet/neoforged/bus/api/Event;)V",
                     shift = At.Shift.AFTER),
            require = 1,
            allow = 1)
    private static void cosmiccore$registerRecipeTypeAlias(CallbackInfo ci, @Local RegisterEvent event) {
        RecipeTypeAliasRegistration.afterRegistration(event);
    }
}
