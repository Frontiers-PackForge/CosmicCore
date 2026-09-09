package com.ghostipedia.cosmiccore.common.compat.gtceu.ae2;

import com.gregtechceu.gtceu.integration.ae2.gui.AEKeyStorageSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.AEStackDisplayWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.ScrollPreservingGrid;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DynamicLinkedSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.scroll.VerticalScrollData;
import brachy.modularui.widgets.DynamicSyncedWidget;
import brachy.modularui.widgets.layout.Flow;

public final class MEOutputUI {

    private MEOutputUI() {}

    public static void build(ParentWidget<?> mainWidget, PanelSyncManager syncManager,
                             IGridConnectedMachine machine, KeyStorage buffer) {
        var online = new BooleanSyncValue(machine::isOnline, machine::setOnline);
        syncManager.syncValue("is_online", online);
        var flow = Flow.col().coverChildren();
        flow.child(Text.dynamic(() -> Component.translatable(online.getBoolValue() ?
                "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));

        var contents = new AEKeyStorageSyncHandler(buffer);
        syncManager.syncValue("ae_output_display", contents);
        int[] scroll = { 0 };
        var rows = new DynamicLinkedSyncHandler<>(contents).widgetProvider((sm, value) -> {
            var column = Flow.col().coverChildren();
            var list = value.getValue();
            if (list.isEmpty()) {
                return column.child(Text.lang("gtceu.gui.waiting_list_empty").asWidget());
            }
            column.child(Text.lang("gtceu.gui.waiting_list").asWidget().margin(0, 2));
            column.child(new ScrollPreservingGrid(scroll)
                    .size(167, 67)
                    .scrollable(new VerticalScrollData())
                    .gridOfSizeWidth(list.size(), 1, (x, y, index) -> Flow.row().coverChildrenHeight()
                            .child(new AEStackDisplayWidget(list, index))
                            .child(Text.dynamic(() -> index < list.size() ? describe(list.get(index)) :
                                    CommonComponents.EMPTY).asWidget().width(140).marginLeft(3))));
            return column;
        });
        flow.child(new DynamicSyncedWidget<>().syncHandler(rows).size(167, 80));
        mainWidget.child(flow);
    }

    private static Component describe(GenericStack stack) {
        String amount = FormattingUtil.formatNumbers(stack.amount());
        return Component.translatable(stack.what() instanceof AEFluidKey ?
                "gtceu.universal.liters" : "cosmiccore.gui.me.item_amount", amount)
                .append(CommonComponents.SPACE).append(stack.what().getDisplayName());
    }
}
