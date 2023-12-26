package com.ghostipedia.cosmiccore.gtbridge.machine.kinetic;

import com.ghostipedia.cosmiccore.gtbridge.AlternatorLogic;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.machine.kinetic.IKineticMachine;
import com.gregtechceu.gtceu.common.machine.trait.NotifiableStressTrait;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import lombok.val;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.GTValues.LuV;

/**
 * @author KilaBash
 * @date 2023/7/9
 * @implNote LargeCombustionEngineMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Alternator extends WorkableElectricMultiblockMachine implements IKineticMachine {


    @Nullable @Getter
    protected EnergyContainerList outputEnergyContainer;
    @Persisted
    protected final NotifiableEnergyContainer energyContainer;
    // runtime
    private boolean isOxygenBoosted = false;
    private List<NotifiableStressTrait> outputStressHatches;

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new AlternatorLogic(this);
    }


    public Alternator(IMachineBlockEntity holder) {
        super(holder);
        this.energyContainer = createEnergyContainer();
    }



    public NotifiableEnergyContainer createEnergyContainer() {
        // create an internal energy container for temp storage. its capacity is decided when the structure formed.
        // it doesn't provide any capability of all sides, but null for the goggles mod to check it storages.
        var container = new NotifiableEnergyContainer(this, 0, 0, 0, 0, 0);
        //container.setCapabilityValidator(Objects::isNull);
        container.setSideInputCondition(dir -> dir.getAxis() != getRotationFacing().getAxis());
        container.setCapabilityValidator(dir -> dir.getAxis() != getRotationFacing().getAxis());
        return container;
    }

    private boolean isIntakesObstructed() {
        var facing = this.getFrontFacing();
        boolean permuteXZ = facing.getAxis() == Direction.Axis.Z;
        var centerPos = this.getPos().relative(facing);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                //Skip the controller block itself
                if (x == 0 && y == 0)
                    continue;
                var blockPos = centerPos.offset(permuteXZ ? x : 0, y, permuteXZ ? 0 : x);
                var blockState = this.getLevel().getBlockState(blockPos);
                if (!blockState.isAir())
                    return true;
            }
        }
        return false;
    }

    //Forming Logic
    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.outputEnergyContainer = null;
        energyContainer.resetBasicInfo(0, 0, 0, 0, 0);
        energyContainer.setEnergyStored(0);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // capture all energy containers
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE || io == IO.IN) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                }
            }
        }
        this.outputEnergyContainer = new EnergyContainerList(energyContainers);
    }

    public static long calculateEnergyStorageFactor(int tier, int energyInputAmount) {
        return energyInputAmount * (long) Math.pow(2, tier - LuV) * 10000000L;
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        if (!isRemote()) {
            if (oldFacing.getAxis() != newFacing.getAxis()) {
                var holder = getKineticHolder();
                if (holder.hasNetwork()) {
                    holder.getOrCreateNetwork().remove(holder);
                }
                holder.detachKinetics();
                holder.removeSource();
            }
        }
    }

    private boolean isExtreme() {
        return getTier() > GTValues.EV;
    }

    public boolean isBoostAllowed() {
        return getMaxVoltage() >= GTValues.V[getTier() + 1];
    }

    //////////////////////////////////////
    //******     Recipe Logic    *******//
    //////////////////////////////////////




    //@Nullable
    //public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
    //    if (machine instanceof Alternator alternator) {
     //       var EUt = RecipeHelper.getOutputEUt(recipe);
            // has lubricant
       //         return recipe;
         //   }
       // return null;
   // }

    @Override
    public void onWorking() {
        super.onWorking();
        // check lubricant
        val totalContinuousRunningTime = recipeLogic.getTotalContinuousRunningTime();

            // insufficient lubricant
        }

    @Override
    public boolean dampingWhenWaiting() {
        return false;
    }

    //////////////////////////////////////
    //*******        GUI        ********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            if (isBoostAllowed()) {
                if (!isExtreme()) {
                    if (isOxygenBoosted) {
                        textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.oxygen_boosted"));
                    } else {
                        textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.supply_oxygen_to_boost"));
                    }
                }
                else {
                    if (isOxygenBoosted) {
                        textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.liquid_oxygen_boosted"));
                    } else {
                        textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.supply_liquid_oxygen_to_boost"));
                    }
                }
            } else {
                textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.boost_disallowed"));
            }
        }
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        super.attachTooltips(tooltipsPanel);
        tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(
                () -> GuiTextures.INDICATOR_NO_STEAM.get(false),
                () -> List.of(Component.translatable("gtceu.multiblock.large_combustion_engine.obstructed").setStyle(Style.EMPTY.withColor(ChatFormatting.RED))),
                this::isIntakesObstructed,
                () -> null));
    }
}