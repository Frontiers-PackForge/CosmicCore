package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.vitae.CultivationProfileManager;
import com.ghostipedia.cosmiccore.common.vitae.EnderIOSpawnerResolver;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.ItemSlot;
import lombok.Getter;

public class SpawnerHatchPartMachine extends TieredIOPartMachine implements IMuiMachine {

    @Getter
    @SaveField
    private final NotifiableItemStackHandler inventory;

    public SpawnerHatchPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, IO.IN);
        inventory = attachTrait(new NotifiableItemStackHandler(1, IO.IN));
        inventory.setFilter(stack -> EnderIOSpawnerResolver.resolve(stack)
                .flatMap(CultivationProfileManager.INSTANCE::get)
                .isPresent());
    }

    public ItemStack getSpawner() {
        return inventory.getStackInSlot(0);
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        inventory.dropInventoryInWorld();
    }

    @Override
    protected RecipeHandlerList getHandlerList() {
        return RecipeHandlerList.NO_DATA;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = ModularPanel.defaultPanel(getDefinition().getId().getPath(), 176, 166);
        panel.child(GTMuiWidgets.createTitleBar(getDefinition(), 176));
        panel.child(new ItemSlot()
                .slot(SyncHandlers.itemSlot(inventory.storage, 0)
                        .changeListener((oldItem, newItem, client, init) -> {
                            if (!ItemStack.isSameItemSameComponents(oldItem, newItem)) {
                                inventory.onContentsChanged();
                            }
                        })
                        .accessibility(true, true))
                .horizontalCenter()
                .top(36));
        panel.child(SlotGroupWidget.playerInventory(true).left(7).bottom(7));
        return panel;
    }
}
