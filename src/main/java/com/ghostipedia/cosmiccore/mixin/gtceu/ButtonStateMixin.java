package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.gregtechceu.gtceu.integration.map.ButtonState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(value = ButtonState.class, remap = false)
public abstract class ButtonStateMixin {

    @Inject(method = "getAllButtons", at = @At("RETURN"), cancellable = true, remap = false)
    private static void cosmiccore$hideFluidButton(CallbackInfoReturnable<List<ButtonState.Button>> cir) {
        List<ButtonState.Button> buttons = cir.getReturnValue();
        if (buttons.stream().anyMatch(b -> b.name.equals("bedrock_fluids"))) {
            cir.setReturnValue(buttons.stream()
                    .filter(b -> !b.name.equals("bedrock_fluids"))
                    .collect(Collectors.toList()));
        }
    }
}
