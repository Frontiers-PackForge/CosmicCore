package com.ghostipedia.cosmiccore.integration.recipes.emi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.qualityfood.QualityFoodCompat;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.AeroponicsCrops;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.AeroponicsTower;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.WidgetGroup;
import dev.emi.emi.widget.RecipeBackground;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

public final class AeroponicsEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 174;
    private static final int HEADER_HEIGHT = 116;
    private static final int ROW_HEIGHT = 11;
    private static final String LANG = "cosmiccore.aeroponics.emi.";
    private static final EmiStack TOWER = EmiStack.of(AeroponicsTower.AEROPONICS_TOWER.asStack());
    private static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(CosmicCore.id("aeroponics"), TOWER) {

        @Override
        public Component getName() {
            return TOWER.getName();
        }
    };

    private final AeroponicsCrops.Catalog catalog;
    private final Item seed;
    private final boolean supportsQuality;
    private final Selection selection;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> catalysts;
    private final List<EmiStack> outputs;
    private Preview preview;
    private int version = -1;

    private AeroponicsEmiRecipe(AeroponicsCrops.Catalog catalog, Item seed, Selection selection,
                                List<EmiIngredient> inputs) {
        this.catalog = catalog;
        this.seed = seed;
        this.selection = selection;
        this.inputs = inputs;
        var key = BuiltInRegistries.ITEM.getKey(seed);
        id = CosmicCore.id("/aeroponics/" + key.getNamespace() + "/" + key.getPath());
        catalysts = List.of(EmiStack.of(seed));
        var baseSeed = new ItemStack(seed);
        supportsQuality = QualityFoodCompat.supportsQuality(baseSeed);
        outputs = catalog.resolve(baseSeed).harvest().stream().map(EmiStack::of).toList();
    }

    public static void register(EmiRegistry registry) {
        var catalog = AeroponicsCrops.clientCatalog();
        var selection = new Selection();
        var inputs = new ArrayList<EmiIngredient>();
        BuiltInRegistries.FLUID.getOptional(AeroponicsTower.NUTRIENT)
                .ifPresent(fluid -> inputs.add(EmiStack.of(fluid)));
        inputs.add(EmiIngredient.of(AeroponicsTower.Fertilizer.TIERS.stream()
                .map(tier -> EmiStack.of(tier.item().asStack())).toList()));
        var feed = List.copyOf(inputs);
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, TOWER);
        catalog.seeds().stream().sorted(Comparator.comparing(BuiltInRegistries.ITEM::getKey))
                .forEach(seed -> registry.addRecipe(new AeroponicsEmiRecipe(catalog, seed, selection, feed)));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEADER_HEIGHT + preview().traits.size() * ROW_HEIGHT;
    }

    static final class Selection {

        private final Set<String> selected = new LinkedHashSet<>();
        private int quality, version;

        void cycle(int direction) {
            quality = Math.floorMod(quality + direction, QualityFoodCompat.TIERS.size());
            version++;
        }

        int qualityFor(boolean supportsQuality) {
            return supportsQuality ? quality : 0;
        }

        void toggle(String id) {
            if (!selected.remove(id)) selected.add(id);
            version++;
        }

        List<String> active(Set<String> applicable) {
            return selected.stream().filter(applicable::contains).toList();
        }
    }

    private record Preview(EmiStack seed, List<EmiStack> harvest, List<Component> costs, Component quality,
                           ResourceLocation star, List<Component> traits) {}

    private static Component label(String key, Object... args) {
        return Component.translatable(LANG + key, args);
    }

    private static Component traitName(String id) {
        return Component.translatableWithFallback("mutation." + id.replace(':', '.'), id);
    }

    private Preview preview() {
        if (version == selection.version) return preview;
        var applicable = catalog.applicable(seed);
        var active = selection.active(applicable);
        int tier = selection.qualityFor(supportsQuality);
        var stack = catalog.previewSeed(seed, tier, active);
        var cycle = catalog.resolve(stack);
        var rates = cycle.rates();
        var numbers = NumberFormat.getNumberInstance();
        numbers.setMaximumFractionDigits(4);
        var costs = List.of(label("mb", numbers.format(rates.nutrient())),
                label("seconds", numbers.format(rates.duration())),
                label("units", numbers.format(rates.fertilizer())));
        var names = new ArrayList<>(selection.selected.stream().map(id -> applicable.contains(id) ? traitName(id) :
                label("inactive", traitName(id))).toList());
        names.addAll(cycle.requirements());
        preview = new Preview(EmiStack.of(stack), cycle.harvest().stream().map(EmiStack::of).toList(), costs,
                supportsQuality ? label("quality." + QualityFoodCompat.TIERS.get(tier)) : label("normal_tier_only"),
                QualityFoodCompat.star(tier), names.isEmpty() ? List.of(label("none")) : names);
        version = selection.version;
        return preview;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var font = Minecraft.getInstance().font;
        int height = Math.min(HEADER_HEIGHT, widgets.getHeight() - ROW_HEIGHT);
        int qualityY = height - 34, mutationY = height - 17;
        int costY = qualityY - 34;
        int slotY = 1;
        int seedX = 24;
        widgets.add(new PreviewSlot(seedX, slotY, () -> preview().seed)).catalyst(true);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, slotY + 1);
        var displayed = new ArrayList<EmiStack>();
        displayed.add(EmiStack.of(seed));
        displayed.addAll(outputs);
        for (int i = 0; i < outputs.size(); i++) {
            int index = i;
            widgets.add(new PreviewSlot(outputX(i), slotY, () -> preview().harvest.get(index))).recipeContext(this);
        }

        if (costY >= 31) {
            for (int i = 0; i < displayed.size(); i++) {
                int center = (i == 0 ? seedX : outputX(i - 1)) + 9;
                int maxWidth = i == 0 ? 63 : outputs.size() > 1 ? 45 : 83;
                var lines = font.split(displayed.get(i).getName(), maxWidth);
                int maxLines = Math.min(2, (costY - 20) / 9);
                for (int line = 0; line < Math.min(lines.size(), maxLines); line++) {
                    var text = lines.get(line);
                    widgets.addText(text, center - font.width(text) / 2, 21 + 9 * line, 0x404040, false);
                }
            }
        }
        var costLabels = List.of(label("nutrient"), label("time"), label("fertilizer"));
        for (int i = 0; i < costLabels.size(); i++) {
            widgets.addText(costLabels.get(i), 2, costY + i * 11, 0x404040, false);
        }
        var qualityLabel = label("quality");
        int editWidth = 35;
        int controlsRight = WIDTH - 2, controlsLeft = controlsRight - editWidth;
        int arrowWidth = 16;
        widgets.addText(qualityLabel, 2, qualityY + 3, 0x404040, false);
        widgets.addText(label("mutations"), 2, mutationY + 3, 0x404040, false);
        widgets.addDrawable(0, 0, 0, 0, (g, mx, my, delta) -> {
            Preview p = preview();
            for (int i = 0; i < p.costs.size(); i++) {
                var cost = p.costs.get(i);
                g.drawString(font, cost, WIDTH - 2 - font.width(cost), costY + 11 * i, 0x404040, false);
            }
            int tierRight = supportsQuality ? controlsLeft - 4 : controlsRight;
            int tierLeft = 2 + font.width(qualityLabel) + 4;
            int nameWidth = font.width(p.quality);
            int tierWidth = nameWidth + (p.star == null ? 0 : 10);
            float scale = Math.clamp((float) (tierRight - tierLeft) / Math.max(1, tierWidth), 0f, 1f);
            g.pose().pushPose();
            g.pose().translate(tierRight, qualityY + 3 + (1 - scale) * font.lineHeight / 2, 0);
            g.pose().scale(scale, scale, 1);
            if (p.star != null) g.blitSprite(p.star, -tierWidth, 0, 16, 16);
            g.drawString(font, p.quality, -nameWidth, 0, 0x404040, false);
            g.pose().popPose();
        });
        if (supportsQuality) {
            widgets.add(
                    new Control(controlsLeft, qualityY, arrowWidth, Component.literal("<"), () -> selection.cycle(-1)));
            widgets.add(new Control(controlsRight - arrowWidth, qualityY, arrowWidth, Component.literal(">"),
                    () -> selection.cycle(1)));
        }
        widgets.add(new MutationList(height, widgets.getHeight() - height));
        if (widgets instanceof WidgetGroup group) {
            widgets.add(new Control(controlsLeft, mutationY, editWidth, label("edit"),
                    () -> Minecraft.getInstance().pushGuiLayer(new MutationPicker(group))));
        }
    }

    private int outputX(int index) {
        return outputs.size() == 1 ? 116 : 96 + 46 * index;
    }

    private final class MutationList extends Widget implements GuiEventListener {

        private static final int BAR_WIDTH = 6;
        private static final ResourceLocation TRACK = ResourceLocation
                .withDefaultNamespace("widget/scroller_background");
        private static final ResourceLocation THUMB = ResourceLocation.withDefaultNamespace("widget/scroller");
        private final Bounds bounds;
        private final int capacity;
        private double offset;
        private boolean focused;

        private MutationList(int top, int height) {
            bounds = new Bounds(2, top, WIDTH - 4, height);
            capacity = Math.max(1, height / ROW_HEIGHT);
        }

        @Override
        public Bounds getBounds() {
            return bounds;
        }

        private int overflow() {
            return Math.max(0, preview().traits.size() - capacity);
        }

        private int thumbHeight() {
            return Math.max(5, bounds.height() * capacity / preview().traits.size());
        }

        @Override
        public void render(GuiGraphics g, int mx, int my, float delta) {
            var font = Minecraft.getInstance().font;
            var traits = preview().traits;
            int max = overflow();
            offset = Math.clamp(offset, 0, max);
            int right = bounds.right();
            if (max > 0) {
                int h = thumbHeight();
                right -= BAR_WIDTH;
                g.blitSprite(TRACK, right, bounds.y(), BAR_WIDTH, bounds.height());
                g.blitSprite(THUMB, right, bounds.y() + (int) Math.round(offset * (bounds.height() - h) / max),
                        BAR_WIDTH, h);
                right -= 3;
            }
            int first = (int) Math.round(offset);
            for (int i = first; i < Math.min(first + capacity, traits.size()); i++) {
                var text = traits.get(i);
                int width = right - bounds.x();
                var line = font.width(text) <= width ? text.getVisualOrderText() :
                        Language.getInstance().getVisualOrder(font.substrByWidth(text, width));
                g.drawString(font, line, bounds.x(),
                        bounds.y() + (i - first) * ROW_HEIGHT + 1, 0x404040, false);
            }
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int x, int y) {
            int row = (y - bounds.y()) / ROW_HEIGHT;
            int i = (int) Math.round(offset) + row;
            var traits = preview().traits;
            return row < capacity && i < traits.size() && (overflow() == 0 || x < bounds.right() - BAR_WIDTH) ?
                    List.of(ClientTooltipComponent.create(traits.get(i).getVisualOrderText())) : List.of();
        }

        @Override
        public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
            int max = overflow();
            if (max == 0 || vertical == 0) return false;
            offset = Math.clamp(offset - vertical, 0, max);
            return true;
        }

        @Override
        public boolean mouseClicked(int x, int y, int button) {
            int max = overflow();
            if (button != 0 || max == 0 || x < bounds.right() - BAR_WIDTH ||
                    !(Minecraft.getInstance().screen instanceof RecipeScreen screen))
                return false;
            int h = thumbHeight(), travel = bounds.height() - h;
            int top = bounds.y() + (int) Math.round(offset * travel / max);
            if (y < top || y >= top + h) offset = Math.clamp((y - bounds.y() - h / 2.0) * max / travel, 0, max);
            screen.setFocused(this);
            screen.setDragging(true);
            return true;
        }

        @Override
        public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
            int max = overflow();
            if (button != 0 || max == 0) return false;
            offset = Math.clamp(offset + dy * max / (bounds.height() - thumbHeight()), 0, max);
            return true;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }
    }

    private static final class PreviewSlot extends SlotWidget {

        private final Supplier<EmiStack> displayed;

        private PreviewSlot(int x, int y, Supplier<EmiStack> displayed) {
            super(EmiStack.EMPTY, x, y);
            this.displayed = displayed;
        }

        @Override
        public EmiIngredient getStack() {
            return displayed.get();
        }
    }

    private static final class Control extends Widget {

        private final Button button;
        private final Bounds bounds;

        private Control(int x, int y, int width, Component title, Runnable click) {
            bounds = new Bounds(x, y, width, 14);
            button = Button.builder(title, b -> click.run()).bounds(x, y, width, 14).build();
        }

        @Override
        public Bounds getBounds() {
            return bounds;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, float delta) {
            button.render(g, x, y, delta);
        }

        @Override
        public boolean mouseClicked(int x, int y, int which) {
            return button.mouseClicked(x, y, which);
        }
    }

    private final class MutationPicker extends Screen {

        private final int offsetX, offsetY, panelHeight;
        private final int previousHeight = getDisplayHeight();
        private final List<MutationButton> rows = new ArrayList<>();
        private final List<String> entries;
        private List<String> filtered = List.of();
        private EditBox search;
        private Button selected, up, down;
        private RecipeBackground background;
        private boolean selectedOnly;
        private int start, x, y;

        private MutationPicker(WidgetGroup page) {
            super(label("mutations"));
            var window = Minecraft.getInstance().getWindow();
            offsetX = page.x() - window.getGuiScaledWidth() / 2;
            offsetY = page.y() - window.getGuiScaledHeight() / 2;
            panelHeight = page.getHeight();
            entries = catalog.applicable(seed).stream().sorted(Comparator.comparing(id -> traitName(id).getString()))
                    .toList();
        }

        @Override
        protected void init() {
            String query = search == null ? "" : search.getValue();
            x = Math.clamp(width / 2 + offsetX, 4, Math.max(4, width - WIDTH - 4));
            y = Math.clamp(height / 2 + offsetY, 4, Math.max(4, height - panelHeight - 4));
            background = new RecipeBackground(x - 4, y - 4, WIDTH + 8, panelHeight + 8);
            search = addRenderableWidget(new EditBox(font, x + 3, y + 3, WIDTH - 80, 16, label("search")));
            var hint = Component.translatable("gui.recipebook.search_hint");
            search.setHint(Component.literal(font.plainSubstrByWidth(hint.getString(), search.getInnerWidth()))
                    .withStyle(ChatFormatting.GRAY));
            search.setTooltip(Tooltip.create(label("search")));
            search.setValue(query);
            selected = button(x + WIDTH - 74, y + 3, 72, 16, CommonComponents.EMPTY, b -> {
                selectedOnly = !selectedOnly;
                start = 0;
                refresh();
            });
            rows.clear();
            int capacity = Math.max(1, (panelHeight - 43) / 20);
            for (int i = 0; i < capacity; i++) {
                int row = i;
                rows.add(addRenderableWidget(new MutationButton(x + 2, y + 22 + i * 20, WIDTH - 21, b -> {
                    selection.toggle(filtered.get(start + row));
                    refresh();
                })));
            }
            up = button(x + WIDTH - 17, y + 22, 15, 14, Component.literal("▲"), b -> scroll(-1));
            down = button(x + WIDTH - 17, y + 37, 15, 14, Component.literal("▼"), b -> scroll(1));
            button(x + WIDTH - 42, y + panelHeight - 17, 40, 15, CommonComponents.GUI_DONE, b -> onClose());
            search.setResponder(q -> {
                start = 0;
                refresh();
            });
            refresh();
            setInitialFocus(search);
        }

        private Button button(int left, int top, int width, int height, Component text, Button.OnPress action) {
            return addRenderableWidget(Button.builder(text, action).bounds(left, top, width, height).build());
        }

        private Component effect(String id) {
            return catalog.mutation(id).description();
        }

        private void refresh() {
            String query = search.getValue().strip().toLowerCase(Locale.ROOT);
            var candidates = selectedOnly ? selection.selected.stream() : entries.stream();
            filtered = candidates.filter(id -> (traitName(id).getString() + " " + effect(id).getString())
                    .toLowerCase(Locale.ROOT).contains(query)).toList();
            start = Math.clamp(start, 0, Math.max(0, filtered.size() - rows.size()));
            selected.setMessage(label("selected", selection.selected.size()));
            selected.setFGColor(selectedOnly ? 0xAAFFAA : 0xFFFFFF);
            for (int i = 0; i < rows.size(); i++) {
                MutationButton row = rows.get(i);
                row.visible = start + i < filtered.size();
                if (!row.visible) continue;
                String id = filtered.get(start + i);
                Component name = traitName(id);
                if (!catalog.applicable(seed).contains(id)) name = label("inactive", name);
                row.checked = selection.selected.contains(id);
                row.setMessage(name);
                row.setTooltip(Tooltip.create(name.copy().append("\n").append(effect(id))));
            }
            up.active = start > 0;
            down.active = start + rows.size() < filtered.size();
        }

        private void scroll(int by) {
            start += by;
            refresh();
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
            if (mx >= x && mx < x + WIDTH && my >= y + 20 && my < y + panelHeight - 18 && vertical != 0) {
                scroll(vertical > 0 ? -1 : 1);
                return true;
            }
            return super.mouseScrolled(mx, my, horizontal, vertical);
        }

        @Override
        public void renderBackground(GuiGraphics g, int mx, int my, float delta) {
            background.render(g, mx, my, delta);
            g.drawString(font, font.plainSubstrByWidth(seed.getDescription().getString(), WIDTH - 49),
                    x + 3, y + panelHeight - 13, 0x404040, false);
            if (filtered.isEmpty()) g.drawString(font, label("no_matches"), x + 4, y + 26, 0x404040, false);
        }

        @Override
        public void onClose() {
            minecraft.popGuiLayer();
            if (previousHeight != getDisplayHeight() && minecraft.screen instanceof RecipeScreen screen) {
                screen.resize(minecraft, screen.width, screen.height);
                screen.focusRecipe(AeroponicsEmiRecipe.this);
            }
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }

    private static final class MutationButton extends Button {

        private static final ResourceLocation CHECK = GTCEu.id("textures/gui/widget/button_check.png");
        private static final ResourceLocation CLIPBOARD_BUTTONS = GTCEu.id("textures/gui/widget/clipboard_button.png");
        private boolean checked;

        private MutationButton(int x, int y, int width, OnPress press) {
            super(x, y, width, 19, CommonComponents.EMPTY, press, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mx, int my, float delta) {
            g.blitSprite(SPRITES.get(active, isHoveredOrFocused()), getX() + 18, getY(), getWidth() - 18, getHeight());
            g.blitSprite(SPRITES.get(checked, false), getX(), getY() + 1, 16, 16);
            if (checked) {
                g.blit(CHECK, getX() + 1, getY() + 2, 14, 14, 0, 0, 16, 16, 16, 16);
            } else {
                g.blit(CLIPBOARD_BUTTONS, getX() + 3, getY() + 4, 2, 47, 11, 11, 15, 60);
            }
            renderString(g, Minecraft.getInstance().font, 0xFFFFFFFF);
        }

        @Override
        public void renderString(GuiGraphics g, Font font, int color) {
            int left = getX() + 22;
            renderScrollingString(g, font, getMessage(), left, left, getY(), getRight() - 4, getBottom(), color);
        }

        @Override
        protected MutableComponent createNarrationMessage() {
            return CommonComponents.optionStatus(getMessage(), checked);
        }
    }
}
