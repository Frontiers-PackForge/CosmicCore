package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.compat.gtceu.RainSealant;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.BreadthFirstBlockSearch;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.TriPredicate;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.blockentity.networking.CableBusBlockEntity;
import com.google.common.collect.ImmutableMap;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.Protection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class InfiniteSprayCanBehavior implements IInteractionItem, IAddInformation {

    public InfiniteSprayCanBehavior() {}

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (level.isClientSide && player.isShiftKeyDown()) {
            SprayCanClientHandler.openScreen(player, usedHand);
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        }
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, @NotNull UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        SprayCanState state = SprayCanState.read(itemStack);
        if (state.rainSealant()) return applyRainSealant(context);
        int maxBlocksToRecolor = Math.max(1, ConfigHolder.INSTANCE.tools.sprayCanChainLength);
        if (player.isShiftKeyDown() && state.mode() == SprayCanState.SprayMode.A_B) {
            handleABSelection(itemStack, context, maxBlocksToRecolor);
            return InteractionResult.SUCCESS;
        }
        int limit = player.isShiftKeyDown() ? maxBlocksToRecolor : 1;
        var first = level.getBlockEntity(pos);

        if (player.isShiftKeyDown() && state.mode() == SprayCanState.SprayMode.LINE) {
            if (first instanceof ShulkerBoxBlockEntity) {
                handleSpecialBlockEntities(first, 1, context);
            } else {
                Direction direction = Direction.getNearest(player.getLookAngle().x, player.getLookAngle().y,
                        player.getLookAngle().z);
                paintLine(validPrefix(SprayCanTraversal.line(pos, direction, limit), context), context);
            }
        } else if (first == null || !handleSpecialBlockEntities(first, limit, context)) {
            handleBlocks(pos, limit, context);
        }
        GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, player.position(), 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    InteractionResult applyRainSealant(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.mayBuild()) return InteractionResult.PASS;
        var machine = MetaMachine.getMachine(context.getLevel(), context.getClickedPos());
        if (machine == null) return InteractionResult.PASS;
        var targets = RainSealant.targets(machine);
        if (targets.isEmpty()) return InteractionResult.PASS;
        for (var target : targets) {
            if (!canEdit(context, target.getBlockPos())) return InteractionResult.FAIL;
        }
        int changed = RainSealant.apply(targets);
        if (changed == 0) {
            player.displayClientMessage(Component.translatable("cosmiccore.sealant.already_sealed"), true);
            return InteractionResult.CONSUME;
        }
        GTSoundEntries.SPRAY_CAN_TOOL.play(context.getLevel(), null, player.position(), 1.0f, 1.0f);
        player.displayClientMessage(Component.translatable("cosmiccore.sealant.applied", changed), true);
        return InteractionResult.CONSUME;
    }

    private void handleABSelection(ItemStack stack, UseOnContext context, int limit) {
        Player player = context.getPlayer();
        if (player == null) return;
        ResourceLocation dimension = context.getLevel().dimension().location();
        BlockPos clicked = context.getClickedPos();
        var selection = SprayCanState.selection(stack);
        if (selection.isEmpty()) {
            SprayCanState.select(stack, dimension, clicked);
            player.displayClientMessage(Component.translatable("cosmiccore.item.spraycan.ab.selected"), true);
            return;
        }
        SprayCanState.clearSelection(stack);
        var first = selection.get();
        List<BlockPos> positions = first.dimension().equals(dimension) ?
                SprayCanTraversal.between(first.position(), clicked, limit) : List.of();
        if (!SprayCanTraversal.isComplete(positions, candidate -> canPaintLine(candidate, context))) {
            player.displayClientMessage(Component.translatable("cosmiccore.item.spraycan.ab.invalid"), true);
            return;
        }
        paintLine(positions, context);
        GTSoundEntries.SPRAY_CAN_TOOL.play(context.getLevel(), null, player.position(), 1.0f, 1.0f);
    }

    public static void printColorToActionBar(Player player, ExtendedDyeColor color) {
        String colorName = color.name().replace('_', ' ');
        MutableComponent colorComponent = Component.literal(colorName)
                .setStyle(
                        color == ExtendedDyeColor.SOLVENT ? Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)) :
                                Style.EMPTY.withColor(TextColor.fromRgb(color.getTextColor())));

        player.displayClientMessage(
                Component.translatable("cosmiccore.item.spraycan.actionbar.color", colorComponent), true);
    }

    public static Component lockedMessage() {
        return Component.translatable("cosmiccore.item.spraycan.locked")
                .withStyle(style -> style.withColor(ChatFormatting.RED).withBold(true));
    }

    public static Component lockMessage(boolean locked) {
        return Component.translatable(locked ? "cosmiccore.item.spraycan.now_locked" :
                "cosmiccore.item.spraycan.now_unlocked");
    }

    // vanilla
    private static final ImmutableMap<DyeColor, Block> GLASS_MAP;
    private static final ImmutableMap<DyeColor, Block> GLASS_PANE_MAP;
    private static final Map<DyeColor, Block> TERRACOTTA_MAP;
    private static final Map<DyeColor, Block> WOOL_MAP;
    private static final Map<DyeColor, Block> CARPET_MAP;
    private static final Map<DyeColor, Block> CONCRETE_MAP;
    private static final Map<DyeColor, Block> CONCRETE_POWDER_MAP;
    private static final Map<DyeColor, Block> SHULKER_BOX_MAP;
    private static final Map<DyeColor, Block> CANDLE_MAP;

    private static ResourceLocation getId(String modid, DyeColor color, String postfix) {
        return ResourceLocation.fromNamespaceAndPath(modid, "%s_%s".formatted(color.getSerializedName(), postfix));
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

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag flag) {
        SprayCanState state = SprayCanState.read(stack);
        ExtendedDyeColor color = state.color();
        tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.lclick"));
        tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.lclick_sneak"));
        tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.rclick"));
        tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.rclick_sneak"));
        tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.rclick_offhand"));
        tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.middle"));
        tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.middle_sneak"));
        tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.current_mode",
                Component.translatable("cosmiccore.item.spraycan.mode." + state.mode().name().toLowerCase())));

        if (state.locked()) {
            tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.locked")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }

        if (color != null) {
            tooltip.add(Component
                    .translatable("cosmiccore.item.spraycan.tooltip.current_color", color.getSerializedName())
                    .setStyle(
                            color == ExtendedDyeColor.SOLVENT ? Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)) // white
                                    : Style.EMPTY.withColor(TextColor.fromRgb(color.getTextColor()))));
        } else {
            tooltip.add(Component.translatable("cosmiccore.item.spraycan.tooltip.solvent_mode")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    private static boolean paintPaintable(IPaintable paintable, ExtendedDyeColor color) {
        if (color.isSolvent()) {
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
    public boolean handleSpecialBlockEntities(BlockEntity first, int limit, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) {
            return false;
        }
        ExtendedDyeColor color = SprayCanState.read(context.getItemInHand()).color();
        Predicate<BlockPos> authorization = pos -> canEdit(context, pos);
        if (GTCEu.Mods.isAE2Loaded() && AE2CallWrapper.isAE2Cable(first)) {
            var collected = AE2CallWrapper.collect(first, limit, authorization);
            var ae2Color = color.isSolvent() ? AEColor.TRANSPARENT : AEColor.fromDye(color.getColor());
            for (var c : collected) {
                if (c.getColor() == ae2Color) {
                    continue;
                }
                c.recolourBlock(null, ae2Color, player);
            }
        } else if (first instanceof PipeBlockEntity pipe) {
            var collected = BreadthFirstBlockSearch.conditionalBlockEntitySearch(PipeBlockEntity.class, pipe,
                    (parent, child, direction) -> SprayCanTraversal.canEnter(child.getBlockPos(), authorization,
                            gtPipePredicate.test(parent, child, direction)),
                    limit, limit * 6);
            for (var c : collected) {
                if (!paintPaintable(c, color)) {
                    continue;
                }
            }
        } else if (first instanceof MetaMachine mmbe) {
            var collected = BreadthFirstBlockSearch.conditionalBlockEntitySearch(MetaMachine.class, mmbe,
                    (parent, child, direction) -> SprayCanTraversal.canEnter(child.getBlockPos(), authorization,
                            gtMetaMachinePredicate.test(parent, child, direction)),
                    limit, limit * 6);
            for (var c : collected) {
                if (!paintPaintable(c, color)) {
                    continue;
                }
            }

        } else if (first instanceof IPaintable) {
            var collected = BreadthFirstBlockSearch.conditionalBlockEntitySearch(BlockEntity.class, first,
                    (parent, child, direction) -> SprayCanTraversal.canEnter(child.getBlockPos(), authorization,
                            paintablePredicateWrapper.test(parent, child, direction)),
                    limit, limit * 6);
            for (var c : collected) {
                if (!paintPaintable((IPaintable) c, color)) {
                    continue;
                }
            }
        } else if (first instanceof ShulkerBoxBlockEntity shulkerBoxBE) {
            var level = first.getLevel();
            var pos = first.getBlockPos();
            var tag = shulkerBoxBE.saveWithFullMetadata(level.registryAccess());
            recolorBlockNoState(SHULKER_BOX_MAP, color.getColor(), level, pos, Blocks.SHULKER_BOX);
            if (level.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity newShulker) {
                newShulker.loadWithComponents(tag, level.registryAccess());
            }
        } else {
            return false;
        }
        return true;
    }

    public void handleBlocks(BlockPos start, int limit, UseOnContext context) {
        final var level = context.getLevel();
        var player = context.getPlayer();
        if (player == null) {
            return;
        }
        var stack = context.getItemInHand();
        ExtendedDyeColor color = SprayCanState.read(stack).color();
        Predicate<BlockPos> authorization = pos -> canEdit(context, pos);
        var collected = BreadthFirstBlockSearch
                .conditionalBlockPosSearch(start,
                        (parent, child) -> SprayCanTraversal.canEnter(child, authorization,
                                parent == null ||
                                        level.getBlockState(child).is(level.getBlockState(parent).getBlock())),
                        limit, limit * 6);
        for (var pos : collected) {
            if (!tryPaintBlock(level, pos, color)) {
                break;
            }
        }
    }

    private boolean canPaintLine(List<BlockPos> positions, UseOnContext context) {
        if (positions.isEmpty()) return false;
        if (!SprayCanTraversal.allAuthorized(positions, pos -> canEdit(context, pos))) return false;
        Level level = context.getLevel();
        BlockEntity seed = level.getBlockEntity(positions.getFirst());
        if (seed instanceof CableBusBlockEntity cable) return AE2CallWrapper.matchesLine(cable, positions, level);
        if (seed instanceof PipeBlockEntity pipe) return matchesPipeLine(pipe, positions, level);
        if (seed instanceof MetaMachine machine) return matchesMachineLine(machine, positions, level);
        if (seed instanceof IPaintable paintable) return matchesPaintableLine(paintable, positions, level);
        Block seedBlock = level.getBlockState(positions.getFirst()).getBlock();
        return positions.stream().allMatch(pos -> level.hasChunkAt(pos) && level.getBlockState(pos).is(seedBlock));
    }

    private static boolean canEdit(UseOnContext context, BlockPos pos) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return false;
        Level level = context.getLevel();
        if (!level.isInWorldBounds(pos) || !level.mayInteract(player, pos)) return false;
        if (!ModList.get().isLoaded("ftbchunks")) return true;
        return FTBChunksProtection.canEdit(player, context.getHand(), pos);
    }

    private List<BlockPos> validPrefix(List<BlockPos> positions, UseOnContext context) {
        return SprayCanTraversal.validPrefix(positions, candidate -> canPaintLine(candidate, context));
    }

    private void paintLine(List<BlockPos> positions, UseOnContext context) {
        if (!canPaintLine(positions, context)) return;
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return;
        ExtendedDyeColor color = SprayCanState.read(context.getItemInHand()).color();
        BlockEntity seed = level.getBlockEntity(positions.getFirst());
        if (seed instanceof CableBusBlockEntity) {
            AEColor aeColor = color.isSolvent() ? AEColor.TRANSPARENT : AEColor.fromDye(color.getColor());
            for (BlockPos pos : positions) {
                ((CableBusBlockEntity) level.getBlockEntity(pos)).recolourBlock(null, aeColor, player);
            }
        } else if (seed instanceof IPaintable) {
            for (BlockPos pos : positions) {
                paintPaintable((IPaintable) level.getBlockEntity(pos), color);
            }
        } else {
            for (BlockPos pos : positions) {
                tryPaintBlock(level, pos, color);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static boolean matchesPipeLine(PipeBlockEntity seed, List<BlockPos> positions, Level level) {
        PipeBlockEntity previous = null;
        for (BlockPos pos : positions) {
            if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) ||
                    !samePipeType(seed, pipe))
                return false;
            if (previous != null) {
                Direction direction = directionBetween(previous.getBlockPos(), pos);
                if (!previous.isConnected(direction) || !pipe.isConnected(direction.getOpposite())) return false;
            }
            previous = pipe;
        }
        return true;
    }

    private static boolean matchesMachineLine(MetaMachine seed, List<BlockPos> positions, Level level) {
        return positions.stream().allMatch(pos -> level.hasChunkAt(pos) &&
                level.getBlockEntity(pos) instanceof MetaMachine machine &&
                seed.getDefinition().equals(machine.getDefinition()));
    }

    private static boolean matchesPaintableLine(IPaintable seed, List<BlockPos> positions, Level level) {
        return positions.stream().allMatch(pos -> level.hasChunkAt(pos) &&
                level.getBlockEntity(pos) instanceof IPaintable paintable &&
                seed.getClass().equals(paintable.getClass()));
    }

    @SuppressWarnings("rawtypes")
    private static boolean samePipeType(PipeBlockEntity first, PipeBlockEntity second) {
        return pipeSignature(first).equals(pipeSignature(second));
    }

    @SuppressWarnings("rawtypes")
    private static SprayCanTraversal.ConnectionSignature pipeSignature(PipeBlockEntity pipe) {
        return new SprayCanTraversal.ConnectionSignature(pipe.getPipeBlock(), pipe.getPipeType(), pipe.getNodeData());
    }

    private static Direction directionBetween(BlockPos first, BlockPos second) {
        return Direction.getNearest(second.getX() - first.getX(), second.getY() - first.getY(),
                second.getZ() - first.getZ());
    }

    private boolean tryPaintBlock(Level world, BlockPos pos, ExtendedDyeColor color) {
        var blockState = world.getBlockState(pos);
        var block = blockState.getBlock();
        if (color.isSolvent()) {
            return tryStripBlockColor(world, pos, block);
        }
        return recolorBlockState(world, pos, color) || tryPaintSpecialBlock(world, pos, block, color);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean recolorBlockState(Level level, BlockPos pos, ExtendedDyeColor color) {
        BlockState state = level.getBlockState(pos);
        for (Property property : state.getProperties()) {
            if (property.getValueClass() == DyeColor.class) {
                state.setValue(property, color);
                return true;
            }
        }
        return false;
    }

    private boolean tryPaintSpecialBlock(Level world, BlockPos pos, @NotNull Block block, ExtendedDyeColor color) {
        if (block.defaultBlockState().is(Tags.Blocks.GLASS_BLOCKS)) {
            if (recolorBlockNoState(GLASS_MAP, color.getColor(), world, pos, Blocks.GLASS)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(Tags.Blocks.GLASS_PANES)) {
            if (recolorBlockNoState(GLASS_PANE_MAP, color.getColor(), world, pos, Blocks.GLASS_PANE)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.TERRACOTTA)) {
            if (recolorBlockNoState(TERRACOTTA_MAP, color.getColor(), world, pos, Blocks.TERRACOTTA)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.WOOL)) {
            if (recolorBlockNoState(WOOL_MAP, color.getColor(), world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.WOOL_CARPETS)) {
            if (recolorBlockNoState(CARPET_MAP, color.getColor(), world, pos)) {
                return true;
            }
        }
        if (CONCRETE_MAP.containsValue(block)) {
            if (recolorBlockNoState(CONCRETE_MAP, color.getColor(), world, pos)) {
                return true;
            }
        }
        if (CONCRETE_POWDER_MAP.containsValue(block)) {
            if (recolorBlockNoState(CONCRETE_POWDER_MAP, color.getColor(), world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.CANDLES)) {
            if (recolorBlockNoState(CANDLE_MAP, color.getColor(), world, pos)) {
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
        if (CONCRETE_MAP.containsValue(block) && block != Blocks.WHITE_CONCRETE) {
            world.setBlock(pos, Blocks.WHITE_CONCRETE.defaultBlockState(), 3);
            return true;
        }
        if (CONCRETE_POWDER_MAP.containsValue(block) && block != Blocks.WHITE_CONCRETE_POWDER) {
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
            if (prop.getValueClass() == ExtendedDyeColor.class) {
                BlockState defaultState = block.defaultBlockState();
                ExtendedDyeColor defaultColor = ExtendedDyeColor.WHITE;
                try {
                    // try to read the default color value from the default state instead of just
                    // blindly setting it to default state, and potentially resetting other values
                    defaultColor = (ExtendedDyeColor) defaultState.getValue(prop);
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
        boolean connected = parent.isConnected(direction) && child.isConnected(direction.getOpposite());
        return SprayCanTraversal.connectedColorMatches(pipeSignature(parent), parent.getPaintingColor(),
                pipeSignature(child), child.getPaintingColor(), connected);
    };

    private static final TriPredicate<MetaMachine, MetaMachine, Direction> gtMetaMachinePredicate = (parent,
                                                                                                     child,
                                                                                                     direction) -> {
        if (parent == null) return true;
        return paintablePredicate.test(parent, child) &&
                parent.getDefinition().equals(child.getDefinition());
    };

    private static class AE2CallWrapper {

        static Set<CableBusBlockEntity> collect(BlockEntity first, int limit, Predicate<BlockPos> authorization) {
            return BreadthFirstBlockSearch.conditionalBlockEntitySearch(CableBusBlockEntity.class,
                    (CableBusBlockEntity) first,
                    (parent, child, direction) -> SprayCanTraversal.canEnter(child.getBlockPos(), authorization,
                            ae2CablePredicate(parent, child, direction)),
                    limit, limit * 6);
        }

        static boolean isAE2Cable(BlockEntity be) {
            return be instanceof CableBusBlockEntity;
        }

        static boolean ae2CablePredicate(CableBusBlockEntity parent, CableBusBlockEntity child, Direction direction) {
            if (parent == null) return true;
            var childDirection = direction.getOpposite();
            boolean connected = parent.getPart(direction) == null &&
                    parent.getCableConnectionType(direction) != AECableType.NONE &&
                    child.getPart(childDirection) == null &&
                    child.getCableConnectionType(childDirection) != AECableType.NONE;
            return SprayCanTraversal.connectedColorMatches(signature(parent), parent.getColor().ordinal(),
                    signature(child), child.getColor().ordinal(), connected);
        }

        static SprayCanTraversal.ConnectionSignature signature(CableBusBlockEntity cable) {
            if (cable.getPart(null) instanceof ICablePart part) {
                return new SprayCanTraversal.ConnectionSignature(part.getPartItem(), null, null);
            }
            return new SprayCanTraversal.ConnectionSignature(null, null, null);
        }

        static boolean matchesLine(CableBusBlockEntity seed, List<BlockPos> positions, Level level) {
            CableBusBlockEntity previous = null;
            for (BlockPos pos : positions) {
                if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof CableBusBlockEntity cable) ||
                        !sameType(seed, cable))
                    return false;
                if (previous != null) {
                    Direction direction = directionBetween(previous.getBlockPos(), pos);
                    if (previous.getPart(direction) != null ||
                            previous.getCableConnectionType(direction) == AECableType.NONE ||
                            cable.getPart(direction.getOpposite()) != null ||
                            cable.getCableConnectionType(direction.getOpposite()) == AECableType.NONE)
                        return false;
                }
                previous = cable;
            }
            return true;
        }

        static boolean sameType(CableBusBlockEntity first, CableBusBlockEntity second) {
            return signature(first).equals(signature(second)) && signature(first).family() != null;
        }
    }

    private static class FTBChunksProtection {

        static boolean canEdit(ServerPlayer player, InteractionHand hand, BlockPos pos) {
            var api = FTBChunksAPI.api();
            return !api.isManagerLoaded() || !api.getManager()
                    .shouldPreventInteraction(player, hand, pos, Protection.EDIT_BLOCK, null);
        }
    }
}
