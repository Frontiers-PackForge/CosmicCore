package com.ghostipedia.cosmiccore.common.airControl;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
            AirQuality.SAFE, rates(0, 2.0, 5f),
            AirQuality.THIN, rates(1, 0.0, 5f),
            AirQuality.TOXIC, rates(1, 0.5, 5f),
            AirQuality.ABYSS, rates(8, 0.0, 1000f),
            AirQuality.NO_AIR, rates(2, 0.0, 5f));

    // Air Ranges

    public static final class AirRanges {

        public final int minY;
        public final int maxY;
        public final AirQuality quality;

        public final Integer drainPerTickOverride;
        public final Double regenOverride;
        public final Float damageOverride;

        public AirRanges(int minY, int maxY, AirQuality quality) {
            this(minY, maxY, quality, null, null, null);
        }

        public AirRanges(int minY, int maxY, AirQuality quality,
                         Integer drainPerTickOverride,
                         Double regenOverride,
                         Float damageOverride) {
            this.minY = minY;
            this.maxY = maxY;
            this.quality = quality;
            this.drainPerTickOverride = drainPerTickOverride;
            this.regenOverride = regenOverride;
            this.damageOverride = damageOverride;
        }

        public boolean presentAtY(int yValue) {
            return yValue >= minY && yValue <= maxY;
        }

        public Rates airRangeRates() {
            Rates base = QUALITY_RATES.get(quality).copy();
            if (drainPerTickOverride != null) base.oxygenDrainPerTick = drainPerTickOverride;
            if (regenOverride != null) base.oxygenRecoveryPerTick = regenOverride;
            if (damageOverride != null) base.suffocationDamage = damageOverride;
            return base;
        }
    }

    private static final Map<ResourceKey<Level>, List<AirRanges>> RANGES = new ConcurrentHashMap<>();

    public static void addRanges(ResourceKey<Level> dimension, AirRanges... ranges) {
        RANGES.computeIfAbsent(dimension, d -> new ArrayList<>()).addAll(Arrays.asList(ranges));
        RANGES.get(dimension).sort(Comparator.comparingInt(b -> b.minY));
    }

    public static AirRanges getRanges(ResourceKey<Level> dimension, int y) {
        List<AirRanges> airRangesList = RANGES.get(dimension);
        if (airRangesList == null || airRangesList.isEmpty()) return null;
        for (AirRanges range : airRangesList) {
            if (range.presentAtY(y)) return range;
        }
        return null;
    }

    // --- Ad Astra dimension keys ---
    private static ResourceKey<Level> adAstraDim(String name) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation("ad_astra", name));
    }

    public static final ResourceKey<Level> MOON = adAstraDim("moon");
    public static final ResourceKey<Level> MOON_ORBIT = adAstraDim("moon_orbit");
    public static final ResourceKey<Level> MARS = adAstraDim("mars");
    public static final ResourceKey<Level> MARS_ORBIT = adAstraDim("mars_orbit");
    public static final ResourceKey<Level> VENUS = adAstraDim("venus");
    public static final ResourceKey<Level> VENUS_ORBIT = adAstraDim("venus_orbit");
    public static final ResourceKey<Level> MERCURY = adAstraDim("mercury");
    public static final ResourceKey<Level> MERCURY_ORBIT = adAstraDim("mercury_orbit");
    public static final ResourceKey<Level> GLACIO = adAstraDim("glacio");
    public static final ResourceKey<Level> GLACIO_ORBIT = adAstraDim("glacio_orbit");
    public static final ResourceKey<Level> EARTH_ORBIT = adAstraDim("earth_orbit");

    // Default range registration
    public static void registerAirRanges() {
        // --- Overworld ---
        addRanges(Level.OVERWORLD,
                // y ≤ 0 : THIN air underground
                new AirRanges(Integer.MIN_VALUE, 0, AirQuality.THIN, 1, 0.0, 2.0f),
                // 1 to 199 : SAFE (faster regen)
                new AirRanges(1, 199, AirQuality.SAFE, null, 3.0, null),
                // 200+ : THIN at high altitude
                new AirRanges(200, Integer.MAX_VALUE, AirQuality.THIN));

        // --- Space (no atmosphere) ---
        // All orbit dimensions have no air at all Y levels
        for (ResourceKey<Level> orbit : List.of(EARTH_ORBIT, MOON_ORBIT, MARS_ORBIT, VENUS_ORBIT, MERCURY_ORBIT,
                GLACIO_ORBIT)) {
            addRanges(orbit, new AirRanges(Integer.MIN_VALUE, Integer.MAX_VALUE, AirQuality.NO_AIR));
        }

        // --- Planetary surfaces (no atmosphere) ---
        // Moon, Mars, Mercury - no atmosphere
        for (ResourceKey<Level> airless : List.of(MOON, MARS, MERCURY)) {
            addRanges(airless, new AirRanges(Integer.MIN_VALUE, Integer.MAX_VALUE, AirQuality.NO_AIR));
        }

        // Venus - toxic atmosphere
        addRanges(VENUS, new AirRanges(Integer.MIN_VALUE, Integer.MAX_VALUE, AirQuality.TOXIC, 2, 0.0, 3.0f));

        // Glacio - thin but breathable at surface (ice world with some atmosphere)
        addRanges(GLACIO,
                new AirRanges(Integer.MIN_VALUE, 0, AirQuality.THIN, 1, 0.0, 2.0f),
                new AirRanges(1, 127, AirQuality.SAFE, null, 1.5, null),
                new AirRanges(128, Integer.MAX_VALUE, AirQuality.THIN));
    }

    public static final class ResolvedAirRange {

        public final AirQuality airQuality;
        public final Rates rates;

        public ResolvedAirRange(AirQuality quality, Rates rates) {
            this.airQuality = quality;
            this.rates = rates;
        }
    }

    public static ResolvedAirRange resolve(ResourceKey<Level> dimension, int yVal) {
        AirRanges range = getRanges(dimension, yVal);
        if (range == null) {
            Rates safe = QUALITY_RATES.get(AirQuality.SAFE).copy();
            return new ResolvedAirRange(AirQuality.SAFE, safe);
        }
        return new ResolvedAirRange(range.quality, range.airRangeRates());
    }
}
