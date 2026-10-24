package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;

import net.minecraft.network.chat.Component;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = GCYMMachines.class, remap = false)
public abstract class GCYMInnateParallelMixin {

    @WrapOperation(method = "<clinit>",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/registry/registrate/builder/MultiblockMachineBuilder;recipeModifiers([Lcom/gregtechceu/gtceu/api/recipe/modifier/RecipeModifier;)Lcom/gregtechceu/gtceu/api/registry/registrate/builder/MachineBuilder;"),
                   require = 26)
    private static MachineBuilder<?, ?, ?> cosmiccore$innateParallels(MultiblockMachineBuilder<?> builder,
                                                                      RecipeModifier[] modifiers,
                                                                      Operation<MachineBuilder<?, ?, ?>> original) {
        var updated = new ArrayList<>(Arrays.asList(modifiers));
        int hatchIndex = updated.indexOf(GTRecipeModifiers.PARALLEL_HATCH);
        if (hatchIndex >= 0) {
            updated.set(hatchIndex, GCYMInnateParallelMixin::cosmiccore$parallel);
        } else {
            updated.addFirst(GCYMInnateParallelMixin::cosmiccore$parallel);
        }
        builder.tooltips(Component.translatable("cosmiccore.machine.gcym.innate_parallel"));
        return original.call(builder, updated.toArray(RecipeModifier[]::new));
    }

    @ModifyArg(method = "*",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/api/multiblock/Predicates;autoAbilities(ZZZ)Lcom/gregtechceu/gtceu/api/multiblock/MultiPredicate;"),
               index = 2,
               require = 26)
    private static boolean cosmiccore$allowParallelHatch(boolean checkParallel) {
        return true;
    }

    @Unique
    private static ModifierFunction cosmiccore$parallel(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MultiblockControllerMachine controller) || !controller.isFormed()) {
            return ModifierFunction.IDENTITY;
        }
        int hatchParallel = controller.getParallelHatch().map(hatch -> hatch.getCurrentParallel()).orElse(0);
        int limit = (int) Math.min(Integer.MAX_VALUE, 16L + hatchParallel);
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, limit);
        if (parallels == 0) return ModifierFunction.NULL;
        if (parallels == 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels)
                .parallels(parallels)
                .build();
    }
}
