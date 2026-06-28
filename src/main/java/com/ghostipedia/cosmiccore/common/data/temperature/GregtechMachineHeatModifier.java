package com.ghostipedia.cosmiccore.common.data.temperature;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import sfiomn.legendarysurvivaloverhaul.api.temperature.ModifierBase;

public class GregtechMachineHeatModifier extends ModifierBase {

    private static final int RADIUS = 8;
    private static final double EU_REFERENCE = 8.0;
    private static final double HEAT_PER_MACHINE = 0.15;
    private static final double MAX_HEAT = 2.0;

    @Override
    public float getWorldInfluence(Player player, Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return 0.0f;
        }
        double total = 0.0;
        double radiusSq = (double) RADIUS * RADIUS;
        int minChunkX = (pos.getX() - RADIUS) >> 4;
        int maxChunkX = (pos.getX() + RADIUS) >> 4;
        int minChunkZ = (pos.getZ() - RADIUS) >> 4;
        int maxChunkZ = (pos.getZ() + RADIUS) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = server.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockPos machinePos : chunk.getBlockEntities().keySet()) {
                    double distSq = machinePos.distSqr(pos);
                    if (distSq > radiusSq) {
                        continue;
                    }
                    long eut = workingMachineEut(server, machinePos);
                    if (eut <= 0L) {
                        continue;
                    }
                    double falloff = Math.max(0.0, 1.0 - Math.sqrt(distSq) / RADIUS);
                    total += HEAT_PER_MACHINE * Math.log10(1.0 + eut / EU_REFERENCE) * falloff;
                }
            }
        }
        return (float) Math.min(MAX_HEAT, total);
    }

    private static long workingMachineEut(ServerLevel level, BlockPos pos) {
        if (MetaMachine.getMachine(level, pos) instanceof IRecipeLogicMachine machine) {
            RecipeLogic logic = machine.getRecipeLogic();
            if (logic != null && logic.isWorking()) {
                GTRecipe recipe = logic.getLastRecipe();
                if (recipe != null) {
                    EnergyStack eut = RecipeHelper.getRealEUt(recipe);
                    return eut == null ? 0L : eut.getTotalEU();
                }
            }
        }
        return 0L;
    }
}
