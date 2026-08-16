package com.ghostipedia.cosmiccore.common.power.steam;

public final class SteamBoilerRates {

    public static final int INTERVAL = 10;

    private SteamBoilerRates() {}

    public static int maximumOutputPerTick(BoilerType type, boolean highPressure) {
        return highPressure ? type.highPressureOutput() : type.lowPressureOutput();
    }

    public static int steamEquivalentPerTick(BoilerType type, boolean highPressure) {
        int output = maximumOutputPerTick(type, highPressure);
        return highPressure ? output * HPBoilerRates.COMPACT_RATE : output;
    }

    public static long outputForCycle(BoilerType type, boolean highPressure, int temperature,
                                      int maximumTemperature) {
        if (temperature < 100 || maximumTemperature <= 0) return 0;
        return (long) maximumOutputPerTick(type, highPressure) * INTERVAL * temperature /
                maximumTemperature;
    }

    public static int maximumLargeBoilerOutputPerTick(int maximumTemperature, int steamPerWater) {
        if (maximumTemperature <= 0 || steamPerWater <= 0) return 0;
        long waterPerCycle = (long) maximumTemperature * INTERVAL / steamPerWater;
        long ordinarySteamPerCycle = waterPerCycle * steamPerWater;
        return Math.toIntExact(ordinarySteamPerCycle / HPBoilerRates.COMPACT_RATE /
                INTERVAL);
    }

    public enum BoilerType {

        SOLAR(8, 2),
        SOLID(12, 3),
        LIQUID(16, 4);

        private final int lowPressureOutput;
        private final int highPressureOutput;

        BoilerType(int lowPressureOutput, int highPressureOutput) {
            this.lowPressureOutput = lowPressureOutput;
            this.highPressureOutput = highPressureOutput;
        }

        public int lowPressureOutput() {
            return lowPressureOutput;
        }

        public int highPressureOutput() {
            return highPressureOutput;
        }
    }
}
