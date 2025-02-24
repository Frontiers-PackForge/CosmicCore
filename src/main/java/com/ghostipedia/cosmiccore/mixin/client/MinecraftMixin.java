package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.CosmicUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean cosmiccore$makeRingGlow(boolean original, Entity entity) {
        return original || (CosmicUtils.hasTheOneRing(player) && CosmicUtils.hasTheOneRing(entity));
    }
}
