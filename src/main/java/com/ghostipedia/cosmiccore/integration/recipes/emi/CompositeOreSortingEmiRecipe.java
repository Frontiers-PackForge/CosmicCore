package com.ghostipedia.cosmiccore.integration.recipes.emi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;
import com.ghostipedia.cosmiccore.common.data.recipe.CompositeOreSortingPlan;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.IndustrialOreSorter;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

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

import static com.ghostipedia.cosmiccore.api.data.CosmicTagPrefix.oreChunk;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.crushed;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rawOre;

public final class CompositeOreSortingEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 150;
    private static final int FIRST_ROW_Y = 34;
    private static final int ROW_HEIGHT = 23;

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            CosmicCore.id("composite_ore_sorting"), EmiStack.of(IndustrialOreSorter.INDUSTRIAL_ORE_SORTER.asStack())) {

        @Override
        public Component getName() {
            return Component.translatable("cosmiccore.emi.composite_ore_sorting");
        }
    };

    private final Material bundle;
    private final List<Material> minerals;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public CompositeOreSortingEmiRecipe(Material bundle) {
        this.bundle = bundle;
        this.minerals = CosmicBundleMaterials.outputsOf(bundle);
        this.id = CosmicCore.id("emi/composite_ore_sorting/" + bundle.getName());
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
        return HEIGHT;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ItemStack raw = ChemicalHelper.get(rawOre, bundle);
        ItemStack crushedStack = ChemicalHelper.get(crushed, bundle);
        SlotWidget rawSlot = widgets.addSlot(EmiStack.of(raw), 4, 1).drawBack(true).recipeContext(this);
        appendEntryTooltip(rawSlot);
        addInfoMarker(widgets, 4, 1);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, 2);
        SlotWidget crushedSlot = widgets.addSlot(EmiStack.of(crushedStack), 54, 1).drawBack(true).recipeContext(this);
        appendEntryTooltip(crushedSlot);
        addInfoMarker(widgets, 54, 1);
        widgets.addText(raw.getHoverName(), 78, 6, 0x404040, false);

        widgets.addText(Component.translatable("cosmiccore.emi.composite_ore_sorting.tier"), 4, 24, 0x404040,
                false);
        widgets.addText(Component.translatable("cosmiccore.emi.composite_ore_sorting.first_recovered"), 58, 24,
                0x404040, false);

        List<CompositeOreSortingPlan.SortStage> stages = CompositeOreSortingPlan.stages();
        for (int stageIndex = 0; stageIndex < stages.size(); stageIndex++) {
            CompositeOreSortingPlan.SortStage stage = stages.get(stageIndex);
            int y = FIRST_ROW_Y + stageIndex * ROW_HEIGHT;
            ItemStack stageInput = ChemicalHelper.get(stage.inputForm(), bundle);
            SlotWidget stageSlot = widgets.addSlot(EmiStack.of(stageInput), 4, y).drawBack(true).recipeContext(this);
            appendStageTooltip(stageSlot, stage, stageIndex);
            addInfoMarker(widgets, 4, y);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 28, y + 1);

            int first = stage.firstOutputIndex();
            int end = Math.min(stage.typeCount(), minerals.size());
            if (first >= end) {
                widgets.addText(Component.translatable("cosmiccore.emi.composite_ore_sorting.no_new_mineral"), 58,
                        y + 5, 0x606060, false);
            } else {
                for (int mineralIndex = first; mineralIndex < end; mineralIndex++) {
                    ItemStack chunk = ChemicalHelper.get(oreChunk, minerals.get(mineralIndex),
                            stage.outputAmount(mineralIndex));
                    widgets.addSlot(EmiStack.of(chunk), 58 + (mineralIndex - first) * 20, y)
                            .drawBack(true).recipeContext(this);
                }
            }

        }
    }

    private void appendStageTooltip(SlotWidget slot, CompositeOreSortingPlan.SortStage stage, int stageIndex) {
        slot.appendTooltip(Component.translatable("cosmiccore.emi.composite_ore_sorting.tier_number", stageIndex + 1));
        slot.appendTooltip(Component.translatable("cosmiccore.emi.composite_ore_sorting.process_order",
                Component.translatable("cosmiccore.emi.composite_ore_sorting.process_order." + (stageIndex + 1))));
        slot.appendTooltip(Component.translatable("cosmiccore.emi.composite_ore_sorting.refinement_ratio"));
        slot.appendTooltip(Component.translatable("cosmiccore.emi.composite_ore_sorting.sorter_yield"));
        int end = Math.min(stage.typeCount(), minerals.size());
        for (int mineralIndex = 0; mineralIndex < end; mineralIndex++) {
            ItemStack chunk = ChemicalHelper.get(oreChunk, minerals.get(mineralIndex));
            slot.appendTooltip(Component.translatable("cosmiccore.emi.composite_ore_sorting.sorter_output",
                    stage.outputAmount(mineralIndex), chunk.getHoverName()));
        }
    }

    private static void appendEntryTooltip(SlotWidget slot) {
        slot.appendTooltip(Component.translatable("cosmiccore.emi.composite_ore_sorting.entry"));
        slot.appendTooltip(Component.translatable("cosmiccore.emi.composite_ore_sorting.entry.hammer"));
        slot.appendTooltip(Component.translatable("cosmiccore.emi.composite_ore_sorting.entry.macerator"));
    }

    private static void addInfoMarker(WidgetHolder widgets, int x, int y) {
        widgets.addText(Component.literal("i"), x + 2, y + 1, 0x55FFFF, true);
    }

    private List<EmiIngredient> buildInputs() {
        List<EmiIngredient> result = new ArrayList<>();
        addInput(result, ChemicalHelper.get(rawOre, bundle));
        addInput(result, ChemicalHelper.get(crushed, bundle));
        for (CompositeOreSortingPlan.SortStage stage : CompositeOreSortingPlan.stages()) {
            addInput(result, ChemicalHelper.get(stage.inputForm(), bundle));
        }
        return List.copyOf(result);
    }

    private List<EmiStack> buildOutputs() {
        List<EmiStack> result = new ArrayList<>();
        for (Material mineral : minerals.subList(0, Math.min(minerals.size(), CompositeOreSortingPlan.SORTER_IO_CAP))) {
            addOutput(result, ChemicalHelper.get(oreChunk, mineral));
            addOutput(result, ChemicalHelper.get(dust, mineral));
        }
        return List.copyOf(result);
    }

    private static void addInput(List<EmiIngredient> result, ItemStack stack) {
        if (!stack.isEmpty()) result.add(EmiStack.of(stack));
    }

    private static void addOutput(List<EmiStack> result, ItemStack stack) {
        if (!stack.isEmpty()) result.add(EmiStack.of(stack));
    }
}
