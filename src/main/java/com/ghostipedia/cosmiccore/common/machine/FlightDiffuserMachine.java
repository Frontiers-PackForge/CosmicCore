package com.ghostipedia.cosmiccore.common.machine;

import com.ghostipedia.cosmiccore.common.flight.FlightDiffuserBehavior;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.UITexture;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FlightDiffuserMachine extends TieredEnergyMachine implements IControllable {

    public static final long AMPERAGE = 8L;
    public static final int RANGE = 256;
    public static final int RANGEBOOST = 64;

    private static final int CHECK_RATE = 5;

    @Getter
    @SaveField
    @SyncToClient
    private boolean isWorkingEnabled = true;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private boolean active;
    @Getter
    private final int range;
    private boolean drainingEnergy;
    private @Nullable TickableSubscription fieldSubscription;

    public FlightDiffuserMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, new NotifiableEnergyContainer(GTValues.V[tier] * AMPERAGE * 64L,
                GTValues.V[tier], AMPERAGE, 0L, 0L));
        range = RANGE + RANGEBOOST * (tier - GTValues.LV);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        energyContainer.addChangedListener(() -> {
            if (!drainingEnergy) updateSubscription();
        });
        updateSubscription();
    }

    @Override
    public void onUnload() {
        stopField();
        super.onUnload();
    }

    private void updateSubscription() {
        boolean shouldRun = isWorkingEnabled && canDrainEnergy();
        if (shouldRun) {
            fieldSubscription = subscribeServerTick(fieldSubscription, this::fieldTick);
            setActive(true);
        } else {
            stopField();
        }
    }

    private void fieldTick() {
        if (!drainEnergy()) {
            updateSubscription();
            return;
        }
        if (getOffsetTimer() % CHECK_RATE == 0) {
            refreshPlayers();
        }
    }

    private boolean canDrainEnergy() {
        return energyContainer.getEnergyStored() >= GTValues.V[getTier()] * AMPERAGE;
    }

    private boolean drainEnergy() {
        long amount = GTValues.V[getTier()] * AMPERAGE;
        if (energyContainer.getEnergyStored() < amount) return false;
        drainingEnergy = true;
        try {
            energyContainer.removeEnergy(amount);
        } finally {
            drainingEnergy = false;
        }
        return true;
    }

    private void refreshPlayers() {
        if (!(getLevel() instanceof ServerLevel level)) return;
        GlobalPos source = GlobalPos.of(level.dimension(), getBlockPos());
        double centerX = getBlockPos().getX() + 0.5;
        double centerY = getBlockPos().getY() + 0.5;
        double centerZ = getBlockPos().getZ() + 0.5;
        for (var player : level.players()) {
            if (Math.abs(player.getX() - centerX) <= range && Math.abs(player.getY() - centerY) <= range &&
                    Math.abs(player.getZ() - centerZ) <= range) {
                FlightDiffuserBehavior.refresh(player, source);
            }
        }
    }

    private void stopField() {
        if (fieldSubscription != null) {
            fieldSubscription.unsubscribe();
            fieldSubscription = null;
        }
        removeSource();
        setActive(false);
    }

    private void removeSource() {
        if (getLevel() instanceof ServerLevel level) {
            FlightDiffuserBehavior.removeSource(level.getServer(), GlobalPos.of(level.dimension(), getBlockPos()));
        }
    }

    private void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_ACTIVE, active));
        syncDataHolder.markClientSyncFieldDirty("active");
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_WORKING_ENABLED, workingEnabled));
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
        updateSubscription();
    }

    @Override
    public @Nullable UITexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                        ItemStack held, net.minecraft.core.Direction side) {
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return isWorkingEnabled ? GTGuiTextures.TOOL_PAUSE : GTGuiTextures.TOOL_START;
        }
        return super.sideTips(player, pos, state, toolTypes, held, side);
    }
}
