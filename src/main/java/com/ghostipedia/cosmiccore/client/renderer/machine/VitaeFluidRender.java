package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.gregtechceu.gtceu.client.util.RenderBufferHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.EnumSet;

final class VitaeFluidRender {

    private VitaeFluidRender() {}

    static void renderPool(PoseStack pose, MultiBufferSource buffer, Vec3 center, int radius) {
        var blood = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("biomesoplenty", "blood"));
        boolean fallback = blood == Fluids.EMPTY;
        var extension = IClientFluidTypeExtensions.of(fallback ? Fluids.WATER : blood);
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(extension.getStillTexture());
        var consumer = buffer.getBuffer(Sheets.solidBlockSheet());
        int color = fallback ? 0xFF950D20 : extension.getTintColor() | 0xFF000000;
        pose.pushPose();
        pose.translate(center.x, center.y, center.z);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x) == radius && Math.abs(z) == radius) continue;
                RenderBufferHelper.renderTexturedCube(consumer, pose.last(), EnumSet.of(Direction.UP), color,
                        LightTexture.FULL_BRIGHT, sprite, x - 0.5f, -0.92f, z - 0.5f, x + 0.5f, 0, z + 0.5f);
            }
        }
        pose.popPose();
    }

    static void renderDroplet(PoseStack pose, MultiBufferSource buffer, FluidStack fluid) {
        var extension = IClientFluidTypeExtensions.of(fluid.getFluid());
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(extension.getStillTexture(fluid));
        var consumer = buffer.getBuffer(Sheets.solidBlockSheet());
        int color = extension.getTintColor(fluid) | 0xFF000000;
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            double b = (i + 1) * Math.PI / 4;
            float ax = (float) Math.cos(a) * 0.35f;
            float az = (float) Math.sin(a) * 0.35f;
            float bx = (float) Math.cos(b) * 0.35f;
            float bz = (float) Math.sin(b) * 0.35f;
            RenderBufferHelper.renderCubeFace(consumer, pose.last(), color, LightTexture.FULL_BRIGHT, Direction.UP,
                    0, 0.55f, 0, sprite.getU0(), sprite.getV0(),
                    bx, 0, bz, sprite.getU1(), sprite.getV1(),
                    ax, 0, az, sprite.getU0(), sprite.getV1(),
                    0, 0.55f, 0, sprite.getU0(), sprite.getV0());
            RenderBufferHelper.renderCubeFace(consumer, pose.last(), color, LightTexture.FULL_BRIGHT, Direction.DOWN,
                    0, -0.35f, 0, sprite.getU0(), sprite.getV0(),
                    ax, 0, az, sprite.getU0(), sprite.getV1(),
                    bx, 0, bz, sprite.getU1(), sprite.getV1(),
                    0, -0.35f, 0, sprite.getU0(), sprite.getV0());
        }
    }
}
