package com.ghostipedia.cosmiccore.mixin.gravity;

import com.ghostipedia.cosmiccore.common.gravity.DirectedGravityCollision;
import com.ghostipedia.cosmiccore.common.gravity.DirectedGravityKernel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityDirectedGravityMixin {

    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    private float eyeHeight;

    @Shadow
    private Vec3 position;

    @Shadow
    public double xo;

    @Shadow
    public double yo;

    @Shadow
    public double zo;

    @Shadow
    public boolean horizontalCollision;

    @Shadow
    public boolean verticalCollision;

    @Shadow
    public boolean verticalCollisionBelow;

    @Shadow
    public boolean minorHorizontalCollision;

    @Shadow
    public Optional<BlockPos> mainSupportingBlockPos;

    @Shadow
    private boolean onGroundNoBlocks;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract Level level();

    @Unique
    private Vec3 cosmiccore$requestedMovement = Vec3.ZERO;

    @Unique
    private Vec3 cosmiccore$actualMovement = Vec3.ZERO;

    @Unique
    private boolean cosmiccore$directedMove;

    @Unique
    private Player cosmiccore$activePlayer() {
        Entity entity = (Entity) (Object) this;
        return entity instanceof Player player && DirectedGravityKernel.isActive(player) ? player : null;
    }

    @Inject(method = "makeBoundingBox()Lnet/minecraft/world/phys/AABB;", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$makeDirectionalBoundingBox(CallbackInfoReturnable<AABB> cir) {
        Player player = cosmiccore$activePlayer();
        if (player != null) cir.setReturnValue(DirectedGravityKernel.makeBoundingBox(player, dimensions, position));
    }

    @Inject(method = "calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void cosmiccore$rotateViewVector(float xRot, float yRot, CallbackInfoReturnable<Vec3> cir) {
        Player player = cosmiccore$activePlayer();
        if (player != null) cir.setReturnValue(DirectedGravityKernel.viewVector(player, cir.getReturnValue()));
    }

    @Inject(method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$getDirectionalEyePosition(CallbackInfoReturnable<Vec3> cir) {
        Player player = cosmiccore$activePlayer();
        if (player != null) cir.setReturnValue(position.add(DirectedGravityKernel.eyeOffset(player, eyeHeight)));
    }

    @Inject(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$getDirectionalEyePosition(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Player player = cosmiccore$activePlayer();
        if (player == null) return;
        Vec3 base = new Vec3(
                Mth.lerp(partialTick, xo, getX()),
                Mth.lerp(partialTick, yo, getY()),
                Mth.lerp(partialTick, zo, getZ()));
        cir.setReturnValue(base.add(DirectedGravityKernel.eyeOffset(player, eyeHeight)));
    }

    @Inject(method = "getEyeY()D", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$getDirectionalEyeY(CallbackInfoReturnable<Double> cir) {
        Player player = cosmiccore$activePlayer();
        if (player != null) cir.setReturnValue(position.y + DirectedGravityKernel.eyeOffset(player, eyeHeight).y);
    }

    @Inject(method = "isInWall()Z", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$checkDirectionalEyeInWall(CallbackInfoReturnable<Boolean> cir) {
        Player player = cosmiccore$activePlayer();
        if (player == null) return;
        if (player.noPhysics) {
            cir.setReturnValue(false);
            return;
        }
        double checkWidth = dimensions.width() * 0.8;
        AABB eyeBox = DirectedGravityCollision.makeDirectionalEyeBox(
                player.getEyePosition(), checkWidth, DirectedGravityKernel.down(player));
        boolean inWall = BlockPos.betweenClosedStream(eyeBox).anyMatch(pos -> {
            BlockState state = level().getBlockState(pos);
            return !state.isAir() && state.isSuffocating(level(), pos) && Shapes.joinIsNotEmpty(
                    state.getCollisionShape(level(), pos).move(pos.getX(), pos.getY(), pos.getZ()),
                    Shapes.create(eyeBox),
                    BooleanOp.AND);
        });
        cir.setReturnValue(inWall);
    }

    @Inject(
            method = "getBlockPosBelowThatAffectsMyMovement()Lnet/minecraft/core/BlockPos;",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$getDirectionalMovementAffectingPos(CallbackInfoReturnable<BlockPos> cir) {
        Player player = cosmiccore$activePlayer();
        if (player != null) cir.setReturnValue(DirectedGravityCollision.getOnPos(player, 0.5000001F));
    }

    @Inject(method = "applyGravity()V", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$applyDirectionalGravity(CallbackInfo ci) {
        Player player = cosmiccore$activePlayer();
        if (player == null) return;
        player.setDeltaMovement(
                DirectedGravityKernel.applyGravity(player, player.getDeltaMovement(), player.getGravity()));
        ci.cancel();
    }

    @ModifyExpressionValue(
                           method = "moveRelative(FLnet/minecraft/world/phys/Vec3;)V",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 cosmiccore$rotateRelativeMovement(Vec3 movement) {
        Player player = cosmiccore$activePlayer();
        return player == null ? movement : DirectedGravityKernel.relativeMovement(player, movement);
    }

    @Inject(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$collideInLocalSpace(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        Player player = cosmiccore$activePlayer();
        if (player != null) cir.setReturnValue(DirectedGravityCollision.collide(player, movement));
    }

    @WrapOperation(
                   method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 cosmiccore$captureCollision(Entity entity, Vec3 movement, Operation<Vec3> original) {
        Vec3 actual = original.call(entity, movement);
        Player player = cosmiccore$activePlayer();
        cosmiccore$directedMove = player != null;
        if (player != null) {
            cosmiccore$requestedMovement = movement;
            cosmiccore$actualMovement = actual;
        }
        return actual;
    }

    @WrapOperation(
                   method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/Entity;setOnGroundWithMovement(ZLnet/minecraft/world/phys/Vec3;)V"))
    private void cosmiccore$setLocalGroundState(Entity entity, boolean onGround, Vec3 movement,
                                                Operation<Void> original) {
        Player player = cosmiccore$activePlayer();
        if (player == null) {
            original.call(entity, onGround, movement);
            return;
        }
        DirectedGravityKernel.MovementState state = DirectedGravityKernel.movementState(
                player, cosmiccore$requestedMovement, movement);
        horizontalCollision = state.horizontalCollision();
        verticalCollision = state.verticalCollision();
        verticalCollisionBelow = state.onGround();
        minorHorizontalCollision = false;
        original.call(entity, state.onGround(), movement);
    }

    @Inject(method = "checkSupportingBlock(ZLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$checkDirectionalSupportingBlock(boolean onGround, Vec3 movement, CallbackInfo ci) {
        Player player = cosmiccore$activePlayer();
        if (player == null) return;
        ci.cancel();
        if (!onGround) {
            onGroundNoBlocks = false;
            mainSupportingBlockPos = Optional.empty();
            return;
        }
        Direction down = DirectedGravityKernel.down(player);
        AABB supportArea = DirectedGravityCollision.supportArea(getBoundingBox(), down);
        Optional<BlockPos> support = level().findSupportingBlock(player, supportArea);
        if (support.isPresent() || onGroundNoBlocks) {
            mainSupportingBlockPos = support;
        } else if (movement != null) {
            support = level().findSupportingBlock(
                    player, DirectedGravityCollision.previousSupportArea(getBoundingBox(), movement, down));
            mainSupportingBlockPos = support;
        }
        onGroundNoBlocks = support.isEmpty();
    }

    @Inject(method = "getOnPos(F)Lnet/minecraft/core/BlockPos;", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$getDirectionalOnPos(float offset, CallbackInfoReturnable<BlockPos> cir) {
        Player player = cosmiccore$activePlayer();
        if (player == null) return;
        cir.setReturnValue(mainSupportingBlockPos.orElseGet(() -> DirectedGravityCollision.getOnPos(player, offset)));
    }

    @WrapOperation(
                   method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"))
    private void cosmiccore$checkDirectionalFall(Entity entity, double y, boolean onGround, BlockState state,
                                                 BlockPos pos, Operation<Void> original) {
        Player player = cosmiccore$activePlayer();
        if (player == null) {
            original.call(entity, y, onGround, state, pos);
            return;
        }
        DirectedGravityKernel.MovementState movementState = DirectedGravityKernel.movementState(
                player, cosmiccore$requestedMovement, cosmiccore$actualMovement);
        original.call(entity, DirectedGravityCollision.localVertical(player, cosmiccore$actualMovement), onGround,
                state, pos);
        if (!entity.isRemoved() && movementState.verticalCollision() &&
                DirectedGravityKernel.down(player) != Direction.DOWN) {
            cosmiccore$applyDirectionalFallResponse(player, state);
        }
    }

    @WrapOperation(
                   method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(DDD)V"))
    private void cosmiccore$preserveLocalCollisionVelocity(Entity entity, double x, double y, double z,
                                                           Operation<Void> original) {
        Player player = cosmiccore$activePlayer();
        if (player == null) {
            original.call(entity, x, y, z);
            return;
        }
        Vec3 velocity = DirectedGravityKernel.removeClippedVelocity(
                player, cosmiccore$requestedMovement, cosmiccore$actualMovement, player.getDeltaMovement());
        original.call(entity, velocity.x, velocity.y, velocity.z);
    }

    @WrapOperation(
                   method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/block/Block;updateEntityAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V"))
    private void cosmiccore$avoidWorldVerticalFallResponse(Block block, BlockGetter level, Entity entity,
                                                           Operation<Void> original) {
        if (!(entity instanceof Player player) || !cosmiccore$directedMove ||
                DirectedGravityKernel.down(player) == Direction.DOWN) {
            original.call(block, level, entity);
        }
    }

    @WrapOperation(
                   method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 cosmiccore$applySurfaceFriction(Vec3 velocity, double xScale, double yScale, double zScale,
                                                 Operation<Vec3> original) {
        Player player = cosmiccore$activePlayer();
        return player == null ? original.call(velocity, xScale, yScale, zScale) :
                DirectedGravityKernel.scaleSurfaceVelocity(player, velocity, xScale);
    }

    @Unique
    private void cosmiccore$applyDirectionalFallResponse(Player player, BlockState state) {
        player.setDeltaMovement(DirectedGravityKernel.worldToLocal(player, player.getDeltaMovement()));
        try {
            state.getBlock().updateEntityAfterFallOn(level(), player);
        } finally {
            player.setDeltaMovement(DirectedGravityKernel.localToWorld(player, player.getDeltaMovement()));
        }
    }
}
