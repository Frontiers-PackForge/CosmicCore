package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.behavior.AtmoPumpBehavior;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.COMPUTER_CASING;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

// NOTE DO NOT ADD BERS/RENDERS TO THIS YET

public class AtmoPump {

    public final static MultiblockMachineDefinition ATMO_PUMP = REGISTRATE
            .multiblock("atmo_pump", AtmoPumpBehavior::new)
            .langValue("§6Atmospheric Siphon")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.ATMOSPHERE_SIPHON)
            .appearanceBlock(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING)
            .partAppearance((controller, part, side) -> TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.getDefaultState())
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("         AA AA         ", "                       ", "                       ", "                       ", "         AA AA         ", "                       ", "         AA AA         ", "                       ", "                       ", "                       ", "         AA AA         ")
                    .aisle("         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ", "          B B          ", "         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ")
                    .aisle("          CCC          ", "          D D          ", "          D D          ", "          D D          ", "          CCC          ", "                       ", "          CCC          ", "          D D          ", "          D D          ", "          D D          ", "          CCC          ")
                    .aisle("         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ", "          B B          ", "         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ")
                    .aisle("         AA AA         ", "                       ", "          BEB          ", "                       ", "         AA AA         ", "                       ", "         AA AA         ", "                       ", "          BEB          ", "                       ", "         AA AA         ")
                    .aisle("                       ", "                       ", "          BEB          ", "                       ", "                       ", "                       ", "                       ", "                       ", "          BEB          ", "                       ", "                       ")
                    .aisle("                       ", "                       ", "          BEB          ", "                       ", "                       ", "                       ", "                       ", "                       ", "          BEB          ", "                       ", "                       ")
                    .aisle("         BBBBB         ", "           B           ", "          BBB          ", "           B           ", "                       ", "                       ", "                       ", "           B           ", "          BBB          ", "           B           ", "         BBBBB         ")
                    .aisle("        BBBBBBB        ", "         BDEDB         ", "         BDEDB         ", "         BDEDB         ", "         BEEEB         ", "         BEZEB         ", "         BEEEB         ", "         BDEDB         ", "         BDEDB         ", "         BDEDB         ", "        BBGGGBB        ")
                    .aisle("AA AA  BBBBBBBBB  AA AA", "        B     B        ", "        B     B        ", "        B     B        ", "AA AA   B     B   AA AA", "        B     B        ", "AA AA   B     B   AA AA", "        B     B        ", "        B     B        ", "        B     B        ", "AA AA  BBGGBGGBB  AA AA")
                    .aisle("ABCBA  BBBBBBBBB  ABCBA", " BDB    D B B D    BDB ", " BDBBBBBD B B DBBBBBDB ", " BDB    D B B D    BDB ", "ABCBA   E B B E   ABCBA", " B B    E B B E    B B ", "ABCBA   E B B E   ABCBA", " BDB    D B B D    BDB ", " BDBBBBBD B B DBBBBBDB ", " BDB    D B B D    BDB ", "ABCBA  BGGBBBGGB  ABCBA")
                    .aisle(" CCC   BBBBBBBBB   CCC ", " D D   BE     EB   D D ", " D DEEEBE     EBEEED D ", " D D   BE     EB   D D ", " CCC    E     E    CCC ", "        Z     Z        ", " CCC    E     E    CCC ", " D D   BE     EB   D D ", " D DEEEBE     EBEEED D ", " D D   BE     EB   D D ", " CCC   BGBBQBBGB   CCC ")
                    .aisle("ABCBA  BBBBBBBBB  ABCBA", " BDB    D B B D    BDB ", " BDBBBBBD B B DBBBBBDB ", " BDB    D B B D    BDB ", "ABCBA   E B B E   ABCBA", " B B    E B B E    B B ", "ABCBA   E B B E   ABCBA", " BDB    D B B D    BDB ", " BDBBBBBD B B DBBBBBDB ", " BDB    D B B D    BDB ", "ABCBA  BGGBBBGGB  ABCBA")
                    .aisle("AA AA  BBBBBBBBB  AA AA", "        B     B        ", "        B     B        ", "        B     B        ", "AA AA   B     B   AA AA", "        B     B        ", "AA AA   B     B   AA AA", "        B     B        ", "        B     B        ", "        B     B        ", "AA AA  BBGGBGGBB  AA AA")
                    .aisle("        BBBBBBB        ", "         BDEDB         ", "         BDEDB         ", "         BDEDB         ", "         BEEEB         ", "         BEZEB         ", "         BEEEB         ", "         BDEDB         ", "         BDEDB         ", "         BDEDB         ", "        BBGGGBB        ")
                    .aisle("         BBBBB         ", "           B           ", "          BBB          ", "           B           ", "                       ", "                       ", "                       ", "           B           ", "          BBB          ", "           B           ", "         BBBBB         ")
                    .aisle("                       ", "                       ", "          BEB          ", "                       ", "                       ", "                       ", "                       ", "                       ", "          BEB          ", "                       ", "                       ")
                    .aisle("                       ", "                       ", "          BEB          ", "                       ", "                       ", "                       ", "                       ", "                       ", "          BEB          ", "                       ", "                       ")
                    .aisle("         AA AA         ", "                       ", "          BEB          ", "                       ", "         AA AA         ", "                       ", "         AA AA         ", "                       ", "          BEB          ", "                       ", "         AA AA         ")
                    .aisle("         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ", "          B B          ", "         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ")
                    .aisle("          CCC          ", "          D D          ", "          D D          ", "          D D          ", "          CCC          ", "                       ", "          CCC          ", "          D D          ", "          D D          ", "          D D          ", "          CCC          ")
                    .aisle("         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ", "          B B          ", "         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ")
                    .aisle("         AA AA         ", "                       ", "                       ", "                       ", "         AA AA         ", "                       ", "         AA AA         ", "                       ", "                       ", "                       ", "         AA AA         ")
.where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('C', blocks(HIGH_TOLERANCE_RHENIUM_CASING.get()))
                    .where('B', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()))   //.setMinGlobalLimited(28)
                    .where('D', blocks(CASING_HEAT_VENT.get()))
                    .where('E', blocks(RESONANTLY_TUNED_VIRTUE_MELD_CASING.get()))
                    .where('A', blocks(COMPUTER_CASING.get()))
                    .where('G', blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.ATMOSPHERE_SIPHON))
                            .or(abilities(PartAbility.IMPORT_FLUIDS,PartAbility.IMPORT_FLUIDS_4X,PartAbility.IMPORT_FLUIDS_9X))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('Z',
                            new TraceabilityPredicate(
                                    new SimplePredicate(
                                            state -> MetaMachine.getMachine(state.getWorld(),
                                                    state.getPos()) instanceof IRotorHolderMachine rotorHolder &&
                                                    state.getWorld()
                                                            .getBlockState(state.getPos()
                                                                    .relative(rotorHolder.self().getFrontFacing()))
                                                            .isAir(),
                                            () -> PartAbility.ROTOR_HOLDER.getAllBlocks().stream()
                                                    .map(BlockInfo::fromBlock).toArray(BlockInfo[]::new)))
                                    .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_3"))
                                    .setExactLimit(4))
                    .build())
            // spotless:on
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/tritanium_lined_heavy_neutronium_casing"),
                    CosmicCore.id("block/multiblock/mantle_bore"))
            .register();

    public static void init() {}
}
