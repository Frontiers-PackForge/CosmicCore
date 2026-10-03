package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.BronzeSteamTurbineMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.STEEL_PLATED_BRONZE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public final class BronzeSteamTurbine {

    public static final MultiblockMachineDefinition DEFINITION = REGISTRATE
            .multiblock("bronze_steam_turbine", BronzeSteamTurbineMachine::new)
            .langValue("Bronze Steam Turbine")
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.STEAM_TURBINE_FUELS)
            .regressWhenWaiting(false)
            .generator(true)
            .recipeModifier(BronzeSteamTurbineMachine::recipeModifier, true)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice(" A A ", " AFA ", " A A ", "     ")
                    .slice("BBBBB", "BBBBB", "BBBBB", " A A ")
                    .slice("BBBBB", "CDDDE", "BBBBB", " A A ")
                    .slice("BBBBB", "BBBBB", "BBBBB", " A A ")
                    .slice(" A A ", " A A ", " A A ", "     ")
                    .where(' ', any())
                    .where('A', blocks(STEEL_PLATED_BRONZE.get()))
                    .where('B', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1)))
                    .where('C', ability(PartAbility.OUTPUT_ENERGY,
                            GTValues.tiersBetween(GTValues.LV, GTValues.MAX)).setExactLimit(1))
                    .where('D', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                    .where('E', builder("LV+ Outward Rotor Holder")
                            .predicate(ctx -> MetaMachine.getMachine(ctx.level(),
                                    ctx.pos()) instanceof RotorHolderPartMachine rotorHolder &&
                                    rotorHolder.getTier() >= GTValues.LV &&
                                    rotorHolder.isFrontFaceFree())
                            .candidates(PartAbility.ROTOR_HOLDER.getAllBlocks().stream().map(BlockInfo::fromBlock))
                            .toMultiPredicate()
                            .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_3"))
                            .setExactLimit(1))
                    .where('F', controller(blocks(definition.getBlock())))
                    .build())
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/steel_plated_bronze_casing"),
                    GTCEu.id("block/multiblock/generator/large_steam_turbine"))
            .tooltips(
                    Component.translatable("gtceu.universal.tooltip.base_production_eut", 128),
                    Component.translatable("cosmiccore.machine.bronze_steam_turbine.tooltip.0"),
                    Component.translatable("cosmiccore.machine.bronze_steam_turbine.tooltip.1"))
            .register();

    private BronzeSteamTurbine() {}

    public static void init() {}
}
