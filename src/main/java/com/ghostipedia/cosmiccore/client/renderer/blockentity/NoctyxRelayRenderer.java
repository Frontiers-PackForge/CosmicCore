package com.ghostipedia.cosmiccore.client.renderer.blockentity;

import com.ghostipedia.cosmiccore.api.noctyx.NoctyxType;
import com.ghostipedia.cosmiccore.common.blockentity.NoctyxBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NoctyxRelayRenderer implements BlockEntityRenderer<NoctyxBlockEntity> {

    protected BlockEntityRendererProvider.Context context;

    public NoctyxRelayRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(NoctyxBlockEntity noctyxEntity, float partialTick, PoseStack stack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (noctyxEntity.getOwnType() == NoctyxType.ALL) {
            handleConnector();
        } else {
            handleRelay();
        }
    }
    
    private void handleConnector() {
        // todo: render connector connections (prioritize input/output colors)
    }
    
    private void handleRelay() {
        // todo: render connector connections (prioritize self color)
    }
}
