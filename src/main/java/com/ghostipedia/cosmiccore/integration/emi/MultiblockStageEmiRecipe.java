package com.ghostipedia.cosmiccore.integration.emi;

import com.ghostipedia.cosmiccore.api.machine.multiblock.GroupedSlicePreviewSupport;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.MultiblockInfoEmiCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public final class MultiblockStageEmiRecipe extends ModularUIEmiRecipe {

    private final MultiblockMachineDefinition definition;
    private final int stage;
    private final List<EmiIngredient> containedBlocks;

    private MultiblockStageEmiRecipe(MultiblockMachineDefinition definition, BlockPattern pattern,
                                     GroupedSlicePreviewSupport.Group group, int stage) {
        super(recipeId(definition, stage), () -> createWidget(definition, pattern, group, stage));
        this.definition = definition;
        this.stage = stage;
        this.containedBlocks = createInputs(definition, pattern, group, stage);
    }

    public static void register(EmiRegistry registry) {
        GTRegistries.MACHINES.stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .forEach(definition -> registerDefinition(registry, definition));
    }

    private static void registerDefinition(EmiRegistry registry, MultiblockMachineDefinition definition) {
        if (!(definition.getStructurePatterns().get(DEFAULT_STRUCTURE).get() instanceof BlockPattern pattern)) return;
        List<GroupedSlicePreviewSupport.Group> groups = GroupedSlicePreviewSupport.variableGroups(pattern);
        if (groups.size() != 1) return;
        GroupedSlicePreviewSupport.Group group = groups.getFirst();
        for (int stage = group.minRepeats() + 1; stage <= group.maxRepeats(); stage++) {
            registry.addRecipe(new MultiblockStageEmiRecipe(definition, pattern, group, stage));
        }
    }

    private static MultiblockPreviewWidget createWidget(MultiblockMachineDefinition definition, BlockPattern pattern,
                                                        GroupedSlicePreviewSupport.Group group, int stage) {
        MultiblockSchemaInfo schemaInfo = new MultiblockSchemaInfo();
        schemaInfo.getUserSliceRepeats().putAll(createRepeats(pattern, group, stage));
        return new MultiblockPreviewWidget(definition, schemaInfo, 200, 180);
    }

    private static List<EmiIngredient> createInputs(MultiblockMachineDefinition definition, BlockPattern pattern,
                                                    GroupedSlicePreviewSupport.Group group, int stage) {
        Int2IntMap repeats = createRepeats(pattern, group, stage);

        Map<BlockPos, BlockInfo> structure = new HashMap<>();
        AbstractStructureHelper.blockPattern(repeats).populate(
                structure,
                pattern,
                null,
                definition.getRotationState().defaultDirection,
                switch (definition.getRotationState()) {
                    case Y_AXIS -> Direction.NORTH;
                    case ALL, NON_Y_AXIS, NONE -> Direction.UP;
                },
                false);

        Object2IntMap<Block> counts = new Object2IntOpenHashMap<>();
        structure.values().forEach(info -> counts.mergeInt(info.getBlockState().getBlock(), 1, Integer::sum));
        List<EmiIngredient> inputs = new ArrayList<>(counts.size());
        counts.forEach((block, count) -> inputs.add(EmiStack.of(new ItemStack(block.asItem(), count))));
        return List.copyOf(inputs);
    }

    private static Int2IntMap createRepeats(BlockPattern pattern, GroupedSlicePreviewSupport.Group group, int stage) {
        Int2IntMap repeats = new Int2IntArrayMap();
        PatternSlice[] slices = pattern.getSlices();
        for (int index = 0; index < slices.length; index++) {
            repeats.put(index, slices[index].getMinRepeats());
        }
        repeats.put(GroupedSlicePreviewSupport.repeatKey(group.index()), stage);
        return repeats;
    }

    private static ResourceLocation recipeId(MultiblockMachineDefinition definition, int stage) {
        ResourceLocation id = definition.getId();
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                "multi_info/" + id.getPath() + "/stage_" + stage);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return MultiblockInfoEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipeId(definition, stage);
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return containedBlocks;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(definition.getBlock()));
    }
}
