package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.client.particle.GTOverheatParticle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = GTOverheatParticle.class, remap = false)
public abstract class GTOverheatParticleZFightFixMixin {

    @ModifyConstant(
                    method = "lambda$renderBloomEffect$0(Lcom/mojang/blaze3d/vertex/BufferBuilder;Lcom/mojang/blaze3d/vertex/PoseStack;FFFDDDDDD)V",
                    constant = @Constant(floatValue = 0.001F),
                    require = 6,
                    expect = 6,
                    allow = 6)
    private float cosmiccore$separateOverheatShell(float original) {
        return 0.003F;
    }
}
