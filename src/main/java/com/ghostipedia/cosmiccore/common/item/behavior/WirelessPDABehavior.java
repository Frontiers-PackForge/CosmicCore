package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;
import com.gregtechceu.gtceu.api.item.component.IItemLifeCycle;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

import java.math.BigInteger;
import java.util.UUID;

public class WirelessPDABehavior implements IItemHUDProvider, IItemLifeCycle {

    private static Level serverLevel;
    private static UUID playerUUID;
    private static UUID wirelessUUID;

    public void ItemMagnetBehavior() {
        NeoForge.EVENT_BUS.register(this);
    }

    public static void setLevel(Level level) {
        serverLevel = level;
    }

    public static void setOwner(Player player) {
        playerUUID = player.getUUID();
        var team = ((FTBOwner) MachineOwner.getOwner(playerUUID)).getTeam();
        wirelessUUID = team != null ? team.getTeamId() : playerUUID;
    }

    @Override
    public void drawHUD(ItemStack stack, GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (serverLevel == null || playerUUID == null) return;
        var wirelessData = WirelessEnergySavedData.getOrCreate((ServerLevel) serverLevel);
        var percentStorage = (wirelessData.getEnergyStored(wirelessUUID).multiply(BigInteger.valueOf(10000))
                .divide(wirelessData.getEnergyCapacity(wirelessUUID)).intValue() / 100.0F);

        guiGraphics.drawString(mc.font,
                Component.translatable("cosmic.gui.wireless.energy.stored",
                        Component.literal(FormattingUtil.formatNumbers(percentStorage)).withStyle(ChatFormatting.GREEN),
                        Component
                                .literal(FormattingUtil
                                        .formatNumberReadable(wirelessData.getEnergyStored(playerUUID).longValue()))
                                .withStyle(ChatFormatting.AQUA),
                        Component
                                .literal(FormattingUtil
                                        .formatNumberReadable(wirelessData.getEnergyCapacity(playerUUID).longValue()))
                                .withStyle(ChatFormatting.AQUA)),
                1, 44, 0xFFFFFF, true);
        guiGraphics.drawString(mc.font,
                Component.translatable("cosmic.command.wireless.energy.input",
                        Component.literal(FormattingUtil.formatNumberReadable(wirelessData.getEnergyInput(playerUUID))))
                        .withStyle(ChatFormatting.GREEN),
                -5, 54, 0xFFFFFF, true);
        guiGraphics.drawString(mc.font,
                Component.translatable("cosmic.command.wireless.energy.output",
                        Component
                                .literal(FormattingUtil.formatNumberReadable(wirelessData.getEnergyOutput(playerUUID))))
                        .withStyle(ChatFormatting.RED),
                -5, 64, 0xFFFFFF, true);
    }

    public static class CosmicCuriosUtils {

        public static boolean hasPDACurio(Player player) {
            return CuriosApi.getCuriosInventory(player)
                    .map(curios -> curios.findFirstCurio(WirelessPDABehavior::isPDA).isPresent())
                    .orElse(false);
        }

        public static ItemStack getPDACurio(Player player) {
            var curioInventory = CuriosApi.getCuriosInventory(player);
            if (!curioInventory.isPresent()) return ItemStack.EMPTY;
            var slotResult = curioInventory.map(curio -> curio.findFirstCurio(WirelessPDABehavior::isPDA));
            if (slotResult.isEmpty()) return ItemStack.EMPTY;
            if (slotResult.get().isEmpty()) return ItemStack.EMPTY;
            return slotResult.get().get().stack();
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (serverLevel != null && playerUUID != null) return;
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        if (CosmicCuriosUtils.hasPDACurio(player)) {
            setLevel(level);
            setOwner(player);
        }
    }

    private static boolean isPDA(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof IComponentItem metaItem) {
            for (var behavior : metaItem.getComponents()) {
                if (behavior instanceof WirelessPDABehavior) {
                    return true;
                }
            }
        }
        return false;
    }
}
