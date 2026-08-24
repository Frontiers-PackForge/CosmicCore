package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

public final class BloomwyrmDisplayUI {

    private static final int WIDTH = 276;
    private static final int HEIGHT = 156;

    private BloomwyrmDisplayUI() {}

    public static Widget<?> create(WorkableElectricMultiblockMachine machine, PanelSyncManager syncManager) {
        boolean hasParallelControl = machine instanceof BloomwyrmUnitMachine unit && unit.supportsParallelControl();
        int controlHeight = hasParallelControl ? 24 : 0;
        int listHeight = HEIGHT - 6 - controlHeight;
        var panel = new ParentWidget<>();
        var list = new ListWidget<>()
                .width(WIDTH - 6)
                .height(listHeight)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .posRel(Alignment.CenterLeft);
        panel.size(WIDTH, HEIGHT).background(GuiTextures.DISPLAY);
        list.children(machine.getWidgetsForDisplay(syncManager));
        panel.child(list.left(3).top(3));
        if (hasParallelControl && machine instanceof BloomwyrmUnitMachine unit) {
            IntSyncValue desiredParallel = new IntSyncValue(unit::getDesiredParallel, unit::setDesiredParallel)
                    .allowC2S();
            syncManager.syncValue("bloomwyrm_unit_parallel_control", desiredParallel);
            panel.child(Flow.row()
                    .width(WIDTH - 10)
                    .height(20)
                    .left(5)
                    .bottom(4)
                    .child(Text.lang("cosmiccore.bloomwyrm.unit.parallel_control")
                            .asWidget()
                            .color(0xFFFFFF)
                            .width(150)
                            .verticalCenter())
                    .child(new TextFieldWidget()
                            .width(42)
                            .height(16)
                            .setTextAlignment(Alignment.CENTER)
                            .setNumbers(1, BloomwyrmUnitMachine.MAX_DESIRED_PARALLEL)
                            .setDefaultNumber(1)
                            .value(desiredParallel)
                            .verticalCenter())
                    .child(Text.lang("cosmiccore.bloomwyrm.unit.parallel_control_max")
                            .asWidget()
                            .color(0xFFFFFF)
                            .marginLeft(6)
                            .verticalCenter()));
        }
        return panel;
    }
}
