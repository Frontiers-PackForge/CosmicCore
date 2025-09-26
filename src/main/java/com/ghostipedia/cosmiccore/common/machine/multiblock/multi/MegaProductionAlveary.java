package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.world.level.block.Blocks;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

public class MegaProductionAlveary {

    public final static MultiblockMachineDefinition MEGA_PRODUCTION_ALVEARY = REGISTRATE
            .multiblock("honey_alveary",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§eHyper Optimized Nectar Extraction Yard [HONEY]")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES) // IDK HOW WE GET RID OF THIS? I THOUGHT WE COULD!
            .appearanceBlock(CASING_STEEL_SOLID)
            .partAppearance((controller, part, side) -> CASING_STEEL_SOLID.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start(RelativeDirection.LEFT,RelativeDirection.UP,RelativeDirection.FRONT)
                    .aisle("           AAAAA    ", "           AAAAA    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ")
                    .aisle("         AAAAAAAAA  ", "         AAAAAAAAA  ", "           AAAAA    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ")
                    .aisle("        AAAAAAAAAAA ", "        AAAAAAAAAAA ", "         AAAAAAAAA  ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ")
                    .aisle("AAAAAAA AAAAAAAAAAA ", "AAAAAAA AAAAAAAAAAA ", "  B B    AAAAAAAAA  ", "  B B               ", "  B B               ", "  B B               ", "  B B               ", "BBBBBBB             ", "  B B               ", "  B B               ", "  B B               ", "  B B               ", "  B B               ", "BBBBBBB             ", "  B B               ", "  B B               ", "                    ", "                    ", "                    ")
                    .aisle("ACCCCCAAAAAAAAAAAAAA", "ACCCCCAAAAAAAAAAAAAA", " CCCCC  AAAAAAAAAAA ", " CCCCC              ", " CCCCC    DDDDDDD   ", " CCCCC    DEEEEED   ", " CCCCC    DDDDDDD   ", "BCCCCCB  B       B  ", " CCCCC    DDDDDDD   ", " CCCCC    DEEEEED   ", " CCCCC    DDDDDDD   ", " CCCCC   B       B  ", " CCCCC    DDDDDDD   ", "BCCCCCB   DEEEEED   ", " CCCCC    DDDDDDD   ", " CCCCC   B       B  ", "          DDDDDDD   ", "          DEEEEED   ", "          DDDDDDD   ")
                    .aisle("ACCCCCAAAAAAAAAAAAAA", "ACCCCCAAAAAAAAAAAAAA", " CCCCC  AAAAAAAAAAA ", " CCCCC   B       B  ", " CCCCC   BDDDDDDDB  ", " CCCCC   BEFFFFFEB  ", " CCCCC   BDDDDDDDB  ", "BCCCCCBBBBBBBBBBBB  ", " CCCCC   BDDDDDDDB  ", " CCCCC   BEFFFFFEB  ", " CCCCC   BDDDDDDDB  ", " CCCCC   BBBBBBBBB  ", " CCCCC   BDDDDDDDB  ", "BCCCCCBBBBEFFFFFEB  ", " CCCCC   BDDDDDDDB  ", " CCCCC   BBBBBBBBB  ", "         BDDDDDDDB  ", "         BEFFFFFEB  ", "         BDDDDDDDB  ")
                    .aisle("ACCCCCAAAAAAAAAAAAAA", "ACCCCCAAAAAAAAAAAAAA", " CCCCC  AAAAAAAAAAA ", " CCCCC              ", " CCCCC    DDDDDDD   ", " CCCCC    EFFFFFE   ", " CCCCC    DDDDDDD   ", "BCCCCCB  B       B  ", " CCCCC    DDDDDDD   ", " CCCCC    EFFFFFE   ", " CCCCC    DDDDDDD   ", " CCCCC   B       B  ", " CCCCC    DDDDDDD   ", "BCCCCCB   EFFFFFE   ", " CCCCC    DDDDDDD   ", " CCCCC   B       B  ", "          DDDDDDD   ", "          EFFFFFE   ", "          DDDDDDD   ")
                    .aisle("ACCCCCAAAAAAAAAAAAAA", "ACCCCCAAAAAAAAAAAAAA", " CCCCC  AAAAAAAAAAA ", " CCCCC   B       B  ", " CCCCC   BDDDDDDDB  ", " CCCCC   BEFFFFFEB  ", " CCCCC   BDDDDDDDB  ", "BCCCCCBBBBBBBBBBBB  ", " CCCCC   BDDDDDDDB  ", " CCCCC   BEFFFFFEB  ", " CCCCC   BDDDDDDDB  ", " CCCCC   BBBBBBBBB  ", " CCCCC   BDDDDDDDB  ", "BCCCCCBBBBEFFFFFEB  ", " CCCCC   BDDDDDDDB  ", " CCCCC   BBBBBBBBB  ", "         BDDDDDDDB  ", "         BEFFFFFEB  ", "         BDDDDDDDB  ")
                    .aisle("ACCCCCAAAAAAAAAAAAAA", "ACCCCCAAAAAAAAAAAAAA", " CCCCC  AAAAAAAAAAA ", " CCCCC              ", " CCCCC    DDDDDDD   ", " CCCCC    DGGEGGD   ", " CCCCC    DDDDDDD   ", "BCCCCCB  B       B  ", " CCCCC    DDDDDDD   ", " CCCCC    DGGEGGD   ", " CCCCC    DDDDDDD   ", " CCCCC   B       B  ", " CCCCC    DDDDDDD   ", "BCCCCCB   DGGEGGD   ", " CCCCC    DDDDDDD   ", " CCCCC   B       B  ", "          DDDDDDD   ", "          DGGEGGD   ", "          DDDDDDD   ")
                    .aisle("AAAAAAAAAAAAAAAAAAA ", "AAAAAAAAAAAAAAAAAAA ", "  B B    AAAAAAAAA  ", "  B B               ", "  B B               ", "  B B               ", "  B B               ", "BBBBBBB             ", "  B B               ", "  B B               ", "  B B               ", "  B B               ", "  B B               ", "BBBBBBB             ", "  B B               ", "  B B               ", "                    ", "                    ", "                    ")
                    .aisle("        AAAAAAAAAAA ", "        AAAAAAAAAAA ", "         AAAAAAAAA  ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ")
                    .aisle("         AAAAAAAAA  ", "         AAAAAAAAA  ", "           AAAAA    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ")
                    .aisle("           AAAAA    ", "           AAQAA    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ", "                    ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.INPUT_ENERGY,PartAbility.INPUT_LASER).setExactLimit(1)) // Can take laser for when expandable vertically (NYI until V8)
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1,1).setMaxGlobalLimited(3)) // For Nutrient Boosters
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1,1).setMaxGlobalLimited(3)) // For Exporting the Products
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1,1).setMaxGlobalLimited(3)) //If we want to do some output waste? idk
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', frames(GTMaterials.Neutronium))
                    .where('C', blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get()))
                    .where('D', blocks(REINFORCED_DAWNSTONE_CASING.get())) // Frame of the hives
                    .where('E', blocks(CASING_GRATE.get())) // GRATES
                    .where('F', blocks(Blocks.HONEYCOMB_BLOCK)) // THE COMBS YAYYYY
                    .where('G', ability(CosmicPartAbility.BEE_HOLDER)) // The Bee Holders
                    .build())
            // spotless:on
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/power_substation"))
            .register();

    public static void init() {}
}
