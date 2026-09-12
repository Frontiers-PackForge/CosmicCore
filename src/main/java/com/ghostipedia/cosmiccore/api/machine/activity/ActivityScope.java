package com.ghostipedia.cosmiccore.api.machine.activity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ActivityScope implements AutoCloseable {

    private static final ThreadLocal<ActivityScope> CURRENT = new ThreadLocal<>();
    private final ActivityScope previous;
    private final MachineActivity activity;
    private final int lane;
    private int mutationDepth;
    private long mutations;

    public ActivityScope(MachineActivity activity, int lane) {
        previous = CURRENT.get();
        this.activity = previous != null && previous.activity == null ? null : activity;
        this.lane = lane;
        CURRENT.set(this);
    }

    public static ActivityScope current() {
        return CURRENT.get();
    }

    public static boolean active() {
        ActivityScope scope = CURRENT.get();
        return scope != null && scope.activity != null;
    }

    public static ActivityScope suspend() {
        return new ActivityScope(null, -1);
    }

    public MachineActivity activity() {
        return activity;
    }

    public int lane() {
        return lane;
    }

    public long mutations() {
        return mutations;
    }

    public boolean enterMutation() {
        return mutationDepth++ == 0;
    }

    public void leaveMutation() {
        mutationDepth--;
    }

    public static void item(ItemStack stack, long amount, boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope == null || scope.activity == null || stack.isEmpty() || amount <= 0) return;
        scope.activity.recordItem(scope.lane, stack, amount, input);
        scope.mutations++;
    }

    public static void fluid(FluidStack stack, long amount, boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope == null || scope.activity == null || stack.isEmpty() || amount <= 0) return;
        scope.activity.recordFluid(scope.lane, stack, amount, input);
        scope.mutations++;
    }

    public static void value(String kind, String id, long amount, boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope == null || scope.activity == null || amount <= 0) return;
        scope.activity.recordValue(scope.lane, kind, id, amount, input);
        scope.mutations++;
    }

    public static void partial(boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope != null && scope.activity != null) scope.activity.markPartial(input);
    }

    public static void partialIfUnrecorded(boolean input, long mutationsBefore, boolean handled) {
        ActivityScope scope = CURRENT.get();
        if (handled && scope != null && scope.activity != null && scope.mutations == mutationsBefore)
            scope.activity.markPartial(input);
    }

    @Override
    public void close() {
        if (previous != null && previous.activity == activity) previous.mutations += mutations;
        if (previous == null) CURRENT.remove();
        else CURRENT.set(previous);
    }
}
