package com.ghostipedia.cosmiccore.mixin;

import net.minecraft.world.level.block.Blocks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

/**
 * @author SpicierSpace153 (kathryne)
 * @reason change hardness so tinkers can aoe
 */

@Unique
@Mixin(value = Blocks.class, remap = true)
public class BlocksMixin {

    @ModifyArg(
               method = "<clinit>",
               at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;strength(F)Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;",
                        ordinal = 127),
               index = 0)
    private static float cosmiccore$modifyNetherrackHardness(float originalHardness) {
        return .8f;
    }
}
