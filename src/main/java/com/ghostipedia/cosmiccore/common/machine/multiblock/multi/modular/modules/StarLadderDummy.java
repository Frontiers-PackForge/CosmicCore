package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.modules;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ModularizedWorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public class StarLadderDummy extends ModularizedWorkableElectricMultiblockMachine {

    public StarLadderDummy(BlockEntityCreationInfo holder, int tier, int moduleTier, int minModuleTier) {
        super(holder, tier, moduleTier, minModuleTier);
    }

    @Override
    public void sendWorkingDisabled() {
        this.recipeLogic.setWorkingEnabled(false);
    }

    @Override
    public void sendWorkingEnabled() {
        this.recipeLogic.setWorkingEnabled(true);
    }

    @Override
    public String getNameForDisplays() {
        return this.getDefinition().getId().toLanguageKey("block", "display_count");
    }
}
