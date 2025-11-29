package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ExoticCombustionEngineMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.ghostipedia.cosmiccore.utils.NumberUtils;

import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class CosmicFluidTooltipAddon {

    private static final String modid = CosmicCore.MOD_ID;
    static String prefix = ".tooltip.prefix";
    public static HashMap<String, HashMap<String, Double>> data = new HashMap<>();

    public static void appendFluidTooltip(ItemStack itemStack, HashMap<String, HashMap<String, Double>> hashMap) {
        if (itemStack.getItem() instanceof BucketItem bucketItem) {
            Fluid fluid = bucketItem.getFluid();
            String fluidID = ForgeRegistries.FLUIDS.getKey(fluid).toString();
            if (hashMap.isEmpty()) {
                hashMapInit(hashMap);
            }
            Set<String> keySet = hashMap.keySet();
            for (String i : keySet) {
                if (hashMap.get(i).containsKey(fluidID)) {
                    HashMap<String, Double> tmp = hashMap.get(i);
                    ResourceLocation fluidResource = new ResourceLocation(fluidID);
                    if (fluid instanceof GTFluid attributeFluid) {
                        FluidAttribute attribute;
                        if (".calorific".equals(i)) {
                            attribute = new FluidAttribute(
                                    fluidResource,
                                    list -> list.accept(Component.translatable(modid + i + prefix,
                                            NumberUtils.formatThousandsSeparators(tmp.get(fluidID)) + " EU/mB")),
                                    list -> {});
                        } else {
                            attribute = new FluidAttribute(
                                    fluidResource,
                                    list -> list.accept(Component.translatable(modid + i + prefix,
                                            tmp.get(fluidID).intValue())),
                                    list -> {});
                        }
                        attributeFluid.addAttribute(attribute);
                    }
                }
            }
        }
    }

    public static void hashMapInit(HashMap<String, HashMap<String, Double>> hashMap) {
        hashMap.put(".calorific", getFuelEnergy());
        hashMap.put(".lubricant", getLubricantTier());
        hashMap.put(".booster", getBoosterTier());
        CosmicCore.LOGGER.info("Finish Additional Fluid Tooltip Init!");
    }

    public static HashMap<String, Double> getFuelEnergy() {
        GTRecipeType[] gtRecipeTypes = {
                GTRecipeTypes.STEAM_TURBINE_FUELS,
                GTRecipeTypes.GAS_TURBINE_FUELS,
                GTRecipeTypes.COMBUSTION_GENERATOR_FUELS,
                CosmicRecipeTypes.NAQUAHINE_REACTOR
        };
        ArrayList<GTRecipe> recipes = new ArrayList<>();
        for (GTRecipeType i : gtRecipeTypes) {
            recipes.addAll(i.getRecipesInCategory(i.getCategory()));
        }
        HashMap<String, Double> calorificValue = new HashMap<>();
        for (GTRecipe i : recipes) {
            int fluidInputAmount = RecipeHelper.getInputFluids(i).get(0).getAmount();
            String fluidInputID = ForgeRegistries.FLUIDS
                    .getKey(RecipeHelper.getInputFluids(i).get(0).getFluid()).toString();
            long EUt = i.getOutputEUt().getTotalEU();
            int duration = i.duration;
            long EUTotal = EUt * duration;
            double EUPer = (double) EUTotal / (double) fluidInputAmount;
            calorificValue.put(fluidInputID, EUPer);
        }
        calorificValue.putIfAbsent("gtceu:steam", calorificValue.get("embers:steam"));
        return calorificValue;  // Why default steam is embers steam???
    }

    public static HashMap<String, Double> getLubricantTier() {
        HashMap<String, Double> lubricantTier = new HashMap<>();
        var tier = ExoticCombustionEngineMachine.getLubricantTiers();
        for (FluidStack i : tier.keySet()) {
            lubricantTier.put(ForgeRegistries.FLUIDS.getKey(i.getFluid()).toString(), (double) tier.getInt(i) - 1);
        }   // Lubricant Tier from 2 to 4?
        return lubricantTier;
    }

    public static HashMap<String, Double> getBoosterTier() {
        HashMap<String, Double> boosterTier = new HashMap<>();
        var tier = ExoticCombustionEngineMachine.getBoostingTiers();
        for (FluidStack i : tier.keySet()) {
            boosterTier.put(ForgeRegistries.FLUIDS.getKey(i.getFluid()).toString(), (double) tier.getInt(i));
        }
        return boosterTier;
    }
}
