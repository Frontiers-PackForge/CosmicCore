package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.pattern.ModularPowerStationPatterns;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SliderWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModularPowerStationMachine extends WorkableElectricMultiblockMachine {

    public static final int AMPERAGE_PER_STAGE = 4;
    public static final int MAXIMUM_STAGES = 4;

    @SyncToClient
    private int driveType;
    @SyncToClient
    private int stageCount;
    @SyncToClient
    private int statorTier = -1;
    @SyncToClient
    private int assemblyStatus = AssemblyStatus.NO_STAGES.ordinal();
    @SyncToClient
    private long ratedAmperage;
    @SaveField
    private int throttleReduction;

    public ModularPowerStationMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        resolveAssembly();
        selectCompatibleRecipeType();
        recipeLogic.updateTickSubscription();
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        clearAssembly();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        clearAssembly();
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        return isAssemblyReady() && super.isRecipeLogicAvailable();
    }

    @Override
    protected @Nullable GTRecipe getRealRecipe(GTRecipe recipe) {
        if (!isAssemblyReady() ||
                !getDriveType().acceptsRecipeType(getActiveRecipeType()) ||
                recipe.recipeType != getRecipeTypes()[getActiveRecipeType()])
            return null;
        return super.getRealRecipe(recipe);
    }

    @Override
    public void cycleActiveRecipeType() {
        selectCompatibleRecipeType();
        recipeLogic.updateTickSubscription();
    }

    @Override
    public long getOverclockVoltage() {
        return getStatorVoltage();
    }

    @Override
    public long getDisplayGeneratorPower() {
        GTRecipe activeRecipe = recipeLogic.getLastRecipe();
        return activeRecipe == null ? getOperatingOutputLimit() : activeRecipe.getOutputEUt().getTotalEU();
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return capability == FluidRecipeCapability.CAP ||
                (capability != EURecipeCapability.CAP && super.canVoidRecipeOutputs(capability));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.modular_power_station.drive",
                Component.translatable(getDriveType().translationKey())).withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(getAssemblyStatus().translationKey())
                .withStyle(isAssemblyReady() ? ChatFormatting.GREEN : ChatFormatting.RED)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.modular_power_station.stages", stageCount)
                .withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(Text.dynamic(this::statorLine).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.modular_power_station.throttle",
                getOutputPercent(), FormattingUtil.formatNumbers(getOperatingOutputLimit()),
                String.format(Locale.ROOT, "%,.1f", getOperatingAmperage()))
                .withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(Text.dynamic(this::fuelConsumptionLine).asWidget());
        widgets.add(GTMultiblockTextUtil.addProgressLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager));
        widgets.addAll(getDefinition().getAdditionalDisplay().apply(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addBatchModeLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addOutputLines(this, syncManager));
        widgets.addAll(GTMultiblockTextUtil.addRecipeFailReasonLines(this, syncManager));
        IntSyncValue outputLimit = new IntSyncValue(this::getOutputPercent, this::setOutputPercent).allowC2S();
        syncManager.syncValue("modularPowerStationOutputLimit", outputLimit);
        widgets.add(new SliderWidget()
                .background(GTGuiTextures.FLUID_SLOT)
                .bounds(5, 100)
                .stopper(1)
                .stopperTexture(IDrawable.EMPTY)
                .sliderSize(8, 14)
                .height(16)
                .width(204)
                .value(outputLimit));
        return widgets;
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.panelBuilder(this)
                .addDefaultConfigurators(false)
                .rightConfigurators(flow -> {
                    flow.child(GTMuiWidgets.createPowerButton(this));
                    flow.child(GTMuiWidgets.createVoidingButton(this));
                });
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(getWideMainTextPanel(syncManager).margin(4, 2));
    }

    private Widget<?> getWideMainTextPanel(PanelSyncManager syncManager) {
        ParentWidget<?> panel = new ParentWidget<>();
        ListWidget<IWidget, ?> list = new ListWidget<>()
                .width(214)
                .height(154)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .posRel(Alignment.CenterLeft)
                .left(3)
                .top(3);
        panel.size(220, 160).background(GuiTextures.DISPLAY);
        list.children(getWidgetsForDisplay(syncManager));
        panel.child(list);
        return panel;
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ModularPowerStationMachine station)) {
            return RecipeModifier.nullWrongType(ModularPowerStationMachine.class, machine);
        }
        if (!station.isAssemblyReady()) return ModifierFunction.NULL;

        EnergyStack output = recipe.getOutputEUt();
        long baseOutput = output.getTotalEU();
        if (output.isEmpty() || baseOutput <= 0) return ModifierFunction.NULL;

        int maximumParallel = (int) Math.min(Integer.MAX_VALUE, station.getRatedOutput() / baseOutput);
        if (maximumParallel < 1) return ModifierFunction.NULL;
        int actualParallel = ParallelLogic.getParallelAmountFast(station, recipe, maximumParallel);
        if (actualParallel < 1) return ModifierFunction.NULL;

        double outputFraction = station.getOutputFraction();
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(actualParallel))
                .outputModifier(ContentModifier.multiplier(actualParallel))
                .eutMultiplier(actualParallel * outputFraction)
                .durationMultiplier(1.0 / outputFraction)
                .parallels(actualParallel)
                .build();
    }

    public DriveType getDriveType() {
        return DriveType.byOrdinal(driveType);
    }

    public AssemblyStatus getAssemblyStatus() {
        return AssemblyStatus.byOrdinal(assemblyStatus);
    }

    public boolean isAssemblyReady() {
        return getAssemblyStatus() == AssemblyStatus.READY;
    }

    public long getRatedAmperage() {
        return ratedAmperage;
    }

    public long getRatedOutput() {
        return getStatorVoltage() * getRatedAmperage();
    }

    public long getOperatingOutputLimit() {
        return Math.round(getRatedOutput() * getOutputFraction());
    }

    public double getOperatingAmperage() {
        return getRatedAmperage() * getOutputFraction();
    }

    public int getThrottleReduction() {
        return throttleReduction;
    }

    public int getOutputPercent() {
        return 100 - throttleReduction;
    }

    public void setThrottleReduction(int throttleReduction) {
        this.throttleReduction = Mth.clamp(throttleReduction, 0, 95);
    }

    public void setOutputPercent(int outputPercent) {
        setThrottleReduction(100 - Mth.clamp(outputPercent, 5, 100));
    }

    private double getOutputFraction() {
        return getOutputPercent() / 100.0;
    }

    private long getStatorVoltage() {
        if (statorTier < GTValues.LV || statorTier > GTValues.EV) return 0;
        return GTValues.V[statorTier];
    }

    private void resolveAssembly() {
        DriveType detectedDrive = DriveType.NONE;
        AssemblyStatus detectedStatus = AssemblyStatus.READY;
        int detectedStages = 0;

        for (int stage = 0; stage < MAXIMUM_STAGES; stage++) {
            int stageLeft = ModularPowerStationPatterns.stageBodyLeftOffset(stage);
            if (!ModularPowerStationPatterns.isIntegral(blockAt(-1, stageLeft, -1))) break;
            DriveType stageDrive = inspectStage(stageLeft);
            detectedStages++;
            if (stageDrive == DriveType.NONE) {
                detectedStatus = AssemblyStatus.INVALID_STAGE;
                break;
            }
            if (detectedDrive == DriveType.NONE) {
                detectedDrive = stageDrive;
            } else if (detectedDrive != stageDrive) {
                detectedStatus = AssemblyStatus.MIXED_STAGES;
                break;
            }
        }

        if (detectedStages == 0) detectedStatus = AssemblyStatus.NO_STAGES;
        int detectedStatorTier = detectStatorTier(
                ModularPowerStationPatterns.outputStartLeftOffset(detectedStages));
        if (detectedStatus == AssemblyStatus.READY && detectedStatorTier == -2) {
            detectedStatus = AssemblyStatus.MIXED_STATORS;
        } else if (detectedStatus == AssemblyStatus.READY && detectedStatorTier < 0) {
            detectedStatus = AssemblyStatus.MISSING_STATOR;
        }
        IEnergyContainer dynamo = getDynamoEnergyContainer();
        if (detectedStatus == AssemblyStatus.READY &&
                (dynamo == null || dynamo.getOutputVoltage() != GTValues.V[detectedStatorTier])) {
            detectedStatus = AssemblyStatus.OUTPUT_MISMATCH;
        }
        applyAssembly(detectedDrive, detectedStages, detectedStatorTier, detectedStatus);
    }

    private DriveType inspectStage(int stageLeft) {
        Block integral = null;
        for (int up = -1; up <= 1; up++) {
            for (int back = 1; back <= 3; back++) {
                if (up == 0 && back == 2) continue;
                Block candidate = blockAt(up, stageLeft, -back);
                if (!ModularPowerStationPatterns.isIntegral(candidate)) return DriveType.NONE;
                if (integral == null) {
                    integral = candidate;
                } else if (integral != candidate) {
                    return DriveType.NONE;
                }
            }
        }

        DriveType drive = integral == CosmicBlocks.STEAM_GAS_TURBINE_INTEGRAL_COMPONENTS.get() ?
                DriveType.TURBINE : DriveType.COMBUSTION;
        for (int back = 1; back <= 3; back++) {
            BlockState optionalCasing = stateAt(-2, stageLeft, -back);
            if (drive == DriveType.TURBINE &&
                    optionalCasing.getBlock() != CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING.get()) {
                return DriveType.NONE;
            }
        }
        return drive;
    }

    private int detectStatorTier(int outputStartLeft) {
        int detectedTier = -1;
        for (int left = outputStartLeft; left < outputStartLeft + 7; left++) {
            for (int up = -2; up <= 2; up++) {
                for (int back = 0; back <= 4; back++) {
                    int tier = ModularPowerStationPatterns.statorTier(blockAt(up, left, -back));
                    if (tier < 0) continue;
                    if (detectedTier < 0) {
                        detectedTier = tier;
                    } else if (detectedTier != tier) {
                        return -2;
                    }
                }
            }
        }
        return detectedTier;
    }

    private Block blockAt(int upOffset, int leftOffset, int forwardOffset) {
        return stateAt(upOffset, leftOffset, forwardOffset).getBlock();
    }

    private BlockState stateAt(int upOffset, int leftOffset, int forwardOffset) {
        if (getLevel() == null) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        BlockPos pos = RelativeDirection.offsetPos(getBlockPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(),
                upOffset, leftOffset, forwardOffset);
        return getLevel().getBlockState(pos);
    }

    private @Nullable IEnergyContainer getDynamoEnergyContainer() {
        for (IRecipeHandler<?> handler : getCapabilitiesFlat(IO.OUT, EURecipeCapability.CAP)) {
            if (handler instanceof IEnergyContainer container && container.getOutputAmperage() > 0) {
                return container;
            }
        }
        return null;
    }

    private void selectCompatibleRecipeType() {
        DriveType drive = getDriveType();
        if (drive == DriveType.COMBUSTION) {
            setActiveRecipeType(1);
        } else if (drive == DriveType.TURBINE) {
            setActiveRecipeType(0);
        }
    }

    private Component statorLine() {
        if (statorTier < GTValues.LV || statorTier > GTValues.EV) {
            return Component.translatable("cosmiccore.multiblock.modular_power_station.stator", "?", "?")
                    .withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable("cosmiccore.multiblock.modular_power_station.stator",
                GTValues.VN[statorTier], FormattingUtil.formatNumbers(GTValues.V[statorTier]))
                .withStyle(ChatFormatting.WHITE);
    }

    private Component fuelConsumptionLine() {
        GTRecipe activeRecipe = recipeLogic.getLastRecipe();
        if (activeRecipe == null || activeRecipe.duration <= 0) {
            return Component.translatable("cosmiccore.multiblock.modular_power_station.fuel.idle")
                    .withStyle(ChatFormatting.WHITE);
        }
        List<FluidStack> fluids = RecipeHelper.getInputFluids(activeRecipe);
        if (fluids.isEmpty()) {
            return Component.translatable("cosmiccore.multiblock.modular_power_station.fuel.none")
                    .withStyle(ChatFormatting.WHITE);
        }
        FluidStack fuel = fluids.getFirst();
        double perMinute = fuel.getAmount() * 1200.0 / activeRecipe.duration;
        double perHour = fuel.getAmount() * 72000.0 / activeRecipe.duration;
        return Component.translatable("cosmiccore.multiblock.modular_power_station.fuel.rate",
                fuel.getHoverName(), FormattingUtil.formatNumbers(perMinute), FormattingUtil.formatNumbers(perHour))
                .withStyle(ChatFormatting.WHITE);
    }

    private void clearAssembly() {
        applyAssembly(DriveType.NONE, 0, -1, AssemblyStatus.NO_STAGES);
    }

    private void applyAssembly(DriveType drive, int stages, int stator, AssemblyStatus status) {
        driveType = drive.ordinal();
        stageCount = stages;
        statorTier = stator;
        assemblyStatus = status.ordinal();
        IEnergyContainer dynamo = getDynamoEnergyContainer();
        ratedAmperage = status == AssemblyStatus.READY && dynamo != null ?
                Math.min((long) stages * AMPERAGE_PER_STAGE, dynamo.getOutputAmperage()) : 0;
        if (!isRemote()) {
            getSyncDataHolder().markClientSyncFieldDirty("driveType");
            getSyncDataHolder().markClientSyncFieldDirty("stageCount");
            getSyncDataHolder().markClientSyncFieldDirty("statorTier");
            getSyncDataHolder().markClientSyncFieldDirty("assemblyStatus");
            getSyncDataHolder().markClientSyncFieldDirty("ratedAmperage");
        }
    }

    public enum DriveType {

        NONE("cosmiccore.multiblock.modular_power_station.drive.none"),
        TURBINE("cosmiccore.multiblock.modular_power_station.drive.turbine"),
        COMBUSTION("cosmiccore.multiblock.modular_power_station.drive.combustion");

        private final String translationKey;

        DriveType(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }

        public boolean acceptsRecipeType(int recipeType) {
            return switch (this) {
                case TURBINE -> recipeType == 0;
                case COMBUSTION -> recipeType == 1;
                default -> false;
            };
        }

        public static DriveType byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : NONE;
        }
    }

    public enum AssemblyStatus {

        READY("cosmiccore.multiblock.modular_power_station.status.ready"),
        NO_STAGES("cosmiccore.multiblock.modular_power_station.status.no_stages"),
        INVALID_STAGE("cosmiccore.multiblock.modular_power_station.status.invalid_stage"),
        MIXED_STAGES("cosmiccore.multiblock.modular_power_station.status.mixed_stages"),
        MISSING_STATOR("cosmiccore.multiblock.modular_power_station.status.missing_stator"),
        MIXED_STATORS("cosmiccore.multiblock.modular_power_station.status.mixed_stators"),
        OUTPUT_MISMATCH("cosmiccore.multiblock.modular_power_station.status.output_mismatch");

        private final String translationKey;

        AssemblyStatus(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }

        public static AssemblyStatus byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : NO_STAGES;
        }
    }
}
