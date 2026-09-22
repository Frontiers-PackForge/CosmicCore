package com.ghostipedia.cosmiccore.client.rate;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.RateCalculatorPackets;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RateCalculatorScreen extends Screen {

    private static final int ROW_HEIGHT = 28;
    private int draggedList = -1;
    private double dragOffset;
    private static final long[] SCALES = { 1, 5, 20, 100, 1_200, 6_000, 36_000, 72_000, 1_728_000 };
    private static final String[] SCALE_KEYS = { "tick", "5_ticks", "second", "5_seconds", "minute", "5_minutes",
            "30_minutes", "hour", "day" };
    private static final String[] SORT_MODES = { "default", "id", "produced", "consumed" };
    private static final String[] SORT_ICONS = { "◆", "A", "+", "−" };
    private static final DecimalFormat AMOUNT = new DecimalFormat("#,##0.##");
    private final UUID tool;
    private final RateCalculatorSmoothing smoothing = new RateCalculatorSmoothing();
    private final List<Button> sortButtons = new ArrayList<>();
    private CompoundTag report;
    private CompoundTag latestReport;
    private boolean held;
    private int rowScroll, contributorScroll, selectedRow = -1, scaleIndex = 2;
    private String selectedKey = "";
    private String sortMode = RateCalculatorRowOrdering.DEFAULT_MODE;
    private boolean reverseSort;
    private long nextRefresh;
    private boolean closed;
    private List<net.minecraft.util.FormattedCharSequence> hoverTooltip;

    private RateCalculatorScreen(UUID tool, CompoundTag report) {
        super(Component.translatable("item.cosmiccore.rate_calculator"));
        this.tool = tool;
        this.report = smoothing.update(report, Util.getMillis());
        this.latestReport = this.report;
    }

    public static void open(UUID tool, CompoundTag report) {
        Minecraft.getInstance().setScreen(new RateCalculatorScreen(tool, report));
    }

    public static void update(UUID tool, CompoundTag report) {
        if (Minecraft.getInstance().screen instanceof RateCalculatorScreen screen && screen.tool.equals(tool)) {
            screen.latestReport = screen.smoothing.update(report, Util.getMillis());
            if (!screen.held) screen.showLatestReport();
        }
    }

    private void showLatestReport() {
        report = latestReport;
        selectedRow = indexOfSelected();
        rowScroll = clamp(rowScroll, 0, Math.max(0, rows().size() - visibleRows()));
        contributorScroll = clamp(contributorScroll, 0, Math.max(0, contributors().size() - visibleContributors()));
    }

    public static void clear(UUID tool) {
        if (Minecraft.getInstance().screen instanceof RateCalculatorScreen screen && screen.tool.equals(tool)) {
            screen.closed = true;
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    protected void init() {
        int left = left(), top = top(), buttonWidth = (panelWidth() - 26) / 2;
        addRenderableWidget(RateCalculatorButton.create(left + 12, top + 34, buttonWidth,
                scaleLabel(), button -> {
                    scaleIndex = (scaleIndex + 1) % SCALES.length;
                    button.setMessage(scaleLabel());
                }));
        addRenderableWidget(RateCalculatorButton.create(left + 14 + buttonWidth, top + 34, buttonWidth,
                Component.translatable("gui.cosmiccore.rate_calculator.reset"),
                button -> CCoreNetwork.sendToServer(new RateCalculatorPackets.Reset(tool))));
        var holdButton = RateCalculatorButton.create(left + panelWidth() / 2 - 62, top + 100, 52,
                holdLabel(), button -> {
                    held = !held;
                    if (!held) showLatestReport();
                    button.setMessage(holdLabel());
                });
        holdButton.setHeight(12);
        holdButton.setTooltip(Tooltip.create(Component.translatable("gui.cosmiccore.rate_calculator.hold_tooltip")));
        addRenderableWidget(holdButton);
        sortButtons.clear();
        int sortY = top + 100;
        for (int i = 0; i < SORT_MODES.length; i++) {
            String mode = SORT_MODES[i];
            Button sortButton = RateCalculatorButton.create(left + 12 + i * 24, sortY, 20,
                    Component.literal(SORT_ICONS[i]), button -> {
                        if (sortMode.equals(mode)) reverseSort = !reverseSort;
                        else {
                            sortMode = mode;
                            reverseSort = false;
                        }
                        selectedRow = indexOfSelected();
                        rowScroll = 0;
                        updateSortButtons();
                    });
            sortButtons.add(sortButton);
            addRenderableWidget(sortButton);
        }
        updateSortButtons();
    }

    private Component holdLabel() {
        return Component.translatable("gui.cosmiccore.rate_calculator." + (held ? "live" : "hold"));
    }

    @Override
    public void tick() {
        if (minecraft != null && minecraft.level != null && minecraft.level.getGameTime() >= nextRefresh) {
            nextRefresh = minecraft.level.getGameTime() + 20L;
            CCoreNetwork.sendToServer(new RateCalculatorPackets.Refresh(tool));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        closeSession();
        super.removed();
    }

    @Override
    public void onClose() {
        closeSession();
        super.onClose();
    }

    private void closeSession() {
        if (!closed) {
            closed = true;
            CCoreNetwork.sendToServer(new RateCalculatorPackets.Close(tool));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0) return false;
        int direction = verticalAmount > 0 ? -1 : 1;
        if (mouseX >= left() + 8 && mouseX < left() + panelWidth() / 2 && mouseY >= rowsTop() && mouseY < rowBottom()) {
            rowScroll = clamp(rowScroll + direction, 0, Math.max(0, rows().size() - visibleRows()));
            return true;
        }
        if (mouseX >= left() + panelWidth() / 2 && mouseX < left() + panelWidth() - 7 && mouseY >= detailRowsTop() &&
                mouseY < rowBottom()) {
            contributorScroll = clamp(contributorScroll + direction, 0,
                    Math.max(0, contributors().size() - visibleContributors()));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0) for (int list = 0; list < 2; list++) {
            int start = list == 0 ? rowsTop() : detailRowsTop();
            if (mouseX >= scrollbarX(list) && mouseX < scrollbarX(list) + 6 && mouseY >= start &&
                    mouseY < rowBottom() && maxScroll(list) > 0) {
                draggedList = list;
                int thumbY = thumbY(list), thumbHeight = thumbHeight(list);
                dragOffset = mouseY >= thumbY && mouseY < thumbY + thumbHeight ? mouseY - thumbY : thumbHeight / 2.0;
                scrollTo(mouseY);
                return true;
            }
        }
        if (mouseX >= left() + 8 && mouseX < scrollbarX(0) - 3 && mouseY >= rowsTop() &&
                mouseY < rowsTop() + visibleRows() * ROW_HEIGHT) {
            if ((mouseY - rowsTop()) % ROW_HEIGHT >= ROW_HEIGHT - 2) return false;
            int index = (int) ((mouseY - rowsTop()) / ROW_HEIGHT) + rowScroll;
            if (button == 0 && index >= 0 && index < rows().size()) {
                selectedRow = index;
                selectedKey = key(rows().get(index));
                contributorScroll = 0;
                return true;
            }
        }
        if (button == 0 && mouseX >= left() + panelWidth() / 2 + 6 && mouseX < scrollbarX(1) - 3 &&
                mouseY >= detailRowsTop() && mouseY < detailRowsTop() + visibleContributors() * ROW_HEIGHT) {
            if ((mouseY - detailRowsTop()) % ROW_HEIGHT >= ROW_HEIGHT - 2) return false;
            int index = (int) ((mouseY - detailRowsTop()) / ROW_HEIGHT) + contributorScroll;
            if (index < contributors().size()) {
                return locate(contributors().get(index));
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        hoverTooltip = null;
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = left(), top = top(), width = panelWidth();
        RateCalculatorPanelRenderer.draw(graphics, left, top, width, panelHeight());
        graphics.drawString(font, title, left + 12, top + 13, 0xFFFFFF);
        graphics.drawString(font,
                Component.translatable(held ? "gui.cosmiccore.rate_calculator.held_snapshot" :
                        "gui.cosmiccore.rate_calculator.snapshot",
                        Component.translatable("gui.cosmiccore.rate_calculator.time." + SCALE_KEYS[scaleIndex])),
                left + 12, top + 57, 0xD8D8D8);
        graphics.drawString(font,
                Component.translatable("gui.cosmiccore.rate_calculator.machines", report.getInt("loaded"),
                        report.getInt("selected"), report.getInt("unknown"), report.getInt("unloaded")),
                left + 12, top + 68, 0xB8B8B8);
        graphics.drawString(font,
                ellipsis(Component.translatable("gui.cosmiccore.rate_calculator.electrical",
                        amount(report.getDouble("configuredEUt"))).getString(),
                        width / 2 - 18),
                left + 12, top + 79, 0xF6D58A);
        String flags = flags();
        if (!flags.isEmpty()) graphics.drawString(font,
                ellipsis(Component.translatable("gui.cosmiccore.rate_calculator.flags", flags).getString(),
                        width / 2 - 14),
                left + width / 2 + 6, top + 79, 0xFFB16A);
        graphics.drawString(font, Component.translatable("gui.cosmiccore.rate_calculator.resources"), left + 12,
                top + 91, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("gui.cosmiccore.rate_calculator.detail"), left + width / 2 + 6,
                top + 91, 0xFFFFFF);
        drawRows(graphics, left, width, mouseX, mouseY);
        drawDetail(graphics, left, top, width, mouseX, mouseY);
        drawScrollbar(graphics, 0);
        drawScrollbar(graphics, 1);
        for (Renderable renderable : renderables) renderable.render(graphics, mouseX, mouseY, partialTick);
        if (hoverTooltip != null) graphics.renderTooltip(font, hoverTooltip, mouseX, mouseY);
    }

    private void drawRows(GuiGraphics graphics, int left, int width, int mouseX, int mouseY) {
        List<CompoundTag> rows = rows();
        int rowWidth = width / 2 - 22;
        graphics.drawString(font, ellipsis(Component.translatable("gui.cosmiccore.rate_calculator.columns.snapshot")
                .getString(), rowWidth - 58),
                left + 12, top() + 124, 0xA0A0A0);
        graphics.enableScissor(left + 9, rowsTop(), left + width / 2 - 4, rowBottom());
        for (int display = 0; display < visibleRows(); display++) {
            int index = display + rowScroll;
            if (index >= rows.size()) break;
            CompoundTag row = rows.get(index);
            int y = rowsTop() + display * ROW_HEIGHT;
            RateCalculatorButton.drawRow(graphics, left + 9, y, rowWidth, ROW_HEIGHT - 2, index == selectedRow ||
                    mouseX >= left + 9 && mouseX < scrollbarX(0) - 3 && mouseY >= y && mouseY < y + ROW_HEIGHT - 2);
            Component name = resourceName(row);
            drawIcon(graphics, row, left + 12, y + 5);
            graphics.drawString(font, ellipsis(name.getString(), rowWidth - 24), left + 30, y + 3,
                    row.getBoolean("known") ? 0xE8E8E8 : 0xFFB16A);
            graphics.drawString(font, ellipsis(rateLine(row), rowWidth - 24), left + 30, y + 14,
                    0xA0D6FF);
            if (mouseX >= left + 9 && mouseX < scrollbarX(0) - 3 && mouseY >= y && mouseY < y + ROW_HEIGHT - 2)
                hoverTooltip = List.of(name.getVisualOrderText(),
                        Component.literal(rateLine(row, false)).getVisualOrderText());
        }
        graphics.disableScissor();
    }

    private void drawDetail(GuiGraphics graphics, int left, int top, int width, int mouseX, int mouseY) {
        if (selectedRow < 0 || selectedRow >= rows().size()) {
            graphics.drawString(font, Component.translatable("gui.cosmiccore.rate_calculator.select_resource"),
                    left + width / 2 + 6, top + 113, 0xB8B8B8);
            return;
        }
        CompoundTag row = rows().get(selectedRow);
        int x = left + width / 2 + 6, paneWidth = width / 2 - 22;
        drawIcon(graphics, row, x, top + 104);
        graphics.drawString(font, ellipsis(resourceName(row).getString(), paneWidth - 20), x + 18, top + 106, 0xE8E8E8);
        graphics.drawString(font, ellipsis(rateLine(row), paneWidth), x, top + 124, 0xA0D6FF);
        drawBalance(graphics, row, x, top, paneWidth, mouseX, mouseY);
        List<CompoundTag> contributors = contributors();
        int start = detailRowsTop();
        graphics.enableScissor(x, start, left + width - 8, rowBottom());
        for (int display = 0; display < visibleContributors(); display++) {
            int index = display + contributorScroll;
            if (index >= contributors.size()) break;
            CompoundTag contributor = contributors.get(index);
            int y = start + display * ROW_HEIGHT;
            RateCalculatorButton.drawRow(graphics, x, y, paneWidth, ROW_HEIGHT - 2,
                    mouseX >= x && mouseX < x + paneWidth && mouseY >= y && mouseY < y + ROW_HEIGHT - 2);
            drawMachineIcon(graphics, contributor, x + 3, y + 5);
            drawIcon(graphics, row, x + 21, y + 5);
            graphics.drawString(font, ellipsis(snapshotContributorLine(contributor), paneWidth - 43), x + 40, y + 9,
                    0xA0D6FF);
            if (mouseX >= x && mouseX < x + paneWidth && mouseY >= y && mouseY < y + ROW_HEIGHT - 2)
                hoverTooltip = contributorTooltip(contributor);
        }
        graphics.disableScissor();
    }

    private void drawBalance(GuiGraphics graphics, CompoundTag row, int x, int top, int paneWidth,
                             int mouseX, int mouseY) {
        String balance = balanceLine(row);
        if (!balance.isEmpty()) {
            graphics.drawString(font, ellipsis(balance, paneWidth), x, top + 143, 0xB8B8B8);
            if (mouseX >= x && mouseX < x + paneWidth && mouseY >= top + 140 && mouseY < top + 152)
                hoverTooltip = List.of(Component.literal(balance).getVisualOrderText());
        }
    }

    private String balanceLine(CompoundTag row) {
        String ratio = row.getBoolean("ratioKnown") ?
                Component.translatable("gui.cosmiccore.rate_calculator.ratio", row.getInt("ratioProducers"),
                        row.getInt("ratioConsumers")).getString() :
                "";
        String adjustment = row.getBoolean("balanced") ?
                Component.translatable("gui.cosmiccore.rate_calculator.balanced").getString() :
                row.getBoolean("adjustmentKnown") ?
                        Component.translatable("gui.cosmiccore.rate_calculator.adjustment",
                                row.getInt("adjustmentCount"),
                                machineName(row.getString("adjustmentMachine")),
                                signed(row.getDouble("adjustmentNet") * SCALES[scaleIndex])).getString() :
                        "";
        return ratio.isEmpty() ? adjustment : adjustment.isEmpty() ? ratio : ratio + " | " + adjustment;
    }

    private String snapshotContributorLine(CompoundTag contributor) {
        return Component.translatable("gui.cosmiccore.rate_calculator.activity_contributor",
                machineName(contributor), value(contributor, "observedNet", false, true,
                        selectedRow >= 0 && selectedRow < rows().size() &&
                                rows().get(selectedRow).getString("kind").equals("fluid")),
                Component.translatable("gui.cosmiccore.rate_calculator.reason." + contributor.getString("reason")))
                .getString();
    }

    private List<net.minecraft.util.FormattedCharSequence> contributorTooltip(CompoundTag contributor) {
        List<net.minecraft.util.FormattedCharSequence> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(machineName(contributor)).getVisualOrderText());
        tooltip.add(Component.translatable("gui.cosmiccore.rate_calculator.recipe", contributor.getString("recipe"))
                .getVisualOrderText());
        tooltip.add(Component.translatable("gui.cosmiccore.rate_calculator.smoothed_rates",
                value(contributor, "observedIn"), value(contributor, "observedOut"),
                value(contributor, "observedNet")).getVisualOrderText());
        tooltip.add(Component.translatable("gui.cosmiccore.rate_calculator.raw_rates",
                value(contributor, "observedIn", true), value(contributor, "observedOut", true),
                value(contributor, "observedNet", true)).getVisualOrderText());
        tooltip.add(Component.translatable(contributor.getBoolean("recordedCapacity") ?
                "gui.cosmiccore.rate_calculator.recorded_capacity" : "gui.cosmiccore.rate_calculator.configured_rates",
                value(contributor, "configuredIn"), value(contributor, "configuredOut"),
                value(contributor, "configuredNet")).getVisualOrderText());
        tooltip.add(Component.translatable("gui.cosmiccore.rate_calculator.reason." + contributor.getString("reason"))
                .getVisualOrderText());
        if (contributor.getBoolean("learning"))
            tooltip.add(Component.translatable("gui.cosmiccore.rate_calculator.learning").getVisualOrderText());
        if (!contributor.getBoolean("observedKnown"))
            tooltip.add(Component.translatable("gui.cosmiccore.rate_calculator.provisional").getVisualOrderText());
        tooltip.add(Component.translatable("gui.cosmiccore.rate_calculator.evidence",
                contributor.getLong("activityElapsed"), contributor.getLong("activityHorizon"),
                contributor.getLong("activityCompleted")).getVisualOrderText());
        if (!contributor.getBoolean("activityFresh")) tooltip
                .add(Component.translatable("gui.cosmiccore.rate_calculator.remembered_activity").getVisualOrderText());
        tooltip.add(Component.translatable("gui.cosmiccore.rate_calculator.locate_group").getVisualOrderText());
        tooltip.add(Component.literal(snapshotContributorLine(contributor)).getVisualOrderText());
        return tooltip;
    }

    private boolean locate(CompoundTag contributor) {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) return false;
        ResourceLocation dimension = ResourceLocation.tryParse(report.getString("dimension"));
        if (!minecraft.level.dimension().location().equals(dimension)) return false;
        List<BlockPos> positions = new ArrayList<>();
        for (Tag tag : contributor.getList("positions", Tag.TAG_LONG))
            positions.add(BlockPos.of(((net.minecraft.nbt.LongTag) tag).getAsLong()));
        if (positions.isEmpty()) return false;
        onClose();
        com.ghostipedia.nebulaeae2.client.locating.ProviderHighlightClient.receive(
                new com.ghostipedia.nebulaeae2.locating.ProviderLocations(
                        minecraft.player.containerMenu.containerId, dimension, positions));
        return true;
    }

    private List<CompoundTag> rows() {
        return RateCalculatorRowOrdering.sorted(tags(report.getList("rows", Tag.TAG_COMPOUND)), sortMode, reverseSort);
    }

    private void updateSortButtons() {
        for (int i = 0; i < sortButtons.size(); i++) {
            Button button = sortButtons.get(i);
            boolean selectedMode = SORT_MODES[i].equals(sortMode);
            button.setMessage(Component.literal(SORT_ICONS[i] + (selectedMode ? reverseSort ? "↑" : "↓" : "")));
            button.setTooltip(sortTooltip(SORT_MODES[i], selectedMode));
        }
    }

    private Tooltip sortTooltip(String mode, boolean selectedMode) {
        boolean shownReverse = selectedMode && reverseSort;
        Component tooltip = Component.empty()
                .append(Component.translatable("gui.cosmiccore.rate_calculator.sort." + mode)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append("\n")
                .append(Component.translatable("gui.cosmiccore.rate_calculator.sort.order." + mode + "." +
                        (shownReverse ? "reverse" : "top")).withStyle(ChatFormatting.GRAY));
        if (selectedMode)
            tooltip = tooltip.copy().append("\n")
                    .append(Component.translatable("gui.cosmiccore.production_statistics.sort.direction." +
                            (reverseSort ? "reverse" : "top")).withStyle(ChatFormatting.AQUA))
                    .append("\n")
                    .append(Component.translatable("gui.cosmiccore.production_statistics.sort.action.reverse")
                            .withStyle(ChatFormatting.YELLOW));
        else tooltip = tooltip.copy().append("\n")
                .append(Component.translatable("gui.cosmiccore.production_statistics.sort.action.select")
                        .withStyle(ChatFormatting.YELLOW));
        return Tooltip.create(tooltip);
    }

    private List<CompoundTag> contributors() {
        return selectedRow < 0 || selectedRow >= rows().size() ? List.of() :
                tags(rows().get(selectedRow).getList("contributors", Tag.TAG_COMPOUND));
    }

    private int indexOfSelected() {
        for (int index = 0; index < rows().size(); index++)
            if (key(rows().get(index)).equals(selectedKey)) return index;
        return -1;
    }

    private static String key(CompoundTag row) {
        return RateCalculatorSimulation.key(row);
    }

    private static List<CompoundTag> tags(ListTag tags) {
        List<CompoundTag> result = new ArrayList<>(tags.size());
        for (Tag tag : tags) if (tag instanceof CompoundTag compound) result.add(compound);
        return result;
    }

    private Component resourceName(CompoundTag row) {
        if (List.of("energy", "charge", "computation").contains(row.getString("kind")))
            return Component.translatable("gui.cosmiccore.rate_calculator.resource." + row.getString("kind"));
        ItemStack stack = icon(row);
        if (!stack.isEmpty()) return stack.getHoverName();
        String idText = row.getString("id");
        if (idText.startsWith("ingredient|")) return groupedName(row.getString("kind"), idText);
        ResourceLocation id = ResourceLocation.tryParse(idText);
        if (id != null && row.getString("kind").equals("item")) {
            var item = BuiltInRegistries.ITEM.getOptional(id);
            if (item.isPresent()) return new ItemStack(item.get()).getHoverName();
        }
        if (id != null && row.getString("kind").equals("fluid")) {
            var fluid = BuiltInRegistries.FLUID.getOptional(id);
            if (fluid.isPresent()) return fluid.get().getFluidType().getDescription();
        }
        return Component.translatable("gui.cosmiccore.rate_calculator.unknown_resource");
    }

    private Component groupedName(String kind, String key) {
        List<String> names = new ArrayList<>();
        for (String part : key.split("\\|")) {
            if (part.equals("ingredient")) continue;
            CompoundTag resource = new CompoundTag();
            resource.putString("kind", kind);
            resource.putString("id", part);
            String name = resourceName(resource).getString();
            if (!name.equals(Component.translatable("gui.cosmiccore.rate_calculator.unknown_resource").getString()))
                names.add(name);
        }
        return names.isEmpty() ? Component.translatable("gui.cosmiccore.rate_calculator.unknown_resource") :
                Component.literal(String.join(" / ", names));
    }

    private ItemStack icon(CompoundTag row) {
        return minecraft == null || minecraft.level == null || !row.getString("kind").equals("item") ? ItemStack.EMPTY :
                ItemStack.parseOptional(minecraft.level.registryAccess(), row.getCompound("icon"));
    }

    private String machineName(CompoundTag contributor) {
        return machineName(contributor.getString("machine"));
    }

    private String machineName(String machine) {
        ResourceLocation id = ResourceLocation.tryParse(machine);
        return id != null ? BuiltInRegistries.BLOCK.getOptional(id)
                .map(block -> Component.translatable(block.getDescriptionId()).getString())
                .orElse(machine) : machine;
    }

    private static String tierName(int tier) {
        return tier >= 0 && tier < GTValues.VNF.length ? GTValues.VNF[tier] : "Fixed";
    }

    private void drawMachineIcon(GuiGraphics graphics, CompoundTag contributor, int x, int y) {
        ResourceLocation id = ResourceLocation.tryParse(contributor.getString("machine"));
        if (id != null) BuiltInRegistries.BLOCK.getOptional(id).map(block -> new ItemStack(block.asItem()))
                .filter(stack -> !stack.isEmpty()).ifPresent(stack -> graphics.renderItem(stack, x, y));
    }

    private String flags() {
        List<String> flags = new ArrayList<>();
        if (report.getBoolean("partial"))
            flags.add(Component.translatable("gui.cosmiccore.rate_calculator.partial").getString());
        if (rows().stream().anyMatch(row -> row.getBoolean("learning")))
            flags.add(Component.translatable("gui.cosmiccore.rate_calculator.learning").getString());
        if (report.getBoolean("truncated"))
            flags.add(Component.translatable("gui.cosmiccore.rate_calculator.truncated").getString());
        return String.join(", ", flags);
    }

    private Component scaleLabel() {
        return Component.translatable("gui.cosmiccore.rate_calculator.scale_value",
                Component.translatable("gui.cosmiccore.rate_calculator.time." + SCALE_KEYS[scaleIndex]));
    }

    private String rateLine(CompoundTag tag) {
        return rateLine(tag, true);
    }

    private String rateLine(CompoundTag tag, boolean compact) {
        return Component.translatable("gui.cosmiccore.rate_calculator.compact_rates",
                value(tag, "observedOut", false, compact), value(tag, "observedIn", false, compact),
                value(tag, "observedNet", false, compact)).getString();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String amount(double value) {
        if (!Double.isFinite(value)) return "?";
        if (value != 0 && Math.abs(value) < 0.01) return value < 0 ? "-<0.01" : "<0.01";
        return AMOUNT.format(value);
    }

    private String value(CompoundTag tag, String key) {
        return value(tag, key, false);
    }

    private String value(CompoundTag tag, String key, boolean raw) {
        return value(tag, key, raw, false);
    }

    private String value(CompoundTag tag, String key, boolean raw, boolean compact) {
        return value(tag, key, raw, compact, tag.getString("kind").equals("fluid"));
    }

    private String value(CompoundTag tag, String key, boolean raw, boolean compact, boolean fluid) {
        boolean known = key.startsWith("configured") ?
                tag.getBoolean("configuredKnown") && tag.getBoolean("expectedKnown") :
                tag.getBoolean(key.equals("observedIn") ? "observedInAvailable" :
                        key.equals("observedOut") ? "observedOutAvailable" : "observedAvailable");
        if (!known) return "?";
        String amountKey = raw ? "rawO" + key.substring(1) : key;
        double scaled = tag.getDouble(amountKey) * SCALES[scaleIndex];
        String quantity;
        if (compact && Double.isFinite(scaled) && (scaled == 0 || Math.abs(scaled) >= 0.01)) {
            quantity = FormattingUtil.formatNumberReadable(scaled, fluid, AMOUNT, fluid ? "B" : null);
            if (key.endsWith("Net") && scaled > 0) quantity = "+" + quantity;
        } else quantity = key.endsWith("Net") ? signed(scaled) : amount(scaled);
        boolean exact = tag.getBoolean(key.equals("observedIn") ? "observedInKnown" :
                key.equals("observedOut") ? "observedOutKnown" : "observedKnown");
        return key.startsWith("observed") && !exact ? quantity + "*" : quantity;
    }

    private String capacityValue(CompoundTag tag, String key) {
        if (!tag.getBoolean("capacityKnown")) return "?";
        return key.endsWith("Net") ? signed(tag.getDouble(key) * SCALES[scaleIndex]) :
                amount(tag.getDouble(key) * SCALES[scaleIndex]);
    }

    private void drawIcon(GuiGraphics graphics, CompoundTag row, int x, int y) {
        ItemStack item = icon(row);
        if (!item.isEmpty()) {
            graphics.renderItem(item, x, y);
            return;
        }
        if (minecraft == null || minecraft.level == null || !row.getString("kind").equals("fluid")) return;
        var fluid = net.neoforged.neoforge.fluids.FluidStack.parseOptional(minecraft.level.registryAccess(),
                row.getCompound("icon"));
        if (fluid.isEmpty()) return;
        var extension = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid());
        var texture = extension.getStillTexture(fluid);
        if (texture == null) return;
        var sprite = minecraft.getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(texture);
        int color = extension.getTintColor(fluid);
        graphics.setColor(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F,
                ((color >>> 24) & 255) / 255F);
        graphics.blit(x, y, 0, 16, 16, sprite);
        graphics.setColor(1, 1, 1, 1);
    }

    private static String signed(double value) {
        return (value > 0 ? "+" : "") + amount(value);
    }

    private String ellipsis(String value, int pixels) {
        return font.width(value) <= pixels ? value :
                font.plainSubstrByWidth(value, Math.max(0, pixels - font.width("..."))) + "...";
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggedList >= 0) {
            scrollTo(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggedList >= 0) {
            draggedList = -1;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int scrollbarX(int list) {
        return left() + (list == 0 ? panelWidth() / 2 - 10 : panelWidth() - 13);
    }

    private int listTop(int list) {
        return list == 0 ? rowsTop() : detailRowsTop();
    }

    private int listSize(int list) {
        return list == 0 ? rows().size() : contributors().size();
    }

    private int listVisible(int list) {
        return list == 0 ? visibleRows() : visibleContributors();
    }

    private int maxScroll(int list) {
        return Math.max(0, listSize(list) - listVisible(list));
    }

    private int thumbHeight(int list) {
        int height = Math.max(0, rowBottom() - listTop(list));
        return Math.min(height, Math.max(12, height * listVisible(list) / Math.max(1, listSize(list))));
    }

    private int thumbY(int list) {
        int scroll = list == 0 ? rowScroll : contributorScroll;
        return listTop(list) + (int) Math.round((rowBottom() - listTop(list) - thumbHeight(list)) *
                clamp(scroll, 0, maxScroll(list)) / (double) Math.max(1, maxScroll(list)));
    }

    private void scrollTo(double mouseY) {
        int list = draggedList, travel = rowBottom() - listTop(list) - thumbHeight(list);
        int scroll = travel <= 0 ? 0 : clamp(
                (int) Math.round((mouseY - dragOffset - listTop(list)) / travel * maxScroll(list)), 0, maxScroll(list));
        if (list == 0) rowScroll = scroll;
        else contributorScroll = scroll;
    }

    private void drawScrollbar(GuiGraphics graphics, int list) {
        int start = listTop(list), end = rowBottom(), x = scrollbarX(list);
        if (end <= start) return;
        if (list == 0) rowScroll = clamp(rowScroll, 0, maxScroll(list));
        else contributorScroll = clamp(contributorScroll, 0, maxScroll(list));
        graphics.fill(x, start, x + 6, end, 0xFF202026);
        int y = maxScroll(list) == 0 ? start : thumbY(list),
                height = maxScroll(list) == 0 ? end - start : thumbHeight(list);
        graphics.fill(x + 1, y, x + 5, y + height, maxScroll(list) == 0 ? 0xFF484850 : 0xFFB8B8C0);
    }

    private int rowsTop() {
        return top() + 135;
    }

    private int visibleRows() {
        return Math.max(0, (rowBottom() - rowsTop()) / ROW_HEIGHT);
    }

    private int visibleContributors() {
        return Math.max(0, (rowBottom() - detailRowsTop()) / ROW_HEIGHT);
    }

    private int rowBottom() {
        return top() + panelHeight() - 12;
    }

    private int detailRowsTop() {
        return top() + 160;
    }

    private int panelWidth() {
        return Math.min(480, width - 12);
    }

    private int panelHeight() {
        return Math.min(330, height - 12);
    }

    private int left() {
        return (width - panelWidth()) / 2;
    }

    private int top() {
        return (height - panelHeight()) / 2;
    }
}
