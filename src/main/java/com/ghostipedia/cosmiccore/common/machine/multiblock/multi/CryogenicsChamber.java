package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import wayoftime.bloodmagic.BloodMagic;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_SOUL;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_NONCONDUCTING;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_STRESS_PROOF;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class CryogenicsChamber {

    public final static MultiblockMachineDefinition CRYOGENICS_CHAMBER = REGISTRATE
            .multiblock("cryogenics_chamber",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§bCryogenics Chamber")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.CRYOGENICS_CHAMBER)
            .hasBER(true)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()

                    .aisle("   AAAAAAA   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                    .aisle("  AAAAAAAAA  ", "    A A A    ", "    A A A    ", "    A A A    ", "    A A A    ", "             ", "             ", "             ")
                    .aisle(" AABBBBBBBAA ", "  ABBBBBBBA  ", "  ABBBBBBBA  ", "  ABBBBBBBA  ", "  A A A A A  ", "             ", "             ", "             ")
                    .aisle("AABBBBBBBBBAA", "  BBBBBBBBB  ", "  BBBBBBBBB  ", "    A A A    ", "             ", "             ", "             ", "             ")
                    .aisle("AABBBBBBBBBAA", " ABBBBBBBBBA ", " ABBBBBBBBBA ", " AB A A A BA ", "  B       B  ", "  B       B  ", "  B       B  ", "             ")
                    .aisle("AABBBBBBBBBAA", "  BBBBBBBBB  ", "  BBBBBBBBB  ", "  BAAAAAAAB  ", "  B  AAA  B  ", "  B  DDD  B  ", "  B  DDD  B  ", "  B  DDD  B  ")
                    .aisle("AABBBBBBBBBAA", " ABBBBBBBBBA ", " ABBBBBBBBBA ", " ABAAAAAAABA ", " AB  AAA  BA ", " AB  DDD  BA ", "  BCCD DCCB  ", "  B  DDD  B  ")
                    .aisle("AABBBBBBBBBAA", "  BBBBBBBBB  ", "  BBBBBBBBB  ", "  BAAAAAAAB  ", "  B  AAA  B  ", "  B  DDD  B  ", "  B  DQD  B  ", "  B  DDD  B  ")
                    .aisle("AABBBBBBBBBAA", " ABBBBBBBBBA ", " ABBBBBBBBBA ", " AB A A A BA ", "  B       B  ", "  B       B  ", "  B       B  ", "             ")
                    .aisle("AABBBBBBBBBAA", "  BBBBBBBBB  ", "  BBBBBBBBB  ", "    A A A    ", "             ", "             ", "             ", "             ")
                    .aisle(" AABBBBBBBAA ", "  ABBBBBBBA  ", "  ABBBBBBBA  ", "  ABBBBBBBA  ", "  A A A A A  ", "             ", "             ", "             ")
                    .aisle("  AAAAAAAAA  ", "    A A A    ", "    A A A    ", "    A A A    ", "    A A A    ", "             ", "             ", "             ")
                    .aisle("   AAAAAAA   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")

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
                    GTCEu.id("block/multiblock/hpca")).andThen(model -> model
                            .addDynamicRenderer(CosmicDynamicRenderHelpers::createCryoChamberPartRender)))
            .register();

    public static void init() {}

}
