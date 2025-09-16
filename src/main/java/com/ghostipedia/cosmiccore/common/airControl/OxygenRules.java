package com.ghostipedia.cosmiccore.common.airControl;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class OxygenRules {
    private OxygenRules() {}


    public enum AirQuality {
        SAFE,
        THIN,
        TOXIC,
        ABYSS,
        NO_AIR
    }

    public static final class Rates {
        public int oxygenDrainPerTick;
        public double oxygenRecoveryPerTick;
        public float suffocationDamage;

        public Rates copy() {
            Rates rates = new Rates();
            rates.oxygenDrainPerTick = oxygenDrainPerTick;
            rates.oxygenRecoveryPerTick = oxygenRecoveryPerTick;
            rates.suffocationDamage = suffocationDamage;
            return rates;
        }
    }

    private static Rates rates(int drain, double regen, float dmg) {
        Rates result = new Rates();
        result.oxygenDrainPerTick = drain;
        result.oxygenRecoveryPerTick = regen;
        result.suffocationDamage = dmg;
        return result;
    }

    public static final Map<AirQuality, Rates> QUALITY_RATES = Map.of(
            AirQuality.SAFE,  rates(0, 2.0, 0f),
            AirQuality.THIN,  rates(1, 0.0, 0f),
            AirQuality.TOXIC, rates(1, 0.5, 0f),
            AirQuality.ABYSS, rates(8, 0.0, 1000f),
            AirQuality.NO_AIR,rates(2, 0.0, 0f)
    );


    //Air Ranges

    public static final class AirRanges {
        public final int minY;
        public final int maxY;
        public final AirQuality quality;

        // Use boxed types so null means "no override"
        public final Integer drainPertickOverride;     // null -> use QUALITY_RATES default
        public final Double  regenOverride;            // null -> use QUALITY_RATES default
        public final Float   damageOverride;           // null -> use QUALITY_RATES default

        public AirRanges(int minY, int maxY, AirQuality quality){
            this(minY, maxY, quality, null, null, null);
        }

        public AirRanges(int minY, int maxY, AirQuality quality,
                         Integer drainPertickOverride,
                         Double regenOverride,
                         Float damageOverride) {
            this.minY = minY;
            this.maxY = maxY;
            this.quality = quality;
            this.drainPertickOverride = drainPertickOverride;
            this.regenOverride = regenOverride;
            this.damageOverride = damageOverride;
        }

        public boolean presentAtY(int yValue){
            return yValue >= minY && yValue <= maxY;
        }

        public Rates airRangeRates() {
            Rates base = QUALITY_RATES.get(quality).copy();
            if (drainPertickOverride != null) base.oxygenDrainPerTick = drainPertickOverride;
            if (regenOverride != null)       base.oxygenRecoveryPerTick = regenOverride;
            if (damageOverride != null)      base.suffocationDamage = damageOverride;
            return base;
        }
    }

    private static final Map<ResourceKey<Level>, List<AirRanges>> RANGES = new ConcurrentHashMap<>();

    public static void addRanges(ResourceKey<Level> dimension, AirRanges... ranges){
        RANGES.computeIfAbsent(dimension, d -> new ArrayList<>()).addAll(Arrays.asList(ranges));
        RANGES.get(dimension).sort(Comparator.comparingInt(b -> b.minY));
    }

    public static AirRanges getRanges(ResourceKey<Level> dimension, int y){
        List<AirRanges> airRangesList = RANGES.get(dimension);
        if (airRangesList == null || airRangesList.isEmpty()) return null;
        for (AirRanges range : airRangesList) if (range.presentAtY(y)) return range;
        return null;
    }

    // Ranges

    public static void registerAirRanges() {
        addRanges(Level.OVERWORLD,
                // y ≤ 0 : NO_AIR
                new AirRanges(Integer.MIN_VALUE, 0,   AirQuality.NO_AIR, 2, 0.0, 2.0f),
                // 1 to 199 : SAFE (regen 3)
                new AirRanges(1, 199, AirQuality.SAFE, null, 3.0, null),
                // 200 to WorldLimit : THIN (slow drain; defaults apply)
                new AirRanges(200, Integer.MAX_VALUE, AirQuality.THIN)
        );
    }
}
