package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.client.CosmicCoreClient;

import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraftforge.client.event.ScreenEvent;

@OnlyIn(Dist.CLIENT)
public class CosmicCoreRenderTypes extends RenderType {


    private CosmicCoreRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                  boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState,
                                  Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    protected static final ShaderStateShard NEBULAE_SHADER = new ShaderStateShard(CosmicCoreClient::getNebulaeShader);

    private static final RenderType NEBULAE = RenderType.create("nebulae",
            DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(NEBULAE_SHADER)
                    .createCompositeState(false));

    public static RenderType nebulae() {
        return NEBULAE;
    }

    protected static final ShaderStateShard GRAVITY_SHADER = new ShaderStateShard(CosmicCoreClient::getGravityShader);
    private static final RenderType GRAVITY = RenderType.create("gravity_well",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS,1024,false, false,
            RenderType.CompositeState.builder().setShaderState(GRAVITY_SHADER).createCompositeState(false));

    public static RenderType gravity(){
        return GRAVITY;
    }

}
