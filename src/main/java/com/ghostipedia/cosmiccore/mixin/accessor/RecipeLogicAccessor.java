package com.ghostipedia.cosmiccore.mixin.accessor;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeLogic.class)
public interface RecipeLogicAccessor {

    @Accessor("progress")
    int getProgress();

    @Accessor("progress")
    void setProgress(int progress);
}
