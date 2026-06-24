package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;

public class WASP {

    public final static MultiblockMachineDefinition WASP = REGISTRATE
            .multiblock("wasp", WorkableElectricMultiblockMachine::new)
            .langValue("Wide Asteroid Separation Platform [WASP]")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.WASP_RECIPES)
            .recipeModifier(CosmicRecipeModifiers::asteroidYieldModifier)
            .appearanceBlock(HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING)
            .partAppearance((controller, part, side) -> HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING.getDefaultState())
            .pattern(definition -> MultiblockPatternBuilder.start()
                    // spotless: off
                    .slice("                       ", "                       ", "        B     B        ",
                            "          C C          ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "          C C          ", "        B     B        ",
                            "                       ", "                       ")
                    .slice("                       ", "                       ", "        B C C B        ",
                            "          C C          ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "          C C          ", "        B C C B        ",
                            "                       ", "                       ")
                    .slice("                       ", "                       ", "        BBCBCBB        ",
                            "          C C          ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "          C C          ", "        BBCBCBB        ",
                            "                       ", "                       ")
                    .slice("                       ", "                       ", "         BCCCB         ",
                            "          CCC          ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "          CCC          ", "         BCCCB         ",
                            "                       ", "                       ")
                    .slice("                       ", "                       ", "      CCCBBCBBCCC      ",
                            "          DCD          ", "          D A          ", "                       ",
                            "                       ", "                       ", "                       ",
                            "          D D          ", "          DCD          ", "      CCCBBCBBCCC      ",
                            "                       ", "                       ")
                    .slice("                       ", "          BCB          ", "     CDDDDBCBDDDDC     ",
                            "        A  C  A        ", "        A     A        ", "                       ",
                            "                       ", "                       ", "                       ",
                            "        A     A        ", "        A  C  A        ", "     CDDDDBCBDDDDC     ",
                            "          BCB          ", "                       ")
                    .slice("                       ", "       DBBBCBBBD       ", "    CDD    C    DDC    ",
                            "      A         A      ", "      A         A      ", "                       ",
                            "                       ", "                       ", "                       ",
                            "      A         A      ", "      A         A      ", "    CDD    C    DDC    ",
                            "       DBBBCBBBD       ", "                       ")
                    .slice("        AABCBAA        ", "      DB  BCB  BD      ", "    CD           DC    ",
                            "                       ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "                       ", "    CD           DC    ",
                            "      DB  BCB  BD      ", "        AABCBAA        ")
                    .slice("       AAABCBAAA       ", "      B E  C  E B      ", "BBB CD  E     E  DC BBB",
                            "     A  E     E  A     ", "     A  E     E  A     ", "        E     E        ",
                            "        E     E        ", "        E     E        ", "        E     E        ",
                            "     A  E     E  A     ", "     A  E     E  A     ", "BBB CD  E     E  DC BBB",
                            "      B E  C  E B      ", "       AAABCBAAA       ")
                    .slice("       AAABCBAAA       ", "      B         B      ", "  BBBD           DBBB  ",
                            "           A           ", "          AAA          ", "          ABA          ",
                            "           B           ", "           B           ", "          ABA          ",
                            "          AAA          ", "           A           ", "  BBBD           DBBB  ",
                            "      B         B      ", "       AAABCBAAA       ")
                    .slice("       BBBBBBBBB       ", "     BBB  AAA  BBB     ", " CCCBB    AAA    BBCCC ",
                            "CCCCD     AAA     DCCCC", "    D    AAAAA    D    ", "         AAAAA         ",
                            "          AAA          ", "          AAA          ", "         AAAAA         ",
                            "    D    AAAAA    D    ", "CCCCD     AAA     DCCCC", " CCCBB    AAA    BBCCC ",
                            "     BBB  AAA  BBB     ", "       BBBBBBBBB       ")
                    .slice("       CCCBBBCCC       ", "     CCCC A A CCCC     ", "  BCCCC   A A   CCCCB  ",
                            "   CCC   AA AA   CCC   ", "         AA AA         ", "         BA AB         ",
                            "         BA AB         ", "         BA AB         ", "         BA AB         ",
                            "         AA AA         ", "   CCC   AA AA   CCC   ", "  BCCCC   A A   CCCCB  ",
                            "     CCCC A A CCCC     ", "       CCCBQBCCC       ")
                    .slice("       BBBBBBBBB       ", "     BBB  AAA  BBB     ", " CCCBB    AAA    BBCCC ",
                            "CCCCD     AAA     DCCCC", "    D    AAAAA    D    ", "         AAAAA         ",
                            "          AAA          ", "          AAA          ", "         AAAAA         ",
                            "    D    AAAAA    D    ", "CCCCD     AAA     DCCCC", " CCCBB    AAA    BBCCC ",
                            "     BBB  AAA  BBB     ", "       BBBBBBBBB       ")
                    .slice("       AAABCBAAA       ", "      B         B      ", "  BBBD           DBBB  ",
                            "           A           ", "          AAA          ", "          ABA          ",
                            "           B           ", "           B           ", "          ABA          ",
                            "          AAA          ", "           A           ", "  BBBD           DBBB  ",
                            "      B         B      ", "       AAABCBAAA       ")
                    .slice("       AAABCBAAA       ", "      B E  C  E B      ", "BBB CD  E     E  DC BBB",
                            "     A  E     E  A     ", "     A  E     E  A     ", "        E     E        ",
                            "        E     E        ", "        E     E        ", "        E     E        ",
                            "     A  E     E  A     ", "     A  E     E  A     ", "BBB CD  E     E  DC BBB",
                            "      B E  C  E B      ", "       AAABCBAAA       ")
                    .slice("        AABCBAA        ", "      DB  BCB  BD      ", "    CD           DC    ",
                            "                       ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "                       ", "    CD           DC    ",
                            "      DB  BCB  BD      ", "        AABCBAA        ")
                    .slice("                       ", "       DBBBCBBBD       ", "    CDD    C    DDC    ",
                            "      A         A      ", "      A         A      ", "                       ",
                            "                       ", "                       ", "                       ",
                            "      A         A      ", "      A         A      ", "    CDD    C    DDC    ",
                            "       DBBBCBBBD       ", "                       ")
                    .slice("                       ", "          BCB          ", "     CDDDDBCBDDDDC     ",
                            "        A  C  A        ", "        A     A        ", "                       ",
                            "                       ", "                       ", "                       ",
                            "        A     A        ", "        A  C  A        ", "     CDDDDBCBDDDDC     ",
                            "          BCB          ", "                       ")
                    .slice("                       ", "                       ", "      CCCBBCBBCCC      ",
                            "          DCD          ", "          D D          ", "                       ",
                            "                       ", "                       ", "                       ",
                            "          D D          ", "          DCD          ", "      CCCBBCBBCCC      ",
                            "                       ", "                       ")
                    .slice("                       ", "                       ", "         BCCCB         ",
                            "          CCC          ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "          CCC          ", "         BCCCB         ",
                            "                       ", "                       ")
                    .slice("                       ", "                       ", "        BBCBCBB        ",
                            "          C C          ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "          C C          ", "        BBCBCBB        ",
                            "                       ", "                       ")
                    .slice("                       ", "                       ", "        B C C B        ",
                            "          C C          ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "          C C          ", "        B C C B        ",
                            "                       ", "                       ")
                    .slice("                       ", "                       ", "        B     B        ",
                            "          C C          ", "                       ", "                       ",
                            "                       ", "                       ", "                       ",
                            "                       ", "          C C          ", "        B     B        ",
                            "                       ", "                       ")
                    // spotless: on
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CosmicBlocks.HIGH_TOLERANCE_RHENIUM_CASING.get()))
                    .where('A', blocks(CosmicBlocks.HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(4))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1, 1)
                                    .setMaxGlobalLimited(3, 3)))
                    .where('E', frames(GTMaterials.Neutronium))
                    .where('D', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where('B', blocks(CosmicBlocks.NAQUADAH_PRESSURE_RESISTANT_CASING.get()))
                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/highly_flexible_reinforced_trinavine_casing"),
                    CosmicCore.id("block/multiblock/mantle_bore"))
            .hasBER(true)
            .register();

    public static void init() {}
}
