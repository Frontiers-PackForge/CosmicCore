package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.block.MurkFloraBlock;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;
import com.ghostipedia.cosmiccore.common.data.tag.block.CosmicBlockTags;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.AbyssalCultureVatMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BiomanaDigestorMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BloomwyrmHeartMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BloomwyrmUnitMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.ManawombLeachingPondMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.SculkBiochamberMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.TagUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import com.sammy.malum.registry.common.block.MalumBlocks;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.autoAbilitiesNoEnergyIn;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.autoAbilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blockTag;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.machines;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.states;

public final class BloomwyrmSystem {

    public static final MultiblockMachineDefinition BLOOMWYRM_HEART = REGISTRATE
            .multiblock("bloomwyrm_heart", BloomwyrmHeartMachine::new)
            .langValue("Bloomwyrm Heart")
            .tooltips(
                    Component.translatable("cosmiccore.machine.bloomwyrm_heart.tooltip.0"),
                    Component.translatable("cosmiccore.machine.bloomwyrm_heart.tooltip.1"),
                    Component.translatable("cosmiccore.machine.bloomwyrm_heart.tooltip.2"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(SOMARUST_CASING)
            .partAppearance((controller, part, side) -> SOMARUST_CASING.getDefaultState())
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("    AAA    ", "    A A    ", "           ", "           ", "           ", "           ",
                            "           ")
                    .slice("   AAAAA   ", "   AAAAA   ", "   AAAAA   ", "   AA AA   ", "           ", "           ",
                            "           ")
                    .slice("  AAAAAAA  ", "  ABBBBBA  ", "  ACCCCCA  ", "  A     A  ", "  A     A  ", "           ",
                            "           ")
                    .slice(" AAAAAAAAA ", " ABBBBBBBA ", " ACC   CCA ", " A       A ", "           ", "           ",
                            "           ")
                    .slice("AAAAAAAAAAA", "AABBBDBBBAA", " AC  D  CA ", " A   D   A ", "    EEE    ", "    EFE    ",
                            "    EEE    ")
                    .slice("AAAAAAAAAAA", " ABBBBBBBA ", " AD     CA ", "  D        ", "  D EEE    ", "  DDE E    ",
                            "    EEE    ")
                    .slice("AAAAAAAAAAA", "AABBDBBBBAA", " AC D  DCA ", " A  D  D A ", "    EEED   ", "    EEE    ",
                            "    EEE    ")
                    .slice(" AAAAAAAAA ", " ABBBBBBBA ", " ACC   CCA ", " A       A ", "           ", "           ",
                            "           ")
                    .slice("  AAAAAAA  ", "  ABBBBBA  ", "  ACCCCCA  ", "  A     A  ", "  A     A  ", "           ",
                            "           ")
                    .slice("   AAAAA   ", "   AAAAA   ", "   AAAAA   ", "   AA AA   ", "           ", "           ",
                            "           ")
                    .slice("    AAA    ", "    A A    ", "           ", "           ", "           ", "           ",
                            "           ")
                    .where(' ', any())
                    .where('A', blocks(SOMARUST_CASING.get())
                            .or(machines(bloomwyrmEnergyInputs()).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(MalumBlocks.BLIGHTED_EARTH.get()))
                    .where('C', blocks(MalumBlocks.BLIGHTED_EARTH.get()))
                    .where('D', blocks(RUST_STAINED_CASING.get()))
                    .where('E', blocks(SOMARUST_CASING.get()))
                    .where('F', controller(blocks(definition.getBlock())))
                    .build())
            .model(createSeparateControllerCasingMachineModel(
                    CosmicCore.id("block/casings/solid/somarust_casing"),
                    CosmicCore.id("block/casings/solid/somarust_casing"),
                    CosmicCore.id("block/multiblock/dawnforge"))
                    .andThen(model -> model
                            .addDynamicRenderer(CosmicDynamicRenderHelpers::createBloomwyrmHeartPartRender)))
            .register();

    private static MachineDefinition[] bloomwyrmEnergyInputs() {
        return Stream.of(
                CosmicMachines.BLOOMWYRM_POWER_ROOT,
                new MachineDefinition[] {
                        GTMachines.ENERGY_INPUT_HATCH[GTValues.LV],
                        GTMachines.ENERGY_INPUT_HATCH[GTValues.MV],
                        GTMachines.ENERGY_INPUT_HATCH[GTValues.HV] },
                GTMachines.ENERGY_INPUT_HATCH_4A,
                GTMachines.ENERGY_INPUT_HATCH_16A)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .toArray(MachineDefinition[]::new);
    }

    public static final MultiblockMachineDefinition ABYSSAL_CULTURE_VAT = REGISTRATE
            .multiblock("abyssal_culture_vat", AbyssalCultureVatMachine::new)
            .langValue("Abyssal Culture Vat")
            .tooltips(
                    Component.translatable("cosmiccore.machine.abyssal_culture_vat.tooltip.0"),
                    Component.translatable("cosmiccore.machine.abyssal_culture_vat.tooltip.1"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.ABYSSAL_CULTURE_VAT)
            .recipeModifier(BloomwyrmUnitMachine::recipeModifier)
            .appearanceBlock(SOMARUST_CASING)
            .partAppearance((controller, part, side) -> SOMARUST_CASING.getDefaultState())
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice(" AAA ", " ADA ", "  A  ", "     ", "     ", "  A  ", " AAA ", " AAA ")
                    .slice("AAAAA", "ACCCA", " CCC ", " CCC ", " CCC ", " CCC ", "ACCCA", "AAAAA")
                    .slice("AAAAA", "ACECA", "ACECA", " CEC ", " CEC ", "ACECA", "ACECA", "AAAAA")
                    .slice("AAAAA", "ACCCA", " CCC ", " CCC ", " CCC ", " CCC ", "ACCCA", "AAAAA")
                    .slice("BAAAB", "BAAAB", "BAAAB", "BAAAB", "BAAAB", "BAAAB", "BAAAB", "BAAAB")
                    .where(' ', any())
                    .where('A', blocks(SOMARUST_CASING.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.ABYSSAL_CULTURE_VAT))
                            .or(autoAbilities(true, false, false)))
                    .where('B', blockTag(TagUtil.createBlockTag("frames/dark_steel")))
                    .where('C', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('D', controller(blocks(definition.getBlock())))
                    .where('E', states(MURK_KELP.getDefaultState().setValue(MurkFloraBlock.WATERLOGGED, true)))
                    .build())
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/somarust_casing"),
                    CosmicCore.id("block/multiblock/mixing_vessel"))
            .register();

    public static final MultiblockMachineDefinition SCULK_BIOCHAMBER = REGISTRATE
            .multiblock("sculk_biochamber", SculkBiochamberMachine::new)
            .langValue("Sculk Biochamber")
            .tooltips(
                    Component.translatable("cosmiccore.machine.sculk_biochamber.tooltip.0"),
                    Component.translatable("cosmiccore.machine.sculk_biochamber.tooltip.1"),
                    Component.translatable("cosmiccore.machine.bloomwyrm_unit.tooltip.parallel"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.SCULK_BIOCHAMBER)
            .recipeModifier(BloomwyrmUnitMachine::recipeModifier)
            .appearanceBlock(SOMARUST_CASING)
            .partAppearance((controller, part, side) -> SOMARUST_CASING.getDefaultState())
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAEAA", "ABBBA", "ABBBA", "ABBBA", "ABBBA", "ABBBA", "AAAAA")
                    .slice("AAAAA", "BCDCB", "BCDCB", "BCDCB", "BCDCB", "BCDCB", "AAAAA")
                    .slice("AAAAA", "BDFDB", "BDFDB", "BDFDB", "BDFDB", "BDFDB", "AAAAA")
                    .slice("AAAAA", "BCDCB", "BCDCB", "BCDCB", "BCDCB", "BCDCB", "AAAAA")
                    .slice("AAAAA", "ABBBA", "ABBBA", "ABBBA", "ABBBA", "ABBBA", "AAAAA")
                    .where(' ', any())
                    .where('A', blocks(SOMARUST_CASING.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.SCULK_BIOCHAMBER))
                            .or(autoAbilities(true, false, false)))
                    .where('B', blocks(Blocks.CYAN_STAINED_GLASS))
                    .where('C', blocks(Blocks.SCULK))
                    .where('D', blockTag(TagUtil.createBlockTag("frames/dark_steel")))
                    .where('E', controller(blocks(definition.getBlock())))
                    .where('F', blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                    .build())
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/somarust_casing"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant"))
            .register();

    public static final MultiblockMachineDefinition BIOMANA_DIGESTOR = REGISTRATE
            .multiblock("biomana_digestor", BiomanaDigestorMachine::new)
            .langValue("Biomana Digestor")
            .tooltips(
                    Component.translatable("cosmiccore.machine.biomana_digestor.tooltip.0"),
                    Component.translatable("cosmiccore.machine.biomana_digestor.tooltip.1"),
                    Component.translatable("cosmiccore.machine.bloomwyrm_unit.tooltip.parallel"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.BIOMANA_DIGESTOR)
            .recipeModifier(BloomwyrmUnitMachine::recipeModifier)
            .appearanceBlock(SOMARUST_CASING)
            .partAppearance((controller, part, side) -> SOMARUST_CASING.getDefaultState())
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                    .slice("       ", "       ", " AAAAA ", " AAAAA ", " CCCCC ", " JMMMF ")
                    .slice(" G   G ", " GAAAG ", "AAALAAA", "AAH LAA", "CC   CC", "JD   KF")
                    .slice("       ", " AAAAA ", "AALHLAA", "AH   IA", "C     C", "E     E")
                    .slice("       ", " AAAAA ", "AHHHLHA", "A     B", "C     C", "E     E")
                    .slice("       ", " AAAAA ", "AALHHAA", "AL   HA", "C     C", "E     E")
                    .slice(" G   G ", " GAAAG ", "AAALAAA", "AAH IAA", "CC   CC", "KF   JD")
                    .slice("       ", "       ", " AAAAA ", " AAAAA ", " CCCCC ", " KMMMD ")
                    .where(' ', any())
                    .where('A', blocks(SOMARUST_CASING.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.BIOMANA_DIGESTOR))
                            .or(autoAbilities(true, false, false)))
                    .where('B', controller(blocks(definition.getBlock())))
                    .where('C', blocks(GTBlocks.CASING_PTFE_INERT.get()))
                    .where('D', blockTag(CosmicBlockTags.INDUSTRIAL_IRON_BARS))
                    .where('E', blockTag(CosmicBlockTags.INDUSTRIAL_IRON_BARS))
                    .where('F', blockTag(CosmicBlockTags.INDUSTRIAL_IRON_BARS))
                    .where('G', blockTag(TagUtil.createBlockTag("frames/stainless_steel")))
                    .where('H', blocks(Blocks.SCULK))
                    .where('I', blocks(Blocks.SCULK_CATALYST))
                    .where('J', blockTag(CosmicBlockTags.INDUSTRIAL_IRON_BARS))
                    .where('K', blockTag(CosmicBlockTags.INDUSTRIAL_IRON_BARS))
                    .where('L', blocks(Blocks.MOSS_BLOCK))
                    .where('M', blockTag(CosmicBlockTags.INDUSTRIAL_IRON_BARS))
                    .build())
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/somarust_casing"),
                    CosmicCore.id("block/multiblock/solidifier"))
            .register();

    public static final MultiblockMachineDefinition MANAWOMB_LEECHING_POND = REGISTRATE
            .multiblock("manawomb_leeching_pond", ManawombLeachingPondMachine::new)
            .langValue("Manawomb Leaching Pond")
            .tooltips(
                    Component.translatable("cosmiccore.machine.manawomb_leeching_pond.tooltip.0"),
                    Component.translatable("cosmiccore.machine.manawomb_leeching_pond.tooltip.1"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.MANAWOMB_LEECHING_POND)
            .recipeModifier(BloomwyrmUnitMachine::recipeModifier)
            .appearanceBlock(SOMARUST_CASING)
            .partAppearance((controller, part, side) -> SOMARUST_CASING.getDefaultState())
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                    .slice("AA     AA", "AA     AA", " BBBBBBB ", "AA     AA", "AA     AA")
                    .slice("AA     AA", "ADDDDDDDA", "BDDDDDDDB", "ADDDDDDDA", "AA     AA")
                    .slice("         ", "ADDDDDDDA", "AD     DA", "AD     DA", "         ")
                    .slice("         ", "ADDDDDDDA", "AD     DC", "AD     DA", "         ")
                    .slice("         ", "ADDDDDDDA", "AD     DA", "AD     DA", "         ")
                    .slice("AA     AA", "ADDDDDDDA", "BDDDDDDDB", "ADDDDDDDA", "AA     AA")
                    .slice("AA     AA", "AA     AA", " BBBBBBB ", "AA     AA", "AA     AA")
                    .where(' ', any())
                    .where('A', blocks(SOMARUST_CASING.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.MANAWOMB_LEECHING_POND))
                            .or(autoAbilities(true, false, false)))
                    .where('B', blockTag(TagUtil.createBlockTag("frames/stainless_steel")))
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('D', blocks(GTBlocks.CASING_TITANIUM_STABLE.get()))
                    .build())
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/somarust_casing"),
                    CosmicCore.id("block/multiblock/wireless_data_transmitter"))
            .register();

    private BloomwyrmSystem() {}

    public static void init() {}
}
