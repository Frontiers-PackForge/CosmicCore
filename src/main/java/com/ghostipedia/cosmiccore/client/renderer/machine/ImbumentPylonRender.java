package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.client.gui.AlphaOverrideVertexConsumer;
import com.ghostipedia.cosmiccore.client.renderer.CosmicCoreRenderTypes;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ImbumentPylonMachine;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeRenderAnchors;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

public final class ImbumentPylonRender extends DynamicRender<ImbumentPylonMachine, ImbumentPylonRender> {

    public static final ImbumentPylonRender INSTANCE = new ImbumentPylonRender();
    public static final MapCodec<ImbumentPylonRender> CODEC = MapCodec.unit(INSTANCE);
    public static final DynamicRenderType<ImbumentPylonMachine, ImbumentPylonRender> TYPE = new DynamicRenderType<>(
            CODEC);

    private final PylonBloodStream stream = new PylonBloodStream();
    private final PylonTranslucentBuffer translucent = new PylonTranslucentBuffer();
    private final Map<ImbumentPylonMachine, Animation> animations = new WeakHashMap<>();

    @Override
    public DynamicRenderType<ImbumentPylonMachine, ImbumentPylonRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(ImbumentPylonMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(ImbumentPylonMachine machine) {
        AABB bounds = new AABB(machine.getBlockPos()).minmax(new AABB(
                VitaeRenderAnchors.pylonFocus(machine), VitaeRenderAnchors.pylonPool(machine))).inflate(2);
        return machine.hasRenderSource() ?
                bounds.minmax(new AABB(machine.renderSource(), machine.renderSource()).inflate(4)) : bounds;
    }

    @Override
    public void render(ImbumentPylonMachine machine, float partialTick, PoseStack pose, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.isFormed() || Minecraft.getInstance().player == null) return;
        double time = (double) Minecraft.getInstance().player.tickCount + partialTick;
        Vec3 origin = Vec3.atLowerCornerOf(machine.getBlockPos());
        Vec3 pool = VitaeRenderAnchors.pylonPool(machine).subtract(origin);
        Vec3 focus = VitaeRenderAnchors.pylonFocus(machine).subtract(origin).add(0, Math.sin(time * 0.05) * 0.08, 0);
        VitaeFluidRender.renderPool(pose, buffer, pool, 1);

        Animation animation = animations.computeIfAbsent(machine, ignored -> new Animation());
        double elapsed = Math.max(0, Math.min(2, time - animation.lastTime));
        animation.lastTime = time;
        float target = machine.hasRenderSource() && machine.getRecipeLogic().isWorking() ? 1 : 0;
        animation.strength += (float) Math.clamp(target - animation.strength, -elapsed / 8, elapsed / 8);
        if (!machine.hasRenderSource()) animation.strength = 0;
        if (animation.strength > 0) {
            Vec3 submerged = pool.add(0, -0.65, 0);
            stream.render(pose, translucent, machine.renderSource().subtract(origin), submerged, time,
                    animation.strength,
                    false, 0);
            for (int strand = 0; strand < 2; strand++) {
                stream.render(pose, translucent, submerged, focus, time, animation.strength, true, strand);
            }
        }

        long sinceCompletion = machine.getLevel().getGameTime() - machine.completedAt();
        boolean completed = sinceCompletion >= 0 && sinceCompletion < 20;
        boolean active = machine.getRecipeLogic().isActive();
        if (!active && !completed) {
            translucent.draw(buffer);
            return;
        }
        pose.pushPose();
        pose.translate(focus.x, focus.y, focus.z);
        pose.mulPose(Axis.YP.rotation((float) (time * 0.025 % (Math.PI * 2))));
        if (active) {
            animation.captureRecipe(machine.getRecipeLogic().getLastUnrolledRecipe());
            float progress = (float) Math.clamp((machine.getRecipeLogic().getProgress() +
                    (machine.getRecipeLogic().isWorking() ? partialTick : 0)) /
                    Math.max(1.0, machine.getRecipeLogic().getDuration()), 0, 1);
            if (animation.recipe == null) progress = 0;
            renderFocal(machine, pose, translucent, machine.focalItem(), machine.focalFluid(), 1 - progress);
            renderFocal(machine, pose, translucent, animation.outputItem, animation.outputFluid, progress);
        } else {
            renderFocal(machine, pose, translucent, machine.completedItem(), machine.completedFluid(), 1);
        }
        renderSphere(pose, translucent, time);
        pose.popPose();
        translucent.draw(buffer);
    }

    private static void renderFocal(ImbumentPylonMachine machine, PoseStack pose, MultiBufferSource buffer,
                                    ItemStack item, FluidStack fluid, float alpha) {
        if (alpha <= 0) return;
        MultiBufferSource translucent = type -> new AlphaOverrideVertexConsumer(
                buffer.getBuffer(CosmicCoreRenderTypes.vitaeTranslucent(type)), (double) alpha);
        pose.pushPose();
        if (!item.isEmpty()) {
            pose.scale(1.5f, 1.5f, 1.5f);
            Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, translucent, machine.getLevel(), 0);
        } else if (!fluid.isEmpty()) {
            VitaeFluidRender.renderDroplet(pose, translucent, fluid);
        }
        pose.popPose();
    }

    private static void renderSphere(PoseStack pose, MultiBufferSource buffer, double time) {
        VertexConsumer vertices = buffer.getBuffer(CosmicCoreRenderTypes.vitaeSphere());
        float radius = sphereRadius(time);
        float scroll = (float) ((time * 0.05) % 1);
        for (int latitude = 0; latitude < 16; latitude++) {
            double lower = -Math.PI / 2 + latitude * Math.PI / 16;
            double upper = lower + Math.PI / 16;
            for (int longitude = 0; longitude < 24; longitude++) {
                double left = longitude * Math.PI / 12;
                double right = left + Math.PI / 12;
                sphereVertex(vertices, pose, radius, lower, left, scroll);
                sphereVertex(vertices, pose, radius, upper, left, scroll);
                sphereVertex(vertices, pose, radius, upper, right, scroll);
                sphereVertex(vertices, pose, radius, lower, right, scroll);
            }
        }
    }

    static float sphereRadius(double time) {
        return (float) (0.95 + 0.025 * Math.sin(time * 0.12));
    }

    private static void sphereVertex(VertexConsumer vertices, PoseStack pose, float radius,
                                     double latitude, double longitude, float scroll) {
        float nx = (float) (Math.cos(latitude) * Math.cos(longitude));
        float ny = (float) Math.sin(latitude);
        float nz = (float) (Math.cos(latitude) * Math.sin(longitude));
        float u = (float) (longitude * 3 / (Math.PI * 2) + latitude * 0.2);
        float v = (float) ((latitude + Math.PI / 2) * 2 / Math.PI) - scroll;
        vertices.addVertex(pose.last().pose(), radius * nx, radius * ny, radius * nz)
                .setColor(165, 16, 36, 100).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose.last(), nx, ny, nz);
    }

    private static final class Animation {

        private double lastTime;
        private float strength;
        private GTRecipe recipe;
        private ItemStack outputItem = ItemStack.EMPTY;
        private FluidStack outputFluid = FluidStack.EMPTY;

        private void captureRecipe(GTRecipe current) {
            if (recipe == current) return;
            recipe = current;
            outputItem = current == null ? ItemStack.EMPTY : current.getOutputContents(ItemRecipeCapability.CAP)
                    .stream()
                    .flatMap(content -> Arrays.stream(ItemRecipeCapability.CAP.of(content.content()).getItems()))
                    .filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
            outputFluid = current == null ? FluidStack.EMPTY : current.getOutputContents(FluidRecipeCapability.CAP)
                    .stream()
                    .flatMap(content -> Arrays.stream(FluidRecipeCapability.CAP.of(content.content()).getFluids()))
                    .filter(stack -> !stack.isEmpty()).findFirst().orElse(FluidStack.EMPTY);
        }
    }
}
