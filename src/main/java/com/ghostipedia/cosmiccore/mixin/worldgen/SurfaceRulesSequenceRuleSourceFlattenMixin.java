package com.ghostipedia.cosmiccore.mixin.worldgen;

import com.ghostipedia.cosmiccore.mixin.accessor.SurfaceRulesSequenceRuleSourceAccessor;

import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$SequenceRuleSource")
public class SurfaceRulesSequenceRuleSourceFlattenMixin {

    @ModifyVariable(method = "<init>(Ljava/util/List;)V", at = @At("HEAD"), argsOnly = true)
    private static List<SurfaceRules.RuleSource> cosmiccore$flattenSources(List<SurfaceRules.RuleSource> sources) {
        ArrayList<SurfaceRules.RuleSource> flattened = null;
        for (int i = 0; i < sources.size(); i++) {
            SurfaceRules.RuleSource source = sources.get(i);
            if (source instanceof SurfaceRulesSequenceRuleSourceAccessor) {
                if (flattened == null) {
                    flattened = new ArrayList<>(sources.size());
                    flattened.addAll(sources.subList(0, i));
                }
                cosmiccore$appendSource(flattened, source);
            } else if (flattened != null) {
                flattened.add(source);
            }
        }
        return flattened == null ? sources : List.copyOf(flattened);
    }

    private static void cosmiccore$appendSource(List<SurfaceRules.RuleSource> flattened,
                                                SurfaceRules.RuleSource source) {
        if (source instanceof SurfaceRulesSequenceRuleSourceAccessor sequence) {
            for (SurfaceRules.RuleSource nested : sequence.cosmiccore$getSequence()) {
                cosmiccore$appendSource(flattened, nested);
            }
        } else {
            flattened.add(source);
        }
    }
}
