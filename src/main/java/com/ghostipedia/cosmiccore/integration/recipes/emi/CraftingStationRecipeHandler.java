package com.ghostipedia.cosmiccore.integration.recipes.emi;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;

import java.util.ArrayList;
import java.util.List;

public final class CraftingStationRecipeHandler implements StandardRecipeHandler<AbstractContainerMenu> {

    private static final ResourceLocation MENU_ID = ResourceLocation.fromNamespaceAndPath("craftingstation",
            "crafting_station");
    private static final String JEMI_HANDLER = "com.leclowndu93150.craftingstationjei.jei.CraftingStationTransferHandler";
    private static final int OUTPUT_SLOT = 0;
    private static final int FIRST_CRAFTING_SLOT = 1;
    private static final int CRAFTING_SLOT_COUNT = 9;

    private static boolean registered;

    public static void register(EmiRegistry registry) {
        BuiltInRegistries.MENU.getOptional(MENU_ID).ifPresent(menuType -> register(registry, menuType));
    }

    public static boolean shouldBypassJemi(Object handler) {
        return registered && handler != null && handler.getClass().getName().equals(JEMI_HANDLER);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void register(EmiRegistry registry, MenuType<?> menuType) {
        registry.addRecipeHandler((MenuType) menuType, new CraftingStationRecipeHandler());
        registered = true;
    }

    @Override
    public List<Slot> getInputSources(AbstractContainerMenu menu) {
        if (!hasCraftingGrid(menu)) {
            return List.of();
        }
        return new ArrayList<>(menu.slots.subList(FIRST_CRAFTING_SLOT, menu.slots.size()));
    }

    @Override
    public List<Slot> getCraftingSlots(AbstractContainerMenu menu) {
        if (!hasCraftingGrid(menu)) {
            return List.of();
        }
        return new ArrayList<>(menu.slots.subList(FIRST_CRAFTING_SLOT,
                FIRST_CRAFTING_SLOT + CRAFTING_SLOT_COUNT));
    }

    @Override
    public Slot getOutputSlot(AbstractContainerMenu menu) {
        return hasCraftingGrid(menu) ? menu.getSlot(OUTPUT_SLOT) : null;
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
    }

    private static boolean hasCraftingGrid(AbstractContainerMenu menu) {
        return menu.slots.size() >= FIRST_CRAFTING_SLOT + CRAFTING_SLOT_COUNT;
    }

    private CraftingStationRecipeHandler() {}
}
