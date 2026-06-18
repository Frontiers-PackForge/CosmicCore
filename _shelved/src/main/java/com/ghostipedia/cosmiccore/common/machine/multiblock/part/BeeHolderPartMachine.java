package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.CosmicGuiTextures;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import forestry.api.ForestryCapabilities;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class BeeHolderPartMachine extends MultiblockPartMachine implements IMachineLife, IFancyUIMachine {

    @Persisted
    @Getter
    private final BeeHolderHandler heldBees;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    public boolean isLocked;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            BeeHolderPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    public BeeHolderPartMachine(IMachineBlockEntity holder) {
        super(holder);
        heldBees = new BeeHolderHandler(this);
    }

    public class BeeHolderHandler extends NotifiableItemStackHandler {

        public BeeHolderHandler(MetaMachine machine) {
            super(machine, 4, IO.IN, IO.BOTH, size -> new CustomItemStackHandler(size) {

                @Override
                public int getSlotLimit(int slot) {
                    return 4;
                }
            });
        }

        @Override
        public int getSlotLimit(int slot) {
            return 4;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!isLocked()) {
                return super.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            var optionalForgeCap = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null);
            if (!optionalForgeCap.isPresent()) return false;
            IIndividualHandlerItem cap = optionalForgeCap.resolve().get();
            IIndividual individual = cap.getIndividual();
            return individual instanceof IBee bee;
        }
    }

    @Override
    public Widget createUIWidget() {
        return new WidgetGroup(0, 0, 176, 65)
                .addWidget(new ImageWidget(8, 5, 160, 60, CosmicGuiTextures.BEE_HOLDER_OVERLAY))
                .addWidget(new BlockableSlotWidget(heldBees, 0, 37, 26).setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, CosmicGuiTextures.BEE_OVERLAY))
                .addWidget(new BlockableSlotWidget(heldBees, 1, 65, 26).setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, CosmicGuiTextures.BEE_OVERLAY))
                .addWidget(new BlockableSlotWidget(heldBees, 2, 93, 26).setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, CosmicGuiTextures.BEE_OVERLAY))
                .addWidget(new BlockableSlotWidget(heldBees, 3, 121, 26).setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, CosmicGuiTextures.BEE_OVERLAY));
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 65, this, entityPlayer).widget(new FancyMachineUIWidget(this, 176, 65));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
