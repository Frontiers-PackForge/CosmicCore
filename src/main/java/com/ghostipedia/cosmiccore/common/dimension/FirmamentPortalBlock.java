package com.ghostipedia.cosmiccore.common.dimension;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public final class FirmamentPortalBlock extends Block implements Portal {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape X_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    private static final VoxelShape Z_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public FirmamentPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return FirmamentPortalShape.find(level, pos) == null ?
                net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() :
                super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        if (!(entity instanceof Player player)) return 0;
        return Math.max(1,
                level.getGameRules()
                        .getInt(player.getAbilities().invulnerable ?
                                GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY :
                                GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
    }

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ServerLevel destination = level.getServer().getLevel(
                level.dimension().equals(FirmamentDimension.KEY) ? Level.OVERWORLD : FirmamentDimension.KEY);
        if (destination == null) return null;

        Direction.Axis axis = level.getBlockState(pos).getOptionalValue(AXIS).orElse(Direction.Axis.X);
        BlockPos target = destination.getWorldBorder().clampToBounds(entity.getX(), entity.getY(), entity.getZ());
        BlockPos surface = findSurface(destination, target.getX(), target.getZ());
        int baseY = Mth.clamp(surface.getY(), destination.getMinBuildHeight() + 2,
                destination.getMaxBuildHeight() - 6);
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockPos base = new BlockPos(surface.getX(), baseY, surface.getZ()).relative(horizontal, -1);
        BlockPos entrance = FirmamentPortalShape.build(destination, base, axis);
        Vec3 arrival = Vec3.atBottomCenterOf(entrance);
        return new DimensionTransition(destination, arrival, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
    }

    @Override
    public Transition getLocalTransition() {
        return Transition.CONFUSION;
    }

    private static BlockPos findSurface(ServerLevel level, int x, int z) {
        if (level.dimension().equals(FirmamentDimension.KEY)) {
            BlockPos lowerSurface = findFirmamentLowerSurface(level, x, z);
            if (lowerSurface != null) return lowerSurface;
            return new BlockPos(x, 64, z);
        }
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (surface > level.getMinBuildHeight() + 2) return new BlockPos(x, surface, z);
        for (int radius = 8; radius <= 64; radius += 8) {
            for (int offsetX = -radius; offsetX <= radius; offsetX += 8) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ += 8) {
                    int candidate = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            x + offsetX, z + offsetZ);
                    if (candidate > level.getMinBuildHeight() + 2) {
                        return new BlockPos(x + offsetX, candidate, z + offsetZ);
                    }
                }
            }
        }
        return new BlockPos(x, level.getSharedSpawnPos().getY(), z);
    }

    @Nullable
    private static BlockPos findFirmamentLowerSurface(ServerLevel level, int x, int z) {
        for (int radius = 0; radius <= 64; radius += 8) {
            for (int offsetX = -radius; offsetX <= radius; offsetX += 8) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ += 8) {
                    if (radius > 0 && Math.abs(offsetX) != radius && Math.abs(offsetZ) != radius) continue;
                    int sampleX = x + offsetX;
                    int sampleZ = z + offsetZ;
                    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(sampleX, 112, sampleZ);
                    for (int y = 112; y >= 16; y--) {
                        cursor.setY(y);
                        if (!isFirmamentTerrain(level.getBlockState(cursor))) continue;
                        if (!level.getBlockState(cursor.above()).isAir() ||
                                !level.getBlockState(cursor.above(2)).isAir() ||
                                !level.getBlockState(cursor.above(3)).isAir())
                            continue;
                        return cursor.above().immutable();
                    }
                }
            }
        }
        return null;
    }

    private static boolean isFirmamentTerrain(BlockState state) {
        return state.is(CosmicBlocks.FIRMAMENT_SAPROLITE.get()) ||
                state.is(CosmicBlocks.FIRMAMENT_SAPROLITE_SLAB.get()) ||
                state.is(CosmicBlocks.ASTRAL_REGOLITH.get()) || state.is(CosmicBlocks.STARDUST_TURF.get());
    }
}
