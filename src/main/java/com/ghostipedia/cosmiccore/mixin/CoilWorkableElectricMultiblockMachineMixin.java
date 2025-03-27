package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.api.capability.HeatCapability;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.server.level.ServerLevel;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.ParametersAreNonnullByDefault;

@Debug
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mixin(value = CoilWorkableElectricMultiblockMachine.class, remap = false)
public abstract class CoilWorkableElectricMultiblockMachineMixin extends WorkableElectricMultiblockMachine {

    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            CoilWorkableElectricMultiblockMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Shadow
    private ICoilType coilType;

    @Shadow
    public abstract int getCoilTier();

    @Shadow
    public abstract ICoilType getCoilType();

    // Temperature, in Kelvin (because GT uses kelvin instead of celsius.)
    @Unique
    @Persisted(key = "currentTemp")
    @DescSynced
    private float frontiers$currentTemp = 273;
    @Unique
    private TickableSubscription frontiers$temperatureTick = null;

    public CoilWorkableElectricMultiblockMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public float getTemperature() {
        return frontiers$currentTemp - 273;
    }

    public void setTemperature(float temp) {
        frontiers$currentTemp = temp + 273;
        this.onChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getTemperature() == 0 && this.getLevel() instanceof ServerLevel level) {
            setTemperature(level.getBiome(this.getPos()).get().getBaseTemperature());
        }
        if (frontiers$temperatureTick == null) {
            frontiers$temperatureTick = subscribeServerTick(this::frontiers$temperatureTick);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (frontiers$temperatureTick != null) {
            frontiers$temperatureTick.unsubscribe();
            frontiers$temperatureTick = null;
        }
    }

    @Unique
    private void frontiers$temperatureTick() {
        if (this.getLevel() instanceof ServerLevel level) {
            setTemperature(HeatCapability.adjustTempTowards(getTemperature(),
                    level.getBiome(this.getPos()).get().getBaseTemperature(), 0.5f));
        }
    }

    @Override
    public boolean onWorking() {
        System.out.println("TESTHELP");
        GTRecipe recipe = recipeLogic.getLastRecipe();
        this.setTemperature(HeatCapability.adjustTempTowards(getTemperature(),
                (coilType.getCoilTemperature() + 100 * Math.max(0, this.getTier() - GTValues.MV)) - 273,
                (getCoilTier() + 1) / 1.5f));
        /*
         * nah too evil.
         * if (getTemperature() <= getCoilType().getCoilTemperature()) {
         * if (!this.getCapabilitiesProxy().contains(IO.IN, EURecipeCapability.CAP)) return;
         * 
         * if (getRecipeLogic().getLastRecipe() == null) return;
         * long EUt = RecipeHelper.getInputEUt(getRecipeLogic().getLastRecipe());
         * GTRecipe recipe = GTRecipeBuilder.ofRaw().EUt(EUt).buildRawRecipe();
         * getRecipeLogic().handleTickRecipe(recipe);
         * }
         */
        return super.onWorking();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
