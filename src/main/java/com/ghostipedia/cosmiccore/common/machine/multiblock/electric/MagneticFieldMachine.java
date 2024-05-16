package com.ghostipedia.cosmiccore.common.machine.multiblock.electric;

import com.ghostipedia.cosmiccore.api.machine.multiblock.MagnetWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MagneticFieldMachine extends MagnetWorkableElectricMultiblockMachine implements ITieredMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MagneticFieldMachine.class, MagnetWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);
    @Getter
    private int fieldChargeRate;
    @Getter
    @Persisted
    private int fieldStrength = 0;
    @Nullable
    protected EnergyContainerList inputEnergyContainers;
    @Nullable
    protected TickableSubscription preMagnetSubs;

    public MagneticFieldMachine(IMachineBlockEntity holder) {
        super(holder);
    }
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE || io == IO.OUT) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                    traitSubscriptions.add(handler.addChangedListener(this::updateMagnetFieldSubscription));
                }
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
        updateMagnetFieldSubscription();
    }

    protected void updateMagnetFieldSubscription() {
        if (fieldStrength > 0 || (inputEnergyContainers != null && inputEnergyContainers.getEnergyStored() > 0)) {
            preMagnetSubs = subscribeServerTick(preMagnetSubs, this::updateFieldStrength);
        } else if (preMagnetSubs != null) {
            preMagnetSubs.unsubscribe();
            preMagnetSubs = null;
        }

    }


    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && isFormed()) {
            updateMagnetFieldSubscription();
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.inputEnergyContainers = null;
        fieldStrength = 0;
        updateMagnetFieldSubscription();
    }

    public void updateFieldStrength() {
        if (!isWorkingEnabled() || inputEnergyContainers == null) {
            return;
        }
        if (inputEnergyContainers.getEnergyStored() > getEnergyCost() && getMagnetStrength() > fieldStrength) {
            inputEnergyContainers.removeEnergy(getEnergyCost());
            fieldStrength += getMagnetRegen();

        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed) {
            textList.add(Component.translatable("cosmiccore.multiblock.current_field_strength", fieldStrength));
            textList.add(Component.translatable("cosmiccore.multiblock.magnetic_field_strength",
                    Component.translatable(FormattingUtil.formatNumbers(this.getMagnetType().getMagnetFieldCapacity())).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))));
            textList.add(Component.translatable("cosmiccore.multiblock.magnetic_regen",
                    Component.translatable(FormattingUtil.formatNumbers(this.getMagnetType().getMagnetRegenRate())).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))));
        }

    }


}
