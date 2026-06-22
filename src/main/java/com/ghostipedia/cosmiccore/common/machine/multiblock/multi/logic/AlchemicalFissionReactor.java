package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AlchemicalFissionReactor extends WorkableElectricMultiblockMachine {

    // Base Heat capacity (maybe we make it so there's ways to raise this so there's more 'buffer' in each zone. tbd
    // that sounds like block predicates lol)
    @Getter
    public static long HEAT_CAPACITY = 10_000L;
    @Getter
    public static long COOLANT_DELTA = 0;
    @Getter
    public static long HEATING_DELTA = 10;

    // Heat Band Max Ranges of Reactor Heat % (eg. Low is 0-33%)
    public static final int LOW = 33;
    public static final int MODERATE = 66;
    public static final int HIGH = 100;

    @Getter
    @DescSynced
    @Persisted
    public static long heat = 0;

    @Nullable
    protected FluidHandlerList inputFluidHandlers;

    @Getter
    @DescSynced
    @Persisted
    public static long heatCapacity = HEAT_CAPACITY;

    @Nullable
    protected TickableSubscription fissionLogicSubs;

    @DescSynced
    private static final Object2IntMap<FluidStack> coolantTiers = new Object2IntOpenHashMap<>();

    static {
        coolantTiers.put(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 1), 16);
        coolantTiers.put(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, 1), 64);
    }

    public AlchemicalFissionReactor(BlockEntityCreationInfo holder) {
        super(holder);
    }

    public static long getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // I don't even know if this works I copied my old code from like 2024 :)

        List<IFluidHandler> inputFluidContainers = new ArrayList<>();
        Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap",
                Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getBlockPos().asLong(), IO.BOTH);
            if (io == IO.NONE || io == IO.OUT) continue;
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if ((handlerList.getHandlerIO() != IO.IN)) continue;
                handlerList.getCapability(FluidRecipeCapability.CAP).stream()
                        .filter(IFluidHandler.class::isInstance)
                        .map(IFluidHandler.class::cast)
                        .forEach(inputFluidContainers::add);
            }
        }

        this.inputFluidHandlers = new FluidHandlerList(inputFluidContainers);
        updateFissionSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        updateFissionSubscription();
        heat = 0;
    }

    protected void updateFissionSubscription() {
        if (this.isFormed) {
            fissionLogicSubs = subscribeServerTick(fissionLogicSubs, this::updateHeat);
        } else if (fissionLogicSubs != null) {
            fissionLogicSubs.unsubscribe();
            fissionLogicSubs = null;
        }
    }

    // Main Heating Logic
    public void updateHeat() {
        if (recipeLogic.isWorking()) {
            if (heat >= HEAT_CAPACITY) {
                heat = HEAT_CAPACITY - HEATING_DELTA;
            }
            heat += HEATING_DELTA;
        } else {
            heat -= HEATING_DELTA;
            if (heat <= 0) {
                heat = 0;
            }
        }
    }

    /*
     * Get all coolant tanks
     * get all valid coolants
     * drain all valid coolants
     * convert what was drained into cooling
     * apply / return coolant delta for that tick
     */
    public void setCoolantDelta() {
        if (inputFluidHandlers != null) {
            for (var handler : inputFluidHandlers.handlers) {
                FluidStack coolant = handler.getFluidInTank(0);
            }
            var test = inputFluidHandlers.getFluidInTank(0);
        }
    }

    @Override
    public boolean onWorking() {
        return super.onWorking();
    }

    // Todo, coolant drain stuff.
    private void processCoolant() {}

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed) {
            textList.add(Component.translatable("cosmiccore.multiblock.heat_value",
                    FormattingUtil.formatNumber2Places(heat)));
            textList.add(Component.translatable("cosmiccore.multiblock.heat_capacity",
                    FormattingUtil.formatNumber2Places(getHeatCapacity())));
        }
    }
}
