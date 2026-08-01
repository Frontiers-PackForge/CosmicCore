package com.ghostipedia.cosmiccore.mixin.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiTagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(value = EmiTags.class, remap = false)
public class EmiCircuitTagPreferenceMixin {

    private static final Set<String> COSMICCORE$OMNIA_TIERS = Set.of(
            "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev", "uiv", "uxv", "opv");

    @Inject(method = "getValues", at = @At("RETURN"), cancellable = true)
    private static <T> void cosmiccore$preferOmniaCircuit(EmiTagKey<T> tag,
                                                          CallbackInfoReturnable<List<EmiStack>> cir) {
        if (!tag.id().getNamespace().equals("gtceu") || !tag.id().getPath().startsWith("circuits/")) {
            return;
        }

        String tier = tag.id().getPath().substring("circuits/".length());
        if (!COSMICCORE$OMNIA_TIERS.contains(tier)) {
            return;
        }

        List<EmiStack> stacks = cir.getReturnValue();
        for (int index = 1; index < stacks.size(); index++) {
            if (stacks.get(index).getId().getNamespace().equals("cosmiccore") &&
                    stacks.get(index).getId().getPath().equals("omnia_circuit_" + tier)) {
                List<EmiStack> reordered = new ArrayList<>(stacks);
                reordered.addFirst(reordered.remove(index));
                cir.setReturnValue(List.copyOf(reordered));
                return;
            }
        }
    }
}
