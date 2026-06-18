package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.bee;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.AlvearyModifierPartMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import net.minecraft.core.Vec3i;

import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class AlvearyModifierComposite implements IBeeModifier {

    // Climate
    private final int heaterCount;
    private final int coolerCount;
    private final int humidifierCount;
    private final int dryerCount;

    // Production
    private final int productivityCount;
    private final int sieveCount;
    private final int weatherproofCount;
    private final int lightingCount;

    // Breeding
    private final int mutagenicCount;
    private final int accelerantCount;
    private final int longevityCount;
    private final int stabiliserCount;

    // Utility
    private final int territoryCount;
    private final int sealingCount;

    private AlvearyModifierComposite(int heaterCount, int coolerCount, int humidifierCount, int dryerCount,
                                     int productivityCount, int sieveCount, int weatherproofCount, int lightingCount,
                                     int mutagenicCount, int accelerantCount, int longevityCount, int stabiliserCount,
                                     int territoryCount, int sealingCount) {
        this.heaterCount = heaterCount;
        this.coolerCount = coolerCount;
        this.humidifierCount = humidifierCount;
        this.dryerCount = dryerCount;
        this.productivityCount = productivityCount;
        this.sieveCount = sieveCount;
        this.weatherproofCount = weatherproofCount;
        this.lightingCount = lightingCount;
        this.mutagenicCount = mutagenicCount;
        this.accelerantCount = accelerantCount;
        this.longevityCount = longevityCount;
        this.stabiliserCount = stabiliserCount;
        this.territoryCount = territoryCount;
        this.sealingCount = sealingCount;
    }

    public static AlvearyModifierComposite fromParts(Iterable<IMultiPart> parts) {
        int heater = 0, cooler = 0, humidifier = 0, dryer = 0;
        int productivity = 0, sieve = 0, weatherproof = 0, lighting = 0;
        int mutagenic = 0, accelerant = 0, longevity = 0, stabiliser = 0;
        int territory = 0, sealing = 0;

        for (IMultiPart part : parts) {
            if (!(part instanceof AlvearyModifierPartMachine modifier)) continue;

            switch (modifier.getModifierType()) {
                case HEATER -> heater++;
                case COOLER -> cooler++;
                case HUMIDIFIER -> humidifier++;
                case DRYER -> dryer++;
                case PRODUCTIVITY -> productivity++;
                case SIEVE -> sieve++;
                case WEATHERPROOF -> weatherproof++;
                case LIGHTING -> lighting++;
                case MUTAGENIC -> mutagenic++;
                case ACCELERANT -> accelerant++;
                case LONGEVITY -> longevity++;
                case STABILISER -> stabiliser++;
                case TERRITORY -> territory++;
                case SEALING -> sealing++;
            }
        }

        return new AlvearyModifierComposite(
                heater, cooler, humidifier, dryer,
                productivity, sieve, weatherproof, lighting,
                mutagenic, accelerant, longevity, stabiliser,
                territory, sealing);
    }

    public int getTemperatureOffset() {
        return heaterCount - coolerCount;
    }

    public int getHumidityOffset() {
        return humidifierCount - dryerCount;
    }

    public int getTotalModifierCount() {
        return heaterCount + coolerCount + humidifierCount + dryerCount + productivityCount + sieveCount +
                weatherproofCount + lightingCount + mutagenicCount + accelerantCount + longevityCount +
                stabiliserCount + territoryCount + sealingCount;
    }

    // IBeeModifier

    @Override
    public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
        return currentSpeed * (1.0f + 0.2f * productivityCount);
    }

    @Override
    public float modifyMutationChance(IGenome genome, IGenome mate, IMutation<IBeeSpecies> mutation,
                                      float currentChance) {
        if (stabiliserCount > 0) return 0f;
        float modified = currentChance * (1.0f + 0.15f * mutagenicCount);
        float cap = Math.min((float) (mutation.getChance() * Math.pow(1.5, 4)), 0.5f);
        return Math.min(modified, cap);
    }

    @Override
    public float modifyAging(IGenome genome, @Nullable IGenome mate, float currentAging) {
        float factor = 1.0f;
        if (accelerantCount > 0) factor += 0.5f * accelerantCount;
        if (longevityCount > 0) factor *= 1.0f / (1.0f + 0.3f * longevityCount);
        return currentAging * factor;
    }

    @Override
    public float modifyPollination(IGenome genome, float currentPollination) {
        return currentPollination * (1.0f + 0.5f * sieveCount);
    }

    @Override
    public float modifyGeneticDecay(IGenome genome, float currentDecay) {
        return currentDecay;
    }

    @Override
    public Vec3i modifyTerritory(IGenome genome, Vec3i currentModifier) {
        if (territoryCount <= 0) return currentModifier;
        float scale = 1.0f + 0.5f * territoryCount;
        return new Vec3i(
                Math.round(currentModifier.getX() * scale),
                currentModifier.getY(),
                Math.round(currentModifier.getZ() * scale));
    }

    @Override
    public boolean isSealed() {
        return weatherproofCount > 0;
    }

    @Override
    public boolean isAlwaysActive(IGenome genome) {
        return true;
    }

    @Override
    public boolean isSunlightSimulated() {
        return lightingCount > 0;
    }

    @Override
    public boolean isHellish() {
        return false; // Determined by climate state, not modifier directly
    }
}
