package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class HephaestusCauldronRecipeLogic extends RecipeLogic {

    @Override
    public void serverTick() {}

    public void setGenerating(boolean generating) {
        setStatus(generating ? Status.WORKING : Status.IDLE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateSound() {
        var sound = GTSoundEntries.FIRE;
        if (isWorking() && getRLMachine().shouldWorkingPlaySound()) {
            if (workingSound instanceof AutoReleasedSound soundEntry) {
                if (soundEntry.soundEntry == sound && !soundEntry.isStopped()) return;
                soundEntry.release();
                workingSound = null;
            }
            workingSound = sound.playAutoReleasedSound(
                    () -> getRLMachine().shouldWorkingPlaySound() && isWorking() && !getMachine().isRemoved() &&
                            getMachine().getLevel().isLoaded(getMachine().getBlockPos()) &&
                            MetaMachine.getMachine(getMachine().getLevel(), getMachine().getBlockPos()) == getMachine(),
                    getMachine().getBlockPos(), true, 0, 1, 1);
        } else if (workingSound instanceof AutoReleasedSound soundEntry) {
            soundEntry.release();
            workingSound = null;
        }
    }
}
