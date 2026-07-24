package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EmiScreenManager.ScreenSpace.class, remap = false)
public abstract class ScreenSpaceMixin {

    @Shadow
    @Final
    public int pageSize;

    @Shadow
    @Final
    public int[] widths;

    @Shadow
    @Final
    public boolean search;

    @Shadow
    public abstract SidebarType getType();

    @Inject(method = "getStacks", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$getRecipeProjection(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (manager.isReady() && !search && getType() == SidebarType.FAVORITES &&
                manager.getActiveGroup().isRecipeGroup()) {
            cir.setReturnValue(manager.getRecipeProjection(this, pageSize, widths).stacks());
        }
    }
}
