package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.api.block.IBlockPattern;

import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@Mixin(value = BlockPattern.class, remap = false)
public abstract class BlockPatternMixin implements IBlockPattern {

    @Shadow
    @Final
    protected int[] centerOffset;

    @Shadow
    @Final
    protected int fingerLength;

    @Shadow
    @Final
    public int[][] aisleRepetitions;

    @Shadow
    @Final
    protected int thumbLength;

    @Shadow
    @Final
    protected int palmLength;

    @Shadow
    @Final
    protected TraceabilityPredicate[][][] blockMatches;

    @Shadow
    protected abstract BlockPos setActualRelativeOffset(int x, int y, int z, Direction facing, Direction upwardsFacing,
                                                        boolean isFlipped);

    @Shadow
    protected abstract void resetFacing(BlockPos pos, BlockState blockState, Direction facing,
                                        BiPredicate<BlockPos, Direction> checker, Consumer<BlockState> consumer);

    @Override
    public void cosmiccore$autoBuild(Player player, MultiblockState worldState, IGrid grid) {
        Level world = player.level();
        int minZ = -centerOffset[4];
        worldState.clean();
        MultiblockControllerMachine controller = worldState.getController();
        BlockPos centerPos = controller.getBlockPos();
        Direction facing = controller.getFrontFacing();
        Direction upwardsFacing = controller.getUpwardsFacing();
        boolean isFlipped = controller.isFlipped();
        Map<SimplePredicate, Integer> cacheGlobal = worldState.getGlobalCount();
        Map<SimplePredicate, Integer> cacheLayer = worldState.getLayerCount();
        Map<BlockPos, Object> blocks = new HashMap<>();
        Set<BlockPos> placeBlockPos = new HashSet<>();
        blocks.put(centerPos, controller);
        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            for (r = 0; r < aisleRepetitions[c][0]; r++) {
                cacheLayer.clear();
                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
                        BlockPos pos = setActualRelativeOffset(x, y, z, facing, upwardsFacing, isFlipped)
                                .offset(centerPos.getX(), centerPos.getY(), centerPos.getZ());
                        worldState.update(pos, predicate);
                        if (!world.isEmptyBlock(pos)) {
                            blocks.put(pos, world.getBlockState(pos));
                            for (SimplePredicate limit : predicate.limited) {
                                limit.testLimited(worldState);
                            }
                        } else {
                            boolean find = false;
                            BlockInfo[] infos = new BlockInfo[0];
                            for (SimplePredicate limit : predicate.limited) {
                                if (limit.minLayerCount > 0) {
                                    if (!cacheLayer.containsKey(limit)) {
                                        cacheLayer.put(limit, 1);
                                    } else
                                        if (cacheLayer.get(limit) < limit.minLayerCount && (limit.maxLayerCount == -1 ||
                                                cacheLayer.get(limit) < limit.maxLayerCount)) {
                                                    cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                                } else {
                                                    continue;
                                                }
                                } else {
                                    continue;
                                }
                                infos = limit.candidates == null ? null : limit.candidates.get();
                                find = true;
                                break;
                            }
                            if (!find) {
                                for (SimplePredicate limit : predicate.limited) {
                                    if (limit.minCount > 0) {
                                        if (!cacheGlobal.containsKey(limit)) {
                                            cacheGlobal.put(limit, 1);
                                        } else if (cacheGlobal.get(limit) < limit.minCount &&
                                                (limit.maxCount == -1 || cacheGlobal.get(limit) < limit.maxCount)) {
                                                    cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                                } else {
                                                    continue;
                                                }
                                    } else {
                                        continue;
                                    }
                                    infos = limit.candidates == null ? null : limit.candidates.get();
                                    find = true;
                                    break;
                                }
                            }
                            if (!find) {
                                // no limited
                                for (SimplePredicate limit : predicate.limited) {
                                    if (limit.maxLayerCount != -1 &&
                                            cacheLayer.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxLayerCount)
                                        continue;
                                    if (limit.maxCount != -1 &&
                                            cacheGlobal.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxCount)
                                        continue;
                                    if (cacheLayer.containsKey(limit)) {
                                        cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                    } else {
                                        cacheLayer.put(limit, 1);
                                    }
                                    if (cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        cacheGlobal.put(limit, 1);
                                    }
                                    infos = ArrayUtils.addAll(infos,
                                            limit.candidates == null ? null : limit.candidates.get());
                                }
                                for (SimplePredicate common : predicate.common) {
                                    infos = ArrayUtils.addAll(infos,
                                            common.candidates == null ? null : common.candidates.get());
                                }
                            }
                            List<ItemStack> candidates = new ArrayList<>();
                            if (infos != null) {
                                for (BlockInfo info : infos) {
                                    if (info.getBlockState().getBlock() != Blocks.AIR) {
                                        candidates.add(info.getItemStackForm());
                                    }
                                }
                            }
                            // check inventory
                            ItemStack found = null;
                            AEItemKey key = null;
                            var meInv = grid.getStorageService().getInventory();
                            var counter = meInv.getAvailableStacks();
                            if (!player.isCreative()) {
                                key = cosmiccore$getMatchStackWithHandler(candidates, counter);
                                if (key == null) continue;
                                found = key.toStack();
                            } else {
                                for (ItemStack candidate : candidates) {
                                    found = candidate.copy();
                                    if (!found.isEmpty() && found.getItem() instanceof BlockItem) {
                                        break;
                                    }
                                    found = null;
                                }
                            }
                            if (found == null) continue;
                            BlockItem itemBlock = (BlockItem) found.getItem();
                            BlockPlaceContext context = new BlockPlaceContext(world, player, InteractionHand.MAIN_HAND,
                                    found, BlockHitResult.miss(player.getEyePosition(0), Direction.UP, pos));
                            InteractionResult interactionResult = itemBlock.place(context);
                            if (interactionResult != InteractionResult.FAIL) {
                                placeBlockPos.add(pos);
                                if (key != null) {
                                    grid.getStorageService().getInventory().extract(key, 1, Actionable.MODULATE,
                                            IActionSource.ofPlayer(player));
                                }
                            }
                            if (world.getBlockEntity(pos) instanceof MetaMachine machineBlockEntity) {
                                blocks.put(pos, machineBlockEntity);
                            } else {
                                blocks.put(pos, world.getBlockState(pos));
                            }
                        }
                    }
                }
                z++;
            }
        }
        Direction frontFacing = controller.self().getFrontFacing();
        blocks.forEach((pos, block) -> {
            // adjust facing
            if (!(block instanceof MultiblockControllerMachine)) {
                if (block instanceof BlockState && placeBlockPos.contains(pos)) {
                    resetFacing(pos, (BlockState) block, frontFacing, (p, f) -> {
                        Object object = blocks.get(p.relative(f));
                        return object == null ||
                                (object instanceof BlockState && ((BlockState) object).getBlock() == Blocks.AIR);
                    }, state -> world.setBlock(pos, state, 3));
                } else if (block instanceof MetaMachine machine) {
                    resetFacing(pos, machine.getBlockState(), frontFacing, (p, f) -> {
                        Object object = blocks.get(p.relative(f));
                        if (object == null || (object instanceof BlockState blockState && blockState.isAir())) {
                            return machine.isFacingValid(f);
                        }
                        return false;
                    }, state -> world.setBlock(pos, state, 3));
                }
            }
        });
    }

    @Unique
    @Nullable
    private static AEItemKey cosmiccore$getMatchStackWithHandler(List<ItemStack> candidates, KeyCounter counter) {
        for (var entry : counter) {
            var key = entry.getKey();
            if (candidates
                    .stream()
                    .anyMatch(stack -> AEItemKey.matches(key, stack) && entry.getLongValue() > 0 &&
                            stack.getItem() instanceof BlockItem blockItem && !(blockItem instanceof MetaMachineItem)))
                return (AEItemKey) key;
        }
        return null;
    }
}
