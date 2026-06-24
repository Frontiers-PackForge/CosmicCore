package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_SOUL;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.BLANK_RUNE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.HIGHLY_CONDUCTIVE_FISSION_CASING;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.BATCH_MODE;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;
import static com.klikli_dev.occultism.registry.OccultismBlocks.IESNIUM_BLOCK;

public class HellFireFoundry {

    public static final MultiblockMachineDefinition HELLFIRE_FOUNDRY = REGISTRATE
            .multiblock("hellfire_foundry", WorkableElectricMultiblockMachine::new)
            .langValue("§cHellfire Foundry")
            .recipeType(CosmicRecipeTypes.HELLFIRE_FOUNDRY)
            .rotationState(RotationState.NON_Y_AXIS)
            .partAppearance((controller, part, side) -> HIGHLY_CONDUCTIVE_FISSION_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK), BATCH_MODE)
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice("         ", "         ", " AAAAAAA ", "  AAAAA  ", "         ", "         ", "         ")
                    .slice(" AA   AA ", "         ", "ABBBBBBBA", " BBBBBBB ", " BB   BB ", " B     B ", " C     C ")
                    .slice(" A     A ", "  A   A  ", "ABBBBBBBA", "ABB   BBA", " B     B ", "         ", "         ")
                    .slice("         ", "         ", "ABBBBBBBA", "AB CCC BA", "         ", "         ", "         ")
                    .slice("         ", "         ", "ABBBBBBBA", "AB CXC BA", "         ", "         ", "         ")
                    .slice("         ", "         ", "ABBBBBBBA", "AB CCC BA", "         ", "         ", "         ")
                    .slice(" A     A ", "  A   A  ", "ABBBBBBBA", "ABB   BBA", " B     B ", "         ", "         ")
                    .slice(" AA   AA ", "         ", "ABBBBBBBA", " BBBBBBB ", " BB   BB ", " B     B ", " C     C ")
                    .slice("         ", "         ", " AAAAAAA ", "  AAQAA  ", "         ", "         ", "         ")
                    .where('Q', Predicates.controller(Predicates.blocks(definition.get())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(BLANK_RUNE.get()))
                    .where('B', blocks(HIGHLY_CONDUCTIVE_FISSION_CASING.get()).setMinGlobalLimited(70)
                            .or(autoAbilities(CosmicRecipeTypes.HELLFIRE_FOUNDRY))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('X', abilities(IMPORT_SOUL).setMinGlobalLimited(1, 1).setMaxGlobalLimited(1))
                    .where('C', blocks(IESNIUM_BLOCK.get()))
                    .build())
            .model(createSeparateControllerCasingMachineModel(
                    CosmicCore.id("block/casings/solid/soul_muted_casing"),
                    CosmicCore.id("block/casings/solid/highly_conductive_fission_casing"),
                    GTCEu.id("block/multiblock/network_switch"))
                    .andThen(model -> model
                            .addDynamicRenderer(CosmicDynamicRenderHelpers::createHellfireFoundryPartRender)))
            .register();

    public static void init() {}
}
