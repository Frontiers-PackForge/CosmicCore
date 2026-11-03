package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationArrayMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationArrayTuning;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationComponentTier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;

import java.util.Arrays;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.ME_COMPUTATION_BAY_CASING;
import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.Lumium;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.ability;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.frames;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.machines;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_NONCONDUCTING;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_PALLADIUM_SUBSTATION;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public final class MEComputationArray {

    public static final MachineEntry<MultiblockMachineDefinition> LOW_POWER_COMPUTATION_ARRAY = REGISTRATE
            .multiblock("me_computation_array", MEComputationArrayMachine::new)
            .langValue("Low-Power Computation Array")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(LIGHTWEIGHT_DARK_STEEL_CASING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .pattern(definition -> MultiblockPatternBuilder.start(
                    RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAAAA", "AA AA", "A   A", "     ", "A   A", "AA AA", "AAAAA")
                    .slice("AAAAA", "AB BA", " B B ", " B B ", " B B ", "AB BA", "AAAAA")
                    .slice("AAAAA", "  D  ", "  D  ", "  D  ", "  D  ", "  D  ", "AAAAA")
                    .slice("AAAAA", "AB BA", " B B ", " B B ", " B B ", "AB BA", "AAAAA")
                    .slice("AACAA", "AA AA", "A   A", "     ", "A   A", "AA AA", "AAAAA")
                    .where(' ', any())
                    .where('A', blocks(LIGHTWEIGHT_DARK_STEEL_CASING.get())
                            .or(machines(standardEnergyInputHatches())
                                    .setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(abilities(CosmicPartAbility.ME_COMPUTATION_UPLINK).setExactLimit(1)))
                    .where('B', frames(GTMaterials.Steel))
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('D', componentBays(MEComputationArrayTuning.LOW_POWER_MAXIMUM_COMPONENT_TIER))
                    .build())
            .tooltips(
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.0",
                            MEComputationArrayTuning.COMPONENT_POSITIONS),
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.1",
                            GTValues.VN[MEComputationArrayTuning.LOW_POWER_MINIMUM_COMPONENT_TIER.gtTier()],
                            GTValues.VN[MEComputationArrayTuning.LOW_POWER_MAXIMUM_COMPONENT_TIER.gtTier()]),
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.2"),
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.3"),
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.4"))
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/lightweight_dark_steel_casing"),
                    GTCEu.id("block/multiblock/hpca"))
                    .andThen(model -> model.addDynamicRenderer(
                            CosmicDynamicRenderHelpers::getMEComputationArrayRender)))
            .hasBER(true)
            .register();

    public static final MachineEntry<MultiblockMachineDefinition> LARGE_COMPUTATION_ARRAY = REGISTRATE
            .multiblock("large_computation_array", info -> new MEComputationArrayMachine(info, true))
            .langValue("Large Computation Array")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(CASING_PALLADIUM_SUBSTATION)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .pattern(definition -> MultiblockPatternBuilder.start(
                    RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAAEAAA", "AA   AA", "A     A", "       ", "A     A", "AA   AA", "AAAAAAA")
                    .slice("AAAAAAA", "AB   BA", " B   B ", " B   B ", " B   B ", "AB   BA", "AAAAAAA")
                    .slice("AAAAAAA", "  CCC  ", "  DDD  ", "  CCC  ", "  DDD  ", "  CCC  ", "AAAAAAA")
                    .slice("AAAAAAA", "AB   BA", " B   B ", " B   B ", " B   B ", "AB   BA", "AAAAAAA")
                    .slice("AAAAAAA", "AA   AA", "A     A", "       ", "A     A", "AA   AA", "AAAAAAA")
                    .where(' ', any())
                    .where('A', blocks(CASING_PALLADIUM_SUBSTATION.get())
                            .or(machines(standardEnergyInputHatches())
                                    .setMinGlobalLimited(1).setMaxGlobalLimited(2))
                            .or(abilities(CosmicPartAbility.ME_COMPUTATION_UPLINK).setExactLimit(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1)))
                    .where('B', frames(Lumium))
                    .where('C', componentBays(MEComputationArrayTuning.LARGE_MAXIMUM_COMPONENT_TIER))
                    .where('D', blocks(CASING_NONCONDUCTING.get()))
                    .where('E', controller(blocks(definition.getBlock())))
                    .build())
            .tooltips(
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.0",
                            MEComputationArrayTuning.LARGE_COMPONENT_POSITIONS),
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.1",
                            GTValues.VN[MEComputationArrayTuning.LOW_POWER_MINIMUM_COMPONENT_TIER.gtTier()],
                            GTValues.VN[MEComputationArrayTuning.LARGE_MAXIMUM_COMPONENT_TIER.gtTier()]),
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.2"),
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.3"),
                    Component.translatable("cosmiccore.machine.me_computation_array.tooltip.4"),
                    Component.translatable("cosmiccore.machine.large_computation_array.tooltip.coolant"),
                    Component.translatable("cosmiccore.machine.large_computation_array.tooltip.consumption"))
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/solid/machine_casing_palladium_substation"),
                    GTCEu.id("block/multiblock/hpca"))
                    .andThen(model -> model.addDynamicRenderer(
                            CosmicDynamicRenderHelpers::getMEComputationArrayRender)))
            .hasBER(true)
            .register();

    private MEComputationArray() {}

    private static MultiPredicate componentBays(MEComputationComponentTier maximumTier) {
        int[] tiers = GTValues.tiersBetween(
                MEComputationArrayTuning.LOW_POWER_MINIMUM_COMPONENT_TIER.gtTier(), maximumTier.gtTier());
        return blocks(ME_COMPUTATION_BAY_CASING.get())
                .or(ability(CosmicPartAbility.ME_COMPUTATION_CORE, tiers))
                .or(ability(CosmicPartAbility.ME_POWER_RELAY, tiers));
    }

    private static MachineDefinition[] standardEnergyInputHatches() {
        return Arrays.stream(GTValues.tiersBetween(MEComputationArrayTuning.MINIMUM_ENERGY_HATCH_TIER, GTValues.MAX))
                .mapToObj(tier -> GTMachines.ENERGY_INPUT_HATCH[tier].get())
                .toArray(MachineDefinition[]::new);
    }

    public static void init() {}
}
