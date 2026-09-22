package com.ghostipedia.cosmiccore.integration.recipes.emi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryPyrofluxPolicy;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.HephaestusCauldron;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class HephaestusCauldronEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 86;
    private static final ResourceLocation ID = CosmicCore.id("emi/hephaestus_cauldron/pyroflux_generation");

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            CosmicCore.id("hephaestus_cauldron_pyroflux"),
            EmiStack.of(HephaestusCauldron.MACHINE.asStack())) {

        @Override
        public Component getName() {
            return Component.translatable("cosmiccore.emi.hephaestus_cauldron");
        }
    };

    private final List<EmiIngredient> inputs = List.of(
            EmiStack.of(CosmicMaterials.AcidicWoodLiquor.getFluid(), FoundryPyrofluxPolicy.LIQUOR_PER_BATCH),
            EmiStack.of(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Stone,
                    FoundryPyrofluxPolicy.STONE_DUST_PER_BATCH)));
    private final EmiStack output = EmiStack.of(FoundryPyrofluxPolicy.fluid(),
            FoundryPyrofluxPolicy.CHARGE_PER_BATCH);

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return ID;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(Component.translatable("cosmiccore.emi.hephaestus_cauldron.instruction"),
                4, 4, 0x404040, false);
        SlotWidget liquor = widgets.addSlot(inputs.get(0), 20, 25).drawBack(true).recipeContext(this);
        liquor.appendTooltip(Component.translatable("cosmiccore.emi.hephaestus_cauldron.liquor",
                FoundryPyrofluxPolicy.LIQUOR_PER_BATCH));
        SlotWidget stone = widgets.addSlot(inputs.get(1), 42, 25).drawBack(true).recipeContext(this);
        stone.appendTooltip(Component.translatable("cosmiccore.emi.hephaestus_cauldron.stone",
                FoundryPyrofluxPolicy.STONE_DUST_PER_BATCH));
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 70, 26);
        SlotWidget charge = widgets.addSlot(output, 110, 25).drawBack(true).recipeContext(this);
        charge.appendTooltip(Component.translatable("cosmiccore.emi.hephaestus_cauldron.pyroflux",
                FoundryPyrofluxPolicy.CHARGE_PER_BATCH));
        charge.appendTooltip(Component.translatable("cosmiccore.emi.hephaestus_cauldron.virtual"));
        widgets.addText(Component.translatable("cosmiccore.emi.hephaestus_cauldron.pyroflux_short",
                FoundryPyrofluxPolicy.CHARGE_PER_BATCH), 96, 48, 0x7A3000, false);
        widgets.addText(Component.translatable("cosmiccore.emi.hephaestus_cauldron.requirements",
                FoundryPyrofluxPolicy.GENERATION_EUT,
                FoundryPyrofluxPolicy.GENERATION_DURATION,
                FoundryPyrofluxPolicy.GENERATION_DURATION / 20), 4, 68, 0x404040, false);
    }
}
