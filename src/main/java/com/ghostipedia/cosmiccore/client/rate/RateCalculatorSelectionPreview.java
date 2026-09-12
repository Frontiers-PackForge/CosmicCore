package com.ghostipedia.cosmiccore.client.rate;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.RateCalculatorItem;
import com.ghostipedia.cosmiccore.utils.ItemData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class RateCalculatorSelectionPreview {

    private static final int MAX_AXIS = 128;

    private RateCalculatorSelectionPreview() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        CompoundTag data = selection(minecraft.player.getMainHandItem());
        if (data == null) data = selection(minecraft.player.getOffhandItem());
        if (data == null ||
                !data.getString("dimension").equals(minecraft.level.dimension().location().toString()))
            return;
        PoseStack poses = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        poses.pushPose();
        try {
            poses.translate(-camera.x, -camera.y, -camera.z);
            var buffer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.lines());
            for (long direct : data.getLongArray("direct")) {
                BlockPos position = BlockPos.of(direct);
                LevelRenderer.renderLineBox(poses, buffer, new AABB(position), 0.38f, 0.84f, 1.0f, 0.85f);
            }
            ListTag regions = data.getList("regions", Tag.TAG_COMPOUND);
            for (Tag value : regions) {
                CompoundTag region = (CompoundTag) value;
                BlockPos first = BlockPos.of(region.getLong("first"));
                BlockPos second = BlockPos.of(region.getLong("second"));
                if (bounded(first, second))
                    LevelRenderer.renderLineBox(poses, buffer, box(first, second), 0.38f, 0.84f, 1.0f, 0.85f);
            }
            if (data.contains("first")) {
                BlockPos first = BlockPos.of(data.getLong("first"));
                BlockPos second = target(minecraft);
                if (second != null && bounded(first, second))
                    LevelRenderer.renderLineBox(poses, buffer, box(first, second), 0.92f, 0.72f, 0.30f, 0.85f);
            }
        } finally {
            poses.popPose();
        }
        minecraft.renderBuffers().bufferSource().endBatch(RenderType.lines());
    }

    private static CompoundTag selection(ItemStack stack) {
        return stack.getItem() instanceof RateCalculatorItem ? ItemData.readElement(stack, "rateCalculator") : null;
    }

    private static BlockPos target(Minecraft minecraft) {
        return minecraft.hitResult instanceof BlockHitResult hit ? hit.getBlockPos() : null;
    }

    private static boolean bounded(BlockPos first, BlockPos second) {
        return Math.abs(first.getX() - second.getX()) <= MAX_AXIS &&
                Math.abs(first.getY() - second.getY()) <= MAX_AXIS &&
                Math.abs(first.getZ() - second.getZ()) <= MAX_AXIS;
    }

    private static AABB box(BlockPos first, BlockPos second) {
        return new AABB(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()),
                Math.max(first.getX(), second.getX()) + 1, Math.max(first.getY(), second.getY()) + 1,
                Math.max(first.getZ(), second.getZ()) + 1);
    }
}
