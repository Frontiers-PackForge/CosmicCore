package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;

import java.io.IOException;

@SuppressWarnings("unused")
public class CosmicCoreClient {

    private CosmicCoreClient() {}

    @Getter
    private static ShaderInstance nebulaeShader;

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("rendertype_nebulae"),
                    DefaultVertexFormat.POSITION), (shaderInstance) -> nebulaeShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
