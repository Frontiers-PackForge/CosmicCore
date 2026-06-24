package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
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

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;

public class CryogenicsChamber {

    public final static MultiblockMachineDefinition CRYOGENICS_CHAMBER = REGISTRATE
            .multiblock("cryogenics_chamber",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§bCryogenics Chamber")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.CRYOGENICS_CHAMBER)
            .hasBER(true)
            .partAppearance((controller, part, side) -> CRYOGENIC_CASING.getDefaultState())
            .appearanceBlock(HEAVY_FROST_PROOF_CASING)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start()

                    .slice("   AAAAAAA   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                    .slice("  AAAAAAAAA  ", "    A A A    ", "    A A A    ", "    A A A    ", "    A A A    ", "             ", "             ", "             ")
                    .slice(" AABBBBBBBAA ", "  ABBBBBBBA  ", "  ABBBBBBBA  ", "  ABBBBBBBA  ", "  A A A A A  ", "             ", "             ", "             ")
                    .slice("AABBBBBBBBBAA", "  BBBBBBBBB  ", "  BBBBBBBBB  ", "    A A A    ", "             ", "             ", "             ", "             ")
                    .slice("AABBBBBBBBBAA", " ABBBBBBBBBA ", " ABBBBBBBBBA ", " AB A A A BA ", "  B       B  ", "  B       B  ", "  B       B  ", "             ")
                    .slice("AABBBBBBBBBAA", "  BBBBBBBBB  ", "  BBBBBBBBB  ", "  BAAAAAAAB  ", "  B  AAA  B  ", "  B  DDD  B  ", "  B  DDD  B  ", "  B  DDD  B  ")
                    .slice("AABBBBBBBBBAA", " ABBBBBBBBBA ", " ABBBBBBBBBA ", " ABAAAAAAABA ", " AB  AAA  BA ", " AB  DDD  BA ", "  BCCD DCCB  ", "  B  DDD  B  ")
                    .slice("AABBBBBBBBBAA", "  BBBBBBBBB  ", "  BBBBBBBBB  ", "  BAAAAAAAB  ", "  B  AAA  B  ", "  B  DDD  B  ", "  B  DQD  B  ", "  B  DDD  B  ")
                    .slice("AABBBBBBBBBAA", " ABBBBBBBBBA ", " ABBBBBBBBBA ", " AB A A A BA ", "  B       B  ", "  B       B  ", "  B       B  ", "             ")
                    .slice("AABBBBBBBBBAA", "  BBBBBBBBB  ", "  BBBBBBBBB  ", "    A A A    ", "             ", "             ", "             ", "             ")
                    .slice(" AABBBBBBBAA ", "  ABBBBBBBA  ", "  ABBBBBBBA  ", "  ABBBBBBBA  ", "  A A A A A  ", "             ", "             ", "             ")
                    .slice("  AAAAAAAAA  ", "    A A A    ", "    A A A    ", "    A A A    ", "    A A A    ", "             ", "             ", "             ")
                    .slice("   AAAAAAA   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")

                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(CRYOGENIC_CASING.get()).setMinGlobalLimited(160)
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                    )
                    .where('B', blocks(HEAVY_FROST_PROOF_CASING.get()))
                    .where('C', blocks(CRYOGENIC_CASING.get()))
                    .where('D', blocks(HEAVY_FROST_PROOF_CASING.get()))
                    .build())
            // spotless:on
            .model(createSeparateControllerCasingMachineModel(
                    CosmicCore.id("block/casings/solid/heavy_frost_proof_casing"),
                    CosmicCore.id("block/casings/solid/cryogenic_casing"),
                    GTCEu.id("block/multiblock/hpca")))
            .register();

    public static void init() {}
}
