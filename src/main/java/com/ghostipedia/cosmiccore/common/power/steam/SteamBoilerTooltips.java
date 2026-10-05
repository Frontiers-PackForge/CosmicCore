package com.ghostipedia.cosmiccore.common.power.steam;

import com.ghostipedia.cosmiccore.common.compat.gtceu.MachineDefinitionOverrides;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.Pair;

import java.util.function.BiConsumer;

public final class SteamBoilerTooltips {

    private SteamBoilerTooltips() {}

    public static void init() {
        replaceSmallBoilerTooltips(GTMachines.STEAM_SOLAR_BOILER, SteamBoilerRates.BoilerType.SOLAR);
        replaceSmallBoilerTooltips(GTMachines.STEAM_SOLID_BOILER, SteamBoilerRates.BoilerType.SOLID);
        replaceSmallBoilerTooltips(GTMachines.STEAM_LIQUID_BOILER, SteamBoilerRates.BoilerType.LIQUID);

        var config = ConfigHolder.INSTANCE.machines.largeBoilers;
        appendLargeBoilerTooltips(GTMultiMachines.LARGE_BOILER_BRONZE, config.bronzeBoilerMaxTemperature,
                config.steamPerWater);
        appendLargeBoilerTooltips(GTMultiMachines.LARGE_BOILER_STEEL, config.steelBoilerMaxTemperature,
                config.steamPerWater);
        appendLargeBoilerTooltips(GTMultiMachines.LARGE_BOILER_TITANIUM, config.titaniumBoilerMaxTemperature,
                config.steamPerWater);
        appendLargeBoilerTooltips(GTMultiMachines.LARGE_BOILER_TUNGSTENSTEEL,
                config.tungstensteelBoilerMaxTemperature, config.steamPerWater);
    }

    private static void replaceSmallBoilerTooltips(Pair<MachineEntry<MachineDefinition>, MachineEntry<MachineDefinition>> definitions,
                                                   SteamBoilerRates.BoilerType type) {
        MachineDefinitionOverrides.edit(definitions.first(), properties -> {
            properties.tooltips().clear();
            properties.tooltipBuilder((stack, tooltip) -> {
                tooltip.add(Component.translatable("cosmiccore.tooltip.steam_boiler.maximum_steam_output",
                        SteamBoilerRates.maximumOutputPerTick(type, false)));
                tooltip.add(Component.translatable("cosmiccore.tooltip.steam_boiler.temperature_scaling"));
            });
        });
        MachineDefinitionOverrides.edit(definitions.second(), properties -> {
            properties.tooltips().clear();
            properties.tooltipBuilder((stack, tooltip) -> {
                tooltip.add(Component.translatable("cosmiccore.tooltip.steam_boiler.maximum_pressurized_output",
                        SteamBoilerRates.maximumOutputPerTick(type, true)));
                tooltip.add(Component.translatable("cosmiccore.tooltip.steam_boiler.steam_equivalent",
                        SteamBoilerRates.steamEquivalentPerTick(type, true)));
                tooltip.add(Component.translatable("cosmiccore.tooltip.steam_boiler.temperature_scaling"));
            });
        });
    }

    private static void appendLargeBoilerTooltips(MachineEntry<?> definition, int maximumTemperature,
                                                  int steamPerWater) {
        int output = SteamBoilerRates.maximumLargeBoilerOutputPerTick(maximumTemperature, steamPerWater);
        MachineDefinitionOverrides.edit(definition, properties -> {
            BiConsumer<net.minecraft.world.item.ItemStack, java.util.List<Component>> existing = properties
                    .tooltipBuilder();
            properties.tooltipBuilder((stack, tooltip) -> {
                if (existing != null) existing.accept(stack, tooltip);
                tooltip.add(
                        Component.translatable("cosmiccore.tooltip.steam_boiler.maximum_pressurized_output", output));
                tooltip.add(Component.translatable("cosmiccore.tooltip.steam_boiler.steam_equivalent",
                        output * HPBoilerRates.COMPACT_RATE));
                tooltip.add(Component.translatable("cosmiccore.tooltip.large_boiler.output_scaling"));
            });
        });
    }
}
