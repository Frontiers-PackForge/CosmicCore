package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableEmberContainer;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

public class EmberHatchPartMachine extends TieredIOPartMachine implements IMuiMachine {

    @SyncToClient
    @SaveField
    public double cachedEmber = 0;

    @SyncToClient
    @SaveField
    public double cachedEmberCapacity = 0;

    @SaveField
    public final NotifiableEmberContainer emberContainer;

    public EmberHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier, io);
        this.emberContainer = new NotifiableEmberContainer(this, io, getMaxCapacity(tier), getMaxConsumption(tier));
        this.cachedEmberCapacity = getMaxCapacity(tier);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = ModularPanel.defaultPanel(getDefinition().getId().getPath(), 176, 100);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176));

        panel.child(Flow.column()
                .coverChildren()
                .padding(8)
                .top(14)
                .horizontalCenter()
                .childPadding(4)
                .child(new TextWidget<>(Text.lang(
                        "gui.cosmiccore.ember_hatch.label." + (this.io == IO.IN ? "import" : "export"))))
                .child(new TextWidget<>(Text.dynamic(
                        () -> Component.literal(FormattingUtil.formatNumbers(cachedEmber) + " / " +
                                FormattingUtil.formatNumbers(cachedEmberCapacity) + " Ember")))));

        return panel;
    }

    public static double getMaxCapacity(int tier) {
        return 1000 * Math.pow(4, tier);
    }

    public static double getMaxConsumption(int tier) {
        return 500 * Math.pow(4, tier);
    }
}
