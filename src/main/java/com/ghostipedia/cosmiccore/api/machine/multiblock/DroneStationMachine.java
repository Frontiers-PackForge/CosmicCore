package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.machine.part.DroneMaintenanceInterfacePartMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;
import com.ghostipedia.cosmiccore.api.misc.DroneStationNetwork;
import com.ghostipedia.cosmiccore.api.misc.DroneStationServiceLogic;
import com.ghostipedia.cosmiccore.api.misc.DroneStationServiceLogic.DroneTier;
import com.ghostipedia.cosmiccore.api.misc.DroneStationSpace;
import com.ghostipedia.cosmiccore.client.transmission.PowerTowerMELocator;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class DroneStationMachine extends WorkableElectricMultiblockMachine {

    private static final int UI_PAGE_SIZE = 16;

    private final NavigableMap<Long, DroneStationConnection> connections = new TreeMap<>();
    private TickableSubscription serviceSubscription;

    @SaveField
    private int plasmaticUses;
    @SaveField
    private int sanguineUses;
    @SaveField
    private int industrialUses;
    @SaveField
    private int robustUses;
    @SaveField
    private int rustyUses;

    @SyncToClient
    private int activeTier = -1;
    @SyncToClient
    private int activeRemainingUses;
    @SyncToClient
    private int connectedMachineCount;
    @SyncToClient
    private boolean online;

    public DroneStationMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && isFormed()) startService();
    }

    @Override
    public void onUnload() {
        stopService();
        super.onUnload();
    }

    @Override
    public void onMachineDestroyed() {
        stopService();
        super.onMachineDestroyed();
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (!isRemote()) startService();
    }

    @Override
    public void invalidateStructure(String name) {
        stopService();
        super.invalidateStructure(name);
    }

    @Override
    public void onPartUnload() {
        stopService();
        super.onPartUnload();
    }

    private void startService() {
        DroneStationNetwork.register(this);
        if (serviceSubscription == null) serviceSubscription = subscribeServerTick(this::serviceTick);
    }

    private void stopService() {
        if (!isRemote()) DroneStationNetwork.unregister(this);
        if (serviceSubscription != null) {
            serviceSubscription.unsubscribe();
            serviceSubscription = null;
        }
        for (DroneStationConnection connection : List.copyOf(connections.values())) {
            DroneMaintenanceInterfacePartMachine maintenanceInterface = connection.maintenanceInterface(getLevel());
            if (maintenanceInterface != null) maintenanceInterface.disconnectFrom(this);
        }
        connections.clear();
        setOperationalState(null, false);
        setConnectedMachineCount(0);
    }

    private void serviceTick() {
        if (!isFormed() || energyContainer == null || !isWorkingEnabled()) {
            setOperationalState(null, false);
            return;
        }
        DroneTier selected = selectHighestAvailableTier();
        if (selected == null || energyContainer.getHighestInputVoltage() < selected.voltage() ||
                energyContainer.getEnergyStored() < selected.voltage() ||
                energyContainer.removeEnergy(selected.voltage()) != selected.voltage()) {
            setOperationalState(selected, false);
        } else {
            setOperationalState(selected, true);
        }
        if (getOffsetTimer() % 20 == 0) pruneConnections();
    }

    private @Nullable DroneTier selectHighestAvailableTier() {
        return DroneStationServiceLogic.selectHighest(
                ordinal -> getRemainingUses(DroneTier.values()[ordinal]) > 0,
                ordinal -> hasFreshDrone(DroneTier.values()[ordinal]));
    }

    public boolean canServe(BlockPos target) {
        DroneTier tier = getActiveTier();
        if (!online || tier == null || !isFormed() || isRemoved()) return false;
        long range = tier.range();
        return DroneStationSpace.squaredDistance(getLevel(), getBlockPos(), target) <= range * range;
    }

    public boolean consumeServiceUse(BlockPos interfacePos) {
        DroneTier tier = getActiveTier();
        DroneStationConnection connection = connections.get(interfacePos.asLong());
        if (!canServe(interfacePos) || tier == null || connection == null || !connection.isValid(getLevel())) {
            return false;
        }
        int remaining = getRemainingUses(tier);
        DroneStationServiceLogic.UseResult result = DroneStationServiceLogic.consumeUse(
                remaining, tier.usesPerDrone(), () -> extractFreshDrone(tier));
        if (!result.successful()) return false;
        setRemainingUses(tier, result.remainingUses());
        if (result.remainingUses() == 0 && !hasFreshDrone(tier)) setOperationalState(null, false);
        return true;
    }

    public DroneStationConnection connect(DroneMaintenanceInterfacePartMachine maintenanceInterface) {
        DroneStationConnection connection = new DroneStationConnection(maintenanceInterface, this);
        connections.put(maintenanceInterface.getBlockPos().asLong(), connection);
        setConnectedMachineCount(connections.size());
        return connection;
    }

    public void disconnect(BlockPos interfacePos) {
        if (connections.remove(interfacePos.asLong()) != null) setConnectedMachineCount(connections.size());
    }

    private void pruneConnections() {
        connections.values().removeIf(connection -> !connection.isValid(getLevel()));
        setConnectedMachineCount(connections.size());
    }

    private boolean hasFreshDrone(DroneTier tier) {
        return countFreshDrones(tier) > 0;
    }

    private int countFreshDrones(DroneTier tier) {
        return countDrones(getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP), droneItem(tier));
    }

    static int countDrones(List<IRecipeHandler<?>> handlers, Item drone) {
        int count = 0;
        for (var handler : handlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && stack.is(drone)) {
                    count = (int) Math.min(Integer.MAX_VALUE, (long) count + stack.getCount());
                }
            }
        }
        return count;
    }

    private boolean extractFreshDrone(DroneTier tier) {
        return consumeDrone(getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP), new ItemStack(droneItem(tier)));
    }

    static boolean consumeDrone(List<IRecipeHandler<?>> handlers, ItemStack drone) {
        var required = List.of(RecipeHelper.makeSizedIngredient(drone.copyWithCount(1)));
        for (var handler : handlers) {
            if (handler.getCapability() != ItemRecipeCapability.CAP) continue;
            if (!handler.handleRecipe(IO.IN, null, required, true).isEmpty()) continue;
            return handler.handleRecipe(IO.IN, null, required, false).isEmpty();
        }
        return false;
    }

    private static Item droneItem(DroneTier tier) {
        return switch (tier) {
            case RUSTY -> CosmicItems.RUSTY_DRONE.get();
            case ROBUST -> CosmicItems.ROBUST_DRONE.get();
            case INDUSTRIAL -> CosmicItems.INDUSTRIAL_DRONE.get();
            case SANGUINE -> CosmicItems.SANGUINE_DRONE.get();
            case PLASMATIC -> CosmicItems.PLASMATIC_DRONE.get();
        };
    }

    public int getRemainingUses(DroneTier tier) {
        return switch (tier) {
            case RUSTY -> rustyUses;
            case ROBUST -> robustUses;
            case INDUSTRIAL -> industrialUses;
            case SANGUINE -> sanguineUses;
            case PLASMATIC -> plasmaticUses;
        };
    }

    private void setRemainingUses(DroneTier tier, int uses) {
        switch (tier) {
            case RUSTY -> rustyUses = uses;
            case ROBUST -> robustUses = uses;
            case INDUSTRIAL -> industrialUses = uses;
            case SANGUINE -> sanguineUses = uses;
            case PLASMATIC -> plasmaticUses = uses;
        }
        setOperationalState(getActiveTier(), online);
    }

    public @Nullable DroneTier getActiveTier() {
        return activeTier < 0 || activeTier >= DroneTier.values().length ? null : DroneTier.values()[activeTier];
    }

    public boolean isOnline() {
        return online;
    }

    public int getActiveRemainingUses() {
        return activeRemainingUses;
    }

    public int getConnectedMachineCount() {
        return connectedMachineCount;
    }

    private void setOperationalState(@Nullable DroneTier tier, boolean isOnline) {
        int newTier = tier == null ? -1 : tier.ordinal();
        boolean serviceChanged = activeTier != newTier || online != isOnline;
        int newRemaining = tier == null ? 0 : (int) Math.min(Integer.MAX_VALUE,
                (long) getRemainingUses(tier) + (long) countFreshDrones(tier) * tier.usesPerDrone());
        if (activeTier != newTier) {
            activeTier = newTier;
            getSyncDataHolder().markClientSyncFieldDirty("activeTier");
        }
        if (activeRemainingUses != newRemaining) {
            activeRemainingUses = newRemaining;
            getSyncDataHolder().markClientSyncFieldDirty("activeRemainingUses");
        }
        if (online != isOnline) {
            online = isOnline;
            getSyncDataHolder().markClientSyncFieldDirty("online");
        }
        if (serviceChanged) refreshConnectedCleanrooms();
    }

    private void refreshConnectedCleanrooms() {
        for (DroneStationConnection connection : List.copyOf(connections.values())) {
            DroneMaintenanceInterfacePartMachine maintenanceInterface = connection.maintenanceInterface(getLevel());
            if (maintenanceInterface != null) maintenanceInterface.refreshCleanroomService(this);
        }
    }

    private void setConnectedMachineCount(int count) {
        if (connectedMachineCount == count) return;
        connectedMachineCount = count;
        getSyncDataHolder().markClientSyncFieldDirty("connectedMachineCount");
    }

    public boolean wakeMachine(BlockPos interfacePos) {
        if (!online) return false;
        IRecipeLogicMachine machine = connectedController(interfacePos);
        if (machine == null) return false;
        machine.setSuspendAfterFinish(false);
        machine.setWorkingEnabled(true);
        return true;
    }

    public boolean sleepMachine(BlockPos interfacePos) {
        if (!online) return false;
        IRecipeLogicMachine machine = connectedController(interfacePos);
        if (machine == null) return false;
        if (DroneStationServiceLogic.sleepAction(machine.isActive()) ==
                DroneStationServiceLogic.ControlAction.SUSPEND_AFTER_FINISH) {
            machine.setSuspendAfterFinish(true);
        } else {
            machine.setWorkingEnabled(false);
        }
        return true;
    }

    public void wakeAllMachines() {
        if (!online) return;
        for (DroneStationConnection connection : List.copyOf(connections.values()))
            wakeMachine(connection.interfacePos());
    }

    public void sleepAllMachines() {
        if (!online) return;
        for (DroneStationConnection connection : List.copyOf(connections.values()))
            sleepMachine(connection.interfacePos());
    }

    private @Nullable IRecipeLogicMachine connectedController(BlockPos interfacePos) {
        DroneStationConnection connection = connections.get(interfacePos.asLong());
        if (connection == null || !connection.isValid(getLevel())) return null;
        DroneMaintenanceInterfacePartMachine maintenanceInterface = connection.maintenanceInterface(getLevel());
        if (maintenanceInterface == null) return null;
        MultiblockControllerMachine controller = maintenanceInterface.getController();
        return controller instanceof IRecipeLogicMachine recipeMachine ? recipeMachine : null;
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!isRemote()) sleepAllMachines();
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        widgets.add(Text.dynamic(() -> Component.translatable(
                online ? "cosmiccore.multiblock.drone_station.online" : "cosmiccore.multiblock.drone_station.offline")
                .withStyle(ChatFormatting.WHITE)).asWidget().color(0xFFFFFF));
        widgets.add(Text.dynamic(this::tierStatusLine).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable("cosmiccore.multiblock.drone_station.connections",
                connectedMachineCount).withStyle(ChatFormatting.WHITE)).asWidget().color(0xFFFFFF));
        return widgets;
    }

    private Component tierStatusLine() {
        DroneTier tier = getActiveTier();
        if (tier == null) return Component.translatable("cosmiccore.multiblock.drone_station.no_drones")
                .withStyle(ChatFormatting.WHITE);
        return Component.translatable("cosmiccore.multiblock.drone_station.tier_status",
                Component.translatable(tier.translationKey()), FormattingUtil.formatNumbers(tier.range()),
                FormattingUtil.formatNumbers(tier.voltage()), activeRemainingUses).withStyle(ChatFormatting.WHITE);
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.panelBuilder(this)
                .attachInventory(false)
                .addDefaultConfigurators(false)
                .rightConfigurators(flow -> flow.child(GTMuiWidgets.createPowerButton(this)));
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        ListWidget<IWidget, ?> list = new ListWidget<>()
                .width(294)
                .height(174)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .left(3)
                .top(3);
        list.children(getWidgetsForDisplay(syncManager));
        list.child(globalControlRow(syncManager));
        list.child(Text.lang("cosmiccore.multiblock.drone_station.machine_list").style(ChatFormatting.WHITE)
                .asWidget().color(0xFFFFFF));
        int[] pageSource = { 0 };
        IntSyncValue page = new IntSyncValue(() -> pageSource[0],
                value -> pageSource[0] = Math.max(0, Math.min(value, maximumUiPage()))).allowC2S();
        syncManager.syncValue("droneConnectionPage", page);
        list.child(pageControlRow(page));
        for (int index = 0; index < UI_PAGE_SIZE; index++) list.child(connectionRow(index, page, syncManager));
        mainWidget.size(300, 180).background(GuiTextures.DISPLAY).child(list);
    }

    private IWidget globalControlRow(PanelSyncManager syncManager) {
        syncManager.registerSyncedAction("droneWakeAll", ignored -> wakeAllMachines());
        syncManager.registerSyncedAction("droneSleepAll", ignored -> sleepAllMachines());
        ButtonWidget<?> wake = actionButton("cosmiccore.multiblock.drone_station.wake_all",
                () -> syncManager.callSyncedAction("droneWakeAll"));
        ButtonWidget<?> sleep = actionButton("cosmiccore.multiblock.drone_station.sleep_all",
                () -> syncManager.callSyncedAction("droneSleepAll"));
        return Flow.row().width(286).height(18).childPadding(4).child(wake).child(sleep);
    }

    private IWidget pageControlRow(IntSyncValue page) {
        ButtonWidget<?> previous = actionButton("cosmiccore.multiblock.drone_station.previous",
                () -> page.setValue(Math.max(0, page.getIntValue() - 1))).width(42);
        ButtonWidget<?> next = actionButton("cosmiccore.multiblock.drone_station.next",
                () -> page.setValue(Math.min(maximumUiPage(), page.getIntValue() + 1))).width(42);
        var label = Text.dynamic(() -> Component.translatable("cosmiccore.multiblock.drone_station.page",
                page.getIntValue() + 1, maximumUiPage() + 1).withStyle(ChatFormatting.WHITE))
                .asWidget().color(0xFFFFFF).width(190).height(18);
        return Flow.row().width(286).height(18).childPadding(4).child(previous).child(label).child(next);
    }

    private IWidget connectionRow(int rowIndex, IntSyncValue page, PanelSyncManager syncManager) {
        StringSyncValue name = new StringSyncValue(() -> connectionName(connectionAt(rowIndex, page)));
        StringSyncValue machineId = new StringSyncValue(() -> connectionMachineId(connectionAt(rowIndex, page)));
        LongSyncValue position = new LongSyncValue(() -> connectionPosition(connectionAt(rowIndex, page)));
        StringSyncValue dimension = new StringSyncValue(() -> connectionDimension(connectionAt(rowIndex, page)));
        LongSyncValue target = new LongSyncValue(() -> connectionTarget(connectionAt(rowIndex, page)));
        IntSyncValue status = new IntSyncValue(() -> connectionStatus(connectionAt(rowIndex, page)));
        int index = rowIndex;
        syncManager.syncValue("droneConnectionName", index, name);
        syncManager.syncValue("droneConnectionMachineId", index, machineId);
        syncManager.syncValue("droneConnectionPosition", index, position);
        syncManager.syncValue("droneConnectionDimension", index, dimension);
        syncManager.syncValue("droneConnectionTarget", index, target);
        syncManager.syncValue("droneConnectionStatus", index, status);
        syncManager.registerSyncedAction("droneWake" + index,
                buffer -> wakeMachine(BlockPos.of(buffer.readLong())));
        syncManager.registerSyncedAction("droneSleep" + index,
                buffer -> sleepMachine(BlockPos.of(buffer.readLong())));

        var icon = new DynamicDrawable(() -> connectionIcon(machineId.getStringValue())).asWidget().size(14);
        var text = Text.dynamic(() -> connectionLine(name.getStringValue(), status.getIntValue()))
                .asWidget().color(0xFFFFFF).width(170).height(16).scale(0.75f);
        var locate = new ButtonWidget<>()
                .width(188)
                .height(16)
                .background(IDrawable.NONE)
                .child(Flow.row().width(188).height(16).childPadding(2).child(icon).child(text))
                .tooltipBuilder(tooltip -> {
                    tooltip.addLine(Text.dynamic(() -> connectionLocation(position.getLongValue())));
                    tooltip.addLine(Text.lang("cosmiccore.multiblock.drone_station.locate")
                            .style(ChatFormatting.WHITE));
                })
                .onMousePressed((context, button) -> {
                    ResourceLocation dimensionId = ResourceLocation.tryParse(dimension.getStringValue());
                    if (button != 0 || name.getStringValue().isEmpty() || dimensionId == null) return false;
                    ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
                    if (PowerTowerMELocator.locateDroneMachine(BlockPos.of(position.getLongValue()), dimensionKey)) {
                        ((ModularGuiContext) context).getScreen().getMainPanel().closeIfOpen();
                    }
                    return true;
                });
        ButtonWidget<?> wake = actionButton("cosmiccore.multiblock.drone_station.wake",
                () -> syncManager.callSyncedAction("droneWake" + index,
                        buffer -> buffer.writeLong(target.getLongValue())))
                .width(42);
        ButtonWidget<?> sleep = actionButton("cosmiccore.multiblock.drone_station.sleep",
                () -> syncManager.callSyncedAction("droneSleep" + index,
                        buffer -> buffer.writeLong(target.getLongValue())))
                .width(42);
        return Flow.row().width(286).height(18).childPadding(4).child(locate).child(wake).child(sleep)
                .setEnabledIf(row -> !name.getStringValue().isEmpty());
    }

    private ButtonWidget<?> actionButton(String translationKey, Runnable action) {
        return new ButtonWidget<>()
                .width(88)
                .height(16)
                .background(GTGuiTextures.BUTTON)
                .overlay(new DynamicDrawable(() -> Text.lang(translationKey).style(ChatFormatting.WHITE)))
                .onMousePressed((context, button) -> {
                    action.run();
                    return true;
                });
    }

    private @Nullable DroneStationConnection connectionAt(int rowIndex, IntSyncValue page) {
        int index = page.getIntValue() * UI_PAGE_SIZE + rowIndex;
        if (index < 0 || index >= connections.size()) return null;
        return connections.values().stream().skip(index).findFirst().orElse(null);
    }

    private int maximumUiPage() {
        return Math.max(0, (connectedMachineCount - 1) / UI_PAGE_SIZE);
    }

    private String connectionName(@Nullable DroneStationConnection connection) {
        IRecipeLogicMachine machine = connection == null ? null : connectedController(connection.interfacePos());
        if (!(machine instanceof MultiblockControllerMachine controller)) return "";
        return controller.getDefinition().getDescriptionId();
    }

    private String connectionMachineId(@Nullable DroneStationConnection connection) {
        IRecipeLogicMachine machine = connection == null ? null : connectedController(connection.interfacePos());
        if (!(machine instanceof MultiblockControllerMachine controller)) return "";
        return controller.getDefinition().getId().toString();
    }

    private long connectionPosition(@Nullable DroneStationConnection connection) {
        IRecipeLogicMachine machine = connection == null ? null : connectedController(connection.interfacePos());
        if (!(machine instanceof MultiblockControllerMachine controller)) return 0;
        return DroneStationSpace.worldPosition(controller.getLevel(), controller.getBlockPos()).asLong();
    }

    private String connectionDimension(@Nullable DroneStationConnection connection) {
        IRecipeLogicMachine machine = connection == null ? null : connectedController(connection.interfacePos());
        if (!(machine instanceof MultiblockControllerMachine controller) || controller.getLevel() == null) return "";
        return controller.getLevel().dimension().location().toString();
    }

    private long connectionTarget(@Nullable DroneStationConnection connection) {
        return connection == null ? 0 : connection.interfacePos().asLong();
    }

    private int connectionStatus(@Nullable DroneStationConnection connection) {
        IRecipeLogicMachine machine = connection == null ? null : connectedController(connection.interfacePos());
        if (machine == null) return 0;
        if (machine.isSuspendAfterFinish() && machine.isActive()) return 3;
        if (!machine.isWorkingEnabled()) return 4;
        return machine.isActive() ? 2 : 1;
    }

    private Component connectionLine(String descriptionId, int status) {
        if (descriptionId.isEmpty()) return Component.empty();
        return Component.translatable("cosmiccore.multiblock.drone_station.machine_row",
                Component.translatable(descriptionId),
                Component.translatable("cosmiccore.multiblock.drone_station.status." + status))
                .withStyle(ChatFormatting.WHITE);
    }

    private Component connectionLocation(long packedPos) {
        BlockPos pos = BlockPos.of(packedPos);
        return Component.translatable("cosmiccore.multiblock.drone_station.machine_location",
                pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.WHITE);
    }

    private IDrawable connectionIcon(String machineId) {
        ResourceLocation id = ResourceLocation.tryParse(machineId);
        if (id == null) return IDrawable.EMPTY;
        return BuiltInRegistries.BLOCK.getOptional(id)
                .map(block -> new ItemStack(block.asItem()))
                .filter(stack -> !stack.isEmpty())
                .<IDrawable>map(ItemDrawable::new)
                .orElse(IDrawable.EMPTY);
    }
}
