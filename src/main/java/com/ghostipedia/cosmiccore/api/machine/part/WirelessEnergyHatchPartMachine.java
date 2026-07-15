package com.ghostipedia.cosmiccore.api.machine.part;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.ISubscription;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;

import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyHatchPartMachine extends TieredIOPartMachine {

    protected static final long ticks_between_save_data_operations = 5L * 20L;

    @Persisted
    public final NotifiableEnergyContainer energyContainer;
    protected TickableSubscription wirelessSub;
    @Nullable
    protected ISubscription energyListener;
    @Getter
    protected int amperage;

    public WirelessEnergyHatchPartMachine(BlockEntityCreationInfo holder, int tier, IO io, int amperage) {
        super(holder, tier, io);
        this.amperage = amperage;
        this.energyContainer = createEnergyContainer();
    }

    protected NotifiableEnergyContainer createEnergyContainer() {
        NotifiableEnergyContainer container;
        if (this.io == IO.OUT) {
            container = NotifiableEnergyContainer.emitterContainer(getEnergyCapacity(tier, amperage),
                    GTValues.V[tier], amperage);
            container.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        } else {
            container = NotifiableEnergyContainer.receiverContainer(getEnergyCapacity(tier, amperage),
                    GTValues.V[tier], amperage);
            container.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        }
        return container;
    }

    public static long getEnergyCapacity(int tier, int amperage) {
        // Capacity is twice the maximum throughput over the duration between saveData calls
        return GTValues.V[tier] * amperage * ((long) (ticks_between_save_data_operations * 1.1));
    }

    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel)
            serverLevel.getServer().tell(new TickTask(0, this::updateWirelessSubscription));
        energyListener = energyContainer.addChangedListener(this::updateWirelessSubscription);
        // 8.0.0: onMachinePlaced was removed; the input-hatch wireless EU gap-fill moved here. Idempotent
        // because energyContainer persists (fresh place fills from the global buffer; on reload the gap is ~0).
        if (io == IO.IN && getLevel() instanceof ServerLevel sl) {
            var data = WirelessEnergySavedData.getOrCreate(sl);
            var owner = getTeamUUID();
            long euToTransfer = energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
            long euTransferred = data.addEUToGlobalWirelessEnergy(owner, -euToTransfer);
            energyContainer.changeEnergy(euToTransfer - euTransferred);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energyListener != null) {
            energyListener.unsubscribe();
            energyListener = null;
        }
    }

    protected void updateWirelessSubscription() {
        if (isWorkingEnabled()) wirelessSub = subscribeServerTick(wirelessSub, this::wirelessHandler);
        else if (wirelessSub != null) {
            wirelessSub.unsubscribe();
            wirelessSub = null;
        }
    }

    protected UUID getTeamUUID() {
        if (getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(getOwnerUUID());
            if (team != null) return team.getTeamId();
        }
        return getOwnerUUID();
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        if (getLevel() instanceof ServerLevel serverLevel) {
            var data = WirelessEnergySavedData.getOrCreate(serverLevel);
            var owner = getTeamUUID();
            data.removeEnergyBuffered(owner, getBlockPos());
            if (io == IO.OUT) data.removeEnergyInput(owner, getBlockPos());
            if (io == IO.IN) data.removeEnergyOutput(owner, getBlockPos());
            data.addEUToGlobalWirelessEnergy(owner, energyContainer.getEnergyStored());
            energyContainer.setEnergyStored(0L);
        }
    }

    protected void wirelessHandler() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            if (isWorkingEnabled()) {
                if (getOffsetTimer() % 20 == 0) {
                    var data = WirelessEnergySavedData.getOrCreate(serverLevel);
                    var owner = getTeamUUID();
                    data.setEnergyBuffered(owner, getBlockPos(), energyContainer.getEnergyStored());
                    if (io == IO.IN) data.setEnergyOutput(owner, getBlockPos(), energyContainer.getOutputPerSec() / 20);
                    if (io == IO.OUT) data.setEnergyInput(owner, getBlockPos(), energyContainer.getInputPerSec() / 20);
                }
                if (getOffsetTimer() % ticks_between_save_data_operations == 0) {
                    var data = WirelessEnergySavedData.getOrCreate(serverLevel);
                    var owner = getTeamUUID();

                    if (data.isActive(owner)) {
                        if (io == IO.IN) {
                            long euToTransfer = energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
                            long euTransferred = data.addEUToGlobalWirelessEnergy(owner, -euToTransfer);
                            energyContainer.changeEnergy(euToTransfer - euTransferred);
                        } else if (io == IO.OUT) {
                            long euToTransfer = energyContainer.getEnergyStored();
                            long euTransferred = data.addEUToGlobalWirelessEnergy(owner, euToTransfer);
                            energyContainer.changeEnergy(-(euToTransfer - euTransferred));
                        }
                    }
                }
            }
        }
    }

    public static Component[] getTooltipComponents(int tier, IO io, int amperage) {
        var tooltip = new ArrayList<Component>();

        if (io == IO.IN) {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.voltage_in",
                    FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]));
            tooltip.add(Component.translatable("gtceu.universal.tooltip.amperage_in", amperage));
        } else if (io == IO.OUT) {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.voltage_out",
                    FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]));
            tooltip.add(Component.translatable("gtceu.universal.tooltip.amperage_out", amperage));
        }

        tooltip.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                FormattingUtil.formatNumbers(getEnergyCapacity(tier, amperage))));

        if (io == IO.IN) {
            tooltip.add(Component.translatable(amperage > 1 ? "gtceu.machine.energy_hatch.input_hi_amp.tooltip" :
                    "gtceu.machine.energy_hatch.input.tooltip"));
        } else if (io == IO.OUT) {
            tooltip.add(Component.translatable(amperage > 1 ? "gtceu.machine.energy_hatch.output_hi_amp.tooltip" :
                    "gtceu.machine.energy_hatch.output.tooltip"));
        }

        return tooltip.toArray(new Component[0]);
    }
}
