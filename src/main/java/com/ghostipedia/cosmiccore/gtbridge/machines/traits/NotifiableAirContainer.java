package com.ghostipedia.cosmiccore.gtbridge.machines.traits;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.floats.FloatPredicate;
import lombok.Getter;
import lombok.Setter;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class NotifiableAirContainer extends NotifiableRecipeHandlerTrait<Double> implements IAirHandlerMachine, ICapabilityTrait {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableAirContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);


    protected final IAirHandlerMachine airHandler;
    private final LazyOptional<IAirHandlerMachine> airHandlerCap;
    private final Map<IAirHandlerMachine, List<Direction>> airHandlerMap = new IdentityHashMap<>();
    public final IO handlerIO;
    public final IO capabilityIO;



    @Getter
    @Persisted @DescSynced
    private double particles;
    @Getter
    @Setter
    private long timeStamp;

    @Override
    public float getPressure() {
        return (float) this.airHandler.getPressure();
    }

    @Override
    public int getAir() {
        return this.airHandler.getAir();
    }

    @Override
    public void addAir(int i) {
        this.airHandler.addAir(i);

    }

    @Override
    public int getBaseVolume() {
        return 0;
    }

    @Override
    public void setBaseVolume(int i) {

    }

    @Override
    public int getVolume() {
        return 5000;
    }

    @Override
    public float maxPressure() {
        return this.airHandler.getDangerPressure();
    }


    @Override
    public float getDangerPressure() {
        return 20f;
    }

    @Override
    public float getCriticalPressure() {
        return 0;
    }

    @Override
    public void setPressure(float v) {

    }

    @Override
    public void setVolumeUpgrades(int i) {

    }

    @Override
    public void enableSafetyVenting(FloatPredicate floatPredicate, Direction direction) {

    }

    @Override
    public void disableSafetyVenting() {

    }

    @Override
    public void tick(BlockEntity blockEntity) {

    }

    @Override
    public void setSideLeaking(@Nullable Direction direction) {

    }

    @Nullable
    @Override
    public Direction getSideLeaking() {
        return null;
    }

    @Override
    public List<Connection> getConnectedAirHandlers(BlockEntity blockEntity) {
        List<IAirHandlerMachine.Connection> neighbours = new ArrayList<>();
        return neighbours;
    }

    @Override
    public void setConnectedFaces(List<Direction> list) {

    }

    @Override
    public void printManometerMessage(Player player, List<Component> list) {

    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Persisted
    public final FluidStorage[] storages;

    @Override
    public void deserializeNBT(CompoundTag arg) {

    }
    public boolean isEmpty() {
        if (isEmpty == null) {
            isEmpty = true;
            for (FluidStorage storage : storages) {
                if (!storage.getFluid().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    @Setter
    protected boolean allowSameFluids;
    private Boolean isEmpty;
    public void onContentsChanged() {
        isEmpty = null;
        notifyListeners();
    }


    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY) {
            return airHandlerCap.cast();
        }
        return LazyOptional.empty();
    }

    public NotifiableAirContainer(MetaMachine machine, List<FluidStorage> storages, PressureTier pressureTier, int volume, IO io, IO capabilityIO) {
        super(machine);
        this.handlerIO = io;
        this.capabilityIO = capabilityIO;
        this.storages = storages.toArray(FluidStorage[]::new);
        for (FluidStorage storage : this.storages) {
            storage.setOnContentsChanged(this::onContentsChanged);
        }
        if (io == IO.IN) {
            this.allowSameFluids = true;
        }
        //this.storages
        this.airHandler = PneumaticRegistry.getInstance().getAirHandlerMachineFactory().createTierOneAirHandler(volume);
        this.airHandlerCap = LazyOptional.of(() -> airHandler);
    }
/*
    public static NotifiableAirContainer airContainer(MetaMachine machine, PressureTier pressureTier, int volume, IO io, IO CapabilityIO) {
        return new NotifiableAirContainer(machine, PressureTier.TIER_ONE, volume, io, CapabilityIO);
    }


 */
    @Override
    public List<Double> handleRecipeInner(IO io, GTRecipe recipe, List<Double> left, @Nullable String slotName, boolean simulate) {
        IAirHandlerMachine container = this;
        double pressure = left.stream().reduce(0.0d, Double::sum);

        final double containerPressure = container.getPressure();
        double pressureToChange;



        final double newPressure = containerPressure;
        // pressure must be within +/- 1 exponent of the target
        if (newPressure < pressure / 10 || newPressure > pressure * 10) {
            return left;
        }

        return left;
    }


    @Override
    public RecipeCapability<Double> getCapability() {
        return AirRecipeCapability.CAP;
    }



    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }


    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    @Override
    public IO getCapabilityIO() {
        return capabilityIO;
    }
}
