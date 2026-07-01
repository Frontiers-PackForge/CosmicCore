package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.ghostipedia.cosmiccore.gtbridge.recipemaker.RecipeMakerBehavior.FoodState;
import com.ghostipedia.cosmiccore.gtbridge.recipemaker.RecipeMakerBehavior.State;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class FoodExporter {

    private FoodExporter() {}

    public static String export(State state) {
        ItemStack stack = state.itemOut.getStackInSlot(0);
        if (stack.isEmpty()) return "// place a food item in the slot";
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        FoodState f = state.food;

        StringBuilder sb = new StringBuilder();
        sb.append("CosmicFood.define('").append(id).append("', food => {\n");
        sb.append("    food.category('").append(f.category[0] == 1 ? "brew" : "food").append("')");
        appendNumber(sb, "health", f.health[0]);
        appendNumber(sb, "regen", f.regen[0]);
        if (!f.duration[0].isBlank()) {
            sb.append("\n        .duration('").append(f.duration[0].trim()).append("')");
        }
        for (int i = 0; i < RecipeMakerBehavior.FOOD_ROWS; i++) {
            if (!f.effectId[i].isBlank()) {
                sb.append("\n        .effect('").append(f.effectId[i].trim()).append("', ")
                        .append(asInt(f.effectAmp[i])).append(")");
            }
        }
        for (int i = 0; i < RecipeMakerBehavior.FOOD_ROWS; i++) {
            if (!f.attrId[i].isBlank()) {
                sb.append("\n        .attribute('").append(f.attrId[i].trim()).append("', ")
                        .append(asNumber(f.attrAmount[i]));
                String op = operation(f.attrOp[i]);
                if (!op.equals("add")) sb.append(", '").append(op).append("'");
                sb.append(")");
            }
        }
        for (int i = 0; i < RecipeMakerBehavior.FOOD_ROWS; i++) {
            if (!f.behLabel[i].isBlank()) {
                sb.append("\n        .behavior('").append(f.behGlyph[i]).append("', '").append(f.behColor[i])
                        .append("', '").append(f.behLabel[i]).append("', '").append(f.behValue[i]).append("')");
            }
        }
        sb.append("\n})");
        return sb.toString();
    }

    private static void appendNumber(StringBuilder sb, String method, String value) {
        if (parse(value) != 0) {
            sb.append("\n        .").append(method).append("(").append(trimNumber(value)).append(")");
        }
    }

    private static String operation(int op) {
        return switch (op) {
            case 1 -> "percent";
            case 2 -> "base_percent";
            default -> "add";
        };
    }

    private static String asInt(String value) {
        return String.valueOf((int) parse(value));
    }

    private static String asNumber(String value) {
        return trimNumber(String.valueOf(parse(value)));
    }

    private static double parse(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String trimNumber(String value) {
        String trimmed = value.trim();
        return trimmed.endsWith(".0") ? trimmed.substring(0, trimmed.length() - 2) : trimmed;
    }
}
