package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.capability.recipe.IHeatContainer;
import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableThermiaContainer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class ThermiaHatchPartMachine extends TieredIOPartMachine implements IHeatContainer, IMuiMachine {

    @SaveField
    @SyncToClient
    private final NotifiableThermiaContainer thermiaContainer;

    public ThermiaHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier, io);
        long currentTemp = 0;
        this.thermiaContainer = createThermiaContainer();
    }

    protected NotifiableThermiaContainer createThermiaContainer() {
        NotifiableThermiaContainer container;
        if (io == IO.OUT) {
            container = new NotifiableThermiaContainer(this, IO.OUT, getThermiaLimits(tier), 0);
            container.setSideOutputCondition(s -> s == getFrontFacing());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        } else {
            container = new NotifiableThermiaContainer(this, IO.IN, getThermiaLimits(tier), 0);
            container.setSideInputCondition(s -> s == getFrontFacing());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        }
        return container;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = ModularPanel.defaultPanel(getDefinition().getId().getPath(), 176, 120);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176));

        panel.child(Flow.column()
                .coverChildren()
                .padding(8)
                .top(14)
                .horizontalCenter() // brachy 3.3.0: alignX(float) removed; horizontalCenter() == leftRel(0.5f)
                .childPadding(4)
                .child(new TextWidget<>(Text.lang(
                        "gui.cosmiccore.thermia_hatch.label." + (this.io == IO.IN ? "import" : "export"))))
                .child(new TextWidget<>(Text.lang("gui.cosmiccore.thermia_hatch.hatch_limit")))
                .child(new TextWidget<>(Text.dynamic(
                        () -> Component
                                .literal(FormattingUtil.formatNumbers(thermiaContainer.getOverloadLimit()) + " K"))))
                .child(new TextWidget<>(Text.lang("gui.cosmiccore.thermia_hatch.stored_temp")))
                .child(new TextWidget<>(Text.dynamic(
                        () -> Component
                                .literal(FormattingUtil.formatNumbers(thermiaContainer.getCurrentTemp()) + " K")))));

        return panel;
    }

    public static int getThermiaLimits(int tier) {
        return switch (tier) {
            case GTValues.ZPM -> 95000;
            case GTValues.UV -> 128000;
            case GTValues.UHV -> 108000;
            case GTValues.UEV -> 158000;
            case GTValues.UIV -> 198400;
            case GTValues.UXV -> 360000;
            case GTValues.OpV -> 2500000;
            case GTValues.MAX -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    @Override
    public long acceptHeatFromNetwork(Direction side) {
        return 0;
    }

    @Override
    public boolean inputsHeat(Direction side) {
        return false;
    }

    @Override
    public boolean outputsHeat(Direction side) {
        return IHeatContainer.super.outputsHeat(side);
    }

    @Override
    public long changeHeat(long heatDifference) {
        return 0;
    }

    @Override
    public long getOverloadLimit() {
        return 0;
    }

    @Override
    public long getHeatStorage() {
        return 0;
    }
}
