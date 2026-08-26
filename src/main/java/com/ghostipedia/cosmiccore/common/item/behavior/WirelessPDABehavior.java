package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.PowerCapacitorMachine;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.SyncWirelessPDAHudPacket;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class WirelessPDABehavior implements IItemHUDProvider, IInteractionItem, IAddInformation {

    private static final String TAG_LOCAL_MONITOR = "LocalPowerCapacitor";
    private static final BigInteger COMPACT_THRESHOLD = BigInteger.valueOf(1_000_000_000L);
    private static HudData clientData = HudData.empty();

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if (!(MetaMachine.getMachine(level, pos) instanceof PowerCapacitorMachine)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            bind(context.getItemInHand(), GlobalPos.of(level.dimension(), pos));
            player.displayClientMessage(
                    Component.translatable("cosmiccore.wireless_pda.bound", pos.toShortString())
                            .withStyle(ChatFormatting.GREEN),
                    true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (!player.isShiftKeyDown() || linkedPosition(item) == null) return InteractionResultHolder.pass(item);
        if (!level.isClientSide) {
            ItemData.mutateTag(item, tag -> tag.remove(TAG_LOCAL_MONITOR));
            player.displayClientMessage(
                    Component.translatable("cosmiccore.wireless_pda.unbound").withStyle(ChatFormatting.YELLOW),
                    true);
        }
        return InteractionResultHolder.sidedSuccess(item, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines,
                                TooltipFlag isAdvanced) {
        lines.add(Component.translatable("cosmiccore.wireless_pda.tooltip.bind").withStyle(ChatFormatting.GRAY));
        var linked = linkedPosition(stack);
        if (linked != null) {
            lines.add(Component.translatable("cosmiccore.wireless_pda.tooltip.linked",
                    linked.dimension().location().toString(), linked.pos().toShortString())
                    .withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("cosmiccore.wireless_pda.tooltip.clear")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            lines.add(Component.translatable("cosmiccore.wireless_pda.tooltip.wireless")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public void drawHUD(ItemStack stack, GuiGraphics guiGraphics) {
        if (!clientData.synced()) return;
        Minecraft mc = Minecraft.getInstance();
        String headerKey = clientData.local() ?
                "cosmiccore.wireless_pda.hud.local" : "cosmiccore.wireless_pda.hud.dimensional";
        guiGraphics.drawString(mc.font, Component.translatable(headerKey), 1, 34, 0xFFE8C66A, true);
        if (!clientData.available()) {
            guiGraphics.drawString(mc.font,
                    Component.translatable("cosmiccore.wireless_pda.hud.unavailable"),
                    1, 44, 0xFFFF5555, true);
            return;
        }
        double percentStorage = clientData.capacity().signum() > 0 ?
                clientData.stored().multiply(BigInteger.valueOf(10000)).divide(clientData.capacity()).doubleValue() /
                        100.0 :
                0.0;
        guiGraphics.drawString(mc.font,
                Component.translatable("cosmic.gui.wireless.energy.stored",
                        Component.literal(FormattingUtil.formatNumber2Places(percentStorage))
                                .withStyle(ChatFormatting.GREEN),
                        Component.literal(formatEnergy(clientData.stored())).withStyle(ChatFormatting.AQUA),
                        Component.literal(formatEnergy(clientData.capacity())).withStyle(ChatFormatting.AQUA)),
                1, 44, 0xFFFFFF, true);
        guiGraphics.drawString(mc.font,
                Component.translatable("cosmic.command.wireless.energy.input",
                        Component.literal(FormattingUtil.formatNumberReadable(clientData.input()))),
                -5, 54, 0xFF55FF55, true);
        guiGraphics.drawString(mc.font,
                Component.translatable("cosmic.command.wireless.energy.output",
                        Component.literal(FormattingUtil.formatNumberReadable(clientData.output()))),
                -5, 64, 0xFFFF5555, true);
    }

    public static void syncHud(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        ItemStack stack = CosmicCuriosUtils.getPDACurio(player);
        if (stack.isEmpty()) return;
        GlobalPos linked = linkedPosition(stack);
        if (linked != null) {
            syncLocal(player, linked);
            return;
        }
        UUID playerUUID = player.getUUID();
        var team = MachineOwner.getOwner(playerUUID) instanceof FTBOwner ftbOwner ? ftbOwner.getTeam() : null;
        UUID owner = team != null ? team.getTeamId() : playerUUID;
        var wirelessData = WirelessEnergySavedData.getOrCreate(player.serverLevel());
        var capacity = wirelessData.getEnergyCapacity(owner);
        CCoreNetwork.sendToPlayer(player, new SyncWirelessPDAHudPacket(false, capacity.signum() > 0,
                wirelessData.getEnergyStored(owner), capacity, wirelessData.getEnergyInput(owner),
                wirelessData.getEnergyOutput(owner)));
    }

    public static void setClientData(boolean local, boolean available, BigInteger stored, BigInteger capacity,
                                     long input, long output) {
        clientData = new HudData(true, local, available, stored, capacity, input, output);
    }

    private static void syncLocal(ServerPlayer player, GlobalPos linked) {
        var level = player.server.getLevel(linked.dimension());
        if (level == null || !level.hasChunkAt(linked.pos()) ||
                !(MetaMachine.getMachine(level, linked.pos()) instanceof PowerCapacitorMachine capacitor) ||
                !capacitor.isFormed()) {
            CCoreNetwork.sendToPlayer(player, SyncWirelessPDAHudPacket.unavailable(true));
            return;
        }
        var info = capacitor.getEnergyInfo();
        CCoreNetwork.sendToPlayer(player, new SyncWirelessPDAHudPacket(true, true, info.stored(), info.capacity(),
                capacitor.getInputPerSec() / 20, capacitor.getOutputPerSec() / 20));
    }

    private static String formatEnergy(BigInteger energy) {
        return FormattingUtil.formatNumberOrSic(energy, COMPACT_THRESHOLD);
    }

    private static void bind(ItemStack stack, GlobalPos position) {
        ItemData.mutateTag(stack, root -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("Dimension", position.dimension().location().toString());
            tag.putLong("Position", position.pos().asLong());
            root.put(TAG_LOCAL_MONITOR, tag);
        });
    }

    @Nullable
    private static GlobalPos linkedPosition(ItemStack stack) {
        var root = ItemData.readTag(stack);
        if (!root.contains(TAG_LOCAL_MONITOR)) return null;
        var tag = root.getCompound(TAG_LOCAL_MONITOR);
        var dimension = ResourceLocation.tryParse(tag.getString("Dimension"));
        if (dimension == null || !tag.contains("Position")) return null;
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimension), BlockPos.of(tag.getLong("Position")));
    }

    private static boolean isPDA(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof IComponentItem metaItem) {
            for (var behavior : metaItem.getComponents()) {
                if (behavior instanceof WirelessPDABehavior) return true;
            }
        }
        return false;
    }

    private record HudData(boolean synced, boolean local, boolean available, BigInteger stored, BigInteger capacity,
                           long input, long output) {

        private static HudData empty() {
            return new HudData(false, false, false, BigInteger.ZERO, BigInteger.ZERO, 0, 0);
        }
    }

    public static class CosmicCuriosUtils {

        public static boolean hasPDACurio(Player player) {
            return CuriosApi.getCuriosInventory(player)
                    .map(curios -> curios.findFirstCurio(WirelessPDABehavior::isPDA).isPresent())
                    .orElse(false);
        }

        public static ItemStack getPDACurio(Player player) {
            return CuriosApi.getCuriosInventory(player)
                    .flatMap(curios -> curios.findFirstCurio(WirelessPDABehavior::isPDA))
                    .map(result -> result.stack())
                    .orElse(ItemStack.EMPTY);
        }
    }
}
