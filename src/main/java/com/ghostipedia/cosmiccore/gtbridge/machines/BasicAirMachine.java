package com.ghostipedia.cosmiccore.gtbridge.machines;

import com.ghostipedia.cosmiccore.gtbridge.machines.logic.BasicAirMachineLogic;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicAirMachine extends WorkableElectricMultiblockMachine implements IDisplayUIMachine {




    @Override
    public void onLoad() {
        super.onLoad();
    }


    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new BasicAirMachineLogic(this);
    }


    public BasicAirMachine(IMachineBlockEntity holder) {
        super(holder);
    }


    //Forming Logic
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        scheduleRenderUpdate();
    }
/*
        //getRecipeLogic().resetRecipeLogic();
        // capture all energy containers
        List<IFluidTransfer> fluidTanks = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE || io == IO.IN) continue;
            for (var handler : part.getRecipeHandlers()) {
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                var handlerIO = io == IO.OUT ? handler.getHandlerIO() : io;
                if (handlerIO == IO.IN && handler.getCapability() == FluidRecipeCapability.CAP && handler instanceof IFluidTransfer fluidTransfer) {
                    fluidTanks.add(fluidTransfer);
                }
            }
        }
        this.inputFluidInventory = new FluidTransferList(fluidTanks);
    }


 */
    //////////////////////////////////////
    //******     Recipe Logic    *******//
    //////////////////////////////////////

    @Override
    public boolean alwaysTryModifyRecipe() {
        return false;
    }

    @Override
    public boolean dampingWhenWaiting() {
        return true;
    }


    /**
     * Adds display text to the provided list of components.
     *
     * @param textList The list of components to add the display text to.
     */
    //GUI
    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            var maxVoltage = getMaxVoltage();
            if (maxVoltage > 0) {
                String voltageName = GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage)];
                textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage, voltageName));

            }
        }
    }


    /**
     * Retrieves the sub-tabs associated with the current instance of BasicSteamTurbineMachine.
     *
     * @return A list of IFancyUIProvider objects representing the sub-tabs.
     */
    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return Collections.emptyList();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121).setBackground(getScreenTexture());
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(150)
                .clickHandler(this::handleDisplayClick));
        return new ModularUI(176, 135, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(screen);
        //.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 134, true));
    }
}
