package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.machine.trait.multiblock.MultiblockFluidRendererTrait;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class IPBFMachine extends MuiWorkableMultiblockMachine {

    public static final int MAX_PARALLELS = 8;

    private TickableSubscription hurtSubscription;
    // Renders the lava/fluid blocks inside the structure (migrated from the removed IFluidRenderMulti).
    private final MultiblockFluidRendererTrait fluidRendererTrait;

    public IPBFMachine(BlockEntityCreationInfo info) {
        super(info);
        this.fluidRendererTrait = attachTrait(new MultiblockFluidRendererTrait(this::saveOffsets));
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unsubscribe(hurtSubscription);
        hurtSubscription = null;
    }

    @Override
    public void recipeLogicStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        super.recipeLogicStatusChanged(oldStatus, newStatus);
        if (newStatus == RecipeLogic.Status.WORKING) {
            this.hurtSubscription = subscribeServerTick(this.hurtSubscription, this::hurtEntitiesAndBreakSnow);
        } else if (oldStatus == RecipeLogic.Status.WORKING && hurtSubscription != null) {
            unsubscribe(hurtSubscription);
            hurtSubscription = null;
        }
    }

    // Smoke VFX
    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        super.clientTick();
        if (recipeLogic.isWorking()) {
            var pos = this.getBlockPos();
            var facing = this.getFrontFacing().getOpposite();
            float xPos = facing.getStepX() * 0.76F + pos.getX() + 0.5F;
            float yPos = facing.getStepY() * 0.76F + pos.getY() + 0.25F;
            float zPos = facing.getStepZ() * 0.76F + pos.getZ() + 0.5F;

            float ySpd = facing.getStepY() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
            getLevel().addParticle(ParticleTypes.LARGE_SMOKE, xPos, yPos, zPos, 0, ySpd, 0);
        }
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV) return ModifierFunction.NULL;
        long euTick = RecipeHelper.getRecipeEUtTier(recipe);
        int parallel = ParallelLogic.getParallelAmount(machine, recipe, 8);
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallel))
                .outputModifier(ContentModifier.multiplier(parallel))
                .durationMultiplier(parallel * 0.75)
                .parallels(parallel)
                .build();
    }

    @Override
    public void animateTick(RandomSource random) {
        if (this.isActive()) {
            final BlockPos pos = getBlockPos();
            float x = pos.getX() + 0.5F;
            float z = pos.getZ() + 0.5F;

            final var facing = getFrontFacing();
            final float horizontalOffset = GTValues.RNG.nextFloat() * 0.6F - 0.3F;
            final float y = pos.getY() + GTValues.RNG.nextFloat() * 0.375F + 0.3F;

            if (facing.getAxis() == Direction.Axis.X) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) x += 0.52F;
                else x -= 0.52F;
                z += horizontalOffset;
            } else if (facing.getAxis() == Direction.Axis.Z) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) z += 0.52F;
                else z -= 0.52F;
                x += horizontalOffset;
            }
            if (ConfigHolder.INSTANCE.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                getLevel().playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F,
                        false);
            }
            getLevel().addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
            getLevel().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }

    private void hurtEntitiesAndBreakSnow() {
        BlockPos middlePos = self().getBlockPos().offset(getFrontFacing().getOpposite().getNormal());
        getLevel().getEntities(null, new AABB(middlePos)).forEach(e -> e.hurt(e.damageSources().lava(), 3.0f));

        if (getOffsetTimer() % 10 == 0) {
            BlockState state = getLevel().getBlockState(middlePos);
            GTUtil.tryBreakSnow(getLevel(), middlePos, state, true);
        }
    }

    @NotNull
    public Set<BlockPos> saveOffsets() {
        return Collections.singleton(new BlockPos(getFrontFacing().getOpposite().getNormal()));
    }
}
