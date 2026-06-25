package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.client.model.ctm.CTMMeshBuilder;
import com.gregtechceu.gtceu.client.model.quad.Mesh;
import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Consumer;

@Mixin(value = CTMMeshBuilder.class, remap = false)
public class CtmMeshBuilderAoFixMixin {

    @Redirect(method = "buildCTMQuads(Lcom/gregtechceu/gtceu/client/model/ctm/TextureConnections;Ljava/util/List;Lnet/minecraft/core/Direction;)Ljava/util/List;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/client/model/quad/MutableQuadView;spriteUnbake(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;I)Lcom/gregtechceu/gtceu/client/model/quad/MutableQuadView;"),
              require = 1)
    private static MutableQuadView cosmiccore$processQuad(MutableQuadView quad, TextureAtlasSprite sprite,
                                                          int bakeFlags) {
        MutableQuadView result = quad.spriteUnbake(sprite, bakeFlags);
        cosmiccore$canonicalizeWinding(quad);
        return result;
    }

    private static void cosmiccore$canonicalizeWinding(MutableQuadView quad) {
        Direction face = quad.nominalFace();
        if (face == null) return;

        int target = cosmiccore$anchorIndex(quad, face);
        if (target <= 0) return;

        Vector3f[] pos = new Vector3f[4];
        int[] color = new int[4];
        int[] light = new int[4];
        for (int i = 0; i < 4; i++) {
            pos[i] = quad.copyPos(i, new Vector3f());
            color[i] = quad.color(i);
            light[i] = quad.lightmap(i);
        }
        for (int i = 0; i < 4; i++) {
            int s = (i + target) & 3;
            quad.pos(i, pos[s].x, pos[s].y, pos[s].z);
            quad.color(i, color[s]);
            quad.lightmap(i, light[s]);
        }
    }

    private static int cosmiccore$anchorIndex(MutableQuadView quad, Direction face) {
        for (int i = 0; i < 4; i++) {
            float x = quad.x(i), y = quad.y(i), z = quad.z(i);
            boolean hit = switch (face) {
                case UP -> cosmiccore$near(x, 0.0F) && cosmiccore$near(z, 0.0F);
                case DOWN -> cosmiccore$near(x, 0.0F) && cosmiccore$near(z, 1.0F);
                case NORTH -> cosmiccore$near(x, 1.0F) && cosmiccore$near(y, 1.0F);
                case SOUTH -> cosmiccore$near(x, 0.0F) && cosmiccore$near(y, 1.0F);
                case EAST -> cosmiccore$near(y, 1.0F) && cosmiccore$near(z, 1.0F);
                case WEST -> cosmiccore$near(y, 1.0F) && cosmiccore$near(z, 0.0F);
            };
            if (hit) return i;
        }
        return -1;
    }

    private static boolean cosmiccore$near(float a, float b) {
        return Math.abs(a - b) < 0.01F;
    }

    @Redirect(method = "buildCTMQuads(Lcom/gregtechceu/gtceu/client/model/ctm/TextureConnections;Ljava/util/List;Lnet/minecraft/core/Direction;)Ljava/util/List;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/client/model/quad/MutableQuadView;emit()Lcom/gregtechceu/gtceu/client/model/quad/MutableQuadView;"),
              require = 1)
    private static MutableQuadView cosmiccore$bakeWithAo(MutableQuadView emitter,
                                                         @Local(name = "result") List<BakedQuad> result) {
        result.add(emitter.toBlockBakedQuad());
        return emitter.emit();
    }

    @Redirect(method = "buildCTMQuads(Lcom/gregtechceu/gtceu/client/model/ctm/TextureConnections;Ljava/util/List;Lnet/minecraft/core/Direction;)Ljava/util/List;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/client/model/quad/Mesh;asBlockBakedQuads(Ljava/util/function/Consumer;)V"),
              require = 1)
    private static void cosmiccore$skipLossyMesh(Mesh mesh, Consumer<BakedQuad> consumer) {}
}
