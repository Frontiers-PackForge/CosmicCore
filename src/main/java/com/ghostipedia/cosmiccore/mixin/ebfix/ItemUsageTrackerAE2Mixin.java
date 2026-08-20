package com.ghostipedia.cosmiccore.mixin.ebfix;

import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingAE2Bridge;
import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingOperationExecutor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import neoforge.nl.requios.effortlessbuilding.utilities.ItemUsageTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

@Mixin(ItemUsageTracker.class)
public abstract class ItemUsageTrackerAE2Mixin {

    @Shadow
    public Map<Item, Integer> total;
    @Shadow
    public Map<Item, Integer> inInventory;
    @Shadow
    public Map<Item, Integer> placed;
    @Shadow
    public Map<Item, Integer> missing;
    @Shadow
    public Set<BlockPos> missingPositions;

    @Inject(method = "compute", at = @At("TAIL"))
    private void cosmiccore$includeAE2InPreview(
                                                Player player, Collection<BlockPos> positions, Item heldItem,
                                                boolean creative, CallbackInfo ci) {
        if (creative || heldItem == null || positions.isEmpty()) return;
        boolean exactStackOnly = player.getMainHandItem().is(heldItem) &&
                !player.getMainHandItem().getComponentsPatch().isEmpty();
        int inventory = exactStackOnly ?
                EffortlessBuildingOperationExecutor.countExact(player, player.getMainHandItem()) :
                inInventory.getOrDefault(heldItem, 0);
        int network = 0;
        if (!exactStackOnly) {
            EffortlessBuildingAE2Bridge.requestClientCount(heldItem);
            network = EffortlessBuildingAE2Bridge.cachedClientCount(heldItem);
        }
        int available = EffortlessBuildingAE2Bridge.saturatingAdd(inventory, network);
        int wanted = total.getOrDefault(heldItem, positions.size());
        int canPlace = Math.min(wanted, available);
        inInventory.put(heldItem, available);
        placed.put(heldItem, canPlace);
        missingPositions.clear();
        if (wanted <= available) {
            missing.remove(heldItem);
            return;
        }

        int missingCount = wanted - available;
        missing.put(heldItem, missingCount);
        ArrayList<BlockPos> sorted = new ArrayList<>(positions);
        BlockPos playerPos = player.blockPosition();
        sorted.sort(Comparator.comparingDouble(pos -> pos.distSqr(playerPos)));
        int firstMissing = Math.max(0, sorted.size() - Math.min(missingCount, sorted.size()));
        for (int index = firstMissing; index < sorted.size(); index++) {
            missingPositions.add(sorted.get(index));
        }
    }
}
