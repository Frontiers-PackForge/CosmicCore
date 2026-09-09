package com.ghostipedia.cosmiccore.common.compat.gtceu.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import appeng.api.stacks.GenericStack;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class MEInputConfigActions {

    private MEInputConfigActions() {}

    public static void register(Object machine, IConfigurableSlotList slots, boolean fluid,
                                PanelSyncManager syncManager) {
        syncManager.registerServerSyncedAction("ae_config_set", packet -> {
            int index = packet.readVarInt();
            if (!editable(slots, index)) return;
            ItemStack held = syncManager.getPlayer().containerMenu.getCarried();
            if (held.isEmpty()) return;
            if (fluid) {
                FluidUtil.getFluidContained(held)
                        .ifPresent(stack -> setConfig(machine, slots, index, AEUtil.fromFluidStack(stack)));
            } else {
                setConfig(machine, slots, index, GenericStack.fromItemStack(held));
            }
        });
        syncManager.registerServerSyncedAction("ae_config_clear", packet -> {
            int index = packet.readVarInt();
            if (editable(slots, index)) setConfig(machine, slots, index, null);
        });
        syncManager.registerServerSyncedAction("ae_config_amount", packet -> {
            int index = packet.readVarInt();
            long amount = packet.readVarLong();
            if (machine instanceof IMEStockingPart || !editable(slots, index) || amount < 1 ||
                    amount > Integer.MAX_VALUE)
                return;
            var config = slots.getConfigurableSlot(index).getConfig();
            if (config != null) setConfig(machine, slots, index, new GenericStack(config.what(), amount));
        });
        syncManager.registerServerSyncedAction("cosmiccore_ae_config_scroll", packet -> {
            int index = packet.readVarInt();
            boolean increase = packet.readBoolean();
            boolean control = packet.readBoolean();
            if (machine instanceof IMEStockingPart || !editable(slots, index)) return;
            var config = slots.getConfigurableSlot(index).getConfig();
            if (config != null) {
                int next = MEConfigAmounts.scroll(config.amount(), increase ? 1 : -1, control, Integer.MAX_VALUE);
                setConfig(machine, slots, index, new GenericStack(config.what(), next));
            }
        });
        syncManager.registerServerSyncedAction("ae_config_set_ghost", packet -> {
            int index = packet.readVarInt();
            boolean incomingFluid = packet.readBoolean();
            if (!editable(slots, index) || incomingFluid != fluid) return;
            if (fluid) {
                FluidStack stack = FluidStack.STREAM_CODEC.decode(packet);
                if (!stack.isEmpty()) setConfig(machine, slots, index, AEUtil.fromFluidStack(stack));
            } else {
                ItemStack stack = ItemStack.STREAM_CODEC.decode(packet);
                if (!stack.isEmpty()) setConfig(machine, slots, index, GenericStack.fromItemStack(stack));
            }
        });
        if (!fluid) {
            syncManager.registerServerSyncedAction("ae_stock_pickup", packet -> {
                int index = packet.readVarInt();
                if (machine instanceof IMEStockingPart || !validIndex(slots, index)) return;
                var menu = syncManager.getPlayer().containerMenu;
                if (!menu.getCarried().isEmpty()) return;
                var slot = (ExportOnlyAEItemSlot) slots.getConfigurableSlot(index);
                ItemStack stack = slot.getStackInSlot(0);
                if (!stack.isEmpty()) menu.setCarried(slot.extractItem(0, stack.getMaxStackSize(), false));
            });
        }
    }

    private static boolean validIndex(IConfigurableSlotList slots, int index) {
        return index >= 0 && index < slots.getConfigurableSlots();
    }

    private static boolean editable(IConfigurableSlotList slots, int index) {
        return validIndex(slots, index) &&
                !(slots instanceof ExportOnlyAEItemList items && items.isAutoPull()) &&
                !(slots instanceof ExportOnlyAEFluidList fluids && fluids.isAutoPull());
    }

    private static void setConfig(Object machine, IConfigurableSlotList slots, int index,
                                  @Nullable GenericStack config) {
        var slot = slots.getConfigurableSlot(index);
        if (machine instanceof IMEStockingPart stocking && config != null) {
            config = new GenericStack(config.what(), 1);
            if (stocking.testConfiguredInOtherPart(config)) return;
            for (int other = 0; other < slots.getConfigurableSlots(); other++) {
                var existing = slots.getConfigurableSlot(other).getConfig();
                if (other != index && existing != null && existing.what().equals(config.what())) return;
            }
        }
        if (Objects.equals(slot.getConfig(), config)) return;
        slot.setConfig(config);
        if (machine instanceof MEStockingUIRefresh stocking) {
            slot.setStock(null);
            stocking.cosmiccore$requestStockRefresh();
        }
        if (slot instanceof ExportOnlyAESlot aeSlot) aeSlot.getOnContentsChanged().run();
    }
}
