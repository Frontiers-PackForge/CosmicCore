package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.FluidDrillingArea;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FluidDrillMachine;
import com.gregtechceu.gtceu.common.machine.trait.FluidDrillLogic;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Mixin(value = FluidDrillLogic.class, remap = false)
public abstract class FluidDrillingLogicMixin extends RecipeLogic implements FluidDrillingArea {

    @Shadow
    public abstract FluidDrillMachine getMachine();

    @Shadow
    private Fluid veinFluid;
    @Unique
    @SaveField
    private boolean cosmiccore$expanded;

    @Override
    public boolean cosmiccore$isExpanded() {
        return cosmiccore$expanded;
    }

    @Override
    public void cosmiccore$setExpanded(boolean expanded) {
        if (cosmiccore$expanded == expanded) return;
        cosmiccore$expanded = expanded;
        getMachine().setChanged();
        if (lastRecipe == null) updateTickSubscription();
    }

    @Override
    public List<FluidStack> cosmiccore$getOutputs() {
        if (!(getMachine().getLevel() instanceof ServerLevel level)) return List.of();
        var data = BedrockFluidVeinSavedData.getOrCreate(level);
        int radius = cosmiccore$expanded ? Math.clamp(getMachine().getTier() - GTValues.MV + 1, 1, 3) : 0;
        int cx = SectionPos.blockToSectionCoord(getMachine().getBlockPos().getX());
        int cz = SectionPos.blockToSectionCoord(getMachine().getBlockPos().getZ());
        var totals = new LinkedHashMap<Fluid, Integer>();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                var entry = data.getFluidVeinWorldEntry(x, z);
                if (entry == null || entry.getDefinition() == null) continue;
                int amount = entry.getFluidYield() * FluidDrillMachine.getRigMultiplier(getMachine().getTier());
                if (getMachine().getEnergyTier() > getMachine().getTier()) amount = amount * 3 / 2;
                if (amount > 0) totals.merge(entry.getDefinition().value().getStoredFluid(), amount, Integer::sum);
            }
        }
        var outputs = new ArrayList<FluidStack>();
        totals.forEach((fluid, amount) -> outputs.add(new FluidStack(fluid, amount)));
        return outputs;
    }

    @Unique
    private GTRecipe cosmiccore$recipe() {
        var outputs = cosmiccore$getOutputs();
        veinFluid = outputs.isEmpty() ? null : outputs.getFirst().getFluid();
        if (outputs.isEmpty()) return null;
        var recipe = GTRecipeBuilder.ofRaw().duration(FluidDrillLogic.MAX_PROGRESS)
                .EUt(GTValues.VA[getMachine().getEnergyTier()])
                .outputFluids(outputs.toArray(FluidStack[]::new)).build();
        return RecipeHelper.matchContents(getMachine(), recipe).isSuccess() ? recipe : null;
    }

    @Inject(method = "onRecipeFinish",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/common/machine/trait/FluidDrillLogic;depleteVein()V",
                     shift = At.Shift.AFTER),
            cancellable = true)
    private void cosmiccore$pauseAfterCurrentCycle(CallbackInfo ci) {
        if (!suspendAfterFinish) return;
        setStatus(Status.SUSPEND);
        consecutiveRecipes = 0;
        progress = 0;
        duration = 0;
        isActive = false;
        lastRecipe = null;
        lastUnrolledRecipe = null;
        syncDataHolder.resyncAllFields();
        ci.cancel();
    }

    @Inject(method = "findAndHandleRecipe", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$findAreaRecipe(CallbackInfo ci) {
        if (getMachine().getLevel() instanceof ServerLevel) {
            lastRecipe = null;
            var recipe = cosmiccore$recipe();
            if (recipe != null) setupRecipe(recipe);
        }
        ci.cancel();
    }

    @Inject(method = "getFluidDrillRecipe", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$areaRecipe(CallbackInfoReturnable<GTRecipe> cir) {
        cir.setReturnValue(cosmiccore$recipe());
    }

    @Inject(method = "getFluidToProduce()I", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$totalOutput(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cosmiccore$getOutputs().stream().mapToInt(FluidStack::getAmount).sum());
    }

    @Inject(method = "depleteVein", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$infiniteWells(CallbackInfo ci) {
        ci.cancel();
    }
}
