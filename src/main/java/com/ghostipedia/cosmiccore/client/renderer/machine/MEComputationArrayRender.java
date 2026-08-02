package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.client.renderer.CosmicCoreRenderTypes;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationArrayMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.MEComputationComponentPartMachine;

import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.client.bloom.BloomHandler;
import com.gregtechceu.gtceu.client.bloom.BloomRenderTicket;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.bloom.EffectRenderContext;
import com.gregtechceu.gtceu.client.bloom.IBloomEffect;
import com.gregtechceu.gtceu.client.bloom.IRenderSetup;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.MapCodec;
import org.joml.Matrix4f;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class MEComputationArrayRender extends
                                            DynamicRender<MEComputationArrayMachine, MEComputationArrayRender> {

    public static final MEComputationArrayRender INSTANCE = new MEComputationArrayRender();
    public static final MapCodec<MEComputationArrayRender> CODEC = MapCodec.unit(INSTANCE);
    public static final DynamicRenderType<MEComputationArrayMachine, MEComputationArrayRender> TYPE = new DynamicRenderType<>(
            CODEC);

    private static final Direction[] LED_FACES = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };
    private static final float LED_HALF_SIZE = 1.0f / 16.0f;
    private static final float LED_FACE_OFFSET = 0.5f + 1.0f / 512.0f;
    private static final IRenderSetup BLOOM_SETUP = new IRenderSetup() {

        @Override
        public BufferBuilder preDraw() {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }

        @Override
        public void postDraw(BufferBuilder buffer) {
            IRenderSetup.super.postDraw(buffer);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
        }
    };
    private final Map<MEComputationArrayMachine, BloomRenderTicket> bloomTickets = new WeakHashMap<>();

    private MEComputationArrayRender() {}

    @Override
    public DynamicRenderType<MEComputationArrayMachine, MEComputationArrayRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(MEComputationArrayMachine machine, Vec3 cameraPos) {
        boolean active = machine.isFormed() && hasActiveComponents(machine);
        if (!active) {
            invalidateBloomTicket(machine);
        }
        return active && super.shouldRender(machine, cameraPos);
    }

    @Override
    public void render(MEComputationArrayMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ensureBloomTicket(machine);
        renderComponents(machine, poseStack, buffer.getBuffer(CosmicCoreRenderTypes.computationArrayLed()), false);
    }

    @Override
    public AABB getRenderBoundingBox(MEComputationArrayMachine machine) {
        AABB bounds = new AABB(machine.getBlockPos());
        for (MultiblockPartMachine part : machine.getParts()) {
            if (part instanceof MEComputationComponentPartMachine) {
                bounds = bounds.minmax(new AABB(part.getBlockPos()));
            }
        }
        return bounds.inflate(0.75);
    }

    private void ensureBloomTicket(MEComputationArrayMachine machine) {
        BloomRenderTicket ticket = bloomTickets.get(machine);
        if ((ticket != null && ticket.isValid()) || !BloomShaderManager.isBloomActive()) {
            return;
        }
        WeakReference<MEComputationArrayMachine> machineReference = new WeakReference<>(machine);
        ticket = BloomHandler.registerBloomRender(
                BLOOM_SETUP,
                new ComputationArrayBloomEffect(machineReference),
                ignored -> {
                    MEComputationArrayMachine referencedMachine = machineReference.get();
                    return referencedMachine != null && !referencedMachine.isRemoved();
                },
                () -> {
                    MEComputationArrayMachine referencedMachine = machineReference.get();
                    return referencedMachine == null ? null : referencedMachine.getLevel();
                });
        if (ticket.isValid()) {
            bloomTickets.put(machine, ticket);
        }
    }

    private void invalidateBloomTicket(MEComputationArrayMachine machine) {
        BloomRenderTicket ticket = bloomTickets.remove(machine);
        if (ticket != null && ticket.isValid()) {
            ticket.invalidate();
        }
    }

    private void renderComponents(MEComputationArrayMachine machine, PoseStack poseStack, VertexConsumer consumer,
                                  boolean bloomPass) {
        BlockPos controllerPos = machine.getBlockPos();
        for (MultiblockPartMachine part : machine.getParts()) {
            if (!(part instanceof MEComputationComponentPartMachine component) || !component.isActive()) {
                continue;
            }
            BlockPos offset = component.getBlockPos().subtract(controllerPos);
            poseStack.pushPose();
            poseStack.translate(offset.getX() + 0.5, offset.getY() + 0.5, offset.getZ() + 0.5);
            renderComponent(component, poseStack, consumer, bloomPass);
            poseStack.popPose();
        }
    }

    private void renderComponent(MEComputationComponentPartMachine component, PoseStack poseStack,
                                 VertexConsumer consumer, boolean bloomPass) {
        boolean compute = component.role() == MEComputationComponentPartMachine.Role.COMPUTATION_CORE;
        float red = compute ? 0.16f : 1.0f;
        float green = compute ? 0.82f : 0.48f;
        float blue = compute ? 1.0f : 0.08f;
        float alpha = bloomPass ? 0.82f : 1.0f;
        for (Direction direction : LED_FACES) {
            renderLedPlate(poseStack, consumer, direction, red, green, blue, alpha);
        }
    }

    private static void renderLedPlate(PoseStack poseStack, VertexConsumer consumer, Direction direction,
                                       float red, float green, float blue, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        float faceX = direction.getStepX() * LED_FACE_OFFSET;
        float faceZ = direction.getStepZ() * LED_FACE_OFFSET;
        switch (direction) {
            case NORTH -> renderLedQuad(matrix, consumer,
                    -LED_HALF_SIZE, -LED_HALF_SIZE, faceZ,
                    -LED_HALF_SIZE, LED_HALF_SIZE, faceZ,
                    LED_HALF_SIZE, LED_HALF_SIZE, faceZ,
                    LED_HALF_SIZE, -LED_HALF_SIZE, faceZ,
                    red, green, blue, alpha);
            case SOUTH -> renderLedQuad(matrix, consumer,
                    -LED_HALF_SIZE, -LED_HALF_SIZE, faceZ,
                    LED_HALF_SIZE, -LED_HALF_SIZE, faceZ,
                    LED_HALF_SIZE, LED_HALF_SIZE, faceZ,
                    -LED_HALF_SIZE, LED_HALF_SIZE, faceZ,
                    red, green, blue, alpha);
            case WEST -> renderLedQuad(matrix, consumer,
                    faceX, -LED_HALF_SIZE, -LED_HALF_SIZE,
                    faceX, -LED_HALF_SIZE, LED_HALF_SIZE,
                    faceX, LED_HALF_SIZE, LED_HALF_SIZE,
                    faceX, LED_HALF_SIZE, -LED_HALF_SIZE,
                    red, green, blue, alpha);
            case EAST -> renderLedQuad(matrix, consumer,
                    faceX, -LED_HALF_SIZE, -LED_HALF_SIZE,
                    faceX, LED_HALF_SIZE, -LED_HALF_SIZE,
                    faceX, LED_HALF_SIZE, LED_HALF_SIZE,
                    faceX, -LED_HALF_SIZE, LED_HALF_SIZE,
                    red, green, blue, alpha);
            default -> throw new IllegalArgumentException(direction.getName());
        }
    }

    private static void renderLedQuad(Matrix4f matrix, VertexConsumer consumer,
                                      float x0, float y0, float z0,
                                      float x1, float y1, float z1,
                                      float x2, float y2, float z2,
                                      float x3, float y3, float z3,
                                      float red, float green, float blue, float alpha) {
        consumer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x3, y3, z3).setColor(red, green, blue, alpha);
    }

    private static boolean hasActiveComponents(MEComputationArrayMachine machine) {
        for (MultiblockPartMachine part : machine.getParts()) {
            if (part instanceof MEComputationComponentPartMachine component && component.isActive()) {
                return true;
            }
        }
        return false;
    }

    private final class ComputationArrayBloomEffect implements IBloomEffect {

        private final WeakReference<MEComputationArrayMachine> machineReference;

        private ComputationArrayBloomEffect(WeakReference<MEComputationArrayMachine> machineReference) {
            this.machineReference = machineReference;
        }

        @Override
        public void renderBloomEffect(PoseStack poseStack, BufferBuilder buffer, EffectRenderContext context) {
            MEComputationArrayMachine machine = machineReference.get();
            if (machine == null) {
                return;
            }
            BlockPos pos = machine.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            renderComponents(machine, poseStack, buffer, true);
            poseStack.popPose();
        }

        @Override
        public boolean shouldRenderBloomEffect(EffectRenderContext context) {
            MEComputationArrayMachine machine = machineReference.get();
            return machine != null && MEComputationArrayRender.this.shouldRender(machine, context.camPos()) &&
                    context.frustum().isVisible(MEComputationArrayRender.this.getRenderBoundingBox(machine));
        }
    }
}
