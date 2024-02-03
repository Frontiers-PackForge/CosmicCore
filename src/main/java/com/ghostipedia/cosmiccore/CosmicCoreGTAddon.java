package com.ghostipedia.cosmiccore;


import com.ghostipedia.cosmiccore.api.registries.CosmicRegistries;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

@GTAddon
public class CosmicCoreGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return CosmicRegistries.COSMIC_REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        CosmicCore.LOGGER.info("CosmicCoreGTAddon has loaded!");
    }

    @Override
    public String addonModId() {
        return CosmicCore.MOD_ID;
    }
/*

 */
  //  public static final ContentJS<Double> PRESSURE_IN = new ContentJS<>(NumberComponent.ANY_DOUBLE, CosmicRecipeCaps.PRESSURE, false);
   // public static final ContentJS<Double> PRESSURE_OUT = new ContentJS<>(NumberComponent.ANY_DOUBLE, CosmicRecipeCaps.PRESSURE, true);


   // @Override
  //  public void registerRecipeKeys(KJSRecipeKeyEvent event) {
  //      event.registerKey(CosmicRecipeCaps.PRESSURE, Pair.of(PRESSURE_IN, PRESSURE_OUT));
  //  }
}