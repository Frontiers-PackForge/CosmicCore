package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.client.model.quad.Mesh;
import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;

import net.minecraft.client.renderer.block.model.BakedQuad;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Consumer;

@Mixin(value = FacadeCoverRenderer.class, remap = false)
public class FacadeCoverRendererAoFixMixin {

    @Redirect(method = "renderCover",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/client/model/quad/MutableQuadView;emit()Lcom/gregtechceu/gtceu/client/model/quad/MutableQuadView;"),
              require = 1)
    private MutableQuadView cosmiccore$bakeWithAo(MutableQuadView emitter,
                                                  @Local(name = "quads") List<BakedQuad> quads) {
        quads.add(emitter.toBlockBakedQuad());
        return emitter.emit();
    }

    @Redirect(method = "renderCover",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/client/model/quad/Mesh;asBlockBakedQuads(Ljava/util/function/Consumer;)V"),
              require = 1)
    private void cosmiccore$skipLossyMesh(Mesh mesh, Consumer<BakedQuad> consumer) {}
}
