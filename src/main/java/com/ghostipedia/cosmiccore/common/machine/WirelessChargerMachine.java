package com.ghostipedia.cosmiccore.common.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.owner.ArgonautsOwner;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import dev.ftb.mods.ftbteams.data.PlayerTeam;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WirelessChargerMachine extends TieredEnergyMachine {

    private int tier;
    private ChargeMode mode;
    private long longRange;
    private long shortRange;
    private long chargeAmount;

    private TickableSubscription charge;

    List<Player> oldPlayerList = new ArrayList<>();

    public WirelessChargerMachine(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier, m -> {
            long chargeAmount = GTValues.V[tier];
            return new NotifiableEnergyContainer(chargeAmount * 64L, chargeAmount, 4, 0L, 0L) {

                @Override
                public long getInputAmperage() {
                    if (((WirelessChargerMachine) m).mode == ChargeMode.SUPER_CHARGED) {
                        return 4;
                    }
                    return 1;
                }
            };
        });
        this.tier = tier;
        mode = ChargeMode.SUPER_CHARGED;
        longRange = 1024L * (tier - GTValues.MV);
        shortRange = 512L * (tier - GTValues.MV);
        chargeAmount = GTValues.V[tier];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;

        charge = subscribeServerTick(this::chargeLoop);
    }

    @Override
    public void onUnload() {
        super.onUnload();

        if (charge != null) {
            charge.unsubscribe();
            charge = null;
        }
    }

    public void chargeLoop() {
        var maxChargeValue = chargeAmount * energyContainer.getInputAmperage();
        if (energyContainer.getEnergyStored() < maxChargeValue) return;
        int tickRate = mode == ChargeMode.SUPER_CHARGED ? 4 : 20;
        if (getOffsetTimer() % tickRate == 0) {
            var owner = getOwner();
            List<Player> players = new ArrayList<>();
            if (owner instanceof PlayerOwner) {
                UUID pUUID = owner.getUUID();
                Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(pUUID);
                if (player != null && isPlayerInRange(player)) players.add(player);
            } else if (owner instanceof FTBOwner ftbOwner) {
                var team = ftbOwner.getTeam();
                if (team == null) return;
                if (team.isPlayerTeam()) {
                    for (var pUUID : ((PlayerTeam) team).getEffectiveTeam().getMembers()) {
                        Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(pUUID);
                        if (player != null && isPlayerInRange(player)) players.add(player);
                    }
                } else if (team.isServerTeam() || team.isPartyTeam()) {
                    for (var pUUID : team.getMembers()) {
                        Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(pUUID);
                        if (player != null && isPlayerInRange(player)) players.add(player);
                    }
                }

            } else if (owner instanceof ArgonautsOwner) {
                // DN
            }

            if (!players.isEmpty()) {

                for (var player : players) {
                    if (GTCEu.Mods.isCuriosLoaded()) {
                        IItemHandler curios = CuriosApi.getCuriosInventory(player)
                                .<IItemHandler>map(ICuriosItemHandler::getEquippedCurios)
                                .orElse(EmptyItemHandler.INSTANCE);
                        for (int i = 0; i < curios.getSlots(); i++) {
                            var itemInSlot = curios.getStackInSlot(i);
                            var slotElectricItem = GTCapabilityHelper.getElectricItem(itemInSlot);
                            if (slotElectricItem != null && energyContainer.getEnergyStored() > maxChargeValue &&
                                    slotElectricItem.chargeable()) {
                                long chargedAmount = slotElectricItem.charge(maxChargeValue, tier, true, false);
                                energyContainer.changeEnergy(-chargedAmount);
                            }
                        }
                    }

                    var playerInv = player.getInventory();
                    for (int i = 0; i < playerInv.getContainerSize(); i++) {
                        var itemInSlot = playerInv.getItem(i);
                        var slotElectricItem = GTCapabilityHelper.getElectricItem(itemInSlot);
                        if (slotElectricItem != null && energyContainer.getEnergyStored() > maxChargeValue &&
                                slotElectricItem.chargeable()) {
                            long chargedAmount = slotElectricItem.charge(maxChargeValue, tier, true, false);
                            energyContainer.changeEnergy(-chargedAmount);
                        }
                    }
                }
            }

            List<Player> enteringPlayers = players.stream().filter(player -> !oldPlayerList.contains(player)).toList();
            List<Player> leavingPlayers = oldPlayerList.stream().filter(player -> !players.contains(player)).toList();
            int radius = mode == ChargeMode.SUPER_CHARGED ? (int) shortRange : (int) longRange;
            for (var player : enteringPlayers) {
                player.displayClientMessage(Component.translatable("cosmiccore.wireless_charger.enter_range",
                        FormattingUtil.formatNumbers(radius)), false);
            }
            for (var player : leavingPlayers) {
                player.displayClientMessage(Component.translatable("cosmiccore.wireless_charger.left_range",
                        FormattingUtil.formatNumbers(radius)), false);
            }

            if (oldPlayerList != players) oldPlayerList = players;
        }
    }

    private boolean isPlayerInRange(Player player) {
        int radius = mode == ChargeMode.SUPER_CHARGED ? (int) shortRange : (int) longRange;
        BlockPos a = new BlockPos(getBlockPos().offset(new Vec3i(-radius, -radius, -radius)));
        BlockPos b = new BlockPos(getBlockPos().offset(new Vec3i(radius, radius, radius)));
        var entityList = getLevel().getEntities(null, AABB.encapsulatingFullBlocks(a, b));
        return entityList.contains(player);
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!getLevel().isClientSide) {
            mode = ChargeMode.values()[((mode.ordinal() + 1) % ChargeMode.values().length)];
            if (mode == ChargeMode.SUPER_CHARGED) {
                context.getPlayer().displayClientMessage(Component.translatable("cosmiccore.wireless_charger.mode.0",
                        FormattingUtil.formatNumbers(shortRange)), false);
            } else if (mode == ChargeMode.MIXED) {
                context.getPlayer().displayClientMessage(Component.translatable("cosmiccore.wireless_charger.mode.1",
                        FormattingUtil.formatNumbers(longRange)), false);
            }
        }

        return super.onScrewdriverClick(context);
    }

    enum ChargeMode {
        SUPER_CHARGED,
        MIXED;
    }
}
