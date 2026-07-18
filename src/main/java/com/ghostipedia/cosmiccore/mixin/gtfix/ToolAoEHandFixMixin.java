package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.CommonEventListener;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CommonEventListener.class, remap = false)
public abstract class ToolAoEHandFixMixin {

    @Redirect(
              method = "onBlockStartBreak",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraft/world/entity/player/Player;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"))
    private static InteractionHand cosmiccore$useMainHand(Player player) {
        return InteractionHand.MAIN_HAND;
    }
}
