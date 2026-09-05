package com.ghostipedia.cosmiccore.client.renderer.deployment;

import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentAnimation;
import com.ghostipedia.cosmiccore.mixin.client.deployment.LeylineParticleAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

final class LeylineDeploymentEffects implements AutoCloseable {

    private static final int[] DUST_WAVE_TICKS = { 0, 6, 14 };
    static final int IMPACT_TAIL_TICKS = 74;
    private final LeylineDeploymentAnimation animation;
    private final List<LeylineDeploymentSound> sounds = new ArrayList<>();
    private LeylineDeploymentSound descentSound;
    private int lastParticleTick = Integer.MIN_VALUE;
    private int nextWave;
    private boolean impacted;

    LeylineDeploymentEffects(LeylineDeploymentAnimation animation, double age) {
        this.animation = animation;
        if (age < LeylineDeploymentAnimation.OPEN_TICKS) {
            sounds.add(LeylineDeploymentSound.play(SoundEvents.BEACON_ACTIVATE, opening(), animation.bounds(),
                    0.75f, 0.8f, false));
        }
    }

    void update(double age, double impactAge) {
        if (age < 0) return;
        if (!impacted && age >= LeylineDeploymentAnimation.OPEN_TICKS && descentSound == null) {
            descentSound = LeylineDeploymentSound.play(SoundEvents.BEACON_AMBIENT, opening(), animation.bounds(),
                    0.5f, 0.65f, true);
            sounds.add(descentSound);
        }
        int tick = (int) Math.floor(age);
        if (lastParticleTick == tick) return;
        lastParticleTick = tick;
        if (impacted) {
            while (nextWave < DUST_WAVE_TICKS.length && age - impactAge >= DUST_WAVE_TICKS[nextWave]) {
                if (inRange()) dustWave(nextWave);
                nextWave++;
            }
        } else if (age >= LeylineDeploymentAnimation.OPEN_TICKS &&
                age < LeylineDeploymentAnimation.IMPACT_TICK && inRange()) {
                    storm(age);
                }
    }

    void impact() {
        if (impacted) return;
        impacted = true;
        if (descentSound != null) descentSound.release();
        var bounds = animation.bounds();
        var center = bounds.getCenter();
        var base = new Vec3(center.x, bounds.minY, center.z);
        sounds.add(LeylineDeploymentSound.play(SoundEvents.ANVIL_LAND, base, bounds, 0.12f, 0.55f, false));
        sounds.add(LeylineDeploymentSound.play(SoundEvents.GENERIC_EXPLODE.value(), base, bounds, 0.22f, 0.65f, false));
    }

    private Vec3 opening() {
        var center = animation.bounds().getCenter();
        return new Vec3(center.x, animation.openingY(), center.z);
    }

    private boolean inRange() {
        var minecraft = Minecraft.getInstance();
        double range = (minecraft.options.getEffectiveRenderDistance() + 1) * 16.0;
        return animation.bounds().inflate(range).contains(minecraft.gameRenderer.getMainCamera().getPosition());
    }

    private int count(int desired) {
        var status = Minecraft.getInstance().options.particles().get();
        return Math.max(1,
                desired / (status == ParticleStatus.MINIMAL ? 8 : status == ParticleStatus.DECREASED ? 2 : 1));
    }

    private void storm(double age) {
        var minecraft = Minecraft.getInstance();
        var random = minecraft.level.random;
        var bounds = animation.bounds();
        var center = bounds.getCenter();
        double bottom = bounds.minY + animation.verticalOffset(age);
        double top = Math.min(animation.openingY(), bounds.maxY + animation.verticalOffset(age));
        int count = count(Math.clamp((int) (animation.radius() * 3), 8, 24));
        for (int index = 0; index < count; index++) {
            double angle = age * 0.11 + Math.PI * 2 * (index + random.nextDouble()) / count;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            double radius = animation.radius() * (0.8 + random.nextDouble() * 0.2);
            var spark = minecraft.particleEngine.createParticle(ParticleTypes.END_ROD,
                    center.x + dx * radius, animation.openingY() - 0.08, center.z + dz * radius,
                    -dz * 0.13 - dx * 0.025, -0.07, dx * 0.13 - dz * 0.025);
            if (spark != null) {
                spark.setLifetime(12 + random.nextInt(15));
                spark.setColor(0.55f, 0.8f, 1);
            }
            if (index % 2 == 0 && top > bottom) {
                double edge = edge(bounds, dx, dz) + 0.15;
                var mote = minecraft.particleEngine.createParticle(ParticleTypes.CLOUD,
                        center.x + dx * edge, bottom + random.nextDouble() * (top - bottom), center.z + dz * edge,
                        -dz * 0.045, -0.06, dx * 0.045);
                if (mote != null) {
                    mote.setLifetime(12 + random.nextInt(12));
                    mote.setColor(0.65f, 0.72f, 0.78f);
                    mote.scale(0.45f);
                }
            }
        }
    }

    private void dustWave(int wave) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var bounds = animation.bounds();
        var center = bounds.getCenter();
        int count = count(Math.clamp((int) ((bounds.getXsize() + bounds.getZsize()) * 6), 24, 96));
        for (int index = 0; index < count; index++) {
            double angle = Math.PI * 2 * (index + level.random.nextDouble()) / count;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            double edge = edge(bounds, dx, dz) + 0.2 + wave * 0.45;
            double x = center.x + dx * edge;
            double z = center.z + dz * edge;
            double speed = (0.12 + level.random.nextDouble() * 0.16) * (1 - wave * 0.18);
            var smoke = minecraft.particleEngine.createParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x, bounds.minY + 0.1 + level.random.nextDouble() * 0.25, z,
                    dx * speed, 0.012 + level.random.nextDouble() * 0.025, dz * speed);
            if (smoke != null) {
                int lifetime = 20 + level.random.nextInt(41);
                smoke.setLifetime(lifetime);
                ((LeylineParticleAccessor) smoke).cosmiccore$setAlpha(lifetime * 0.015f);
                smoke.setColor(0.64f, 0.6f, 0.54f);
                smoke.scale(0.65f + level.random.nextFloat() * 0.6f);
            }
            if (wave == 0 && index % 2 == 0) {
                var ground = level.getBlockState(BlockPos.containing(x, bounds.minY - 1, z));
                if (!ground.isAir())
                    minecraft.particleEngine.createParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                            x, bounds.minY + 0.15, z, dx * speed, 0.12, dz * speed);
            }
        }
    }

    private static double edge(AABB bounds, double dx, double dz) {
        return Math.min(bounds.getXsize() / 2 / Math.max(Math.abs(dx), 0.001),
                bounds.getZsize() / 2 / Math.max(Math.abs(dz), 0.001));
    }

    @Override
    public void close() {
        sounds.forEach(LeylineDeploymentSound::release);
    }
}
