package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.client.CosmicCoreClient;

import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

@OnlyIn(Dist.CLIENT)
public class CosmicCoreRenderTypes extends RenderType {

    protected static final ShaderStateShard NEBULAE_SHADER = new ShaderStateShard(CosmicCoreClient::getNebulaeShader);
    protected static final ShaderStateShard SOUL_AURA_SHADER = new ShaderStateShard(
            CosmicCoreClient::getSoulAuraShader);

    private static final RenderType NEBULAE = RenderType.create("nebulae",
            DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(NEBULAE_SHADER)
                    .createCompositeState(false));

    private static final RenderType SOUL_AURA = RenderType.create("soul_aura",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(SOUL_AURA_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    private static final RenderType COMPUTATION_ARRAY_LED = RenderType.create("cosmiccore:computation_array_led",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 2048, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

    private CosmicCoreRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                  boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState,
                                  Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType nebulae() {
        return NEBULAE;
    }

    public static RenderType soulAura() {
        return SOUL_AURA;
    }

    public static RenderType computationArrayLed() {
        return COMPUTATION_ARRAY_LED;
    }
}
