package com.ghostipedia.cosmiccore.api.machine.activity;

import com.ghostipedia.cosmiccore.common.production.ProductionStatisticsService;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ActivityScope implements AutoCloseable {

    private static final ThreadLocal<ActivityScope> CURRENT = new ThreadLocal<>();
    private static final GlobalRecorder DEFAULT_GLOBAL_RECORDER = new GlobalRecorder() {

        @Override
        public void item(MetaMachine machine, ItemStack stack, long amount, boolean input) {
            ProductionStatisticsService.item(machine, stack, amount, input);
        }

        @Override
        public void fluid(MetaMachine machine, FluidStack stack, long amount, boolean input) {
            ProductionStatisticsService.fluid(machine, stack, amount, input);
        }

        @Override
        public void ember(MetaMachine machine, double amount, boolean input) {
            ProductionStatisticsService.ember(machine, amount, input);
        }

        @Override
        public void soul(MetaMachine machine, com.ghostipedia.cosmiccore.api.capability.souls.SoulType type,
                         long amount, boolean input) {
            ProductionStatisticsService.soul(machine, type, amount, input);
        }

        @Override
        public void energy(MetaMachine machine, long amount, boolean input) {
            ProductionStatisticsService.energy(machine, amount, input);
        }

        @Override
        public void partial(MetaMachine machine, String kind, boolean input) {
            ProductionStatisticsService.partial(machine, kind, input);
        }
    };
    private static final ThreadLocal<GlobalRecorder> TEST_GLOBAL_RECORDER = new ThreadLocal<>();
    private final ActivityScope previous;
    private final MachineActivity activity;
    private final int lane;
    private final MetaMachine machine;
    private int mutationDepth;
    private long mutations;

    public ActivityScope(MachineActivity activity, int lane) {
        this(activity, lane, null);
    }

    public ActivityScope(MachineActivity activity, int lane, MetaMachine machine) {
        previous = CURRENT.get();
        this.activity = previous != null && previous.activity == null ? null : activity;
        this.lane = lane;
        this.machine = machine;
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
        globalRecorder().item(scope.machine, stack, amount, input);
        scope.activity.recordItem(scope.lane, stack, amount, input);
        scope.mutations++;
    }

    public static void fluid(FluidStack stack, long amount, boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope == null || scope.activity == null || stack.isEmpty() || amount <= 0) return;
        globalRecorder().fluid(scope.machine, stack, amount, input);
        scope.activity.recordFluid(scope.lane, stack, amount, input);
        scope.mutations++;
    }

    public static void value(String kind, String id, long amount, boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope == null || scope.activity == null || amount <= 0) return;
        scope.activity.recordValue(scope.lane, kind, id, amount, input);
        scope.mutations++;
    }

    public static void ember(double amount, boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope != null && scope.activity != null && Double.isFinite(amount) && amount > 0)
            globalRecorder().ember(scope.machine, amount, input);
        if (scope != null && scope.activity != null && Double.isFinite(amount) && amount > 0) scope.mutations++;
    }

    public static void soul(com.ghostipedia.cosmiccore.api.capability.souls.SoulType type, long amount, boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope != null && scope.activity != null && amount > 0)
            globalRecorder().soul(scope.machine, type, amount, input);
        if (scope != null && scope.activity != null && amount > 0) scope.mutations++;
    }

    public static void energy(long amount, boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope == null || scope.activity == null || amount <= 0) return;
        globalRecorder().energy(scope.machine, amount, input);
        scope.activity.recordValue(scope.lane, "energy", "gtceu:eu", amount, input);
        scope.mutations++;
    }

    static AutoCloseable globalRecorderForTest(GlobalRecorder recorder) {
        GlobalRecorder previous = TEST_GLOBAL_RECORDER.get();
        TEST_GLOBAL_RECORDER.set(recorder);
        return () -> {
            if (previous == null) TEST_GLOBAL_RECORDER.remove();
            else TEST_GLOBAL_RECORDER.set(previous);
        };
    }

    private static GlobalRecorder globalRecorder() {
        GlobalRecorder recorder = TEST_GLOBAL_RECORDER.get();
        return recorder == null ? DEFAULT_GLOBAL_RECORDER : recorder;
    }

    interface GlobalRecorder {

        void item(MetaMachine machine, ItemStack stack, long amount, boolean input);

        void fluid(MetaMachine machine, FluidStack stack, long amount, boolean input);

        void ember(MetaMachine machine, double amount, boolean input);

        void soul(MetaMachine machine, com.ghostipedia.cosmiccore.api.capability.souls.SoulType type, long amount,
                  boolean input);

        void energy(MetaMachine machine, long amount, boolean input);

        void partial(MetaMachine machine, String kind, boolean input);
    }

    public static void partial(boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope != null && scope.activity != null) scope.activity.markPartial(input);
    }

    public static void partial(String kind, boolean input) {
        ActivityScope scope = CURRENT.get();
        if (scope == null || scope.activity == null) return;
        scope.activity.markPartial(input);
        globalRecorder().partial(scope.machine, kind, input);
    }

    public static void partialIfUnrecorded(boolean input, long mutationsBefore, boolean handled) {
        ActivityScope scope = CURRENT.get();
        if (handled && scope != null && scope.activity != null && scope.mutations == mutationsBefore)
            scope.activity.markPartial(input);
    }

    public static void partialIfUnrecorded(String kind, boolean input, long mutationsBefore, boolean handled) {
        ActivityScope scope = CURRENT.get();
        if (handled && scope != null && scope.activity != null && scope.mutations == mutationsBefore)
            partial(kind, input);
    }

    @Override
    public void close() {
        if (previous != null && previous.activity == activity) previous.mutations += mutations;
        if (previous == null) CURRENT.remove();
        else CURRENT.set(previous);
    }
}
