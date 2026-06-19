package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.SyncPredictedVeinsPacket;
import com.ghostipedia.cosmiccore.common.worldgen.survey.VeinSurveyUtil;
import com.ghostipedia.cosmiccore.common.worldgen.survey.VeinSurveyUtil.VeinInfo;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VeinSurveyBehavior implements IInteractionItem, IAddInformation {

    public enum ScanMode {

        RADIAL("cosmiccore.survey.mode.radial", 360),
        DIRECTIONAL("cosmiccore.survey.mode.directional", 90),
        NEAREST("cosmiccore.survey.mode.nearest", 360);

        public final String translationKey;
        public final int coneAngle;

        ScanMode(String translationKey, int coneAngle) {
            this.translationKey = translationKey;
            this.coneAngle = coneAngle;
        }
    }

    public enum DetailLevel {

        BASIC(1),
        COMPASS(2),
        PRECISE(3);

        public final int level;

        DetailLevel(int level) {
            this.level = level;
        }
    }

    private final int radius;
    private final long energyCost;
    private final DetailLevel detailLevel;

    public VeinSurveyBehavior(int radius, long energyCost, DetailLevel detailLevel) {
        this.radius = radius;
        this.energyCost = energyCost;
        this.detailLevel = detailLevel;
    }

    public ScanMode getMode(ItemStack stack) {
        if (stack.isEmpty()) return ScanMode.RADIAL;
        int modeIdx = ItemData.readTag(stack).getInt("ScanMode") % ScanMode.values().length;
        return ScanMode.values()[modeIdx];
    }

    public void setNextMode(ItemStack stack) {
        int nextMode = (ItemData.readTag(stack).getInt("ScanMode") + 1) % ScanMode.values().length;
        ItemData.mutateTag(stack, tag -> tag.putInt("ScanMode", nextMode));
    }

    @Nullable
    public String getVeinFilter(ItemStack stack) {
        if (stack.isEmpty()) return null;
        String filter = ItemData.readTag(stack).getString("VeinFilter");
        return filter.isEmpty() ? null : filter;
    }

    public void setVeinFilter(ItemStack stack, @Nullable String filter) {
        ItemData.mutateTag(stack, tag -> {
            if (filter == null || filter.isEmpty()) {
                tag.remove("VeinFilter");
            } else {
                tag.putString("VeinFilter", filter);
            }
        });
    }

    public boolean drainEnergy(ItemStack stack, boolean simulate) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;
        return electricItem.discharge(energyCost, Integer.MAX_VALUE, true, false, simulate) >= energyCost;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                setNextMode(stack);
                ScanMode mode = getMode(stack);
                player.sendSystemMessage(Component.translatable(mode.translationKey)
                        .withStyle(ChatFormatting.YELLOW));
                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundSource.PLAYERS, 0.5f, 1.2f);
            }
            return InteractionResultHolder.success(stack);
        }

        if (!player.isCreative() && !drainEnergy(stack, true)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("cosmiccore.survey.no_energy")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel &&
                player instanceof ServerPlayer serverPlayer) {
            if (!player.isCreative()) drainEnergy(stack, false);

            ScanMode mode = getMode(stack);
            performScan(serverLevel, serverPlayer, stack, mode);

            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.5f, 0.8f);
        }

        return InteractionResultHolder.success(stack);
    }

    private void performScan(ServerLevel level, ServerPlayer player, ItemStack stack, ScanMode mode) {
        BlockPos center = player.blockPosition();

        IWorldGenLayer layer = WorldGeneratorUtils.WORLD_GEN_LAYERS.values().stream()
                .filter(l -> l.isApplicableForLevel(level.dimension()))
                .findFirst()
                .orElse(null);

        List<VeinInfo> allVeins = VeinSurveyUtil.surveyVeins(level, center, radius, layer);

        if (mode == ScanMode.DIRECTIONAL) {
            Vec3 lookVec = player.getLookAngle();
            double playerYaw = Math.atan2(lookVec.x, lookVec.z);

            allVeins = allVeins.stream()
                    .filter(vein -> isInCone(center, vein.center(), playerYaw, mode.coneAngle))
                    .toList();
        }

        String filter = getVeinFilter(stack);
        if (filter != null) {
            String filterLower = filter.toLowerCase();
            allVeins = allVeins.stream()
                    .filter(v -> v.getVeinName().toLowerCase().contains(filterLower))
                    .toList();
        }

        if (mode == ScanMode.NEAREST) {
            displayNearestResult(player, allVeins, center, filter);
        } else {
            displayScanResults(player, allVeins, center, mode, filter);
        }

        CCoreNetwork.sendToPlayer(player, new SyncPredictedVeinsPacket(allVeins, true));
    }

    private boolean isInCone(BlockPos from, BlockPos to, double facingYaw, int coneAngle) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        if (dx == 0 && dz == 0) return true;

        double toYaw = Math.atan2(dx, dz);
        double angleDiff = Math.abs(normalizeAngle(toYaw - facingYaw));
        return angleDiff <= Math.toRadians(coneAngle / 2.0);
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    private void displayScanResults(ServerPlayer player, List<VeinInfo> veins, BlockPos center,
                                    ScanMode mode, @Nullable String filter) {
        if (veins.isEmpty()) {
            MutableComponent msg = filter != null ?
                    Component.translatable("cosmiccore.survey.no_veins.filtered", filter) :
                    mode == ScanMode.DIRECTIONAL ? Component.translatable("cosmiccore.survey.no_veins.directional") :
                            Component.translatable("cosmiccore.survey.no_veins");
            player.sendSystemMessage(msg.withStyle(ChatFormatting.RED));
            return;
        }

        MutableComponent header = Component.literal("")
                .append(Component.translatable("cosmiccore.survey.header").withStyle(ChatFormatting.GOLD))
                .append("\n")
                .append(Component.translatable("cosmiccore.survey.found", veins.size(), radius)
                        .withStyle(ChatFormatting.GRAY));

        if (mode == ScanMode.DIRECTIONAL) {
            header.append(Component.translatable("cosmiccore.survey.found.directional")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        player.sendSystemMessage(header);

        Map<String, Long> veinCounts = veins.stream()
                .collect(Collectors.groupingBy(VeinInfo::getVeinName, Collectors.counting()));

        MutableComponent typeSummary = Component.translatable("cosmiccore.survey.types")
                .withStyle(ChatFormatting.GRAY);
        boolean first = true;
        for (var entry : veinCounts.entrySet()) {
            if (!first) typeSummary.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
            first = false;
            typeSummary.append(Component.literal(entry.getKey()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("×" + entry.getValue()).withStyle(ChatFormatting.AQUA));
        }
        player.sendSystemMessage(typeSummary);

        player.sendSystemMessage(Component.translatable("cosmiccore.survey.nearest")
                .withStyle(ChatFormatting.YELLOW));

        int displayCount = Math.min(10, veins.size());
        for (int i = 0; i < displayCount; i++) {
            sendVeinEntry(player, veins.get(i), center, i + 1);
        }

        if (veins.size() > 10) {
            player.sendSystemMessage(Component.translatable("cosmiccore.survey.more", veins.size() - 10)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private void displayNearestResult(ServerPlayer player, List<VeinInfo> veins, BlockPos center,
                                      @Nullable String filter) {
        if (veins.isEmpty()) {
            MutableComponent msg = filter != null ?
                    Component.translatable("cosmiccore.survey.no_veins.filtered", filter) :
                    Component.translatable("cosmiccore.survey.no_veins");
            player.sendSystemMessage(msg.withStyle(ChatFormatting.RED));
            return;
        }

        VeinInfo nearest = veins.get(0);
        MutableComponent header = filter != null ?
                Component.translatable("cosmiccore.survey.nearest_vein.filtered", filter) :
                Component.translatable("cosmiccore.survey.nearest_vein");
        player.sendSystemMessage(header.withStyle(ChatFormatting.GOLD));
        sendVeinEntry(player, nearest, center, 1);
    }

    private void sendVeinEntry(ServerPlayer player, VeinInfo vein, BlockPos from, int index) {
        BlockPos pos = vein.center();
        int distance = vein.horizontalDistanceFrom(from);
        String direction = vein.directionFrom(from);

        ChatFormatting nameColor = vein.isConfirmed() ? ChatFormatting.AQUA : ChatFormatting.GRAY;
        String confidenceIndicator = vein.isConfirmed() ? "" : "? ";

        MutableComponent entry = Component.literal("  " + index + ". ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(confidenceIndicator + vein.getVeinName()).withStyle(nameColor))
                .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(distance + "m").withStyle(ChatFormatting.WHITE));

        if (detailLevel.level >= DetailLevel.COMPASS.level) {
            entry.append(Component.literal(" " + direction).withStyle(ChatFormatting.WHITE));
        }

        if (detailLevel.level >= DetailLevel.PRECISE.level) {
            entry.append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY));
            if (player.isCreative()) {
                String tpCommand = "/tp @s " + pos.getX() + " ~ " + pos.getZ();
                entry.append(Component.literal("X:" + pos.getX() + " Z:" + pos.getZ())
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.GREEN)
                                .withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tpCommand))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("cosmiccore.survey.click_tp")))));
            } else {
                entry.append(Component.literal("X:" + pos.getX() + " Z:" + pos.getZ())
                        .withStyle(ChatFormatting.GREEN));
            }
            entry.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
        }

        player.sendSystemMessage(entry);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        ScanMode mode = getMode(stack);
        String filter = getVeinFilter(stack);

        tooltipComponents.add(Component.translatable("cosmiccore.survey.tooltip.radius", radius)
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("cosmiccore.survey.tooltip.mode")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(mode.translationKey).withStyle(ChatFormatting.YELLOW)));

        if (filter != null) {
            tooltipComponents.add(Component.translatable("cosmiccore.survey.tooltip.filter")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(filter).withStyle(ChatFormatting.AQUA)));
        }

        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("cosmiccore.survey.tooltip.use")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("cosmiccore.survey.tooltip.shift")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
