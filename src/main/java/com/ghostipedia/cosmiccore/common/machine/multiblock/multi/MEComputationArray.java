package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationArrayMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationArrayTuning;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;

import java.util.Arrays;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.ME_COMPUTATION_BAY_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.ability;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.frames;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.machines;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public final class MEComputationArray {

    public static final MultiblockMachineDefinition MACHINE = REGISTRATE
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
                    .where('D', blocks(ME_COMPUTATION_BAY_CASING.get())
                            .or(ability(
                                    CosmicPartAbility.ME_COMPUTATION_CORE,
                                    GTValues.tiersBetween(
                                            MEComputationArrayTuning.LOW_POWER_MINIMUM_COMPONENT_TIER.gtTier(),
                                            MEComputationArrayTuning.LOW_POWER_MAXIMUM_COMPONENT_TIER.gtTier())))
                            .or(ability(
                                    CosmicPartAbility.ME_POWER_RELAY,
                                    GTValues.tiersBetween(
                                            MEComputationArrayTuning.LOW_POWER_MINIMUM_COMPONENT_TIER.gtTier(),
                                            MEComputationArrayTuning.LOW_POWER_MAXIMUM_COMPONENT_TIER.gtTier()))))
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

    private MEComputationArray() {}

    private static MachineDefinition[] standardEnergyInputHatches() {
        return Arrays.stream(GTValues.tiersBetween(MEComputationArrayTuning.MINIMUM_ENERGY_HATCH_TIER, GTValues.MAX))
                .mapToObj(tier -> GTMachines.ENERGY_INPUT_HATCH[tier])
                .toArray(MachineDefinition[]::new);
    }

    public static void init() {}
}
