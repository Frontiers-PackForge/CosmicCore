package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.api.data.DebugBlockPattern;
import com.ghostipedia.cosmiccore.api.data.DebugBlockPattern.PatternDirections;
import com.ghostipedia.cosmiccore.api.data.DebugBlockPattern.WorldDirections;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

public class StructureWriteBehavior implements IItemUIHolder {

    public static final StructureWriteBehavior INSTANCE = new StructureWriteBehavior();

    private static final int PANEL_WIDTH = 230;
    private static final int PANEL_HEIGHT = 166;
    private static final int DISPLAY_TEXT_COLOR = 0xFFD8D8D8;
    private static final int SLICE_COLOR = 0xFFFF6666;
    private static final int STRING_COLOR = 0xFF66FF66;
    private static final int CHARACTER_COLOR = 0xFF6699FF;

    protected StructureWriteBehavior() {}

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        StructureWriterControl control = new StructureWriterControl();
        control.setExporter(() -> buildExport(data.getUsedItemStack(), data.getPlayer()));
        control.setActionHandler(action -> handleAction(data, action));
        syncManager.syncValue("structure_writer_control", control.allowC2S());

        StringSyncValue minimum = new StringSyncValue(() -> formatPosition(data.getUsedItemStack(), 0));
        StringSyncValue maximum = new StringSyncValue(() -> formatPosition(data.getUsedItemStack(), 1));
        StringSyncValue dimensions = new StringSyncValue(() -> formatDimensions(data.getUsedItemStack()));
        StringSyncValue volume = new StringSyncValue(() -> formatVolume(data.getUsedItemStack()));
        StringSyncValue facing = new StringSyncValue(() -> getDir(data.getUsedItemStack()).getName());
        syncManager.syncValue("structure_writer_minimum", minimum);
        syncManager.syncValue("structure_writer_maximum", maximum);
        syncManager.syncValue("structure_writer_dimensions", dimensions);
        syncManager.syncValue("structure_writer_volume", volume);
        syncManager.syncValue("structure_writer_facing", facing);

        ModularPanel<?> panel = ModularPanel.defaultPanel("structure_writer", PANEL_WIDTH, PANEL_HEIGHT)
                .background(GTGuiTextures.BACKGROUND);
        panel.child(new TextWidget<>(() -> data.getUsedItemStack().getHoverName())
                .pos(8, 6)
                .size(PANEL_WIDTH - 16, 12));
        panel.child(displayPanel(Flow.column()
                .padding(6)
                .childPadding(2)
                .child(new TextWidget<>(() -> Component.translatable(
                        "item.cosmiccore.debug.structure_writer.selection",
                        minimum.getStringValue(),
                        maximum.getStringValue()))
                        .color(DISPLAY_TEXT_COLOR)
                        .height(10)
                        .widthRel(1))
                .child(new TextWidget<>(() -> Component.translatable(
                        "item.cosmiccore.debug.structure_writer.structural_scale",
                        dimensions.getStringValue(),
                        volume.getStringValue()))
                        .color(DISPLAY_TEXT_COLOR)
                        .height(10)
                        .widthRel(1))
                .child(new TextWidget<>(Component.translatable(
                        "item.cosmiccore.debug.structure_writer.v8_order"))
                        .color(DISPLAY_TEXT_COLOR)
                        .height(10)
                        .widthRel(1))
                .child(new TextWidget<>(() -> directionComponent(
                        "slice",
                        facing.getStringValue()))
                        .color(SLICE_COLOR)
                        .height(10)
                        .widthRel(1))
                .child(new TextWidget<>(() -> directionComponent(
                        "string",
                        facing.getStringValue()))
                        .color(STRING_COLOR)
                        .height(10)
                        .widthRel(1))
                .child(new TextWidget<>(() -> directionComponent(
                        "character",
                        facing.getStringValue()))
                        .color(CHARACTER_COLOR)
                        .height(10)
                        .widthRel(1))
                .child(new TextWidget<>(Component.translatable(
                        "item.cosmiccore.debug.structure_writer.usage"))
                        .color(0xFF999999)
                        .scale(0.75f)
                        .height(9)
                        .widthRel(1)))
                .pos(6, 20)
                .size(PANEL_WIDTH - 12, 100));
        panel.child(Flow.row()
                .childPadding(4)
                .child(actionButton(
                        "item.cosmiccore.debug.structure_writer.copy_pattern",
                        control::requestCopy))
                .child(actionButton(
                        "item.cosmiccore.debug.structure_writer.export_to_log",
                        () -> control.requestAction(StructureWriterControl.PRINT)))
                .pos(6, 124)
                .size(PANEL_WIDTH - 12, 16));
        panel.child(Flow.row()
                .childPadding(4)
                .child(actionButton(
                        "item.cosmiccore.debug.structure_writer.rotate_along_x_axis",
                        () -> control.requestAction(StructureWriterControl.ROTATE_X)))
                .child(actionButton(
                        "item.cosmiccore.debug.structure_writer.rotate_along_y_axis",
                        () -> control.requestAction(StructureWriterControl.ROTATE_Y)))
                .child(actionButton(
                        "item.cosmiccore.debug.structure_writer.clear",
                        () -> control.requestAction(StructureWriterControl.CLEAR)))
                .pos(6, 144)
                .size(PANEL_WIDTH - 12, 16));
        return panel;
    }

    private static ParentWidget<?> displayPanel(Flow content) {
        ParentWidget<?> panel = new ParentWidget<>()
                .background(new Rectangle().color(0xFF555555));
        panel.child(new ParentWidget<>()
                .pos(2, 2)
                .widthRelOffset(1, -4)
                .heightRelOffset(1, -4)
                .background(new Rectangle().color(0xFF000000))
                .child(content.sizeRel(1)));
        return panel;
    }

    private static ButtonWidget<?> actionButton(String translationKey, Runnable action) {
        return new ButtonWidget<>()
                .background(GTGuiTextures.BUTTON)
                .expanded()
                .heightRel(1)
                .onMousePressed((context, button) -> {
                    if (button != 0) return false;
                    action.run();
                    return true;
                })
                .child(new TextWidget<>(Component.translatable(translationKey))
                        .textAlign(Alignment.Center)
                        .sizeRel(1));
    }

    private static Component directionComponent(String role, String facingName) {
        Direction facing = Direction.byName(facingName);
        Direction resolvedFacing = facing == null ? Direction.WEST : facing;
        PatternDirections patternDirections = DebugBlockPattern.directionsFor(resolvedFacing);
        WorldDirections worldDirections = DebugBlockPattern.worldDirectionsFor(resolvedFacing);
        RelativeDirection relativeDirection = switch (role) {
            case "slice" -> patternDirections.slice();
            case "string" -> patternDirections.string();
            default -> patternDirections.character();
        };
        Direction worldDirection = switch (role) {
            case "slice" -> worldDirections.slice();
            case "string" -> worldDirections.string();
            default -> worldDirections.character();
        };
        return Component.translatable(
                "item.cosmiccore.debug.structure_writer.direction." + role,
                Component.translatable(
                        "item.cosmiccore.debug.structure_writer.relative." + relativeDirection.getSerializedName()),
                axisName(worldDirection));
    }

    private static String axisName(Direction direction) {
        String sign = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? "+" : "-";
        return sign + direction.getAxis().getName().toUpperCase();
    }

    private static void handleAction(PlayerInventoryGuiData<?> data, int action) {
        if (!(data.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack stack = data.getUsedItemStack();
        switch (action) {
            case StructureWriterControl.PRINT -> printExport(stack, player);
            case StructureWriterControl.ROTATE_X -> setDir(
                    stack,
                    getDir(stack).getClockWise(Direction.Axis.X));
            case StructureWriterControl.ROTATE_Y -> setDir(
                    stack,
                    getDir(stack).getClockWise(Direction.Axis.Y));
            case StructureWriterControl.CLEAR -> removePos(stack);
            default -> {
                return;
            }
        }
        data.setUsedItemStack(stack);
    }

    private static void printExport(ItemStack stack, ServerPlayer player) {
        String export = buildExport(stack, player);
        if (export.isEmpty()) return;
        GTCEu.LOGGER.info("\n{}", export);
        player.displayClientMessage(
                Component.translatable("item.cosmiccore.debug.structure_writer.output_successful"),
                false);
    }

    private static String buildExport(ItemStack stack, Player player) {
        BlockPos[] positions = getPos(stack);
        if (positions == null) return "";

        PatternDirections directions = DebugBlockPattern.directionsFor(getDir(stack));
        WorldDirections worldDirections = DebugBlockPattern.worldDirectionsFor(getDir(stack));
        DebugBlockPattern blockPattern = new DebugBlockPattern(
                player.level(),
                positions[0].getX(),
                positions[0].getY(),
                positions[0].getZ(),
                positions[1].getX(),
                positions[1].getY(),
                positions[1].getZ());
        blockPattern.orient(worldDirections);

        StringBuilder builder = new StringBuilder()
                .append("MultiblockPatternBuilder.start(\n")
                .append("        RelativeDirection.")
                .append(directions.slice().name())
                .append(",\n")
                .append("        RelativeDirection.")
                .append(directions.string().name())
                .append(",\n")
                .append("        RelativeDirection.")
                .append(directions.character().name())
                .append(")\n");
        for (String[] strings : blockPattern.pattern) {
            builder.append("    .slice(");
            for (int i = 0; i < strings.length; i++) {
                if (i > 0) builder.append(", ");
                builder.append('"')
                        .append(escapeJavaString(strings[i]))
                        .append('"');
            }
            builder.append(")\n");
        }
        builder.append('\n');
        blockPattern.charToBlockMap.forEach((character, resourceLocation) -> builder
                .append(character == ' ' ? "// ' ' = " : "// '" + character + "' = ")
                .append(resourceLocation)
                .append('\n'));
        return builder.toString();
    }

    private static String escapeJavaString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String formatPosition(ItemStack stack, int index) {
        BlockPos[] positions = getPos(stack);
        if (positions == null) return "-";
        BlockPos position = positions[index];
        return position.getX() + ", " + position.getY() + ", " + position.getZ();
    }

    private static String formatDimensions(ItemStack stack) {
        BlockPos[] positions = getPos(stack);
        if (positions == null) return "0 x 0 x 0";
        return (positions[1].getX() - positions[0].getX() + 1) +
                " x " +
                (positions[1].getY() - positions[0].getY() + 1) +
                " x " +
                (positions[1].getZ() - positions[0].getZ() + 1);
    }

    private static String formatVolume(ItemStack stack) {
        BlockPos[] positions = getPos(stack);
        if (positions == null) return "0";
        long x = positions[1].getX() - positions[0].getX() + 1L;
        long y = positions[1].getY() - positions[0].getY() + 1L;
        long z = positions[1].getZ() - positions[0].getZ() + 1L;
        return Long.toString(x * y * z);
    }

    public static boolean isItemStructureWriter(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof ComponentItem item) {
            return item.getComponents().contains(INSTANCE);
        }
        return false;
    }

    public static Direction getDir(ItemStack stack) {
        CompoundTag tag = ItemData.readElement(stack, "structure_writer");
        if (!tag.contains("dir")) return Direction.WEST;
        Direction direction = Direction.byName(tag.getString("dir"));
        return direction == null ? Direction.WEST : direction;
    }

    public static void setDir(ItemStack stack, Direction dir) {
        ItemData.mutateElement(stack, "structure_writer", tag -> tag.putString("dir", dir.getName()));
    }

    public static BlockPos[] getPos(ItemStack stack) {
        CompoundTag tag = ItemData.readElement(stack, "structure_writer");
        if (!tag.contains("minX")) return null;
        return new BlockPos[] {
                new BlockPos(tag.getInt("minX"), tag.getInt("minY"), tag.getInt("minZ")),
                new BlockPos(tag.getInt("maxX"), tag.getInt("maxY"), tag.getInt("maxZ"))
        };
    }

    public static void addPos(ItemStack stack, BlockPos pos) {
        ItemData.mutateElement(stack, "structure_writer", tag -> {
            if (!tag.contains("minX") || tag.getInt("minX") > pos.getX()) {
                tag.putInt("minX", pos.getX());
            }
            if (!tag.contains("maxX") || tag.getInt("maxX") < pos.getX()) {
                tag.putInt("maxX", pos.getX());
            }
            if (!tag.contains("minY") || tag.getInt("minY") > pos.getY()) {
                tag.putInt("minY", pos.getY());
            }
            if (!tag.contains("maxY") || tag.getInt("maxY") < pos.getY()) {
                tag.putInt("maxY", pos.getY());
            }
            if (!tag.contains("minZ") || tag.getInt("minZ") > pos.getZ()) {
                tag.putInt("minZ", pos.getZ());
            }
            if (!tag.contains("maxZ") || tag.getInt("maxZ") < pos.getZ()) {
                tag.putInt("maxZ", pos.getZ());
            }
        });
    }

    public static void removePos(ItemStack stack) {
        ItemData.mutateElement(stack, "structure_writer", tag -> {
            tag.remove("minX");
            tag.remove("maxX");
            tag.remove("minY");
            tag.remove("maxY");
            tag.remove("minZ");
            tag.remove("maxZ");
        });
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(context.getHand());
        if (player.isShiftKeyDown()) {
            removePos(stack);
        } else {
            addPos(stack, context.getClickedPos());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown()) {
            removePos(stack);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return IItemUIHolder.super.use(item, level, player, usedHand);
    }
}
