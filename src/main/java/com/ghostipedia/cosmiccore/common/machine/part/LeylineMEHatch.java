package com.ghostipedia.cosmiccore.common.machine.part;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.deployment.LeylineCraftingPattern;
import com.ghostipedia.cosmiccore.common.deployment.LeylinePrefab;
import com.ghostipedia.cosmiccore.common.machine.multiblock.LeylineCompressorMachine;
import com.ghostipedia.cosmiccore.common.orrery.OrreryCatalogue;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.*;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.*;

import java.util.*;

public final class LeylineMEHatch extends MEBusPartMachine implements ICraftingProvider {

    private List<IPatternDetails> patterns = List.of();
    private boolean refresh = true;
    private boolean formed;
    private boolean pendingMigration;
    @SaveField
    private net.minecraft.nbt.CompoundTag catalogue = new net.minecraft.nbt.CompoundTag();

    public LeylineMEHatch(BlockEntityCreationInfo info) {
        super(info, IO.IN, new NotifiableItemStackHandler(36, IO.NONE, IO.NONE));
        getInventory().setFilter(stack -> stack.is(CosmicItems.LEYLINE_PATTERN.get()));
        getInventory().addChangedListener(() -> refresh = true);
        getMainNode().addService(ICraftingProvider.class, this);
    }

    @Override
    public boolean canShared(MultiblockControllerMachine controller, String name) {
        return false;
    }

    private LeylineCompressorMachine compressor() {
        for (var controller : getControllers())
            if (controller instanceof LeylineCompressorMachine compressor && compressor.isFormed()) return compressor;
        return null;
    }

    @Override
    protected void autoIO() {
        var compressor = compressor();
        boolean nowFormed = compressor != null;
        var grid = getMainNode().getGrid();
        boolean canMigrate = grid != null && !grid.getCraftingService().isRequestingAny();
        if (refresh || formed != nowFormed || pendingMigration && canMigrate) {
            formed = nowFormed;
            pendingMigration = false;
            var next = new ArrayList<IPatternDetails>();
            if (formed) for (int slot = 0; slot < 36; slot++) {
                var stack = getInventory().getStackInSlot(slot);
                if (!stack.is(CosmicItems.LEYLINE_PATTERN.get())) continue;
                try {
                    var migrated = stack.copy();
                    if (canMigrate && getLevel() instanceof net.minecraft.server.level.ServerLevel level &&
                            LeylinePrefab.migrate(migrated, level)) {
                        getInventory().setStackInSlot(slot, migrated);
                        stack = migrated;
                    }
                    var descriptor = LeylinePrefab.descriptor(stack);
                    pendingMigration |= descriptor.contains("payload") || descriptor.contains("positions");
                    var pattern = new LeylineCraftingPattern(stack, getLevel());
                    next.add(pattern);
                    rememberDesigns(List.of(pattern.prefab().id()));
                } catch (RuntimeException ignored) {}
            }
            patterns = List.copyOf(next);
            refresh = false;
            ICraftingProvider.requestUpdate(getMainNode());
            if (grid != null)
                grid.getService(OrreryCatalogue.class).invalidate();
        }
        if (compressor != null && getMainNode().getGrid() != null) {
            var output = compressor.finishedPackage();
            if (!output.isEmpty() && getMainNode().getGrid().getStorageService().getInventory()
                    .insert(AEItemKey.of(output), 1, Actionable.MODULATE, actionSource) == 1)
                compressor.takeFinishedPackage();
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return patterns;
    }

    public Set<UUID> registeredDesigns() {
        var result = new HashSet<UUID>();
        for (String key : catalogue.getAllKeys()) {
            try {
                result.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    public void rememberDesigns(Collection<UUID> designs) {
        boolean changed = false;
        for (UUID id : designs) if (!catalogue.contains(id.toString())) {
            catalogue.putBoolean(id.toString(), true);
            changed = true;
        }
        if (changed) setChanged();
    }

    @Override
    public boolean isBusy() {
        var machine = compressor();
        return machine == null || !machine.canAcceptFabrication();
    }

    @Override
    public boolean pushPattern(IPatternDetails details, KeyCounter[] holders) {
        if (isBusy() || !isOnline() || !(details instanceof LeylineCraftingPattern pattern) ||
                !patterns.contains(details))
            return false;
        KeyCounter supplied = new KeyCounter();
        for (var holder : holders) supplied.addAll(holder);
        KeyCounter expected = new KeyCounter();
        for (var input : pattern.getInputs()) expected.add(input.getPossibleInputs()[0].what(), input.getMultiplier());
        for (var entry : supplied) if (entry.getLongValue() != expected.get(entry.getKey())) return false;
        for (var entry : expected) if (entry.getLongValue() != supplied.get(entry.getKey())) return false;
        if (!compressor().beginFabrication(pattern)) return false;
        for (var holder : holders) holder.clear();
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = ModularPanel.defaultPanel("leyline_ledger", 176, 182);
        var group = new SlotGroup("leyline_patterns", 9, 0, true);
        panel.child(new Grid().left(7).top(10).coverChildren().gridOfSizeWidth(36, 9,
                (x, y, index) -> new ItemSlot()
                        .background(com.gregtechceu.gtceu.common.mui.GTGuiTextures.SLOT,
                                com.gregtechceu.gtceu.common.mui.GTGuiTextures.PRINTED_PAPER_OVERLAY)
                        .slot(SyncHandlers.itemSlot(getInventory().storage, index)
                                .slotGroup(group).filter(stack -> stack.is(CosmicItems.LEYLINE_PATTERN.get()))
                                .changeListener((a, b, client, init) -> {
                                    if (!client) refresh = true;
                                }))));
        panel.child(SlotGroupWidget.playerInventory(true).left(7).bottom(7));
        return panel;
    }
}
