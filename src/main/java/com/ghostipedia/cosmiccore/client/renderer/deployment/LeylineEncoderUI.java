package com.ghostipedia.cosmiccore.client.renderer.deployment;

import com.ghostipedia.cosmiccore.api.machine.multiblock.GroupedSlicePreviewSupport;
import com.ghostipedia.cosmiccore.common.deployment.*;
import com.ghostipedia.cosmiccore.integration.emi.MultiblockPreviewSchemaCache;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.schema.BlockHighlight;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.value.StringValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.ScrollWidget;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widget.scroll.HorizontalScrollData;
import brachy.modularui.widget.scroll.ScrollArea;
import brachy.modularui.widget.scroll.VerticalScrollData;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.dynamic.*;
import brachy.modularui.widgets.menu.ContextMenuButton;
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
        private final DynamicHandler facingControls = new DynamicHandler();
        private MultiblockMachineDefinition definition;
        private MultiblockSchemaInfo schema;
        private LeylineEncoderCache.Lease lease;
        private Map<BlockPos, Direction> facingPreferences = Map.of();
        private BlockPos facingTarget;
        private LeylinePrefab loaded;
        private ItemStack icon = ItemStack.EMPTY;
        private UUID validated;
        private LeylinePrefab cached;
        private int selectionRevision;
        private boolean initializing = true;
        private String observedName = "";
        private int nameSaveDelay;
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
                        t.addLine(Component.translatable("cosmiccore.leyline.facing"));
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
            var previewArea = new ScrollWidget<>(new VerticalScrollData()).pos(154, 24).size(258, 138)
                    .background(GTGuiTextures.BACKGROUND_INVERSE);
            previewArea.getScrollArea().setScrollX(new HorizontalScrollData());
            previewArea.child(new DynamicWidget<>().clientOnlyHandler(preview).coverChildren());
            panel.child(previewArea);
            facingControls.widgetProvider(this::buildFacingControls);
            panel.child(new DynamicWidget<>().clientOnlyHandler(facingControls).pos(154, 165).size(258, 25));
            facingControls.notifyUpdate();
            panel.child(Text.lang("cosmiccore.leyline.name").asWidget().pos(154, 193).size(234, 9));
            panel.child(new TextFieldWidget().value(name).autoUpdateOnChange(true).setMaxLength(80)
                    .hintText(Component.translatable("cosmiccore.leyline.name"))
                    .onUpdateListener(widget -> updateNameDraft()).pos(154, 204).size(234, 18));
            panel.child(new ButtonWidget<>().pos(394, 204).size(18)
                    .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY)
                    .hoverBackground(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY)
                    .padding(1)
                    .overlay(new DynamicDrawable(() -> icon.isEmpty() ? GuiTextures.FILTER : new ItemDrawable(icon)))
                    .onMousePressed((context, button) -> {
                        icon = Minecraft.getInstance().player.containerMenu.getCarried().copyWithCount(1);
                        loaded = null;
                        validated = null;
                        cached = null;
                        saveDraft();
                        return true;
                    }));
            controls.child(
                    new ButtonWidget<>().pos(74, 6).size(56, 22).overlay(Text.lang("cosmiccore.leyline.validate"))
                            .onMousePressed((context, button) -> {
                                submit(false);
                                return true;
                            }));
            controls.child(new ButtonWidget<>().pos(134, 6).size(64, 22)
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
            controls.child(new ButtonWidget<>().pos(30, 6).size(40, 22)
                    .overlay(Text.lang("cosmiccore.leyline.clear"))
                    .tooltip(t -> t.addLine(Component.translatable("cosmiccore.leyline.clear_hint")))
                    .onMousePressed((context, button) -> {
                        clearDraft();
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
                    if (draft.contains("payload")) {
                        var prefab = LeylinePrefab.fromPayload(draft.getByteArray("payload"));
                        select((MultiblockMachineDefinition) GTRegistries.MACHINES.get(prefab.machine()), prefab);
                    } else if (draft.hasUUID("id")) {
                        loadReference(draft.getUUID("id"), panel);
                    }
                } catch (RuntimeException ignored) {}
            }
            initializing = false;
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

        private ParentWidget<?> buildFacingControls() {
            var row = new ParentWidget<>().size(258, 25).background(GTGuiTextures.BACKGROUND_INVERSE);
            List<BlockPos> targets = facingTargets();
            if (targets.isEmpty()) {
                facingTarget = null;
                row.child(Text.lang("cosmiccore.leyline.facing.none").asWidget().pos(6, 8).size(188, 9));
                return addMaterialsMenu(row);
            }
            if (facingTarget == null || !targets.contains(facingTarget)) facingTarget = targets.getFirst();
            BlockInfo info = schema.getStructureBlocks().get(facingTarget);
            row.child(Text.lang("cosmiccore.leyline.facing.label").asWidget().pos(6, 8).size(38, 9));
            row.child(Text.dynamic(() -> Component.translatable(
                    "cosmiccore.leyline.facing.short." + currentFacing().getName())).asWidget().pos(45, 8).size(9, 9)
                    .tooltip(t -> t.addLine(Component.translatable(
                            "cosmiccore.leyline.facing." + currentFacing().getName()))));
            row.child(new ButtonWidget<>().pos(56, 4).size(18).overlay(Text.str("<"))
                    .tooltip(t -> t.addLine(Component.translatable("cosmiccore.leyline.facing.previous")))
                    .onMousePressed((context, button) -> {
                        cycleFacingTarget(targets, -1);
                        return true;
                    }));
            row.child(new ItemDrawable(info.getItemStackForm()).asWidget().pos(77, 5).size(16)
                    .tooltip(t -> t.addFromItem(info.getItemStackForm())
                            .addLine(Component.translatable("cosmiccore.leyline.facing.position",
                                    facingTarget.toShortString()))));
            row.child(new ButtonWidget<>().pos(96, 4).size(18).overlay(Text.str(">"))
                    .tooltip(t -> t.addLine(Component.translatable("cosmiccore.leyline.facing.next")))
                    .onMousePressed((context, button) -> {
                        cycleFacingTarget(targets, 1);
                        return true;
                    }));
            RotationState rotation = ((MetaMachineBlock) info.getBlockState().getBlock()).getRotationState();
            List<Direction> directions = Arrays.stream(Direction.values()).filter(rotation::test).toList();
            var directionMenu = new ContextMenuButton<>("leyline_facing").pos(118, 4).size(76, 18).requiresClick()
                    .openDown()
                    .overlay(Text.lang("cosmiccore.leyline.facing." + currentFacing().getName())
                            .alignment(Alignment.CenterLeft).asIcon().expandWidth().marginLeft(4));
            directionMenu.menuList(list -> list.maxSize(112).coverChildrenWidth().childSeparator(Icon.EMPTY_2PX)
                    .children(directions, direction -> new ButtonWidget<>().size(74, 18)
                            .background(GuiTextures.MC_BUTTON)
                            .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                            .overlay(Text.lang("cosmiccore.leyline.facing." + direction.getName())
                                    .alignment(Alignment.CenterLeft).asIcon().expandWidth().marginLeft(4))
                            .onMousePressed((context, button) -> {
                                if (button != 0) return false;
                                directionMenu.closeMenu(false);
                                if (direction != currentFacing()) setFacing(direction);
                                return true;
                            })));
            row.child(directionMenu);
            return addMaterialsMenu(row);
        }

        private ParentWidget<?> addMaterialsMenu(ParentWidget<?> row) {
            if (schema == null) return row;
            var materials = new ContextMenuButton<>("leyline_materials").pos(197, 4).size(55, 18).requiresClick()
                    .openLeftDown()
                    .overlay(Text.lang("cosmiccore.leyline.materials").alignment(Alignment.CenterLeft).asIcon()
                            .expandWidth().marginLeft(4));
            materials.menuList(list -> {
                var entries = new ArrayList<>(schema.getBlockCounts().reference2IntEntrySet());
                entries.sort(Comparator.comparing(entry -> new ItemStack(entry.getKey()).getHoverName().getString()));
                list.maxSize(112).coverChildrenWidth().childSeparator(Icon.EMPTY_2PX)
                        .children(entries, entry -> {
                            ItemStack stack = new ItemStack(entry.getKey(), entry.getIntValue());
                            return new ButtonWidget<>().size(104, 18)
                                    .background(GuiTextures.MC_BUTTON)
                                    .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                                    .overlay(new ItemDrawable(stack).asIcon().size(16)
                                            .alignment(Alignment.CenterLeft).marginLeft(2),
                                            Text.str("x " + entry.getIntValue()).alignment(Alignment.CenterLeft)
                                                    .asIcon().expandWidth().marginLeft(22))
                                    .tooltip(t -> t.addFromItem(stack)
                                            .addLine(Component.translatable("cosmiccore.leyline.material_count",
                                                    entry.getIntValue())))
                                    .onMousePressed((context, button) -> {
                                        if (button != 0) return false;
                                        materials.closeMenu(false);
                                        return true;
                                    });
                        });
            });
            row.child(materials);
            return row;
        }

        private List<BlockPos> facingTargets() {
            if (schema == null || definition == null) return List.of();
            return schema.getStructureBlocks().entrySet().stream()
                    .filter(entry -> entry.getValue().getBlockState().getBlock() instanceof MetaMachineBlock)
                    .filter(entry -> !entry.getValue().getBlockState().is(definition.getBlock()))
                    .filter(entry -> {
                        var state = entry.getValue().getBlockState();
                        RotationState rotation = ((MetaMachineBlock) state.getBlock()).getRotationState();
                        return rotation != RotationState.NONE && state.hasProperty(rotation.property);
                    })
                    .map(Map.Entry::getKey)
                    .sorted(Comparator.comparingInt((BlockPos pos) -> pos.getY())
                            .thenComparingInt(pos -> pos.getZ())
                            .thenComparingInt(pos -> pos.getX()))
                    .toList();
        }

        private void cycleFacingTarget(List<BlockPos> targets, int step) {
            int index = targets.indexOf(facingTarget);
            facingTarget = targets.get(Math.floorMod(index + step, targets.size()));
            LeylineEncoderHighlight.flash(schema.getRenderer(), facingTarget);
            facingControls.notifyUpdate();
        }

        private Direction currentFacing() {
            BlockInfo info = facingTarget == null || schema == null ? null :
                    schema.getStructureBlocks().get(facingTarget);
            if (info == null || !(info.getBlockState().getBlock() instanceof MetaMachineBlock machine))
                return Direction.NORTH;
            RotationState rotation = machine.getRotationState();
            return rotation == RotationState.NONE || !info.getBlockState().hasProperty(rotation.property) ?
                    Direction.NORTH : info.getBlockState().getValue(rotation.property);
        }

        private void setFacing(Direction direction) {
            if (facingTarget == null || schema == null) return;
            BlockInfo info = schema.getStructureBlocks().get(facingTarget);
            if (info == null || !(info.getBlockState().getBlock() instanceof MetaMachineBlock machine) ||
                    info.getBlockState().is(definition.getBlock()) || !machine.getRotationState().test(direction))
                return;
            facingPreferences.put(facingTarget, direction);
            applyFacingPreferences(schema, definition, facingPreferences);
            loaded = null;
            validated = null;
            cached = null;
            lease.changed();
            LeylineEncoderHighlight.flash(schema.getRenderer(), facingTarget);
            facingControls.notifyUpdate();
            saveDraft();
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
                control.saveDraft(prefab);
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
            facingPreferences = new HashMap<>();
            facingTarget = null;
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
                    saved.blocks().forEach(p -> {
                        BlockPos pos = p.relativeOffset().offset(origin);
                        rememberFacing(facingPreferences, definition, pos, p.state());
                        schema.getUserGlobalBlockPreferences().put(pos.asLong(),
                                new BlockInfo(normalizeFacing(p.state())));
                    });
                    schema.refreshSchema(definition, Direction.NORTH, Direction.UP, false, null);
                    applyFacingPreferences(schema, definition, facingPreferences);
                }
            }
            captureFacingPreferences(schema, definition, facingPreferences);
            name.setStringValue(saved == null ? definition.getItem().getDescription().getString() : saved.name());
            icon = saved == null ? new ItemStack(definition.getItem()) :
                    new ItemStack(BuiltInRegistries.ITEM.get(saved.icon()));
            validated = null;
            blockCount = schema.getStructureBlocks().values().stream().filter(BlockInfo::nonAir).count();
            var selectedLease = lease;
            var selectedSchema = schema;
            preview.widgetProvider(() -> {
                var widget = new EncoderPreviewWidget(definition, selectedLease, facingPreferences,
                        this::selectFacingTarget);
                widget.setFrontFacing(Direction.NORTH).setUpFacing(Direction.UP)
                        .setOnSchemaRefresh(() -> {
                            selectedLease.changed();
                            if (schema != selectedSchema) return;
                            loaded = null;
                            validated = null;
                            cached = null;
                            blockCount = schema.getStructureBlocks().values().stream().filter(BlockInfo::nonAir)
                                    .count();
                            facingControls.notifyUpdate();
                            saveDraft();
                        });
                return widget;
            });
            preview.notifyUpdate();
            facingControls.notifyUpdate();
            saveDraft();
        }

        private void selectFacingTarget(BlockPos pos) {
            if (!facingTargets().contains(pos)) return;
            facingTarget = pos.immutable();
            LeylineEncoderHighlight.flash(schema.getRenderer(), facingTarget);
            facingControls.notifyUpdate();
        }

        private void saveDraft() {
            if (initializing || schema == null || definition == null) return;
            try {
                control.saveDraft(current());
            } catch (RuntimeException ignored) {}
        }

        private void updateNameDraft() {
            String currentName = name.getStringValue();
            if (!currentName.equals(observedName)) {
                observedName = currentName;
                nameSaveDelay = 10;
                loaded = null;
                validated = null;
                cached = null;
                return;
            }
            if (nameSaveDelay > 0 && --nameSaveDelay == 0) saveDraft();
        }

        private void clearDraft() {
            selectionRevision++;
            definition = null;
            schema = null;
            loaded = null;
            cached = null;
            validated = null;
            facingTarget = null;
            facingPreferences = Map.of();
            icon = ItemStack.EMPTY;
            name.setStringValue("");
            observedName = "";
            nameSaveDelay = 0;
            blockCount = 0;
            status = Component.translatable("cosmiccore.leyline.choose");
            preview.widgetProvider(() -> null);
            preview.notifyUpdate();
            facingControls.notifyUpdate();
            control.clearDraft();
        }

        private static BlockState normalizeFacing(BlockState state) {
            if (!(state.getBlock() instanceof MetaMachineBlock machine)) return state;
            RotationState rotation = machine.getRotationState();
            if (rotation == RotationState.NONE || !state.hasProperty(rotation.property)) return state;
            BlockState normalized = state.setValue(rotation.property, rotation.defaultDirection);
            if (normalized.hasProperty(GTBlockStateProperties.UPWARDS_FACING)) {
                normalized = normalized.setValue(GTBlockStateProperties.UPWARDS_FACING, Direction.UP);
            }
            return normalized;
        }

        private static void captureFacingPreferences(MultiblockSchemaInfo schema,
                                                     MultiblockMachineDefinition definition,
                                                     Map<BlockPos, Direction> preferences) {
            schema.getStructureBlocks()
                    .forEach((pos, info) -> rememberFacing(preferences, definition, pos, info.getBlockState()));
        }

        private static void rememberFacing(Map<BlockPos, Direction> preferences,
                                           MultiblockMachineDefinition definition,
                                           BlockPos pos, BlockState state) {
            if (!(state.getBlock() instanceof MetaMachineBlock machine) || state.is(definition.getBlock())) return;
            RotationState rotation = machine.getRotationState();
            if (rotation != RotationState.NONE && state.hasProperty(rotation.property)) {
                preferences.put(pos.immutable(), state.getValue(rotation.property));
            }
        }
    }

    private static void applyFacingPreferences(MultiblockSchemaInfo schema,
                                               MultiblockMachineDefinition definition,
                                               Map<BlockPos, Direction> preferences) {
        if (schema == null || schema.getMapSchema() == null) return;
        boolean changed = false;
        var iterator = preferences.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            BlockInfo info = schema.getStructureBlocks().get(entry.getKey());
            if (info == null || !(info.getBlockState().getBlock() instanceof MetaMachineBlock machine) ||
                    info.getBlockState().is(definition.getBlock())) {
                iterator.remove();
                continue;
            }
            RotationState rotation = machine.getRotationState();
            if (rotation == RotationState.NONE || !rotation.test(entry.getValue()) ||
                    !info.getBlockState().hasProperty(rotation.property)) {
                iterator.remove();
                continue;
            }
            BlockState state = info.getBlockState().setValue(rotation.property, entry.getValue());
            if (state.equals(info.getBlockState())) continue;
            schema.getStructureBlocks().put(entry.getKey(), BlockInfo.fromBlockState(state));
            schema.getMapSchema().updateBlockState(entry.getKey(), state);
            changed = true;
        }
        if (changed && schema.getRenderer() != null) schema.getRenderer().notifyRecompile();
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

    private static final class EncoderPreviewWidget extends MultiblockPreviewWidget
                                                    implements LeylineEncoderPreviewAccess {

        private boolean initialized;
        private boolean acceptButtons;
        private final LeylineEncoderCache.Lease lease;
        private final MultiblockMachineDefinition definition;
        private final Map<BlockPos, Direction> facingPreferences;
        private final java.util.function.Consumer<BlockPos> selection;

        private EncoderPreviewWidget(MultiblockMachineDefinition definition, LeylineEncoderCache.Lease lease,
                                     Map<BlockPos, Direction> facingPreferences,
                                     java.util.function.Consumer<BlockPos> selection) {
            super(definition, lease.schema(), 176, previewHeight(definition));
            this.lease = lease;
            this.definition = definition;
            this.facingPreferences = facingPreferences;
            this.selection = selection;
            this.acceptButtons = true;
            getMultiblockSchemaInfo().getRenderer().highlightRenderer(
                    new BlockHighlight(Color.withAlpha(Color.RED.brighter(1), 0.95F), 1 / 24.0F));
            IWidget parts = WidgetTree.findFirstWithNameNullable(this, "parts_view");
            if (parts != null && parts.hasParent() && parts.getParent() instanceof ParentWidget<?> parent) {
                parent.remove(parts);
            }
        }

        private static int previewHeight(MultiblockMachineDefinition definition) {
            var pattern = definition.getStructurePatterns().get("main").get();
            if (pattern instanceof com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern blockPattern) {
                return 120 + GroupedSlicePreviewSupport.variableGroups(blockPattern).size() * 25;
            }
            return 120;
        }

        @Override
        public void dispose() {
            LeylineEncoderHighlight.clear(getMultiblockSchemaInfo().getRenderer());
            super.dispose();
            lease.release();
        }

        @Override
        public boolean addChild(IWidget child, int index) {
            if (!acceptButtons && child instanceof ButtonWidget<?>) return true;
            return super.addChild(child, index);
        }

        @Override
        public void refreshSchema() {
            if (initialized || getMultiblockSchemaInfo() == null || getMultiblockSchemaInfo().getMapSchema() == null) {
                super.refreshSchema();
            }
            initialized = true;
            if (facingPreferences != null) {
                applyFacingPreferences(getMultiblockSchemaInfo(), definition, facingPreferences);
            }
        }

        @Override
        public void cosmiccore$selectFacingTarget(BlockPos pos) {
            selection.accept(pos);
        }
    }
}
