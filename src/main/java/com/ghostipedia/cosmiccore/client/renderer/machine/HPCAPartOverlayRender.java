package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.HPCAIndicatorPartMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.BloomUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.util.FastColor.ARGB32.blue;
import static net.minecraft.util.FastColor.ARGB32.color;
import static net.minecraft.util.FastColor.ARGB32.green;
import static net.minecraft.util.FastColor.ARGB32.red;

public class HPCAPartOverlayRender extends DynamicRender<HPCAIndicatorPartMachine, HPCAPartOverlayRender> {

    // spotless:off
    public static final Codec<HPCAPartOverlayRender> CODEC = Codec.unit(HPCAPartOverlayRender::new);
    public static final DynamicRenderType<HPCAIndicatorPartMachine, HPCAPartOverlayRender> TYPE = new DynamicRenderType<>(HPCAPartOverlayRender.CODEC);
    // spotless:on

    public HPCAPartOverlayRender() {}

    @Override
    public @NotNull DynamicRenderType<HPCAIndicatorPartMachine, HPCAPartOverlayRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(HPCAIndicatorPartMachine machine, Vec3 cameraPos) {
        return machine.recipeLogic.isWorking() || delta > 0;
    }

    @Override
    public void render(HPCAIndicatorPartMachine machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.recipeLogic.isWorking() && delta <= 0) {
            return;
        }
        if (GTCEu.Mods.isShimmerLoaded()) {
            PoseStack finalStack = RenderUtils.copyPoseStack(poseStack);
            BloomUtils.entityBloom(source -> renderLightRing(machine, partialTick, finalStack,
                    source.getBuffer(GTRenderTypes.getLightRing())));
        } else {
            renderLightRing(machine, partialTick, poseStack, buffer.getBuffer(GTRenderTypes.getLightRing()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderLightRing(FusionReactorMachine machine, float partialTicks, PoseStack stack,
                                 VertexConsumer buffer) {
        var color = machine.getColor();
        var alpha = 1f;
        if (machine.recipeLogic.isWorking()) {
            lastColor = color;
            delta = FADEOUT;
        } else {
            alpha = delta / FADEOUT;
            lastColor = color(Mth.floor(alpha * 255), red(lastColor), green(lastColor), blue(lastColor));
            delta -= Minecraft.getInstance().getDeltaFrameTime();
        }

        final var lerpFactor = Math.abs((Math.abs(machine.getOffsetTimer() % 50) + partialTicks) - 25) / 25;
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        var back = RelativeDirection.BACK.getRelative(front, upwards, flipped);
        var axis = RelativeDirection.UP.getRelative(front, upwards, flipped).getAxis();
        var r = Mth.lerp(lerpFactor, red(lastColor), 255) / 255f;
        var g = Mth.lerp(lerpFactor, green(lastColor), 255) / 255f;
        var b = Mth.lerp(lerpFactor, blue(lastColor), 255) / 255f;
        RenderBufferHelper.renderRing(stack, buffer,
                back.getStepX() * 7 + 0.5F,
                back.getStepY() * 7 + 0.5F,
                back.getStepZ() * 7 + 0.5F,
                6, 0.2F, 10, 20,
                r, g, b, alpha, axis);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(FusionReactorMachine machine) {
        return machine.recipeLogic.isWorking() || delta > 0;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public AABB getRenderBoundingBox(FusionReactorMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance() / 2.0D);
    }

    public static void initType() {}
}
