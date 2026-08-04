package com.ghostipedia.cosmiccore.mixin.gravity;

import com.ghostipedia.cosmiccore.common.gravity.DirectedGravityCollision;
import com.ghostipedia.cosmiccore.common.gravity.DirectedGravityKernel;

import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDirectedGravityMixin {

    @Inject(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$checkDirectionalPose(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!DirectedGravityKernel.isActive(player)) return;
        AABB box = DirectedGravityKernel.makeBoundingBox(player, player.getDimensions(pose), player.position())
                .deflate(1.0E-7);
        cir.setReturnValue(player.level().noCollision(player, box));
    }

    @Inject(
            method = "maybeBackOffFromEdge(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$backOffFromDirectionalEdge(Vec3 movement, MoverType mover,
                                                       CallbackInfoReturnable<Vec3> cir) {
        Player player = (Player) (Object) this;
        if (DirectedGravityKernel.isActive(player)) {
            cir.setReturnValue(DirectedGravityCollision.backOffFromEdge(player, movement, mover));
        }
    }
}
