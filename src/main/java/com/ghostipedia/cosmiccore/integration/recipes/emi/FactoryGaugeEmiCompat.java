package com.ghostipedia.cosmiccore.integration.recipes.emi;

import com.ghostipedia.cosmiccore.client.compat.create.FluidGaugeSetItemScreenExtension;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

public final class FactoryGaugeEmiCompat {

    private FactoryGaugeEmiCompat() {}

    public static void register(EmiRegistry registry) {
        registry.addDragDropHandler(FactoryPanelSetItemScreen.class, FactoryGaugeEmiCompat::dropFluid);
    }

    private static boolean dropFluid(
                                     FactoryPanelSetItemScreen screen, EmiIngredient ingredient, int mouseX,
                                     int mouseY) {
        if (!(screen instanceof FluidGaugeSetItemScreenExtension extension)) return false;
        FluidStack fluid = cosmiccore$resolveFluid(ingredient);
        return !fluid.isEmpty() && extension.cosmiccore$acceptFluidDrop(fluid, mouseX, mouseY);
    }

    private static FluidStack cosmiccore$resolveFluid(EmiIngredient ingredient) {
        for (EmiStack stack : ingredient.getEmiStacks()) {
            Fluid fluid = stack.getKeyOfType(Fluid.class);
            if (fluid != null) {
                return new FluidStack(
                        BuiltInRegistries.FLUID.wrapAsHolder(fluid), 1000, stack.getComponentChanges());
            }
            ItemStack item = stack.getItemStack();
            if (!item.isEmpty()) {
                FluidStack contained = FilterItemStack.of(item).fluid(Minecraft.getInstance().level);
                if (!contained.isEmpty()) return contained.copyWithAmount(1000);
            }
        }
        return FluidStack.EMPTY;
    }
}
