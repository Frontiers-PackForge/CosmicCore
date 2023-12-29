package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.gtbridge.machine.kinetic.BasicSteamTurbineMachine;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.trait.NotifiableStressTrait;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;




public class BasicSteamTurbineLogic extends RecipeLogic {

    @Nullable @Getter
    public FluidTransferList inputFluidInventory;

    public BasicSteamTurbineLogic(BasicSteamTurbineMachine machine) {
        super(machine);
    }

    @Override
    public BasicSteamTurbineMachine getMachine() {
        return (BasicSteamTurbineMachine)super.getMachine();
    }

 //   @Override
   // public GTRecipe.ActionResult handleTickRecipe(GTRecipe recipe) {
     //   if (recipe.hasTick()) {
       //     var result = recipe.matchRecipe(IO.IN,getMachine(),recipe.tickInputs,false);
         ////     recipe.handleTickRecipeIO(IO.IN, this.machine);
             //   result = recipe.matchRecipe(IO.OUT,getMachine(),recipe.tickOutputs,false);
               // if( result.isSuccess()){
                //    recipe.handleTickRecipeIO(IO.OUT, this.machine);
                //}

           // } else {
            //    return result;
           // }
    //    }
   //     return GTRecipe.ActionResult.SUCCESS;
  //  }

    @Override
    public void findAndHandleRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            var match = getTurbineRecipe();
            if (match != null) {
                if (match.matchRecipe(this.machine).isSuccess() && match.matchTickRecipe(this.machine).isSuccess()) {
                    setupRecipe(match);
                }
            }
        }
    }

    @Nullable
    private GTRecipe getTurbineRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            var recipe = GTRecipeBuilder.ofRaw()
                    .perTick(true)
                    //.rpm(64).perTick(true)
                    .outputStress(2048)
                    .duration(Integer.MAX_VALUE)
                    .perTick(true)
                    .inputFluids(GTMaterials.Steam.getFluid(800))
                    .buildRawRecipe();
            if (recipe.matchRecipe(getMachine()).isSuccess() && recipe.matchTickRecipe(getMachine()).isSuccess()) {{
                    return recipe;
                }
            }
        }
        return null;
    }
}
