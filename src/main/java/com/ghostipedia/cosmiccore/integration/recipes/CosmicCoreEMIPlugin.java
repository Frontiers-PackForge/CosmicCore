package com.ghostipedia.cosmiccore.integration.recipes;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.LARVA;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.LarvaMachine;
import com.ghostipedia.cosmiccore.integration.recipes.emi.AsteroidEmiRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.ArrayList;
import java.util.List;

@EmiEntrypoint
public class CosmicCoreEMIPlugin implements EmiPlugin {

    public static final ResourceLocation ASTEROID_CATEGORY_ID = CosmicCore.id("asteroid_mining");
    public static final EmiRecipeCategory ASTEROID_CATEGORY = new EmiRecipeCategory(ASTEROID_CATEGORY_ID,
            EmiStack.of(CosmicItems.TARGETING_CHIP));

    @Override
    public void register(EmiRegistry registry) {
        registry.setDefaultComparison(CosmicItems.TARGETING_CHIP.asStack(), Comparison.compareNbt());

        registry.addCategory(ASTEROID_CATEGORY);
        registry.addWorkstation(ASTEROID_CATEGORY, EmiStack.of(LARVA.LARVA.getBlock()));

        addAsteroidRecipe(registry, "carbonic_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.CARBON_ASTEROID.asStack())), CosmicItems.CARBON_ASTEROID.asStack());
        addAsteroidRecipe(registry, "ferric_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.FERRIC_ASTEROID.asStack())), CosmicItems.FERRIC_ASTEROID.asStack());
        addAsteroidRecipe(registry, "rare_metal_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.RARE_METAL_ASTEROID.asStack())),
                CosmicItems.RARE_METAL_ASTEROID.asStack());
        addAsteroidRecipe(registry, "auric_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.AURIC_ASTEROID.asStack())), CosmicItems.AURIC_ASTEROID.asStack());
        addAsteroidRecipe(registry, "brimstone_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.BRIMSTONE_ASTEROID.asStack())),
                CosmicItems.BRIMSTONE_ASTEROID.asStack());
        addAsteroidRecipe(registry, "lith_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.LITH_ASTEROID.asStack())), CosmicItems.LITH_ASTEROID.asStack());
        addAsteroidRecipe(registry, "mafic_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.MAFIC_ASTEROID.asStack())), CosmicItems.MAFIC_ASTEROID.asStack());
        addAsteroidRecipe(registry, "mossy_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.MOSSY_ASTEROID.asStack())), CosmicItems.MOSSY_ASTEROID.asStack());
        addAsteroidRecipe(registry, "occult_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.OCCULT_ASTEROID.asStack())), CosmicItems.OCCULT_ASTEROID.asStack());
        addAsteroidRecipe(registry, "oxide_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.OXIDE_ASTEROID.asStack())), CosmicItems.OXIDE_ASTEROID.asStack());
        addAsteroidRecipe(registry, "sanguine_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.SANGUINE_ASTEROID.asStack())), CosmicItems.SANGUINE_ASTEROID.asStack());
        addAsteroidRecipe(registry, "wasteland_asteroid", CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(),
                List.of(EmiStack.of(CosmicItems.WASTELAND_ASTEROID.asStack())),
                CosmicItems.WASTELAND_ASTEROID.asStack());
    }

    private void addAsteroidRecipe(EmiRegistry registry, String keySuffix, ItemStack spool, List<EmiStack> outputs,
                                   ItemStack icon) {
        ItemStack chip = LarvaMachine.getAstroidDataChip(keySuffix, 1);

        List<EmiIngredient> inputs = new ArrayList<>();
        inputs.add(EmiStack.of(chip));
        inputs.add(EmiStack.of(spool));

        ResourceLocation recipeID = CosmicCore.id("emi/asteroid_processing/" + keySuffix);
        registry.addRecipe(new AsteroidEmiRecipe(ASTEROID_CATEGORY, recipeID, inputs, outputs, EmiStack.of(icon)));
    }
}
