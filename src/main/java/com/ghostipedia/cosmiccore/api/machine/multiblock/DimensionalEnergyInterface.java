package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.utils.CosmicFormattingUtil;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.DummyWorld;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ghostipedia.cosmiccore.utils.CosmicFormattingUtil.combineWithConstantWidth;
import static com.ghostipedia.cosmiccore.utils.CosmicFormattingUtil.formatWithConstantWidth;

public class DimensionalEnergyInterface extends WorkableMultiblockMachine
                                        implements IFancyUIMachine, IDisplayUIMachine {

    protected static final long ticks_between_save_data_operations = 5L * 20L; // Once per 5s
    private static final int uiWidth = 182;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DimensionalEnergyInterface.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    protected IMaintenanceMachine maintenance;
    protected EnergyContainerList inputHatches;
    protected EnergyContainerList outputHatches;
    protected long passiveDrain;

    @Persisted
    protected IEnergyContainer energyBuffer;

    // Stats tracked for UI display
    private long netInLastSec;
    private long netOutLastSec;
    private long averageInLastSec;
    private long averageOutLastSec;
    protected boolean localDisplay;

    protected ConditionalSubscriptionHandler tickSubscription;

    public DimensionalEnergyInterface(BlockEntityCreationInfo holder) {
        super(holder);
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::transferEnergyTick, this::isActive);
        this.localDisplay = true;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof DummyWorld) return;

        initializeAbilities();
        setEnergyBuffer();

        tickSubscription.updateSubscription();
    }

    private void initializeAbilities() {
        List<IEnergyContainer> inputs = new ArrayList<>();
        List<IEnergyContainer> outputs = new ArrayList<>();

        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getBlockPos().asLong(), IO.BOTH);
            if (io == IO.NONE) continue;

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if (!handlerList.isValid(io)) continue;
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
        // CosmicCore.LOGGER.warn("getting team UUID");
        var owner = getOwner();
        var ownerUUID = getOwnerUUID();
        // Faultcheck the Owner and OwnerUUID
        if (owner == null) return MachineOwner.EMPTY;
        if (ownerUUID == null) return MachineOwner.EMPTY;

        // CosmicCore.LOGGER.warn("Owner UUID: " + ownerUUID.toString());
        var team = ((FTBOwner) owner).getPlayerTeam(ownerUUID);
        if (team == null) return ownerUUID;

        // CosmicCore.LOGGER.warn("Team UUID: " + team);
        // CosmicCore.LOGGER.warn("Team UUID: " + team.getTeamId());
        return team.getTeamId();
    }

    @Override
    public void onStructureInvalid() {
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
            this.energyBuffer = null;
            this.passiveDrain = 0;
            this.netInLastSec = 0;
            this.averageInLastSec = 0;
            this.netOutLastSec = 0;
            this.averageOutLastSec = 0;
        }

        tickSubscription.unsubscribe();
        super.onStructureInvalid();
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
        this.energyBuffer = new NotifiableEnergyContainer(this, bufferSize, Long.MAX_VALUE, Long.MAX_VALUE,
                Long.MAX_VALUE, Long.MAX_VALUE);
    }

    public long getPassiveDrainPerTick() {
        return 20_000L; // 0 in the interfaces, Overridden in the Capacitor
    }

    public long getPassiveDrain() {
        if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
            if (maintenance == null) {
                for (IMultiPart part : getParts()) {
                    if (part instanceof IMaintenanceMachine maintenanceMachine) {
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

    private static MutableComponent getTimeToFillDrainText(BigInteger timeToFillSeconds) {
        if (timeToFillSeconds.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
            // too large to represent in a java Duration
            timeToFillSeconds = BIG_INTEGER_MAX_LONG;
        }

        Duration duration = Duration.ofSeconds(timeToFillSeconds.longValue());
        String key;
        long fillTime;
        if (duration.getSeconds() <= 180) {
            fillTime = duration.getSeconds();
            key = "gtceu.multiblock.power_substation.time_seconds";
        } else if (duration.toMinutes() <= 180) {
            fillTime = duration.toMinutes();
            key = "gtceu.multiblock.power_substation.time_minutes";
        } else if (duration.toHours() <= 72) {
            fillTime = duration.toHours();
            key = "gtceu.multiblock.power_substation.time_hours";
        } else if (duration.toDays() <= 730) { // 2 years
            fillTime = duration.toDays();
            key = "gtceu.multiblock.power_substation.time_days";
        } else if (duration.toDays() / 365 < 1_000_000) {
            fillTime = duration.toDays() / 365;
            key = "gtceu.multiblock.power_substation.time_years";
        } else {
            return Component.translatable("gtceu.multiblock.power_substation.time_forever");
        }

        return Component.translatable(key, FormattingUtil.formatNumbers(fillTime));
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

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (this.isFormed()) {
            // Multiblock status
            if (!isWorkingEnabled()) textList.add(Component.translatable("gtceu.multiblock.work_paused"));
            else if (isActive()) textList.add(Component.translatable("gtceu.multiblock.running"));
            else textList.add(Component.translatable("gtceu.multiblock.idling"));

            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.waiting")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }

            var owner = getTeamUUID();
            if (energyBuffer != null && owner != MachineOwner.EMPTY) {
                if (getLevel() instanceof ServerLevel serverLevel) {
                    var data = WirelessEnergySavedData.getOrCreate(serverLevel);

                    var STYLE_GOLD = Style.EMPTY.withColor(ChatFormatting.GOLD);
                    var STYLE_DARK_RED = Style.EMPTY.withColor(ChatFormatting.DARK_RED);
                    var STYLE_GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);
                    var STYLE_RED = Style.EMPTY.withColor(ChatFormatting.RED);

                    // Tittle
                    var buttonComponent = ComponentPanelWidget.withButton(Component.literal("[").append(localDisplay ?
                            Component.translatable("cosmic.multiblock.capacitor.info.global") :
                            Component.translatable("cosmic.multiblock.capacitor.info.local"))
                            .append(Component.literal("]")), "local_display");

                    var labelComponent = localDisplay ?
                            Component.translatable("cosmic.multiblock.capacitor.info.tittle.local") :
                            Component.translatable("cosmic.multiblock.capacitor.info.tittle.global");

                    textList.add(localDisplay ? combineWithConstantWidth(labelComponent, buttonComponent, uiWidth - 4) :
                            combineWithConstantWidth(buttonComponent, labelComponent, uiWidth - 4));

                    BigInteger energyCapacity = data.getEnergyCapacity(owner);;
                    BigInteger energyStored = data.getEnergyStored(owner);
                    energyStored = energyStored.add(BigInteger.valueOf(energyBuffer.getEnergyStored()));
                    BigInteger energyBuffered;
                    long passiveDrain;
                    long avgIn;
                    long avgOut;

                    if (localDisplay) {
                        energyBuffered = BigInteger.valueOf(energyBuffer.getEnergyStored());
                        avgIn = averageInLastSec;
                        avgOut = averageOutLastSec;
                        passiveDrain = getPassiveDrain();
                    } else {
                        energyBuffered = data.getEnergyBufferedExceptLocal(owner, getBlockPos());
                        energyBuffered = energyBuffered.add(BigInteger.valueOf(energyBuffer.getEnergyStored()));
                        avgIn = data.getEnergyInput(owner);
                        avgOut = data.getEnergyOutput(owner);
                        passiveDrain = data.getPassiveDrain(owner);
                    }

                    var storedComponent = Component
                            .literal(CosmicFormattingUtil.formatNumberWithCharacterLimit(energyStored, 12));
                    textList.add(formatWithConstantWidth("gtceu.multiblock.power_substation.stored",
                            storedComponent.setStyle(STYLE_GOLD), uiWidth - 4));

                    var capacityComponent = Component
                            .literal(CosmicFormattingUtil.formatNumberWithCharacterLimit(energyCapacity, 12));
                    textList.add(formatWithConstantWidth("gtceu.multiblock.power_substation.capacity",
                            capacityComponent.setStyle(STYLE_GOLD), uiWidth - 4));

                    var bufferedComponent = Component
                            .literal(CosmicFormattingUtil.formatNumberWithCharacterLimit(energyBuffered, 12));
                    textList.add(formatWithConstantWidth("cosmic.multiblock.capacitor.buffered",
                            bufferedComponent.setStyle(STYLE_GOLD), uiWidth - 4));

                    var passiveDrainComponent = Component.literal(
                            CosmicFormattingUtil.formatNumberWithCharacterLimit(BigInteger.valueOf(passiveDrain), 9));
                    textList.add(formatWithConstantWidth("gtceu.multiblock.power_substation.passive_drain",
                            passiveDrainComponent.setStyle(STYLE_DARK_RED), uiWidth - 4));

                    var avgInComponent = Component.literal(
                            CosmicFormattingUtil.formatNumberWithCharacterLimit(BigInteger.valueOf(avgIn), 10));
                    textList.add(formatWithConstantWidth("gtceu.multiblock.power_substation.average_in",
                            avgInComponent.setStyle(STYLE_GREEN), uiWidth - 4)
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("gtceu.multiblock.power_substation.average_in_hover")))));

                    var avgOutComponent = Component.literal(CosmicFormattingUtil
                            .formatNumberWithCharacterLimit(BigInteger.valueOf(Math.abs(avgOut)), 10));
                    textList.add(formatWithConstantWidth("gtceu.multiblock.power_substation.average_out",
                            avgOutComponent.setStyle(STYLE_RED), uiWidth - 4)
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("gtceu.multiblock.power_substation.average_out_hover")))));

                    if (!localDisplay) {
                        var avgInput = data.getEnergyInput(owner);
                        var avgOutput = data.getEnergyOutput(owner);
                        if (avgInput > avgOutput) {
                            BigInteger timeToFillSeconds = data.getEnergyCapacity(owner)
                                    .subtract(data.getEnergyStored(owner))
                                    .divide(BigInteger.valueOf((avgInput - avgOutput) * 20));
                            textList.add(formatWithConstantWidth("gtceu.multiblock.power_substation.time_to_fill",
                                    getTimeToFillDrainText(timeToFillSeconds).setStyle(STYLE_GREEN), uiWidth - 4));
                        } else if (avgInput < avgOutput) {
                            BigInteger timeToDrainSeconds = energyStored
                                    .divide(BigInteger.valueOf((avgOutput - avgInput) * 20));
                            textList.add(formatWithConstantWidth("gtceu.multiblock.power_substation.time_to_drain",
                                    getTimeToFillDrainText(timeToDrainSeconds).setStyle(STYLE_RED), uiWidth - 4));
                        }
                    }
                }
            }
        }

        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if (componentData.equals("local_display")) {
                localDisplay = !localDisplay;
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, uiWidth + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, uiWidth, 117).setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(uiWidth - 4)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(uiWidth + 8, 208, this, entityPlayer)
                .widget(new FancyMachineUIWidget(this, uiWidth + 8, 208));
    }
}
