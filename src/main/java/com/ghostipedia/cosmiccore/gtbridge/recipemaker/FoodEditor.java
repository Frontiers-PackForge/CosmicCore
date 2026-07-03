package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.ghostipedia.cosmiccore.gtbridge.recipemaker.RecipeMakerBehavior.FoodState;
import com.ghostipedia.cosmiccore.gtbridge.recipemaker.RecipeMakerBehavior.State;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FoodEditor {

    private FoodEditor() {}

    public static void build(PanelSyncManager sm, ListWidget editor, State state, RecipeMakerControl control,
                             IPanelHandler effectPanel, IPanelHandler attrPanel) {
        FoodState food = state.food;
        IntSyncValue pick = RecipeMakerBehavior.intSync(sm, "fpick", () -> food.pickRow[0], v -> food.pickRow[0] = v);

        editor.child(new TextWidget<>(Text.dynamic(() -> Component.literal(vanillaLine(state)))).height(9));
        editor.child(new TextWidget<>(Text.dynamic(() -> Component.literal(autoLine(state)))).height(9));

        editor.child(Flow.row().coverChildrenHeight().widthRel(1f).childPadding(6)
                .child(new ConfigurableItemSlot(RecipeMakerBehavior.itemSync(sm, "fitem", 0, state.itemOut), () -> {})
                        .size(18))
                .child(new CycleButtonWidget().background(GTGuiTextures.BUTTON).stateCount(2)
                        .stateOverlay(0, Text.str("food").alignment(Alignment.TopLeft).asTextIcon())
                        .stateOverlay(1, Text.str("brew").alignment(Alignment.TopLeft).asTextIcon())
                        .value(RecipeMakerBehavior.intSync(sm, "fcat", () -> food.category[0],
                                v -> food.category[0] = v))
                        .expanded().height(14)));

        editor.child(RecipeMakerBehavior.fieldRow("Health",
                field(sm, "fhp", () -> food.health[0], v -> food.health[0] = v).expanded()));
        editor.child(RecipeMakerBehavior.fieldRow("Regen",
                field(sm, "freg", () -> food.regen[0], v -> food.regen[0] = v).expanded()));
        editor.child(RecipeMakerBehavior.fieldRow("Dur",
                field(sm, "fdur", () -> food.duration[0], v -> food.duration[0] = v).expanded()));

        editor.child(new TextWidget<>(Text.str("Effects  (effect, level)")).height(9));
        for (int i = 0; i < RecipeMakerBehavior.FOOD_ROWS; i++) {
            int r = i;
            editor.child(Flow.row().coverChildrenHeight().widthRel(1f).childPadding(2)
                    .child(pickerButton(() -> display(food.effectId[r], "effect..."),
                            () -> {
                                pick.setIntValue(r, true, true);
                                effectPanel.openPanel();
                            }).expanded())
                    .child(field(sm, "fefamp" + r, () -> food.effectAmp[r], v -> food.effectAmp[r] = v).width(26)));
        }

        editor.child(new TextWidget<>(Text.str("Attributes  (attribute, amount, op)")).height(9));
        for (int i = 0; i < RecipeMakerBehavior.FOOD_ROWS; i++) {
            int r = i;
            editor.child(Flow.row().coverChildrenHeight().widthRel(1f).childPadding(2)
                    .child(pickerButton(() -> display(food.attrId[r], "attribute..."),
                            () -> {
                                pick.setIntValue(r, true, true);
                                attrPanel.openPanel();
                            }).expanded())
                    .child(field(sm, "fatam" + r, () -> food.attrAmount[r], v -> food.attrAmount[r] = v).width(26))
                    .child(new CycleButtonWidget().background(GTGuiTextures.BUTTON).stateCount(3)
                            .stateOverlay(0, Text.str("+").alignment(Alignment.Center).asTextIcon())
                            .stateOverlay(1, Text.str("%").alignment(Alignment.Center).asTextIcon())
                            .stateOverlay(2, Text.str("b%").alignment(Alignment.Center).asTextIcon())
                            .value(RecipeMakerBehavior.intSync(sm, "fatop" + r, () -> food.attrOp[r],
                                    v -> food.attrOp[r] = v))
                            .width(26).height(14)));
        }

        editor.child(new TextWidget<>(Text.str("Behaviors  (glyph, color, label, value)")).height(9));
        for (int i = 0; i < RecipeMakerBehavior.FOOD_ROWS; i++) {
            int r = i;
            editor.child(Flow.row().coverChildrenHeight().widthRel(1f).childPadding(2)
                    .child(field(sm, "fbg" + r, () -> food.behGlyph[r], v -> food.behGlyph[r] = v).width(20))
                    .child(field(sm, "fbc" + r, () -> food.behColor[r], v -> food.behColor[r] = v).width(48))
                    .child(field(sm, "fbl" + r, () -> food.behLabel[r], v -> food.behLabel[r] = v).expanded())
                    .child(field(sm, "fbv" + r, () -> food.behValue[r], v -> food.behValue[r] = v).expanded()));
        }

        control.setExporter(() -> FoodExporter.export(state));
        editor.child(RecipeMakerBehavior.copyButton(control));
    }

    static ModularPanel<?> buildEffectPicker(PanelSyncManager sm, State state, ModularPanel<?> parent,
                                             IPanelHandler self) {
        return buildPicker(sm, parent, self, "rm_feff_dialog", sortedIds(BuiltInRegistries.MOB_EFFECT.keySet()),
                RecipeMakerBehavior.strSync(sm, "feffval", () -> state.food.effectId[state.food.pickRow[0]],
                        v -> state.food.effectId[state.food.pickRow[0]] = v));
    }

    static ModularPanel<?> buildAttrPicker(PanelSyncManager sm, State state, ModularPanel<?> parent,
                                           IPanelHandler self) {
        return buildPicker(sm, parent, self, "rm_fattr_dialog", sortedIds(BuiltInRegistries.ATTRIBUTE.keySet()),
                RecipeMakerBehavior.strSync(sm, "fattrval", () -> state.food.attrId[state.food.pickRow[0]],
                        v -> state.food.attrId[state.food.pickRow[0]] = v));
    }

    private static ModularPanel<?> buildPicker(PanelSyncManager sm, ModularPanel<?> parent, IPanelHandler self,
                                               String name, List<ResourceLocation> ids, StringSyncValue value) {
        StringValue search = new StringValue("");
        ListWidget body = new ListWidget<>();
        body.collapseDisabledChildren().expanded().widthRel(1f);
        for (ResourceLocation id : ids) {
            body.child(new ButtonWidget<>().background(GTGuiTextures.BUTTON).widthRel(1f).height(12)
                    .setEnabledIf(w -> RecipeMakerBehavior.matches(id.toString(), search.getStringValue()))
                    .onMousePressed((context, button) -> {
                        value.setStringValue(id.toString(), true, true);
                        self.closePanel();
                        return true;
                    })
                    .child(new TextWidget<>(Text.str(id.toString())).textAlign(Alignment.CenterLeft).sizeRel(1f)
                            .padding(3, 0, 0, 0)));
        }
        Flow content = Flow.column().sizeRel(1f).padding(4).childPadding(2)
                .child(new TextFieldWidget().value(search).widthRel(1f).height(12).autoUpdateOnChange(true))
                .child(body);
        return RecipeMakerBehavior.popout(name, parent, 220, 210, content);
    }

    private static ButtonWidget<?> pickerButton(Supplier<String> label, Runnable action) {
        return new ButtonWidget<>().background(GTGuiTextures.BUTTON).height(14)
                .onMousePressed((context, button) -> {
                    action.run();
                    return true;
                })
                .child(new TextWidget<>(Text.dynamic(() -> Component.literal(label.get())))
                        .textAlign(Alignment.CenterLeft).sizeRel(1f).padding(3, 0, 0, 0));
    }

    private static TextFieldWidget field(PanelSyncManager sm, String key, Supplier<String> getter,
                                         Consumer<String> setter) {
        return new TextFieldWidget().value(RecipeMakerBehavior.strSync(sm, key, getter, setter)).height(12);
    }

    private static List<ResourceLocation> sortedIds(Set<ResourceLocation> keys) {
        List<ResourceLocation> ids = new ArrayList<>(keys);
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        return ids;
    }

    private static String display(String id, String placeholder) {
        if (id == null || id.isEmpty()) return placeholder;
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    private static String vanillaLine(State state) {
        ItemStack stack = state.itemOut.getStackInSlot(0);
        if (stack.isEmpty()) return "Vanilla: (place a food item)";
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) return "Vanilla: not a food item";
        return "Vanilla: " + food.nutrition() + " hunger, " + String.format("%.1f", food.saturation()) + " sat";
    }

    private static String autoLine(State state) {
        ItemStack stack = state.itemOut.getStackInSlot(0);
        FoodProperties food = stack.isEmpty() ? null : stack.get(DataComponents.FOOD);
        if (food == null) return "Auto: -";
        int nutrition = food.nutrition();
        float saturation = food.saturation();
        double hearts = Math.max(nutrition, 2) / 2.0;
        double regen = Mth.clamp(nutrition * 0.10, 0.25, 2.0);
        int durationTicks = Mth.clamp((int) ((nutrition + saturation) * 600), 6000, 72000);
        return "Auto: " + hearts + " ♥  " + String.format("%.2f", regen) + "/s  " + durationTicks / 1200 + "m";
    }
}
