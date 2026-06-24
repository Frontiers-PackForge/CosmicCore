package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.api.data.DebugBlockPattern;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

import com.google.common.base.Joiner;

public class StructureWriteBehavior implements IItemUIHolder {

    public static final StructureWriteBehavior INSTANCE = new StructureWriteBehavior();

    protected StructureWriteBehavior() {
        /**/
    }

    // TODO(8.0.0 MUI2): custom UI shelved; default UI used (orig in git).
    //  The original createUI(HeldItemUIFactory.HeldItemHolder, Player) was a full LDLib ModularUI:
    //   - a DISPLAY-backed panel showing the structural scale (1 + max-min on X/Y/Z) and the export
    //     order (DebugBlockPattern.getDir(dir)[0..2] names);
    //   - an "export_to_log" button -> exportLog(...);
    //   - "rotate_along_x_axis" / "rotate_along_y_axis" buttons -> changeDirX(...) / changeDirY(...).
    //  Rebuild those widgets in MUI2 here, wiring the export/rotate helpers below (which retain the
    //  full export-to-log logic). For now we open a minimal default panel.
    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return ModularPanel.defaultPanel("structure_writer", 176, 120);
    }

    /**
     * Export-to-log logic (PRESERVED from the pre-8.0.0 createUI button). Builds the multiblock
     * .slice(...) lines + a character->block legend and dumps them to the log. Retyped to take the
     * held stack + player directly now that LDLib's HeldItemUIFactory.HeldItemHolder is gone; a future
     * MUI2 panel button should call this.
     */
    private void exportLog(ItemStack heldStack, Player heldPlayer) {
        if (getPos(heldStack) != null && heldPlayer instanceof ServerPlayer player) {
            BlockPos[] blockPos = getPos(heldStack);
            Direction direction = getDir(heldStack);
            StringBuilder builder = new StringBuilder();
            DebugBlockPattern blockPattern = new DebugBlockPattern(
                    heldPlayer.level(),
                    blockPos[0].getX(),
                    blockPos[0].getY(),
                    blockPos[0].getZ(),
                    blockPos[1].getX(),
                    blockPos[1].getY(),
                    blockPos[1].getZ());
            var dirs = DebugBlockPattern.getDir(direction);
            blockPattern.changeDir(dirs[0], dirs[1], dirs[2]);
            player.displayClientMessage(
                    Component.translatable("item.cosmiccore.debug.structure_writer.output_successful"), false);
            for (int i = 0; i < blockPattern.pattern.length; i++) {
                String[] strings = blockPattern.pattern[i];
                builder.append(".slice(\"%s\")\n".formatted(Joiner.on("\", \"").join(strings)));
            }

            // Add legend mapping characters to block resource locations
            builder.append("\n// Block Legend:\n");
            blockPattern.charToBlockMap.forEach((character, resourceLocation) -> {
                if (character == ' ') {
                    builder.append("// ' ' (space) - %s\n".formatted(resourceLocation));
                } else {
                    builder.append("// %c - %s\n".formatted(character, resourceLocation));
                }
            });

            GTCEu.LOGGER.info("\n" + builder.toString());
        }
    }

    private void changeDirX(ItemStack itemStack, Player heldPlayer) {
        if (getPos(itemStack) != null && heldPlayer instanceof ServerPlayer) {
            Direction direction = getDir(itemStack);
            direction = direction.getClockWise(Direction.Axis.X);
            setDir(itemStack, direction);
        }
    }

    private void changeDirY(ItemStack itemStack, Player heldPlayer) {
        if (getPos(itemStack) != null && heldPlayer instanceof ServerPlayer) {
            Direction direction = getDir(itemStack);
            direction = direction.getClockWise(Direction.Axis.Y);
            setDir(itemStack, direction);
        }
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
        return Direction.byName(tag.getString("dir"));
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
        var player = context.getPlayer();
        if (player == null) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(context.getHand());
        if (!player.isShiftKeyDown()) {
            addPos(stack, context.getClickedPos());
        } else {
            removePos(stack);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown()) {
            removePos(stack);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        // Open the structure-writer UI via the MUI2 player-inventory factory (IItemUIHolder default).
        return IItemUIHolder.super.use(item, level, player, usedHand);
    }
}
