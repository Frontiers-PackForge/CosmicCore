package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.client.CosmicCoreClient;
import com.ghostipedia.cosmiccore.client.compat.IrisCompat;

import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class CosmicCoreRenderTypes extends RenderType {

    private static final RenderType VITAE_SPHERE = RenderType.create("cosmiccore:vitae_sphere",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 65536, false, true,
            CompositeState.builder()
                    .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentEmissiveShader))
                    .setTextureState(new TextureStateShard(
                            ResourceLocation.fromNamespaceAndPath("neovitae", "textures/misc/stream.png"), false,
                            false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                    .setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
                    .createCompositeState(false));
    private static final Function<RenderType, RenderType> VITAE_TRANSLUCENT = Util.memoize(
            original -> new RenderType("cosmiccore:vitae_focal/" + original, original.format(), original.mode(),
                    original.bufferSize(), false, true, () -> {
                        original.setupRenderState();
                        if (original.format() == DefaultVertexFormat.NEW_ENTITY) {
                            RenderSystem.setShader(GameRenderer::getRendertypeEntityTranslucentShader);
                        } else if (original.format() == DefaultVertexFormat.BLOCK) {
                            RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
                        }
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.depthMask(false);
                    }, () -> {
                        original.clearRenderState();
                        RenderSystem.depthMask(true);
                        RenderSystem.disableBlend();
                    }) {});

    public static RenderType vitaeSphere() {
        return VITAE_SPHERE;
    }

    public static RenderType vitaeTranslucent(RenderType original) {
        return VITAE_TRANSLUCENT.apply(original);
    }

    protected static final ShaderStateShard NEBULAE_SHADER = new ShaderStateShard(CosmicCoreClient::getNebulaeShader);
    protected static final ShaderStateShard FIRMAMENT_STORM_CURRENT_SHADER = new ShaderStateShard(
            CosmicCoreClient::getFirmamentStormCurrentShader);
    protected static final ShaderStateShard FIRMAMENT_WIND_CURRENT_SHADER = new ShaderStateShard(
            CosmicCoreClient::getFirmamentWindCurrentShader);
    protected static final ShaderStateShard POSITION_TEX_COLOR_CARRIER_SHADER = new ShaderStateShard(
            GameRenderer::getPositionTexColorShader);
    private static final RenderType NEBULAE = RenderType.create("nebulae",
            DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(NEBULAE_SHADER)
                    .createCompositeState(false));

    private static final RenderType COMPUTATION_ARRAY_LED = RenderType.create("cosmiccore:computation_array_led",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 2048, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

    private static final RenderType FIRMAMENT_STORM_CURRENT = RenderType.create("cosmiccore:firmament_storm_current",
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 786432, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(FIRMAMENT_STORM_CURRENT_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    private static final RenderType FIRMAMENT_STORM_CURRENT_IRIS = RenderType.create(
            "cosmiccore:firmament_storm_current_iris",
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 786432, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_TEX_COLOR_CARRIER_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    private static final RenderType FIRMAMENT_WIND_CURRENT = RenderType.create("cosmiccore:firmament_wind_current",
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 262144, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(FIRMAMENT_WIND_CURRENT_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    private static final RenderType FIRMAMENT_WIND_CURRENT_IRIS = RenderType.create(
            "cosmiccore:firmament_wind_current_iris",
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 262144, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_TEX_COLOR_CARRIER_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    private CosmicCoreRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                  boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState,
                                  Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType nebulae() {
        return NEBULAE;
    }

    public static RenderType computationArrayLed() {
        return COMPUTATION_ARRAY_LED;
    }

    public static RenderType firmamentStormCurrent() {
        return IrisCompat.shadersActive() ? FIRMAMENT_STORM_CURRENT_IRIS : FIRMAMENT_STORM_CURRENT;
    }

    public static RenderType firmamentWindCurrent() {
        return IrisCompat.shadersActive() ? FIRMAMENT_WIND_CURRENT_IRIS : FIRMAMENT_WIND_CURRENT;
    }
}
