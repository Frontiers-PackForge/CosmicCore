package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.utils.CosmicFormattingUtil;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.DummyWorld;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DimensionalEnergyInterface extends WorkableMultiblockMachine
    implements IFancyUIMachine, IDisplayUIMachine {

//    protected static final long ticks_between_save_data_operations = 60L * 20L; // Once per minute
    protected static final long ticks_between_save_data_operations = 10L * 20L; // Once per 10s

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DimensionalEnergyInterface.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    protected IMaintenanceMachine maintenance;
    protected EnergyContainerList inputHatches;
    protected EnergyContainerList outputHatches;

    @Persisted
    protected IEnergyContainer energyBuffer;

    // Stats tracked for UI display
    private long netInLastSec;
    private long netOutLastSec;
    private long averageInLastSec;
    private long averageOutLastSec;
    protected boolean localDisplay;

    protected ConditionalSubscriptionHandler tickSubscription;

    public DimensionalEnergyInterface(IMachineBlockEntity holder) {
        super(holder);
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::transferEnergyTick, this::isFormed);
        this.localDisplay = true;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof DummyWorld) return;

        List<IEnergyContainer> inputs = new ArrayList<>();
        List<IEnergyContainer> outputs = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE) continue;
            if (part instanceof IMaintenanceMachine maintenanceMachine) this.maintenance = maintenanceMachine;
            for (var handler : part.getRecipeHandlers()) {
                var handlerIO = handler.getHandlerIO();
                if (io != IO.BOTH && handlerIO != IO.BOTH && io != handlerIO) continue;
                if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    if (handlerIO == IO.IN) inputs.add(container);
                    else if (handlerIO == IO.OUT) outputs.add(container);
                    traitSubscriptions.add(handler.addChangedListener(tickSubscription::updateSubscription));
                }
            }
        }
        this.inputHatches = new EnergyContainerList(inputs);
        this.outputHatches = new EnergyContainerList(outputs);

        setEnergyBuffer();

        tickSubscription.updateSubscription();
    }

    private void setEnergyBuffer() {
        long totalIOPerTick = (inputHatches.getInputVoltage() + outputHatches.getOutputVoltage());
        // Size is the totalIOPerTick over the duration between operations doubled
        long bufferSize = totalIOPerTick * (ticks_between_save_data_operations + (ticks_between_save_data_operations / 2L)) * 2L;
        if (bufferSize < 0L) throw new RuntimeException("DimensionalEnergyCapacitor: Calculated buffer size is too big.");
        this.energyBuffer = new NotifiableEnergyContainer(this, bufferSize, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
    }

    @Override
    public void onStructureInvalid() {
        if (getLevel() instanceof ServerLevel serverLevel) { // Transfer buffer content to avoid losses
            var data = WirelessEnergySavedData.getOrCreate(serverLevel);
            data.addEUToGlobalWirelessEnergy(getHolder().getOwner().getUUID(), energyBuffer.getEnergyStored());
        }

        this.inputHatches = null;
        this.outputHatches = null;
        this.energyBuffer = null;

        super.onStructureInvalid();
    }

    protected void transferEnergyTick() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            if(isWorkingEnabled() && isFormed()) {
                if (getOffsetTimer() % 20 == 0) {
                    // TODO: handle WORKING / IDLE
                    averageInLastSec = netInLastSec / 20;
                    averageOutLastSec = netOutLastSec / 20;
                    netInLastSec = 0;
                    netOutLastSec = 0;

                    // Send IO values to global Storage to display in the Dimensional Storage.
                    var data = WirelessEnergySavedData.getOrCreate(serverLevel);
                    var owner = getHolder().getOwner().getUUID();
                    data.setEnergyInput(owner, getPos(), averageInLastSec);
                    data.setEnergyOutput(owner, getPos(), averageOutLastSec);
                    data.setEnergyBuffered(owner, getPos(), energyBuffer.getEnergyStored());
                }

                // Handle inputs
                long energyBuffered = energyBuffer.addEnergy(inputHatches.getEnergyStored());
                inputHatches.changeEnergy(-energyBuffered);
                netInLastSec += energyBuffered;

                // Handle outputs
                long energyNeed = outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored();
                long energyDeBuffered = energyBuffer.removeEnergy(energyNeed);
                outputHatches.changeEnergy(energyDeBuffered);
                netOutLastSec += energyDeBuffered;

                // Handle buffer transfer to WirelessEnergySavedData
                if (getOffsetTimer() % ticks_between_save_data_operations == 0) {
                    var data = WirelessEnergySavedData.getOrCreate(serverLevel);
                    var owner = getHolder().getOwner().getUUID();
                    if (data.isActive(owner)) {
                        // After operation buffer should aim to be 50% full
                        var euToTransfer = energyBuffer.getEnergyStored() - (energyBuffer.getEnergyCapacity() / 2);
                        var euTransferred = data.addEUToGlobalWirelessEnergy(owner, euToTransfer);
                        energyBuffer.changeEnergy(-(euToTransfer - euTransferred));
                        data.setEnergyBuffered(owner, getPos(), energyBuffer.getEnergyStored());
                    }
                }
            }
        }
    }


    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (this.isFormed()){
            // Multiblock status
            if (!isWorkingEnabled()) textList.add(Component.translatable("gtceu.multiblock.work_paused"));
            else if (isActive()) textList.add(Component.translatable("gtceu.multiblock.running"));
            else textList.add(Component.translatable("gtceu.multiblock.idling"));

            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.waiting").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }

            if (energyBuffer != null) {
                if (getLevel() instanceof ServerLevel serverLevel) {
                    var owner = getHolder().getOwner().getUUID();
                    var data = WirelessEnergySavedData.getOrCreate(serverLevel);

                    var STYLE_GOLD = Style.EMPTY.withColor(ChatFormatting.GOLD);
                    var STYLE_DARK_RED = Style.EMPTY.withColor(ChatFormatting.DARK_RED);
                    var STYLE_GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);
                    var STYLE_RED = Style.EMPTY.withColor(ChatFormatting.RED);

                    textList.add((localDisplay ?
                            Component.translatable("cosmic.multiblock.capacitor.info.tittle.local") :
                            Component.translatable("cosmic.multiblock.capacitor.info.tittle.global"))
                            .append(ComponentPanelWidget.withButton(Component.literal(" [")
                                    .append(localDisplay ?
                                            Component.translatable("cosmic.multiblock.capacitor.info.global") :
                                            Component.translatable("cosmic.multiblock.capacitor.info.local"))
                                    .append(Component.literal("]")), "local_display")));

                    BigInteger energyCapacity = data.getEnergyCapacity(owner);;
                    BigInteger energyStored = data.getTotalNetworkEnergyStoredExceptLocalBuffer(owner, getPos());
                    energyStored = energyStored.add(BigInteger.valueOf(energyBuffer.getEnergyStored()));
                    BigInteger energyBuffered;
                    long avgIn;
                    long avgOut;

                    if (localDisplay) {
                        energyBuffered = BigInteger.valueOf(energyBuffer.getEnergyStored());
                        avgIn = averageInLastSec;
                        avgOut = averageOutLastSec;
                    } else {
                        energyBuffered = data.getEnergyBufferedExceptLocal(owner, getPos());
                        energyBuffered = energyBuffered.add(BigInteger.valueOf(energyBuffer.getEnergyStored()));
                        avgIn = data.getEnergyInput(owner);
                        avgOut = data.getEnergyOutput(owner);
                    }

                    int width = 182 - 32;
                    var storedComponent = Component.literal(CosmicFormattingUtil.formatNumberWithCharacterLimit(energyStored, 12));
                    textList.add(combineWithConstantWidth("gtceu.multiblock.power_substation.stored", storedComponent.setStyle(STYLE_GOLD), width));

                    var capacityComponent = Component.literal(CosmicFormattingUtil.formatNumberWithCharacterLimit(energyCapacity, 12));
                    textList.add(combineWithConstantWidth("gtceu.multiblock.power_substation.capacity", capacityComponent.setStyle(STYLE_GOLD), width));

                    var bufferedComponent = Component.literal(CosmicFormattingUtil.formatNumberWithCharacterLimit(energyBuffered, 12));
                    textList.add(combineWithConstantWidth("cosmic.multiblock.capacitor.buffered", bufferedComponent.setStyle(STYLE_GOLD), width));

                    var avgInComponent = Component.literal(CosmicFormattingUtil.formatNumberWithCharacterLimit(BigInteger.valueOf(avgIn), 10));
                    textList.add(combineWithConstantWidth("gtceu.multiblock.power_substation.average_in", avgInComponent.setStyle(STYLE_GREEN), width)
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("gtceu.multiblock.power_substation.average_in_hover")))));

                    var avgOutComponent = Component.literal(CosmicFormattingUtil.formatNumberWithCharacterLimit(BigInteger.valueOf(Math.abs(avgOut)), 10));
                    textList.add(combineWithConstantWidth("gtceu.multiblock.power_substation.average_out", avgOutComponent.setStyle(STYLE_RED), width)
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("gtceu.multiblock.power_substation.average_out_hover")))));
                }
            }

        }
        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

    private MutableComponent combineWithConstantWidth(String labelKey, Component body, int width) {
        var tmp = Component.translatable(labelKey, body);
        var baseLength = getComponentLength(tmp);
        var spaceLength = width - baseLength;
        if (spaceLength <= 0) return Component.literal("Err: Too long");
        var spacerComponent = Component.literal(".".repeat((spaceLength / 2) - 4) + " ").withStyle(ChatFormatting.DARK_GRAY);
        return Component.translatable(labelKey, spacerComponent.append(body));
    }

    private int getComponentLength(Component component) {
        return Minecraft.getInstance().font.width(component.getString());
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
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(150).clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }
}
