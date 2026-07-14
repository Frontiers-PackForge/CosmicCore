package com.ghostipedia.cosmiccore.mixin.terrablender;

import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(targets = "terrablender.worldgen.surface.NamespacedSurfaceRuleSource", remap = false)
public interface NamespacedSurfaceRuleSourceAccessor {

    @Accessor("base")
    SurfaceRules.RuleSource cosmiccore$getBase();

    @Accessor("sources")
    Map<String, SurfaceRules.RuleSource> cosmiccore$getSources();
}
