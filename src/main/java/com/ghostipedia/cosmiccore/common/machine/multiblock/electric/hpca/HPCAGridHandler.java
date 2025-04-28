package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.componentWrappers.HPCAComponentHatchWrapper;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IHPCAComponentHatch;
import com.gregtechceu.gtceu.api.capability.IHPCAComputationProvider;
import com.gregtechceu.gtceu.api.capability.IHPCACoolantProvider;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class HPCAGridHandler implements IManaged {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(HPCAGridHandler.class);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Nullable
    private final HPCAMachine controller;

    // structure info
    private final List<IHPCAComponentHatch> components = new ObjectArrayList<>();
    private final Set<IHPCACoolantProvider> coolantProviders = new ObjectOpenHashSet<>();
    private final Set<IHPCAComputationProvider> computationProviders = new ObjectOpenHashSet<>();
    private int numBridges;

    // transaction info
    /** How much CWU/t is currently allocated for this tick. */
    @Getter
    private int allocatedCWUt;

    @Getter
    @DescSynced
    private int arrayLength;

    // cached gui info
    // holding these values past the computation clear because GUI is too "late" to read the state in time
    @DescSynced
    protected long cachedEUt;
    @DescSynced
    protected int cachedCWUt;

    public HPCAGridHandler(@Nullable HPCAMachine controller) {
        this.controller = controller;
    }

    public void onStructureFormed(Collection<HPCAComponentHatchWrapper> components, int arrayLength) {
        reset();
        this.arrayLength = arrayLength;
        for (HPCAComponentHatchWrapper component : components) {
            this.components.add(component);
            var coolantProvider = component.getHPCACoolantProvider();
            if (coolantProvider != null) this.coolantProviders.add(coolantProvider);
            var computationProvider = component.getHPCAComputationProvider();
            if (computationProvider != null) this.computationProviders.add(computationProvider);
            if (component.isBridge()) this.numBridges++;
        }
    }

    public void onStructureInvalid() {
        reset();
    }

    private void reset() {
        clearComputationCache();
        components.clear();
        coolantProviders.clear();
        computationProviders.clear();
        numBridges = 0;
        arrayLength = 0;
    }

    void clearComputationCache() {
        allocatedCWUt = 0;
    }

    public void tick() {
        if (cachedCWUt != allocatedCWUt)
            cachedCWUt = allocatedCWUt;
        cachedEUt = getCurrentEUt();
        if (allocatedCWUt != 0)
            allocatedCWUt = 0;
    }

    /**
     * Calculate the temperature differential this tick given active computation and consume coolant.
     *
     * @param coolantTank         The tank to drain coolant from.
     * @param forceCoolWithActive Whether active coolers should forcibly cool even if temperature is already
     *                            decreasing due to passive coolers. Used when the HPCA is running very hot.
     * @return The temperature change, can be positive or negative.
     */
    public double calculateTemperatureChange(IFluidHandler coolantTank, boolean forceCoolWithActive) {
        // calculate temperature increase
        int maxCWUt = Math.max(1, getMaxCWUt()); // avoids dividing by 0 and the behavior is no different
        int maxCoolingDemand = getMaxCoolingDemand();

        // temperature increase is proportional to the amount of actively used computation
        // a * (b / c)
        int temperatureIncrease = (int) Math.round(1.0 * maxCoolingDemand * allocatedCWUt / maxCWUt);

        // calculate temperature decrease
        long maxPassiveCooling = 0;
        long maxActiveCooling = 0;
        int maxCoolantDrain = 0;

        for (var coolantProvider : coolantProviders) {
            if (coolantProvider.isActiveCooler()) {
                maxActiveCooling += coolantProvider.getCoolingAmount();
                maxCoolantDrain += coolantProvider.getMaxCoolantPerTick();
            } else {
                maxPassiveCooling += coolantProvider.getCoolingAmount();
            }
        }

        double temperatureChange = temperatureIncrease - maxPassiveCooling;
        // quick exit if no active cooling/coolant drain is present
        if (maxActiveCooling == 0 && maxCoolantDrain == 0) {
            return temperatureChange;
        }
        if (forceCoolWithActive || maxActiveCooling <= temperatureChange) {
            // try to fully utilize active coolers
            FluidStack coolantStack = GTTransferUtils.drainFluidAccountNotifiableList(coolantTank,
                    getCoolantStack(maxCoolantDrain), IFluidHandler.FluidAction.EXECUTE);
            if (!coolantStack.isEmpty()) {
                long coolantDrained = coolantStack.getAmount();
                if (coolantDrained == maxCoolantDrain) {
                    // coolant requirement was fully met
                    temperatureChange -= maxActiveCooling;
                } else {
                    // coolant requirement was only partially met, cool proportional to fluid amount drained
                    // a * (b / c)
                    temperatureChange -= maxActiveCooling * (1.0 * coolantDrained / maxCoolantDrain);
                }
            }
        } else if (temperatureChange > 0) {
            // try to partially utilize active coolers to stabilize to zero
            double temperatureToDecrease = Math.min(temperatureChange, maxActiveCooling);
            int coolantToDrain = Math.max(1, (int) (maxCoolantDrain * (temperatureToDecrease / maxActiveCooling)));
            FluidStack coolantStack = GTTransferUtils.drainFluidAccountNotifiableList(coolantTank,
                    getCoolantStack(coolantToDrain), IFluidHandler.FluidAction.EXECUTE);
            if (!coolantStack.isEmpty()) {
                int coolantDrained = coolantStack.getAmount();
                if (coolantDrained == coolantToDrain) {
                    // successfully stabilized to zero
                    return 0;
                } else {
                    // coolant requirement was only partially met, cool proportional to fluid amount drained
                    // a * (b / c)
                    temperatureChange -= temperatureToDecrease * (1.0 * coolantDrained / coolantToDrain);
                }
            }
        }
        return temperatureChange;
    }

    /**
     * Get the coolant stack for this HPCA. Eventually this could be made more diverse with different
     * coolants from different Active Cooler components, but currently it is just a fixed Fluid.
     */
    public FluidStack getCoolantStack(int amount) {
        return new FluidStack(getCoolant(), amount);
    }

    private Fluid getCoolant() {
        return GTMaterials.PCBCoolant.getFluid();
    }

    /**
     * Roll a 1/200 chance to damage a HPCA component marked as damageable. Randomly selects the component.
     * If called every tick, this succeeds on average once every 10 seconds.
     */
    public void attemptDamageHPCA() {
        // 1% chance each tick to damage a component if running too hot
        if (GTValues.RNG.nextInt(200) == 0) {
            // randomize which component is actually damaged
            List<IHPCAComponentHatch> candidates = new ArrayList<>();
            for (var component : components) {
                if (component.canBeDamaged()) {
                    candidates.add(component);
                }
            }
            if (!candidates.isEmpty()) {
                candidates.get(GTValues.RNG.nextInt(candidates.size())).setDamaged(true);
            }
        }
    }

    /** Allocate computation on a given request. Allocates for one tick. */
    public int allocateCWUt(int cwut, boolean simulate) {
        int maxCWUt = getMaxCWUt();
        int availableCWUt = maxCWUt - this.allocatedCWUt;
        int toAllocate = Math.min(cwut, availableCWUt);
        if (!simulate) {
            this.allocatedCWUt += toAllocate;
        }
        return toAllocate;
    }

    /** The maximum amount of CWUs (Compute Work Units) created per tick. */
    public int getMaxCWUt() {
        int maxCWUt = 0;
        for (var computationProvider : computationProviders) {
            maxCWUt += computationProvider.getCWUPerTick();
        }
        return maxCWUt;
    }

    /** The current EU/t this HPCA should use, considering passive drain, current computation, etc.. */
    public long getCurrentEUt() {
        long maximumCWUt = Math.max(1, getMaxCWUt()); // behavior is no different setting this to 1 if it is 0
        long maximumEUt = getMaxEUt();
        long upkeepEUt = getUpkeepEUt();

        if (maximumEUt == upkeepEUt) {
            return maximumEUt;
        }

        // energy draw is proportional to the amount of actively used computation
        // a + c(b - a) / d
        return upkeepEUt + ((maximumEUt - upkeepEUt) * allocatedCWUt / maximumCWUt);
    }

    /** The amount of EU/t this HPCA uses just to stay on with 0 output computation. */
    public long getUpkeepEUt() {
        long upkeepEUt = 0;
        for (var component : components) {
            upkeepEUt += component.getUpkeepEUt();
        }
        return upkeepEUt;
    }

    /** The maximum EU/t that this HPCA could ever use with the given configuration. */
    public long getMaxEUt() {
        long maximumEUt = 0;
        for (var component : components) {
            maximumEUt += component.getMaxEUt();
        }
        return maximumEUt;
    }

    /** Whether this HPCA has a Bridge to allow connecting to other HPCA's */
    public boolean hasHPCABridge() {
        return numBridges > 0;
    }

    /** Whether this HPCA has any cooling providers which are actively cooled. */
    public boolean hasActiveCoolers() {
        for (var coolantProvider : coolantProviders) {
            if (coolantProvider.isActiveCooler()) return true;
        }
        return false;
    }

    /** How much cooling this HPCA can provide. NOT related to coolant fluid consumption. */
    public int getMaxCoolingAmount() {
        int maxCooling = 0;
        for (var coolantProvider : coolantProviders) {
            maxCooling += coolantProvider.getCoolingAmount();
        }
        return maxCooling;
    }

    /** How much cooling this HPCA can require. NOT related to coolant fluid consumption. */
    public int getMaxCoolingDemand() {
        int maxCooling = 0;
        for (var computationProvider : computationProviders) {
            maxCooling += computationProvider.getCoolingPerTick();
        }
        return maxCooling;
    }

    /** How much coolant this HPCA can consume in a tick, in mB/t. */
    public int getMaxCoolantDemand() {
        int maxCoolant = 0;
        for (var coolantProvider : coolantProviders) {
            maxCoolant += coolantProvider.getMaxCoolantPerTick();
        }
        return maxCoolant;
    }

    public void addInfo(List<Component> textList) {
        // Max Computation
        MutableComponent data = Component.literal(Integer.toString(getMaxCWUt())).withStyle(ChatFormatting.AQUA);
        textList.add(Component.translatable("gtceu.multiblock.hpca.info_max_computation", data)
                .withStyle(ChatFormatting.GRAY));

        // Cooling
        ChatFormatting coolingColor = getMaxCoolingAmount() < getMaxCoolingDemand() ? ChatFormatting.RED :
                ChatFormatting.GREEN;
        data = Component.literal(Integer.toString(getMaxCoolingDemand())).withStyle(coolingColor);
        textList.add(Component.translatable("gtceu.multiblock.hpca.info_max_cooling_demand", data)
                .withStyle(ChatFormatting.GRAY));

        data = Component.literal(Integer.toString(getMaxCoolingAmount())).withStyle(coolingColor);
        textList.add(Component.translatable("gtceu.multiblock.hpca.info_max_cooling_available", data)
                .withStyle(ChatFormatting.GRAY));

        // Coolant Required
        if (getMaxCoolantDemand() > 0) {
            data = Component.translatable("gtceu.universal.liters", getMaxCoolantDemand())
                    .withStyle(ChatFormatting.YELLOW).append(" ");
            Component coolantName = Component.translatable("gtceu.multiblock.hpca.info_coolant_name")
                    .withStyle(ChatFormatting.YELLOW);
            data.append(coolantName);
        } else {
            data = Component.literal("0").withStyle(ChatFormatting.GREEN);
        }
        textList.add(Component.translatable("gtceu.multiblock.hpca.info_max_coolant_required", data)
                .withStyle(ChatFormatting.GRAY));

        // Bridging
        if (numBridges > 0) {
            textList.add(Component.translatable("gtceu.multiblock.hpca.info_bridging_enabled")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            textList.add(Component.translatable("gtceu.multiblock.hpca.info_bridging_disabled")
                    .withStyle(ChatFormatting.RED));
        }
    }

    public void addWarnings(List<Component> textList) {
        List<Component> warnings = new ArrayList<>();
        if (numBridges > 1) {
            warnings.add(Component.translatable("gtceu.multiblock.hpca.warning_multiple_bridges")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (computationProviders.isEmpty()) {
            warnings.add(Component.translatable("gtceu.multiblock.hpca.warning_no_computation")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (getMaxCoolingDemand() > getMaxCoolingAmount()) {
            warnings.add(Component.translatable("gtceu.multiblock.hpca.warning_low_cooling")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (!warnings.isEmpty()) {
            textList.add(Component.translatable("gtceu.multiblock.hpca.warning_structure_header")
                    .withStyle(ChatFormatting.YELLOW));
            textList.addAll(warnings);
        }
    }

    public void addErrors(List<Component> textList) {
        if (components.stream().anyMatch(IHPCAComponentHatch::isDamaged)) {
            textList.add(
                    Component.translatable("gtceu.multiblock.hpca.error_damaged").withStyle(ChatFormatting.RED));
        }
    }

    public ResourceTexture getComponentTexture(int index) {
        if (components.size() <= index) {
            return GuiTextures.BLANK_TRANSPARENT;
        }
        return components.get(index).getComponentIcon();
    }

    public void tryGatherClientComponents(Level world, BlockPos pos, Direction frontFacing,
                                          Direction upwardsFacing, boolean flip) {
        Direction relativeUp = RelativeDirection.UP.getRelativeFacing(frontFacing, upwardsFacing, flip);

        if (components.isEmpty()) {
            BlockPos testPos = pos
                    .relative(frontFacing.getOpposite(), arrayLength)
                    .relative(relativeUp, 3);

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < arrayLength; j++) {
                    BlockPos tempPos = testPos.relative(frontFacing, j).relative(relativeUp.getOpposite(), i);
                    BlockEntity be = world.getBlockEntity(tempPos);
                    if (be instanceof IHPCAComponentHatch hatch) {
                        components.add(hatch);
                    } else if (be instanceof IMachineBlockEntity machineBE) {
                        MetaMachine machine = machineBE.getMetaMachine();
                        if (machine instanceof IHPCAComponentHatch hatch) {
                            components.add(hatch);
                        }
                    }
                    // if here without a hatch, something went wrong, better to skip than add a null into the mix.
                }
            }
        }
    }

    public void clearClientComponents() {
        components.clear();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        controller.onChanged();
    }
}
