package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.LarvaMachine;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.*;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.ITEM_IMPORT_BUS;

public class LARVA {

    public final static MultiblockMachineDefinition LARVA = REGISTRATE
            .multiblock("larva", LarvaMachine::new)
            .langValue("Logistic Asteroid Reclamation and Valuing Assembly [LARVA]")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .recipeModifier(RecipeModifier.NO_MODIFIER)
            .appearanceBlock(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING)
            .partAppearance((controller, part, side) -> TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.getDefaultState())
            .pattern(definition -> FactoryBlockPattern.start()
                    // spotless: off
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "                 BBBBB         BBBBB         BBBBB                 ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "                CA   AC       CA   AC       CA   AC                ",
                            "                BB D BB       BB D BB       BB D BB                ",
                            "                CA   AC       CA   AC       CA   AC                ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "               CC     CC     CC     CC     CC     CC               ",
                            "               BB  D  BB     BB  D  BB     BB  D  BB               ",
                            "               CC     CC     CC     CC     CC     CC               ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                  AAA           AAA           AAA                  ",
                            "              AA  BBB  AA   AA  BBB  AA   AA  BBB  AA              ",
                            "              BB  CCC  BB   BB  CCC  BB   BB  CCC  BB              ",
                            "              AA  BBB  AA   AA  BBB  AA   AA  BBB  AA              ",
                            "                  AAA           AAA           AAA                  ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "              B  C   C  B   B  C   C  B   B  C   C  B              ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "                 AAEAA         AAEAA         AAEAA                 ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "              BDDC   CDDB   BDDC   CDDB   BDDC   CDDB              ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "                 AEFEA         AEFEA         AEFEA                 ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "              B  C   C  B   B  C   C  B   B  C   C  B              ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "                 AAEAA         AAEAA         AAEAA                 ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                  AAA           AAA           AAA                  ",
                            "              AA  BBB  AA   AA  BBB  AA   AA  BBB  AA              ",
                            "    AAAA      BB  CCC  BB   BB  CCC  BB   BB  CCC  BB      AAAA    ",
                            "              AA  BBB  AA   AA  BBB  AA   AA  BBB  AA              ",
                            "                  AAA           AAA           AAA                  ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "     CCC       CC     CC     CC     CC     CC     CC       CCC     ",
                            "   AAAAAAAA    BB  D  BB     BB  D  BB     BB  D  BB    AAAAAAAA   ",
                            "    CCCC       CC     CC     CC     CC     CC     CC       CCCC    ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "   CC  CCCC     CA   AC       CA   AC       CA   AC     CCCC  CC   ",
                            "  AAA  AAAAAA   BB D BB       BB D BB       BB D BB   AAAAAA  AAA  ",
                            "   CC  CCCC     CA   AC       CA   AC       CA   AC     CCCC  CC   ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "  C       CCC    AAAAA         AAAAA         AAAAA    CCC       C  ",
                            " AA       AAAA   BBBBB         BBBBB         BBBBB   AAAA       AA ",
                            "  C       CCC    AAAAA         AAAAA         AAAAA    CCC       C  ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            " C          CC    ABA     CCCCCCABACCCCCC     ABA    CC          C ",
                            "AA          AAA   BAB     CCCCCCBABCCCCCC     BAB   AAA          AA",
                            " C          CC    ABA     CCCCCCABACCCCCC     ABA    CC          C ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            " C           CC   BDB    CCCCCCCBDBCCCCCCC    BDB   CC           C ",
                            "AA           AAA  D D    CCCCCCCD DCCCCCCC    D D  AAA           AA",
                            " C           CC   BDB    CCCCCCCBDBCCCCCCC    BDB   CC           C ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "C             CC  BDB   CCCCCCCCBDBCCCCCCCC   BDB  CC             C",
                            "A             AA  D D   CCCCCCCCD DCCCCCCCC   D D  AA             A",
                            "              CC  BDB   CCCFFFCCBDBCCFFFCCC   BDB  CC              ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "               C  BDB  CCCCCCCCCBDBCCCCCCCCC  BDB  C               ",
                            "               A  D D  CCCCCCCCCD DCCCCCCCCC  D D  A               ",
                            "               C  BDB  CCFFFCCCCBDBCCCCFFFCC  BDB  C               ",
                            "                               FFFFF                               ",
                            "                                                                   ")
                    .aisle("                 CCCCC         CCCCC         CCCCC                 ",
                            "                CDAAADC       CDAAADC       CDAAADC                ",
                            "               CDAA AADCCCCCCCDAA AADCCCCCCCDAA AADC               ",
                            "               CAA   AACCCCCCCAA   AACCCCCCCAA   AAC               ",
                            "               CDAA AADCCCCCCCDAA AADCCCCCCCDAA AADC               ",
                            "                CDAAADC       CDAAADC       CDAAADC                ",
                            "                 CCCCC         CFFFC         CCCCC                 ")
                    .aisle("                 ADADA         ADADA         ADADA                 ",
                            "                A     A       A     A       A     A                ",
                            "                A      AAAAAAAA     AAAAAAAA      A                ",
                            "               DA      AAAAAAAA     AAAAAAAA      AD               ",
                            "                A      AAAAAAAA     AAAAAAAA      A                ",
                            "                A     A       A     A       A     A                ",
                            "                 ADFDA         ADFDA         ADFDA                 ")
                    .aisle("                 ADADA         ADADA         ADADA                 ",
                            "                A     A       A     A       A     A                ",
                            "                A      AAAAAAAA     AAAAAAAA      A                ",
                            "               DA      AAAAAAAA     AAAAAAAA      AD               ",
                            "                A      AFFFFFAA     AAFFFFFA      A                ",
                            "                A     A       A     A       A     A                ",
                            "                 ADFDA         ADQDA         ADFDA                 ")
                    .aisle("                 ADADA         ADADA         ADADA                 ",
                            "                A     A       A     A       A     A                ",
                            "                A      AAAAAAAA     AAAAAAAA      A                ",
                            "               DA      AAAAAAAA     AAAAAAAA      AD               ",
                            "                A      AAAAAAAA     AAAAAAAA      A                ",
                            "                A     A       A     A       A     A                ",
                            "                 ADFDA         ADFDA         ADFDA                 ")
                    .aisle("                 CCCCC         CCCCC         CCCCC                 ",
                            "                CDAAADC       CDAAADC       CDAAADC                ",
                            "               CDABABADCCCCCCCDABABADCCCCCCCDABABADC               ",
                            "               CAAAAAAACCCCCCCAAAAAAACCCCCCCAAAAAAAC               ",
                            "               CDABABADCCCCCCCDABABADCCCCCCCDABABADC               ",
                            "                CDAAADC       CDAAADC       CDAAADC                ",
                            "                 CCCCC         CFFFC         CCCCC                 ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "               C  BDB  CCCCCCCCCBDBCCCCCCCCC  BDB  C               ",
                            "               AAADCD  CCCCCCCCCDCDCCCCCCCCC  DCDAAA               ",
                            "               C  BDB  CCFFFCCCCBDBCCCCFFFCC  BDB  C               ",
                            "                               FFFFF                               ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "C             CC  BDB   CCCCCCCCBDBCCCCCCCC   BDB  CC             C",
                            "A             AAA DCD   CCCCCCCCDCDCCCCCCCC   DCD AAA             A",
                            "              CC  BDB   CCCFFFCCBDBCCFFFCCC   BDB  CC              ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            " C           CC   BDB    CCCCCCCBDBCCCCCCC    BDB   CC           C ",
                            "AA           AAA  DCD    CCCCCCCDCDCCCCCCC    DCD  AAA           AA",
                            " C           CC   BDB    CCCCCCCBDBCCCCCCC    BDB   CC           C ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            " C          CC    ABA     CCCCCCABACCCCCC     ABA    CC          C ",
                            "AA          AAA   BCB     CCCCCCBCBCCCCCC     BCB   AAA          AA",
                            " C          CC    ABA     CCCCCCABACCCCCC     ABA    CC          C ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "  C       CCC    AAAAA         AAAAA         AAAAA    CCC       C  ",
                            " AA       AAAA   BBCBB         BBCBB         BBCBB   AAAA       AA ",
                            "  C       CCC    AAAAA         AAAAA         AAAAA    CCC       C  ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "   CC  CCCC     CA   AC       CA   AC       CA   AC     CCCC  CC   ",
                            "  AAA  AAAAAA   BB D BB       BB D BB       BB D BB   AAAAAA  AAA  ",
                            "   CC  CCCC     CA   AC       CA   AC       CA   AC     CCCC  CC   ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "     CCC       CC     CC     CC     CC     CC     CC       CCC     ",
                            "   AAAAAAAA    BB  D  BB     BB  D  BB     BB  D  BB    AAAAAAAA   ",
                            "    CCCC       CC     CC     CC     CC     CC     CC       CCCC    ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                  AAA           AAA           AAA                  ",
                            "              AA  BBB  AA   AA  BBB  AA   AA  BBB  AA              ",
                            "    AAAA      BB  CCC  BB   BB  CCC  BB   BB  CCC  BB      AAAA    ",
                            "              AA  BBB  AA   AA  BBB  AA   AA  BBB  AA              ",
                            "                  AAA           AAA           AAA                  ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "              B  C   C  B   B  C   C  B   B  C   C  B              ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "                 AAEAA         AAEAA         AAEAA                 ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "              BDDC   CDDB   BDDC   CDDB   BDDC   CDDB              ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "                 AEFEA         AEFEA         AEFEA                 ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "              B  C   C  B   B  C   C  B   B  C   C  B              ",
                            "              A  B   B  A   A  B   B  A   A  B   B  A              ",
                            "                 AAEAA         AAEAA         AAEAA                 ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                  AAA           AAA           AAA                  ",
                            "              AA  BBB  AA   AA  BBB  AA   AA  BBB  AA              ",
                            "              BB  CCC  BB   BB  CCC  BB   BB  CCC  BB              ",
                            "              AA  BBB  AA   AA  BBB  AA   AA  BBB  AA              ",
                            "                  AAA           AAA           AAA                  ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "               CC     CC     CC     CC     CC     CC               ",
                            "               BB  D  BB     BB  D  BB     BB  D  BB               ",
                            "               CC     CC     CC     CC     CC     CC               ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "                CA   AC       CA   AC       CA   AC                ",
                            "                BB D BB       BB D BB       BB D BB                ",
                            "                CA   AC       CA   AC       CA   AC                ",
                            "                                                                   ",
                            "                                                                   ")
                    .aisle("                                                                   ",
                            "                                                                   ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "                 BBBBB         BBBBB         BBBBB                 ",
                            "                 AAAAA         AAAAA         AAAAA                 ",
                            "                                                                   ",
                            "                                                                   ")
                    // spotless: on
                    .where(' ', any())
                    .where("Q", controller(blocks(definition.getBlock())))
                    .where('C', blocks(CosmicBlocks.HIGH_TOLERANCE_RHENIUM_CASING.get()))
                    .where('A', blocks(CosmicBlocks.HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING.get()))
                    .where('F', blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(4))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1, 1)
                                    .setMaxGlobalLimited(3, 3)))
                    .where('E',
                            blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get())
                                    .or(blocks(ITEM_IMPORT_BUS[1].getBlock())))
                    .where('D', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where('B', blocks(CosmicBlocks.NAQUADAH_PRESSURE_RESISTANT_CASING.get()))
                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/highly_flexible_reinforced_trinavine_casing"),
                    CosmicCore.id("block/multiblock/mantle_bore"))
            .hasBER(true)
            .register();

    public static void init() {}
}
