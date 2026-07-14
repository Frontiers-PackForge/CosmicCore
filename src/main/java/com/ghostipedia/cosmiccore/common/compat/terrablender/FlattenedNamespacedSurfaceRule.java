package com.ghostipedia.cosmiccore.common.compat.terrablender;

import com.ghostipedia.cosmiccore.mixin.accessor.SurfaceRulesContextAccessor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class FlattenedNamespacedSurfaceRule implements SurfaceRules.SurfaceRule {

    private final SurfaceRulesContextAccessor context;
    private final Map<String, SurfaceRules.SurfaceRule> rules;
    private final SurfaceRules.SurfaceRule fallback;
    private Holder<Biome> cachedBiome;
    private SurfaceRules.SurfaceRule cachedRule;

    public FlattenedNamespacedSurfaceRule(SurfaceRules.Context context,
                                          Map<String, SurfaceRules.SurfaceRule> rules,
                                          SurfaceRules.SurfaceRule fallback) {
        this.context = (SurfaceRulesContextAccessor) (Object) context;
        this.rules = rules;
        this.fallback = fallback;
    }

    @Override
    @Nullable
    public BlockState tryApply(int x, int y, int z) {
        Holder<Biome> biome = this.context.cosmiccore$getBiome().get();
        if (biome != this.cachedBiome) {
            this.cachedBiome = biome;
            ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);
            this.cachedRule = biomeKey == null ? null : this.rules.get(biomeKey.location().getNamespace());
        }
        if (this.cachedRule != null) {
            BlockState state = this.cachedRule.tryApply(x, y, z);
            if (state != null) {
                return state;
            }
        }
        return this.fallback.tryApply(x, y, z);
    }
}
