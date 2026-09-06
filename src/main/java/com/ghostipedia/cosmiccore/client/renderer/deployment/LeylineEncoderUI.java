package com.ghostipedia.cosmiccore.client.renderer.deployment;

import com.ghostipedia.cosmiccore.common.deployment.*;
import com.ghostipedia.cosmiccore.integration.emi.MultiblockPreviewSchemaCache;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.StringValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.ScrollWidget;
import brachy.modularui.widget.scroll.HorizontalScrollData;
import brachy.modularui.widget.scroll.ScrollArea;
import brachy.modularui.widget.scroll.VerticalScrollData;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.dynamic.*;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import java.util.*;

public final class LeylineEncoderUI {

    public static void populate(ModularPanel panel, ParentWidget<?> controls, LeylineEncoderControl control,
                                CompoundTag draft) {
        new Editor(panel, controls, control, draft);
    }

    private static final class Editor {

        private final LeylineEncoderControl control;
        private final StringValue name = new StringValue("");
        private String search = "";
        private final DynamicHandler machineList = new DynamicHandler();
        private final DynamicHandler preview = new DynamicHandler();
        private MultiblockMachineDefinition definition;
        private MultiblockSchemaInfo schema;
        private LeylineEncoderCache.Lease lease;
        private LeylinePrefab loaded;
        private ItemStack icon = ItemStack.EMPTY;
        private UUID validated;
        private LeylinePrefab cached;
        private int selectionRevision;
        private long blockCount;
        private Component status = Component.translatable("cosmiccore.leyline.choose");

        private Editor(ModularPanel panel, ParentWidget<?> controls, LeylineEncoderControl control, CompoundTag draft) {
            this.control = control;
            panel.child(Text.lang("cosmiccore.leyline.encoder").asWidget().pos(8, 7));
            panel.child(new ButtonWidget<>().pos(396, 4).size(16).padding(1).overlay(GTGuiTextures.INFO)
                    .tooltip(t -> {
                        t.addLine(Component.translatable("cosmiccore.leyline.choose"));
                        t.addLine(Component.translatable("cosmiccore.leyline.input"));
                        t.addLine(Component.translatable("cosmiccore.leyline.output"));
                        t.addLine(Component.translatable("cosmiccore.leyline.encode_hint"));
                        t.addLine(Component.translatable("cosmiccore.leyline.icon"));
                    }));
            panel.child(new TextFieldWidget().value(new StringValue.Dynamic(() -> search, value -> {
                search = value;
                machineList.notifyUpdate();
            })).autoUpdateOnChange(true)
                    .hintText(Component.translatable("cosmiccore.leyline.search")).pos(8, 24).size(140, 18));
            machineList.widgetProvider(this::buildMachineList);
            panel.child(new DynamicWidget<>().clientOnlyHandler(machineList).pos(9, 43).size(138, 154));
            machineList.notifyUpdate();
            var previewArea = new ScrollWidget<>(new VerticalScrollData()).pos(154, 24).size(258, 166)
                    .background(GTGuiTextures.BACKGROUND_INVERSE);
            previewArea.getScrollArea().setScrollX(new HorizontalScrollData());
            previewArea.child(new DynamicWidget<>().clientOnlyHandler(preview).coverChildren());
            panel.child(previewArea);
            panel.child(Text.lang("cosmiccore.leyline.name").asWidget().pos(154, 193).size(234, 9));
            panel.child(new TextFieldWidget().value(name).autoUpdateOnChange(true).setMaxLength(80)
                    .hintText(Component.translatable("cosmiccore.leyline.name")).pos(154, 204).size(234, 18));
            panel.child(new ButtonWidget<>().pos(394, 204).size(18)
                    .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY)
                    .hoverBackground(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY)
                    .padding(1)
                    .overlay(new DynamicDrawable(() -> icon.isEmpty() ? GuiTextures.FILTER : new ItemDrawable(icon)))
                    .onMousePressed((context, button) -> {
                        icon = Minecraft.getInstance().player.containerMenu.getCarried().copyWithCount(1);
                        return true;
                    }));
            controls.child(
                    new ButtonWidget<>().pos(30, 6).size(82, 22).overlay(Text.lang("cosmiccore.leyline.validate"))
                            .onMousePressed((context, button) -> {
                                submit(false);
                                return true;
                            }));
            controls.child(new ButtonWidget<>().pos(116, 6).size(82, 22)
                    .background(new DynamicDrawable(
                            () -> canEncode() ? GuiTextures.MC_BUTTON : GuiTextures.MC_BUTTON_DISABLED))
                    .hoverBackground(new DynamicDrawable(
                            () -> canEncode() ? GuiTextures.MC_BUTTON_HOVERED : GuiTextures.MC_BUTTON_DISABLED))
                    .overlay(Text.lang("cosmiccore.leyline.encode").color(() -> canEncode() ? 0xFFFFFFFF : 0xFFA0A0A0))
                    .onMousePressed((context, button) -> {
                        if (!canEncode()) return false;
                        submit(true);
                        return true;
                    }));
            controls.child(Text.dynamic(() -> status).asWidget().alignment(Alignment.TopLeft)
                    .pos(6, 30).size(222, 20)
                    .tooltip(t -> control.errors.forEach(t::addLine)).tooltipAutoUpdate(true));
            controls.child(Text
                    .dynamic(() -> schema == null ? Component.empty() :
                            Component.translatable("cosmiccore.leyline.estimate",
                                    blockCount / 20.0))
                    .asWidget().pos(6, 51).size(222, 10));
            control.loadDesign = stack -> {
                try {
                    loadReference(LeylinePrefab.reference(stack), panel);
                } catch (RuntimeException exception) {
                    status = Component.translatable("cosmiccore.leyline.invalid");
                }
            };
            control.result = (valid, encoded) -> {
                validated = valid ? submitted : null;
                status = Component.translatable(encoded ? "cosmiccore.leyline.encoded" :
                        valid ? "cosmiccore.leyline.valid" : "cosmiccore.leyline.invalid");
            };
            if (!draft.isEmpty()) {
                try {
                    if (draft.hasUUID("id")) loadReference(draft.getUUID("id"), panel);
                } catch (RuntimeException ignored) {}
            }
        }

        private UUID submitted;

        private void loadReference(UUID id, ModularPanel panel) {
            if (id != null && id.equals(currentId())) return;
            int revision = ++selectionRevision;
            status = Component.translatable("cosmiccore.leyline.loading");
            var request = LeylinePrefabClient.request(id);
            boolean immediate = request.isDone();
            request.thenAccept(prefab -> {
                if (revision != selectionRevision || !immediate && !panel.isValid()) return;
                if (prefab == null) status = Component.translatable("cosmiccore.leyline.unavailable");
                else {
                    select((MultiblockMachineDefinition) GTRegistries.MACHINES.get(prefab.machine()), prefab);
                }
            });
        }

        private ListWidget<?, ?> buildMachineList() {
            var machines = new ListWidget<>().size(138, 154).padding(3).scrollDirection(new EncoderListScrollData())
                    .background(GTGuiTextures.BACKGROUND_INVERSE);
            String query = LeylineEncoderCache.normalize(search);
            for (var row : LeylineEncoderCache.machines()) {
                if (!row.matches(query)) continue;
                machines.child(new ButtonWidget<>().width(123).height(row.height())
                        .padding(4).overlay(Text.str(row.title().getString()))
                        .tooltip(t -> t.addLine(row.title())
                                .addLine(Component.literal(row.definition().getId().toString())))
                        .onMousePressed((context, button) -> {
                            select(row.definition(), null);
                            return true;
                        }));
            }
            return machines;
        }

        private boolean canEncode() {
            return control.canWritePattern.getAsBoolean() && validated != null && cached != null &&
                    validated.equals(cached.id()) &&
                    cached.name().equals(name.getStringValue().strip()) &&
                    cached.icon().equals(
                            BuiltInRegistries.ITEM.getKey(icon.isEmpty() ? definition.getItem() : icon.getItem()));
        }

        private void submit(boolean encode) {
            try {
                var prefab = current();
                submitted = prefab.id();
                control.submit(prefab, encode);
            } catch (RuntimeException exception) {
                validated = null;
                status = Component.translatable("cosmiccore.leyline.invalid");
            }
        }

        private UUID currentId() {
            if (loaded != null && loaded.name().equals(name.getStringValue().strip()) &&
                    loaded.icon().equals(BuiltInRegistries.ITEM.getKey(icon.getItem())))
                return loaded.id();
            try {
                return current().id();
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        private LeylinePrefab current() {
            if (schema == null) throw new IllegalStateException();
            var selectedIcon = BuiltInRegistries.ITEM.getKey(icon.isEmpty() ? definition.getItem() : icon.getItem());
            if (cached != null && cached.name().equals(name.getStringValue().strip()) &&
                    cached.icon().equals(selectedIcon))
                return cached;
            var origin = schema.getStructureBlocks().entrySet().stream()
                    .filter(e -> e.getValue().getBlockState().is(definition.getBlock())).findFirst().orElseThrow()
                    .getKey();
            cached = new LeylinePrefab(definition.getId(), name.getStringValue(), selectedIcon,
                    schema.getStructureBlocks().entrySet().stream().filter(e -> e.getValue().nonAir())
                            .map(e -> new LeylineBlockPlacement(e.getKey().subtract(origin),
                                    e.getValue().getBlockState()))
                            .toList(),
                    java.util.stream.IntStream.range(0, schema.getUserSliceRepeats().size())
                            .map(schema.getUserSliceRepeats()::get).boxed().toList(),
                    schema.getUserDimensions().intStream().boxed().toList());
            lease.identify(cached.id());
            return cached;
        }

        private void select(MultiblockMachineDefinition definition, LeylinePrefab saved) {
            selectionRevision++;
            try {
                selectSupported(definition, saved);
            } catch (RuntimeException exception) {
                schema = null;
                loaded = null;
                cached = null;
                validated = null;
                status = Component.translatable("cosmiccore.leyline.unsupported");
                preview.widgetProvider(() -> null);
                preview.notifyUpdate();
            }
        }

        private void selectSupported(MultiblockMachineDefinition definition, LeylinePrefab saved) {
            if (this.definition == definition && schema != null && saved == null) return;
            this.definition = definition;
            lease = LeylineEncoderCache.acquire(definition, saved == null ? null : saved.id());
            schema = lease.schema();
            cached = null;
            loaded = saved;
            if (schema.getMapSchema() == null) {
                if (saved != null) {
                    for (int i = 0; i < saved.repeats().size(); i++)
                        schema.getUserSliceRepeats().put(i, saved.repeats().get(i).intValue());
                    saved.dimensions().forEach(value -> schema.getUserDimensions().add(value.intValue()));
                }
                if (!MultiblockPreviewSchemaCache.apply(definition, schema, Direction.NORTH, Direction.UP, false)) {
                    schema.refreshSchema(definition, Direction.NORTH, Direction.UP, false, null);
                    if (saved == null && definition.getRotationState().defaultDirection == Direction.NORTH) {
                        MultiblockPreviewSchemaCache.capture(definition, schema.getStructureBlocks());
                    }
                }
                if (saved != null) {
                    var origin = schema.getMapSchema().getControllerPos();
                    saved.blocks().forEach(p -> schema.getUserGlobalBlockPreferences()
                            .put(p.relativeOffset().offset(origin).asLong(), new BlockInfo(p.state())));
                    schema.refreshSchema(definition, Direction.NORTH, Direction.UP, false, null);
                }
            }
            name.setStringValue(saved == null ? definition.getItem().getDescription().getString() : saved.name());
            icon = saved == null ? new ItemStack(definition.getItem()) :
                    new ItemStack(BuiltInRegistries.ITEM.get(saved.icon()));
            validated = null;
            blockCount = schema.getStructureBlocks().values().stream().filter(BlockInfo::nonAir).count();
            var selectedLease = lease;
            var selectedSchema = schema;
            preview.widgetProvider(() -> {
                var widget = new EncoderPreviewWidget(definition, selectedLease);
                widget.setFrontFacing(Direction.NORTH).setUpFacing(Direction.UP)
                        .setOnSchemaRefresh(() -> {
                            selectedLease.changed();
                            if (schema != selectedSchema) return;
                            loaded = null;
                            validated = null;
                            cached = null;
                            blockCount = schema.getStructureBlocks().values().stream().filter(BlockInfo::nonAir)
                                    .count();
                        });
                return widget;
            });
            preview.notifyUpdate();
        }
    }

    private static final class EncoderListScrollData extends VerticalScrollData {

        @Override
        public int getThickness() {
            return Math.max(2, super.getThickness()) + 3;
        }

        @Override
        public int getScrollBarLength(ScrollArea area) {
            int visible = getFullVisibleSize(area);
            if (getScrollSize() <= 0) return visible;
            int proportional = (int) ((float) visible * visible / getScrollSize());
            int normalLength = Math.max(proportional, Math.max(2, super.getThickness()) + 1);
            return Math.min(visible, normalLength + 2);
        }
    }

    private static final class EncoderPreviewWidget extends MultiblockPreviewWidget {

        private boolean initialized;
        private final LeylineEncoderCache.Lease lease;

        private EncoderPreviewWidget(MultiblockMachineDefinition definition, LeylineEncoderCache.Lease lease) {
            super(definition, lease.schema(), 176, 120);
            this.lease = lease;
        }

        @Override
        public void dispose() {
            super.dispose();
            lease.release();
        }

        @Override
        public boolean addChild(IWidget child, int index) {
            if (child instanceof ButtonWidget<?>) return true;
            return super.addChild(child, index);
        }

        @Override
        public void refreshSchema() {
            if (initialized || getMultiblockSchemaInfo() == null || getMultiblockSchemaInfo().getMapSchema() == null) {
                super.refreshSchema();
            }
            initialized = true;
        }
    }
}
