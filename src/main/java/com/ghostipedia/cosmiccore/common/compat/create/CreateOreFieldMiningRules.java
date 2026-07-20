package com.ghostipedia.cosmiccore.common.compat.create;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class CreateOreFieldMiningRules {

    public static final int BASE_FLUID_COST = 100;
    public static final int LINEAR_ACTOR_LIMIT = 16;
    public static final float YIELD_CHANCE = 0.5f;

    private static final int SURCHARGE_DENOMINATOR = LINEAR_ACTOR_LIMIT * LINEAR_ACTOR_LIMIT;
    private static final Map<Contraption, Integer> MINING_ACTOR_COUNTS = Collections
            .synchronizedMap(new WeakHashMap<>());

    private CreateOreFieldMiningRules() {}

    public static int fluidCost(MovementContext context) {
        int actorCount = MINING_ACTOR_COUNTS.computeIfAbsent(
                context.contraption, CreateOreFieldMiningRules::countMiningActors);
        int excessActors = Math.max(0, actorCount - LINEAR_ACTOR_LIMIT);
        long surchargeNumerator = (long) BASE_FLUID_COST * excessActors * excessActors;
        long surcharge = (surchargeNumerator + SURCHARGE_DENOMINATOR - 1) / SURCHARGE_DENOMINATOR;
        return (int) Math.min(Integer.MAX_VALUE, BASE_FLUID_COST + surcharge);
    }

    public static boolean hasFluid(MovementContext context) {
        return drainFluid(context, IFluidHandler.FluidAction.SIMULATE);
    }

    public static boolean consumeFluid(MovementContext context) {
        if (!hasFluid(context)) return false;
        return drainFluid(context, IFluidHandler.FluidAction.EXECUTE);
    }

    private static boolean drainFluid(MovementContext context, IFluidHandler.FluidAction action) {
        int amount = fluidCost(context);
        var drained = context.contraption.getStorage().getFluids()
                .drain(GTMaterials.DrillingFluid.getFluid(amount), action);
        return drained.getAmount() == amount;
    }

    private static int countMiningActors(Contraption contraption) {
        return (int) contraption.getActors().stream()
                .filter(actor -> {
                    if (AllBlocks.MECHANICAL_DRILL.has(actor.getLeft().state())) return true;
                    if (!AllBlocks.DEPLOYER.has(actor.getLeft().state())) return false;
                    var blockEntityData = actor.getLeft().nbt();
                    return blockEntityData != null && "PUNCH".equalsIgnoreCase(blockEntityData.getString("Mode"));
                })
                .count();
    }
}
