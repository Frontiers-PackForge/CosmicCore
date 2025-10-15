package com.ghostipedia.cosmiccore.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = Blocks.class, remap = false)
public class BlocksMixin {
    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;strength(F)Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;",
                    ordinal = 127
            ),
            index = 0
    )
    private static float modifyNetherrackHardness(float originalHardness) {
        return .8f;
    }
}
