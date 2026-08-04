package com.ghostipedia.cosmiccore.common.firmament;

import com.ghostipedia.cosmiccore.common.data.worldgen.firmament.FirmamentMiddleBandLayout;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class FirmamentTraversalForces {

    private static final double MAX_STORM_DELTA = 0.04;
    private static final double MAX_STORM_ACCELERATED_SPEED = 1.25;

    private FirmamentTraversalForces() {}

    public static boolean apply(Player player, double weight, long phaseTick, boolean residualGravity) {
        double clampedWeight = Math.clamp(weight, 0.0, 1.0);
        Vec3 inherited = player.getDeltaMovement();
        Vec3 residual = residualGravity ? residualGravity(player, clampedWeight, inherited) : Vec3.ZERO;
        Vec3 base = inherited.add(residual);
        Vec3 storm = limitedStormDelta(player, clampedWeight, phaseTick, base);
        Vec3 result = base.add(storm);
        if (result.equals(inherited)) return false;
        player.setDeltaMovement(result);
        return true;
    }

    private static Vec3 residualGravity(Player player, double weight, Vec3 inherited) {
        if (player.hasEffect(MobEffects.LEVITATION)) return Vec3.ZERO;
        double gravity = player.getAttributeValue(Attributes.GRAVITY);
        if (inherited.y <= 0.0 && player.hasEffect(MobEffects.SLOW_FALLING)) {
            gravity = Math.min(gravity, 0.01);
        }
        return new Vec3(0.0, -gravity * (1.0 - weight), 0.0);
    }

    private static Vec3 limitedStormDelta(Player player, double weight, long phaseTick, Vec3 inherited) {
        if (weight <= 0.0) return Vec3.ZERO;
        FirmamentMiddleBandLayout.WindCorridor wind = FirmamentMiddleBandLayout.sampleWind(
                player.getX(), player.getZ());
        double time = (phaseTick + player.getId() * 17L) / 20.0;
        double pulse = 0.78 + 0.22 * Math.sin(time * 0.91 + player.getX() * 0.013 - player.getZ() * 0.009);
        double along = (0.12 + wind.strength() * 0.30) * pulse;
        double cross = 0.05 * Math.sin(time * 1.37 + player.getX() * 0.021 + player.getZ() * 0.017);
        double lift = 0.06 * Math.sin(time * 1.11 + player.getX() * 0.016 - player.getZ() * 0.019);
        double normalX = -wind.directionZ();
        double normalZ = wind.directionX();
        Vec3 flow = new Vec3(
                wind.directionX() * along + normalX * cross,
                lift,
                wind.directionZ() * along + normalZ * cross);
        double coupling = weight * (0.02 + wind.strength() * 0.02);
        Vec3 delta = boundDelta(flow.subtract(inherited).scale(coupling));
        return limitAddedSpeed(inherited, delta);
    }

    private static Vec3 boundDelta(Vec3 delta) {
        double lengthSqr = delta.lengthSqr();
        double maximumSqr = MAX_STORM_DELTA * MAX_STORM_DELTA;
        if (lengthSqr <= maximumSqr) return delta;
        return delta.scale(MAX_STORM_DELTA / Math.sqrt(lengthSqr));
    }

    private static Vec3 limitAddedSpeed(Vec3 inherited, Vec3 delta) {
        double deltaSqr = delta.lengthSqr();
        if (deltaSqr == 0.0) return delta;
        Vec3 candidate = inherited.add(delta);
        double inheritedSqr = inherited.lengthSqr();
        double candidateSqr = candidate.lengthSqr();
        double maximumSqr = MAX_STORM_ACCELERATED_SPEED * MAX_STORM_ACCELERATED_SPEED;
        if (inheritedSqr >= maximumSqr) {
            if (candidateSqr <= inheritedSqr) return delta;
            double dot = inherited.dot(delta);
            if (dot >= 0.0) return Vec3.ZERO;
            double scale = Math.clamp(-2.0 * dot / deltaSqr, 0.0, 1.0);
            return delta.scale(scale);
        }
        if (candidateSqr <= maximumSqr) return delta;
        double dot = inherited.dot(delta);
        double discriminant = dot * dot + deltaSqr * (maximumSqr - inheritedSqr);
        if (discriminant <= 0.0) return Vec3.ZERO;
        double scale = Math.clamp((-dot + Math.sqrt(discriminant)) / deltaSqr, 0.0, 1.0);
        return delta.scale(scale);
    }
}
