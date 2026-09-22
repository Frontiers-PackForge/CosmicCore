package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

public class IndependentBloomwyrmRecipeLogic extends BloomwyrmRecipeLogic {

    @Override
    public void serverTick() {
        BloomwyrmUnitMachine machine = getUnit();
        if (machine.hasAllocation() || isActive()) {
            super.serverTick();
            return;
        }
        if (machine.isAvailableForAllocation() && machine.getOffsetTimer() % 5 == 0) {
            clearFailureReason();
            BloomwyrmHeartMachine heart = machine.getHeart();
            if (heart == null) {
                machine.denyAllocation(BloomwyrmAllocationConstraint.NO_HEART);
            } else {
                var request = createRequest();
                if (request.isPresent()) {
                    heart.tryAllocateIndependent(machine, request.get());
                } else {
                    machine.denyAllocation(BloomwyrmAllocationConstraint.NO_RECIPE);
                }
            }
        }
        if (machine.hasAllocation() || isActive()) {
            super.serverTick();
        }
    }

    @Override
    protected BloomwyrmUnitMachine getUnit() {
        return (BloomwyrmUnitMachine) super.getMachine();
    }
}
