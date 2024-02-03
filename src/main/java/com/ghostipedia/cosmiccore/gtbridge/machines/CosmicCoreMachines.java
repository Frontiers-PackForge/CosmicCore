package com.ghostipedia.cosmiccore.gtbridge.machines;

import com.ghostipedia.cosmiccore.Statics;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.machines.parts.PressureHatchPartMachine;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistries.COSMIC_REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.autoAbilities;

@SuppressWarnings("unused")
public class CosmicCoreMachines {

    public static final PartAbility PRESSURE_CONTAINER = new PartAbility("pressure_container");

    public final static int[] ELECTRIC_TIERS = GTCEuAPI.isHighTier() ?
            new int[] {GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV, GTValues.ZPM, GTValues.UV, GTValues.UHV, GTValues.UEV, GTValues.UIV, GTValues.UXV, GTValues.OpV} :
            new int[] {GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV, GTValues.ZPM, GTValues.UV};
    public final static int[] HIGH_TIERS = GTCEuAPI.isHighTier() ?
            new int[] {GTValues.IV, GTValues.LuV, GTValues.ZPM, GTValues.UV, GTValues.UHV, GTValues.UEV, GTValues.UIV, GTValues.UXV, GTValues.OpV} :
            new int[] {GTValues.IV, GTValues.LuV, GTValues.ZPM, GTValues.UV, GTValues.UHV};

    public static MachineDefinition[] registerPressureTieredMachines(String name,
                                                                     BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                     BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                                     int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[Statics.P.length];
        for (int tier : tiers) {
            var register = COSMIC_REGISTRATE.machine(Statics.PN[tier].toLowerCase(Locale.ROOT) + "_" + name, holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static TraceabilityPredicate pressurePredicate() {
        return abilities(PRESSURE_CONTAINER).setMaxGlobalLimited(1).setPreviewCount(1);
    }


    public static final MachineDefinition[] PRESSURE_HATCH = registerPressureTieredMachines("pressure_hatch", (holder, tier) -> {
                double min = Statics.P[Statics.UEV];
                double max = Statics.P[Statics.UEV];
                if (tier < Statics.UEV) min = Statics.P[tier];
                else if (tier > Statics.UEV) max = Statics.P[tier];

                tier = Math.abs(Statics.UEV - tier);
                return new PressureHatchPartMachine(holder, tier, min, max);
            }, (tier, builder) -> builder
                    .langValue("%s Hatch".formatted(Statics.PRESSURE_NAMES[tier]))
                    .abilities(PRESSURE_CONTAINER)
                    .rotationState(RotationState.ALL)
                    .overlayTieredHullRenderer("pressure_hatch")
                    .register(),
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            var register =  COSMIC_REGISTRATE.machine(GTValues.VN[tier].toLowerCase() + "_" + name, holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[i] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static final MultiblockMachineDefinition AIR_MACHINE = COSMIC_REGISTRATE.multiblock("airmachine", BasicAirMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            //.tooltips(Component.translatable("gtceu.multiblock.alternator.tooltip1"))
            .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
            //.recipeModifier(BasicSteamTurbineMachine::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("F###F", "ZWBWZ", "ZWBWZ", "ZWBWZ")
                    .aisle("#####", "ZWBWZ", "XOOOX", "ZWBWZ")
                    .aisle("F###F", "ZWBWZ", "ZWCWZ", "ZWBWZ")
                    .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('#', Predicates.any())
                    .where('W', Predicates.blocks(CosmicBlocks.ALTERNATOR_FLUX_COILING.get()))
                    .where('B', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('Z', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(abilities(PRESSURE_CONTAINER).setExactLimit(1)))
                    .where('X', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1)))
                    .where('O', Predicates.blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where('F', Predicates.frames(GTMaterials.Steel))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/generator/large_bronze_boiler"), false)
            .register();

    public static void init() {
    }
}