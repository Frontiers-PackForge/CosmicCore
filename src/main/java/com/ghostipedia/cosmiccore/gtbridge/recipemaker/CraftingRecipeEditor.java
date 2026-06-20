package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Hand-built editor for vanilla 3x3 crafting (shaped + shapeless) - the default view of the Recipe Forge. Shaped
 * recipes are trimmed to their bounding box and compiled to a pattern + key map; shapeless just lists the
 * ingredients. Exports KubeJS event.shaped / event.shapeless.
 */
public final class CraftingRecipeEditor {

    private CraftingRecipeEditor() {}

    public static void build(WidgetGroup editor) {
        CustomItemStackHandler grid = new CustomItemStackHandler(9);
        CustomItemStackHandler output = new CustomItemStackHandler(1);
        boolean[] shapeless = { false };
        String[] recipeId = { "" };

        editor.addWidget(new LabelWidget(0, 0, "Crafting"));
        editor.addWidget(new LabelWidget(0, 11, "id"));
        editor.addWidget(new TextFieldWidget(16, 10, 106, 12, () -> recipeId[0], s -> recipeId[0] = s));
        for (int i = 0; i < 9; i++) {
            editor.addWidget(new ConfigurableItemSlot(grid, i, (i % 3) * 18, 28 + (i / 3) * 18));
        }
        editor.addWidget(new LabelWidget(60, 50, "->"));
        editor.addWidget(new ConfigurableItemSlot(output, 0, 74, 46));

        editor.addWidget(new LabelWidget(0, 94, "Mode"));
        editor.addWidget(new SelectorWidget(34, 92, 84, 14, List.of("shaped", "shapeless"), 0)
                .setOnChanged(name -> shapeless[0] = name.equals("shapeless"))
                .setSupplier(() -> shapeless[0] ? "shapeless" : "shaped"));

        editor.addWidget(new ExportButtonWidget(0, 192, 120, 16, GuiTextures.VANILLA_BUTTON,
                () -> export(grid, output, shapeless[0], recipeId[0])));
        editor.addWidget(new LabelWidget(34, 196, "Copy KubeJS"));
    }

    private static String export(CustomItemStackHandler grid, CustomItemStackHandler output, boolean shapeless,
                                 String recipeId) {
        ItemStack out = output.getStackInSlot(0);
        if (out.isEmpty()) return "// set a crafting output";
        String result = itemString(out);
        String recipe = shapeless ? shapeless(grid, result) : shaped(grid, result);
        if (recipe.startsWith("//")) return recipe;
        if (!recipeId.isBlank()) recipe += ".id('" + recipeId.trim() + "')";
        return recipe;
    }

    private static String shapeless(CustomItemStackHandler grid, String result) {
        StringJoiner inputs = new StringJoiner(", ");
        for (int i = 0; i < 9; i++) {
            ItemStack stack = grid.getStackInSlot(i);
            if (!stack.isEmpty()) inputs.add(itemString(stack));
        }
        if (inputs.length() == 0) return "// add ingredients";
        return "event.shapeless(" + result + ", [" + inputs + "])";
    }

    private static String shaped(CustomItemStackHandler grid, String result) {
        int minRow = 3;
        int maxRow = -1;
        int minCol = 3;
        int maxCol = -1;
        for (int i = 0; i < 9; i++) {
            if (grid.getStackInSlot(i).isEmpty()) continue;
            minRow = Math.min(minRow, i / 3);
            maxRow = Math.max(maxRow, i / 3);
            minCol = Math.min(minCol, i % 3);
            maxCol = Math.max(maxCol, i % 3);
        }
        if (maxRow < 0) return "// add ingredients";

        Map<String, Character> keyMap = new LinkedHashMap<>();
        char next = 'A';
        List<String> rows = new ArrayList<>();
        for (int r = minRow; r <= maxRow; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = minCol; c <= maxCol; c++) {
                ItemStack stack = grid.getStackInSlot(r * 3 + c);
                if (stack.isEmpty()) {
                    row.append(' ');
                    continue;
                }
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                Character ch = keyMap.get(id);
                if (ch == null) {
                    ch = next++;
                    keyMap.put(id, ch);
                }
                row.append(ch.charValue());
            }
            rows.add(row.toString());
        }

        StringJoiner pattern = new StringJoiner("', '", "'", "'");
        for (String row : rows) pattern.add(row);
        StringJoiner key = new StringJoiner(", ");
        for (Map.Entry<String, Character> entry : keyMap.entrySet()) {
            key.add(entry.getValue() + ": '" + entry.getKey() + "'");
        }
        return "event.shaped(" + result + ", [" + pattern + "], {" + key + "})";
    }

    private static String itemString(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return stack.getCount() > 1 ? "'" + stack.getCount() + "x " + id + "'" : "'" + id + "'";
    }
}
