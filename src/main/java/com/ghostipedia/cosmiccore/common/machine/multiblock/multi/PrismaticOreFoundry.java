package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.CosmicCoilBlock;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.ghostipedia.cosmiccore.utils.StringUtil;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

// NOTE DO NOT ADD BERS/RENDERS TO THIS YET

public class PrismaticOreFoundry {

    public final static MultiblockMachineDefinition PRISMATIC_ORE_FOUNDRY = REGISTRATE
            .multiblock("prismatic_ore_foundry",
                    WorkableElectricMultiblockMachine::new)
            .langValue(StringUtil
                    .rainbowDancing("Prismatic Ore Factory"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.PRISMA_FOUNDRY)
            .appearanceBlock(SELF_HEALING_PTHANTERUM)
            .partAppearance((controller, part, side) -> SELF_HEALING_PTHANTERUM.getDefaultState())
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.UP,RelativeDirection.FRONT)
                    .aisle(" NNNNNNNNNNNNNNNN ", " NHHHHNHHHHHNNNNN ", " NHHHHNHHHHHNNNNN ", " NNNNNNNNNNNNNNNN ", "  N   N     N   N ", "  N   N     N   N ", "  N   N     N   N ", "  NNNNN     NNNNN ", "                  ", "                  ", "                  ", "                  ", "                  ", "                  ")
                    .aisle(" NNNNNNNNNNNNNNNN ", " H              N ", " HKKKKKKKKKKKKKKN ", " NN   NHHHHHHIIIN ", "   OOO       III  ", "   OOO       III  ", "   OOO       III  ", "  NHNHN     NNNNN ", "                  ", "                  ", "                  ", "                  ", "                  ", "                  ")
                    .aisle(" NNNNNNNNNNNNNNNN ", " HKKKKKKKKKKKKKKN ", " H              N ", " NN   NHHHHHHI IN ", "   O O       I I  ", "   O O       I I  ", "   O O       I I  ", "  NNINN     NNNNN ", "    I             ", "    I             ", "    I             ", "                  ", "                  ", "                  ")
                    .aisle(" NNNNNNNNNNNNNNNN ", " H              N ", " HKKKKKKKKKKKKKKN ", " NN   NHHHHHHIIIN ", "   OOO       III  ", "   OOO       III  ", "   OOO       III  ", "  NHNHN     NNNNN ", "                  ", "                  ", "    I             ", "                  ", "                  ", "                  ")
                    .aisle(" NNNNNNNNNNNNNNNN ", " NHHHHNHHHHHNNNNN ", " NHHHHNHHHHHNNNNN ", " NNNNNNNNNNNNNNNN ", "  N   N     N   N ", "  N   N     N I N ", "  N   N     N   N ", "  NNNNN     NNNNN ", "                  ", "                  ", "    I             ", "                  ", "                  ", "                  ")
                    .aisle("                  ", "                  ", "                  ", "                  ", "             M M  ", "              I   ", "   M M       M M  ", "                  ", "                  ", "                  ", "    I             ", "                  ", "                  ", "                  ")
                    .aisle("  AAAAA           ", "  AAAAA           ", "  AAAAA           ", "  AAAAA           ", "             M M  ", "  AAAAA       I   ", "  AAAAA      M M  ", "  AAAAA           ", "                  ", "  AAAAA           ", "  AAIAA           ", "  AAAAA           ", "                  ", "                  ")
                    .aisle(" ABBBBBA     CCC  ", " ABBBBBA     CCC  ", " ABBBBBA          ", " ABBBBBA          ", "  DDDDD      M M  ", " ABBBBBA      I   ", " ABBBBBA     M M  ", " ABBBBBA          ", "  DDDDD      CCC  ", " ABBBBBA     CCC  ", " ABBBBBA          ", " ABBBBBA          ", "  BBBBB           ", "   B B            ")
                    .aisle("ABBBBBBBA   CEEEC ", "ABF   FBA   CEEEC ", "ABFGGGFBA   CEHEC ", "ABF   FBA   CEHEC ", " DD   DD    CEEEC ", "ABF   FBA   CEIEC ", "ABFGGGFBA   CEEEC ", "ABF   FBA   CEHEC ", " DD   DD    CEEEC ", "ABF   FBA   CCCCC ", "ABFGGGFBA         ", "ABF   FBA         ", " B     B          ", "                  ")
                    .aisle("ABBBBBBBA ICEEEEEC", "AB  J  BA ICE   EC", "ABG   GBA I EKKKE ", "AB  J  BA I E   E ", " D     D  I EKKKE ", "AB  J  BA I E   E ", "ABG   GBA I EKKKE ", "AB  J  BA I E   E ", " D     D  ICEKKKEC", "AB  J  BA ICCCCCCC", "ABG   GBA I       ", "AB  J  BA I       ", " B     IIII       ", " B     B          ")
                    .aisle("ABBBBBBBA  CEEEEEC", "AB JJJ BA  CE   EC", "ABG J GBALL H   H ", "AB JJJ BA   HKKKH ", " D     D    H   H ", "AB JJJ BA   HKKKH ", "ABG J GBALL H   H ", "AB JJJ BA   HKKKH ", " D     D   CE   EC", "AB JJJ BA  CCCCCCC", "ABG J GBALL       ", "AB JJJ BA         ", " B     B          ", "                  ")
                    .aisle("ABBBBBBBA ICEEEEEC", "AB  J  BA ICE   EC", "ABG   GBA I EKKKE ", "AB  J  BA I E   E ", " D     D  I EKKKE ", "AB  J  BA I E   E ", "ABG   GBA I EKKKE ", "AB  J  BA I E   E ", " D     D  ICEKKKEC", "AB  J  BA ICCCCCCC", "ABG   GBA I       ", "AB  J  BA I       ", " B     IIII       ", " B     B          ")
                    .aisle("ABBBBBBBA   CEEEC ", "ABF   FBA   CEEEC ", "ABFGGGFBA   CEHEC ", "ABF   FBA   CEHEC ", " DD   DD    CEHEC ", "ABF   FBA   CEHEC ", "ABFGGGFBA   CEHEC ", "ABF   FBA   CEHEC ", " DD   DD    CEEEC ", "ABF   FBA   CCCCC ", "ABFGGGFBA         ", "ABF   FBA         ", " B     B          ", "                  ")
                    .aisle(" ABBBBBA     CCC  ", " ABBBBBA     CQC  ", " ABBBBBA          ", " ABBBBBA          ", "  DDDDD           ", " ABBBBBA          ", " ABBBBBA          ", " ABBBBBA          ", "  DDDDD      CCC  ", " ABBBBBA     CCC  ", " ABBBBBA          ", " ABBBBBA          ", "  BBBBB           ", "   B B            ")
                    .aisle("  AAAAA           ", "  AAAAA           ", "  AAAAA           ", "  AAAAA           ", "                  ", "  AAAAA           ", "  AAAAA           ", "  AAAAA           ", "                  ", "  AAAAA           ", "  AAAAA           ", "  AAAAA           ", "                  ", "                  ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(GCYMBlocks.CASING_REACTION_SAFE.get()))
                    .where('B', blocks(GCYMBlocks.CASING_WATERTIGHT.get()))   //.setMinGlobalLimited(28)
                    .where('C', blocks(SELF_HEALING_PTHANTERUM.get())
                            .or(autoAbilities(CosmicRecipeTypes.VOID_MINER))
                            .or(abilities(PartAbility.IMPORT_FLUIDS,PartAbility.IMPORT_FLUIDS_4X,PartAbility.IMPORT_FLUIDS_9X))
                            .or(abilities(PartAbility.EXPORT_ITEMS,PartAbility.IMPORT_ITEMS))
                            .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.INPUT_LASER).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('D', blocks(GCYMBlocks.CASING_CORROSION_PROOF.get()))
                    .where('E', blocks(WEAR_RESISTANT_RURIDIT_CASING.get()))
                    .where('F', blocks(CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .where('G', blocks(CASING_TITANIUM_PIPE.get()))
                    .where('H', blocks(ZBLAN_REINFORCED_GLASS.get()))
                    .where('I', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()))
                    .where('J', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('K', blocks(GEARBOX_PTHANTERUM.get()))
                    .where('L', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.TungstenCarbide)))
                    .where('M', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Neutronium)))
                    .where('N', blocks(CASING_STAINLESS_CLEAN.get()))
                    .where('O', blocks(COIL_TRANAVINE.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/self_healing_pthanterum_casing"),
                    GTCEu.id("block/multiblock/generator/large_gas_turbine"))
            .register();

    public static void init() {}
}
