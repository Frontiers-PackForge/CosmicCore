package com.ghostipedia.cosmiccore.common.deployment;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;

import net.minecraft.server.level.ServerLevel;

public final class GtmMultiblockFormationValidator implements LeylineDeploymentExecutor.PostPlacementValidator {

    @Override
    public boolean validate(ServerLevel level, LeylineDeploymentPlan plan) {
        MetaMachine machine = MetaMachine.getMachine(level, plan.controllerPos());
        if (!(machine instanceof MultiblockControllerMachine controller)) return false;
        controller.getDefaultPatternState().getCache().clear();
        controller.getDefaultPatternState().setShouldUpdate(true);
        controller.getDefaultPatternState().setState(PatternState.CheckState.UNINITIALIZED);
        controller.checkAndFormStructure();
        return controller.isFormed();
    }
}
