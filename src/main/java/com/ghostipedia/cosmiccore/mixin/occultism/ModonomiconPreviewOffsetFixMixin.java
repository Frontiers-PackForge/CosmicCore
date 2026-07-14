package com.ghostipedia.cosmiccore.mixin.occultism;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;

import com.klikli_dev.modonomicon.api.multiblock.Multiblock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(targets = "com.klikli_dev.modonomicon.client.render.MultiblockPreviewRenderer", remap = false)
public class ModonomiconPreviewOffsetFixMixin {

    @Inject(method = "setMultiblock(Lcom/klikli_dev/modonomicon/api/multiblock/Multiblock;Lnet/minecraft/network/chat/Component;ZLjava/util/function/Function;)V",
            at = @At("HEAD"),
            remap = false)
    private static void cosmiccore$restoreOccultismViewOffset(Multiblock multiblock, Component name, boolean flip,
                                                              Function<BlockPos, BlockPos> offsetApplier,
                                                              CallbackInfo ci) {
        if (multiblock == null || !"occultism".equals(multiblock.getId().getNamespace())) {
            return;
        }

        Vec3i offset = multiblock.getOffset();
        Vec3i viewOffset = multiblock.getViewOffset();
        if (!offset.equals(viewOffset)) {
            multiblock.offsetView(offset.getX() - viewOffset.getX(), offset.getY() - viewOffset.getY(),
                    offset.getZ() - viewOffset.getZ());
        }
    }
}
