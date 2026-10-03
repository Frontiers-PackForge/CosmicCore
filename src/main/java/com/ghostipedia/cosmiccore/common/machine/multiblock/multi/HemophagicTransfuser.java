package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.HemophagicTransfuserMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_VITAE_NETWORK;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.BLOODSTEEL_KUVITE_PLATING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.SOUL_MUTED_CASING;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.autoAbilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;

public final class HemophagicTransfuser {

    public static final MultiblockMachineDefinition HEMOPHAGIC_TRANSFUSER = REGISTRATE
            .multiblock("hemophagic_transfuser", HemophagicTransfuserMachine::new)
            .langValue("§aHemophagic Transfuser")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(SOUL_MUTED_CASING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .noRecipeModifier()
            .partAppearance((controller, part, side) -> SOUL_MUTED_CASING.getDefaultState())
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("   AAAAAAA   ", "   AAAAAAA   ", "             ", "             ", "             ",
                            "             ", "             ")
                    .slice("  AAAAAAAAA  ", "  ABCCCCCBA  ", "   B     B   ", "   B     B   ", "             ",
                            "             ", "             ")
                    .slice(" AAAAAAAAAAA ", " ABCBBBBBCBA ", "  B BDDDB B  ", "  B       B  ", "  B       B  ",
                            "             ", "             ")
                    .slice("AAAAAAAAAAAAA", "ABCBAAAAABCBA", " B BDAAADB B ", " B   AGA   B ", "             ",
                            "             ", "             ")
                    .slice("AAAAAAAAAAAAA", "ACBAAAAAAABCA", "  BDAEEEADB  ", "    A   A    ", "             ",
                            "             ", "             ")
                    .slice("AAAAAAAAAAAAA", "ACBAAAAAAABCA", "  DAEEEEEAD  ", "   A     A   ", "             ",
                            "             ", "             ")
                    .slice("AAAAAAAAAAAAA", "ACBAAAAAAABCA", "  DAEEEEEAD  ", "   A     A   ", "             ",
                            "             ", "      F      ")
                    .slice("AAAAAAAAAAAAA", "ACBAAAAAAABCA", "  DAEEEEEAD  ", "   A     A   ", "             ",
                            "             ", "             ")
                    .slice("AAAAAAAAAAAAA", "ACBAAAAAAABCA", "  BDAEEEADB  ", "    A   A    ", "             ",
                            "             ", "             ")
                    .slice("AAAAAAAAAAAAA", "ABCBAAAAABCBA", " B BDAAADB B ", " B   AAA   B ", "             ",
                            "             ", "             ")
                    .slice(" AAAAAAAAAAA ", " ABCBBBBBCBA ", "  B BDDDB B  ", "  B       B  ", "  B       B  ",
                            "             ", "             ")
                    .slice("  AAAAAAAAA  ", "  ABCCCCCBA  ", "   B     B   ", "   B     B   ", "             ",
                            "             ", "             ")
                    .slice("   AAAAAAA   ", "   AAAAAAA   ", "             ", "             ", "             ",
                            "             ", "             ")
                    .where(' ', any())
                    .where('A', blocks(SOUL_MUTED_CASING.get())
                            .or(abilities(IMPORT_VITAE_NETWORK).setExactLimit(1).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_ITEMS))
                            .or(abilities(PartAbility.IMPORT_FLUIDS))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                            .or(autoAbilities(true, false, false)))
                    .where('B', blocks(BLOODSTEEL_KUVITE_PLATING.get()))
                    .where('C', blocks(BuiltInRegistries.BLOCK.get(
                            ResourceLocation.fromNamespaceAndPath("neovitae", "rune_blank"))))
                    .where('D', blocks(GTBlocks.MACHINE_CASING_HV.get()).setExactLimit(20)
                            .or(blocks(GTBlocks.MACHINE_CASING_EV.get()).setExactLimit(20))
                            .or(blocks(GTBlocks.MACHINE_CASING_IV.get()).setExactLimit(20)))
                    .where('E', any())
                    .where('F', any())
                    .where('G', controller(blocks(definition.getBlock())))
                    .build())
            .model(createSeparateControllerCasingMachineModel(
                    CosmicCore.id("block/casings/solid/soul_muted_casing"),
                    CosmicCore.id("block/casings/solid/soul_muted_casing"),
                    GTCEu.id("block/multiblock/network_switch"))
                    .andThen(model -> model.addDynamicRenderer(
                            CosmicDynamicRenderHelpers::getHemophagicTransfuserRender)))
            .hasBER(true)
            .register();

    private HemophagicTransfuser() {}

    public static void init() {}
}
