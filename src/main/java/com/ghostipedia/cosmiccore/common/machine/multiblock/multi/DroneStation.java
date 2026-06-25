package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.world.level.block.Blocks;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

public class DroneStation {

    public final static MultiblockMachineDefinition DRONE_STATION = REGISTRATE
            .multiblock("drone_station",
                    DroneStationMachine::new)
            .langValue("Drone Station")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(CASING_STAINLESS_CLEAN)
            .partAppearance((controller, part, side) -> CASING_STAINLESS_CLEAN.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK))
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice(" AAAAA     ", "           ", "           ", "           ", "           ", "           ",
                            "           ", "           ", "           ", "           ", "           ")
                    .slice("AAAAAAA    ", "B     B    ", "B     B    ", "B  C  B    ", "B CCC B    ", "BCCCCCB    ",
                            "BCCDCCB    ", "BCCCCCB    ", "B CCC B  F ", "B     B F  ", "           ")
                    .slice("AAAAAAAAAA ", "       FFF ", "  CCC  FFF ", "  CEC  FFF ", " CEEEC FFF ", " CEEEC FFF ",
                            " CEEEC     ", " CEEEC   FF", " CEEEC  F  ", "  CCC  F   ", "       F   ")
                    .slice("AAAAAAAAAA ", "       F F ", "  CCC  F F ", " CEEEC F F ", " CEEEC F F ", " CEEEC FFF ",
                            " DEEED  B  ", " CEEEC  BFF", " CEEEC  H  ", "  CCC  F H ", "       F   ")
                    .slice("AAAAAAAAAA ", "       FFF ", "  CCC  FQF ", "  CEC  FFF ", " CEEEC FFF ", " CEEEC FFF ",
                            " CEEEC     ", " CEEEC   FF", " CEEEC  F  ", "  CCC  F   ", "       F   ")
                    .slice("AAAAAAA    ", "B     B    ", "B     B    ", "B  C  B    ", "B CCC B    ", "BCCCCCB    ",
                            "BCCDCCB    ", "BCCCCCB    ", "B CCC B  F ", "B     B F  ", "           ")
                    .slice(" AAAAA     ", "           ", "           ", "           ", "           ", "           ",
                            "           ", "           ", "           ", "           ", "           ")

                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(CASING_STEEL_SOLID.get()))
                    .where('B', frames(GTMaterials.StainlessSteel))
                    .where('C', blocks(CASING_TITANIUM_STABLE.get()))
                    .where('D', blocks(CASING_GRATE.get()))
                    .where('E', blocks(Blocks.HONEYCOMB_BLOCK))
                    .where('F', blocks(CASING_STAINLESS_CLEAN.get())
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1)
                                    .setPreviewCount(1)))
                    .where('H', blocks(CASING_TITANIUM_PIPE.get()))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/power_substation"))
            .register();

    public static void init() {}
}
