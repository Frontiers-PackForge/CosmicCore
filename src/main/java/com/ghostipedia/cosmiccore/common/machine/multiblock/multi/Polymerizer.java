package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.BATCH_MODE;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class Polymerizer {

    public static final MultiblockMachineDefinition POLYMERIZER = REGISTRATE
            .multiblock("polymerizer", WorkableElectricMultiblockMachine::new)
            .langValue("§aPolymerizer")
            .recipeType(CosmicRecipeTypes.POLYMERIZER)
            .rotationState(RotationState.NON_Y_AXIS)
            .partAppearance((controller, part, side) -> CYCLOZINE_CHEMICALLY_REPELLING_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK), BATCH_MODE)
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice("X       X", "X       X", "AABBABBAA", "AABBABBAA", "AABBABBAA", "         ", "         ")
                    .slice("         ", "AABBABBAA", "AD#####DA", "ADD###DDA", "AD#####DA", "AABBABBAA", "         ")
                    .slice("AABBABBAA", "AD#####DA", "EF#####FE", "EFD###DFE", "EF#####FE", "AD#####DA", "AABBABBAA")
                    .slice("AABBABBAA", "ADD###DDA", "EFD###DFE", "EDDFFFDDE", "EFD###DFE", "ADD###DDA", "AABBABBAA")
                    .slice("AABBABBAA", "AD#####DA", "EF#####FE", "EFD###DFE", "EF#####FE", "AD#####DA", "AABBABBAA")
                    .slice("         ", "AABBABBAA", "AD#####DA", "ADD###DDA", "AD#####DA", "AABBABBAA", "         ")
                    .slice("X       X", "X       X", "AABBABBAA", "AABBQBBAA", "AABBABBAA", "         ", "         ")
                    .where('Q', Predicates.controller(Predicates.blocks(definition.get())))
                    .where(' ', Predicates.any())
                    .where('#', Predicates.air())
                    .where('A', blocks(HIGH_TOLERANCE_RHENIUM_CASING.get()))
                    .where('B', blocks(FUSION_GLASS.get()))
                    .where('D', blocks(RESONANTLY_TUNED_VIRTUE_MELD_CASING.get()))
                    .where('E', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.POLYMERIZER))
                            .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.INPUT_LASER).setMaxGlobalLimited(2, 2)
                                    .setPreviewCount(1)))
                    .where('F', blocks(GEARBOX_PTHANTERUM.get()))
                    .where('X', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)))
                    .build())
            .model(createSeparateControllerCasingMachineModel(
                    CosmicCore.id("block/casings/solid/high_tolerance_rhenium_casing"),
                    CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                    GTCEu.id("block/multiblock/assembly_line")))
            .register();

    public static void init() {}
}
