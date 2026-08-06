package com.ghostipedia.cosmiccore.mixin.terrablender;

import com.ghostipedia.cosmiccore.common.compat.terrablender.FlattenedNamespacedSurfaceRule;

import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(targets = "terrablender.worldgen.surface.NamespacedSurfaceRuleSource", priority = 2000, remap = false)
public abstract class NamespacedSurfaceRuleSourceFlattenMixin {

    @Shadow
    @Final
    private SurfaceRules.RuleSource base;

    @Shadow
    @Final
    private Map<String, SurfaceRules.RuleSource> sources;

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static SurfaceRules.RuleSource cosmiccore$flattenSerializedBase(SurfaceRules.RuleSource base) {
        while (base instanceof NamespacedSurfaceRuleSourceAccessor namespaced) {
            base = namespaced.cosmiccore$getBase();
        }
        return base;
    }

    @Inject(
            method = "apply(Lnet/minecraft/world/level/levelgen/SurfaceRules$Context;)Lnet/minecraft/world/level/levelgen/SurfaceRules$SurfaceRule;",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$flattenNestedSources(SurfaceRules.Context context,
                                                 CallbackInfoReturnable<SurfaceRules.SurfaceRule> cir) {
        if (!(this.base instanceof NamespacedSurfaceRuleSourceAccessor)) {
            return;
        }

        Map<String, List<SurfaceRules.RuleSource>> flattenedSources = new HashMap<>();
        cosmiccore$appendSources(flattenedSources, this.sources);

        SurfaceRules.RuleSource flattenedBase = this.base;
        while (flattenedBase instanceof NamespacedSurfaceRuleSourceAccessor namespaced) {
            cosmiccore$appendSources(flattenedSources, namespaced.cosmiccore$getSources());
            flattenedBase = namespaced.cosmiccore$getBase();
        }

        Map<String, SurfaceRules.SurfaceRule> compiledSources = new HashMap<>(flattenedSources.size());
        flattenedSources
                .forEach((namespace, rules) -> compiledSources.put(namespace, cosmiccore$compile(rules, context)));
        cir.setReturnValue(new FlattenedNamespacedSurfaceRule(context, compiledSources, flattenedBase.apply(context)));
    }

    private static void cosmiccore$appendSources(Map<String, List<SurfaceRules.RuleSource>> flattened,
                                                 Map<String, SurfaceRules.RuleSource> sources) {
        sources.forEach(
                (namespace, source) -> flattened.computeIfAbsent(namespace, key -> new ArrayList<>()).add(source));
    }

    private static SurfaceRules.SurfaceRule cosmiccore$compile(List<SurfaceRules.RuleSource> sources,
                                                               SurfaceRules.Context context) {
        if (sources.size() == 1) {
            return sources.getFirst().apply(context);
        }
        return SurfaceRules.sequence(sources.toArray(SurfaceRules.RuleSource[]::new)).apply(context);
    }
}
