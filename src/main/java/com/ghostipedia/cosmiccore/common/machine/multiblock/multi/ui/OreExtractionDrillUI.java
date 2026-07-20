package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.ui;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.OreExtractionDrillLogic;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.OreExtractionDrillLogic.DrillPhase;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.OreExtractionDrillLogic.OreLedgerEntry;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.OreExtractionDrillMachine;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.DynamicLinkedSyncHandler;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;

import java.util.List;

public final class OreExtractionDrillUI {

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 230;
    private static final int DISPLAY_WIDTH = PANEL_WIDTH - 12;
    private static final int OPERATIONS_WIDTH = 92;
    private static final int CONTENT_HEIGHT = 124;
    private static final int HEADER_TEXT_COLOR = 0xFFBFBFBF;
    private static final int BODY_TEXT_COLOR = 0xFFD8D8D8;

    private OreExtractionDrillUI() {}

    public static ModularPanel<?> build(OreExtractionDrillMachine machine, PosGuiData data,
                                        PanelSyncManager syncManager, UISettings settings) {
        OreExtractionDrillLogic logic = machine.getRecipeLogic();

        BooleanSyncValue formed = new BooleanSyncValue(machine::isFormed);
        BooleanSyncValue enabled = new BooleanSyncValue(machine::isWorkingEnabled);
        EnumSyncValue<DrillPhase> phase = new EnumSyncValue<>(DrillPhase.class, logic::getPhase);
        EnumSyncValue<RecipeLogic.Status> recipeStatus = new EnumSyncValue<>(RecipeLogic.Status.class,
                logic::getStatus);
        DoubleSyncValue operationProgress = new DoubleSyncValue(logic::getOperationProgress);
        DoubleSyncValue scanProgress = new DoubleSyncValue(logic::getScanProgressPercent);
        IntSyncValue excavated = new IntSyncValue(logic::getExcavatedOreCount);
        IntSyncValue total = new IntSyncValue(logic::getPendingOreCount);
        IntSyncValue remaining = new IntSyncValue(logic::getRemainingOreCount);
        IntSyncValue surveyChunks = new IntSyncValue(logic::getSurveyChunksPerSide);
        IntSyncValue cycleTicks = new IntSyncValue(logic::getCurrentCycleTicks);
        LongSyncValue etaSeconds = new LongSyncValue(logic::getEstimatedSecondsRemaining);

        syncManager.syncValue("drill_formed", formed);
        syncManager.syncValue("drill_enabled", enabled);
        syncManager.syncValue("drill_phase", phase);
        syncManager.syncValue("drill_recipe_status", recipeStatus);
        syncManager.syncValue("drill_progress", operationProgress);
        syncManager.syncValue("drill_scan_progress", scanProgress);
        syncManager.syncValue("drill_excavated", excavated);
        syncManager.syncValue("drill_total", total);
        syncManager.syncValue("drill_remaining", remaining);
        syncManager.syncValue("drill_survey_chunks", surveyChunks);
        syncManager.syncValue("drill_cycle_ticks", cycleTicks);
        syncManager.syncValue("drill_eta", etaSeconds);

        GenericListSyncHandler<RegistryFriendlyByteBuf, OreLedgerEntry> ledger = GenericListSyncHandler
                .<RegistryFriendlyByteBuf, OreLedgerEntry>builder()
                .getter(logic::getOreLedgerEntries)
                .deserializer(buffer -> new OreLedgerEntry(
                        buffer.readUtf(),
                        buffer.readResourceLocation(),
                        buffer.readVarInt()))
                .serializer((buffer, entry) -> {
                    buffer.writeUtf(entry.translationKey());
                    buffer.writeResourceLocation(entry.itemId());
                    buffer.writeVarInt(entry.count());
                })
                .immutableCopy()
                .build();
        syncManager.syncValue("drill_ledger", ledger);

        DynamicLinkedSyncHandler<RegistryFriendlyByteBuf, GenericListSyncHandler<RegistryFriendlyByteBuf, OreLedgerEntry>> ledgerWidgets = new DynamicLinkedSyncHandler<>(
                ledger)
                .widgetProvider((widgetSyncManager, value) -> buildLedger(value.getValue()));

        ModularPanel<?> panel = ModularPanel
                .defaultPanel(machine.getDefinition().getId().getPath(), PANEL_WIDTH, PANEL_HEIGHT)
                .background(GTGuiTextures.BACKGROUND);
        panel.child(GTMuiWidgets.createTitleBar(machine.getDefinition(), PANEL_WIDTH));
        panel.child(displayPanel(Flow.row()
                .padding(5)
                .childPadding(5)
                .child(buildOperations(machine, formed, enabled, phase, recipeStatus, operationProgress,
                        scanProgress, excavated, total, remaining, surveyChunks, cycleTicks, etaSeconds)
                        .width(OPERATIONS_WIDTH)
                        .heightRel(1))
                .child(new ParentWidget<>()
                        .width(1)
                        .heightRel(1)
                        .background(new Rectangle().color(0xFF2A2A2A)))
                .child(Flow.column()
                        .childPadding(3)
                        .child(new TextWidget<>(Component.translatable(
                                "cosmiccore.machine.ore_extraction_drill.ui.ledger"))
                                .color(HEADER_TEXT_COLOR)
                                .height(10)
                                .widthRel(1))
                        .child(new DynamicWidget<>()
                                .syncHandler(ledgerWidgets)
                                .widthRel(1)
                                .heightRel(1))
                        .widthRel(1)
                        .heightRel(1)))
                .pos(6, 17)
                .size(DISPLAY_WIDTH, CONTENT_HEIGHT));
        panel.child(SlotGroupWidget.playerInventory(7, true,
                (index, slot) -> slot.background(GTGuiTextures.SLOT)));
        panel.child(Flow.column()
                .coverChildren()
                .leftRel(1.0f)
                .reverseLayout(true)
                .padding(2, 4, 4, 4)
                .bottom(16)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .childPadding(2)
                .excludeAreaInRecipeViewer()
                .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                .child(GTMuiWidgets.createPowerButton(machine)));
        return panel;
    }

    private static Flow buildOperations(OreExtractionDrillMachine machine, BooleanSyncValue formed,
                                        BooleanSyncValue enabled, EnumSyncValue<DrillPhase> phase,
                                        EnumSyncValue<RecipeLogic.Status> recipeStatus,
                                        DoubleSyncValue operationProgress, DoubleSyncValue scanProgress,
                                        IntSyncValue excavated, IntSyncValue total, IntSyncValue remaining,
                                        IntSyncValue surveyChunks, IntSyncValue cycleTicks,
                                        LongSyncValue etaSeconds) {
        return Flow.column()
                .childPadding(3)
                .child(new TextWidget<>(Component.translatable(
                        "cosmiccore.machine.ore_extraction_drill.ui.operations"))
                        .color(HEADER_TEXT_COLOR)
                        .height(10)
                        .widthRel(1))
                .child(new TextWidget<>(() -> statusComponent(
                        formed.getBoolValue(),
                        enabled.getBoolValue(),
                        phase.getValue(),
                        recipeStatus.getValue()))
                        .height(10)
                        .widthRel(1))
                .child(progressBar(operationProgress))
                .child(new TextWidget<>(() -> progressComponent(
                        phase.getValue(),
                        scanProgress.getDoubleValue(),
                        excavated.getIntValue(),
                        total.getIntValue()))
                        .color(BODY_TEXT_COLOR)
                        .scale(0.8f)
                        .height(12)
                        .widthRel(1))
                .child(new TextWidget<>(() -> Component.translatable(
                        "cosmiccore.machine.ore_extraction_drill.ui.area",
                        surveyChunks.getIntValue(),
                        surveyChunks.getIntValue()))
                        .color(BODY_TEXT_COLOR)
                        .scale(0.8f)
                        .height(9)
                        .widthRel(1))
                .child(new TextWidget<>(() -> Component.translatable(
                        "cosmiccore.machine.ore_extraction_drill.ui.power",
                        FormattingUtil.formatNumbers(machine.getEnergyPerTick())))
                        .color(BODY_TEXT_COLOR)
                        .scale(0.8f)
                        .height(9)
                        .widthRel(1))
                .child(new TextWidget<>(() -> Component.translatable(
                        "cosmiccore.machine.ore_extraction_drill.ui.cycle",
                        FormattingUtil.formatNumbers(cycleTicks.getIntValue() / 20.0)))
                        .color(BODY_TEXT_COLOR)
                        .scale(0.8f)
                        .height(9)
                        .widthRel(1))
                .child(new TextWidget<>(() -> Component.translatable(
                        "cosmiccore.machine.ore_extraction_drill.ui.remaining",
                        FormattingUtil.formatNumbers(remaining.getIntValue())))
                        .color(BODY_TEXT_COLOR)
                        .scale(0.8f)
                        .height(9)
                        .widthRel(1))
                .child(new TextWidget<>(() -> etaComponent(phase.getValue(), etaSeconds.getLongValue()))
                        .color(BODY_TEXT_COLOR)
                        .scale(0.8f)
                        .height(9)
                        .widthRel(1));
    }

    private static ParentWidget<?> displayPanel(Flow content) {
        ParentWidget<?> panel = new ParentWidget<>()
                .background(new Rectangle().color(0xFF555555));
        panel.child(new ParentWidget<>()
                .pos(2, 2)
                .widthRelOffset(1, -4)
                .heightRelOffset(1, -4)
                .background(new Rectangle().color(0xFF000000))
                .child(content
                        .widthRel(1)
                        .heightRel(1)));
        return panel;
    }

    private static ParentWidget<?> progressBar(DoubleSyncValue progress) {
        return new ParentWidget<>()
                .height(9)
                .widthRel(1)
                .background(new Rectangle().color(0xFF555555))
                .child(new ProgressWidget()
                        .value(progress)
                        .texture(
                                new Rectangle().color(0xFF141414),
                                new Rectangle().horizontalGradient(0xFF2D7D68, 0xFF65C89A),
                                ProgressDrawable.Direction.RIGHT)
                        .pos(1, 1)
                        .height(7)
                        .widthRelOffset(1, -2));
    }

    private static Component statusComponent(boolean formed, boolean enabled, DrillPhase phase,
                                             RecipeLogic.Status recipeStatus) {
        if (!formed) {
            return Component.translatable("cosmiccore.machine.ore_extraction_drill.ui.status.unformed")
                    .withStyle(ChatFormatting.RED);
        }
        if (!enabled) {
            return Component.translatable("cosmiccore.machine.ore_extraction_drill.ui.status.paused")
                    .withStyle(ChatFormatting.YELLOW);
        }
        if (phase == DrillPhase.MINING && recipeStatus == RecipeLogic.Status.WAITING) {
            return Component.translatable("cosmiccore.machine.ore_extraction_drill.ui.status.awaiting_power")
                    .withStyle(ChatFormatting.RED);
        }
        String key = "cosmiccore.machine.ore_extraction_drill.ui.status." + phase.name().toLowerCase();
        ChatFormatting color = switch (phase) {
            case IDLE -> ChatFormatting.GRAY;
            case SCANNING -> ChatFormatting.AQUA;
            case MINING -> ChatFormatting.GOLD;
            case COMPLETE -> ChatFormatting.GREEN;
        };
        return Component.translatable(key).withStyle(color);
    }

    private static Component progressComponent(DrillPhase phase, double scanProgress, int excavated, int total) {
        if (phase == DrillPhase.SCANNING) {
            return Component.translatable(
                    "cosmiccore.machine.ore_extraction_drill.ui.scan_progress",
                    String.format("%.1f", scanProgress));
        }
        return Component.translatable(
                "cosmiccore.machine.ore_extraction_drill.ui.mining_progress",
                FormattingUtil.formatNumbers(excavated),
                FormattingUtil.formatNumbers(total));
    }

    private static Component etaComponent(DrillPhase phase, long seconds) {
        Component duration;
        if (phase != DrillPhase.MINING && phase != DrillPhase.COMPLETE) {
            duration = Component.translatable("cosmiccore.machine.ore_extraction_drill.ui.eta.calculating");
        } else if (seconds <= 0) {
            duration = Component.translatable("cosmiccore.machine.ore_extraction_drill.ui.eta.complete");
        } else if (seconds >= 86_400) {
            duration = Component.translatable(
                    "cosmiccore.machine.ore_extraction_drill.ui.duration.days",
                    seconds / 86_400,
                    seconds % 86_400 / 3_600);
        } else if (seconds >= 3_600) {
            duration = Component.translatable(
                    "cosmiccore.machine.ore_extraction_drill.ui.duration.hours",
                    seconds / 3_600,
                    seconds % 3_600 / 60);
        } else if (seconds >= 60) {
            duration = Component.translatable(
                    "cosmiccore.machine.ore_extraction_drill.ui.duration.minutes",
                    seconds / 60,
                    seconds % 60);
        } else {
            duration = Component.translatable(
                    "cosmiccore.machine.ore_extraction_drill.ui.duration.seconds",
                    seconds);
        }
        return Component.translatable("cosmiccore.machine.ore_extraction_drill.ui.eta", duration);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static ListWidget buildLedger(List<OreLedgerEntry> entries) {
        ListWidget list = new ListWidget();
        list.widthRel(1);
        list.heightRel(1);
        list.collapseDisabledChildren();
        list.crossAxisAlignment(Alignment.CrossAxis.START);
        if (entries.isEmpty()) {
            list.child(new TextWidget<>(Component.translatable(
                    "cosmiccore.machine.ore_extraction_drill.ui.ledger.empty"))
                    .color(BODY_TEXT_COLOR)
                    .scale(0.8f)
                    .widthRel(1)
                    .height(12));
            return list;
        }
        for (OreLedgerEntry entry : entries) {
            Item item = BuiltInRegistries.ITEM.get(entry.itemId());
            ItemStack stack = new ItemStack(item == Items.AIR ? Items.BARRIER : item);
            list.child(Flow.row()
                    .height(15)
                    .widthRel(1)
                    .child(new ItemDrawable(stack).asWidget()
                            .size(12)
                            .marginRight(2))
                    .child(new TextWidget<>(Component.translatable(entry.translationKey()))
                            .color(BODY_TEXT_COLOR)
                            .scale(0.75f)
                            .height(12)
                            .width(61))
                    .child(new TextWidget<>(FormattingUtil.formatNumbers(entry.count()))
                            .color(BODY_TEXT_COLOR)
                            .scale(0.75f)
                            .textAlign(Alignment.CenterRight)
                            .height(12)
                            .width(34)));
        }
        return list;
    }
}
