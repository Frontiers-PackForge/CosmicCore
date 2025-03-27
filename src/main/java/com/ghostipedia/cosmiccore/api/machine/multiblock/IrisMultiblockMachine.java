package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class IrisMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    @Persisted
    private final NotifiableItemStackHandler inventory;
    @Persisted
    protected NotifiableItemStackHandler storageSlot;
    @Getter
    protected boolean ignite;
    @Getter
    protected boolean isFuelable;

    public IrisMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
        this.inventory = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
    }

    public void isfuelable(boolean fuelable) {
        this.isFuelable = fuelable;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText)
                        .setMaxWidthLimit(150)
                        .clickHandler(this::handleDisplayClick)));
        group.addWidget(new SlotWidget(inventory.storage, 0, 7, 101, true, true)
                .setBackground(GuiTextures.SLOT, GuiTextures.ATOMIC_OVERLAY_1));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new ButtonWidget(
                27,
                100,
                158,
                20,
                new GuiTextureGroup(
                        GuiTextures.BUTTON,
                        new TextTexture("cosmiccore.multiblock.fuel_star")),
                clickData -> isfuelable(true)));
        return group;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            textList.add(Component.translatable("cosmiccore.multiblock.iris.star_stage_early_star"));
            textList.add(Component.translatable("cosmiccore.multiblock.iris.star_stage_sustain"));
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }
}
