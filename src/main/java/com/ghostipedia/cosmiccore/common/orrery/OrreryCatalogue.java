package com.ghostipedia.cosmiccore.common.orrery;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.deployment.LeylineCraftingPattern;
import com.ghostipedia.cosmiccore.common.deployment.LeylineFabricationLibrary;
import com.ghostipedia.cosmiccore.common.deployment.LeylinePrefab;
import com.ghostipedia.cosmiccore.common.machine.part.LeylineMEHatch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;

import java.util.*;

public final class OrreryCatalogue implements IGridService, IGridServiceProvider {

    private final IGrid grid;
    private final Set<LeylineMEHatch> hatches = new HashSet<>();
    private final Set<appeng.api.implementations.blockentities.IWirelessAccessPoint> accessPoints = new HashSet<>();
    private Map<UUID, OrreryDesign> snapshot = Map.of();
    private long refreshedAt = Long.MIN_VALUE;

    public OrreryCatalogue(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public void addNode(IGridNode node, CompoundTag data) {
        if (node.getService(ICraftingProvider.class) instanceof LeylineMEHatch hatch) hatches.add(hatch);
        if (node.getOwner() instanceof appeng.api.implementations.blockentities.IWirelessAccessPoint accessPoint)
            accessPoints.add(accessPoint);
        invalidate();
    }

    @Override
    public void removeNode(IGridNode node) {
        if (node.getService(ICraftingProvider.class) instanceof LeylineMEHatch hatch) hatches.remove(hatch);
        if (node.getOwner() instanceof appeng.api.implementations.blockentities.IWirelessAccessPoint accessPoint)
            accessPoints.remove(accessPoint);
        invalidate();
    }

    public void invalidate() {
        refreshedAt = Long.MIN_VALUE;
    }

    public Collection<appeng.api.implementations.blockentities.IWirelessAccessPoint> accessPoints() {
        return Collections.unmodifiableSet(accessPoints);
    }

    public AEItemKey craftingOutput(UUID design) {
        for (var hatch : hatches) {
            if (!hatch.isOnline()) continue;
            for (var pattern : hatch.getAvailablePatterns()) {
                if (pattern instanceof LeylineCraftingPattern leyline && leyline.prefab().id().equals(design)) {
                    var output = AEItemKey.of(leyline.output());
                    if (grid.getCraftingService().isCraftable(output)) return output;
                }
            }
        }
        return null;
    }

    public Map<UUID, OrreryDesign> designs(ServerLevel level) {
        long now = level.getServer().getTickCount();
        if (refreshedAt != Long.MIN_VALUE && now - refreshedAt < 20) return snapshot;
        refreshedAt = now;
        var known = new HashSet<UUID>();
        var craftable = new HashSet<UUID>();
        for (var hatch : hatches) {
            known.addAll(hatch.registeredDesigns());
            if (hatch.isOnline()) for (var pattern : hatch.getAvailablePatterns())
                if (pattern instanceof LeylineCraftingPattern leyline) craftable.add(leyline.prefab().id());
        }
        var counts = new HashMap<UUID, Long>();
        for (var entry : grid.getStorageService().getCachedInventory()) {
            if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey key) ||
                    key.getItem() != CosmicItems.LEYLINE_PACKAGE.get())
                continue;
            UUID id = LeylinePrefab.reference(key.toStack());
            if (id != null)
                counts.merge(id, entry.getLongValue(), (a, b) -> a > Long.MAX_VALUE - b ? Long.MAX_VALUE : a + b);
        }
        var newlyStored = new HashSet<>(counts.keySet());
        newlyStored.removeAll(known);
        hatches.stream()
                .min(Comparator.comparing((LeylineMEHatch hatch) -> hatch.getLevel().dimension().location().toString())
                        .thenComparingLong(hatch -> hatch.getBlockPos().asLong()))
                .ifPresent(hatch -> hatch.rememberDesigns(newlyStored));
        known.addAll(counts.keySet());
        var library = LeylineFabricationLibrary.get(level);
        var result = new HashMap<UUID, OrreryDesign>();
        for (var id : known) {
            var prefab = library.find(id);
            if (prefab != null)
                result.put(id, OrreryDesign.of(prefab, counts.getOrDefault(id, 0L), craftable.contains(id)));
        }
        snapshot = Map.copyOf(result);
        return snapshot;
    }
}
