package com.ghostipedia.cosmiccore.gtbridge.machines.parts;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pressure.PressureHelper;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachineFactory;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.capabilities.BasicAirHandler;
import me.desht.pneumaticcraft.common.capabilities.MachineAirHandler;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.upgrades.IUpgradeHolder;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.upgrades.UpgradeCache;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;


import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AirHatchPartMachine extends TieredPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AirHatchPartMachine.class, TieredPartMachine.MANAGED_FIELD_HOLDER);



    private BasicAirHandler airHandler;
    private final LazyOptional<IAirHandler> airCap = LazyOptional.of(this::getAirHandler);
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription tankSubs;


/*


 */



  //  public Item getHatchType() {
        // return the item which has the same name as our entity type
  //      return PneumaticCraftUtils.getRegistryName(ForgeRegistries.ENTITY_TYPES, getType())
    //            .map(ForgeRegistries.ITEMS::getValue)
    //            .orElseThrow();
   // }

    private final Map<Direction, LazyOptional<IAirHandlerMachine>> neighbourAirHandlers = new EnumMap<>(Direction.class);

    private int volumeUpgrades = 0;


protected final IO io;
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

    // The `Object... args` parameter is necessary in case a superclass needs to pass any args along to createTank().
    // We can't use fields here because those won't be available while createTank() is called.
    public AirHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, Object... args) {
        super(holder, tier);
        this.io = io;
        //IAirHandlerMachineFactory factory = getAirHandlerMachineFactory();
        this.airHandler = getAirHandler();
        for (Direction dir : DirectionUtil.VALUES) {
            this.neighbourAirHandlers.put(dir, LazyOptional.empty());
            }
        //this.tank = createTank(initialCapacity, slots, args);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }


/*
    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTankSubscription));
        }
        //tankSubs = tank.addChangedListener(this::updateTankSubscription);
    }


 */
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
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
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
