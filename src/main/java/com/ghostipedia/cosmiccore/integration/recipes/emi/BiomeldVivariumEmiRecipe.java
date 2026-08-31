package com.ghostipedia.cosmiccore.integration.recipes.emi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.BloomwyrmSystem;
import com.ghostipedia.cosmiccore.common.vitae.CultivationItemOutput;
import com.ghostipedia.cosmiccore.common.vitae.CultivationProfile;
import com.ghostipedia.cosmiccore.common.vitae.EnderIOSpawnerResolver;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BiomeldVivariumEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 176;
    private static final int MINIMUM_HEIGHT = 126;
    private static final int OUTPUT_START_X = 84;
    private static final int OUTPUT_START_Y = 43;
    private static final int OUTPUT_COLUMNS = 4;
    private static final ResourceLocation VITAE_FLUID = ResourceLocation.fromNamespaceAndPath(
            "neovitae", "essentia_vitae_source");

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            CosmicCore.id("biomeld_vivarium_cultivation"),
            EmiStack.of(BloomwyrmSystem.BIOMELD_VIVARIUM.asStack())) {

        @Override
        public Component getName() {
            return Component.translatable("cosmiccore.emi.biomeld_vivarium");
        }
    };

    private final CultivationProfile profile;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public BiomeldVivariumEmiRecipe(CultivationProfile profile) {
        this.profile = profile;
        this.id = CosmicCore.id("emi/biomeld_vivarium/" +
                profile.entity().getNamespace() + "/" + profile.entity().getPath());
        this.inputs = buildInputs();
        this.outputs = buildOutputs();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return Math.max(MINIMUM_HEIGHT, footerY() + 40);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        Component entityName = BuiltInRegistries.ENTITY_TYPE.get(profile.entity()).getDescription();
        widgets.addText(entityName, 4, 3, 0x404040, false);
        widgets.addText(Component.translatable("cosmiccore.biomeld_vivarium.mode.material"), 4, 15, 0x404040,
                false);

        SlotWidget spawner = widgets.addSlot(inputs.get(0), 8, 43).drawBack(true).catalyst(true).recipeContext(this);
        spawner.appendTooltip(Component.translatable(
                "cosmiccore.emi.biomeld_vivarium.spawner",
                entityName));
        SlotWidget nutrient = widgets.addSlot(inputs.get(1), 30, 43).drawBack(true).recipeContext(this);
        nutrient.appendTooltip(Component.translatable(
                "cosmiccore.emi.biomeld_vivarium.nutrient",
                profile.tier().nutrientAmount()));
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 56, 44);

        addMaterialOutputs(widgets);
        int footerY = footerY();
        widgets.addText(Component.translatable(
                "cosmiccore.emi.biomeld_vivarium.spiritus",
                profile.spiritus().units(profile.tier())), 4, footerY, 0x404040, false);

        widgets.addText(Component.translatable(
                "cosmiccore.emi.biomeld_vivarium.charge",
                profile.tier().bloomwyrmCharge()), 4, footerY + 14, 0x404040, false);
        widgets.addText(Component.translatable(
                "cosmiccore.emi.biomeld_vivarium.requirements",
                profile.tier().getSerializedName().toUpperCase(Locale.ROOT),
                profile.tier().eut(),
                formatSeconds(profile.tier().duration())), 4, footerY + 26, 0x404040, false);
    }

    private void addMaterialOutputs(WidgetHolder widgets) {
        int outputIndex = 0;
        for (CultivationItemOutput output : profile.itemOutputs()) {
            if (output.maxCount() <= 0) continue;
            SlotWidget slot = widgets.addSlot(createItemOutput(output), outputX(outputIndex), outputY(outputIndex))
                    .drawBack(true)
                    .recipeContext(this);
            slot.appendTooltip(Component.translatable(
                    "cosmiccore.emi.biomeld_vivarium.count_range",
                    output.minCount(),
                    output.maxCount()));
            if (output.chance() < 1.0) {
                slot.appendTooltip(Component.translatable(
                        "cosmiccore.emi.biomeld_vivarium.chance",
                        formatChance(output.chance())));
            }
            outputIndex++;
        }
        int vitae = profile.vitae().units(profile.tier());
        if (vitae > 0 && BuiltInRegistries.FLUID.containsKey(VITAE_FLUID)) {
            SlotWidget slot = widgets.addSlot(
                    EmiStack.of(BuiltInRegistries.FLUID.get(VITAE_FLUID), vitae),
                    outputX(outputIndex),
                    outputY(outputIndex)).drawBack(true).recipeContext(this);
            slot.appendTooltip(Component.translatable(
                    "cosmiccore.emi.biomeld_vivarium.vitae",
                    vitae));
        }
    }

    private List<EmiIngredient> buildInputs() {
        var result = new ArrayList<EmiIngredient>();
        ItemStack spawner = EnderIOSpawnerResolver.createAttunedPoweredSpawner(profile.entity());
        result.add(EmiStack.of(spawner));
        List<EmiStack> nutrients = profile.tier().acceptedNutrients().stream()
                .map(material -> material.getFluid(profile.tier().nutrientAmount()))
                .map(stack -> EmiStack.of(stack.getFluid(), stack.getAmount()))
                .toList();
        result.add(EmiIngredient.of(nutrients));
        return List.copyOf(result);
    }

    private List<EmiStack> buildOutputs() {
        var result = new ArrayList<EmiStack>();
        for (CultivationItemOutput output : profile.itemOutputs()) {
            if (output.maxCount() > 0) result.add(createItemOutput(output));
        }
        int vitae = profile.vitae().units(profile.tier());
        if (vitae > 0 && BuiltInRegistries.FLUID.containsKey(VITAE_FLUID)) {
            result.add(EmiStack.of(BuiltInRegistries.FLUID.get(VITAE_FLUID), vitae));
        }
        return List.copyOf(result);
    }

    private static EmiStack createItemOutput(CultivationItemOutput output) {
        return EmiStack.of(new ItemStack(BuiltInRegistries.ITEM.get(output.item()), output.maxCount()))
                .setChance((float) output.chance());
    }

    private static int outputX(int outputIndex) {
        return OUTPUT_START_X + outputIndex % OUTPUT_COLUMNS * 20;
    }

    private static int outputY(int outputIndex) {
        return OUTPUT_START_Y + outputIndex / OUTPUT_COLUMNS * 20;
    }

    private int footerY() {
        int rows = (outputSlotCount() + OUTPUT_COLUMNS - 1) / OUTPUT_COLUMNS;
        return Math.max(86, OUTPUT_START_Y + rows * 20 + 3);
    }

    private int outputSlotCount() {
        int count = (int) profile.itemOutputs().stream().filter(output -> output.maxCount() > 0).count();
        if (profile.vitae().units(profile.tier()) > 0 && BuiltInRegistries.FLUID.containsKey(VITAE_FLUID)) count++;
        return count;
    }

    private static String formatChance(double chance) {
        return String.format(Locale.ROOT, "%.1f%%", chance * 100.0);
    }

    private static String formatSeconds(int duration) {
        return String.format(Locale.ROOT, "%.1f", duration / 20.0);
    }
}
