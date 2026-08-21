package com.ghostipedia.cosmiccore.integration.jade;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.EmberHatchPartMachine;
import com.ghostipedia.cosmiccore.ember.ICosmicEmberStats;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.rekindled.embers.api.capabilities.EmbersCapabilities;
import com.rekindled.embers.api.power.IEmberCapability;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

public enum CosmicEmberProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String DATA = "CosmicEmberMachineData";
    private static final String EMBER = "Ember";
    private static final String AMOUNT = "Amount";
    private static final String CAPACITY = "Capacity";
    private static final String VOLATILE = "Volatile";
    private static final String TRANSFER = "Transfer";

    private static final int BAR_START = -34304;
    private static final int BAR_END = -2536960;
    private static final int BAR_TEXT = -1;
    private static final int BAR_BACKGROUND = -13494518;
    private static final int BAR_BORDER = -9752040;

    @Override
    public ResourceLocation getUid() {
        return CosmicCoreJadePlugin.EMBER_DETAILS;
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return isCosmicEmberBlock(accessor);
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!isCosmicEmberBlock(accessor)) {
            return;
        }
        IEmberCapability cap = getEmber(accessor);
        if (cap == null) {
            return;
        }
        if (cap.getEmberCapacity() <= 0 && cap.getEmber() <= 0) {
            return;
        }
        CompoundTag ember = new CompoundTag();
        ember.putDouble(AMOUNT, cap.getEmber());
        ember.putDouble(CAPACITY, cap.getEmberCapacity());
        ember.putBoolean(VOLATILE, cap.acceptsVolatile());
        if (accessor.getBlockEntity() instanceof ICosmicEmberStats stats) {
            ember.putDouble(TRANSFER, stats.transfer());
        }
        CompoundTag root = new CompoundTag();
        root.put(EMBER, ember);
        data.put(DATA, root);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag server = accessor.getServerData();
        if (!server.contains(DATA, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag root = server.getCompound(DATA);
        if (!root.contains(EMBER, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag ember = root.getCompound(EMBER);
        double amount = ember.getDouble(AMOUNT);
        double capacity = ember.getDouble(CAPACITY);
        float fill = capacity > 0 ? (float) Math.max(0.0D, Math.min(1.0D, amount / capacity)) : 0.0F;

        Component label = Component.translatable(
                "cosmiccore.jade.ember",
                CosmicJadeFormatting.fixedTwoDecimals(amount),
                CosmicJadeFormatting.fixedTwoDecimals(capacity))
                .withStyle(ChatFormatting.WHITE);

        IElementHelper helper = IElementHelper.get();
        ProgressStyle style = helper.progressStyle().color(BAR_START, BAR_END).textColor(BAR_TEXT);
        BoxStyle.GradientBorder box = BoxStyle.GradientBorder.DEFAULT_NESTED_BOX.clone();
        box.bgColor = BAR_BACKGROUND;
        box.borderColor = new int[] { BAR_BORDER, BAR_BORDER, BAR_BORDER, BAR_BORDER };
        box.roundCorner = Boolean.FALSE;
        tooltip.add(helper.progress(fill, label, style, box, true));

        if (ember.getBoolean(VOLATILE)) {
            tooltip.add(Component.translatable("cosmiccore.jade.ember.volatile").withStyle(ChatFormatting.WHITE));
        }
        if (ember.contains(TRANSFER, Tag.TAG_DOUBLE)) {
            tooltip.add(Component.translatable(
                    "cosmiccore.jade.ember.transfer",
                    CosmicJadeFormatting.fixedTwoDecimals(ember.getDouble(TRANSFER)))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static boolean isCosmicEmberBlock(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ICosmicEmberStats) {
            return true;
        }
        return MetaMachine.getMachine(accessor.getLevel(), accessor.getPosition()) instanceof EmberHatchPartMachine;
    }

    private static IEmberCapability getEmber(BlockAccessor accessor) {
        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        BlockState state = accessor.getBlockState();
        return level.getCapability(EmbersCapabilities.EMBER_BLOCK_CAPABILITY, pos, state,
                accessor.getBlockEntity(), accessor.getSide());
    }
}
