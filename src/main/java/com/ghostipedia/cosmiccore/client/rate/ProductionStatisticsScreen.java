package com.ghostipedia.cosmiccore.client.rate;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.ProductionStatisticsPackets;
import com.ghostipedia.cosmiccore.common.production.ProductionStatisticsData;
import com.ghostipedia.cosmiccore.common.production.ProductionStatisticsRates;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductionStatisticsScreen extends Screen {

    private static final DecimalFormat COMPACT_AMOUNT = new DecimalFormat("#,##0.##");

    private static final String[] KINDS = { "", "item", "fluid", "ember", "soul", "energy" };
    private static final String[] WINDOWS = { "all", "5s", "1m", "10m", "1h", "10h", "25h", "50h", "100h",
            "250h", "500h", "750h", "1000h" };
    private static final String[] SORT_MODES = { "default", "id", "produced", "consumed" };
    private static final String[] SORT_ICONS = { "◆", "A", "+", "−" };
    private static final int[] COLORS = { 0xFF78C8FF, 0xFFFFB45E, 0xFFA5E887, 0xFFE394FF, 0xFFFF718D, 0xFFEBDF73,
            0xFF76E3D4, 0xFFC2A2FF };
    private CompoundTag report = new CompoundTag();
    private final List<String> selected = new ArrayList<>();
    private final List<Button> sortButtons = new ArrayList<>();
    private EditBox search;
    private int kindIndex;
    private int dimensionIndex;
    private int window = -1;
    private String sortMode = ProductionStatisticsData.DEFAULT_SORT_MODE;
    private boolean reverseSort;
    private int page;
    private int scroll;
    private long nextRefresh;
    private List<net.minecraft.util.FormattedCharSequence> rowTooltip;

    private ProductionStatisticsScreen(CompoundTag report) {
        super(Component.translatable("gui.cosmiccore.production_statistics.title"));
        if (!report.isEmpty()) accept(report);
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        minecraft.setScreen(new ProductionStatisticsScreen(new CompoundTag()));
    }

    public static void receive(CompoundTag report) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ProductionStatisticsScreen screen) screen.accept(report);
        else if (report.getBoolean("open")) minecraft.setScreen(new ProductionStatisticsScreen(report));
    }

    private void accept(CompoundTag value) {
        report = value.copy();
        window = Math.max(-1, Math.min(ProductionStatisticsData.WINDOWS.length - 1, value.getInt("window")));
        sortMode = ProductionStatisticsData.normalizeSortMode(value.getString("sort"));
        reverseSort = value.getBoolean("reverse");
        page = Math.max(0, value.getInt("page"));
        kindIndex = Math.max(0, indexOf(KINDS, value.getString("kind")));
        List<String> dimensions = dimensions();
        dimensionIndex = Math.max(0, dimensions.indexOf(value.getString("dimension")) + 1);
        selected.clear();
        ListTag tags = value.getList("selected", Tag.TAG_STRING);
        for (int i = 0; i < tags.size(); i++) selected.add(tags.getString(i));
        scroll = 0;
        updateSortButtons();
    }

    @Override
    protected void init() {
        int x = left() + 10;
        int y = top() + 30;
        int controlWidth = Math.max(60, (panelWidth() - 38) / 4);
        search = new EditBox(font, x, y, controlWidth, 18,
                Component.translatable("gui.cosmiccore.production_statistics.search"));
        search.setHint(Component.translatable("gui.cosmiccore.production_statistics.search"));
        search.setResponder(value -> {
            page = 0;
            nextRefresh = 0;
        });
        addRenderableWidget(search);
        addRenderableWidget(
                RateCalculatorButton.create(x + controlWidth + 6, y - 1, controlWidth, kindLabel(), button -> {
                    kindIndex = (kindIndex + 1) % KINDS.length;
                    page = 0;
                    button.setMessage(kindLabel());
                    request();
                }));
        addRenderableWidget(RateCalculatorButton.create(x + (controlWidth + 6) * 2, y - 1, controlWidth,
                windowLabel(), button -> {
                    window++;
                    if (window >= ProductionStatisticsData.WINDOWS.length) window = -1;
                    button.setMessage(windowLabel());
                    request();
                }));
        addRenderableWidget(RateCalculatorButton.create(x + (controlWidth + 6) * 3, y - 1,
                panelWidth() - 20 - (controlWidth + 6) * 3,
                dimensionLabel(), button -> {
                    dimensionIndex = (dimensionIndex + 1) % (dimensions().size() + 1);
                    page = 0;
                    button.setMessage(dimensionLabel());
                    request();
                }));
        sortButtons.clear();
        int sortY = y + 21;
        int sortWidth = 20;
        for (int i = 0; i < SORT_MODES.length; i++) {
            String mode = SORT_MODES[i];
            Button sortButton = RateCalculatorButton.create(x + i * (sortWidth + 4), sortY, sortWidth,
                    Component.literal(SORT_ICONS[i]), button -> {
                        if (sortMode.equals(mode)) reverseSort = !reverseSort;
                        else {
                            sortMode = mode;
                            reverseSort = false;
                        }
                        page = 0;
                        updateSortButtons();
                        request();
                    });
            sortButtons.add(sortButton);
            addRenderableWidget(sortButton);
        }
        updateSortButtons();
        addRenderableWidget(
                RateCalculatorButton.create(left() + 10, bottom() - 27, 45, Component.literal("<"), button -> {
                    if (page > 0) {
                        page--;
                        request();
                    }
                }));
        addRenderableWidget(
                RateCalculatorButton.create(left() + 59, bottom() - 27, 45, Component.literal(">"), button -> {
                    if (report.getBoolean("more")) {
                        page++;
                        request();
                    }
                }));
    }

    @Override
    public void tick() {
        if (minecraft != null && minecraft.level != null && minecraft.level.getGameTime() >= nextRefresh) {
            nextRefresh = minecraft.level.getGameTime() + 20;
            request();
        }
    }

    private void request() {
        CompoundTag query = new CompoundTag();
        query.putInt("window", window);
        query.putInt("page", page);
        query.putString("kind", KINDS[kindIndex]);
        query.putString("dimension", dimension());
        query.putString("search", search == null ? "" : search.getValue());
        query.putString("sort", sortMode);
        query.putBoolean("reverse", reverseSort);
        ListTag selection = new ListTag();
        selected.forEach(key -> selection.add(StringTag.valueOf(key)));
        query.put("selected", selection);
        CCoreNetwork.sendToServer(new ProductionStatisticsPackets.Request(query));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (mouseX < left() + panelWidth() / 2 && mouseY > top() + 86 && mouseY < bottom() - 32) {
            scroll = Math.max(0,
                    Math.min(Math.max(0, visibleRows().size() - rowCount()), scroll + (vertical < 0 ? 1 : -1)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        int row = (int) ((mouseY - top() - 88) / 22) + scroll;
        List<CompoundTag> rows = visibleRows();
        if (button == 0 && mouseX >= left() + 10 && mouseX < left() + panelWidth() / 2 - 4 && row >= 0 &&
                row < rows.size() && mouseY >= top() + 88 && mouseY < bottom() - 32) {
            String key = rows.get(row).getString("key");
            if (selected.remove(key) || selected.size() < 8 && selected.add(key)) request();
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        rowTooltip = null;
        renderBackground(graphics, mouseX, mouseY, partialTick);
        RateCalculatorPanelRenderer.draw(graphics, left(), top(), panelWidth(), panelHeight());
        graphics.drawString(font, title, left() + 12, top() + 12, 0xFFFFFF);
        String coverage = window < 0 ? Component.translatable("gui.cosmiccore.production_statistics.lifetime",
                ticks(report.getLong("lifetime"))).getString() : Component
                        .translatable(
                                "gui.cosmiccore.production_statistics.coverage", ticks(report.getLong("covered")))
                        .getString();
        graphics.drawString(font, coverage, left() + panelWidth() - 12 - font.width(coverage), top() + 12, 0xA8A8A8);
        drawRows(graphics, mouseX, mouseY);
        drawGraph(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("gui.cosmiccore.production_statistics.page", page + 1),
                left() + 112, bottom() - 21, 0xB8B8B8);
        Component partial = partialLabel();
        if (partial != null)
            graphics.drawString(font, partial, left() + panelWidth() - 12 - font.width(partial), bottom() - 21,
                    0xFFFF8A80);
        for (Renderable renderable : renderables) renderable.render(graphics, mouseX, mouseY, partialTick);
        if (rowTooltip != null) graphics.renderTooltip(font, rowTooltip, mouseX, mouseY);
    }

    private void drawRows(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = left() + 10, width = panelWidth() / 2 - 16;
        List<CompoundTag> rows = visibleRows();
        graphics.drawString(font, Component.translatable("gui.cosmiccore.production_statistics.columns"), x,
                top() + 76, 0xB8B8B8);
        graphics.enableScissor(x, top() + 87, x + width, bottom() - 32);
        for (int shown = 0; shown < rowCount(); shown++) {
            int index = shown + scroll;
            if (index >= rows.size()) break;
            CompoundTag row = rows.get(index);
            int y = top() + 88 + shown * 22;
            boolean chosen = selected.contains(row.getString("key"));
            RateCalculatorButton.drawRow(graphics, x, y, width, 20, chosen || mouseX >= x && mouseX < x + width &&
                    mouseY >= y && mouseY < y + 20);
            graphics.drawString(font, (chosen ? "◆ " : "◇ ") + name(row), x + 4, y + 3, 0xE8E8E8);
            if (row.getString("kind").equals("energy")) {
                String average = "-" + compactRate(row, "input") + "/+" + compactRate(row, "output") + " EU/t";
                graphics.drawString(font, average, x + width - 5 - font.width(average), y + 3, 0xFFFFD27A);
            }
            String amounts = "-" + compactAmount(row, "input") + "  +" + compactAmount(row, "output");
            graphics.drawString(font, amounts, x + width - 5 - font.width(amounts), y + 11, 0xA9D7FF);
            if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 20) {
                boolean energy = row.getString("kind").equals("energy");
                List<net.minecraft.util.FormattedCharSequence> tooltip = new ArrayList<>();
                tooltip.add(Component.literal(name(row)).getVisualOrderText());
                tooltip.add(Component.translatable(energy ? "gui.cosmiccore.production_statistics.precise_energy" :
                        "gui.cosmiccore.production_statistics.precise", amount(row.getString("input")),
                        amount(row.getString("output"))).getVisualOrderText());
                tooltip.add(Component.translatable(energy ? "gui.cosmiccore.production_statistics.rate_energy" :
                        "gui.cosmiccore.production_statistics.rate",
                        energy ? ratePerTick(row.getString("input")) :
                                rate(row.getString("input")),
                        energy ? ratePerTick(row.getString("output")) :
                                rate(row.getString("output")))
                        .getVisualOrderText());
                if (energy) tooltip.add(Component.translatable("gui.cosmiccore.production_statistics.energy_coverage",
                        report.getLong("energyStarted")).getVisualOrderText());
                rowTooltip = tooltip;
            }
        }
        graphics.disableScissor();
    }

    private void drawGraph(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = left() + panelWidth() / 2 + 4, y = top() + 88, width = panelWidth() / 2 - 14;
        int height = bottom() - y - 34;
        graphics.fill(x, y, x + width, y + height, 0x88202028);
        if (window < 0) {
            graphics.drawCenteredString(font,
                    Component.translatable("gui.cosmiccore.production_statistics.all_no_graph"),
                    x + width / 2, y + height / 2, 0xB8B8B8);
            return;
        }
        ListTag points = report.getList("graph", Tag.TAG_COMPOUND);
        BigDecimal max = BigDecimal.ONE;
        for (int i = 0; i < points.size(); i++) {
            CompoundTag point = points.getCompound(i);
            max = max.max(number(point.getString("input"))).max(number(point.getString("output")));
        }
        long cutoff = report.getLong("clock") - ProductionStatisticsData.WINDOWS[window] + 1;
        for (int i = 0; i < points.size(); i++) {
            CompoundTag point = points.getCompound(i);
            int color = COLORS[Math.max(0, selected.indexOf(point.getString("resource"))) % COLORS.length];
            int px = x + (int) Math.max(0, Math.min(width - 1,
                    (point.getLong("start") - cutoff) * width / ProductionStatisticsData.WINDOWS[window]));
            int half = Math.max(1, height / 2 - 8);
            int inputBar = bar(number(point.getString("input")), max, half);
            int outputBar = bar(number(point.getString("output")), max, half);
            int middle = y + height / 2;
            graphics.fill(px, middle - outputBar, Math.min(x + width, px + 2), middle, color);
            graphics.fill(px, middle, Math.min(x + width, px + 2), middle + inputBar,
                    0x88000000 | color & 0x00FFFFFF);
            if (mouseX >= px && mouseX < px + 3 && mouseY >= middle - outputBar && mouseY <= middle + inputBar) {
                graphics.renderTooltip(font, List.of(
                        Component.literal(nameForKey(point.getString("resource"))).getVisualOrderText(),
                        Component.literal("-" + amount(point.getString("input")) + "  +" +
                                amount(point.getString("output"))).getVisualOrderText(),
                        Component.translatable("gui.cosmiccore.production_statistics.interval", point.getLong("start"),
                                point.getLong("end")).getVisualOrderText()),
                        mouseX, mouseY);
            }
        }
        int legendY = y + 4;
        for (int i = 0; i < selected.size(); i++) {
            graphics.drawString(font, "◆ " + nameForKey(selected.get(i)), x + 5, legendY, COLORS[i % COLORS.length]);
            legendY += 10;
        }
    }

    private List<CompoundTag> visibleRows() {
        String term = search == null ? "" : search.getValue().toLowerCase(Locale.ROOT);
        ListTag tags = report.getList("rows", Tag.TAG_COMPOUND);
        List<CompoundTag> rows = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            CompoundTag row = tags.getCompound(i);
            if (term.isEmpty() || name(row).toLowerCase(Locale.ROOT).contains(term) ||
                    row.getString("id").toLowerCase(Locale.ROOT).contains(term))
                rows.add(row);
        }
        return rows;
    }

    private String name(CompoundTag row) {
        if (minecraft != null && minecraft.level != null && row.getString("kind").equals("item")) {
            ItemStack stack = ItemStack.parseOptional(minecraft.level.registryAccess(), row.getCompound("icon"));
            if (!stack.isEmpty()) return stack.getHoverName().getString();
        }
        ResourceLocation id = ResourceLocation.tryParse(row.getString("id"));
        if (id != null && row.getString("kind").equals("item"))
            return BuiltInRegistries.ITEM.getOptional(id).map(item -> new ItemStack(item).getHoverName().getString())
                    .orElse(row.getString("id"));
        if (id != null && row.getString("kind").equals("fluid"))
            return BuiltInRegistries.FLUID.getOptional(id)
                    .map(fluid -> fluid.getFluidType().getDescription().getString())
                    .orElse(row.getString("id"));
        if (row.getString("kind").equals("ember"))
            return Component.translatable("gui.cosmiccore.production_statistics.resource.ember").getString();
        if (row.getString("kind").equals("soul"))
            return Component.translatable("gui.cosmiccore.production_statistics.resource." + row.getString("id"))
                    .getString();
        if (row.getString("kind").equals("energy"))
            return Component.translatable("gui.cosmiccore.production_statistics.resource.eu").getString();
        return row.getString("id");
    }

    private String nameForKey(String key) {
        for (CompoundTag row : visibleRows()) if (row.getString("key").equals(key)) return name(row);
        String[] parts = key.split("\\u0000", 3);
        return parts.length > 1 ? parts[1] : key;
    }

    private List<String> dimensions() {
        ListTag tags = report.getList("dimensions", Tag.TAG_STRING);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) result.add(tags.getString(i));
        return result;
    }

    private String dimension() {
        return dimensionIndex == 0 || dimensionIndex - 1 >= dimensions().size() ? "" :
                dimensions().get(dimensionIndex - 1);
    }

    private Component dimensionLabel() {
        return dimension().isEmpty() ?
                Component.translatable("gui.cosmiccore.production_statistics.global") : Component.literal(dimension());
    }

    private Component kindLabel() {
        return Component.translatable("gui.cosmiccore.production_statistics.kind." +
                (KINDS[kindIndex].isEmpty() ? "all" : KINDS[kindIndex]));
    }

    private Component windowLabel() {
        return Component.literal(WINDOWS[window + 1]);
    }

    private Component sortLabel(String mode) {
        return Component.translatable("gui.cosmiccore.production_statistics.sort." + mode);
    }

    private void updateSortButtons() {
        for (int i = 0; i < sortButtons.size(); i++) {
            Button button = sortButtons.get(i);
            boolean selectedMode = SORT_MODES[i].equals(sortMode);
            button.active = true;
            button.setMessage(Component.literal(SORT_ICONS[i] + (selectedMode ? reverseSort ? "↑" : "↓" : "")));
            button.setTooltip(sortTooltip(SORT_MODES[i], selectedMode));
        }
    }

    private Tooltip sortTooltip(String mode, boolean selectedMode) {
        boolean shownReverse = selectedMode && reverseSort;
        Component tooltip = Component.empty()
                .append(sortLabel(mode).copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append("\n")
                .append(Component.translatable("gui.cosmiccore.production_statistics.sort.order." + mode + "." +
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

    private int rowCount() {
        return Math.max(1, (panelHeight() - 123) / 22);
    }

    private int panelWidth() {
        return Math.max(300, Math.min(620, width - 12));
    }

    private int panelHeight() {
        return Math.max(210, Math.min(400, height - 12));
    }

    private int left() {
        return (width - panelWidth()) / 2;
    }

    private int top() {
        return (height - panelHeight()) / 2;
    }

    private int bottom() {
        return top() + panelHeight();
    }

    private static int indexOf(String[] values, String value) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(value)) return i;
        return 0;
    }

    private Component partialLabel() {
        boolean input = report.getBoolean("partialInput");
        boolean output = report.getBoolean("partialOutput");
        if (!input && !output) return null;
        return Component.translatable("gui.cosmiccore.production_statistics.partial_" +
                (input && output ? "both" : input ? "input" : "output"));
    }

    private static BigDecimal number(String value) {
        try {
            BigDecimal decimal = new BigDecimal(value);
            return decimal.signum() < 0 ? BigDecimal.ZERO : decimal;
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private static int bar(BigDecimal value, BigDecimal max, int height) {
        if (value.signum() == 0) return 0;
        return Math.max(1, value.multiply(BigDecimal.valueOf(height)).divide(max, 0, RoundingMode.DOWN).intValue());
    }

    private static String amount(String value) {
        BigDecimal amount = number(value);
        return amount.signum() == 0 ? "0" : amount.stripTrailingZeros().toPlainString();
    }

    private static String compactAmount(CompoundTag row, String key) {
        double value = number(row.getString(key)).doubleValue();
        boolean fluid = row.getString("kind").equals("fluid");
        boolean energy = row.getString("kind").equals("energy");
        if (!Double.isFinite(value) || value != 0 && value < 0.01)
            return amount(row.getString(key)) + (fluid ? "mB" : energy ? " EU" : "");
        return FormattingUtil.formatNumberReadable(value, fluid, COMPACT_AMOUNT, fluid ? "B" : null) +
                (energy ? " EU" : "");
    }

    private String compactRate(CompoundTag row, String key) {
        double value = perTick(row.getString(key), true).doubleValue();
        if (!Double.isFinite(value)) return perTick(row.getString(key), true).stripTrailingZeros().toPlainString();
        return FormattingUtil.formatNumberReadable(value, false, COMPACT_AMOUNT, null);
    }

    private String rate(String micros) {
        long ticks = window < 0 ? report.getLong("lifetime") : report.getLong("covered");
        if (ticks <= 0) return "0";
        return number(micros).multiply(BigDecimal.valueOf(1200))
                .divide(BigDecimal.valueOf(ticks), 6, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    private BigDecimal perTick(String value, boolean energy) {
        long ticks = energy ? report.getLong("energyCovered") :
                window < 0 ? report.getLong("lifetime") : report.getLong("covered");
        return ProductionStatisticsRates.perTick(number(value), ticks);
    }

    private String ratePerTick(String value) {
        return perTick(value, true).stripTrailingZeros().toPlainString() + " EU";
    }

    private static String ticks(long ticks) {
        return String.format(Locale.ROOT, "%.1fs", ticks / 20.0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
