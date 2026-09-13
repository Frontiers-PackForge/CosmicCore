package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.utils.ItemData;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record SprayCanState(ExtendedDyeColor color, boolean locked, SprayMode mode, boolean rainSealant) {

    public static final int DEFAULT_COLOR_ID = 1;
    public static final String COLOR_TAG = "color";
    private static final String LOCKED_TAG = "spray_can_locked";
    private static final String MODE_TAG = "spray_can_mode";
    private static final String RAIN_SEALANT_TAG = "spray_can_rain_sealant";
    private static final String SELECTION_TAG = "spray_can_selection";
    private static final String SELECTION_DIMENSION_TAG = "dimension";
    private static final String SELECTION_POSITION_TAG = "position";

    public static SprayCanState read(ItemStack stack) {
        return read(ItemData.readTag(stack));
    }

    static SprayCanState read(CompoundTag tag) {
        int colorId = tag.contains(COLOR_TAG) ? tag.getInt(COLOR_TAG) : DEFAULT_COLOR_ID;
        ExtendedDyeColor color = colorId == -1 ? ExtendedDyeColor.SOLVENT : colorId >= 0 && colorId < 16 ?
                ExtendedDyeColor.getColorFromDyeId(colorId) : ExtendedDyeColor.getColorFromDyeId(DEFAULT_COLOR_ID);
        int modeId = tag.contains(MODE_TAG) ? tag.getInt(MODE_TAG) : SprayMode.LINE.ordinal();
        SprayMode[] modes = SprayMode.values();
        SprayMode mode = modeId >= 0 && modeId < modes.length ? modes[modeId] : SprayMode.LINE;
        return new SprayCanState(color, tag.getBoolean(LOCKED_TAG), mode, tag.getBoolean(RAIN_SEALANT_TAG));
    }

    public void write(ItemStack stack) {
        ItemData.mutateTag(stack, this::write);
    }

    void write(CompoundTag tag) {
        tag.putInt(COLOR_TAG, color.getColorId());
        tag.putBoolean(LOCKED_TAG, locked);
        tag.putInt(MODE_TAG, mode.ordinal());
        tag.putBoolean(RAIN_SEALANT_TAG, rainSealant);
    }

    public SprayCanState withColor(ExtendedDyeColor newColor) {
        return new SprayCanState(newColor, locked, mode, false);
    }

    public SprayCanState withLocked(boolean newLocked) {
        return new SprayCanState(color, newLocked, mode, rainSealant);
    }

    public SprayCanState withMode(SprayMode newMode) {
        return new SprayCanState(color, locked, newMode, rainSealant);
    }

    public SprayCanState withRainSealant(boolean newRainSealant) {
        return new SprayCanState(color, locked, mode, newRainSealant);
    }

    public SprayCanState cycle(int direction) {
        ExtendedDyeColor[] colors = ExtendedDyeColor.values();
        int next = Math.floorMod(color.ordinal() + Integer.signum(direction), colors.length);
        return withColor(colors[next]);
    }

    public static float modelColor(ItemStack stack) {
        return read(stack).color().getColorId();
    }

    public static Optional<Selection> selection(ItemStack stack) {
        return selection(ItemData.readTag(stack));
    }

    static Optional<Selection> selection(CompoundTag root) {
        if (!root.contains(SELECTION_TAG)) return Optional.empty();
        CompoundTag selection = root.getCompound(SELECTION_TAG);
        ResourceLocation dimension = ResourceLocation.tryParse(selection.getString(SELECTION_DIMENSION_TAG));
        if (dimension == null || !selection.contains(SELECTION_POSITION_TAG)) return Optional.empty();
        return Optional.of(new Selection(dimension, BlockPos.of(selection.getLong(SELECTION_POSITION_TAG))));
    }

    public static void select(ItemStack stack, ResourceLocation dimension, BlockPos position) {
        ItemData.mutateTag(stack, root -> select(root, dimension, position));
    }

    static void select(CompoundTag root, ResourceLocation dimension, BlockPos position) {
        CompoundTag selection = new CompoundTag();
        selection.putString(SELECTION_DIMENSION_TAG, dimension.toString());
        selection.putLong(SELECTION_POSITION_TAG, position.asLong());
        root.put(SELECTION_TAG, selection);
    }

    public static void clearSelection(ItemStack stack) {
        ItemData.mutateTag(stack, root -> root.remove(SELECTION_TAG));
    }

    static void clearSelection(CompoundTag root) {
        root.remove(SELECTION_TAG);
    }

    public record Selection(ResourceLocation dimension, BlockPos position) {}

    public enum SprayMode {
        LINE,
        CONNECTED_COLOR,
        A_B
    }
}
