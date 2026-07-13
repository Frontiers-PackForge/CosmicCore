package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.recipe.GTRecipeCategoryLifecycle;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.RecipeManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientRecipeCategorySyncFixMixin {

    @Shadow
    public abstract RecipeManager getRecipeManager();

    @Inject(method = "handleUpdateRecipes",
            at = @At(value = "INVOKE",
                     target = "Lnet/neoforged/neoforge/client/ClientHooks;onRecipesUpdated(Lnet/minecraft/world/item/crafting/RecipeManager;)V",
                     shift = At.Shift.BEFORE))
    private void cosmiccore$rebuildGtRecipeCategories(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        GTRecipeCategoryLifecycle.rebuild(getRecipeManager().getRecipes());
    }
}
