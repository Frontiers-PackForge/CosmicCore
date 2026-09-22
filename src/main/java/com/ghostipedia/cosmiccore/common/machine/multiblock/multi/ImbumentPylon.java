package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ImbumentPylonMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.BLOODSTEEL_KUVITE_PLATING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.REFRACTORY_STRUCTURAL_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public final class ImbumentPylon {

    public static final MultiblockMachineDefinition IMBUMENT_PYLON = REGISTRATE
            .multiblock("imbument_pylon", ImbumentPylonMachine::new)
            .langValue("Imbument Pylon")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(REFRACTORY_STRUCTURAL_CASING)
            .recipeType(CosmicRecipeTypes.IMBUMENT_PYLON)
            .partAppearance((controller, part, side) -> REFRACTORY_STRUCTURAL_CASING.getDefaultState())
            .recipeModifiers(ImbumentPylonMachine::recipeModifier,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice(" AAA ", " ACA ", "  A  ", "  A  ", "     ", "     ", "     ", "     ", "     ")
                    .slice("AAAAA", "ABBBA", " BBB ", " BBB ", " BBB ", " BBB ", "  B  ", "     ", "     ")
                    .slice("AAAAA", "ABBBA", "ABBBA", "ABBBA", " BBB ", " BBB ", " BBB ", "  B  ", "  B  ")
                    .slice("AAAAA", "ABBBA", " BBB ", " BBB ", " BBB ", " BBB ", "  B  ", "     ", "     ")
                    .slice(" AAA ", " AAA ", "  A  ", "  A  ", "     ", "     ", "     ", "     ", "     ")
                    .where(' ', any())
                    .where('A', blocks(REFRACTORY_STRUCTURAL_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1)))
                    .where('B', blocks(BLOODSTEEL_KUVITE_PLATING.get()))
                    .where('C', controller(blocks(definition.getBlock())))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/refractory_structural_casing"),
                    GTCEu.id("block/multiblock/network_switch")))
            .register();

    private ImbumentPylon() {}

    public static void init() {}
}
