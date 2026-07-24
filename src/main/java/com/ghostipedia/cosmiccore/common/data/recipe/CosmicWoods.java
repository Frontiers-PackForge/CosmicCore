package com.ghostipedia.cosmiccore.common.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.WoodTypeEntry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CosmicWoods {

    private CosmicWoods() {}

    private static final String[][] IRREGULAR = {
            { "occultism", "otherplanks", "otherplanks", "otherworld" },
    };

    private static List<WoodTypeEntry> entries;
    private static final Map<String, List<ItemLike>> plankInputs = new HashMap<>();

    public static List<WoodTypeEntry> entries() {
        if (entries == null) discover();
        return entries;
    }

    private static void discover() {
        plankInputs.clear();
        List<WoodTypeEntry> found = new ArrayList<>();
        Set<ResourceLocation> forms = new HashSet<>();
        List<ResourceLocation> logs = new ArrayList<>();
        List<ResourceLocation> strippedLogs = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            String p = id.getPath();
            if (!p.endsWith("_log") && !p.endsWith("_stem")) continue;
            if (p.startsWith("stripped_")) {
                strippedLogs.add(id);
            } else {
                logs.add(id);
            }
        }
        for (ResourceLocation planksId : BuiltInRegistries.ITEM.keySet()) {
            String ns = planksId.getNamespace();
            if (ns.equals("minecraft") || ns.equals("gtceu")) continue;
            String path = planksId.getPath();
            if (!path.endsWith("_planks")) continue;
            String name = path.substring(0, path.length() - 7);
            buildWood(ns, name, name, planksId, logs, strippedLogs, found, forms);
        }
        for (String[] irr : IRREGULAR) {
            ResourceLocation planksId = ResourceLocation.fromNamespaceAndPath(irr[0], irr[1]);
            if (BuiltInRegistries.ITEM.getOptional(planksId).isPresent()) {
                buildWood(irr[0], irr[2], irr[3], planksId, logs, strippedLogs, found, forms);
            }
        }
        entries = found;
        WoodFormRemovals.forms = forms;
    }

    private static void buildWood(String ns, String formStem, String logStem, ResourceLocation planksId,
                                  List<ResourceLocation> logs, List<ResourceLocation> strippedLogs,
                                  List<WoodTypeEntry> found, Set<ResourceLocation> forms) {
        Item planks = BuiltInRegistries.ITEM.getOptional(planksId).orElse(null);
        if (planks == null) return;

        boolean variant = false;
        Item log = firstOf(ns, logStem + "_log", logStem + "_stem");
        Item strippedLog = firstOf(ns, "stripped_" + logStem + "_log", "stripped_" + logStem + "_stem");
        if (log == null) {
            log = variantLog(logs, ns, logStem);
            strippedLog = variantLog(strippedLogs, ns, logStem);
            variant = true;
        }
        if (log == null) {
            Item bareLog = firstOf(ns, logStem, logStem + "_block");
            Item bareStripped = firstOf(ns, "stripped_" + logStem, "stripped_" + logStem + "_block");
            if (bareLog != null && bareStripped != null) {
                log = bareLog;
                strippedLog = bareStripped;
            }
        }
        if (log == null) return;

        Item wood = firstOf(ns, logStem + "_wood", logStem + "_hyphae");
        Item strippedWood = firstOf(ns, "stripped_" + logStem + "_wood", "stripped_" + logStem + "_hyphae");
        Item door = form(ns, formStem, "door");
        Item trapdoor = form(ns, formStem, "trapdoor");
        Item slab = form(ns, formStem, "slab");
        Item fence = form(ns, formStem, "fence");
        Item fenceGate = form(ns, formStem, "fence_gate");
        Item stairs = form(ns, formStem, "stairs");
        Item sign = form(ns, formStem, "sign");
        Item hangingSign = form(ns, formStem, "hanging_sign");
        Item boat = firstOf(ns, formStem + "_boat", formStem + "_raft");
        Item chestBoat = firstOf(ns, formStem + "_chest_boat", formStem + "_chest_raft");
        Item button = form(ns, formStem, "button");
        Item pressurePlate = form(ns, formStem, "pressure_plate");

        if (slab == null) {
            sign = null;
            hangingSign = null;
            boat = null;
            pressurePlate = null;
        }
        if (sign == null || strippedLog == null) hangingSign = null;
        if (boat == null) chestBoat = null;
        if (pressurePlate == null) button = null;

        String woodName = ns + "_" + formStem;
        WoodTypeEntry.Builder builder = new WoodTypeEntry.Builder(ns, woodName)
                .material(GTMaterials.Wood)
                .generateLogToPlankRecipe(false)
                .addSlabRecipe()
                .addStairsRecipe()
                .logTag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ns, logStem + "_logs")))
                .planks(planks, null)
                .log(log);
        if (strippedLog != null) builder.strippedLog(strippedLog);
        if (wood != null) builder.wood(wood);
        if (strippedWood != null) builder.strippedWood(strippedWood);

        addForm(builder::door, forms, door);
        addForm(builder::trapdoor, forms, trapdoor);
        addForm(builder::slab, forms, slab);
        addForm(builder::fence, forms, fence);
        addForm(builder::fenceGate, forms, fenceGate);
        addForm(builder::stairs, forms, stairs);
        addForm(builder::sign, forms, sign);
        addForm(builder::hangingSign, forms, hangingSign);
        addForm(builder::boat, forms, boat);
        addForm(builder::chestBoat, forms, chestBoat);
        addForm(builder::button, forms, button);
        addForm(builder::pressurePlate, forms, pressurePlate);
        forms.add(planksId);

        found.add(builder.build());

        List<ItemLike> inputs = new ArrayList<>();
        inputs.add(log);
        if (strippedLog != null) inputs.add(strippedLog);
        if (wood != null) inputs.add(wood);
        if (strippedWood != null) inputs.add(strippedWood);
        if (variant) collectVariants(ns, logStem, inputs);
        plankInputs.put(woodName, inputs);
    }

    public static void registerLogToPlankRecipes(RecipeOutput provider) {
        boolean nerf = ConfigHolder.INSTANCE.recipes.nerfWoodCrafting;
        for (WoodTypeEntry e : entries()) {
            Ingredient logs = logIngredient(e);
            VanillaRecipeHelper.addShapelessRecipe(provider, e.woodName + "_planks",
                    new ItemStack(e.planks, nerf ? 2 : 4), logs);
            VanillaRecipeHelper.addShapedRecipe(provider, e.woodName + "_planks_saw",
                    new ItemStack(e.planks, nerf ? 4 : 6), "s", "L", 'L', logs);
            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder(e.woodName + "_planks")
                    .inputItems(logs)
                    .outputItems(new ItemStack(e.planks, 6))
                    .outputItems(TagPrefix.dust, GTMaterials.Wood, 2)
                    .duration(200).EUt(GTValues.VA[GTValues.ULV])
                    .save(provider);
        }
    }

    private static Ingredient logIngredient(WoodTypeEntry e) {
        List<Ingredient> parts = new ArrayList<>();
        if (e.logTag != null) parts.add(Ingredient.of(e.logTag));
        List<ItemLike> items = plankInputs.getOrDefault(e.woodName, List.of());
        if (!items.isEmpty()) parts.add(Ingredient.of(items.toArray(new ItemLike[0])));
        if (parts.isEmpty()) return Ingredient.of(e.planks);
        if (parts.size() == 1) return parts.get(0);
        return CompoundIngredient.of(parts.toArray(new Ingredient[0]));
    }

    private static void collectVariants(String ns, String logStem, List<ItemLike> out) {
        String[] suffixes = { "_log", "_stem", "_wood", "_hyphae" };
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            if (!id.getNamespace().equals(ns)) continue;
            String p = id.getPath();
            for (String suffix : suffixes) {
                if (p.equals(logStem + suffix) || p.endsWith("_" + logStem + suffix)) {
                    Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                    if (item != null && !out.contains(item)) out.add(item);
                    break;
                }
            }
        }
    }

    private interface FormSetter {

        WoodTypeEntry.Builder set(Item item, String recipeName);
    }

    private static void addForm(FormSetter setter, Set<ResourceLocation> forms, Item item) {
        if (item == null) return;
        setter.set(item, null);
        forms.add(BuiltInRegistries.ITEM.getKey(item));
    }

    private static Item form(String ns, String name, String suffix) {
        return firstOf(ns, name + "_" + suffix, name + "_planks_" + suffix);
    }

    private static Item variantLog(List<ResourceLocation> candidates, String ns, String name) {
        String suffixLog = "_" + name + "_log";
        String suffixStem = "_" + name + "_stem";
        for (ResourceLocation id : candidates) {
            if (!id.getNamespace().equals(ns)) continue;
            String path = id.getPath();
            if (path.endsWith(suffixLog) || path.endsWith(suffixStem)) {
                return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            }
        }
        return null;
    }

    private static Item firstOf(String ns, String... paths) {
        for (String path : paths) {
            Item item = lookup(ns, path);
            if (item != null) return item;
        }
        return null;
    }

    private static Item lookup(String ns, String path) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(ns, path)).orElse(null);
    }
}
