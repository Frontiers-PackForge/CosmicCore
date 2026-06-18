package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.bee;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import forestry.api.IForestryApi;
import forestry.api.climate.IClimateManager;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import lombok.Getter;

@Getter
public class AlvearyClimateState {

    private final TemperatureType baseTemperature;
    private final HumidityType baseHumidity;
    private final TemperatureType effectiveTemperature;
    private final HumidityType effectiveHumidity;

    private AlvearyClimateState(TemperatureType baseTemp, HumidityType baseHumid,
                                TemperatureType effectiveTemp, HumidityType effectiveHumid) {
        this.baseTemperature = baseTemp;
        this.baseHumidity = baseHumid;
        this.effectiveTemperature = effectiveTemp;
        this.effectiveHumidity = effectiveHumid;
    }

    public static AlvearyClimateState create(Level level, BlockPos controllerPos,
                                             AlvearyModifierComposite modifiers) {
        IClimateManager climateManager = IForestryApi.INSTANCE.getClimateManager();
        IClimateProvider provider = climateManager.createClimateProvider(level, controllerPos);

        TemperatureType baseTemp = provider.temperature();
        HumidityType baseHumid = provider.humidity();

        int tempOffset = modifiers.getTemperatureOffset();
        TemperatureType effectiveTemp;
        if (tempOffset >= 0) {
            effectiveTemp = baseTemp.up(tempOffset);
        } else {
            effectiveTemp = baseTemp.down(-tempOffset);
        }

        int humidOffset = modifiers.getHumidityOffset();
        HumidityType effectiveHumid;
        if (humidOffset >= 0) {
            effectiveHumid = baseHumid.up(humidOffset);
        } else {
            effectiveHumid = baseHumid.down(-humidOffset);
        }

        return new AlvearyClimateState(baseTemp, baseHumid, effectiveTemp, effectiveHumid);
    }
}
