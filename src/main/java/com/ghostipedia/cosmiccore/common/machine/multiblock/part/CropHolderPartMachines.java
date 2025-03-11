package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.CosmicGuiTextures;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;

public class CropHolderPartMachines extends MultiblockPartMachine implements IMachineLife, IFancyUIMachine {
    @Persisted
    private final CropHolderHandler heldCrops;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    private boolean isLocked;
    protected  static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CropHolderPartMachines.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    public CropHolderPartMachines(IMachineBlockEntity holder) {
        super(holder);
        heldCrops = new CropHolderHandler(this);
    }

    private class CropHolderHandler extends NotifiableItemStackHandler {

        public CropHolderHandler(MetaMachine machine) {
            super(machine, 1, IO.IN,IO.BOTH, size -> new CustomItemStackHandler(size){
                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            });
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!isLocked()){
            return super.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            var item = stack.getItem();
            if (stack.isEmpty()) {
                return true;
            }
            if (item instanceof ItemNameBlockItem plantBlock){
                var block = plantBlock.getBlock();
                if (block instanceof IPlantable plantable){
                    return true;
                }
            }
            if (item instanceof BlockItem plantBlock){
                var block = plantBlock.getBlock();
                if (block instanceof IPlantable){
                    return true;
                }
            }
            //TODO; Come back for manual Recipe map Injection
            return false;
        }
    }
    @Override
    public Widget createUIWidget() {
        return new WidgetGroup(new Position(0, 0))
                .addWidget(new ImageWidget(0, 15, 84, 60, GuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE))
                .addWidget(new BlockableSlotWidget(heldCrops, 0, 33, 36)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, CosmicGuiTextures.PLANT_OVERLAY));
    }
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

}
