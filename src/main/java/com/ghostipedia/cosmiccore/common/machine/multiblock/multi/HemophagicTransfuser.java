package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.HemophagicTransfuserMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
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
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;

public final class HemophagicTransfuser {

    public static final MultiblockMachineDefinition HEMOPHAGIC_TRANSFUSER = REGISTRATE
            .multiblock("hemophagic_transfuser", HemophagicTransfuserMachine::new)
            .langValue("§aHemophagic Transfuser")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(BLOODSTEEL_KUVITE_PLATING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .noRecipeModifier()
            .partAppearance((controller, part, side) -> BLOODSTEEL_KUVITE_PLATING.getDefaultState())
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("    AAAAAAA    ", "               ", "               ", "               ",
                            "               ", "               ", "               ")
                    .slice("  AABBBBBBBAA  ", "               ", "               ", "               ",
                            "               ", "               ", "               ")
                    .slice(" ACCAAAAAAACCA ", "   C       C   ", "   C       C   ", "               ",
                            "               ", "               ", "               ")
                    .slice(" ACCDDDADDDCCA ", "  CC       CC  ", "  CC       CC  ", "   C       C   ",
                            "   C       C   ", "               ", "               ")
                    .slice("ABADDCAAACDDABA", "     C   C     ", "               ", "               ",
                            "               ", "               ", "               ")
                    .slice("ABADCAAAAACDABA", "    C     C    ", "               ", "               ",
                            "               ", "               ", "               ")
                    .slice("ABADAAAAAAADABA", "      AAA      ", "      AEA      ", "       A       ",
                            "       A       ", "               ", "               ")
                    .slice("ABAAAAAAAAAAABA", "      A A      ", "      A A      ", "      A A      ",
                            "      A A      ", "       A       ", "       A       ")
                    .slice("ABADAAAAAAADABA", "      AAA      ", "      AAA      ", "       A       ",
                            "       A       ", "               ", "               ")
                    .slice("ABADCAAAAACDABA", "    C     C    ", "               ", "               ",
                            "               ", "               ", "               ")
                    .slice("ABADDCAAACDDABA", "     C   C     ", "               ", "               ",
                            "               ", "               ", "               ")
                    .slice(" ACCDDDADDDCCA ", "  CC       CC  ", "  CC       CC  ", "   C       C   ",
                            "   C       C   ", "               ", "               ")
                    .slice(" ACCAAAAAAACCA ", "   C       C   ", "   C       C   ", "               ",
                            "               ", "               ", "               ")
                    .slice("  AABBBBBBBAA  ", "               ", "               ", "               ",
                            "               ", "               ", "               ")
                    .slice("    AAAAAAA    ", "               ", "               ", "               ",
                            "               ", "               ", "               ")
                    .where(' ', any())
                    .where('A', blocks(BLOODSTEEL_KUVITE_PLATING.get())
                            .or(abilities(IMPORT_VITAE_NETWORK).setExactLimit(1).setPreviewCount(1)))
                    .where('B', blocks(BuiltInRegistries.BLOCK.get(
                            ResourceLocation.fromNamespaceAndPath("neovitae", "rune_blank"))))
                    .where('C', blocks(SOUL_MUTED_CASING.get()))
                    .where('D', blocks(GTBlocks.MACHINE_CASING_HV.get()).setExactLimit(28)
                            .or(blocks(GTBlocks.MACHINE_CASING_EV.get()).setExactLimit(28))
                            .or(blocks(GTBlocks.MACHINE_CASING_IV.get()).setExactLimit(28)))
                    .where('E', controller(blocks(definition.getBlock())))
                    .build())
            .model(createSeparateControllerCasingMachineModel(
                    CosmicCore.id("block/casings/solid/bloodsteel_kuvite_plating"),
                    CosmicCore.id("block/casings/solid/bloodsteel_kuvite_plating"),
                    GTCEu.id("block/multiblock/network_switch"))
                    .andThen(model -> model.addDynamicRenderer(
                            CosmicDynamicRenderHelpers::getHemophagicTransfuserRender)))
            .hasBER(true)
            .register();

    private HemophagicTransfuser() {}

    public static void init() {}
}
