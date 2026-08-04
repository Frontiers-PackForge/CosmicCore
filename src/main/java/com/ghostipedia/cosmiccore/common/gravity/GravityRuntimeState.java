package com.ghostipedia.cosmiccore.common.gravity;

import com.ghostipedia.cosmiccore.api.gravity.GravityFrame;
import com.ghostipedia.cosmiccore.api.gravity.GravityMode;

import net.minecraft.core.Direction;

import java.util.Objects;

public final class GravityRuntimeState {

    private double transitionWeight;
    private GravityFrame targetFrame = GravityFrame.NORMAL;
    private int enterStableTicks;
    private int exitStableTicks;
    private boolean policyManaged;
    private long appliedRevision = Long.MIN_VALUE;
    private GravityMode appliedMode = GravityMode.NORMAL;
    private Direction appliedDown = Direction.DOWN;
    private int appliedTransitionTicks;
    private boolean directedActive;
    private long suppressedDirectedRevision = Long.MIN_VALUE;

    public double transitionWeight() {
        return transitionWeight;
    }

    public void setTransitionWeight(double transitionWeight) {
        if (Double.isNaN(transitionWeight)) {
            this.transitionWeight = 0.0;
            return;
        }
        this.transitionWeight = Math.clamp(transitionWeight, 0.0, 1.0);
    }

    public GravityFrame targetFrame() {
        return targetFrame;
    }

    public void setTargetFrame(GravityFrame targetFrame) {
        this.targetFrame = Objects.requireNonNull(targetFrame);
    }

    public int enterStableTicks() {
        return enterStableTicks;
    }

    public int incrementEnterStableTicks() {
        if (enterStableTicks < Integer.MAX_VALUE) enterStableTicks++;
        return enterStableTicks;
    }

    public void resetEnterStableTicks() {
        enterStableTicks = 0;
    }

    public int exitStableTicks() {
        return exitStableTicks;
    }

    public int incrementExitStableTicks() {
        if (exitStableTicks < Integer.MAX_VALUE) exitStableTicks++;
        return exitStableTicks;
    }

    public void resetExitStableTicks() {
        exitStableTicks = 0;
    }

    public boolean policyManaged() {
        return policyManaged;
    }

    public void setPolicyManaged(boolean policyManaged) {
        this.policyManaged = policyManaged;
    }

    public boolean directedActive() {
        return directedActive;
    }

    public GravityMode appliedMode() {
        return appliedMode;
    }

    public Direction appliedDown() {
        return appliedDown;
    }

    public int appliedTransitionTicks() {
        return appliedTransitionTicks;
    }

    public boolean isDirectedSuppressed(long revision) {
        return suppressedDirectedRevision == revision;
    }

    public void suppressDirected(long revision) {
        suppressedDirectedRevision = revision;
    }

    public boolean frameBasisChanged(GravityFrame frame) {
        return appliedMode != frame.mode() || appliedDown != frame.down();
    }

    public boolean needsDimensionRefresh(GravityFrame frame, boolean active) {
        return appliedRevision != frame.revision() || frameBasisChanged(frame) || directedActive != active;
    }

    public void markDimensionsApplied(GravityFrame frame, boolean active) {
        appliedRevision = frame.revision();
        appliedMode = frame.mode();
        appliedDown = frame.down();
        appliedTransitionTicks = frame.transitionTicks();
        directedActive = active;
        if (frame.mode() != GravityMode.DIRECTED || suppressedDirectedRevision != frame.revision()) {
            suppressedDirectedRevision = Long.MIN_VALUE;
        }
    }

    public void reset() {
        transitionWeight = 0.0;
        targetFrame = GravityFrame.NORMAL;
        enterStableTicks = 0;
        exitStableTicks = 0;
        policyManaged = false;
        appliedRevision = Long.MIN_VALUE;
        appliedMode = GravityMode.NORMAL;
        appliedDown = Direction.DOWN;
        appliedTransitionTicks = 0;
        directedActive = false;
        suppressedDirectedRevision = Long.MIN_VALUE;
    }
}
