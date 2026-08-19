package com.ghostipedia.cosmiccore.client.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class SonarPulseRenderer {

    private SonarPulseRenderer() {}

    private static final float PULSE_SPEED = 2.8f;
    private static final float PULSE_RANGE = 96f;
    private static final int SCAN_DOWN = 120;
    private static final int SCAN_UP = 40;
    private static final int FACE_LIFETIME = 140;
    private static final int MAX_FACES = 48000;
    private static final float FACE_INSET = 0.014f;
    private static final int NEAR_R = 0xFF, NEAR_G = 0x5A, NEAR_B = 0x4A;
    private static final int FAR_R = 0x59, FAR_G = 0xE8, FAR_B = 0xFF;

    private static final Deque<FaceHit> FACES = new ArrayDeque<>();
    private static final LongSet SEEN = new LongOpenHashSet();
    private static Vec3 pulseOrigin = null;
    private static float ringRadius = 0f;

    private record FaceHit(BlockPos pos, Direction face, long born) {}

    public static void firePulse(Vec3 origin) {
        pulseOrigin = origin;
        ringRadius = 1.5f;
        FACES.clear();
        SEEN.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            var resonate = BuiltInRegistries.SOUND_EVENT
                    .get(ResourceLocation.parse("block.amethyst_block.resonate"));
            if (resonate != null) {
                mc.level.playLocalSound(origin.x, origin.y, origin.z, resonate, SoundSource.PLAYERS, 0.9f, 0.6f,
                        false);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;
        long now = MurkbloomClientState.ticks();

        while (!FACES.isEmpty() && now - FACES.peekFirst().born() > FACE_LIFETIME) {
            FACES.pollFirst();
        }

        if (pulseOrigin == null) return;
        float r0 = ringRadius;
        float r1 = Math.min(r0 + PULSE_SPEED, PULSE_RANGE);
        int oy = Mth.floor(pulseOrigin.y);
        int ox = Mth.floor(pulseOrigin.x);
        int oz = Mth.floor(pulseOrigin.z);
        int bound = Mth.ceil(r1);

        for (int dx = -bound; dx <= bound; dx++) {
            for (int dz = -bound; dz <= bound; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < r0 || dist >= r1) continue;
                scanColumn(mc.level, ox + dx, oz + dz, oy, now);
            }
        }

        ringRadius = r1;
        if (r1 >= PULSE_RANGE) {
            pulseOrigin = null;
        }
    }

    private static void scanColumn(Level level, int x, int z, int oy, long now) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (int y = oy + SCAN_UP; y >= oy - SCAN_DOWN; y--) {
            pos.set(x, y, z);
            if (!isSolid(level, pos)) continue;
            BlockPos surface = null;
            for (Direction direction : Direction.values()) {
                neighbor.set(
                        x + direction.getStepX(),
                        y + direction.getStepY(),
                        z + direction.getStepZ());
                if (!isSolid(level, neighbor)) {
                    if (surface == null) surface = pos.immutable();
                    addFace(surface, direction, now);
                }
            }
        }
    }

    private static void addFace(BlockPos pos, Direction face, long now) {
        if (FACES.size() >= MAX_FACES) return;
        long key = (pos.asLong() << 3) | face.ordinal();
        if (!SEEN.add(key)) return;
        FACES.addLast(new FaceHit(pos, face, now));
    }

    private static boolean isSolid(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    public static void renderWireframe(Matrix4f matrix, MultiBufferSource.BufferSource buffers, Vec3 cam,
                                       long now) {
        if (FACES.isEmpty()) return;
        VertexConsumer vc = buffers.getBuffer(RenderType.lines());
        for (FaceHit hit : FACES) {
            float life = (now - hit.born()) / (float) FACE_LIFETIME;
            float in = Mth.clamp(life / 0.05f, 0f, 1f);
            float out = Mth.clamp((1f - life) / 0.45f, 0f, 1f);
            int alpha = (int) (235 * in * out);
            if (alpha <= 3) continue;
            double dist = Math.sqrt(hit.pos().distToCenterSqr(cam.x, cam.y, cam.z));
            float t = Mth.clamp((float) (dist / PULSE_RANGE), 0f, 1f);
            int r = (int) Mth.lerp(t, NEAR_R, FAR_R);
            int g = (int) Mth.lerp(t, NEAR_G, FAR_G);
            int b = (int) Mth.lerp(t, NEAR_B, FAR_B);
            drawFaceOutline(vc, matrix, hit.pos(), hit.face(), r, g, b, alpha);
        }
        buffers.endBatch(RenderType.lines());
    }

    private static void drawFaceOutline(VertexConsumer vc, Matrix4f matrix, BlockPos pos, Direction face,
                                        int r, int g, int b, int alpha) {
        Direction.Axis axis = face.getAxis();
        float bx = pos.getX(), by = pos.getY(), bz = pos.getZ();
        float o = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1f + FACE_INSET : -FACE_INSET;
        float[][] corners = switch (axis) {
            case X -> new float[][] { { bx + o, by, bz }, { bx + o, by + 1, bz }, { bx + o, by + 1, bz + 1 },
                    { bx + o, by, bz + 1 } };
            case Y -> new float[][] { { bx, by + o, bz }, { bx + 1, by + o, bz }, { bx + 1, by + o, bz + 1 },
                    { bx, by + o, bz + 1 } };
            case Z -> new float[][] { { bx, by, bz + o }, { bx + 1, by, bz + o }, { bx + 1, by + 1, bz + o },
                    { bx, by + 1, bz + o } };
        };
        for (int i = 0; i < 4; i++) {
            float[] a = corners[i];
            float[] c = corners[(i + 1) % 4];
            line(vc, matrix, a, c, r, g, b, alpha);
        }
    }

    private static void line(VertexConsumer vc, Matrix4f matrix, float[] a, float[] b, int r, int g, int bl,
                             int alpha) {
        float dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
        float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0e-4f) return;
        dx /= len;
        dy /= len;
        dz /= len;
        vc.addVertex(matrix, a[0], a[1], a[2]).setColor(r, g, bl, alpha).setNormal(dx, dy, dz);
        vc.addVertex(matrix, b[0], b[1], b[2]).setColor(r, g, bl, alpha).setNormal(dx, dy, dz);
    }
}
