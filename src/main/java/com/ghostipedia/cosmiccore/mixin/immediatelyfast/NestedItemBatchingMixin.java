package com.ghostipedia.cosmiccore.mixin.immediatelyfast;

import net.raphimc.immediatelyfast.feature.batching.HudBatchingBufferSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(value = HudBatchingBufferSource.class, remap = false)
public abstract class NestedItemBatchingMixin {

    @Unique
    private int cosmiccore$itemRenderDepth;

    @ModifyVariable(method = "setRenderingItem(Z)V", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 1)
    boolean cosmiccore$trackItemRenderDepth(boolean renderingItem) {
        cosmiccore$itemRenderDepth = renderingItem ? cosmiccore$itemRenderDepth + 1 :
                Math.max(0, cosmiccore$itemRenderDepth - 1);
        return cosmiccore$itemRenderDepth > 0;
    }
}
