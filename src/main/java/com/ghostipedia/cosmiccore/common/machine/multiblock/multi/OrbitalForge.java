package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.HEAT_VENT;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.BATCH_MODE;

public class OrbitalForge {

    public final static MultiblockMachineDefinition ORBITAL_TEMPERING_FORGE = REGISTRATE.multiblock(
            "orbital_tempering_forge", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(CosmicRecipeTypes.ORBITAL_FORGE)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers::ebfOverclock, BATCH_MODE)
            .appearanceBlock(CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING)
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("                                   ", "                                   ", "   AAAAA                   AAAAA   ", "   BBBBB                   BBBBB   ", "   AAAAA                   AAAAA   ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "  CA   AC      C   C      CA   AC  ", "  BB D BB      CDDDC      BB D BB  ", "  CA   AC      C   C      CA   AC  ", "                                   ", "                                   ")
                    .aisle("                                   ", "               CAAAC               ", " CC     CC     DEEED     CC     CC ", " BB  D  BB     AEEEA     BB  D  BB ", " CC     CC     DEEED     CC     CC ", "               CAAAC               ", "                                   ")
                    .aisle("               C   C               ", "    AAA        DEEED        AAA    ", "AA  FFF  AA    A   A    AA  FFF  AA", "BB  CCC  BB    A   A    BB  CCC  BB", "AA  FFF  AA    A   A    AA  FFF  AA", "    AAA        DEEED        AAA    ", "               C   C               ")
                    .aisle("               CDDDC               ", "   AAAAA       AEEEA       AAAAA   ", "A  F   F  AABBBA   ABBBAA  F   F  A", "B  C   C  BBDDD     DDDBB  C   C  B", "A  F   F  AABBBA   ABBBAA  F   F  A", "   AAAAA       AEEEA       AAAAA   ", "               CDDDC               ")
                    .aisle("               C   C               ", "   AAAAA       AEEEA       AAAAA   ", "A  F   F  ABDDD     DDDBA  F   F  A", "BDDC   CDDBA           ABDDC   CDDB", "A  F   F  ABDDD     DDDBA  F   F  A", "   AAAAA       AEEEA       AAAAA   ", "               C   C               ")
                    .aisle("               CDDDC               ", "   AAAAA       AEEEA       AAAAA   ", "A  F   F  AABBBA   ABBBAA  F   F  A", "B  C   C  BBDDD     DDDBB  C   C  B", "A  F   F  AABBBA   ABBBAA  F   F  A", "   AAAAA       AEEEA       AAAAA   ", "               CDDDC               ")
                    .aisle("               C   C               ", "    AAA        DEEED        AAA    ", "AA  FFF  AA    A   A    AA  FFF  AA", "BB  CCC  BB    A   A    BB  CCC  BB", "AA  FFF  AA    A   A    AA  FFF  AA", "    AAA        DEEED        AAA    ", "               C   C               ")
                    .aisle("                                   ", "               CAAAC               ", " CC     CC     DEEED     CC     CC ", " BB  D  BB     AEEEA     BB  D  BB ", " CC     CC     DEEED     CC     CC ", "               CAAAC               ", "                                   ")
                    .aisle("                                   ", "                                   ", "  CA   AC      CAAAC      CA   AC  ", "  BB D BB      CAXAC      BB D BB  ", "  CA   AC      CAAAC      CA   AC  ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "   AAAAA                   AAAAA   ", "   BBCBB                   BBCBB   ", "   AAAAA                   AAAAA   ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "    ABA                     ABA    ", "    BCB                     BCB    ", "    ABA                     ABA    ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "    BDB                     BDB    ", "    DCD                     DCD    ", "    BDB                     BDB    ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "    BDB                     BDB    ", "    DCD                     DCD    ", "    BDB                     BDB    ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "    BDB                     BDB    ", "    DCD                     DCD    ", "    BDB                     BDB    ", "                                   ", "                                   ")
                    .aisle("   CCCCC                   CCCCC   ", "  CDAAADC                 CDAAADC  ", " CDABABADC               CDABABADC ", " CAAAAAAAC               CAAAAAAAC ", " CDABABADC               CDABABADC ", "  CDAAADC                 CDAAADC  ", "   CCCCC                   CCCCC   ")
                    .aisle("    D D                     D D    ", "  AEEEEEA                 AEEEEEA  ", "  E     EA               AE     E  ", " DE     EA               AE     ED ", "  E     EA               AE     E  ", "  AEEEEEA                 AEEEEEA  ", "    D D                     D D    ")
                    .aisle("    D D                     D D    ", "  AEEEEEA                 AEEEEEA  ", "  E     EA               AE     E  ", " DE     EA               AE     ED ", "  E     EA               AE     E  ", "  AEEEEEA                 AEEEEEA  ", "    D D                     D D    ")
                    .aisle("    D D                     D D    ", "  AEEEEEA                 AEEEEEA  ", "  E     EA               AE     E  ", " DE     EA               AE     ED ", "  E     EA               AE     E  ", "  AEEEEEA                 AEEEEEA  ", "    D D                     D D    ")
                    .aisle("   CCCCC                   CCCCC   ", "  CDAAADC                 CDAAADC  ", " CDAA AADC               CDAA AADC ", " CAA   AAC               CAA   AAC ", " CDAA AADC               CDAA AADC ", "  CDAAADC                 CDAAADC  ", "   CCCCC                   CCCCC   ")
                    .aisle("                                   ", "                                   ", "    BDB                     BDB    ", "    D D                     D D    ", "    BDB                     BDB    ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "    BDB                     BDB    ", "    D D                     D D    ", "    BDB                     BDB    ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "    BDB                     BDB    ", "    D D                     D D    ", "    BDB                     BDB    ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "    ABA                     ABA    ", "    BAB                     BAB    ", "    ABA                     ABA    ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "   AAAAA                   AAAAA   ", "   BBBBB                   BBBBB   ", "   AAAAA                   AAAAA   ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "  CA   AC      CAAAC      CA   AC  ", "  BB D BB      CAAAC      BB D BB  ", "  CA   AC      CAAAC      CA   AC  ", "                                   ", "                                   ")
                    .aisle("                                   ", "               CAAAC               ", " CC     CC     DEEED     CC     CC ", " BB  D  BB     AEEEA     BB  D  BB ", " CC     CC     DEEED     CC     CC ", "               CAAAC               ", "                                   ")
                    .aisle("               C   C               ", "    AAA        DEEED        AAA    ", "AA  FFF  AA    A   A    AA  FFF  AA", "BB  CCC  BB    A   A    BB  CCC  BB", "AA  FFF  AA    A   A    AA  FFF  AA", "    AAA        DEEED        AAA    ", "               C   C               ")
                    .aisle("               CDDDC               ", "   AAAAA       AEEEA       AAAAA   ", "A  F   F  AABBBB   BBBBAA  F   F  A", "B  C   C  BBDDDA   ADDDBB  C   C  B", "A  F   F  AABBBB   BBBBAA  F   F  A", "   AAAAA       AEEEA       AAAAA   ", "               CDDDC               ")
                    .aisle("               C   C               ", "   AAAAA       AEEEA       AAAAA   ", "A  F   F  ABDDDA   ADDDBA  F   F  A", "BDDC   CDDCCCCCA   ACCCCCDDC   CDDB", "A  F   F  ABDDDA   ADDDBA  F   F  A", "   AAAAA       AEEEA       AAAAA   ", "               C   C               ")
                    .aisle("               CDDDC               ", "   AAAAA       AEEEA       AAAAA   ", "A  F   F  AABBBB   BBBBAA  F   F  A", "B  C   C  BBDDDA   ADDDBB  C   C  B", "A  F   F  AABBBB   BBBBAA  F   F  A", "   AAAAA       AEEEA       AAAAA   ", "               CDDDC               ")
                    .aisle("               C   C               ", "    AAA        DEEED        AAA    ", "AA  FFF  AA    A   A    AA  FFF  AA", "BB  CCC  BB    A   A    BB  CCC  BB", "AA  FFF  AA    A   A    AA  FFF  AA", "    AAA        DEEED        AAA    ", "               C   C               ")
                    .aisle("                                   ", "               CAAAC               ", " CC     CC     DEEED     CC     CC ", " BB  D  BB     AEEEA     BB  D  BB ", " CC     CC     DEEED     CC     CC ", "               CAAAC               ", "                                   ")
                    .aisle("                                   ", "                                   ", "  CA   AC      CAAAC      CA   AC  ", "  BB D BB      CDDDC      BB D BB  ", "  CA   AC      CAAAC      CA   AC  ", "                                   ", "                                   ")
                    .aisle("                                   ", "                                   ", "   AAAAA                   AAAAA   ", "   BBBBB                   BBBBB   ", "   AAAAA                   AAAAA   ", "                                   ", "                                   ")
                    .where(' ', any())
                    .where("X", controller(blocks(definition.getBlock())))
                    .where('C', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
                    .where('E', heatingCoils())
                    .where('B', blocks(ULTRA_POWERED_CASING.get()))
                    .where('D', blocks(CYCLOZINE_CHEMICALLY_REPELLING_PIPE.get()))
                    .where('F', blocks(HEAT_VENT.get()))
                    .where('A', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()).setMinGlobalLimited(650, 660)
                            .or(abilities(PartAbility.IMPORT_FLUIDS))
                            .or(abilities(PartAbility.EXPORT_FLUIDS))
                            .or(abilities(PartAbility.IMPORT_ITEMS))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                            .or(abilities(PartAbility.COMPUTATION_DATA_RECEPTION).setMaxGlobalLimited(1, 1))
                            .or(abilities(PartAbility.DATA_ACCESS, PartAbility.OPTICAL_DATA_RECEPTION)
                                    .setMaxGlobalLimited(1, 1))
                            .or(abilities(PartAbility.PARALLEL_HATCH, CosmicPartAbility.COSMIC_PARALLEL_HATCH)
                                    .setExactLimit(1))
                            .or(abilities(PartAbility.INPUT_LASER, PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component
                                    .translatable(
                                            FormattingUtil
                                                    .formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) +
                                                    "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            })
            .register();

    public static void init() {}
}
