package com.ghostipedia.cosmiccore.integration.recipes;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.mirror.ClientDeedCache;
import com.ghostipedia.cosmiccore.client.mirror.DeedInventoryButton;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicOreFormPolicy;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodRegistry;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.BloomwyrmSystem;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.IndustrialFlotationPlant;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.IndustrialOreSorter;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.LARVA;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.Powderizer;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.LarvaMachine;
import com.ghostipedia.cosmiccore.common.vitae.CultivationProfileManager;
import com.ghostipedia.cosmiccore.common.vitae.EnderIOSpawnerResolver;
import com.ghostipedia.cosmiccore.integration.recipes.emi.AsteroidEmiRecipe;
import com.ghostipedia.cosmiccore.integration.recipes.emi.BiomeldVivariumEmiRecipe;
import com.ghostipedia.cosmiccore.integration.recipes.emi.CompositeOreSortingEmiRecipe;
import com.ghostipedia.cosmiccore.integration.recipes.emi.CraftingStationRecipeHandler;
import com.ghostipedia.cosmiccore.integration.recipes.emi.FactoryGaugeEmiCompat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.integration.emi.handler.EmiScreenHandler;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ScreenWrapper;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EmiEntrypoint
public class CosmicCoreEMIPlugin implements EmiPlugin {

    public static final ResourceLocation ASTEROID_CATEGORY_ID = CosmicCore.id("asteroid_mining");
    public static final EmiRecipeCategory ASTEROID_CATEGORY = new EmiRecipeCategory(ASTEROID_CATEGORY_ID,
            EmiStack.of(CosmicItems.TARGETING_CHIP));

    @Override
    public void initialize(EmiInitRegistry registry) {
        Map<Item, Boolean> hiddenOreFormCache = new ConcurrentHashMap<>();
        registry.disableStacks(stack -> {
            Item item = stack.getKeyOfType(Item.class);
            return item != null && hiddenOreFormCache.computeIfAbsent(item,
                    CosmicOreFormPolicy::isUnusedGeneratedOreForm);
        });
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.setDefaultComparison(CosmicItems.TARGETING_CHIP.asStack(), Comparison.compareComponents());
        registry.removeEmiStacks(stack -> EnderIOSpawnerResolver.resolveSpawnerSoul(stack.getItemStack())
                .filter(entity -> CultivationProfileManager.INSTANCE.get(entity).isEmpty())
                .isPresent());

        registerModularUIScreen(registry, ScreenWrapper.class);
        registerModularUIScreen(registry, ContainerScreenWrapper.class);
        registry.addExclusionArea(InventoryScreen.class, (screen, consumer) -> {
            if (!ClientDeedCache.entryUnlocked() || !DeedInventoryButton.visibleOnScreen(screen)) return;
            int top = screen.getGuiTop() + screen.getYSize() - DeedInventoryButton.TEXTURE_BUFFER;
            consumer.accept(new Bounds(
                    screen.getGuiLeft(),
                    top,
                    screen.getXSize(),
                    DeedInventoryButton.visualBottom(screen) - top));
        });
        CraftingStationRecipeHandler.register(registry);
        FactoryGaugeEmiCompat.register(registry);
        registerFoodRoleAliases(registry);

        registry.addCategory(CompositeOreSortingEmiRecipe.CATEGORY);
        registry.addWorkstation(CompositeOreSortingEmiRecipe.CATEGORY,
                EmiStack.of(IndustrialOreSorter.INDUSTRIAL_ORE_SORTER.asStack()));
        registry.addWorkstation(CompositeOreSortingEmiRecipe.CATEGORY,
                EmiStack.of(Powderizer.POWDERIZER.asStack()));
        registry.addWorkstation(CompositeOreSortingEmiRecipe.CATEGORY,
                EmiStack.of(IndustrialFlotationPlant.INDUSTRIAL_FLOTATION_PLANT.asStack()));
        for (var bundle : CosmicBundleMaterials.bundleOres()) {
            registry.addRecipe(new CompositeOreSortingEmiRecipe(bundle));
        }

        registry.addCategory(BiomeldVivariumEmiRecipe.CATEGORY);
        registry.addWorkstation(BiomeldVivariumEmiRecipe.CATEGORY,
                EmiStack.of(BloomwyrmSystem.BIOMELD_VIVARIUM.asStack()));
        CultivationProfileManager.INSTANCE.profiles().values().stream()
                .sorted(Comparator.comparing(profile -> profile.entity().toString()))
                .forEach(profile -> registry.addRecipe(new BiomeldVivariumEmiRecipe(profile)));

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

    private static void registerFoodRoleAliases(EmiRegistry registry) {
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (!CosmicFoodRegistry.isConsumable(stack)) continue;
            registry.addAlias(EmiStack.of(stack), CosmicFoodRegistry.plateRole(stack).label());
        }
    }

    private static <T extends Screen & IMuiScreen> void registerModularUIScreen(EmiRegistry registry, Class<T> cls) {
        EmiScreenHandler<T> handler = EmiScreenHandler.of(cls);
        registry.addDragDropHandler(cls, handler);
        registry.addStackProvider(cls, handler);
        registry.addExclusionArea(cls, handler);
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
