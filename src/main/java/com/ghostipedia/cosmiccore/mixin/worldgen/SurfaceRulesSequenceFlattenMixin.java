package com.ghostipedia.cosmiccore.mixin.worldgen;

import com.ghostipedia.cosmiccore.mixin.accessor.SurfaceRulesSequenceRuleAccessor;

import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$SequenceRule")
public class SurfaceRulesSequenceFlattenMixin {

    @ModifyVariable(method = "<init>(Ljava/util/List;)V", at = @At("HEAD"), argsOnly = true)
    private static List<SurfaceRules.SurfaceRule> cosmiccore$flattenRules(List<SurfaceRules.SurfaceRule> rules) {
        ArrayList<SurfaceRules.SurfaceRule> flattened = null;
        for (int i = 0; i < rules.size(); i++) {
            SurfaceRules.SurfaceRule rule = rules.get(i);
            if (rule instanceof SurfaceRulesSequenceRuleAccessor) {
                if (flattened == null) {
                    flattened = new ArrayList<>(rules.size());
                    flattened.addAll(rules.subList(0, i));
                }
                cosmiccore$appendRule(flattened, rule);
            } else if (flattened != null) {
                flattened.add(rule);
            }
        }
        return flattened == null ? rules : List.copyOf(flattened);
    }

    private static void cosmiccore$appendRule(List<SurfaceRules.SurfaceRule> flattened,
                                              SurfaceRules.SurfaceRule rule) {
        if (rule instanceof SurfaceRulesSequenceRuleAccessor sequence) {
            for (SurfaceRules.SurfaceRule nested : sequence.cosmiccore$getRules()) {
                cosmiccore$appendRule(flattened, nested);
            }
        } else {
            flattened.add(rule);
        }
    }
}
