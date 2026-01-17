package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.CosmicFavorite;

import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.screen.EmiScreenManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds CTRL+A to pin stacks with their amount, and CTRL+scroll to adjust pinned amounts.
 */
@Mixin(value = EmiScreenManager.class, remap = false)
public abstract class EmiScreenManagerMixin {

    @Shadow
    private static int lastMouseX;
    @Shadow
    private static int lastMouseY;

    @Shadow
    public static EmiStackInteraction getHoveredStack(int mouseX, int mouseY, boolean notClick) {
        throw new AssertionError();
    }

    @Shadow
    public static void repopulatePanels(SidebarType type) {
        throw new AssertionError();
    }

    // CTRL+A: pin with amount
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$ctrlAPin(int keyCode, int scanCode, int modifiers,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (!EmiInput.isControlDown() || EmiInput.isShiftDown()) return;
        if (!cosmiccore$isFavoriteKey(keyCode)) return;

        EmiStackInteraction hovered = getHoveredStack(lastMouseX, lastMouseY, true);
        if (hovered.getStack().isEmpty()) return;

        EmiIngredient stack = hovered.getStack();
        long amount = stack.getEmiStacks().isEmpty() ? 1 : stack.getEmiStacks().get(0).getAmount();
        if (amount <= 0) amount = 1;

        EmiFavorites.addFavorite(new CosmicFavorite(stack, amount), null);
        repopulatePanels(SidebarType.FAVORITES);
        cir.setReturnValue(true);
    }

    // CTRL+scroll: adjust amount on CosmicFavorites
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$ctrlScrollAdjust(double mouseX, double mouseY, double scrollDelta,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (!EmiInput.isControlDown()) return;

        EmiStackInteraction hovered = getHoveredStack((int) mouseX, (int) mouseY, true);
        if (!(hovered.getStack() instanceof CosmicFavorite fav)) return;

        long step = EmiInput.isShiftDown() ? cosmiccore$bigStep(fav) : 1;
        fav.adjustAmount((long) scrollDelta * step);

        repopulatePanels(SidebarType.FAVORITES);
        cir.setReturnValue(true);
    }

    @Unique
    private static boolean cosmiccore$isFavoriteKey(int keyCode) {
        return EmiConfig.favorite.boundKeys.stream()
                .anyMatch(k -> k.key().getValue() == keyCode);
    }

    @Unique
    private static long cosmiccore$bigStep(EmiIngredient stack) {
        if (stack.getEmiStacks().isEmpty()) return 64;
        EmiStack first = stack.getEmiStacks().get(0);
        return first.getKey() instanceof Fluid ? 1000 : 64;
    }
}
