package com.ghostipedia.cosmiccore.common.airControl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.airControl.RebreatherHelper.RebreatherType;
import com.ghostipedia.cosmiccore.common.armor.InstalledOxygen;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.item.behavior.OxygenSupplyTankBehavior;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.OxygenWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncOxygenBarPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.ghostipedia.cosmiccore.common.airControl.OxygenConfig.*;
import static com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap.OXYGEN_SUPPLY;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class OxygenLogic {

    private OxygenLogic() {}

    private record SyncSample(long oxygenTicks, long gameTime) {}

    private static final Map<UUID, SyncSample> lastSync = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long gameTime = player.serverLevel().getGameTime();
        if (player.isCreative() || player.isSpectator()) {
            if (gameTime % HUD_SYNC_INTERVAL == 0) {
                lastSync.remove(player.getUUID());
                CCoreNetwork.sendToPlayer(player,
                        new SyncOxygenBarPacket(MAX_OXYGEN_TICKS, MAX_OXYGEN_TICKS, false, 0, 0, false));
            }
            return;
        }

        var budget = player.getData(CosmicAttachmentTypes.OXYGEN_BUDGET);
        var environment = OxygenRules.resolve(player.level().dimension(), player.blockPosition().getY());
        var quality = environment.airQuality;
        var rates = environment.rates;
        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (!player.level().getFluidState(eyePos).isEmpty() && !player.hasEffect(MobEffects.WATER_BREATHING)) {
            var noAir = OxygenRules.QUALITY_RATES.get(OxygenRules.AirQuality.NO_AIR);
            rates.oxygenDrainPerTick = Math.max(rates.oxygenDrainPerTick, noAir.oxygenDrainPerTick);
            rates.oxygenRecoveryPerTick = 0;
            rates.suffocationDamage = Math.max(rates.suffocationDamage, noAir.suffocationDamage);
            quality = OxygenRules.AirQuality.NO_AIR;
        }

        var rebreather = RebreatherHelper.getEquippedRebreather(player);
        boolean protectedFromThinAir = quality == OxygenRules.AirQuality.THIN && rebreather != RebreatherType.NONE;
        double drain = protectedFromThinAir ? 0 : rates.oxygenDrainPerTick;
        var chest = player.getItemBySlot(EquipmentSlot.CHEST);
        var backTanks = rebreather == RebreatherType.PRESSURIZED ? backSlotStacks(player) : List.<ItemStack>of();
        long previous = budget.getOxygenTicks();
        boolean suffocating = budget.tick(drain, rates.oxygenRecoveryPerTick,
                requested -> drainFromTanks(chest, backTanks, requested, rebreather));
        long remaining = budget.getOxygenTicks();

        if (remaining < previous) {
            for (int seconds : WARNING_SECONDS) {
                long threshold = seconds * 20L;
                if (previous > threshold && remaining <= threshold) {
                    CCoreNetwork.sendToPlayer(player, new OxygenWarnPacket("cosmiccore.oxygen.warn", seconds));
                    break;
                }
            }
        }
        if (suffocating && rates.suffocationDamage > 0 && gameTime % SUFFOCATION_DAMAGE_INTERVAL == 0) {
            player.hurt(player.damageSources().drown(), rates.suffocationDamage);
        }

        if (gameTime % HUD_SYNC_INTERVAL == 0) {
            var previousSync = lastSync.put(player.getUUID(), new SyncSample(remaining, gameTime));
            double rate = previousSync != null && gameTime > previousSync.gameTime() ?
                    (remaining - previousSync.oxygenTicks()) * 20.0 / (gameTime - previousSync.gameTime()) : 0;
            long tankSeconds = rebreather == RebreatherType.PRESSURIZED ?
                    tankSecondsRemaining(tankTicksRemaining(chest, backTanks), remaining, drain) : 0;
            CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(remaining, MAX_OXYGEN_TICKS,
                    drain > 0 || remaining < MAX_OXYGEN_TICKS, rate, tankSeconds, suffocating));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncReserve(player);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncReserve(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        lastSync.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!event.isEndConquered()) player.getData(CosmicAttachmentTypes.OXYGEN_BUDGET).reset();
            syncReserve(player);
        }
    }

    private static void syncReserve(ServerPlayer player) {
        lastSync.remove(player.getUUID());
        long remaining = player.getData(CosmicAttachmentTypes.OXYGEN_BUDGET).getOxygenTicks();
        CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(remaining, MAX_OXYGEN_TICKS,
                !player.isCreative() && !player.isSpectator() && remaining < MAX_OXYGEN_TICKS, 0, 0, false));
    }

    public static int drainFromTanks(ItemStack chest, List<ItemStack> backTanks, int requestTicks,
                                     RebreatherType rebreather) {
        if (requestTicks <= 0 || rebreather != RebreatherType.PRESSURIZED) return 0;
        int remaining = requestTicks - InstalledOxygen.drain(chest, requestTicks);
        for (ItemStack stack : backTanks) {
            if (remaining <= 0) break;
            var provider = stack.getCapability(OXYGEN_SUPPLY);
            if (provider != null) {
                remaining -= Math.clamp(provider.drainOxygenTicks(stack, remaining), 0, remaining);
            }
        }
        return requestTicks - remaining;
    }

    private static List<ItemStack> backSlotStacks(Player player) {
        var inventory = CuriosApi.getCuriosInventory(player);
        if (inventory.isEmpty()) return List.of();
        var backHandler = inventory.get().getStacksHandler("back");
        if (backHandler.isEmpty()) return List.of();
        var stacks = backHandler.get().getStacks();
        List<ItemStack> tanks = new ArrayList<>();
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            if (!stack.isEmpty()) tanks.add(stack);
        }
        return tanks;
    }

    public static long tankTicksRemaining(ItemStack chest, List<ItemStack> backTanks) {
        long total = InstalledOxygen.remaining(chest);
        for (ItemStack stack : backTanks) {
            if (stack.getCapability(OXYGEN_SUPPLY) != null) {
                total += OxygenSupplyTankBehavior.remainingTicks(stack);
            }
        }
        return total;
    }

    static long tankSecondsRemaining(long tankTicks, long reserve, double drainPerTick) {
        if (tankTicks <= 0 || drainPerTick <= 0) return 0;
        double refillTicks = (MAX_OXYGEN_TICKS - reserve) / (double) TANK_TOPUP_TICKS_PER_TICK;
        double refillCost = refillTicks * (drainPerTick + TANK_TOPUP_TICKS_PER_TICK);
        double lifetime = tankTicks < refillCost ? tankTicks / (drainPerTick + TANK_TOPUP_TICKS_PER_TICK) :
                refillTicks + (tankTicks - refillCost) / drainPerTick;
        return (long) (lifetime / 20);
    }
}
