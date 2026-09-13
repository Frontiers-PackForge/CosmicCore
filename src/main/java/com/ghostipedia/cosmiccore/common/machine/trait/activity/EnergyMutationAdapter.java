package com.ghostipedia.cosmiccore.common.machine.trait.activity;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;

import java.util.function.LongUnaryOperator;

public final class EnergyMutationAdapter {

    private EnergyMutationAdapter() {}

    public static long execute(long requested, LongUnaryOperator operation) {
        ActivityScope scope = ActivityScope.current();
        if (!ActivityScope.active()) return operation.applyAsLong(requested);
        boolean root = scope.enterMutation();
        try {
            long changed = operation.applyAsLong(requested);
            if (root) {
                if (changed == Long.MIN_VALUE) ActivityScope.partial("energy", requested < 0);
                else ActivityScope.energy(Math.abs(changed), changed < 0);
            }
            return changed;
        } finally {
            scope.leaveMutation();
        }
    }
}
