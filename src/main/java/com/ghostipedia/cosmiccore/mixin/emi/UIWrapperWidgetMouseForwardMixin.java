package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.EmbedMouseForwarder;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.screen.ModularScreen;
import com.google.common.cache.LoadingCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModularUIEmiRecipe.UIWrapperWidget.class, remap = false)
public class UIWrapperWidgetMouseForwardMixin implements EmbedMouseForwarder {

    @Unique
    private ModularScreen cosmiccore$screen;

    @Redirect(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
              at = @At(value = "INVOKE",
                       target = "Lcom/google/common/cache/LoadingCache;getUnchecked(Ljava/lang/Object;)Ljava/lang/Object;"),
              remap = false,
              require = 0)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object cosmiccore$captureScreen(LoadingCache cache, Object key) {
        Object screen = cache.getUnchecked(key);
        if (screen instanceof ModularScreen ms) {
            cosmiccore$screen = ms;
        }
        return screen;
    }

    @Override
    public boolean cosmiccore$mouseScrolled(double scrollX, double scrollY) {
        return cosmiccore$screen != null && cosmiccore$screen.mouseScrolled(scrollX, scrollY);
    }

    @Override
    public boolean cosmiccore$mouseDragged(int button, double dragX, double dragY) {
        return cosmiccore$screen != null && cosmiccore$screen.mouseDragged(button, dragX, dragY);
    }

    @Override
    public boolean cosmiccore$mouseReleased(int button) {
        return cosmiccore$screen != null && cosmiccore$screen.mouseReleased(button);
    }
}
