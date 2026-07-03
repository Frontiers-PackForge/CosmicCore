package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.ghostipedia.cosmiccore.gtbridge.recipemaker.RecipeMakerBehavior.State;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Hand-built editor for vanilla 3x3 crafting (shaped + shapeless). Uses the first 9 slots of the shared input pool as
 * the grid and output slot 0 as the result. Exports KubeJS event.shaped / event.shapeless.
 */
public final class CraftingRecipeEditor {

    private CraftingRecipeEditor() {}

    public static void build(PanelSyncManager sm, ListWidget editor, State state, RecipeMakerControl control,
                             IntSyncValue selSlot, IntSyncValue selSide, IntSyncValue selType,
                             IPanelHandler slotPanel) {
        Flow grid = Flow.column().coverChildren();
        for (int r = 0; r < 3; r++) {
            Flow row = Flow.row().coverChildren();
            for (int c = 0; c < 3; c++) {
                int i = r * 3 + c;
                row.child(new ConfigurableItemSlot(RecipeMakerBehavior.itemSync(sm, "ii", i, state.itemIn), () -> {
                    selSlot.setIntValue(i, true, true);
                    selSide.setIntValue(0, true, true);
                    selType.setIntValue(0, true, true);
                    slotPanel.openPanel();
                }).size(18));
            }
            grid.child(row);
        }
        editor.child(Flow.row().coverChildrenHeight().widthRel(1f).childPadding(6)
                .child(grid)
                .child(new TextWidget<>(Text.str("->")))
                .child(new ConfigurableItemSlot(RecipeMakerBehavior.itemSync(sm, "io", 0, state.itemOut), () -> {
                    selSlot.setIntValue(0, true, true);
                    selSide.setIntValue(1, true, true);
                    selType.setIntValue(0, true, true);
                    slotPanel.openPanel();
                }).size(18)));

        editor.child(RecipeMakerBehavior.fieldRow("Mode", new CycleButtonWidget().background(GTGuiTextures.BUTTON)
                .stateCount(2)
                .stateOverlay(0, Text.str("shaped").alignment(Alignment.TopLeft).asTextIcon())
                .stateOverlay(1, Text.str("shapeless").alignment(Alignment.TopLeft).asTextIcon())
                .value(RecipeMakerBehavior.intSync(sm, "cmode", () -> state.craftMode[0], v -> state.craftMode[0] = v))
                .expanded().height(14)));

        control.setExporter(() -> export(state));
        editor.child(RecipeMakerBehavior.copyButton(control));
    }

    private static String export(State state) {
        ItemStack out = state.itemOut.getStackInSlot(0);
        if (out.isEmpty()) return "// set a crafting output";
        String result = itemString(out);
        boolean shapeless = state.craftMode[0] == 1;
        String recipe = shapeless ? shapeless(state) : shaped(state);
        if (recipe.startsWith("//")) return recipe;
        if (!state.recipeId[0].isBlank()) recipe += ".id('" + state.recipeId[0].trim() + "')";
        return recipe;
    }

    private static String shapeless(State state) {
        StringJoiner inputs = new StringJoiner(", ");
        for (int i = 0; i < 9; i++) {
            ItemStack stack = state.itemIn.getStackInSlot(i);
            if (!stack.isEmpty()) inputs.add("'" + craftIngredient(state, i) + "'");
        }
        if (inputs.length() == 0) return "// add ingredients";
        return "event.shapeless(" + itemString(state.itemOut.getStackInSlot(0)) + ", [" + inputs + "])";
    }

    private static String shaped(State state) {
        int minRow = 3;
        int maxRow = -1;
        int minCol = 3;
        int maxCol = -1;
        for (int i = 0; i < 9; i++) {
            if (state.itemIn.getStackInSlot(i).isEmpty()) continue;
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
                ItemStack stack = state.itemIn.getStackInSlot(r * 3 + c);
                if (stack.isEmpty()) {
                    row.append(' ');
                    continue;
                }
                String ingredient = craftIngredient(state, r * 3 + c);
                Character ch = keyMap.get(ingredient);
                if (ch == null) {
                    ch = next++;
                    keyMap.put(ingredient, ch);
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
        return "event.shaped(" + itemString(state.itemOut.getStackInSlot(0)) + ", [" + pattern + "], {" + key + "})";
    }

    private static String itemString(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return stack.getCount() > 1 ? "'" + stack.getCount() + "x " + id + "'" : "'" + id + "'";
    }

    private static String craftIngredient(State state, int i) {
        String tag = state.inTag[i];
        if (tag != null && !tag.isEmpty()) return "#" + tag;
        return BuiltInRegistries.ITEM.getKey(state.itemIn.getStackInSlot(i).getItem()).toString();
    }
}
