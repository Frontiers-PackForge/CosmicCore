package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;

public class MuiWorkableMultiblockMachine extends WorkableMultiblockMachine implements IMuiMachine {

    public MuiWorkableMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var listWidget = new ListWidget<>()
                .width(166)
                .height(130)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .posRel(Alignment.CenterLeft);
        listWidget.children(getWidgetsForDisplay(syncManager));
        mainWidget.size(172, 136).background(GuiTextures.DISPLAY).child(listWidget.left(3).top(3));
    }
}
