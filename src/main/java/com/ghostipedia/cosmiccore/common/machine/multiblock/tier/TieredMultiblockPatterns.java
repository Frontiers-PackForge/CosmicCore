package com.ghostipedia.cosmiccore.common.machine.multiblock.tier;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;

import com.google.common.base.Suppliers;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class TieredMultiblockPatterns {

    private static final Map<MultiblockMachineDefinition, List<Supplier<IBlockPattern>>> PATTERNS = new IdentityHashMap<>();
    private static final Map<MultiblockMachineDefinition, List<PatternLabel>> LABELS = new IdentityHashMap<>();

    private TieredMultiblockPatterns() {}

    @SafeVarargs
    public static void register(MultiblockMachineDefinition definition, Supplier<IBlockPattern>... extraTiers) {
        Supplier<IBlockPattern> defaultPattern = definition.getStructurePatterns().get("main");
        if (defaultPattern == null) {
            throw new IllegalArgumentException("Tiered multiblock requires a main pattern: " + definition.getId());
        }
        List<Supplier<IBlockPattern>> tiers = new ArrayList<>(extraTiers.length + 1);
        tiers.add(Suppliers.memoize(defaultPattern::get));
        for (Supplier<IBlockPattern> extraTier : extraTiers) {
            tiers.add(Suppliers.memoize(extraTier::get));
        }
        PATTERNS.put(definition, List.copyOf(tiers));
    }

    @SafeVarargs
    public static void registerConfigurations(MultiblockMachineDefinition definition, List<PatternLabel> labels,
                                              Supplier<IBlockPattern>... extraConfigurations) {
        register(definition, extraConfigurations);
        if (labels.size() != tierCount(definition)) {
            throw new IllegalArgumentException("Configuration labels do not match patterns: " + definition.getId());
        }
        LABELS.put(definition, List.copyOf(labels));
    }

    public static boolean isTiered(MultiblockMachineDefinition definition) {
        return tierCount(definition) > 1;
    }

    public static int tierCount(MultiblockMachineDefinition definition) {
        List<Supplier<IBlockPattern>> tiers = PATTERNS.get(definition);
        return tiers == null ? 1 : tiers.size();
    }

    public static int clampTier(MultiblockMachineDefinition definition, int tier) {
        return Math.clamp(tier, 0, tierCount(definition) - 1);
    }

    public static IBlockPattern pattern(MultiblockMachineDefinition definition, int tier) {
        List<Supplier<IBlockPattern>> tiers = PATTERNS.get(definition);
        if (tiers == null) {
            return definition.getStructurePatterns().get("main").get();
        }
        return tiers.get(clampTier(definition, tier)).get();
    }

    public static boolean hasConfigurationLabels(MultiblockMachineDefinition definition) {
        return LABELS.containsKey(definition);
    }

    public static PatternLabel label(MultiblockMachineDefinition definition, int tier) {
        List<PatternLabel> labels = LABELS.get(definition);
        if (labels == null) {
            throw new IllegalArgumentException("Multiblock has no configuration labels: " + definition.getId());
        }
        return labels.get(clampTier(definition, tier));
    }

    public record PatternLabel(String nameKey, String shortKey) {}
}
