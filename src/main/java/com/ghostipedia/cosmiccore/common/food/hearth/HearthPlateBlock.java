package com.ghostipedia.cosmiccore.common.food.hearth;

import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodData;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodRegistry;
import com.ghostipedia.cosmiccore.common.food.HearthLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class HearthPlateBlock extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 2, 15);

    public HearthPlateBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CosmicBlockEntities.HEARTH_PLATE_BE.get().create(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof HearthPlateBlockEntity plate)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!CosmicFoodRegistry.isConsumable(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (CosmicFoodRegistry.isVile(stack.getItem())) {
            return served(player, "cosmiccore.hearth.plate.vile");
        }

        CosmicFoodRegistry.PlateRole role = CosmicFoodRegistry.plateRole(stack);
        boolean isDrink = role == CosmicFoodRegistry.PlateRole.DRINK;
        boolean isSide = role == CosmicFoodRegistry.PlateRole.SIDE;

        if (isDrink) {
            if (!plate.drink.isEmpty()) return served(player, "cosmiccore.hearth.plate.drink_full");
            plate.drink = stack.copyWithCount(1);
        } else if (isSide) {
            if (!plate.side.isEmpty()) return served(player, "cosmiccore.hearth.plate.side_full");
            plate.side = stack.copyWithCount(1);
        } else {
            if (!plate.main.isEmpty()) return served(player, "cosmiccore.hearth.plate.main_full");
            plate.main = stack.copyWithCount(1);
        }
        if (!player.isCreative()) stack.shrink(1);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.7f, 1.1f);
        plate.sync();
        return ItemInteractionResult.CONSUME;
    }

    private static ItemInteractionResult served(Player player, String langKey) {
        player.displayClientMessage(Component.translatable(langKey), true);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof HearthPlateBlockEntity plate)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown()) {
            ItemStack back = ItemStack.EMPTY;
            if (!plate.drink.isEmpty()) {
                back = plate.drink;
                plate.drink = ItemStack.EMPTY;
            } else if (!plate.side.isEmpty()) {
                back = plate.side;
                plate.side = ItemStack.EMPTY;
            } else if (!plate.main.isEmpty()) {
                back = plate.main;
                plate.main = ItemStack.EMPTY;
            }
            if (back.isEmpty()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CosmicFoodData data = serverPlayer.getData(CosmicAttachmentTypes.FOOD_DATA);
                    if (HearthLogic.canInscribe(data)) {
                        boolean roomFor = data.signatures.size() < HearthLogic.MAX_SIGNATURES;
                        serverPlayer.sendSystemMessage(HearthLogic.inscribeCurrentMeal(serverPlayer));
                        if (roomFor) {
                            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                                    0.9f, 0.8f);
                        }
                    }
                }
                return InteractionResult.CONSUME;
            }
            if (!player.addItem(back)) player.drop(back, false);
            plate.sync();
            return InteractionResult.CONSUME;
        }

        if (plate.main.isEmpty()) {
            player.displayClientMessage(Component.translatable("cosmiccore.hearth.plate.no_main"), true);
            return InteractionResult.CONSUME;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.CONSUME;
        if (!HearthLogic.isAtHome(serverPlayer)) {
            player.displayClientMessage(Component.translatable("cosmiccore.hearth.plate.not_home"), true);
            return InteractionResult.CONSUME;
        }

        if (HearthLogic.applyHomeMeal(serverPlayer, plate.main, plate.side, plate.drink, 0, 0)) {
            plate.main = ItemStack.EMPTY;
            plate.side = ItemStack.EMPTY;
            plate.drink = ItemStack.EMPTY;
            level.playSound(null, pos, SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8f, 1.0f);
            plate.sync();
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock() &&
                level.getBlockEntity(pos) instanceof HearthPlateBlockEntity plate) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), plate.main);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), plate.side);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), plate.drink);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
