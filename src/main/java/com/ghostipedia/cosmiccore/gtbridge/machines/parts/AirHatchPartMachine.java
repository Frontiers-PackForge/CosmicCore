package com.ghostipedia.cosmiccore.gtbridge.machines.parts;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.floats.FloatPredicate;
import lombok.Getter;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachineFactory;
import me.desht.pneumaticcraft.common.capabilities.MachineAirHandler;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;


import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static com.gregtechceu.gtceu.api.blockentity.forge.MetaMachineBlockEntityImpl.getCapability;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AirHatchPartMachine extends TieredIOPartMachine implements IAirHandlerMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AirHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);




private float pressure;
private int volume;
private int air;

    protected final IAirHandlerMachine airHandler;
    protected final IO io;

    public void initializeHullAirHandlers() {
        airHandlerMap.clear();
        for (Direction side : DirectionUtil.VALUES) {
            getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, side)
                    .ifPresent(handler -> airHandlerMap.computeIfAbsent(handler, k -> new ArrayList<>()).add(side));
        }
        airHandlerMap.forEach(IAirHandlerMachine::setConnectedFaces);
    }

    public void initializeHullAirHandlerClient(Direction dir, IAirHandlerMachine handler) {
        airHandlerMap.clear();
        List<Direction> l = Collections.singletonList(dir);
        airHandlerMap.put(handler, l);
        handler.setConnectedFaces(l);
    }

    private final LazyOptional<IAirHandlerMachine> airHandlerCap;
    private final Map<IAirHandlerMachine, List<Direction>> airHandlerMap = new IdentityHashMap<>();
    // The `Object... args` parameter is necessary in case a superclass needs to pass any args along to createTank().
    // We can't use fields here because those won't be available while createTank() is called.
    public AirHatchPartMachine(IMachineBlockEntity holder, BlockPos pos, BlockState state, int tier, IO io, PressureTier pressureTier, int volume, Object... args) {
        super(holder, tier, io);
        this.io = io;
        this.airHandler = PneumaticRegistry.getInstance().getAirHandlerMachineFactory().createAirHandler(pressureTier, volume);
        this.airHandlerCap = LazyOptional.of(() -> airHandler);
    }
    public boolean canConnectPneumatic(Direction side) {
        return true;
    }


    public void handleUpdateTag(CompoundTag tag) {
        handleUpdateTag(tag);

        initializeHullAirHandlers();
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY ) {
            return level != null && (side == null || canConnectPneumatic(side)) ? airHandlerCap.cast() : LazyOptional.empty();
        } else {
            return getCapability(cap, side);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        initializeHullAirHandlers();
    }

    @Override
    public void onChanged() {
        super.onChanged();
        initializeHullAirHandlers();
        airHandlerMap.keySet().forEach(h -> h.setSideLeaking(null));

    }
/*
    public void saveAdditional(CompoundTag tag) {
       saveAdditional(tag);
        tag.put(NBTKeys.NBT_AIR_HANDLER, airHandler.serializeNBT());
    }


 */
    public BlockPos getBlockPos() {
        // Replacing `getPos()` with the method you use to get BlockPos of your block.
        return getPos();
    }
    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }












    //////////////////////////////////////
    //********     Auto IO     *********//
    //////////////////////////////////////




    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////


    protected Widget createSingleSlotGUI() {
        var group = new WidgetGroup(0, 0, 89, 63);

        group.addWidget(new ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY))
                .addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"));
                //.addWidget(new LabelWidget(8, 18, () -> String.valueOf(tank.getFluidInTank(0).getAmount())).setTextColor(-1).setDropShadow(true))
                //.addWidget(new TankWidget(tank.storages[0], 67, 22, true, io.support(IO.IN)).setBackground(GuiTextures.FLUID_SLOT));

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

}
