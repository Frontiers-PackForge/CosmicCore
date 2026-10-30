package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.utils.CosmicFormattingUtil;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DimensionalEnergyInterface extends MuiWorkableMultiblockMachine {

    protected static final long ticks_between_save_data_operations = 5L * 20L; // Once per 5s

    protected MaintenanceHatchPartMachine maintenance;
    protected EnergyContainerList inputHatches;
    protected EnergyContainerList outputHatches;
    protected long passiveDrain;

    @SaveField
    protected final NotifiableEnergyContainer energyBuffer;

    // Stats tracked for UI display
    private long netInLastSec;
    private long netOutLastSec;
    private long averageInLastSec;
    private long averageOutLastSec;
    protected boolean localDisplay;

    protected ConditionalSubscriptionHandler tickSubscription;

    private static final int DISPLAY_WIDTH = 214;

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var list = new ListWidget<>()
                .width(DISPLAY_WIDTH).height(130)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren().left(3).top(3);
        list.children(getWidgetsForDisplay(syncManager));
        mainWidget.size(DISPLAY_WIDTH + 6, 136).background(GuiTextures.DISPLAY).child(list);
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        var local = new BooleanSyncValue(() -> localDisplay, value -> localDisplay = value).allowC2S();
        var available = new BooleanSyncValue(this::isEnergyDisplayAvailable);
        syncManager.syncValue("energyDisplayLocal", local);
        syncManager.syncValue("energyDisplayAvailable", available);
        for (int index = 0; index < 9; index++) {
            if (index == 2) {
                widgets.add(Flow.row().width(DISPLAY_WIDTH).height(16)
                        .child(Text.dynamic(() -> Component.translatable(local.getBoolValue() ?
                                "cosmic.multiblock.capacitor.info.tittle.local" :
                                "cosmic.multiblock.capacitor.info.tittle.global"))
                                .asWidget().color(0xFFFFFF).width(DISPLAY_WIDTH - 63).height(16))
                        .child(new ButtonWidget<>().size(48, 16)
                                .overlay(Text.dynamic(() -> Component.translatable(local.getBoolValue() ?
                                        "cosmic.multiblock.capacitor.info.global" :
                                        "cosmic.multiblock.capacitor.info.local").withStyle(ChatFormatting.WHITE)))
                                .onMousePressed((context, button) -> {
                                    if (button != 0) return false;
                                    local.setValue(!local.getBoolValue());
                                    return true;
                                }))
                        .setEnabledIf(widget -> available.getBoolValue()));
            }
            int lineIndex = index;
            var line = GenericSyncValue.<RegistryFriendlyByteBuf, Component>builder(Component.class)
                    .getter(() -> getEnergyDisplayLine(lineIndex)).adapter(ByteBufAdapters.COMPONENT)
                    .copy(Component::copy).equalsDefault().build();
            syncManager.syncValue("energyDisplayLine", index, line);
            var widget = Text.dynamic(line::getValue).asWidget().color(0xFFFFFF).width(DISPLAY_WIDTH)
                    .setEnabledIf(w -> !line.getValue().getString().isEmpty());
            if (index == 6 || index == 7) {
                widget.tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(lineIndex == 6 ?
                        "gtceu.multiblock.power_substation.average_in_hover" :
                        "gtceu.multiblock.power_substation.average_out_hover")));
            }
            widgets.add(widget);
        }
        widgets.addAll(getDefinition().getAdditionalDisplay().apply(this, syncManager));
        return widgets;
    }

    protected boolean isEnergyDisplayAvailable() {
        return getLevel() instanceof ServerLevel && isFormed() && !MachineOwner.EMPTY.equals(getTeamUUID());
    }

    protected Component getEnergyDisplayLine(int line) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return Component.empty();
        var owner = getTeamUUID();
        if (MachineOwner.EMPTY.equals(owner)) {
            return line == 0 ? Component.translatable("cosmic.multiblock.capacitor.owner.null")
                    .withStyle(ChatFormatting.RED) : Component.empty();
        }
        if (!isFormed()) {
            return line == 0 ? Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(ChatFormatting.RED) : Component.empty();
        }
        if (line == 0) return Component.translatable(!isWorkingEnabled() ? "gtceu.multiblock.work_paused" :
                isActive() ? "gtceu.multiblock.running" : "gtceu.multiblock.idling");
        if (line == 1) return recipeLogic.isWaiting() ? Component.translatable("gtceu.multiblock.waiting")
                .withStyle(ChatFormatting.RED) : Component.empty();
        var data = WirelessEnergySavedData.getOrCreate(serverLevel);
        BigInteger stored = data.getEnergyStored(owner).add(BigInteger.valueOf(energyBuffer.getEnergyStored()));
        BigInteger value;
        String key;
        ChatFormatting color = ChatFormatting.GOLD;
        int limit = 12;
        switch (line) {
            case 2 -> {
                key = "gtceu.multiblock.power_substation.stored";
                value = stored;
            }
            case 3 -> {
                key = "gtceu.multiblock.power_substation.capacity";
                value = data.getEnergyCapacity(owner);
            }
            case 4 -> {
                key = "cosmic.multiblock.capacitor.buffered";
                value = BigInteger.valueOf(energyBuffer.getEnergyStored());
                if (!localDisplay) value = value.add(data.getEnergyBufferedExceptLocal(owner, getBlockPos()));
            }
            case 5 -> {
                key = "gtceu.multiblock.power_substation.passive_drain";
                value = BigInteger.valueOf(localDisplay ? getPassiveDrain() : data.getPassiveDrain(owner));
                color = ChatFormatting.DARK_RED;
                limit = 9;
            }
            case 6 -> {
                key = "gtceu.multiblock.power_substation.average_in";
                value = BigInteger.valueOf(localDisplay ? averageInLastSec : data.getEnergyInput(owner));
                color = ChatFormatting.GREEN;
                limit = 10;
            }
            case 7 -> {
                key = "gtceu.multiblock.power_substation.average_out";
                value = BigInteger.valueOf(localDisplay ? averageOutLastSec : data.getEnergyOutput(owner)).abs();
                color = ChatFormatting.RED;
                limit = 10;
            }
            case 8 -> {
                if (localDisplay) return Component.empty();
                BigInteger net = BigInteger.valueOf(data.getEnergyInput(owner))
                        .subtract(BigInteger.valueOf(data.getEnergyOutput(owner))).multiply(BigInteger.valueOf(20));
                if (net.signum() == 0) return Component.empty();
                boolean filling = net.signum() > 0;
                BigInteger remaining = filling ? data.getEnergyCapacity(owner).subtract(data.getEnergyStored(owner)) :
                        stored;
                return CosmicFormattingUtil.formatWithConstantWidth(filling ?
                        "gtceu.multiblock.power_substation.time_to_fill" :
                        "gtceu.multiblock.power_substation.time_to_drain",
                        CosmicFormattingUtil.formatEnergyStorageDuration(remaining.divide(net.abs()))
                                .withStyle(filling ? ChatFormatting.GREEN : ChatFormatting.RED),
                        DISPLAY_WIDTH);
            }
            default -> {
                return Component.empty();
            }
        }
        return CosmicFormattingUtil.formatWithConstantWidth(key,
                Component.literal(CosmicFormattingUtil.formatNumberWithCharacterLimit(value, limit)).withStyle(color),
                DISPLAY_WIDTH);
    }

    public DimensionalEnergyInterface(BlockEntityCreationInfo info) {
        super(info);
        this.energyBuffer = attachTrait(new NotifiableEnergyContainer(0, Long.MAX_VALUE, Long.MAX_VALUE,
                Long.MAX_VALUE, Long.MAX_VALUE));
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::transferEnergyTick, this::isActive);
        this.localDisplay = true;
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (getLevel() == null || getLevel().isClientSide) return;

        initializeAbilities();
        setEnergyBuffer();

        tickSubscription.updateSubscription();
    }

    private void initializeAbilities() {
        List<IEnergyContainer> inputs = new ArrayList<>();
        List<IEnergyContainer> outputs = new ArrayList<>();

        for (MultiblockPartMachine part : getParts()) {
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if (handlerList.getHandlerIO() == IO.IN) {
                    handlerList.getCapability(EURecipeCapability.CAP).stream()
                            .filter(IEnergyContainer.class::isInstance)
                            .map(IEnergyContainer.class::cast)
                            .forEach(inputs::add);
                } else if (handlerList.getHandlerIO() == IO.OUT) {
                    handlerList.getCapability(EURecipeCapability.CAP).stream()
                            .filter(IEnergyContainer.class::isInstance)
                            .map(IEnergyContainer.class::cast)
                            .forEach(outputs::add);
                }
            }
        }

        this.inputHatches = new EnergyContainerList(inputs);
        this.outputHatches = new EnergyContainerList(outputs);
    }

    protected UUID getTeamUUID() {
        var owner = getOwner();
        var ownerUUID = getOwnerUUID();
        // Faultcheck the Owner and OwnerUUID
        if (owner == null) return MachineOwner.EMPTY;
        if (ownerUUID == null) return MachineOwner.EMPTY;

        var team = owner instanceof FTBOwner ftbOwner ? ftbOwner.getPlayerTeam(ownerUUID) : null;
        if (team == null) return ownerUUID;

        return team.getTeamId();
    }

    @Override
    public void invalidateStructure(String name) {
        if (getLevel() instanceof ServerLevel serverLevel) { // Transfer buffer content to avoid losses
            var data = WirelessEnergySavedData.getOrCreate(serverLevel);
            var owner = getTeamUUID();
            if (owner != MachineOwner.EMPTY) {
                if (energyBuffer != null) {
                    data.addEUToGlobalWirelessEnergy(owner, energyBuffer.getEnergyStored());
                    energyBuffer.removeEnergy(energyBuffer.getEnergyStored());
                }
                data.removeEnergyBuffered(owner, getBlockPos());
                data.removeEnergyInput(owner, getBlockPos());
                data.removeEnergyOutput(owner, getBlockPos());
                data.removePassiveDrain(owner, getBlockPos());
            }
            this.inputHatches = null;
            this.outputHatches = null;
            this.energyBuffer.setEnergyStored(0);
            this.energyBuffer.resetBasicInfo(0, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
            this.passiveDrain = 0;
            this.netInLastSec = 0;
            this.averageInLastSec = 0;
            this.netOutLastSec = 0;
            this.averageOutLastSec = 0;
        }

        tickSubscription.unsubscribe();
        super.invalidateStructure(name);
    }

    public boolean isActive() {
        return isFormed();
    }

    private void setEnergyBuffer() {
        long totalIOPerTick = (inputHatches.getInputVoltage() + outputHatches.getOutputVoltage());
        // Size is the totalIOPerTick over the duration between operations doubled
        long bufferSize = totalIOPerTick *
                (ticks_between_save_data_operations + (ticks_between_save_data_operations / 2L)) * 2L;
        bufferSize += (getPassiveDrainPerTick() * 8 * 2) * ticks_between_save_data_operations;
        if (bufferSize < 0L)
            throw new RuntimeException("DimensionalEnergyCapacitor: Calculated buffer size is too big.");
        this.energyBuffer.resetBasicInfo(bufferSize, Long.MAX_VALUE, Long.MAX_VALUE,
                Long.MAX_VALUE, Long.MAX_VALUE);
    }

    public long getPassiveDrainPerTick() {
        return 20_000L; // 0 in the interfaces, Overridden in the Capacitor
    }

    public long getPassiveDrain() {
        if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
            if (maintenance == null) {
                for (MultiblockPartMachine part : getParts()) {
                    if (part instanceof MaintenanceHatchPartMachine maintenanceMachine) {
                        this.maintenance = maintenanceMachine;
                        break;
                    }
                }
            }
            if (maintenance == null) return getPassiveDrainPerTick();
            int multiplier = 1 + maintenance.getNumMaintenanceProblems();
            double modifier = maintenance.getDurationMultiplier();
            return (long) (getPassiveDrainPerTick() * multiplier * modifier);
        }
        return getPassiveDrainPerTick();
    }

    protected void transferEnergyTick() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            var data = WirelessEnergySavedData.getOrCreate(serverLevel);
            var owner = getTeamUUID();

            if (isWorkingEnabled() && isFormed() && owner != MachineOwner.EMPTY) {
                if (getOffsetTimer() % 20 == 0) {
                    getRecipeLogic().setStatus((energyBuffer != null && energyBuffer.getEnergyStored() > 0) ?
                            RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE);

                    averageInLastSec = netInLastSec / 20;
                    averageOutLastSec = netOutLastSec / 20;
                    netInLastSec = 0;
                    netOutLastSec = 0;

                    // Send IO values to global Storage to display in the Dimensional Storage.
                    data.setEnergyInput(owner, getBlockPos(), averageInLastSec);
                    data.setEnergyOutput(owner, getBlockPos(), averageOutLastSec);
                    data.setEnergyBuffered(owner, getBlockPos(), energyBuffer.getEnergyStored());
                }

                // Handle inputs
                long energyBuffered = energyBuffer.addEnergy(inputHatches.getEnergyStored());
                inputHatches.changeEnergy(-energyBuffered);
                netInLastSec += energyBuffered;

                // Passive Drain
                long energyPassiveDrained = energyBuffer.removeEnergy(getPassiveDrain());
                netOutLastSec += energyPassiveDrained;

                // Handle outputs
                long energyNeed = outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored();
                long energyDeBuffered = energyBuffer.removeEnergy(energyNeed);
                outputHatches.changeEnergy(energyDeBuffered);
                netOutLastSec += energyDeBuffered;

                // Handle buffer transfer to WirelessEnergySavedData
                if (getOffsetTimer() % ticks_between_save_data_operations == 0) {
                    if (data.isActive(owner)) {
                        // After operation buffer should aim to be 50% full
                        var euToTransfer = energyBuffer.getEnergyStored() - (energyBuffer.getEnergyCapacity() / 2);
                        var euTransferred = data.addEUToGlobalWirelessEnergy(owner, euToTransfer);
                        energyBuffer.changeEnergy(-(euToTransfer - euTransferred));
                        data.setEnergyBuffered(owner, getBlockPos(), energyBuffer.getEnergyStored());
                        data.setPassiveDrain(owner, getBlockPos(), getPassiveDrain());
                    }
                }
            } else {
                data.removeEnergyBuffered(owner, getBlockPos());
                data.removeEnergyInput(owner, getBlockPos());
                data.removeEnergyOutput(owner, getBlockPos());
                data.removePassiveDrain(owner, getBlockPos());
            }
        }
    }
}
