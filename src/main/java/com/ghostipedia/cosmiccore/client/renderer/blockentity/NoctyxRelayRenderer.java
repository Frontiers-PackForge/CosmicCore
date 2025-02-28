package com.ghostipedia.cosmiccore.client.renderer.blockentity;

import com.ghostipedia.cosmiccore.api.noctyx.NoctyxTypes;
import com.ghostipedia.cosmiccore.api.utility.render.LaserUtil;
import com.ghostipedia.cosmiccore.common.blockentity.NoctyxBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;

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
        if (noctyxEntity.getOwnType().equals(NoctyxTypes.ALL)) {
            handleConnector(noctyxEntity, stack, buffer);
        } else {
            handleRelay(noctyxEntity, stack, buffer);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(NoctyxBlockEntity noctyxEntity) {
        return !noctyxEntity.getNeighbors().isEmpty();
    }

    private void handleConnector(NoctyxBlockEntity noctyxEntity, PoseStack stack, MultiBufferSource buffer) {
        var selfPos = noctyxEntity.getBlockPos();
        noctyxEntity.getNeighbors().forEach(pos -> {
            assert noctyxEntity.getLevel() != null;
            if (!(noctyxEntity.getLevel().getBlockEntity(pos) instanceof NoctyxBlockEntity nbe)) {
                return;
            }
            if (nbe.getOwnType() != NoctyxTypes.ALL) {
                return;
            }
            var ray = new Vector3f(pos.getX() - selfPos.getX(), pos.getY() - selfPos.getY(),
                    pos.getZ() - selfPos.getZ());
            LaserUtil.renderLaser(ray, stack, buffer, 1, 1, 1, 1, 0, 0);
        });
    }

    private void handleRelay(NoctyxBlockEntity noctyxEntity, PoseStack stack, MultiBufferSource buffer) {
        var selfPos = noctyxEntity.getBlockPos();
        var type = noctyxEntity.getOwnType();
        noctyxEntity.getNeighbors().forEach(pos -> {
            var ray = new Vector3f(pos.getX() - selfPos.getX(), pos.getY() - selfPos.getY(),
                    pos.getZ() - selfPos.getZ());
            LaserUtil.renderLaser(ray, stack, buffer, type.red(), type.green(), type.blue(), type.alpha(), 0, 0);
        });
    }
}
