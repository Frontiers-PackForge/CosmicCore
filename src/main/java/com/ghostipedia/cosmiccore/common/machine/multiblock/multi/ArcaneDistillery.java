package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_ATOMIC;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class ArcaneDistillery {

    public static final MultiblockMachineDefinition ARCANE_DISTILLERY = REGISTRATE
            .multiblock("arcane_distillery", WorkableElectricMultiblockMachine::new)
            .langValue("§6Arcane Distillery")
            .recipeTypes(CosmicRecipeTypes.ARCANE_DISTILLERY, CosmicRecipeTypes.ARCANE_FOLDING)
            .rotationState(RotationState.NON_Y_AXIS)
            .partAppearance((controller, part, side) -> HIGH_TOLERANCE_RHENIUM_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK))
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("     AAAAAAAAA     ", "        A A        ", "      AAA AAA      ", "        AAA        ", "       BBABB       ", "        BBB        ", "A        B        A", "A A             A A", "A A B         B A A", "AAAABB       BBAAAA", "A  AABB     BBAA  A", "AAAABB       BBAAAA", "A A B         B A A", "A A             A A", "A        B        A", "        BBB        ", "       BBABB       ", "        AAA        ", "      AAA AAA      ", "        A A        ", "     AAAAAAAAA     ")
                    .aisle("    AA       AA    ", "      CCCACCC      ", "     A  A A  A     ", "       BAAAB       ", "      BB A BB      ", "A                 A", "A A             A A", " C  B         B  C ", " C BB         BB C ", " CAA           AAC ", " A AA         AA A ", " CAA           AAC ", " C BB         BB C ", " C  B         B  C ", "A A             A A", "A                 A", "      BB A BB      ", "       BAAAB       ", "     A  A A  A     ", "      CCCACCC      ", "    AA       AA    ")
                    .aisle("    A         A    ", "     CCAAAAACC     ", "    A D A A D A    ", "      DBAAABD      ", "     BD  A  DB     ", "A A   D     D   A A", " C  B D     D B  C ", " CDDDDDDDDDDDDDDDC ", " A B  D     D  B A ", " AAA  D     D  AAA ", " A AA D     D AA A ", " AAA  D     D  AAA ", " A B  D     D  B A ", " CDDDDDDDDDDDDDDDC ", " C  B D     D B  C ", "A A   D     D   A A", "     BD  A  DB     ", "      DBAAABD      ", "    A D A A D A    ", "     CCAAAAACC     ", "    A         A    ")
                    .aisle("    A         A    ", "     CAAAAAAAC     ", "    A  AA AA  A    ", "     BBBAAABBB     ", "    BB  A A  BB    ", "A A B   A A   B A A", " C BB   A A   BB C ", " A B           B A ", " AAB           BAA ", " AAAAAA     AAAAAA ", " A A           A A ", " AAAAAA     AAAAAA ", " AAB           BAA ", " A B           B A ", " C BB   A A   BB C ", "A A B   A A   B A A", "    BB  A A  BB    ", "     BBBAAABBB     ", "    A  AA AA  A    ", "     CAAAAAAAC     ", "    A         A    ")
                    .aisle("    A         A    ", "    ACAAEEEAACA    ", "    AAAAAAAAAAA    ", "    AAAAAAAAAAA    ", "    B  AF FA  B    ", "AAAAB  AF FA  BAAAA", " CAA   AF FA   AAC ", " AAA           AAA ", " AAAAAA     AAAAAA ", " EAAFFF     FFFAAE ", " EAA           AAE ", " EAAFFF     FFFAAE ", " AAAAAA     AAAAAA ", " AAA           AAA ", " CAA   AF FA   AAC ", "AAAAB  AF FA  BAAAA", "    B  AF FA  B    ", "    AAAAAAAAAAA    ", "    AAAAAAAAAAA    ", "    ACAAEEEAACA    ", "    A         A    ")
                    .aisle("    A         A    ", "     AAAEEEAAA     ", "        A A        ", "    AAAAA AAAAA    ", "    AAA     AAA    ", "A  AA         AA  A", " A AA         AA A ", " A AA         AA A ", " A A           A A ", " EAA           AAE ", " E               E ", " EAA           AAE ", " A A           A A ", " A AA         AA A ", " A AA         AA A ", "A  AA         AA  A", "    AAA     AAA    ", "    AAAAA AAAAA    ", "        A A        ", "     AAAEEEAAA     ", "    A         A    ")
                    .aisle("    A         A    ", "    ACAAEEEAACA    ", "    AAAAAAAAAAA    ", "    AAAAAAAAAAA    ", "    B  AF FA  B    ", "AAAAB  AF FA  BAAAA", " CAA   AF FA   AAC ", " AAA           AAA ", " AAAAAA     AAAAAA ", " EAAFFF     FFFAAE ", " EAA           AAE ", " EAAFFF     FFFAAE ", " AAAAAA     AAAAAA ", " AAA           AAA ", " CAA   AF FA   AAC ", "AAAAB  AF FA  BAAAA", "    B  AF FA  B    ", "    AAAAAAAAAAA    ", "    AAAAAAAAAAA    ", "    ACAAEEEAACA    ", "    A         A    ")
                    .aisle("    A         A    ", "     CAAAAAAAC     ", "    A  AA AA  A    ", "     BBBAAABBB     ", "    BB  A A  BB    ", "A A B   A A   B A A", " C BB   A A   BB C ", " A B           B A ", " AAB           BAA ", " AAAAAA     AAAAAA ", " A A           A A ", " AAAAAA     AAAAAA ", " AAB           BAA ", " A B           B A ", " C BB   A A   BB C ", "A A B   A A   B A A", "    BB  A A  BB    ", "     BBBAAABBB     ", "    A  AA AA  A    ", "     CAAAAAAAC     ", "    A         A    ")
                    .aisle("    A         A    ", "     CCAAAAACC     ", "    A D A A D A    ", "      DBAAABD      ", "     BD  A  DB     ", "A A   D     D   A A", " C  B D     D B  C ", " CDDDDDDDDDDDDDDDC ", " A B  D     D  B A ", " AAA  D     D  AAA ", " A AA D     D AA A ", " AAA  D     D  AAA ", " A B  D     D  B A ", " CDDDDDDDDDDDDDDDC ", " C  B D     D B  C ", "A A   D     D   A A", "     BD  A  DB     ", "      DBAAABD      ", "    A D A A D A    ", "     CCAAAAACC     ", "    A         A    ")
                    .aisle("    AA       AA    ", "      CCCACCC      ", "     A  A A  A     ", "       BAAAB       ", "      BB A BB      ", "A                 A", "A A             A A", " C  B         B  C ", " C BB         BB C ", " CAA           AAC ", " A AA         AA A ", " CAA           AAC ", " C BB         BB C ", " C  B         B  C ", "A A             A A", "A                 A", "      BB A BB      ", "       BAAAB       ", "     A  A A  A     ", "      CCCACCC      ", "    AA       AA    ")
                    .aisle("     AAAAAAAAA     ", "        A A        ", "      AAA AAA      ", "        AQA        ", "       BBABB       ", "        BBB        ", "A        B        A", "A A             A A", "A A B         B A A", "AAAABB       BBAAAA", "A  AABB     BBAA  A", "AAAABB       BBAAAA", "A A B         B A A", "A A             A A", "A        B        A", "        BBB        ", "       BBABB       ", "        AAA        ", "      AAA AAA      ", "        A A        ", "     AAAAAAAAA     ")
                    .where('Q', Predicates.controller(Predicates.blocks(definition.get())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(CASING_ATOMIC.get()))
                    .where('B', blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get()))
                    .where('C', blocks(RESONANTLY_TUNED_VIRTUE_MELD_CASING.get()))
                    .where('D', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, CosmicMaterials.Trinavine)))
                    .where('E', blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.ARCANE_DISTILLERY))
                            .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.INPUT_LASER).setMaxGlobalLimited(2, 2)
                                    .setPreviewCount(1)))
                    .where('F', blocks(FUSION_COIL.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/high_tolerance_rhenium_casing"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant"))
            .register();

    public static void init() {}
}
