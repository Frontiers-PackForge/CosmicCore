package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.*;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;

import java.io.IOException;

public class CosmicCoreClient {

    private CosmicCoreClient() {}

    public static void init(IEventBus modBus) {
        modBus.register(CosmicCoreClient.class);

        DynamicRenderManager.register(CosmicCore.id("hpca_indicator"), HPCAIndicatorRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("hellfire_foundry_parts"), HellFireFoundryPartRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("hemographic_transfuser"), HemophagicTransfuserRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("suffering_chamber"), SufferingChamberRenderer.TYPE);
        DynamicRenderManager.register(CosmicCore.id("stellar_iris"), StellarIrisRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("star_ballast"), StarBallastRender.TYPE);
    }

    @Getter
    private static ShaderInstance nebulaeShader;

    @Getter
    private static ShaderInstance gravityShader;

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("rendertype_nebulae"),
                    DefaultVertexFormat.POSITION), (shaderInstance) -> nebulaeShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("rendertype_gravity_shader"),
                    DefaultVertexFormat.POSITION_TEX), (shaderInstance) -> gravityShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onGUIRegisterUIOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("cosmichud", new CosmicHudGuiOverlay());
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(StellarIrisRender.IRIS_MODEL_CORE);
        event.register(StellarIrisRender.IRIS_MODEL_RING);
        event.register(StellarIrisRender.IRIS_MODEL_RING_WHITE);

        event.register(StarBallastRender.STAR_MODEL_CORE);
        event.register(StarBallastRender.STAR_MODEL_OUTER);
        event.register(StarBallastRender.STAR_MODEL_INNER);
        event.register(StarBallastRender.STAR_MODEL_BEAM);
    }
}
