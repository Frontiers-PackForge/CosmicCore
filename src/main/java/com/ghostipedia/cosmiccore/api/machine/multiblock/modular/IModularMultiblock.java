package com.ghostipedia.cosmiccore.api.machine.multiblock.modular;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import net.minecraft.core.BlockPos;

import java.util.List;

public interface IModularMultiblock {

    void addModule(IMultiblockModule module);

    void removeModule(IMultiblockModule module);

    void onModuleUpdate();

    void notifyModules();

    int getModuleCount();

    boolean isWorking();

    BlockPos getPos();

    List<IRecipeHandler<?>> getCapabilities(IO io, RecipeCapability<?> cap);
}
