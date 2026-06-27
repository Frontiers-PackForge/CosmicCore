package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.CosmicCoreAPI;
import com.ghostipedia.cosmiccore.api.block.IMagnetType;
import com.ghostipedia.cosmiccore.common.block.MagnetBlock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;

import net.minecraft.network.chat.Component;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class MagnetWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private IMagnetType magnetType = MagnetBlock.MagnetType.HIGH_POWERED;

    public MagnetWorkableElectricMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        // Re-derive magnet type post-formation (match-context accumulator removed in 8.0.0);
        // mixed magnet types are rejected, mirroring GTCEu's heating-coil consistency check.
        IMagnetType found = null;
        var cache = patternStates.get(substructureName).getCache();
        for (var entry : cache.long2ObjectEntrySet()) {
            var state = entry.getValue().getBlockState();
            for (var coilEntry : CosmicCoreAPI.MAGNET_COILS.entrySet()) {
                if (state.is(coilEntry.getValue().get())) {
                    if (found == null) {
                        found = coilEntry.getKey();
                    } else if (found != coilEntry.getKey()) {
                        patternStates.get(substructureName).setError(
                                new PatternStringError(
                                        Component.translatable("gtceu.multiblock.pattern.error.coils")));
                        invalidateStructure(substructureName);
                        return;
                    }
                    break;
                }
            }
        }
        if (found != null) {
            this.magnetType = found;
        }
    }

    public int getMagnetStrength() {
        return magnetType.getMagnetFieldCapacity();
    }

    public int getMagnetRegen() {
        return magnetType.getMagnetRegenRate();
    }

    public int getEnergyCost() {
        return magnetType.energyConsumption();
    }
}
