package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.machine.impl.QuantumChestItemRender;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuantumChestItemRender.class, remap = false)
public class QuantumChestUpwardFacingCrashFixMixin {

    @Inject(method = "setupModelRotation(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private static void cosmiccore$safeUpwardFacingRotation(MetaMachine machine, PoseStack poseStack,
                                                            CallbackInfo ci) {
        Direction frontFacing = machine.getFrontFacing();
        Direction upwardFacing = machine.getUpwardsFacing();

        poseStack.translate(0.5F, 0.5F, 0.5F);
        float roll = frontFacing.getAxis().isHorizontal() ?
                cosmiccore$frontAxisRollAngle(frontFacing, upwardFacing, Direction.UP) :
                GTMatrixUtils.upwardFacingAngle(upwardFacing) +
                        (upwardFacing.getAxis() == Direction.Axis.X ? Mth.PI : 0);
        GTMatrixUtils.rotateMatrix(poseStack.last().pose(), roll, GTMatrixUtils.getDirectionAxis(frontFacing));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        ci.cancel();
    }

    private static float cosmiccore$frontAxisRollAngle(Direction frontFacing, Direction upwardsFacing,
                                                       Direction referenceUp) {
        Vector3fc frontAxis = GTMatrixUtils.getDirectionAxis(frontFacing);
        Vector3f actualUp = upwardsFacing.step();
        Vector3f reference = referenceUp.step();
        float sin = reference.cross(actualUp, new Vector3f()).dot(frontAxis);
        float cos = reference.dot(actualUp);
        return (float) Math.atan2(sin, cos);
    }
}
