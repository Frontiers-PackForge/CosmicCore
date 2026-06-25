package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;

import net.minecraft.network.RegistryFriendlyByteBuf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GTRecipeSerializer.class, remap = false)
public class GTRecipeSerializerNullSyncFixMixin {

    @Inject(method = "toNetwork", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$writeRecipePresence(RegistryFriendlyByteBuf buf, GTRecipe recipe, CallbackInfo ci) {
        buf.writeBoolean(recipe != null);
        if (recipe == null) {
            ci.cancel();
        }
    }

    @Inject(method = "fromNetwork", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$readRecipePresence(RegistryFriendlyByteBuf buf,
                                                      CallbackInfoReturnable<GTRecipe> cir) {
        if (!buf.readBoolean()) {
            cir.setReturnValue(null);
        }
    }
}
