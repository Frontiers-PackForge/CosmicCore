package com.ghostipedia.cosmiccore.mixin.accessor;

import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$SequenceRule")
public interface SurfaceRulesSequenceRuleAccessor {

    @Accessor("rules")
    List<SurfaceRules.SurfaceRule> cosmiccore$getRules();
}
