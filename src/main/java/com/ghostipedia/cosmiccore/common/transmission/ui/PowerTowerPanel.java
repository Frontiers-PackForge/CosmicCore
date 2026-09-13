package com.ghostipedia.cosmiccore.common.transmission.ui;

import com.ghostipedia.cosmiccore.client.transmission.PowerTowerMELocator;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.machine.transmission.PowerTowerMachine;
import com.ghostipedia.cosmiccore.common.transmission.energy.LoadedPowerTowerTerminal;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerGraph;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerRole;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;
import com.ghostipedia.cosmiccore.common.transmission.upgrade.PowerTowerUpgradeService;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.value.sync.SyncHandler;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.PageButton;
import brachy.modularui.widgets.PagedWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class PowerTowerPanel {

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 230;
    private static final int DISPLAY_WIDTH = PANEL_WIDTH - 12;
    private static final int DISPLAY_HEIGHT = 124;
    private static final int ROWS = 5;
    private static final int HEADER_TEXT_COLOR = 0xFFBFBFBF;
    private static final int BODY_TEXT_COLOR = 0xFFD8D8D8;
    private static final int MUTED_TEXT_COLOR = 0xFF9A9A9A;

    private PowerTowerPanel() {}

    public static ModularPanel<?> build(PowerTowerMachine tower, PosGuiData data, PanelSyncManager sync) {
        var panel = ModularPanel.defaultPanel("power_tower", PANEL_WIDTH, PANEL_HEIGHT)
                .background(GTGuiTextures.BACKGROUND);
        panel.child(GTMuiWidgets.createTitleBar(tower.getDefinition(), PANEL_WIDTH));
        var view = new View(tower);
        var control = new Control(tower, view);
        sync.syncValue("actions", control.allowC2S());

        var componentId = string(sync, "component", view::componentId);
        var version = longValue(sync, "version", view::version);
        var wireTier = integer(sync, "wire_tier", view::wireTier);
        var activeTarget = integer(sync, "active_target", view::activeTarget);
        var targetTier = integer(sync, "target_tier", view::targetTier);
        var towerCount = integer(sync, "tower_count", view::towerCount);
        var connectionCount = integer(sync, "connection_count", view::connectionCount);
        var upgradedCount = integer(sync, "upgraded_count", view::upgradedCount);
        var remainingCount = integer(sync, "remaining_count", view::remainingCount);
        var upgradeProgress = doubleValue(sync, "upgrade_progress", view::upgradeProgress);
        var localRole = string(sync, "local_role", view::localRole);
        var localTier = integer(sync, "local_tier", view::localTier);
        var inputCapacity = longValue(sync, "input_capacity", view::inputCapacity);
        var outputCapacity = longValue(sync, "output_capacity", view::outputCapacity);
        var page = integer(sync, "page", view::page);
        var remoteCount = integer(sync, "remote_count", view::remoteCount);

        List<GenericSyncValue<ByteBuf, BlockPos>> positions = new ArrayList<>(ROWS);
        List<IntSyncValue> tiers = new ArrayList<>(ROWS);
        List<StringSyncValue> states = new ArrayList<>(ROWS);
        for (int row = 0; row < ROWS; row++) {
            final int index = row;
            positions.add(position(sync, "row_position_" + row, () -> view.position(index)));
            tiers.add(integer(sync, "row_tier_" + row, () -> view.tier(index)));
            states.add(string(sync, "row_state_" + row, () -> view.state(index)));
        }

        var tabController = new PagedWidget.Controller();
        var pages = new PagedWidget<>()
                .controller(tabController)
                .sizeRel(1)
                .addPage(overviewPage(wireTier, towerCount, connectionCount, localRole, localTier, inputCapacity,
                        outputCapacity))
                .addPage(wiresPage(control, componentId, version, wireTier, activeTarget, targetTier, connectionCount,
                        upgradedCount, remainingCount, upgradeProgress))
                .addPage(towersPage(tower, control, page, remoteCount, positions, tiers, states));

        panel.child(displayPanel(pages).pos(6, 17).size(DISPLAY_WIDTH, DISPLAY_HEIGHT));
        panel.child(tabStrip(tabController));
        panel.child(SlotGroupWidget.playerInventory(7, true,
                (index, slot) -> slot.background(GTGuiTextures.SLOT)));
        return panel;
    }

    private static IWidget overviewPage(IntSyncValue wireTier, IntSyncValue towerCount, IntSyncValue connectionCount,
                                        StringSyncValue localRole, IntSyncValue localTier,
                                        LongSyncValue inputCapacity, LongSyncValue outputCapacity) {
        return Flow.row()
                .sizeRel(1)
                .padding(6)
                .childPadding(6)
                .child(Flow.column()
                        .width(93)
                        .heightRel(1)
                        .childPadding(4)
                        .child(section("network"))
                        .child(body(() -> tr("wire_tier", tierName(wireTier.getIntValue()))))
                        .child(body(() -> tr("tower_count", format(towerCount.getIntValue()))))
                        .child(body(() -> tr("connection_count", format(connectionCount.getIntValue())))))
                .child(new ParentWidget<>()
                        .width(1)
                        .heightRel(1)
                        .background(new Rectangle().color(0xFF2A2A2A)))
                .child(Flow.column()
                        .widthRel(1)
                        .heightRel(1)
                        .childPadding(3)
                        .child(section("local_tower"))
                        .child(body(() -> tr("role", tr(localRole.getStringValue()))))
                        .child(body(() -> tr("hatch_tier", tierName(localTier.getIntValue()))))
                        .child(capacity("input_capacity", inputCapacity))
                        .child(capacity("output_capacity", outputCapacity)));
    }

    private static IWidget wiresPage(Control control, StringSyncValue componentId, LongSyncValue version,
                                     IntSyncValue wireTier, IntSyncValue activeTarget, IntSyncValue targetTier,
                                     IntSyncValue connectionCount, IntSyncValue upgradedCount,
                                     IntSyncValue remainingCount, DoubleSyncValue upgradeProgress) {
        var page = new ParentWidget<>().sizeRel(1);
        page.child(section("wire_upgrade").pos(6, 4).size(190, 10));
        page.child(GTGuiTextures.INFO.asWidget()
                .pos(207, 4)
                .size(9)
                .tooltipBuilder(tooltip -> tooltip
                        .addLine(Text.lang("cosmiccore.power_tower.ui.upgrade_help_cost"))
                        .addLine(Text.lang("cosmiccore.power_tower.ui.upgrade_help_refund"))
                        .addLine(Text.lang("cosmiccore.power_tower.ui.upgrade_help_inventory"))
                        .addLine(Text.lang("cosmiccore.power_tower.ui.upgrade_help_hatches"))));
        page.child(Text.lang("cosmiccore.power_tower.ui.target").asWidget()
                .pos(6, 21)
                .size(59, 10)
                .color(BODY_TEXT_COLOR)
                .scale(0.8f));
        page.child(iconButton(68, 18, GuiTextures.MOVE_LEFT,
                () -> control.requestTarget(targetTier.getIntValue() - 1, componentId.getStringValue(),
                        version.getLongValue()))
                .setEnabledIf(widget -> targetTier.getIntValue() >= 0 &&
                        targetTier.getIntValue() - 1 > wireTier.getIntValue()));
        page.child(new TextWidget<>(Text.dynamic(() -> Component.literal(tierName(targetTier.getIntValue()))))
                .pos(89, 21)
                .size(40, 10)
                .color(BODY_TEXT_COLOR)
                .textAlign(Alignment.Center));
        page.child(iconButton(132, 18, GuiTextures.MOVE_RIGHT,
                () -> control.requestTarget(targetTier.getIntValue() + 1, componentId.getStringValue(),
                        version.getLongValue()))
                .setEnabledIf(widget -> targetTier.getIntValue() >= 0 &&
                        targetTier.getIntValue() < maximumCoilTier()));
        page.child(new TextWidget<>(Text.dynamic(() -> tr("current_wire", tierName(wireTier.getIntValue()))))
                .pos(154, 21)
                .size(64, 10)
                .color(MUTED_TEXT_COLOR)
                .scale(0.75f)
                .textAlign(Alignment.CenterRight));
        page.child(progressBar(upgradeProgress).pos(6, 42).size(212, 9));
        page.child(body(() -> tr("upgrade_progress", format(upgradedCount.getIntValue()),
                format(connectionCount.getIntValue()))).pos(6, 56).size(212, 9));
        page.child(body(() -> targetTier.getIntValue() < 0 ? tr("no_upgrade") :
                tr("coils_remaining", format(remainingCount.getIntValue()), tierName(targetTier.getIntValue())))
                .pos(6, 69).size(212, 9));
        page.child(new TextWidget<>(Text.dynamic(() -> targetTier.getIntValue() < 0 ? Component.empty() :
                tr("hatch_limit_short", tierName(wireTier.getIntValue()))))
                .pos(6, 82)
                .size(212, 10)
                .color(0xFFFFAA00)
                .scale(0.75f));
        page.child(new ButtonWidget<>()
                .pos(52, 99)
                .size(120, 18)
                .overlay(Text.dynamic(() -> tr(activeTarget.getIntValue() < 0 ? "set_target" : "upgrade_wires")))
                .setEnabledIf(widget -> targetTier.getIntValue() >= 0 &&
                        (activeTarget.getIntValue() < 0 || remainingCount.getIntValue() > 0))
                .onMousePressed((context, button) -> {
                    if (button != 0) return false;
                    if (activeTarget.getIntValue() < 0) {
                        control.requestTarget(targetTier.getIntValue(), componentId.getStringValue(),
                                version.getLongValue());
                    } else {
                        control.requestUpgrade(activeTarget.getIntValue(), componentId.getStringValue(),
                                version.getLongValue());
                    }
                    return true;
                }));
        return page;
    }

    private static IWidget towersPage(PowerTowerMachine tower, Control control, IntSyncValue page,
                                      IntSyncValue remoteCount,
                                      List<GenericSyncValue<ByteBuf, BlockPos>> positions,
                                      List<IntSyncValue> tiers, List<StringSyncValue> states) {
        var pageWidget = new ParentWidget<>().sizeRel(1);
        pageWidget.child(section("connected_towers").pos(6, 4).size(212, 10));
        pageWidget.child(tableHeader("coordinates").pos(7, 16).size(101, 8));
        pageWidget.child(tableHeader("tier").pos(110, 16).size(24, 8).textAlign(Alignment.Center));
        pageWidget.child(tableHeader("status").pos(137, 16).size(57, 8));
        for (int row = 0; row < ROWS; row++) {
            var position = positions.get(row);
            var tier = tiers.get(row);
            var state = states.get(row);
            var rowWidget = new ParentWidget<>()
                    .pos(4, 25 + row * 15)
                    .size(216, 14)
                    .background(new Rectangle().color(row % 2 == 0 ? 0xFF0B0B0B : 0xFF141414));
            rowWidget.child(new TextWidget<>(Text.dynamic(() -> position.getValue() == null ? Component.empty() :
                    Component.literal(compactPosition(position.getValue()))))
                    .pos(3, 3)
                    .size(103, 9)
                    .color(BODY_TEXT_COLOR)
                    .scale(0.75f)
                    .tooltipBuilder(tooltip -> tooltip.addLine(Text.dynamic(() -> position.getValue() == null ?
                            Component.empty() : Component.literal(position.getValue().toShortString())))));
            rowWidget.child(new TextWidget<>(Text.dynamic(() -> position.getValue() == null ? Component.empty() :
                    Component.literal(tierName(tier.getIntValue()))))
                    .pos(108, 3)
                    .size(24, 9)
                    .color(BODY_TEXT_COLOR)
                    .scale(0.75f)
                    .textAlign(Alignment.Center));
            rowWidget.child(new TextWidget<>(Text.dynamic(() -> position.getValue() == null ? Component.empty() :
                    tr(state.getStringValue()).withStyle(statusColor(state.getStringValue()))))
                    .pos(135, 3)
                    .size(59, 9)
                    .scale(0.75f));
            rowWidget.child(locateButton(199, 0, tower, position::getValue));
            pageWidget.child(rowWidget);
        }
        pageWidget.child(new TextWidget<>(Text.lang("cosmiccore.power_tower.ui.no_connected_towers"))
                .pos(6, 52)
                .size(212, 10)
                .color(MUTED_TEXT_COLOR)
                .scale(0.8f)
                .textAlign(Alignment.Center)
                .setEnabledIf(widget -> remoteCount.getIntValue() == 0));
        pageWidget.child(iconButton(6, 101, GuiTextures.MOVE_LEFT, () -> control.requestPage(-1))
                .setEnabledIf(widget -> page.getIntValue() > 0));
        pageWidget.child(new TextWidget<>(Text.dynamic(() -> tr("page", page.getIntValue() + 1,
                pageCount(remoteCount.getIntValue()))))
                .pos(27, 104)
                .size(170, 9)
                .color(BODY_TEXT_COLOR)
                .scale(0.8f)
                .textAlign(Alignment.Center));
        pageWidget.child(iconButton(202, 101, GuiTextures.MOVE_RIGHT, () -> control.requestPage(1))
                .setEnabledIf(widget -> page.getIntValue() + 1 < pageCount(remoteCount.getIntValue())));
        return pageWidget;
    }

    private static ParentWidget<?> displayPanel(IWidget content) {
        return new ParentWidget<>()
                .background(new Rectangle().color(0xFF555555))
                .child(new ParentWidget<>()
                        .pos(2, 2)
                        .widthRelOffset(1, -4)
                        .heightRelOffset(1, -4)
                        .background(new Rectangle().color(0xFF000000))
                        .child(content));
    }

    private static Flow tabStrip(PagedWidget.Controller controller) {
        return Flow.column()
                .coverChildren()
                .leftRelOffset(1, -5)
                .top(20)
                .child(new PageButton(0, controller)
                        .tab(GuiTextures.TAB_RIGHT, -1)
                        .overlay(GTGuiTextures.INFO.asIcon().size(16))
                        .tooltipBuilder(
                                tooltip -> tooltip.addLine(Text.lang("cosmiccore.power_tower.ui.tab.overview"))))
                .child(new PageButton(1, controller)
                        .tab(GuiTextures.TAB_RIGHT, 0)
                        .overlay(GTGuiTextures.WIREMILL_OVERLAY.asIcon().size(16))
                        .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang("cosmiccore.power_tower.ui.tab.wires"))))
                .child(new PageButton(2, controller)
                        .tab(GuiTextures.TAB_RIGHT, 1)
                        .overlay(GuiTextures.SERVER.asIcon().size(16))
                        .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang("cosmiccore.power_tower.ui.tab.towers"))));
    }

    private static TextWidget<?> section(String key) {
        return new TextWidget<>(Text.lang("cosmiccore.power_tower.ui." + key))
                .color(HEADER_TEXT_COLOR)
                .height(10)
                .widthRel(1);
    }

    private static TextWidget<?> tableHeader(String key) {
        return new TextWidget<>(Text.lang("cosmiccore.power_tower.ui." + key))
                .color(MUTED_TEXT_COLOR)
                .scale(0.7f);
    }

    private static TextWidget<?> body(Supplier<Component> value) {
        return new TextWidget<>(Text.dynamic(value))
                .color(BODY_TEXT_COLOR)
                .scale(0.8f)
                .height(9)
                .widthRel(1);
    }

    private static TextWidget<?> capacity(String key, LongSyncValue value) {
        return body(() -> tr(key, formatReadable(value.getLongValue())))
                .tooltipBuilder(tooltip -> tooltip.addLine(Text.dynamic(() -> tr(key,
                        format(value.getLongValue())))));
    }

    private static ParentWidget<?> progressBar(DoubleSyncValue progress) {
        return new ParentWidget<>()
                .background(new Rectangle().color(0xFF555555))
                .child(new ProgressWidget()
                        .value(progress)
                        .texture(
                                new Rectangle().color(0xFF141414),
                                new Rectangle().horizontalGradient(0xFFB36A16, 0xFFFFB52E),
                                ProgressDrawable.Direction.RIGHT)
                        .pos(1, 1)
                        .heightRelOffset(1, -2)
                        .widthRelOffset(1, -2));
    }

    private static ButtonWidget<?> iconButton(int x, int y, brachy.modularui.api.drawable.IDrawable icon,
                                              Runnable action) {
        return new ButtonWidget<>()
                .pos(x, y)
                .size(18)
                .overlay(icon.asIcon().size(8))
                .onMousePressed((context, button) -> {
                    if (button != 0) return false;
                    action.run();
                    return true;
                });
    }

    private static ButtonWidget<?> locateButton(int x, int y, PowerTowerMachine tower,
                                                Supplier<BlockPos> position) {
        return new ButtonWidget<>()
                .pos(x, y)
                .size(14)
                .overlay(GuiTextures.SEARCH.asIcon().size(10))
                .setEnabledIf(widget -> position.get() != null)
                .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang("cosmiccore.power_tower.ui.locate")))
                .onMousePressed((context, button) -> {
                    if (button != 0 || position.get() == null || tower.getLevel() == null) return false;
                    if (PowerTowerMELocator.locateTower(position.get(), tower.getLevel().dimension()))
                        ((ModularGuiContext) context).getScreen().getMainPanel().closeIfOpen();
                    return true;
                });
    }

    private static GenericSyncValue<ByteBuf, BlockPos> position(PanelSyncManager sync, String id,
                                                                Supplier<BlockPos> supplier) {
        var value = GenericSyncValue.<ByteBuf, BlockPos>builder(BlockPos.class)
                .getter(supplier)
                .adapter(ByteBufAdapters.BLOCKPOS)
                .copyImmutable()
                .nullable()
                .build();
        sync.syncValue(id, value);
        return value;
    }

    private static StringSyncValue string(PanelSyncManager sync, String id, Supplier<String> supplier) {
        var value = new StringSyncValue(supplier, ignored -> {});
        sync.syncValue(id, value);
        return value;
    }

    private static IntSyncValue integer(PanelSyncManager sync, String id, IntSupplier supplier) {
        var value = new IntSyncValue(supplier, ignored -> {});
        sync.syncValue(id, value);
        return value;
    }

    private static LongSyncValue longValue(PanelSyncManager sync, String id, LongSupplier supplier) {
        var value = new LongSyncValue(supplier, ignored -> {});
        sync.syncValue(id, value);
        return value;
    }

    private static DoubleSyncValue doubleValue(PanelSyncManager sync, String id, DoubleSupplier supplier) {
        var value = new DoubleSyncValue(supplier);
        sync.syncValue(id, value);
        return value;
    }

    private static String tierName(int tier) {
        return tier < 0 || tier >= GTValues.VNF.length ? "-" : GTValues.VNF[tier];
    }

    private static String format(long value) {
        return FormattingUtil.formatNumbers(value);
    }

    private static String formatReadable(long value) {
        return FormattingUtil.formatNumberReadable(value);
    }

    private static String compactPosition(BlockPos position) {
        return formatReadable(position.getX()) + "," + formatReadable(position.getY()) + "," +
                formatReadable(position.getZ());
    }

    private static int pageCount(int entries) {
        return Math.max(1, (entries + ROWS - 1) / ROWS);
    }

    private static int maximumCoilTier() {
        int maximum = -1;
        for (var coil : CosmicItems.POWER_TOWER_COILS) maximum = Math.max(maximum, coil.get().getVoltageTier());
        return maximum;
    }

    private static net.minecraft.network.chat.MutableComponent tr(String key, Object... args) {
        return Component.translatable("cosmiccore.power_tower.ui." + key, args);
    }

    private static ChatFormatting statusColor(String status) {
        return switch (status) {
            case "loaded", "relay" -> ChatFormatting.GREEN;
            case "unloaded" -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
    }

    private static final class View {

        private final PowerTowerMachine tower;
        private PowerTowerGraph.ComponentSnapshot snapshot;
        private UUID nodeId;
        private List<PowerTowerNode> remoteNodes = List.of();
        private int page;

        private View(PowerTowerMachine tower) {
            this.tower = tower;
        }

        private void refresh() {
            if (!(tower.getLevel() instanceof ServerLevel level)) return;
            UUID currentNode = tower.getGraphNodeId();
            if (currentNode == null) {
                snapshot = null;
                nodeId = null;
                remoteNodes = List.of();
                page = 0;
                return;
            }
            PowerTowerGraph graph = PowerTowerSavedData.getOrCreate(level).graph();
            UUID component = graph.componentIdContainingNode(currentNode);
            long version = graph.componentVersionContainingNode(currentNode);
            if (snapshot != null && currentNode.equals(nodeId) && snapshot.id().equals(component) &&
                    snapshot.topologyVersion() == version)
                return;
            nodeId = currentNode;
            snapshot = graph.componentContainingNode(currentNode);
            if (snapshot == null) {
                remoteNodes = List.of();
                page = 0;
                return;
            }
            remoteNodes = snapshot.nodes().values().stream()
                    .filter(node -> !node.id().equals(currentNode))
                    .sorted(Comparator.comparing(PowerTowerNode::controllerPos))
                    .toList();
            page = Math.min(page, Math.max(0, (remoteNodes.size() - 1) / ROWS));
        }

        String componentId() {
            refresh();
            return snapshot == null ? "" : snapshot.id().toString();
        }

        long version() {
            refresh();
            return snapshot == null ? -1 : snapshot.topologyVersion();
        }

        int wireTier() {
            refresh();
            return snapshot == null ? -1 : snapshot.wireTier().orElse(-1);
        }

        int activeTarget() {
            refresh();
            return snapshot == null ? -1 : snapshot.upgradeTargetTier();
        }

        int targetTier() {
            int active = activeTarget();
            if (active >= 0) return active;
            int wire = wireTier();
            return wire >= 0 && wire < maximumCoilTier() ? wire + 1 : -1;
        }

        int towerCount() {
            refresh();
            return snapshot == null ? 0 : snapshot.nodes().size();
        }

        int connectionCount() {
            refresh();
            return snapshot == null ? 0 : snapshot.spans().size();
        }

        int upgradedCount() {
            refresh();
            return snapshot == null || snapshot.upgradeTargetTier() < 0 ? 0 : snapshot.upgradedConnections();
        }

        int remainingCount() {
            refresh();
            return snapshot == null ? 0 : snapshot.upgradeTargetTier() < 0 ? snapshot.spans().size() :
                    snapshot.remainingConnections();
        }

        double upgradeProgress() {
            int total = connectionCount();
            return activeTarget() < 0 || total <= 0 ? 0 : upgradedCount() / (double) total;
        }

        String localRole() {
            PowerTowerNode node = localNode();
            return node == null || !node.structureOperational() ? "broken" :
                    node.role() == PowerTowerRole.DUMMY ? "relay" : "terminal";
        }

        int localTier() {
            PowerTowerNode node = localNode();
            return node == null ? -1 : node.terminalVoltageTier();
        }

        long inputCapacity() {
            LoadedPowerTowerTerminal terminal = terminal(localNode());
            return terminal == null ? 0 : terminal.inputCapacity();
        }

        long outputCapacity() {
            LoadedPowerTowerTerminal terminal = terminal(localNode());
            return terminal == null ? 0 : terminal.outputCapacity();
        }

        int page() {
            refresh();
            return page;
        }

        int remoteCount() {
            refresh();
            return remoteNodes.size();
        }

        BlockPos position(int row) {
            PowerTowerNode node = rowNode(row);
            return node == null ? null : node.controllerPos();
        }

        int tier(int row) {
            PowerTowerNode node = rowNode(row);
            return node == null ? -1 : node.terminalVoltageTier();
        }

        String state(int row) {
            PowerTowerNode node = rowNode(row);
            return node == null ? "broken" : !node.structureOperational() ? "broken" :
                    node.role() == PowerTowerRole.DUMMY ? "relay" : terminal(node) == null ? "unloaded" : "loaded";
        }

        private PowerTowerNode localNode() {
            refresh();
            return snapshot == null || nodeId == null ? null : snapshot.nodes().get(nodeId);
        }

        private PowerTowerNode rowNode(int row) {
            refresh();
            int index = page * ROWS + row;
            return index < 0 || index >= remoteNodes.size() ? null : remoteNodes.get(index);
        }

        private LoadedPowerTowerTerminal terminal(PowerTowerNode node) {
            return node == null || !(tower.getLevel() instanceof ServerLevel level) ? null :
                    PowerTowerSavedData.getOrCreate(level).loadedTerminals().terminal(node.id());
        }
    }

    private static final class Control extends SyncHandler<Control> {

        private static final int SET_TARGET = 0;
        private static final int UPGRADE = 1;
        private static final int PAGE = 2;
        private final PowerTowerMachine tower;
        private final View view;

        private Control(PowerTowerMachine tower, View view) {
            this.tower = tower;
            this.view = view;
        }

        void requestTarget(int target, String component, long version) {
            request(SET_TARGET, target, component, version);
        }

        void requestUpgrade(int target, String component, long version) {
            request(UPGRADE, target, component, version);
        }

        void requestPage(int direction) {
            syncToServer(PAGE, buffer -> buffer.writeVarInt(Integer.signum(direction)));
        }

        private void request(int action, int target, String component, long version) {
            UUID id;
            try {
                id = UUID.fromString(component);
            } catch (IllegalArgumentException exception) {
                return;
            }
            syncToServer(action, buffer -> {
                buffer.writeVarInt(target);
                buffer.writeUUID(id);
                buffer.writeVarLong(version);
            });
        }

        @Override
        public void readOnServer(int id, RegistryFriendlyByteBuf buffer) {
            if (id == PAGE) {
                int direction = Integer.signum(buffer.readVarInt());
                view.refresh();
                view.page = Math.clamp(view.page + direction, 0,
                        Math.max(0, (view.remoteNodes.size() - 1) / ROWS));
                return;
            }
            if (!(getSyncManager().getPlayer() instanceof ServerPlayer player)) return;
            if (id != SET_TARGET && id != UPGRADE) return;
            int target = buffer.readVarInt();
            UUID component = buffer.readUUID();
            long version = buffer.readVarLong();
            PowerTowerUpgradeService.Result result = id == SET_TARGET ?
                    PowerTowerUpgradeService.setTarget(player, tower, component, version, target) :
                    PowerTowerUpgradeService.upgradeAvailable(player, tower, component, version, target);
            feedback(player, result, target);
        }

        private static void feedback(ServerPlayer player, PowerTowerUpgradeService.Result result, int target) {
            Component message = switch (result.status()) {
                case TARGET_SET -> tr("target_set", tierName(target));
                case UPGRADED -> tr("batch_complete", format(result.upgradedConnections()),
                        format(result.remainingConnections()));
                case NO_AVAILABLE_EXCHANGE -> tr("batch_stopped", tierName(target));
                case INVALID_TARGET -> tr("invalid_target");
                case STALE_OR_DENIED -> tr("stale_or_denied");
            };
            player.displayClientMessage(message, true);
        }

        @Override
        public void readOnClient(int id, RegistryFriendlyByteBuf buffer) {}
    }
}
