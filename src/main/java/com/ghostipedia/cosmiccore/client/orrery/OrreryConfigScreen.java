package com.ghostipedia.cosmiccore.client.orrery;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.packet.OrreryPackets;
import com.ghostipedia.cosmiccore.common.orrery.*;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IDraggable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.IntValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class OrreryConfigScreen extends ModularScreen {

    private final StringValue name = new StringValue("");
    private final DynamicHandler catalogue = new DynamicHandler();
    private List<UUID> visibleDesigns = List.of();
    private String search = "";
    private int bar, sort, page;
    private boolean descending, nameLoaded, listChanged = true;
    private int volume;
    private boolean showPreview, showHud, settingsLoaded;
    private long searchChangedAt = -1;
    private UUID tool;

    public OrreryConfigScreen() {
        this(null);
    }

    public OrreryConfigScreen(View view) {
        super(CosmicCore.MOD_ID, ModularPanel.defaultPanel("leyline_orrery_config"));
        useTheme(GTGuiTheme.STANDARD.getId());
        pausesGame(false);
        openParentOnClose(false);
        var state = OrreryClient.state();
        if (state != null) {
            bar = state.active;
            tool = OrreryClient.toolId();
        }
        if (view != null) {
            bar = view.bar();
            sort = view.sort();
            page = view.page();
            descending = view.descending();
            search = view.search();
        }
        loadSettings();
        buildPanel();
        updateName();
    }

    public record View(int bar, int sort, int page, boolean descending, String search) {}

    public View view() {
        return new View(bar, sort, page, descending, search);
    }

    public static OrreryConfigScreen current() {
        return ModularScreen.getCurrent() instanceof OrreryConfigScreen screen ? screen : null;
    }

    private void buildPanel() {
        var panel = getMainPanel();
        var window = Minecraft.getInstance().getWindow();
        int width = Math.min(420, window.getGuiScaledWidth() - 16);
        int height = Math.min(306, window.getGuiScaledHeight() - 24);
        panel.size(width, height);
        panel.child(Text.dynamic(() -> OrreryClient.text("title")).asWidget().pos(9, 8).size(width - 65, 10));
        panel.child(Text.dynamic(OrreryClient::status).asWidget().pos(9, 21).size(width - 18, 10));
        panel.child(ButtonWidget.panelCloseButton().size(16));
        panel.child(new ButtonWidget<>().pos(width - 40, 4).size(16).padding(1).overlay(GTGuiTextures.INFO)
                .playClickSound(false).tooltip(t -> LangHandler.getMultiLang("cosmiccore.orrery.help")
                        .forEach(t::addLine)));
        int tabWidth = (width - 18) / OrreryState.LOADOUTS;
        for (int index = 0; index < OrreryState.LOADOUTS; index++) {
            int target = index;
            panel.child(button(9 + index * tabWidth, 35, tabWidth - 2, () -> {
                var state = OrreryClient.state();
                return shortened(state == null ? OrreryClient.text("loadout", target + 1) : state.label(target),
                        tabWidth - 10);
            }, () -> {
                saveName();
                bar = target;
                updateName();
                OrreryClient.sound(OrreryClient.Feedback.BAR);
            }).tooltip(t -> {
                var state = OrreryClient.state();
                if (state != null) t.addLine(state.label(target));
            }).tooltipAutoUpdate(true)
                    .background(new DynamicDrawable(() -> bar == target ? GTGuiTextures.SLOT : GTGuiTextures.BUTTON)));
        }
        panel.child(new TextFieldWidget().value(name).autoUpdateOnChange(true).setMaxLength(32)
                .hintText(OrreryClient.text("loadout_name")).pos(9, 57).size(width - 88, 18));
        panel.child(button(width - 74, 57, 65, () -> OrreryClient.text("rename"), this::saveName));
        int slotWidth = (width - 18) / OrreryState.SLOTS;
        for (int slot = 0; slot < OrreryState.SLOTS; slot++) {
            panel.child(new DesignWidget(slot, null).pos(9 + slot * slotWidth, 81).size(slotWidth - 3, 30));
        }
        panel.child(new TextFieldWidget().value(new StringValue.Dynamic(() -> search, value -> {
            search = value;
            page = 0;
            searchChangedAt = net.minecraft.Util.getMillis();
        })).autoUpdateOnChange(true).setMaxLength(80).hintText(OrreryClient.text("search"))
                .pos(9, 117).size(width - 18, 18));
        int sortWidth = (width - 122) / 2;
        panel.child(button(9, 139, sortWidth, () -> OrreryClient.text("sort." + sort), () -> {
            sort = (sort + 1) % 3;
            page = 0;
            OrreryClient.query();
        }));
        panel.child(button(13 + sortWidth, 139, sortWidth,
                () -> OrreryClient.text(descending ? "descending" : "ascending"), () -> {
                    descending = !descending;
                    page = 0;
                    OrreryClient.query();
                }));
        panel.child(button(width - 99, 139, 18, () -> Component.literal("<"), () -> changePage(-1)));
        panel.child(Text.dynamic(() -> Component.literal((page + 1) + "/" + pages())).asWidget()
                .pos(width - 78, 143).size(46, 10));
        panel.child(button(width - 27, 139, 18, () -> Component.literal(">"), () -> changePage(1)));
        catalogue.widgetProvider(() -> buildCatalogue(width - 18, height - 191));
        panel.child(new DynamicWidget<>().clientOnlyHandler(catalogue).pos(9, 162).size(width - 18, height - 191));
        panel.child(Text.dynamic(() -> OrreryClient.text("sound")).asWidget()
                .pos(9, height - 20).size(36, 10));
        panel.child(new TextFieldWidget().value(new IntValue.Dynamic(() -> volume,
                value -> settings(value, showPreview, showHud)))
                .setNumbers(0, 100).acceptsExpressions(false).setMaxLength(3).setPattern(Pattern.compile("[0-9]*"))
                .pos(49, height - 24).size(32, 18));
        panel.child(Text.str("%").asWidget().pos(84, height - 20).size(8, 10));
        int settingWidth = (width - 108) / 2;
        panel.child(button(99, height - 24, settingWidth - 3,
                () -> OrreryClient.text("preview", Component.translatable(showPreview ? "options.on" : "options.off")),
                () -> settings(volume, !showPreview, showHud)));
        panel.child(button(99 + settingWidth, height - 24, settingWidth - 3,
                () -> OrreryClient.text("hud", Component.translatable(showHud ? "options.on" : "options.off")),
                () -> settings(volume, showPreview, !showHud)));
    }

    private ListWidget<?, ?> buildCatalogue(int width, int height) {
        var list = new ListWidget<>().size(width, height).padding(3).background(GTGuiTextures.BACKGROUND_INVERSE);
        var entries = OrreryClient.catalogue(view());
        if (entries.isEmpty()) {
            list.child(Text.dynamic(() -> OrreryClient.text(OrreryClient.live() ? "empty_catalogue" : "connect_hint"))
                    .asWidget().width(width - 12).height(Math.max(18, height - 6)));
        } else {
            for (var design : entries) list.child(new DesignWidget(-1, design).width(width - 14).height(30));
        }
        return list;
    }

    private ButtonWidget<?> button(int x, int y, int width, Supplier<Component> label, Runnable action) {
        return new ButtonWidget<>().pos(x, y).size(width, 18).padding(2)
                .overlay(Text.dynamic(() -> shortened(label.get(), width - 6)))
                .playClickSound(false).onMousePressed((context, button) -> {
                    if (button != 0) return false;
                    action.run();
                    return true;
                });
    }

    private static Component shortened(Component text, int width) {
        var font = Minecraft.getInstance().font;
        return font.width(text) <= width ? text :
                Component
                        .literal(font.plainSubstrByWidth(text.getString(), Math.max(0, width - font.width("…"))) + "…");
    }

    private void settings(int volume, boolean preview, boolean hud) {
        var state = OrreryClient.state();
        if (!settingsLoaded || state == null || !Objects.equals(tool, state.id)) return;
        volume = Math.clamp(volume, 0, 100);
        if (this.volume == volume && showPreview == preview && showHud == hud) return;
        this.volume = volume;
        showPreview = preview;
        showHud = hud;
        OrreryClient.send(OrreryPackets.Action.SETTINGS, 0, 0, null, "", volume, preview, hud);
    }

    private void loadSettings() {
        var state = OrreryClient.state();
        if (!settingsLoaded && state != null && tool != null && tool.equals(state.id)) {
            volume = state.volume;
            showPreview = state.showPreview;
            showHud = state.showHud;
            settingsLoaded = true;
        }
    }

    private void changePage(int direction) {
        page = Math.clamp(page + direction, 0, pages() - 1);
        OrreryClient.query();
    }

    private int pages() {
        return Math.max(1,
                (OrreryClient.catalogueTotal(view()) + OrreryPackets.PAGE_SIZE - 1) / OrreryPackets.PAGE_SIZE);
    }

    public String query() {
        return search;
    }

    public int sort() {
        return sort;
    }

    public int page() {
        return page;
    }

    public boolean descending() {
        return descending;
    }

    private void saveName() {
        var state = OrreryClient.state();
        if (nameLoaded && state != null && Objects.equals(tool, state.id) &&
                !name.getStringValue().equals(state.name(bar)))
            OrreryClient.send(OrreryPackets.Action.RENAME, bar, 0, null, name.getStringValue(), 0, false, false);
    }

    private void updateName() {
        var state = OrreryClient.state();
        if (state != null && tool != null && tool.equals(state.id)) {
            name.setStringValue(state.name(bar));
            nameLoaded = true;
        }
    }

    public void received() {
        var state = OrreryClient.state();
        if (tool == null && state != null) tool = state.id;
        loadSettings();
        if (!nameLoaded) updateName();
        var snapshot = OrreryClient.snapshot();
        if (snapshot != null) {
            page = snapshot.pageIndex();
            var ids = OrreryClient.catalogue(view()).stream().map(OrreryDesign::id).toList();
            if (!ids.equals(visibleDesigns)) {
                visibleDesigns = ids;
                listChanged = true;
            }
        }
    }

    @Override
    public void onOpen() {
        super.onOpen();
        OrreryClient.query();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        var state = OrreryClient.state();
        if (OrreryClient.heldHand() == null || tool != null && state != null && !tool.equals(state.id)) {
            close();
            return;
        }
        if (listChanged && !getContext().hasDraggable()) {
            listChanged = false;
            catalogue.notifyUpdate();
        }
        if (searchChangedAt >= 0 && net.minecraft.Util.getMillis() - searchChangedAt >= 180) {
            searchChangedAt = -1;
            OrreryClient.query();
        }
    }

    @Override
    public void onClose() {
        getContext().removeFocus();
        saveName();
        super.onClose();
    }

    private final class DesignWidget extends ButtonWidget<DesignWidget> implements IDraggable {

        private final int slot;
        private final OrreryDesign entry;
        private boolean moving;
        private UUID draggedDesign;
        private int sourceBar, startX, startY;
        private DesignWidget dropTarget;

        private DesignWidget(int slot, OrreryDesign entry) {
            this.slot = slot;
            this.entry = entry;
            playClickSound(false);
            if (slot >= 0) {
                background(new DynamicDrawable(() -> {
                    var state = OrreryClient.state();
                    return state != null && state.active == bar && state.selected(bar) == slot ?
                            GTGuiTextures.BUTTON : GTGuiTextures.SLOT;
                }), GTGuiTextures.FILTER_SLOT_OVERLAY);
                overlay(new DynamicDrawable(() -> {
                    var design = design();
                    return design == null ? GuiTextures.FILTER.asIcon().size(14).marginBottom(8) :
                            new ItemDrawable(design.iconStack()).asIcon().size(16).marginBottom(8);
                }), Text.dynamic(() -> {
                    var design = design();
                    return design == null ? Component.literal(Integer.toString(slot + 1)) :
                            shortened(Component.literal(OrreryClient.count(design)), getArea().w() - 6);
                }).asIcon().expandWidth().height(9).alignment(Alignment.BottomCenter).marginBottom(2));
            } else {
                overlay(new ItemDrawable(entry.iconStack()).asIcon().size(16).alignment(Alignment.CenterLeft)
                        .marginLeft(5),
                        Text.dynamic(() -> Text.of(shortened(Component.literal(design().name()), getArea().w() - 110))
                                .alignment(Alignment.CenterLeft)).asIcon().expandWidth().height(10)
                                .alignment(Alignment.TopLeft).margin(27, 79, 4, 0),
                        Text.dynamic(() -> Text.of(shortened(OrreryClient.text("machine", design().machineName()),
                                (int) ((getArea().w() - 126) / 0.75f)))
                                .alignment(Alignment.CenterLeft).scale(0.75f)).asIcon().expandWidth().height(10)
                                .alignment(Alignment.BottomLeft).margin(27, 95, 0, 4),
                        Text.dynamic(() -> shortened(Component.literal(OrreryClient.count(design())), 70))
                                .asIcon().size(70, 10).alignment(Alignment.TopRight).margin(0, 5, 4, 0),
                        Text.dynamic(() -> shortened(OrreryClient
                                .text(design().craftable() ? "pattern_ready_short" : "pattern_missing_short"), 88))
                                .asIcon().size(88, 10).alignment(Alignment.BottomRight).margin(0, 5, 0, 4));
            }
            tooltip(t -> {
                var design = design();
                if (design == null) return;
                t.addLine(Component.literal(design.name()).withStyle(ChatFormatting.WHITE))
                        .addLine(OrreryClient.text("machine", design.machineName()).copy()
                                .withStyle(ChatFormatting.GRAY))
                        .addLine(OrreryClient.text("charges", OrreryClient.count(design)).copy()
                                .withStyle(ChatFormatting.AQUA))
                        .addLine((OrreryClient.live() ?
                                OrreryClient.text(design.craftable() ? "pattern_ready" : "pattern_missing") :
                                OrreryClient.status()).copy().withStyle(OrreryClient.live() && design.craftable() ?
                                        ChatFormatting.GREEN : ChatFormatting.YELLOW))
                        .addLine(OrreryClient.text("craft_hint").copy().withStyle(ChatFormatting.DARK_AQUA));
            }).tooltipAutoUpdate(true);
            onMousePressed((context, button) -> {
                if (button == 2 && design() != null) {
                    saveName();
                    OrreryClient.requestCraft(design().id(), view());
                    return true;
                }
                if (slot < 0 || button != 1) return false;
                OrreryClient.send(OrreryPackets.Action.ASSIGN, bar, slot, null, "", 0, false, false);
                return true;
            });
        }

        private OrreryDesign design() {
            var state = OrreryClient.state();
            return slot < 0 ? Objects.requireNonNullElse(OrreryClient.design(entry.id()), entry) :
                    state == null ? null : OrreryClient.design(state.filter(bar, slot));
        }

        @Override
        public boolean onDragStart(int button) {
            var design = design();
            if (button != 0 || design == null) return false;
            draggedDesign = design.id();
            sourceBar = bar;
            startX = getContext().getAbsMouseX();
            startY = getContext().getAbsMouseY();
            dropTarget = null;
            return true;
        }

        @Override
        public boolean canDropHere(int x, int y, IWidget widget) {
            while (widget != null) {
                if (widget instanceof DesignWidget target && target.slot >= 0) {
                    dropTarget = target;
                    return true;
                }
                widget = widget.hasParent() ? widget.getParent() : null;
            }
            return false;
        }

        @Override
        public void onDragEnd(boolean successful) {
            if (successful && dropTarget != null) {
                if (slot >= 0 && !Screen.hasShiftDown() && sourceBar == bar)
                    OrreryClient.send(OrreryPackets.Action.SWAP, bar, slot, null, "", dropTarget.slot, false, false);
                else OrreryClient.send(OrreryPackets.Action.ASSIGN, bar, dropTarget.slot, draggedDesign, "", 0, false,
                        false);
                OrreryClient.sound(OrreryClient.Feedback.SELECT);
            } else if (slot >= 0 && sourceBar == bar && Math.abs(startX - getContext().getAbsMouseX()) +
                    Math.abs(startY - getContext().getAbsMouseY()) < 3) {
                        OrreryClient.select(bar, slot);
                    }
            draggedDesign = null;
            dropTarget = null;
        }

        @Override
        public void drawMovingState(GuiGraphics graphics, ModularGuiContext context, float partialTicks) {
            var design = OrreryClient.design(draggedDesign);
            if (design != null) new ItemDrawable(design.iconStack()).draw(context, context.getAbsMouseX() - 8,
                    context.getAbsMouseY() - 8, 16, 16, getScreen().getCurrentTheme().getItemSlotTheme().theme());
        }

        @Override
        public void onDrag(int button, double timeSinceLastClick) {}

        @Override
        public Area getMovingArea() {
            return null;
        }

        @Override
        public boolean isMoving() {
            return moving;
        }

        @Override
        public void setMoving(boolean moving) {
            this.moving = moving;
        }
    }
}
