package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.client.renderer.machine.HemophagicTransfuserRender;
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

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.*;

public class HemophagicTransfuser {

    public final static MultiblockMachineDefinition HEMOPHAGIC_TRANSFUSER = REGISTRATE
            .multiblock("hemophagic_transfuser",
                    IrisMultiblockMachine::new)
            .langValue("§aHemophagic Transfuser")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.HEMOPHAGIC_TRANSFUSER)
            .partAppearance((controller, part, side) -> CYCLOZINE_CHEMICALLY_REPELLING_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAAA   AAAA", "A  AAAAA  A", "A         A", "AA       AA", " A       A ", " A       A ",
                            " A       A ", "AA       AA", "A         A", "A  AAAAA  A", "AAAA   AAAA")
                    .aisle("A  AAAAA  A", "   BCCCB   ", "  B     B  ", "A         A", "AC       CA", "AC       CA",
                            "AC       CA", "A         A", "  B     B  ", "   BCCCB   ", "A  AAAAA  A")
                    .aisle("A         A", "  B     B  ", " B       B ", "           ", "D         D", "D         D",
                            "D         D", "           ", " B       B ", "  B     B  ", "A         A")
                    .aisle("AA       AA", "AB       BA", "           ", "   EEEEE   ", "D  E   E  D", "D  E   E  D",
                            "D  E   E  D", "   EEEEE   ", "           ", "AB       BA", "AA       AA")
                    .aisle(" A       A ", "AC       CA", "F         F", "F  E   E  F", "D         D", "D         D",
                            "D         D", "F  E   E  F", "F         F", "AC       CA", " A       A ")
                    .aisle(" A       A ", "AC       CA", "           ", "   E   E   ", "D         D", "D         D",
                            "D         D", "   E   E   ", "           ", "AC       CA", " A       A ")
                    .aisle(" A       A ", "AC       CA", "F         F", "F  E   E  F", "D         D", "D         D",
                            "D         D", "F  E   E  F", "F         F", "AC       CA", " A       A ")
                    .aisle("AA       AA", "AB       BA", "           ", "   EEEEE   ", "D  E   E  D", "D  E   E  D",
                            "D  E   E  D", "   EEEEE   ", "           ", "AB       BA", "AA       AA")
                    .aisle("A         A", "  B     B  ", " B       B ", "           ", "D         D", "D         D",
                            "D         D", "           ", " B       B ", "  B     B  ", "A         A")
                    .aisle("A  AAAAA  A", "   BCCCB   ", "  B     B  ", "A         A", "AC       CA", "AC       CA",
                            "AC       CA", "A         A", "  B     B  ", "   BCCCB   ", "A  AAAAA  A")
                    .aisle("AAAA   AAAA", "A  AAQAA  A", "A         A", "AA       AA", " A       A ", " A       A ",
                            " A       A ", "AA       AA", "A         A", "A  AAAAA  A", "AAAA   AAAA")
                    .where(' ', any())
                    .where("Q", controller(blocks(definition.getBlock())))
                    .where('A', blocks(BloodMagicBlocks.BLANK_RUNE.get()))
                    .where('B', blocks(BloodMagicBlocks.DAWN_RITUAL_STONE.get()))
                    .where('C', blocks(BloodMagicBlocks.DUSK_RITUAL_STONE.get()))
                    .where('F', blocks(CASING_STRESS_PROOF.get()))
                    .where('E', blocks(CASING_STRESS_PROOF.get()))
                    .where('D', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()).setMinGlobalLimited(28)
                            // TODO see how to limit to 1 laser OR 1 energy, not 1 of each..
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1)
                                    .setPreviewCount(1)))
                    .build())
            .renderer(() -> new HemophagicTransfuserRender(
                    BloodMagic.rl("block/blankrune"),
                    CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                    GTCEu.id("block/multiblock/network_switch")))
            .hasTESR(true)
            .register();

    public static void init() {}
}
