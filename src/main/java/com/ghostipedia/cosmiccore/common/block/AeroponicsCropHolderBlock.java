package com.ghostipedia.cosmiccore.common.block;

import com.ghostipedia.cosmiccore.common.blockentity.AeroponicsCropHolderBlockEntity;
import com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities;
import com.ghostipedia.cosmiccore.common.data.tag.item.CosmicItemTags;
import com.ghostipedia.cosmiccore.utils.VoxelShapeUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class AeroponicsCropHolderBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final MapCodec<AeroponicsCropHolderBlock> CODEC = simpleCodec(AeroponicsCropHolderBlock::new);

    private static final VoxelShape LEFT_ARM_SHAPE = Shapes.or(
            box(3, 0.5, 11, 5, 4, 11.5),
            box(3, 1, 11.5, 5, 4.5, 12),
            box(3, 1.5, 12, 5, 5, 12.5),
            box(3, 2, 12.5, 5, 5.5, 13),
            box(3, 2.5, 13, 5, 6, 13.5),
            box(3, 3, 13.5, 5, 6.5, 14));
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            box(0, 0, 14, 16, 16, 16),
            box(1, 0, 5, 7, 4, 11),
            box(9, 0, 5, 15, 4, 11),
            box(1, 7, 8, 7, 11, 14),
            box(9, 7, 8, 15, 11, 14),
            LEFT_ARM_SHAPE,
            LEFT_ARM_SHAPE.move(0.5, 0, 0));
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, NORTH_SHAPE,
            Direction.EAST, VoxelShapeUtils.rotateHorizontal(NORTH_SHAPE, Direction.EAST),
            Direction.SOUTH, VoxelShapeUtils.rotateHorizontal(NORTH_SHAPE, Direction.SOUTH),
            Direction.WEST, VoxelShapeUtils.rotateHorizontal(NORTH_SHAPE, Direction.WEST));

    public AeroponicsCropHolderBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CosmicBlockEntities.AEROPONICS_CROP_HOLDER.get().create(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        var planter = Planter.atHit(state.getValue(FACING),
                hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)));
        if (planter == null || !player.mayBuild() || !SeedlingPlants.canPlant(stack) ||
                !(level.getBlockEntity(pos) instanceof AeroponicsCropHolderBlockEntity holder)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (holder.plant(planter, stack)) {
            if (!player.isCreative()) stack.shrink(1);
            level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.8F, 1.0F);
        } else {
            player.displayClientMessage(Component.translatable("cosmiccore.aeroponics.planter_occupied"), true);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        var planter = Planter.atHit(state.getValue(FACING),
                hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)));
        if (planter == null || !player.mayBuild() || !player.getMainHandItem().isEmpty() ||
                !(level.getBlockEntity(pos) instanceof AeroponicsCropHolderBlockEntity holder) ||
                holder.getSeedling(planter).isEmpty())
            return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack seed = holder.removeSeedling(planter);
        if (!player.addItem(seed)) player.drop(seed, false);
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide &&
                level.getBlockEntity(pos) instanceof AeroponicsCropHolderBlockEntity holder) {
            holder.dropSeedlings();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("cosmiccore.aeroponics.plant_hint").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("cosmiccore.aeroponics.remove_hint").withStyle(ChatFormatting.GRAY));
    }

    public static final class SeedlingPlants {

        private SeedlingPlants() {}

        public static boolean canPlant(ItemStack stack) {
            return !stack.isEmpty() && (stack.is(CosmicItemTags.AEROPONICS_SEEDLINGS) || defaultVisual(stack) != null);
        }

        public static @Nullable BlockState defaultVisual(ItemStack stack) {
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem item)) return null;
            var block = item.getBlock();
            if (!(block instanceof BushBlock || block instanceof GrowingPlantBlock || block instanceof SugarCaneBlock ||
                    block instanceof CactusBlock || block instanceof CocoaBlock ||
                    stack.is(CosmicItemTags.AEROPONICS_SEEDLINGS)))
                return null;
            BlockState state = block.defaultBlockState();
            for (var property : state.getProperties()) {
                if (property instanceof IntegerProperty age && age.getName().equals("age")) {
                    state = state.setValue(age, age.getPossibleValues().stream().max(Integer::compareTo).orElseThrow());
                }
            }
            return state;
        }
    }

    public enum Planter {

        LOWER_LEFT(1, 0, 5, 4, 2, 8),
        LOWER_RIGHT(9, 0, 5, 12, 2, 8),
        UPPER_LEFT(1, 7, 8, 4, 9, 11),
        UPPER_RIGHT(9, 7, 8, 12, 9, 11);

        public static final List<Planter> ALL = List.of(values());

        private final AABB bounds;
        private final Vec3 plantOrigin;

        Planter(int x, int y, int z, int plantX, int plantY, int plantZ) {
            bounds = new AABB(x / 16.0, y / 16.0, z / 16.0,
                    (x + 6) / 16.0, (y + 4) / 16.0, (z + 6) / 16.0).inflate(0.00001);
            plantOrigin = new Vec3(plantX / 16.0, plantY / 16.0, plantZ / 16.0);
        }

        public Vec3 plantOrigin() {
            return plantOrigin;
        }

        public static @Nullable Planter atHit(Direction facing, Vec3 hitWithinBlock) {
            double x = hitWithinBlock.x;
            double z = hitWithinBlock.z;
            Vec3 local = switch (facing) {
                case NORTH -> hitWithinBlock;
                case EAST -> new Vec3(z, hitWithinBlock.y, 1 - x);
                case SOUTH -> new Vec3(1 - x, hitWithinBlock.y, 1 - z);
                case WEST -> new Vec3(1 - z, hitWithinBlock.y, x);
                default -> throw new IllegalArgumentException("Expected a horizontal facing: " + facing);
            };
            for (var planter : ALL) {
                if (planter.bounds.contains(local)) return planter;
            }
            return null;
        }
    }
}
