package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.behavior.AtmoPumpBehavior;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.COMPUTER_CASING;
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
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("         AA AA         ", "                       ", "                       ", "                       ", "         AA AA         ", "                       ", "         AA AA         ", "                       ", "                       ", "                       ", "         AA AA         ")
                    .slice("         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ", "          B B          ", "         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ")
                    .slice("          CCC          ", "          D D          ", "          D D          ", "          D D          ", "          CCC          ", "                       ", "          CCC          ", "          D D          ", "          D D          ", "          D D          ", "          CCC          ")
                    .slice("         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ", "          B B          ", "         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ")
                    .slice("         AA AA         ", "                       ", "          BEB          ", "                       ", "         AA AA         ", "                       ", "         AA AA         ", "                       ", "          BEB          ", "                       ", "         AA AA         ")
                    .slice("                       ", "                       ", "          BEB          ", "                       ", "                       ", "                       ", "                       ", "                       ", "          BEB          ", "                       ", "                       ")
                    .slice("                       ", "                       ", "          BEB          ", "                       ", "                       ", "                       ", "                       ", "                       ", "          BEB          ", "                       ", "                       ")
                    .slice("         BBBBB         ", "           B           ", "          BBB          ", "           B           ", "                       ", "                       ", "                       ", "           B           ", "          BBB          ", "           B           ", "         BBBBB         ")
                    .slice("        BBBBBBB        ", "         BDEDB         ", "         BDEDB         ", "         BDEDB         ", "         BEEEB         ", "         BEZEB         ", "         BEEEB         ", "         BDEDB         ", "         BDEDB         ", "         BDEDB         ", "        BBGGGBB        ")
                    .slice("AA AA  BBBBBBBBB  AA AA", "        B     B        ", "        B     B        ", "        B     B        ", "AA AA   B     B   AA AA", "        B     B        ", "AA AA   B     B   AA AA", "        B     B        ", "        B     B        ", "        B     B        ", "AA AA  BBGGBGGBB  AA AA")
                    .slice("ABCBA  BBBBBBBBB  ABCBA", " BDB    D B B D    BDB ", " BDBBBBBD B B DBBBBBDB ", " BDB    D B B D    BDB ", "ABCBA   E B B E   ABCBA", " B B    E B B E    B B ", "ABCBA   E B B E   ABCBA", " BDB    D B B D    BDB ", " BDBBBBBD B B DBBBBBDB ", " BDB    D B B D    BDB ", "ABCBA  BGGBBBGGB  ABCBA")
                    .slice(" CCC   BBBBBBBBB   CCC ", " D D   BE     EB   D D ", " D DEEEBE     EBEEED D ", " D D   BE     EB   D D ", " CCC    E     E    CCC ", "        Z     Z        ", " CCC    E     E    CCC ", " D D   BE     EB   D D ", " D DEEEBE     EBEEED D ", " D D   BE     EB   D D ", " CCC   BGBBQBBGB   CCC ")
                    .slice("ABCBA  BBBBBBBBB  ABCBA", " BDB    D B B D    BDB ", " BDBBBBBD B B DBBBBBDB ", " BDB    D B B D    BDB ", "ABCBA   E B B E   ABCBA", " B B    E B B E    B B ", "ABCBA   E B B E   ABCBA", " BDB    D B B D    BDB ", " BDBBBBBD B B DBBBBBDB ", " BDB    D B B D    BDB ", "ABCBA  BGGBBBGGB  ABCBA")
                    .slice("AA AA  BBBBBBBBB  AA AA", "        B     B        ", "        B     B        ", "        B     B        ", "AA AA   B     B   AA AA", "        B     B        ", "AA AA   B     B   AA AA", "        B     B        ", "        B     B        ", "        B     B        ", "AA AA  BBGGBGGBB  AA AA")
                    .slice("        BBBBBBB        ", "         BDEDB         ", "         BDEDB         ", "         BDEDB         ", "         BEEEB         ", "         BEZEB         ", "         BEEEB         ", "         BDEDB         ", "         BDEDB         ", "         BDEDB         ", "        BBGGGBB        ")
                    .slice("         BBBBB         ", "           B           ", "          BBB          ", "           B           ", "                       ", "                       ", "                       ", "           B           ", "          BBB          ", "           B           ", "         BBBBB         ")
                    .slice("                       ", "                       ", "          BEB          ", "                       ", "                       ", "                       ", "                       ", "                       ", "          BEB          ", "                       ", "                       ")
                    .slice("                       ", "                       ", "          BEB          ", "                       ", "                       ", "                       ", "                       ", "                       ", "          BEB          ", "                       ", "                       ")
                    .slice("         AA AA         ", "                       ", "          BEB          ", "                       ", "         AA AA         ", "                       ", "         AA AA         ", "                       ", "          BEB          ", "                       ", "         AA AA         ")
                    .slice("         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ", "          B B          ", "         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ")
                    .slice("          CCC          ", "          D D          ", "          D D          ", "          D D          ", "          CCC          ", "                       ", "          CCC          ", "          D D          ", "          D D          ", "          D D          ", "          CCC          ")
                    .slice("         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ", "          B B          ", "         ABCBA         ", "          BDB          ", "          BDB          ", "          BDB          ", "         ABCBA         ")
                    .slice("         AA AA         ", "                       ", "                       ", "                       ", "         AA AA         ", "                       ", "         AA AA         ", "                       ", "                       ", "                       ", "         AA AA         ")
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
                            // 8.0.0 migration: SimplePredicate removed -> BasePredicate; mirrors stock
                            // GTMachineUtils#rotorHolder. IRotorHolderMachine -> RotorHolderPartMachine.
                            new PatternPredicate(
                                    new BasePredicate(
                                            worldState -> MetaMachine.getMachine(worldState.getLevel(),
                                                    worldState.getPos().immutable()) instanceof
                                                    RotorHolderPartMachine rotorHolder &&
                                                    worldState.getLevel()
                                                            .getBlockState(worldState.getPos().immutable()
                                                                    .relative(rotorHolder.self().getFrontFacing()))
                                                            .isAir() ? null : PLACEHOLDER,
                                            PartAbility.ROTOR_HOLDER.getAllBlocks().stream()
                                                    .map(BlockInfo::fromBlock).toList()))
                                    .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_3"))
                                    .setExactLimit(4))
                    .build())
            // spotless:on
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/tritanium_lined_heavy_bolted_neutronium_casing"),
                    CosmicCore.id("block/multiblock/mantle_bore"))
            .register();

    public static void init() {}
}
