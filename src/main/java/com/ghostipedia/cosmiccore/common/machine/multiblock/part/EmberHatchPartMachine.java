package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableEmberContainer;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class EmberHatchPartMachine extends TieredIOPartMachine {

    @DescSynced
    @Persisted
    public double cachedEmber = 0;

    @Persisted
    @DescSynced
    private final NotifiableEmberContainer emberContainer;

    public EmberHatchPartMachine(BlockEntityCreationInfo holder, int tier, IO io) {
        super(holder, tier, io);
        this.emberContainer = new NotifiableEmberContainer(this, io, getMaxCapacity(tier), getMaxConsumption(tier));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 128, 63);

        group.addWidget(new ImageWidget(4, 4, 120, 55, GuiTextures.DISPLAY));
        group.addWidget(new LabelWidget(8, 8,
                Component.translatable(
                        "gui.cosmiccore.ember_hatch.label." + (this.io == IO.IN ? "import" : "export"))));

        group.addWidget(
                new LabelWidget(8, 18,
                        () -> I18n.get("gui.cosmiccore.ember_hatch.ember",
                                FormattingUtil.formatNumbers(cachedEmber)))
                        .setClientSideWidget());

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    public static double getMaxCapacity(int tier) {
        return 1000 * Math.pow(4, tier);
    }

    public static double getMaxConsumption(int tier) {
        return 500 * Math.pow(4, tier);
    }
}
