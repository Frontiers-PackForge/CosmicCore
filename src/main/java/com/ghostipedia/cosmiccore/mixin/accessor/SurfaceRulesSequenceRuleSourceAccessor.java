package com.ghostipedia.cosmiccore.mixin.accessor;

import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$SequenceRuleSource")
public interface SurfaceRulesSequenceRuleSourceAccessor {

    @Accessor("sequence")
    List<SurfaceRules.RuleSource> cosmiccore$getSequence();
}
