package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * Turns a {@link RecipeDraft} into a single GTCEu KubeJS recipe-event line that can be pasted into a server
 * script. Output shape: {@code event.recipes.<namespace>.<path>('id').itemInputs(...).itemOutputs(...)
 * .inputFluids(...).outputFluids(...).chancedOutput(...).EUt(v, a).duration(t)}. The recipe-type id directly
 * names the KubeJS accessor, since GTCEu registers a recipe schema per type id.
 */
public final class KubeJsRecipeExporter {

    private KubeJsRecipeExporter() {}

    public static String export(RecipeDraft draft, String recipeId) {
        if (draft.recipeType == null) return "// no recipe type selected";

        var typeId = draft.recipeType.registryName;
        String idArg = recipeId == null || recipeId.isBlank() ? "" : "'" + recipeId.trim() + "'";
        List<String> lines = new ArrayList<>();
        lines.add("event.recipes." + typeId.getNamespace() + "." + typeId.getPath() + "(" + idArg + ")");

        StringJoiner consumed = new StringJoiner(", ");
        List<String> extraInputs = new ArrayList<>();
        for (int i = 0; i < draft.itemInputs.size(); i++) {
            ItemStack stack = draft.itemInputs.get(i);
            if (stack == null || stack.isEmpty()) continue;
            boolean noConsume = i < draft.itemInputNotConsumed.size() && draft.itemInputNotConsumed.get(i);
            int chance = i < draft.itemInputChances.size() ? draft.itemInputChances.get(i) : RecipeDraft.GUARANTEED;
            int boost = i < draft.itemInputBoosts.size() ? draft.itemInputBoosts.get(i) : 0;
            String arg = tagOrItem(draft.itemInputTags, i, stack);
            if (noConsume) {
                extraInputs.add(".notConsumable(" + arg + ")");
            } else if (chance < RecipeDraft.GUARANTEED) {
                extraInputs.add(".chancedInput(" + arg + ", " + chance + ", " + boost + ")");
            } else {
                consumed.add(arg);
            }
        }
        if (consumed.length() > 0) lines.add(".itemInputs(" + consumed + ")");
        lines.addAll(extraInputs);

        StringJoiner guaranteed = new StringJoiner(", ");
        List<String> chanced = new ArrayList<>();
        for (int i = 0; i < draft.itemOutputs.size(); i++) {
            ItemStack stack = draft.itemOutputs.get(i);
            if (stack == null || stack.isEmpty()) continue;
            int chance = draft.chanceOf(i);
            if (chance >= RecipeDraft.GUARANTEED) {
                guaranteed.add(itemString(stack));
            } else {
                int boost = i < draft.itemOutputBoosts.size() ? draft.itemOutputBoosts.get(i) : 0;
                chanced.add(".chancedOutput(" + itemString(stack) + ", " + chance + ", " + boost + ")");
            }
        }
        if (guaranteed.length() > 0) lines.add(".itemOutputs(" + guaranteed + ")");
        lines.addAll(chanced);

        for (int i = 0; i < draft.fluidInputs.size(); i++) {
            FluidStack fluid = draft.fluidInputs.get(i);
            if (fluid == null || fluid.isEmpty()) continue;
            int chance = i < draft.fluidInputChances.size() ? draft.fluidInputChances.get(i) : RecipeDraft.GUARANTEED;
            int boost = i < draft.fluidInputBoosts.size() ? draft.fluidInputBoosts.get(i) : 0;
            if (chance < RecipeDraft.GUARANTEED) {
                lines.add(".chancedInput(" + fluidArgs(fluid) + ", " + chance + ", " + boost + ")");
            } else {
                lines.add(".inputFluids(" + fluidArgs(fluid) + ")");
            }
        }
        for (int i = 0; i < draft.fluidOutputs.size(); i++) {
            FluidStack fluid = draft.fluidOutputs.get(i);
            if (fluid == null || fluid.isEmpty()) continue;
            int chance = i < draft.fluidOutputChances.size() ? draft.fluidOutputChances.get(i) : RecipeDraft.GUARANTEED;
            int boost = i < draft.fluidOutputBoosts.size() ? draft.fluidOutputBoosts.get(i) : 0;
            if (chance < RecipeDraft.GUARANTEED) {
                lines.add(".chancedOutput(" + fluidArgs(fluid) + ", " + chance + ", " + boost + ")");
            } else {
                lines.add(".outputFluids(" + fluidArgs(fluid) + ")");
            }
        }

        lines.addAll(draft.extraLines);

        if (draft.rawEU) {
            lines.add(".EUt(" + draft.rawVoltage + ", " + draft.amperage + ")");
        } else {
            lines.add(".EUt(GTValues." + draft.voltageArray + "[GTValues." + GTValues.VN[draft.voltageTier] + "], " +
                    draft.amperage + ")");
        }
        if (draft.blastTemp > 0) lines.add(".blastFurnaceTemp(" + draft.blastTemp + ")");
        if (draft.cwu > 0) lines.add(".CWUt(" + draft.cwu + ")");
        if (draft.cleanroom != null && !draft.cleanroom.isEmpty() && !draft.cleanroom.equals("none")) {
            lines.add(".cleanroom(CleanroomType." + draft.cleanroom.toUpperCase(Locale.ROOT) + ")");
        }
        if (draft.dimension != null && !draft.dimension.isEmpty()) {
            lines.add(".dimension('" + draft.dimension + "')");
        }
        lines.add(".duration(" + draft.duration + ")");

        return String.join("\n", lines);
    }

    private static String itemString(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return stack.getCount() > 1 ? "'" + stack.getCount() + "x " + id + "'" : "'" + id + "'";
    }

    private static String tagOrItem(List<String> tags, int i, ItemStack stack) {
        String tag = i < tags.size() ? tags.get(i) : "";
        if (tag == null || tag.isEmpty()) return itemString(stack);
        return stack.getCount() > 1 ? "'" + stack.getCount() + "x #" + tag + "'" : "'#" + tag + "'";
    }

    private static String fluidArgs(FluidStack stack) {
        return "'" + BuiltInRegistries.FLUID.getKey(stack.getFluid()) + " " + stack.getAmount() + "'";
    }
}
