package com.ghostipedia.cosmiccore.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerBreathingMixin {

    @Shadow
    protected boolean wasUnderwater;

    @Unique
    private boolean cosmicCore$didTurtleEffect = false;

    @ModifyExpressionValue(method = "updateIsUnderwater",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean cosmicCore$updateTurtleEffectStatus(boolean isInWater) {
        if (!isInWater && this.wasUnderwater) {
            cosmicCore$didTurtleEffect = false;
        }
        return isInWater;
    }

    @Redirect(method = "turtleHelmetTick",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean cosmicCore$turtleHelmetTick(Player instance, TagKey<Fluid> tagKey) {
        // the boolean is inverted after the redirected method call, so this resolves to
        // !wasUnderwater && !cosmicCore$didTurtleEffect
        return this.wasUnderwater || cosmicCore$didTurtleEffect;
    }
}
