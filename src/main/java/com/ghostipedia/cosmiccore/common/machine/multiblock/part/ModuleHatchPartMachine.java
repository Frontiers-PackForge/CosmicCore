package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import lombok.Getter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModuleHatchPartMachine extends TieredIOPartMachine implements IMuiMachine {

    @Getter
    @SaveField
    private final NotifiableItemStackHandler inventory;

    public ModuleHatchPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, IO.IN);
        this.inventory = attachTrait(new NotifiableItemStackHandler(getSlots(tier), IO.IN));
        this.inventory.setFilter(ModuleHatchPartMachine::isModule);
    }

    private static List<Item> MODULES = null;

    private static boolean isModule(ItemStack stack) {
        if (MODULES == null) {
            MODULES = List.of(
                    CosmicItems.PROD_MOD_1.asItem(),
                    CosmicItems.PROD_MOD_2.asItem(),
                    CosmicItems.PROD_MOD_3.asItem(),
                    CosmicItems.PROD_MOD_4.asItem(),
                    CosmicItems.PARA_MOD_1.asItem(),
                    CosmicItems.PARA_MOD_2.asItem(),
                    CosmicItems.PARA_MOD_3.asItem(),
                    CosmicItems.PARA_MOD_4.asItem(),
                    CosmicItems.RESONANT_MODULE.asItem(),
                    CosmicItems.PROTOCYTE_MOD.asItem(),
                    CosmicItems.FUSION_MODULE_MK1.asItem());
        }
        return MODULES.contains(stack.getItem());
    }

    private int getSlots(int tier) {
        switch (tier) {
            case GTValues.UV:
                return 4;
            case GTValues.UHV:
                return 8;
            case GTValues.UEV:
                return 12;
            default:
                return 4;
        }
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        getInventory().dropInventoryInWorld();
    }

    protected RecipeHandlerList getHandlerList() {
        return RecipeHandlerList.NO_DATA;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        int colSize = 4;
        int totalSlots = getInventory().getSlots();
        int rowSize = totalSlots / colSize;

        int width = Math.max(176, 18 * colSize + 14);
        int height = Math.max(168, (18 * rowSize) + 78 + 19);
        var panel = ModularPanel.defaultPanel(getDefinition().getId().getPath(), width, height);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), width));

        SlotGroup group = new SlotGroup("module_inv", colSize, 0, true);
        panel.child(new Grid()
                .coverChildren()
                .top(10)
                .horizontalCenter() // brachy 3.3.0: alignX(float) removed; horizontalCenter() == leftRel(0.5f)
                // brachy 3.3.0: Grid.mapTo(width,count,fn) removed -> gridOfSizeWidth(size,width,(x,y,i)->...)
                .gridOfSizeWidth(totalSlots, colSize, (x, y, index) -> new ItemSlot()
                        .slot(SyncHandlers.itemSlot(inventory, index)
                                .slotGroup(group)
                                .changeListener((newItem, amount, client, init) -> {
                                    if (amount) {
                                        inventory.onContentsChanged();
                                    }
                                })
                                .accessibility(inventory.handlerIO.support(IO.IN), true))))

                .child(SlotGroupWidget.playerInventory(true)
                        .left(7)
                        .bottom(7));

        return panel;
    }
}
