package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.componentWrappers.HPCAComponentHatchWrapper;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.HPCAIndicatorPartMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IHPCAComponentHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.util.TimedProgressSupplier;
import com.gregtechceu.gtceu.api.gui.widget.ExtendedProgressWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class HPCAMachine extends WorkableElectricMultiblockMachine
                         implements IOpticalComputationProvider, IControllable, IMachineLife, IDropSaveMachine {

    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HPCAMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final int MIN_COMPONENTS_SLICES = 3;
    public static final int MAX_COMPONENTS_SLICES = 15;
    private static final double IDLE_TEMPERATURE = 200;
    private static final double DAMAGE_TEMPERATURE = 1000;

    private IMaintenanceMachine maintenance;
    private IEnergyContainer energyContainer;
    private IFluidHandler coolantHandler;
    @Persisted
    @DescSynced
    private final HPCAGridHandler hpcaHandler;

    private boolean hasNotEnoughEnergy;

    @Persisted
    private double temperature = IDLE_TEMPERATURE; // start at idle temperature
    private final TimedProgressSupplier progressSupplier;

    @Nullable
    protected TickableSubscription tickSubs;

    @Persisted
    @DescSynced
    @DropSaved
    private long seed = 0L;
    private HPCAModifier[] hpcaModifiers;

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        IMachineLife.super.onMachinePlaced(player, stack);
        if (seed == 0L) this.seed = GTValues.RNG.nextLong();
    }

    public HPCAMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.progressSupplier = new TimedProgressSupplier(200, 47, false);
        this.hpcaHandler = new HPCAGridHandler(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        List<IFluidHandler> coolantContainers = new ArrayList<>();
        List<HPCAComponentHatchWrapper> componentHatches = new ArrayList<>();

        int indicatorCounter = 0;

        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            var pos = part.self().getPos();
            IO io = ioMap.getOrDefault(pos.asLong(), IO.BOTH);
            if (part instanceof IHPCAComponentHatch componentHatch) {
                componentHatches.add(
                        new HPCAComponentHatchWrapper(componentHatch, getColumnModifier(pos), getRowModifier(pos)));
            }
            if (part instanceof IMaintenanceMachine maintenanceMachine)
                maintenance = maintenanceMachine;
            if (part instanceof HPCAIndicatorPartMachine) indicatorCounter++;
            if (io == IO.NONE || io == IO.OUT) continue;
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if (!handlerList.isValid(io)) continue;
                handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .forEach(energyContainers::add);
                handlerList.getCapability(FluidRecipeCapability.CAP).stream()
                        .filter(IFluidHandler.class::isInstance)
                        .map(IFluidHandler.class::cast)
                        .forEach(coolantContainers::add);
            }
        }
        this.energyContainer = new EnergyContainerList(energyContainers);
        this.coolantHandler = new FluidHandlerList(coolantContainers);
        this.hpcaHandler.onStructureFormed(componentHatches, indicatorCounter - 3);

        if (getLevel() instanceof ServerLevel serverLevel)
            serverLevel.getServer().tell(new TickTask(0, this::updateTickSubscription));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTickSubscription));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    protected void updateTickSubscription() {
        if (isFormed) {
            tickSubs = subscribeServerTick(tickSubs, this::tick);
        } else if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.hpcaHandler.onStructureInvalid();
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isActive() && isWorkingEnabled() && !hasNotEnoughEnergy ? hpcaHandler.allocateCWUt(cwut, simulate) : 0;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isActive() && isWorkingEnabled() ? hpcaHandler.getMaxCWUt() : 0;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return !isFormed() || hpcaHandler.hasHPCABridge();
    }

    public void tick() {
        if (isWorkingEnabled()) consumeEnergy();
        if (isActive()) {
            // forcibly use active coolers at full rate if temperature is half-way to damaging temperature
            double midpoint = (DAMAGE_TEMPERATURE - IDLE_TEMPERATURE) / 2;
            double temperatureChange = hpcaHandler.calculateTemperatureChange(coolantHandler, temperature >= midpoint) /
                    2.0;
            if (temperature + temperatureChange <= IDLE_TEMPERATURE) {
                temperature = IDLE_TEMPERATURE;
            } else {
                temperature += temperatureChange;
            }
            if (temperature >= DAMAGE_TEMPERATURE) {
                hpcaHandler.attemptDamageHPCA();
            }
            hpcaHandler.tick();
        } else {
            hpcaHandler.clearComputationCache();
            // passively cool (slowly) if not active
            temperature = Math.max(IDLE_TEMPERATURE, temperature - 0.25);
        }
    }

    private void consumeEnergy() {
        long energyToConsume = hpcaHandler.getCurrentEUt();
        boolean hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance && this.maintenance != null;
        if (hasMaintenance) {
            // 10% more energy per maintenance problem
            energyToConsume += maintenance.getNumMaintenanceProblems() * energyToConsume / 10;
        }

        if (this.hasNotEnoughEnergy && energyContainer.getInputPerSec() > 19L * energyToConsume) {
            this.hasNotEnoughEnergy = false;
        }

        if (this.energyContainer.getEnergyStored() >= energyToConsume) {
            if (!hasNotEnoughEnergy) {
                long consumed = this.energyContainer.removeEnergy(energyToConsume);
                if (consumed == energyToConsume) {
                    getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
                } else {
                    this.hasNotEnoughEnergy = true;
                    getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
                }
            }
        } else {
            this.hasNotEnoughEnergy = true;
            getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
        }
    }

    @Override
    public Widget createUIWidget() {
        var width = Math.max((8 + 15 * hpcaHandler.getArrayLength()), 182);
        WidgetGroup builder = new WidgetGroup(0, 0, width + 8, 117 + 8);
        builder.addWidget(new DraggableScrollableWidgetGroup(4, 4, width, 117).setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText).setMaxWidthLimit(200)
                        .clickHandler(this::handleDisplayClick)));
        builder.setBackground(GuiTextures.BACKGROUND_INVERSE);

        int startX = 4 + (width - 15 * hpcaHandler.getArrayLength()) / 2;
        int startY = 59;

        var texture = new ResourceTexture(
                "cosmiccore:textures/gui/widget/hpca/component_outline_" + hpcaHandler.getArrayLength() + ".png");

        // Create the hover grid
        builder.addWidget(new ExtendedProgressWidget(
                () -> hpcaHandler.getAllocatedCWUt() > 0 ? progressSupplier.getAsDouble() : 0,
                startX, 57, 15 * hpcaHandler.getArrayLength() + 2, 47, texture)
                .setServerTooltipSupplier(hpcaHandler::addInfo)
                .setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT));

        startX += 2;

        // we need to know what components we have on the client
        if (getLevel().isClientSide) {
            if (isFormed) {
                hpcaHandler.tryGatherClientComponents(this.getLevel(), this.getPos(), this.getFrontFacing(),
                        this.getUpwardsFacing(), this.isFlipped);
            } else {
                hpcaHandler.clearClientComponents();
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < hpcaHandler.getArrayLength(); j++) {
                final int index = i * hpcaHandler.getArrayLength() + j;
                Supplier<IGuiTexture> textureSupplier = () -> hpcaHandler.getComponentTexture(index);
                builder.addWidget(new ImageWidget(startX + (15 * j), startY + (15 * i), 13, 13, textureSupplier));
            }
        }

        builder.addWidget(new ComponentPanelWidget(8, 108, this.hpcaHandler::addErrors)
                .textSupplier(this.getLevel().isClientSide ? null : this.hpcaHandler::addErrors).setMaxWidthLimit(200));

        return builder;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(true, hpcaHandler.getAllocatedCWUt() > 0) // transform into two-state system for
                // display
                .setWorkingStatusKeys(
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.data_bank.providing")
                .addCustom(tl -> {
                    if (isFormed()) {
                        // Energy Usage
                        tl.add(Component.translatable(
                                "gtceu.multiblock.hpca.energy",
                                FormattingUtil.formatNumbers(hpcaHandler.cachedEUt),
                                FormattingUtil.formatNumbers(hpcaHandler.getMaxEUt()),
                                GTValues.VNF[GTUtil.getTierByVoltage(hpcaHandler.getMaxEUt())])
                                .withStyle(ChatFormatting.GRAY));

                        // Provided Computation
                        Component cwutInfo = Component.literal(
                                hpcaHandler.cachedCWUt + " / " + hpcaHandler.getMaxCWUt() + " CWU/t")
                                .withStyle(ChatFormatting.AQUA);
                        tl.add(Component.translatable(
                                "gtceu.multiblock.hpca.computation",
                                cwutInfo).withStyle(ChatFormatting.GRAY));
                    }
                })
                .addWorkingStatusLine();
    }

    private ChatFormatting getDisplayTemperatureColor() {
        if (temperature < 500) {
            return ChatFormatting.GREEN;
        } else if (temperature < 750) {
            return ChatFormatting.YELLOW;
        }
        return ChatFormatting.RED;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private int getModifierIndex(BlockPos pos) {
        var index = 0;
        var verticalDelta = Math.abs(pos.getY() - getPos().getY());
        var horizontalDelta = Math.abs(pos.getX() - getPos().getX()) + Math.abs(pos.getZ() - getPos().getZ());
        if (verticalDelta < 4) index = verticalDelta;
        else index = horizontalDelta + 3;
        return index - 1;
    }

    public HPCAModifier[] getModifierState() {
        if (hpcaModifiers != null) return hpcaModifiers;
        var state = new HPCAModifier[MAX_COMPONENTS_SLICES + 3];

        var seededRandom = RandomSource.create(this.seed);
        for (int i = 0; i < state.length; i++) state[i] = HPCAModifier.getRandomModifier(seededRandom);
        this.hpcaModifiers = state;
        return this.hpcaModifiers;
    }

    public HPCAModifier getColumnModifier(BlockPos pos) {
        var state = getModifierState();
        var horizontalDelta = Math.abs(pos.getX() - getPos().getX()) + Math.abs(pos.getZ() - getPos().getZ());
        if (horizontalDelta > MAX_COMPONENTS_SLICES) throw new IllegalStateException();
        return state[horizontalDelta + 3 - 1];
    }

    public HPCAModifier getRowModifier(BlockPos pos) {
        var state = getModifierState();
        var verticalDelta = Math.abs(pos.getY() - getPos().getY());
        if (verticalDelta > 3) throw new IllegalStateException();
        return state[verticalDelta - 1];
    }

    public HPCAModifier getModifier(BlockPos pos) {
        var state = getModifierState();
        return state[getModifierIndex(pos)];
    }
}
