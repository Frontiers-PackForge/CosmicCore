package com.ghostipedia.cosmiccore.common.item.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.BreadthFirstBlockSearch;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.TriPredicate;

import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.blockentity.networking.CableBusBlockEntity;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.plaf.multi.MultiTabbedPaneUI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public class InfiniteSprayCanBehavior implements IInteractionItem, IAddInformation, ItemColor {

    @Getter
    @Setter
    private DyeColor color;
    @Getter
    @Setter
    private Boolean isLocked;

    @DescSynced
    boolean isSwinging = true;


    @Override
    public int getColor(ItemStack itemStack, int i) {
        DyeColor stackColor = getColorFromStack(itemStack);
        return stackColor.getTextColor();
    }

    public static DyeColor getColorFromStack(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Color")) {
            return DyeColor.byId(stack.getTag().getInt("Color"));
        }
        return DyeColor.GRAY; // default
    }


    public static void setColorToStack(ItemStack stack, DyeColor color) {
        stack.getOrCreateTag().putInt("Color", color.getId());
    }

    public InfiniteSprayCanBehavior(int color) {
        DyeColor[] colors = DyeColor.values();
        this.color = color >= colors.length || color < 0 ? null : colors[color];
        int colorValue = this.color == null ? 0x969696 : this.color.getTextColor();
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, @NotNull UseOnContext context) {
        isSwinging = false;
        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        boolean isClient = level.isClientSide();
        int maxBlocksToRecolor = Math.max(1,player.isCrouching() ? ConfigHolder.INSTANCE.tools.sprayCanChainLength : 1);
        var first = level.getBlockEntity(pos);
        //middle mouse handler
        //todo middle mouse handler

        if (first == null || !handleSpecialBlockEntities(first, maxBlocksToRecolor, context)) {
            handleBlocks(pos, maxBlocksToRecolor, context);
        }
        GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, player.position(), 1.0f, 1.0f);
        return InteractionResult.PASS;
    }


    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity){

        if(!isSwinging){
            isSwinging = true;
            return true;
        }
        if (entity instanceof Player player) {
        boolean isClient = player.level().isClientSide();
        //does the crouch swap code

        int nextColor = player.isCrouching()
                ? (color.ordinal() - 1 + DyeColor.values().length) % DyeColor.values().length
                : (color.ordinal() + 1) % DyeColor.values().length;
        this.color = DyeColor.values()[nextColor];
        setColorToStack(stack, this.color);

        //message to action bar
        MutableComponent message = Component.literal("Spray Can Color is: ");
        MutableComponent colorComponent = Component.literal(color.toString())
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color.getTextColor())));
        message.append(colorComponent);

        player.displayClientMessage(message, true);
        return false;
    }
    return true;

}


    /*
     * REIMPLEMENTING OLD SPRAY CAN BEHAVIOR FROM GT (maybe mixin) (copied and pasted from gt with durability yeeted)
     * idk if there is a better way to uh, reimplement this. maybe i can extend the class but i couldn't figure it out
     * without it collapsing inwards like my soul
     */

    // vanilla
    private static final ImmutableMap<DyeColor, Block> GLASS_MAP;
    private static final ImmutableMap<DyeColor, Block> GLASS_PANE_MAP;
    private static final ImmutableMap<DyeColor, Block> TERRACOTTA_MAP;
    private static final ImmutableMap<DyeColor, Block> WOOL_MAP;
    private static final ImmutableMap<DyeColor, Block> CARPET_MAP;
    private static final ImmutableMap<DyeColor, Block> CONCRETE_MAP;
    private static final ImmutableMap<DyeColor, Block> CONCRETE_POWDER_MAP;
    private static final ImmutableMap<DyeColor, Block> SHULKER_BOX_MAP;
    private static final ImmutableMap<DyeColor, Block> CANDLE_MAP;

    private static ResourceLocation getId(String modid, DyeColor color, String postfix) {
        return new ResourceLocation(modid, "%s_%s".formatted(color.getSerializedName(), postfix));
    }

    static {
        ImmutableMap.Builder<DyeColor, Block> glassBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> glassPaneBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> terracottaBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> woolBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> carpetBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> concreteBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> concretePowderBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> shulkerBoxBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> candleBuilder = ImmutableMap.builder();

        for (DyeColor color : DyeColor.values()) {
            // if there are > 16 colors (vanilla end) & tinted is loaded, use tinted blocks
            if (color.ordinal() > 15 && GTCEu.isModLoaded(GTValues.MODID_TINTED)) {
                glassBuilder.put(color,
                        BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "stained_glass")));
                glassPaneBuilder.put(color,
                        BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "stained_glass_pane")));
                terracottaBuilder.put(color,
                        BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "terracotta")));
                woolBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "wool")));
                carpetBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "carpet")));
                concreteBuilder.put(color,
                        BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "concrete")));
                concretePowderBuilder.put(color,
                        BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "concrete_powder")));
                shulkerBoxBuilder.put(color,
                        BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "shulker_box")));
                candleBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "candle")));
            } else {
                glassBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "stained_glass")));
                glassPaneBuilder.put(color,
                        BuiltInRegistries.BLOCK.get(getId("minecraft", color, "stained_glass_pane")));
                terracottaBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "terracotta")));
                woolBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "wool")));
                carpetBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "carpet")));
                concreteBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "concrete")));
                concretePowderBuilder.put(color,
                        BuiltInRegistries.BLOCK.get(getId("minecraft", color, "concrete_powder")));
                shulkerBoxBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "shulker_box")));
                candleBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "candle")));
            }
        }
        GLASS_MAP = glassBuilder.build();
        GLASS_PANE_MAP = glassPaneBuilder.build();
        TERRACOTTA_MAP = terracottaBuilder.build();
        WOOL_MAP = woolBuilder.build();
        CARPET_MAP = carpetBuilder.build();
        CONCRETE_MAP = concreteBuilder.build();
        CONCRETE_POWDER_MAP = concretePowderBuilder.build();
        SHULKER_BOX_MAP = shulkerBoxBuilder.build();
        CANDLE_MAP = candleBuilder.build();

    }

    private static int mixColors(float ratio, int... colors) {
        int r = 0, g = 0, b = 0;
        ratio = ratio * (1.0f / colors.length);
        for (int color : colors) {
            r += FastColor.ARGB32.red(color) * ratio;
            g += FastColor.ARGB32.green(color) * ratio;
            b += FastColor.ARGB32.blue(color) * ratio;
        }
        return FastColor.ARGB32.color(255, r, g, b);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        if (color != null) {
            tooltipComponents
                    .add(Component.translatable("behaviour.paintspray." + this.color.getSerializedName() + ".tooltip"));
        } else {
            tooltipComponents.add(Component.translatable("behaviour.paintspray.solvent.tooltip"));
        }
    }

    private static boolean paintPaintable(IPaintable paintable, DyeColor color) {
        if (color == null) {
            if (paintable.getPaintingColor() == -1) {
                return false;
            }
            paintable.setPaintingColor(-1);
        } else if (paintable.getPaintingColor() != color.getTextColor()) {
            paintable.setPaintingColor(color.getTextColor());
        } else {
            return false;
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    private boolean handleSpecialBlockEntities(BlockEntity first, int limit, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) {
            return false;
        }
        if (GTCEu.Mods.isAE2Loaded() && InfiniteSprayCanBehavior.AE2CallWrapper.isAE2Cable(first)) {
            var collected = InfiniteSprayCanBehavior.AE2CallWrapper.collect(first, limit);
            var ae2Color = color == null ? AEColor.TRANSPARENT : AEColor.values()[color.ordinal()];
            for (var c : collected) {
                if (c.getColor() == ae2Color) {
                    continue;
                }
                c.recolourBlock(null, ae2Color, player);
            }
        } else if (first instanceof PipeBlockEntity pipe) {
            var collected = BreadthFirstBlockSearch.conditionalBlockEntitySearch(PipeBlockEntity.class, pipe,
                    gtPipePredicate, limit, limit * 6);
            for (var c : collected) {
                if (!paintPaintable(c, color)) {
                    continue;
                }
            }
        } else if (first instanceof MetaMachineBlockEntity mmbe) {
            var collected = BreadthFirstBlockSearch.conditionalBlockEntitySearch(MetaMachineBlockEntity.class, mmbe,
                    gtMetaMachinePredicate, limit, limit * 6);
            for (var c : collected) {
                if (!paintPaintable(c.getMetaMachine(), color)) {
                    continue;
                }
            }

        } else if (first instanceof IPaintable) {
            var collected = BreadthFirstBlockSearch.conditionalBlockEntitySearch(BlockEntity.class, first,
                    paintablePredicateWrapper, limit, limit * 6);
            for (var c : collected) {
                if (!paintPaintable((IPaintable) c, color)) {
                    continue;
                }
            }
        } else if (first instanceof ShulkerBoxBlockEntity shulkerBoxBE) {
            var tag = shulkerBoxBE.saveWithFullMetadata();
            var level = first.getLevel();
            var pos = first.getBlockPos();
            recolorBlockNoState(SHULKER_BOX_MAP, color, level, pos, Blocks.SHULKER_BOX);
            if (level.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity newShulker) {
                newShulker.load(tag);
            }
        } else {
            return false;
        }
        return true;
    }

    private void handleBlocks(BlockPos start, int limit, UseOnContext context) {
        final var level = context.getLevel();
        var player = context.getPlayer();
        if (player == null) {
            return;
        }
        var stack = context.getItemInHand();
        var collected = BreadthFirstBlockSearch
                .conditionalBlockPosSearch(start,
                        (parent, child) -> parent == null ||
                                level.getBlockState(child).is(level.getBlockState(parent).getBlock()),
                        limit, limit * 6);
        for (var pos : collected) {
            if (!tryPaintBlock(level, pos)) {
                break;
            }
        }
    }

    private boolean tryPaintBlock(Level world, BlockPos pos) {
        var blockState = world.getBlockState(pos);
        var block = blockState.getBlock();
        if (color == null) {
            return tryStripBlockColor(world, pos, block);
        }
        return recolorBlockState(world, pos, color) || tryPaintSpecialBlock(world, pos, block);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean recolorBlockState(Level level, BlockPos pos, DyeColor color) {
        BlockState state = level.getBlockState(pos);
        for (Property property : state.getProperties()) {
            if (property.getValueClass() == DyeColor.class) {
                state.setValue(property, color);
                return true;
            }
        }
        return false;
    }

    private boolean tryPaintSpecialBlock(Level world, BlockPos pos, Block block) {
        if (block.defaultBlockState().is(Tags.Blocks.GLASS)) {
            if (recolorBlockNoState(GLASS_MAP, this.color, world, pos, Blocks.GLASS)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(Tags.Blocks.GLASS_PANES)) {
            if (recolorBlockNoState(GLASS_PANE_MAP, this.color, world, pos, Blocks.GLASS_PANE)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.TERRACOTTA)) {
            if (recolorBlockNoState(TERRACOTTA_MAP, this.color, world, pos, Blocks.TERRACOTTA)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.WOOL)) {
            if (recolorBlockNoState(WOOL_MAP, this.color, world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.WOOL_CARPETS)) {
            if (recolorBlockNoState(CARPET_MAP, this.color, world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_BLOCK)) {
            if (recolorBlockNoState(CONCRETE_MAP, this.color, world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_POWDER_BLOCK)) {
            if (recolorBlockNoState(CONCRETE_POWDER_MAP, this.color, world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.CANDLES)) {
            if (recolorBlockNoState(CANDLE_MAP, this.color, world, pos)) {
                return true;
            }
        }
        return false;
    }

    private static boolean recolorBlockNoState(Map<DyeColor, Block> map, DyeColor color, Level world, BlockPos pos) {
        return recolorBlockNoState(map, color, world, pos, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean recolorBlockNoState(Map<DyeColor, Block> map, DyeColor color, Level world, BlockPos pos,
                                               Block _default) {
        Block newBlock = map.getOrDefault(color, _default);
        BlockState old = world.getBlockState(pos);
        if (newBlock == Blocks.AIR) newBlock = _default;
        if (newBlock != null && newBlock != old.getBlock()) {
            BlockState state = newBlock.defaultBlockState();
            for (Property property : old.getProperties()) {
                state.setValue(property, old.getValue(property));
            }
            world.setBlock(pos, state, 3);
            world.sendBlockUpdated(pos, old, state, 3);
            return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean tryStripBlockColor(Level world, BlockPos pos, Block block) {
        // MC special cases
        if (block instanceof StainedGlassBlock) {
            world.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
            return true;
        }
        if (block instanceof StainedGlassPaneBlock) {
            world.setBlock(pos, Blocks.GLASS_PANE.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.TERRACOTTA) && block != Blocks.TERRACOTTA) {
            world.setBlock(pos, Blocks.TERRACOTTA.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.WOOL) && block != Blocks.WHITE_WOOL) {
            world.setBlock(pos, Blocks.WHITE_WOOL.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.WOOL_CARPETS) && block != Blocks.WHITE_CARPET) {
            world.setBlock(pos, Blocks.WHITE_CARPET.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_BLOCK) && block != Blocks.WHITE_CONCRETE) {
            world.setBlock(pos, Blocks.WHITE_CONCRETE.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_POWDER_BLOCK) && block != Blocks.WHITE_CONCRETE_POWDER) {
            world.setBlock(pos, Blocks.WHITE_CONCRETE_POWDER.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.CANDLES) && block != Blocks.WHITE_CANDLE) {
            recolorBlockNoState(CANDLE_MAP, DyeColor.WHITE, world, pos);
            return true;
        }

        // General case
        BlockState state = world.getBlockState(pos);
        for (Property prop : state.getProperties()) {
            if (prop.getValueClass() == DyeColor.class) {
                BlockState defaultState = block.defaultBlockState();
                DyeColor defaultColor = DyeColor.WHITE;
                try {
                    // try to read the default color value from the default state instead of just
                    // blindly setting it to default state, and potentially resetting other values
                    defaultColor = (DyeColor) defaultState.getValue(prop);
                } catch (IllegalArgumentException ignored) {
                    // no default color, we may have to fallback to WHITE here
                    // other mods that have custom behavior can be done as
                    // special cases above on a case-by-case basis
                }
                recolorBlockState(world, pos, defaultColor);
                return true;
            }
        }

        return false;
    }

    private static final BiPredicate<IPaintable, IPaintable> paintablePredicate = (parent, child) -> {
        if (!parent.getClass().equals(child.getClass())) {
            return false;
        }
        return parent.getPaintingColor() == child.getPaintingColor();
    };

    private static final TriPredicate<BlockEntity, BlockEntity, Direction> paintablePredicateWrapper = (parent, child,
                                                                                                        direction) -> {
        if (parent == null && child instanceof IPaintable) return true;
        return parent instanceof IPaintable pp && child instanceof IPaintable pc && paintablePredicate.test(pp, pc);
    };

    @SuppressWarnings("rawtypes")
    private static final TriPredicate<PipeBlockEntity, PipeBlockEntity, Direction> gtPipePredicate = (parent, child,
                                                                                                      direction) -> {
        if (parent == null) return true;
        if (!paintablePredicate.test(parent, child)) {
            return false;
        }
        return parent.isConnected(direction) && child.isConnected(direction.getOpposite());
    };

    private static final TriPredicate<MetaMachineBlockEntity, MetaMachineBlockEntity, Direction> gtMetaMachinePredicate = (parent,
                                                                                                                           child,
                                                                                                                           direction) -> {
        if (parent == null) return true;
        return paintablePredicate.test(parent.getMetaMachine(), child.getMetaMachine()) &&
                parent.getMetaMachine().getDefinition().equals(child.getMetaMachine().getDefinition());
    };


    private static class AE2CallWrapper {

        static Set<CableBusBlockEntity> collect(BlockEntity first, int limit) {
            return BreadthFirstBlockSearch.conditionalBlockEntitySearch(CableBusBlockEntity.class,
                    (CableBusBlockEntity) first,
                    InfiniteSprayCanBehavior.AE2CallWrapper::ae2CablePredicate,
                    limit, limit * 6);
        }

        static boolean isAE2Cable(BlockEntity be) {
            return be instanceof CableBusBlockEntity;
        }

        static boolean ae2CablePredicate(CableBusBlockEntity parent, CableBusBlockEntity child, Direction direction) {
            if (parent == null) return true;
            var childDirection = direction.getOpposite();
            if (parent.getPart(direction) != null || parent.getCableConnectionType(direction) == AECableType.NONE ||
                    child.getPart(childDirection) != null ||
                    child.getCableConnectionType(childDirection) == AECableType.NONE ||
                    parent.getColor() != child.getColor()) {
                return false;
            }
            return true;
        }
    }
}
