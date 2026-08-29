package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.PowerCapacitorMachine;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.SyncWirelessPDAHudPacket;
import com.ghostipedia.cosmiccore.utils.ItemData;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;

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

import appeng.blockentity.networking.ControllerBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class WirelessPDABehavior implements IItemHUDProvider, IInteractionItem, IAddInformation {

    private static final String TAG_LOCAL_MONITOR = "LocalPowerCapacitor";
    private static final String TAG_ME_CONTROLLER = "MEController";
    private static final BigInteger COMPACT_THRESHOLD = BigInteger.valueOf(1_000_000_000L);
    private static HudData clientData = HudData.empty();

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if (MetaMachine.getMachine(level, pos) instanceof PowerCapacitorMachine) {
            if (!level.isClientSide) {
                bind(context.getItemInHand(), TAG_LOCAL_MONITOR, GlobalPos.of(level.dimension(), pos));
                player.displayClientMessage(
                        Component.translatable("cosmiccore.wireless_pda.bound.power", pos.toShortString())
                                .withStyle(ChatFormatting.GREEN),
                        true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.getBlockEntity(pos) instanceof ControllerBlockEntity) {
            if (!level.isClientSide) {
                bind(context.getItemInHand(), TAG_ME_CONTROLLER, GlobalPos.of(level.dimension(), pos));
                player.displayClientMessage(
                        Component.translatable("cosmiccore.wireless_pda.bound.compute", pos.toShortString())
                                .withStyle(ChatFormatting.GREEN),
                        true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (!player.isShiftKeyDown() ||
                (linkedPosition(item, TAG_LOCAL_MONITOR) == null && linkedPosition(item, TAG_ME_CONTROLLER) == null)) {
            return InteractionResultHolder.pass(item);
        }
        if (!level.isClientSide) {
            ItemData.mutateTag(item, tag -> {
                tag.remove(TAG_LOCAL_MONITOR);
                tag.remove(TAG_ME_CONTROLLER);
            });
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
        var linkedPower = linkedPosition(stack, TAG_LOCAL_MONITOR);
        var linkedCompute = linkedPosition(stack, TAG_ME_CONTROLLER);
        if (linkedPower != null) {
            lines.add(Component.translatable("cosmiccore.wireless_pda.tooltip.linked.power",
                    linkedPower.dimension().location().toString(), linkedPower.pos().toShortString())
                    .withStyle(ChatFormatting.AQUA));
        } else {
            lines.add(Component.translatable("cosmiccore.wireless_pda.tooltip.wireless")
                    .withStyle(ChatFormatting.AQUA));
        }
        if (linkedCompute != null) {
            lines.add(Component.translatable("cosmiccore.wireless_pda.tooltip.linked.compute",
                    linkedCompute.dimension().location().toString(), linkedCompute.pos().toShortString())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (linkedPower != null || linkedCompute != null) {
            lines.add(Component.translatable("cosmiccore.wireless_pda.tooltip.clear")
                    .withStyle(ChatFormatting.DARK_GRAY));
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
        } else {
            double percentStorage = clientData.capacity().signum() > 0 ?
                    clientData.stored().multiply(BigInteger.valueOf(10000)).divide(clientData.capacity())
                            .doubleValue() /
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
        if (clientData.computeLinked()) {
            Component computeLine = clientData.computeAvailable() ?
                    Component.translatable("cosmiccore.wireless_pda.hud.compute",
                            Component.literal(FormattingUtil.formatNumberReadable(clientData.computeUsed()))
                                    .withStyle(ChatFormatting.GREEN),
                            Component.literal(FormattingUtil.formatNumberReadable(clientData.computeCapacity()))
                                    .withStyle(ChatFormatting.AQUA)) :
                    Component.translatable("cosmiccore.wireless_pda.hud.compute_unavailable");
            guiGraphics.drawString(mc.font, computeLine, -5, 74,
                    clientData.computeAvailable() ? 0xFFFFFFFF : 0xFFFF5555, true);
        }
    }

    public static void syncHud(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        ItemStack stack = CosmicCuriosUtils.getPDACurio(player);
        if (stack.isEmpty()) return;
        GlobalPos linkedPower = linkedPosition(stack, TAG_LOCAL_MONITOR);
        GlobalPos linkedCompute = linkedPosition(stack, TAG_ME_CONTROLLER);
        PowerTelemetry power = linkedPower == null ? readWirelessPower(player) : readLocalPower(player, linkedPower);
        ComputeTelemetry compute = linkedCompute == null ? ComputeTelemetry.unlinked() :
                readCompute(player, linkedCompute);
        CCoreNetwork.sendToPlayer(player, new SyncWirelessPDAHudPacket(
                power.local(), power.available(), power.stored(), power.capacity(), power.input(), power.output(),
                compute.linked(), compute.available(), compute.used(), compute.capacity()));
    }

    private static PowerTelemetry readWirelessPower(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        var team = MachineOwner.getOwner(playerUUID) instanceof FTBOwner ftbOwner ? ftbOwner.getTeam() : null;
        UUID owner = team != null ? team.getTeamId() : playerUUID;
        var wirelessData = WirelessEnergySavedData.getOrCreate(player.serverLevel());
        var capacity = wirelessData.getEnergyCapacity(owner);
        return new PowerTelemetry(false, capacity.signum() > 0,
                wirelessData.getEnergyStored(owner), capacity, wirelessData.getEnergyInput(owner),
                wirelessData.getEnergyOutput(owner));
    }

    public static void setClientData(boolean local, boolean available, BigInteger stored, BigInteger capacity,
                                     long input, long output, boolean computeLinked, boolean computeAvailable,
                                     long computeUsed, long computeCapacity) {
        clientData = new HudData(true, local, available, stored, capacity, input, output, computeLinked,
                computeAvailable, computeUsed, computeCapacity);
    }

    private static PowerTelemetry readLocalPower(ServerPlayer player, GlobalPos linked) {
        var level = player.server.getLevel(linked.dimension());
        if (level == null || !level.hasChunkAt(linked.pos()) ||
                !(MetaMachine.getMachine(level, linked.pos()) instanceof PowerCapacitorMachine capacitor) ||
                !capacitor.isFormed()) {
            return PowerTelemetry.unavailable();
        }
        var info = capacitor.getEnergyInfo();
        return new PowerTelemetry(true, true, info.stored(), info.capacity(), capacitor.getInputPerSec() / 20,
                capacitor.getOutputPerSec() / 20);
    }

    private static ComputeTelemetry readCompute(ServerPlayer player, GlobalPos linked) {
        var level = player.server.getLevel(linked.dimension());
        if (level == null || !level.hasChunkAt(linked.pos()) ||
                !(level.getBlockEntity(linked.pos()) instanceof ControllerBlockEntity controller) ||
                !controller.getMainNode().isReady()) {
            return ComputeTelemetry.unavailable();
        }
        var grid = controller.getMainNode().getGrid();
        if (grid == null) return ComputeTelemetry.unavailable();
        var snapshot = grid.getService(IComputeService.class).snapshot();
        return new ComputeTelemetry(true, true, snapshot.reservedCwut(), snapshot.capacityCwut());
    }

    private static String formatEnergy(BigInteger energy) {
        return FormattingUtil.formatNumberOrSic(energy, COMPACT_THRESHOLD);
    }

    private static void bind(ItemStack stack, String key, GlobalPos position) {
        ItemData.mutateTag(stack, root -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("Dimension", position.dimension().location().toString());
            tag.putLong("Position", position.pos().asLong());
            root.put(key, tag);
        });
    }

    @Nullable
    private static GlobalPos linkedPosition(ItemStack stack, String key) {
        var root = ItemData.readTag(stack);
        if (!root.contains(key)) return null;
        var tag = root.getCompound(key);
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
                           long input, long output, boolean computeLinked, boolean computeAvailable, long computeUsed,
                           long computeCapacity) {

        private static HudData empty() {
            return new HudData(false, false, false, BigInteger.ZERO, BigInteger.ZERO, 0, 0, false, false, 0, 0);
        }
    }

    private record PowerTelemetry(boolean local, boolean available, BigInteger stored, BigInteger capacity, long input,
                                  long output) {

        private static PowerTelemetry unavailable() {
            return new PowerTelemetry(true, false, BigInteger.ZERO, BigInteger.ZERO, 0, 0);
        }
    }

    private record ComputeTelemetry(boolean linked, boolean available, long used, long capacity) {

        private static ComputeTelemetry unlinked() {
            return new ComputeTelemetry(false, false, 0, 0);
        }

        private static ComputeTelemetry unavailable() {
            return new ComputeTelemetry(true, false, 0, 0);
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
