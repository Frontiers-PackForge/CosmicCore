package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModuleHatchPartMachine extends TieredIOPartMachine implements IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ModuleHatchPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);
    @Getter
    @Persisted
    private final NotifiableItemStackHandler inventory;

    public ModuleHatchPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.IN);
        this.inventory = new NotifiableItemStackHandler(this, getSlots(tier), IO.IN);
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
        // Always have 4 slots, change this to give different slots per tier
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
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(getInventory().storage);
    }

    protected RecipeHandlerList getHandlerList() {
        return RecipeHandlerList.NO_DATA;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        int rowSize = 4;
        int colSize = this.getInventory().getSlots() / 4;
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                container.addWidget(
                        new SlotWidget(getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, io.support(IO.IN))
                                .setBackgroundTexture(GuiTextures.SLOT)
                                .setIngredientIO(IngredientIO.INPUT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
    }
}
