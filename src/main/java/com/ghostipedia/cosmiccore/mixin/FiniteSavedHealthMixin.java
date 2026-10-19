package com.ghostipedia.cosmiccore.mixin;

import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class FiniteSavedHealthMixin {

    @Shadow
    public abstract float getMaxHealth();

    @ModifyArg(method = "readAdditionalSaveData",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"),
               index = 0)
    private float cosmiccore$recoverSavedHealth(float health) {
        if (Float.isFinite(health)) return health;
        float maximum = getMaxHealth();
        return Float.isFinite(maximum) && maximum > 0 ? maximum : 1.0f;
    }
}
