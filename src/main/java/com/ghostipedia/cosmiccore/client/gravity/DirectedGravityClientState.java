package com.ghostipedia.cosmiccore.client.gravity;

import com.ghostipedia.cosmiccore.api.gravity.GravityMode;
import com.ghostipedia.cosmiccore.api.gravity.GravityTransforms;
import com.ghostipedia.cosmiccore.common.gravity.GravityManager;
import com.ghostipedia.cosmiccore.common.gravity.GravityRuntimeState;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

public final class DirectedGravityClientState {

    private static final float ROLL_EPSILON = 1.0E-5F;
    private static final float VECTOR_EPSILON_SQUARED = 1.0E-8F;
    private static final Map<Player, CameraState> CAMERA_STATES = new WeakHashMap<>();
    private static final Map<Player, ModelState> MODEL_STATES = new WeakHashMap<>();

    private DirectedGravityClientState() {}

    public static @Nullable Quaternionf cameraRotation(Player player, Quaternionfc localRotation, float partialTick) {
        VisualTarget target = target(player);
        double now = now(player, partialTick);
        Quaternionf destination = target.directed() ?
                new Quaternionf(CardinalGravityRotation.forDown(target.down())).mul(localRotation) :
                new Quaternionf(localRotation);
        CameraState state = CAMERA_STATES.get(player);
        if (state == null) {
            state = new CameraState(target, destination, now);
            CAMERA_STATES.put(player, state);
        } else if (state.targetChanged(target)) {
            state.retarget(target, destination, now);
        } else {
            state.observe(target);
        }

        float roll = state.sampleRoll(now);
        Quaternionf rendered = new Quaternionf(destination);
        if (Math.abs(roll) > ROLL_EPSILON) rendered.rotateZ(roll);
        state.setLastRendered(rendered);
        return target.directed() || Math.abs(roll) > ROLL_EPSILON ? rendered : null;
    }

    public static @Nullable Quaternionf modelRotation(Player player, float partialTick) {
        VisualTarget target = target(player);
        double now = now(player, partialTick);
        Quaternionf destination = target.directed() ?
                CardinalGravityRotation.forDown(target.down()) : new Quaternionf();
        ModelState state = MODEL_STATES.get(player);
        if (state == null) {
            state = new ModelState(target, destination, now);
            MODEL_STATES.put(player, state);
        } else if (state.targetChanged(target)) {
            state.retarget(target, destination, now);
        } else {
            state.observe(target);
        }

        Quaternionf rendered = state.sample(now);
        return target.directed() || !state.transitionComplete(now) ? rendered : null;
    }

    public static @Nullable Vec3 eyeOffset(Player player, float eyeHeight) {
        VisualTarget target = target(player);
        return target.directed() ?
                GravityTransforms.localToWorld(new Vec3(0.0, eyeHeight, 0.0), target.down()) : null;
    }

    private static VisualTarget target(Player player) {
        GravityRuntimeState runtime = GravityManager.runtime(player);
        boolean directed = runtime.directedActive() && runtime.appliedMode() == GravityMode.DIRECTED;
        return new VisualTarget(
                directed,
                directed ? runtime.appliedDown() : Direction.DOWN,
                directed ? runtime.appliedTransitionTicks() : 0);
    }

    private static double now(Player player, float partialTick) {
        return player.tickCount + Math.clamp((double) partialTick, 0.0, 1.0);
    }

    private static float signedHorizonRoll(Quaternionfc destination, Quaternionfc previous) {
        Vector3f axis = destination.transform(0.0F, 0.0F, 1.0F, new Vector3f()).normalize();
        Vector3f destinationUp = destination.transform(0.0F, 1.0F, 0.0F, new Vector3f());
        Vector3f previousUp = previous.transform(0.0F, 1.0F, 0.0F, new Vector3f());
        destinationUp.sub(new Vector3f(axis).mul(destinationUp.dot(axis)));
        previousUp.sub(new Vector3f(axis).mul(previousUp.dot(axis)));
        if (destinationUp.lengthSquared() <= VECTOR_EPSILON_SQUARED ||
                previousUp.lengthSquared() <= VECTOR_EPSILON_SQUARED) {
            return 0.0F;
        }
        destinationUp.normalize();
        previousUp.normalize();
        Vector3f cross = destinationUp.cross(previousUp, new Vector3f());
        return (float) Math.atan2(axis.dot(cross), destinationUp.dot(previousUp));
    }

    private static float easedProgress(double now, double start, int duration) {
        if (duration <= 0) return 1.0F;
        double progress = Math.clamp((now - start) / duration, 0.0, 1.0);
        return (float) (progress * progress * progress * (progress * (progress * 6.0 - 15.0) + 10.0));
    }

    private record VisualTarget(boolean directed, Direction down, int duration) {}

    private static final class CameraState {

        private boolean directed;
        private Direction down;
        private int directedDuration;
        private float initialRoll;
        private double start;
        private int duration;
        private Quaternionf lastRendered;

        private CameraState(VisualTarget target, Quaternionfc destination, double now) {
            directed = target.directed();
            down = target.down();
            directedDuration = target.directed() ? target.duration() : 0;
            start = now;
            lastRendered = new Quaternionf(destination);
        }

        private boolean targetChanged(VisualTarget target) {
            return directed != target.directed() || down != target.down();
        }

        private void retarget(VisualTarget target, Quaternionfc destination, double now) {
            int transitionDuration = target.directed() ? target.duration() : directedDuration;
            initialRoll = signedHorizonRoll(destination, lastRendered);
            start = now;
            duration = transitionDuration;
            directed = target.directed();
            down = target.down();
            if (target.directed()) directedDuration = target.duration();
        }

        private void observe(VisualTarget target) {
            if (target.directed()) directedDuration = target.duration();
        }

        private float sampleRoll(double now) {
            return initialRoll * (1.0F - easedProgress(now, start, duration));
        }

        private void setLastRendered(Quaternionfc rotation) {
            lastRendered.set(rotation);
        }
    }

    private static final class ModelState {

        private boolean directed;
        private Direction down;
        private int directedDuration;
        private Quaternionf from;
        private Quaternionf destination;
        private double start;
        private int duration;

        private ModelState(VisualTarget target, Quaternionfc destination, double now) {
            directed = target.directed();
            down = target.down();
            directedDuration = target.directed() ? target.duration() : 0;
            from = new Quaternionf();
            this.destination = new Quaternionf(destination);
            start = now;
            duration = target.directed() ? target.duration() : 0;
        }

        private boolean targetChanged(VisualTarget target) {
            return directed != target.directed() || down != target.down();
        }

        private void retarget(VisualTarget target, Quaternionfc destination, double now) {
            from = sample(now);
            this.destination = new Quaternionf(destination);
            start = now;
            duration = target.directed() ? target.duration() : directedDuration;
            directed = target.directed();
            down = target.down();
            if (target.directed()) directedDuration = target.duration();
        }

        private void observe(VisualTarget target) {
            if (target.directed()) directedDuration = target.duration();
        }

        private Quaternionf sample(double now) {
            return new Quaternionf(from).slerp(destination, easedProgress(now, start, duration));
        }

        private boolean transitionComplete(double now) {
            return duration <= 0 || now - start >= duration;
        }
    }
}
