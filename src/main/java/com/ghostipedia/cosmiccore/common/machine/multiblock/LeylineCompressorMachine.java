package com.ghostipedia.cosmiccore.common.machine.multiblock;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.deployment.*;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.*;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.*;

public final class LeylineCompressorMachine extends WorkableElectricMultiblockMachine {

    @SaveField
    private final NotifiableItemStackHandler encoder = attachTrait(new NotifiableItemStackHandler(2, IO.NONE, IO.NONE));
    @SaveField
    private ItemStack fabrication = ItemStack.EMPTY;
    @SaveField
    private int remainingTicks;
    @SaveField
    @com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient
    private CompoundTag draft = new CompoundTag();
    @SaveField
    private CompoundTag unavailableDraft = new CompoundTag();

    public LeylineCompressorMachine(BlockEntityCreationInfo info) {
        super(info, new FabricationLogic());
    }

    @Override
    public void onLoad() {
        if (getLevel() instanceof ServerLevel level) {
            if (draft.contains("positions")) {
                try {
                    var prefab = LeylinePrefab.load(draft);
                    LeylineFabricationLibrary.get(level).add(prefab);
                    draft = prefab.referenceTag();
                } catch (RuntimeException exception) {
                    unavailableDraft = draft;
                    draft = new CompoundTag();
                }
                setChanged();
            }
            migrateEncoder();
        }
        super.onLoad();
    }

    public boolean isFabricating() {
        return !fabrication.isEmpty();
    }

    public boolean canAcceptFabrication() {
        return isFormed() && isWorkingEnabled() && !isFabricating() && getMaxVoltage() >= 128;
    }

    public boolean beginFabrication(LeylineCraftingPattern pattern) {
        var prefab = pattern.prefab();
        if (!(getLevel() instanceof ServerLevel level) || !canAcceptFabrication() ||
                !LeylinePrefabValidation.valid(level, prefab))
            return false;
        LeylineFabricationLibrary.get(level).add(prefab);
        fabrication = pattern.output();
        remainingTicks = prefab.blocks().size();
        setChanged();
        return true;
    }

    private void fabricationTick() {
        if (!isWorkingEnabled()) {
            getRecipeLogic().setStatus(RecipeLogic.Status.SUSPEND);
            return;
        }
        if (!isFormed() || fabrication.isEmpty() || remainingTicks <= 0) {
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
            return;
        }
        if (getMaxVoltage() >= 128 && energyContainer != null && energyContainer.getEnergyStored() >= 128 &&
                energyContainer.removeEnergy(128) == 128) {
            remainingTicks--;
            getRecipeLogic().setStatus(remainingTicks > 0 ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE);
            setChanged();
        } else getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
    }

    public ItemStack finishedPackage() {
        return remainingTicks == 0 ? fabrication.copy() : ItemStack.EMPTY;
    }

    public void takeFinishedPackage() {
        fabrication = ItemStack.EMPTY;
        setChanged();
    }

    public boolean encode(LeylinePrefab prefab) {
        if (!(getLevel() instanceof ServerLevel level) || !canWritePattern() ||
                !LeylinePrefabValidation.valid(level, prefab))
            return false;
        boolean revising = encoder.getStackInSlot(1).is(CosmicItems.LEYLINE_PATTERN.get());
        LeylineFabricationLibrary.get(level).add(prefab);
        if (!revising) encoder.extractItemInternal(0, 1, false);
        encoder.setStackInSlot(1, prefab.stack(true));
        draft = prefab.referenceTag();
        getSyncDataHolder().markClientSyncFieldDirty("draft");
        setChanged();
        return true;
    }

    private boolean canWritePattern() {
        return encoder.getStackInSlot(1).is(CosmicItems.LEYLINE_PATTERN.get()) ||
                encoder.getStackInSlot(1).isEmpty() &&
                        encoder.getStackInSlot(0).is(CosmicItems.BLANK_LEYLINE_PATTERN.get());
    }

    @Override
    public void onMachineDestroyed() {
        if (!isRemote() && !fabrication.isEmpty()) {
            if (remainingTicks == 0) drop(fabrication);
            else {
                var prefab = LeylinePrefab.fromStack(fabrication, getLevel());
                if (prefab != null) prefab.ingredients().forEach((item, count) -> {
                    int left = count;
                    while (left > 0) {
                        int amount = Math.min(left, item.getDefaultMaxStackSize());
                        drop(new ItemStack(item, amount));
                        left -= amount;
                    }
                });
            }
            fabrication = ItemStack.EMPTY;
        }
        super.onMachineDestroyed();
    }

    private void drop(ItemStack stack) {
        var pos = getBlockPos();
        Containers.dropItemStack(getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
    }

    private void migrateEncoder() {
        if (!(getLevel() instanceof ServerLevel level)) return;
        for (int slot = 0; slot < encoder.getSlots(); slot++) {
            var stack = encoder.getStackInSlot(slot).copy();
            try {
                if (LeylinePrefab.migrate(stack, level)) encoder.setStackInSlot(slot, stack);
            } catch (RuntimeException ignored) {}
        }
    }

    private static final class FabricationLogic extends RecipeLogic {

        @Override
        public void serverTick() {
            ((LeylineCompressorMachine) getMachine()).fabricationTick();
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        migrateEncoder();
        var panel = ModularPanel.defaultPanel("leyline_encoder", 420, 310).topRel(0.25f);
        var controls = new ParentWidget<>().pos(178, 224).size(234, 80).background(GTGuiTextures.BACKGROUND_INVERSE);
        panel.child(controls);
        var control = new LeylineEncoderControl(this);
        control.canWritePattern = this::canWritePattern;
        syncManager.syncValue("encoder", control.allowC2S());
        var progress = new IntSyncValue(() -> fabrication.isEmpty() ? -1 : remainingTicks, value -> {});
        syncManager.syncValue("fabrication_remaining", progress);
        controls.child(
                brachy.modularui.api.drawable.Text.dynamic(() -> net.minecraft.network.chat.Component.translatable(
                        progress.getIntValue() < 0 ? "cosmiccore.leyline.idle" : "cosmiccore.leyline.remaining",
                        Math.max(0, progress.getIntValue()) / 20.0)).asWidget().pos(6, 64).size(222, 10));
        controls.child(new ItemSlot().pos(6, 8)
                .background(GTGuiTextures.SLOT, GTGuiTextures.PAPER_OVERLAY)
                .slot(SyncHandlers.itemSlot(encoder.storage, 0)
                        .filter(stack -> stack.is(CosmicItems.BLANK_LEYLINE_PATTERN.get()))));
        controls.child(new ItemSlot().pos(209, 8)
                .background(GTGuiTextures.SLOT, GTGuiTextures.PRINTED_PAPER_OVERLAY)
                .slot(SyncHandlers.itemSlot(encoder.storage, 1)
                        .filter(stack -> stack.is(CosmicItems.LEYLINE_PACKAGE.get()) ||
                                stack.is(CosmicItems.LEYLINE_PATTERN.get()))
                        .changeListener((oldStack, newStack, client, init) -> {
                            if (!client) migrateEncoder();
                            if (client && !newStack.isEmpty() &&
                                    (init || !ItemStack.isSameItemSameComponents(oldStack, newStack))) {
                                control.loadDesign.accept(newStack.copy());
                            }
                        })));
        panel.child(Text.lang("container.inventory").asWidget().pos(8, 214).size(162, 10));
        panel.child(SlotGroupWidget.playerInventory(true).left(7).bottom(7));
        if (isRemote())
            com.ghostipedia.cosmiccore.client.renderer.deployment.LeylineEncoderUI.populate(panel, controls, control,
                    draft);
        return panel;
    }
}
