package com.ghostipedia.cosmiccore.gtbridge.machines.traits;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class NotifiableAirContainer extends NotifiableRecipeHandlerTrait<Double> implements ICapabilityProvider {
   // public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableAirContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);


    protected final IAirHandlerMachine airHandler;

    private final LazyOptional<IAirHandlerMachine> airHandlerCap;

    public final IO handlerIO;
    public final float maxPressure;

    protected boolean isWorking;

    public int volume;

    private static final float MAX_PRESSURE = 3;



    @Getter
    @Persisted @DescSynced
    private double particles;
    @Getter
    @Setter
    private long timeStamp;

    public void updateAirHandler() {
        ArrayList<Direction> sides = new ArrayList<>();
        for (Direction side: new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            if (canConnectPneumatic(side)) {
                sides.add(side);
            }
        }
        sides.add(Direction.UP);
        airHandler.setConnectedFaces(sides);
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if (cap == PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY && canConnectPneumatic(side)) {
            return airHandlerCap.cast();
        }
        return getCapability(cap, side);
    }



    public boolean canConnectPneumatic(Direction side) {
        return true;
    }



    public NotifiableAirContainer(MetaMachine machine, PressureTier pressureTier, int volume, float maxPressure, IO io) {
        super(machine);
        this.handlerIO = io;
        this.volume = volume;
        this.maxPressure = maxPressure;
        this.airHandler = PneumaticRegistry.getInstance().getAirHandlerMachineFactory().createAirHandler(pressureTier, volume);
        this.airHandlerCap = LazyOptional.of(() -> airHandler);
        this.isWorking = true;
        updateAirHandler();
    }
    
    @Override
    public List<Double> handleRecipeInner(IO io, GTRecipe recipe, List<Double> left, @Nullable String slotName, boolean simulate) {
        NotifiableAirContainer container = this;
        double pressure = left.stream().reduce(0.0d, Double::sum);

        //final double containerPressure = container.getPressure();
        double pressureToChange;



        //final double newPressure = containerPressure;
        // pressure must be within +/- 1 exponent of the target
       // if (newPressure < pressure / 10 || newPressure > pressure * 10) {
            return left;
       // }

       // return left;
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

}
