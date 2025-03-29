package com.ghostipedia.cosmiccore.common.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.owner.ArgonautsOwner;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.server.ServerLifecycleHooks;

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

    public WirelessChargerMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
        this.tier = tier;
        mode = ChargeMode.SUPER_CHARGED;
        longRange = 1024L * (tier - GTValues.MV);
        shortRange = 512L * (tier - GTValues.MV);
        chargeAmount = GTValues.V[tier];
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        long chargeAmount = GTValues.V[getTier()];
        return new NotifiableEnergyContainer(this, chargeAmount * 64L, chargeAmount, 4, 0L, 0L) {

            @Override
            public long getInputAmperage() {
                if (mode == ChargeMode.SUPER_CHARGED) {
                    return 4;
                }
                return 1;
            };
        };
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
                for (var pUUID : team.getMembers()) {
                    Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(pUUID);
                    if (player != null && isPlayerInRange(player)) players.add(player);
                }

            } else if (owner instanceof ArgonautsOwner) {
                // DN
            }

            if (!players.isEmpty()) {

                for (var player : players) {
                    if (GTCEu.Mods.isCuriosLoaded()) {
                        IItemHandler curios = CuriosApi.getCuriosInventory(player)
                                .<IItemHandler>map(ICuriosItemHandler::getEquippedCurios)
                                .orElse(EmptyHandler.INSTANCE);
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
        BlockPos a = new BlockPos(getPos().offset(new Vec3i(-radius, -radius, -radius)));
        BlockPos b = new BlockPos(getPos().offset(new Vec3i(radius, radius, radius)));
        var entityList = getLevel().getEntities(null, new AABB(a, b));
        return entityList.contains(player);
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (!getLevel().isClientSide) {
            mode = ChargeMode.values()[((mode.ordinal() + 1) % ChargeMode.values().length)];
            if (mode == ChargeMode.SUPER_CHARGED) {
                playerIn.displayClientMessage(Component.translatable("cosmiccore.wireless_charger.mode.0",
                        FormattingUtil.formatNumbers(shortRange)), false);
            } else if (mode == ChargeMode.MIXED) {
                playerIn.displayClientMessage(Component.translatable("cosmiccore.wireless_charger.mode.1",
                        FormattingUtil.formatNumbers(longRange)), false);
            }
        }

        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    enum ChargeMode {
        SUPER_CHARGED,
        MIXED;
    }
}
