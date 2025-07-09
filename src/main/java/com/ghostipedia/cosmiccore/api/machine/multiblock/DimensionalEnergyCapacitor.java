package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.data.savedData.UniqueMultiblockSavedData;
import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.DummyWorld;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DimensionalEnergyCapacitor extends DimensionalEnergyInterface {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DimensionalEnergyCapacitor.class, DimensionalEnergyInterface.MANAGED_FIELD_HOLDER);

    public static final int MAX_BATTERY_LAYER = 18;
    public static final int MIN_CASINGS = 14;

    // Passive Drain Constants
    // 1% capacity per 24 hours
    public static final long PASSIVE_DRAIN_DIVISOR = 20 * 60 * 60 * 24 * 100;
    // no more than 100kEU/t per storage block
    public static final long PASSIVE_DRAIN_MAX_PER_STORAGE = 100_000L;

    // Used to make sure you cannot have more than one of this multiblock per player / team
    @Persisted
    public boolean isDuplicate = false;

    @Persisted
    private long[] capacities;

    public DimensionalEnergyCapacitor(IMachineBlockEntity holder) {
        super(holder);
        this.localDisplay = false;
    }

    @Override
    public void onStructureFormed() {
        if (getLevel() instanceof DummyWorld) super.onStructureFormed();

        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            if (owner == null) {
                CosmicCore.LOGGER.warn("DimensionalEnergyCapcitor tried to form with null team.");
                return;
            }
            var multiblockId = getDefinition().getId().toString();
            var wirelessData = WirelessEnergySavedData.getOrCreate(serverLevel);
            var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);

            if (uniqueMultiblockMapping.hasData(owner, multiblockId, getDimension())) {
                this.isDuplicate = !uniqueMultiblockMapping.isUnique(owner, multiblockId, getDimension(), getPos());
                if (isDuplicate) {
                    recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
                    return;
                }
            } else uniqueMultiblockMapping.addMultiblock(owner, getDefinition().getId().toString(), getDimension(),
                    getPos());

            List<IBatteryData> batteries = new ArrayList<>();
            for (Map.Entry<String, Object> battery : getMultiblockState().getMatchContext().entrySet()) {
                if (battery.getKey().startsWith(PowerSubstationMachine.PMC_BATTERY_HEADER) &&
                        battery.getValue() instanceof PowerSubstationMachine.BatteryMatchWrapper wrapper) {
                    for (int i = 0; i < wrapper.getAmount(); i++) {
                        batteries.add(wrapper.getPartType());
                    }
                }
            }

            this.capacities = batteries.stream().mapToLong(IBatteryData::getCapacity).toArray();

            if (batteries.isEmpty()) {
                onStructureInvalid();
                return;
            }

            super.onStructureFormed(); // This order is important do not move

            var capacity = batteries.stream().mapToLong(IBatteryData::getCapacity)
                    .mapToObj(BigInteger::valueOf).reduce(BigInteger.ZERO, BigInteger::add);

            wirelessData.setCapacity(owner, capacity);
            wirelessData.setActive(owner, true);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            if (owner != null) {
                var wirelessData = WirelessEnergySavedData.getOrCreate(serverLevel);
                var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);
                wirelessData.setActive(owner, false);
                uniqueMultiblockMapping.removeMultiblock(owner, getDefinition().getId().toString(), getDimension(),
                        getPos());
            }
        }
        this.capacities = null;
    }

    @Override
    public boolean isActive() {
        if (isDuplicate) return false;
        return super.isActive();
    }

    @Override
    public long getPassiveDrainPerTick() {
        long[] drains = Arrays.stream(capacities)
                .map(cap -> Math.min(PASSIVE_DRAIN_MAX_PER_STORAGE, cap / PASSIVE_DRAIN_DIVISOR)).toArray();
        return Arrays.stream(drains).sum();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (this.isDuplicate) {
            textList.add(Component.translatable("cosmic.multiblock.capacitor.duplicate.multiblock.1")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
            textList.add(Component.translatable("cosmic.multiblock.capacitor.duplicate.multiblock.2")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
        } else super.addDisplayText(textList);
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            if (owner != null) {
                var wirelessData = WirelessEnergySavedData.getOrCreate(serverLevel);
                wirelessData.setActive(owner, isWorkingAllowed);
            }
        }
    }

    private boolean hasOwner() {
        var owner = getTeamUUID();
        return owner != null;
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        super.attachTooltips(tooltipsPanel);
        tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(
                () -> GuiTextures.INDICATOR_NO_ENERGY,
                () -> List.of(Component.translatable("cosmic.multiblock.capacitor.owner.null")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))),
                () -> (!this.hasOwner()),
                () -> null));
    }

    private String getDimension() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            return serverLevel.dimension().location().toString();
        }
        return null;
    }
}
