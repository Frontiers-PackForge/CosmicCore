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
        NO_AIR;
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

    private static Rates rates(int drain, double regen, float dmg){
        Rates rates = new Rates();
        rates.oxygenDrainPerTick = drain;
        rates.oxygenRecoveryPerTick = regen;
        rates.suffocationDamage = dmg;
        return rates;
    }

    public static final Map<AirQuality, Rates> QUALITY_RATES = Map.of(
            AirQuality.SAFE, rates(0,2,0),
            AirQuality.THIN, rates(1,0,0),
            AirQuality.TOXIC, rates(1,0.5,0),
            AirQuality.ABYSS, rates(8,0,1000),
            AirQuality.NO_AIR, rates(2,0,0)
    );


    //Air Ranges

    public static final class AirRanges {
        public final int minY;
        public final int maxY;
        public final AirQuality quality;

        public final int drainPertickOverride;
        public final double regenOverride;
        public final float damageOverride;


        public AirRanges(int minY, int maxY, AirQuality quality){
            this(minY, maxY, quality, 0, 0, 0);
        }

        public AirRanges(int minY, int maxY, AirQuality quality, int drainPertickOverride, double regenOverride, float damageOverride) {
            this.minY = minY;
            this.maxY = maxY;
            this.quality = quality;
            this.drainPertickOverride = drainPertickOverride;
            this.regenOverride = regenOverride;
            this.damageOverride = damageOverride;
        }

        public boolean presentAtY(int yValue){
            return minY < yValue && yValue <= maxY;
        }

        public Rates airRangeRates() {
            Rates base = QUALITY_RATES.get(quality).copy();
            if (drainPertickOverride == 0) base.oxygenDrainPerTick = drainPertickOverride;
            if (regenOverride == 0) base.oxygenRecoveryPerTick = regenOverride;
            if (damageOverride ==0 ) base.suffocationDamage = damageOverride;
            return base;
        }
    }

    private static final Map<ResourceKey<Level>, List<AirRanges>> RANGES = new ConcurrentHashMap<>();

    public static void addRanges(ResourceKey<Level> dimension, AirRanges... ranges){
        RANGES.computeIfAbsent(dimension, d -> new ArrayList<>()).addAll(Arrays.asList(ranges));
        RANGES.get(dimension).sort(Comparator.comparingInt(b -> b.minY));
    }

    public static AirRanges getRanges(ResourceKey<Level> dimension, int y){
        List<AirRanges> list = RANGES.get(dimension);
        if (list == null || list.isEmpty()) return null;
        for (AirRanges r : list) if (r.presentAtY(y)) return r;
        return null;
    }

    // Ranges and Dimensions


    public static void registerAirRanges() {
        addRanges(Level.OVERWORLD,
                //<0 - Thin : Air will deplete
                new AirRanges(Integer.MIN_VALUE, 0, AirQuality.NO_AIR, 2, 0.0, 2.0f),
                //1 to 199 - Safe : Regen Air
                new AirRanges(1, 199, AirQuality.SAFE, 0, 3.0,0),
                //200 to World Height. - Thin : Air will deplete
                new AirRanges(200, Integer.MAX_VALUE, AirQuality.THIN));
    }


}
