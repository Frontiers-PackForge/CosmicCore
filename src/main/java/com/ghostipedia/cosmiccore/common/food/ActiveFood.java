package com.ghostipedia.cosmiccore.common.food;

import com.ghostipedia.cosmiccore.common.compat.qualityfood.QualityFoodCompat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ActiveFood {

    public static final double MAX_SERVINGS = 2.0;
    private static final double EPSILON = 1.0E-9;

    public final Item item;
    public final FoodDefinition def;
    private final double[] qualityReserves = new double[4];

    public ActiveFood(Item item) {
        this.item = item;
        this.def = CosmicFoodRegistry.get(new ItemStack(item));
    }

    public ActiveFood(Item item, int ticksLeft, int quality) {
        this(item);
        int tier = Math.clamp(quality, 0, 3);
        int duration = durationFor(tier);
        qualityReserves[tier] = Math.clamp((double) ticksLeft / duration, 0.0, MAX_SERVINGS);
    }

    public void addServing(int quality) {
        int tier = Math.clamp(quality, 0, 3);
        double room = MAX_SERVINGS - totalServings();
        double added = Math.min(1.0, Math.max(room, 0.0));
        double replacement = 1.0 - added;
        for (int lowerTier = 0; lowerTier < tier && replacement > EPSILON; lowerTier++) {
            double removed = Math.min(replacement, qualityReserves[lowerTier]);
            qualityReserves[lowerTier] -= removed;
            replacement -= removed;
            added += removed;
        }
        qualityReserves[tier] += added;
    }

    public boolean tick(int ticks) {
        int previousQuality = quality();
        double remaining = ticks;
        for (int tier = qualityReserves.length - 1; tier >= 0 && remaining > EPSILON; tier--) {
            double availableTicks = qualityReserves[tier] * durationFor(tier);
            double consumedTicks = Math.min(remaining, availableTicks);
            qualityReserves[tier] -= consumedTicks / durationFor(tier);
            if (qualityReserves[tier] < EPSILON) qualityReserves[tier] = 0.0;
            remaining -= consumedTicks;
        }
        return previousQuality != quality();
    }

    public int ticksLeft() {
        double ticks = 0.0;
        for (int tier = 0; tier < qualityReserves.length; tier++) {
            ticks += qualityReserves[tier] * durationFor(tier);
        }
        return (int) Math.ceil(ticks - EPSILON);
    }

    public int quality() {
        for (int tier = qualityReserves.length - 1; tier >= 0; tier--) {
            if (qualityReserves[tier] > EPSILON) return tier;
        }
        return 0;
    }

    public int baseDuration() {
        return durationFor(quality());
    }

    public boolean isExpired() {
        return totalServings() <= EPSILON;
    }

    public double[] qualityReserves() {
        return qualityReserves.clone();
    }

    public void restoreQualityReserves(double[] reserves) {
        for (int tier = qualityReserves.length - 1; tier >= 0; tier--) {
            double room = MAX_SERVINGS - totalServings();
            if (room <= EPSILON) return;
            double reserve = Double.isFinite(reserves[tier]) ? Math.max(reserves[tier], 0.0) : 0.0;
            qualityReserves[tier] = Math.min(reserve, room);
        }
    }

    private double totalServings() {
        double total = 0.0;
        for (double reserve : qualityReserves) total += reserve;
        return total;
    }

    private int durationFor(int quality) {
        return QualityFoodCompat.scaleDuration(def.durationTicks(), quality);
    }
}
