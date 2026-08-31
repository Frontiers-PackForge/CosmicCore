package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;

public final class BiomeldVivariumDisplayUI {

    private static final int WIDTH = 276;
    private static final int HEIGHT = 156;

    private BiomeldVivariumDisplayUI() {}

    public static Widget<?> create(BiomeldVivariumMachine machine, PanelSyncManager syncManager) {
        var panel = new ParentWidget<>();
        var list = new ListWidget<>()
                .width(WIDTH - 6)
                .height(HEIGHT - 30)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .posRel(Alignment.CenterLeft);
        panel.size(WIDTH, HEIGHT).background(GuiTextures.DISPLAY);
        list.children(machine.getWidgetsForDisplay(syncManager));
        panel.child(list.left(3).top(3));

        IntSyncValue mode = new IntSyncValue(machine::getModeOrdinal, machine::setModeOrdinal).allowC2S();
        syncManager.syncValue("biomeld_vivarium_mode_control", mode);
        panel.child(Flow.row()
                .width(WIDTH - 10)
                .height(20)
                .left(5)
                .bottom(4)
                .child(Text.lang("cosmiccore.biomeld_vivarium.mode_control")
                        .asWidget()
                        .color(0xFFFFFF)
                        .width(150)
                        .verticalCenter())
                .child(new CycleButtonWidget()
                        .background(GTGuiTextures.BUTTON)
                        .stateCount(BiomeldVivariumMachine.Mode.values().length)
                        .stateOverlay(BiomeldVivariumMachine.Mode.MATERIAL.ordinal(),
                                Text.lang("cosmiccore.biomeld_vivarium.mode.material.short")
                                        .alignment(Alignment.Center)
                                        .asTextIcon())
                        .stateOverlay(BiomeldVivariumMachine.Mode.EXPERIENCE.ordinal(),
                                Text.lang("cosmiccore.biomeld_vivarium.mode.experience.short")
                                        .alignment(Alignment.Center)
                                        .asTextIcon())
                        .value(mode)
                        .width(70)
                        .height(16)
                        .verticalCenter()));
        return panel;
    }
}
