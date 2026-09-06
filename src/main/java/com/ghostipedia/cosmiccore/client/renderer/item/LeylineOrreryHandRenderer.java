package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.util.TransformationHelper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class LeylineOrreryHandRenderer {

    private static final float MODEL_UNIT = 1.0F / 16.0F;
    private static final float CUFF_CENTER_X = 8.0F;
    private static final float CUFF_CENTER_Y = 8.2F;
    private static final float FINGERTIP_Z = 4.7F;
    private static final float ARM_TIP_Y = 10.0F;
    private static final float ARM_WIDTH_SCALE = 0.9F;
    private static final float ARM_DEPTH_SCALE = 0.8F;
    private static final float TOOL_BACK_OFFSET = 3.0F;
    private static final float TOOL_DOWN_OFFSET = -0.5F;
    private static final ModelPart[] ARMS = createArms(false);
    private static final ModelPart[] SLEEVES = createArms(true);

    private LeylineOrreryHandRenderer() {}

    public static void offsetTool(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                  PoseStack poseStack) {
        var model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity.level(), entity,
                entity.getId() + context.ordinal());
        var transform = model.getTransforms().getTransform(context);
        float side = leftHand ? -1 : 1;
        var offset = new Vector3f(0, TOOL_DOWN_OFFSET, TOOL_BACK_OFFSET).mul(transform.scale)
                .rotate(TransformationHelper.quatFromXYZ(transform.rotation.x(), transform.rotation.y() * side,
                        transform.rotation.z() * side, true))
                .mul(MODEL_UNIT);
        poseStack.translate(offset.x(), offset.y(), offset.z());
    }

    public static void renderArm(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                 PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!context.firstPerson() || !stack.is(CosmicItems.LEYLINE_ORRERY.get()) ||
                !(entity instanceof AbstractClientPlayer player) || player.isInvisible()) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        var model = minecraft.getItemRenderer().getModel(stack, player.level(), player,
                player.getId() + context.ordinal());
        var skin = player.getSkin();
        boolean slim = skin.model() == PlayerSkin.Model.SLIM;
        float armCenterOffset = slim ? 0.5F : 1.0F;
        int armIndex = (slim ? 2 : 0) + (leftHand ? 1 : 0);
        poseStack.pushPose();
        try {
            var transformedModel = ClientHooks.handleCameraTransforms(poseStack, model, context, leftHand);
            var mounting = transformedModel.getTransforms().getTransform(context).rightRotation;
            float side = leftHand ? -1 : 1;
            poseStack.mulPose(TransformationHelper.quatFromXYZ(mounting.x(), mounting.y() * side,
                    mounting.z() * side, true).conjugate());
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            poseStack.translate(CUFF_CENTER_X * MODEL_UNIT, CUFF_CENTER_Y * MODEL_UNIT,
                    (FINGERTIP_Z + ARM_TIP_Y) * MODEL_UNIT);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.scale(ARM_WIDTH_SCALE, 1.0F, ARM_DEPTH_SCALE);
            poseStack.translate((leftHand ? -armCenterOffset : armCenterOffset) * MODEL_UNIT, 0.0F, 0.0F);
            ARMS[armIndex].render(poseStack, buffer.getBuffer(RenderType.entitySolid(skin.texture())),
                    packedLight, OverlayTexture.NO_OVERLAY);
            if (player.isModelPartShown(leftHand ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE)) {
                SLEEVES[armIndex].render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(skin.texture())),
                        packedLight, OverlayTexture.NO_OVERLAY);
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static ModelPart[] createArms(boolean sleeve) {
        ModelPart[] parts = new ModelPart[4];
        for (int index = 0; index < parts.length; index++) {
            boolean slim = index >= 2;
            boolean left = (index & 1) != 0;
            int width = slim ? 3 : 4;
            int x = left ? -1 : 1 - width;
            int u = left ? (sleeve ? 48 : 32) : 40;
            int v = left ? 48 : (sleeve ? 32 : 16);
            float inflation = sleeve ? 0.25F : 0.0F;
            var cubes = List.of(new ModelPart.Cube(u, v, x, -2, -2, width, 12, 4,
                    inflation, inflation, inflation, false, 64, 64, EnumSet.allOf(Direction.class)));
            parts[index] = new ModelPart(cubes, Map.of());
        }
        return parts;
    }
}
