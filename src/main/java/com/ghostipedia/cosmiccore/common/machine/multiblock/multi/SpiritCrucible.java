package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BloodMagicBlocks;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_SOUL;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_STRESS_PROOF;

public class SpiritCrucible {


    public final static MultiblockMachineDefinition HEMOPHAGIC_TRANSFUSER = REGISTRATE
            .multiblock("hemophagic_transfuser",
                    IrisMultiblockMachine::new)
            .langValue("§aHemophagic Transfuser")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.HEMOPHAGIC_TRANSFUSER)
            .partAppearance((controller, part, side) -> CYCLOZINE_CHEMICALLY_REPELLING_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK))
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("   A     A   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                    .aisle("  AA BBB AA  ", "     BBB     ", "             ", "             ", "             ", "             ", "             ", "    BBBBB    ")
                    .aisle(" AAABCCCBAAA ", "  DD     DD  ", "  DD     DD  ", "   D     D   ", "   D     D   ", "   D     D   ", "   D     D   ", "   BB E BB   ")
                    .aisle("AAABBCCCBBAAA", "  DBB   BBD  ", "  DB     BD  ", "  DB FFF BD  ", "  DI HHH GD  ", "  D       D  ", "  D       D  ", "  BB  E  BB  ")
                    .aisle("  BBCCCCCBB  ", "   B     B   ", "     FFF     ", "    F   F    ", "    H   H    ", "             ", "             ", " BB       BB ")
                    .aisle(" BCCCCCCCCCB ", " B         B ", "    FFFFF    ", "   F     F   ", "   H     H   ", "             ", "             ", " B         B ")
                    .aisle(" BCCCCCCCCCB ", " B    J    B ", "    FFFFF    ", "   F     F   ", "   H     H   ", "             ", "             ", " BKK     KKB ")
                    .aisle(" BCCCCCCCCCB ", " B         B ", "    FFFFF    ", "   F     F   ", "   H     H   ", "             ", "             ", " B         B ")
                    .aisle("  BBCCCCCBB  ", "   B     B   ", "     FFF     ", "    F   F    ", "    H   H    ", "             ", "             ", " BB       BB ")
                    .aisle("AAABBCCCBBAAA", "  DBB   BBD  ", "  DB     BD  ", "  DB FFF BD  ", "  DG HHH ID  ", "  D       D  ", "  D       D  ", "  BB  E  BB  ")
                    .aisle(" AAABCCCBAAA ", "  DD     DD  ", "  DD     DD  ", "   D     D   ", "   D     D   ", "   D     D   ", "   D     D   ", "   BB E BB   ")
                    .aisle("  AA BBB AA  ", "     BLB     ", "             ", "             ", "             ", "             ", "             ", "    BBBBB    ")
                    .aisle("   A     A   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(BloodMagicBlocks.BLANK_RUNE.get()))
                    .where('B', blocks(BloodMagicBlocks.DAWN_RITUAL_STONE.get()))
                    .where('C', blocks(BloodMagicBlocks.DUSK_RITUAL_STONE.get()))
                    .where('F', blocks(CASING_STRESS_PROOF.get()))
                    .where('E', blocks(CASING_STRESS_PROOF.get()))
                    .where('D', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()).setMinGlobalLimited(28)
                            // TODO see how to limit to 1 laser OR 1 energy, not 1 of each..
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
                            .or(abilities(IMPORT_SOUL)))
                    .build())
            // spotless:on
            .model(createSeparateControllerCasingMachineModel(BloodMagic.rl("block/blankrune"),
                    CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                    GTCEu.id("block/multiblock/network_switch"))
                    .andThen(model -> model
                            .addDynamicRenderer(CosmicDynamicRenderHelpers::getHemophagicTransfuserRender)))
            .hasBER(true)
            .register();

    public static void init() {}

}
