package com.ghostipedia.cosmiccore.gtbridge.machines.parts;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.floats.FloatPredicate;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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






    private IAirHandlerMachine airHandler;
  //  private final LazyOptional<IAirHandler> airCap = LazyOptional.of(this::getAirHandler);
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription tankSubs;

    @Override
    public float getDangerPressure() {
        return 0;
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
    @Nonnull
    @Override
    public List<Connection> getConnectedAirHandlers(BlockEntity blockEntity) {
        return new ArrayList<>();
    }

    @Override
    public void setConnectedFaces(List<Direction> side) {

    }

    @Override
    public float getPressure() {
        return 0;
    }

    @Override
    public int getAir() {
        return 0;
    }

    @Override
    public void addAir(int i) {

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
        return 0;
    }

    @Override
    public float maxPressure() {
        return 0;
    }

    @Override
    public void printManometerMessage(Player player, List<Component> list) {

    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {

    }
/*


 */


/*
    public Item getDroneItem() {
        // return the item which has the same name as our entity type
        return PneumaticCraftUtils.getRegistryName(ForgeRegistries.ENTITY_TYPES, getType())
                .map(ForgeRegistries.ITEMS::getValue)
                .orElseThrow();
    }


 */



protected final IO io;

/*
    protected BasicAirHandler getAirHandler() {

        if (airHandler == null) {
            int vol = PressureHelper.getUpgradedVolume(5000, volumeUpgrades);
           // ItemStack stack = new ItemStack(getHatchType());
           // EnchantmentHelper.setEnchantments(stackEnchants, stack);
           // vol = ItemRegistry.getInstance().getModifiedVolume(stack, vol);
            airHandler = new BasicAirHandler(vol) {
                @Override
                public void addAir(int amount) {
               //     if (amount > 0 || getUpgrades(ModUpgrades.CREATIVE.get()) == 0)
                         {
                        super.addAir(amount);
                    }
                }
            };
        }
        return airHandler;
    }


 */



/*
public void initializeHullAirHandlers() {
    airHandlerMap.clear();
    for (Direction side : DirectionUtil.VALUES) {
        getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, side, getFrontFacing())
                .ifPresent(handler -> airHandlerMap.computeIfAbsent(handler, k -> new ArrayList<>()).add(side));
    }
    airHandlerMap.forEach(IAirHandlerMachine::setConnectedFaces);
}

 */
    private final LazyOptional<IAirHandlerMachine> airHandlerCap;
    private final Map<IAirHandlerMachine, List<Direction>> airHandlerMap = new IdentityHashMap<>();
    // The `Object... args` parameter is necessary in case a superclass needs to pass any args along to createTank().
    // We can't use fields here because those won't be available while createTank() is called.
    public AirHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, PressureTier pressureTier, int volume, Object... args) {
        super(holder, tier, io);
        this.io = io;
        this.airHandler = PneumaticRegistry.getInstance().getAirHandlerMachineFactory().createAirHandler(pressureTier, volume);
        this.airHandlerCap = LazyOptional.of(() -> airHandler);
        //IAirHandlerMachineFactory factory = getAirHandlerMachineFactory();
        //this.airHandler = getAirHandler();

        //this.tank = createTank(initialCapacity, slots, args);
    }
    public boolean canConnectPneumatic(Direction side) {
        return true;
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY) {
            return airHandlerCap.cast();
        }
        // chain to super so not to forget anything.
        return getCapability(capability, side);
        //return LazyOptional.empty();
    }

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



    @Override
    public void onLoad() {
        super.onLoad();
       // initializeHullAirHandlers();
    }






    @Override
    public void onUnload() {
        super.onUnload();
        if (tankSubs != null) {
            tankSubs.unsubscribe();
            tankSubs = null;
        }
    }

    //////////////////////////////////////
    //********     Auto IO     *********//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        //updateTankSubscription();
       // initializeHullAirHandlers();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
       // initializeHullAirHandlers();

        //updateTankSubscription();
    }
/*
    protected void updateTankSubscription() {
        if (isWorkingEnabled() && ((io == IO.OUT && tank.getAir() > 0) || io == IO.IN)
                && FluidTransferHelper.getFluidTransfer(getLevel(), getPos().relative(getFrontFacing()), getFrontFacing().getOpposite()) != null) {
            //autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }


 */
/*
    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            if (isWorkingEnabled()) {
                if (io == IO.OUT) {
                    tank.exportToNearby(getFrontFacing());
                } else if (io == IO.IN){
                    tank.importFromNearby(getFrontFacing());
                }
            }
            updateTankSubscription();
        }
    }


 */

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
