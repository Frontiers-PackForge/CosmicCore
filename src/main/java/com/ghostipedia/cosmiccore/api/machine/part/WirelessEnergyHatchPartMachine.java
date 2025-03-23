package com.ghostipedia.cosmiccore.api.machine.part;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyHatchPartMachine extends TieredIOPartMachine {

    protected static final long ticks_between_save_data_operations = 10L * 20L;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessEnergyHatchPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableEnergyContainer energyContainer;
    protected TickableSubscription wirelessSub;
    @Nullable
    protected ISubscription energyListener;
    @Getter
    protected int amperage;

    public WirelessEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage) {
        super(holder, tier, io);
        this.amperage = amperage;
        this.energyContainer = createEnergyContainer();
    }

    protected NotifiableEnergyContainer createEnergyContainer() {
        NotifiableEnergyContainer container;
        if (this.io == IO.IN) {
            container = NotifiableEnergyContainer.emitterContainer(this, getEnergyCapacity(tier, amperage), GTValues.V[tier], amperage);
            container.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        } else {
            container = NotifiableEnergyContainer.receiverContainer(this, getEnergyCapacity(tier, amperage), GTValues.V[tier], amperage);
            container.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        }
        return container;
    }

    public static long getEnergyCapacity(int tier, int amperage) {
        // Capacity is twice the maximum throughput over the duration between saveData calls
        return GTValues.V[tier] * amperage * ticks_between_save_data_operations * 2;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) serverLevel.getServer().tell(new TickTask(0, this::updateWirelessSubscription));
        energyListener = energyContainer.addChangedListener(this::updateWirelessSubscription);
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

    protected void wirelessHandler() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            if (isWorkingEnabled()) {
                if (getOffsetTimer() % ticks_between_save_data_operations == 0) {
                    var data = WirelessEnergySavedData.getOrCreate(serverLevel);
                    var owner = getHolder().getOwner().getUUID();

                    //Handle IO
                    if (io == IO.IN) {
                        long euToTransfer = energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
                        long euTransferred = data.addEUToGlobalWirelessEnergy(owner, - euToTransfer);
                        energyContainer.changeEnergy(euToTransfer - euTransferred);
                    } else if (io == IO.OUT) {
                        long euToTransfer = energyContainer.getEnergyStored();
                        long euTransferred = data.addEUToGlobalWirelessEnergy(owner, euToTransfer);
                        energyContainer.changeEnergy(-(euToTransfer - euTransferred));
                    }
                    data.setEnergyBuffered(owner, getPos(), energyContainer.getEnergyStored());
                }
            } else {
                var data = WirelessEnergySavedData.getOrCreate(serverLevel);
                var owner = getHolder().getOwner().getUUID();

                data.addEUToGlobalWirelessEnergy(owner, energyContainer.getEnergyStored());
                data.removeEnergyBuffered(owner, getPos());
            }
        }
    }

    private long getPassiveDrain() {
        return 0L;
    }
}
