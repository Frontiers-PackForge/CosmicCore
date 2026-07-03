package com.ghostipedia.cosmiccore.client.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicParticleTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class MurkWispRenderer {

    private MurkWispRenderer() {}

    private static final int[] TARGET_COUNT = { 0, 3, 7, 14, 22 };
    private static final float[] RING_MIN = { 0f, 30f, 18f, 10f, 5f };
    private static final float[] RING_MAX = { 0f, 38f, 26f, 16f, 9f };
    private static final float TANGENTIAL_SPEED = 0.09f;
    private static final float CURIOSITY_SPEED = 0.014f;
    private static final float APPROACH_SPEED = 0.05f;
    private static final float FLINCH_SPEED = 0.55f;

    private static final List<Wisp> WISPS = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();
    private static long lastBurst = 0;
    private static long nextShyRoll = 0;
    private static long nextFreezeRoll = 0;
    private static long freezeUntil = -1;

    private static final class Wisp {

        Vec3 pos;
        Vec3 vel = Vec3.ZERO;
        float baseSize;
        float phase;
        float orbitDir;
        int age = 0;
        boolean shy = false;

        Wisp(Vec3 pos, float baseSize, float phase, float orbitDir) {
            this.pos = pos;
            this.baseSize = baseSize;
            this.phase = phase;
            this.orbitDir = orbitDir;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        if (!AbyssClientFog.inHollowWater(mc)) {
            WISPS.clear();
            return;
        }

        float intensity = MurkbloomClientState.intensity();
        float fIdx = Mth.clamp(intensity, 0f, 1f) * 4f;
        int target = Math.round(arrayLerp(TARGET_COUNT, fIdx));
        float ringMin = arrayLerp(RING_MIN, Math.max(fIdx, 1f));
        float ringMax = arrayLerp(RING_MAX, Math.max(fIdx, 1f));
        Vec3 player = mc.player.getEyePosition();

        while (WISPS.size() > target) {
            WISPS.remove(WISPS.size() - 1);
        }
        if (WISPS.size() < target && RANDOM.nextInt(3) == 0) {
            spawnWisp(player, ringMin, ringMax);
        }

        float flinch = MurkbloomClientState.peekFlinch();
        long ticks = MurkbloomClientState.ticks();
        float hunger = Mth.clamp((intensity - 0.42f) / 0.58f, 0f, 1f);
        Vec3 look = mc.player.getLookAngle();

        if (intensity > 0.35f && ticks >= nextShyRoll) {
            nextShyRoll = ticks + 300 + RANDOM.nextInt(500);
            spawnShyWisp(mc, player, look);
        }
        if (intensity > 0.60f && ticks >= nextFreezeRoll) {
            nextFreezeRoll = ticks + 800 + RANDOM.nextInt(1200);
            freezeUntil = ticks + 24 + RANDOM.nextInt(12);
        }
        boolean frozen = ticks < freezeUntil;

        Iterator<Wisp> it = WISPS.iterator();
        while (it.hasNext()) {
            Wisp w = it.next();
            if (frozen && !w.shy) {
                w.vel = Vec3.ZERO;
                emit(mc, w, ticks, intensity);
                continue;
            }
            w.phase += 0.03f;
            w.age++;
            Vec3 toPlayer = player.subtract(w.pos);
            double dist = toPlayer.length();
            if (dist < 0.5 || dist > ringMax * 1.8) {
                it.remove();
                continue;
            }
            Vec3 dir = toPlayer.scale(1.0 / dist);
            if (w.shy) {
                double seen = look.dot(dir.scale(-1));
                if (seen > 0.72) {
                    w.vel = w.vel.scale(0.6).add(dir.scale(-0.35));
                    w.baseSize *= 0.90f;
                    if (w.baseSize < 0.4f) {
                        it.remove();
                        continue;
                    }
                    Vec3 fleeNext = w.pos.add(w.vel);
                    if (isOpenWater(mc, fleeNext)) {
                        w.pos = fleeNext;
                    }
                    emit(mc, w, ticks, intensity);
                    continue;
                }
            }
            Vec3 tangent = new Vec3(-dir.z, 0, dir.x).scale(w.orbitDir);
            Vec3 vel = tangent.scale(TANGENTIAL_SPEED * (0.7 + 0.3 * Math.sin(w.phase)));
            vel = vel.add(dir.scale(CURIOSITY_SPEED));
            if (hunger > 0f) {
                double approach = dist > ringMin ? APPROACH_SPEED * 3.0 * hunger : -APPROACH_SPEED * 0.6;
                vel = vel.add(dir.scale(approach));
            }
            if (flinch > 0.05f) {
                vel = vel.add(dir.scale(FLINCH_SPEED * flinch));
            }
            vel = vel.add(0, Math.sin(w.phase * 1.4) * 0.010, 0);
            w.vel = w.vel.scale(0.82).add(vel.scale(0.18));
            Vec3 next = w.pos.add(w.vel);
            if (isOpenWater(mc, next)) {
                w.pos = next;
            } else {
                w.vel = w.vel.scale(-0.4);
            }
            emit(mc, w, ticks, intensity);
        }

        if (intensity > 0.38f && ticks % 3 == 0) {
            emitSpore(mc, player, intensity);
        }

        if (flinch > 0.6f && ticks - lastBurst > 12 && !WISPS.isEmpty()) {
            lastBurst = ticks;
            Wisp burst = WISPS.get(RANDOM.nextInt(WISPS.size()));
            Vec3 lunge = player.subtract(burst.pos).normalize().scale(0.10);
            for (int i = 0; i < 8; i++) {
                mc.level.addParticle(CosmicParticleTypes.MURK.get(),
                        burst.pos.x + (RANDOM.nextDouble() - 0.5) * burst.baseSize,
                        burst.pos.y + (RANDOM.nextDouble() - 0.5) * burst.baseSize,
                        burst.pos.z + (RANDOM.nextDouble() - 0.5) * burst.baseSize,
                        lunge.x + (RANDOM.nextDouble() - 0.5) * 0.04,
                        lunge.y + (RANDOM.nextDouble() - 0.5) * 0.04,
                        lunge.z + (RANDOM.nextDouble() - 0.5) * 0.04);
            }
        }
    }

    private static void emit(Minecraft mc, Wisp w, long ticks, float intensity) {
        long salt = ticks + (long) (w.phase * 100);
        if (salt % 2 != 0) return;
        if (w.age < 50 && RANDOM.nextFloat() > w.age / 50f) return;

        for (int i = 0; i < 3; i++) {
            double spread = i == 2 ? 2.6 : 1.6;
            double ox = (RANDOM.nextDouble() - 0.5) * w.baseSize * spread;
            double oy = (RANDOM.nextDouble() - 0.5) * w.baseSize * spread * 0.65;
            double oz = (RANDOM.nextDouble() - 0.5) * w.baseSize * spread;
            double offR = Math.sqrt(ox * ox + oy * oy + oz * oz) / (w.baseSize * 1.3);
            var species = offR > 0.55 || RANDOM.nextFloat() < 0.12 ? CosmicParticleTypes.MURK_PALE.get() :
                    CosmicParticleTypes.MURK.get();
            mc.level.addParticle(species,
                    w.pos.x + ox, w.pos.y + oy, w.pos.z + oz,
                    -oz * 0.010 + w.vel.x * 0.35,
                    w.vel.y * 0.35 + ox * 0.002,
                    ox * 0.010 + w.vel.z * 0.35);
        }
        if (salt % 8 == 0) {
            mc.level.addParticle(ParticleTypes.ASH,
                    w.pos.x + (RANDOM.nextDouble() - 0.5) * w.baseSize * 3.0,
                    w.pos.y + (RANDOM.nextDouble() - 0.5) * w.baseSize * 2.0,
                    w.pos.z + (RANDOM.nextDouble() - 0.5) * w.baseSize * 3.0,
                    0, 0, 0);
        }
        if (salt % 14 == 0) {
            mc.level.addParticle(ParticleTypes.BUBBLE,
                    w.pos.x + (RANDOM.nextDouble() - 0.5) * w.baseSize,
                    w.pos.y + w.baseSize * 0.4,
                    w.pos.z + (RANDOM.nextDouble() - 0.5) * w.baseSize,
                    0, 0.04, 0);
        }
        if (intensity > 0.45f && salt % 6 == 0 && mc.player != null) {
            emitTendril(mc, w);
        }
    }

    private static void emitTendril(Minecraft mc, Wisp w) {
        Vec3 toPlayer = mc.player.getEyePosition().subtract(w.pos);
        double len = toPlayer.length();
        if (len < 0.5) return;
        Vec3 dir = toPlayer.scale(1.0 / len);
        Vec3 perp = new Vec3(-dir.z, 0, dir.x);
        double bend = (RANDOM.nextDouble() - 0.5) * 1.2;
        for (int i = 1; i <= 5; i++) {
            double t = i * 0.7;
            double sway = Math.sin(i * 0.9 + w.phase) * bend;
            Vec3 p = w.pos.add(dir.scale(t)).add(perp.scale(sway)).add(0, Math.sin(i + w.phase) * 0.25, 0);
            mc.level.addParticle(CosmicParticleTypes.MURK_MOTE.get(),
                    p.x, p.y, p.z, dir.x * 0.035, dir.y * 0.02, dir.z * 0.035);
        }
    }

    private static void emitSpore(Minecraft mc, Vec3 player, float intensity) {
        double angle = RANDOM.nextDouble() * Math.PI * 2;
        double ring = 6 + RANDOM.nextDouble() * 9;
        Vec3 pos = player.add(Math.cos(angle) * ring, (RANDOM.nextDouble() - 0.5) * 5, Math.sin(angle) * ring);
        if (!isOpenWater(mc, pos)) return;
        Vec3 drift = player.subtract(pos).normalize().scale(0.035 + 0.035 * intensity);
        mc.level.addParticle(CosmicParticleTypes.MURK_MOTE.get(),
                pos.x, pos.y, pos.z, drift.x, drift.y, drift.z);
    }

    private static void spawnShyWisp(Minecraft mc, Vec3 player, Vec3 look) {
        double lookYaw = Math.atan2(look.z, look.x);
        double offset = (1.22 + RANDOM.nextDouble() * 0.52) * (RANDOM.nextBoolean() ? 1 : -1);
        double angle = lookYaw + offset;
        double dist = 12 + RANDOM.nextDouble() * 8;
        Vec3 pos = player.add(Math.cos(angle) * dist, (RANDOM.nextDouble() - 0.5) * 4, Math.sin(angle) * dist);
        if (!isOpenWater(mc, pos)) return;
        Wisp w = new Wisp(pos, 1.4f + RANDOM.nextFloat() * 1.2f, RANDOM.nextFloat() * 6.28f,
                RANDOM.nextBoolean() ? 1f : -1f);
        w.shy = true;
        w.age = 50;
        WISPS.add(w);
    }

    private static void spawnWisp(Vec3 player, float ringMin, float ringMax) {
        float ring = Mth.lerp(RANDOM.nextFloat(), ringMin, ringMax);
        double angle = RANDOM.nextDouble() * Math.PI * 2;
        double dy = (RANDOM.nextDouble() - 0.5) * ring * 0.5;
        Vec3 pos = player.add(Math.cos(angle) * ring, dy, Math.sin(angle) * ring);
        Minecraft mc = Minecraft.getInstance();
        if (!isOpenWater(mc, pos)) return;
        WISPS.add(new Wisp(pos, 1.6f + RANDOM.nextFloat() * 2.2f, RANDOM.nextFloat() * 6.28f,
                RANDOM.nextBoolean() ? 1f : -1f));
    }

    private static float arrayLerp(int[] arr, float idx) {
        int i = Mth.clamp(Mth.floor(idx), 0, arr.length - 1);
        int j = Math.min(i + 1, arr.length - 1);
        return Mth.lerp(idx - i, arr[i], arr[j]);
    }

    private static float arrayLerp(float[] arr, float idx) {
        int i = Mth.clamp(Mth.floor(idx), 0, arr.length - 1);
        int j = Math.min(i + 1, arr.length - 1);
        return Mth.lerp(idx - i, arr[i], arr[j]);
    }

    private static boolean isOpenWater(Minecraft mc, Vec3 pos) {
        BlockPos bp = BlockPos.containing(pos);
        return mc.level != null && mc.level.getFluidState(bp).is(FluidTags.WATER);
    }

    public static Vec3 randomWispPos() {
        if (WISPS.isEmpty()) return null;
        return WISPS.get(RANDOM.nextInt(WISPS.size())).pos;
    }
}
