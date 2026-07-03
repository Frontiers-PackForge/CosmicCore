package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.ghostipedia.cosmiccore.gtbridge.recipemaker.RecipeCodecReader.JsonField;
import com.ghostipedia.cosmiccore.gtbridge.recipemaker.RecipeMakerBehavior.State;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generic editor for any recipe type with no hand-built or GregTech editor. A sampled recipe (from
 * {@link RecipeCodecReader}) is the template: numeric/string fields pre-fill and edit, item/fluid fields map onto the
 * shared slot pool by role. Both the layout and the export walk the same deterministic field-to-pool allocation, so
 * the export reads exactly the slots the user filled. Emits a builder call when the type ships a KubeJS schema, else
 * {@code event.custom({...})}. Export runs server-side where the schema storage is reachable.
 */
public final class CodecRecipeEditor {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int COLS = 6;

    private CodecRecipeEditor() {}

    private record Alloc(JsonField field, Kind kind, int role, int start, int count, int valIndex) {}

    public static void build(PanelSyncManager sm, ListWidget editor, Player player, String typeId, State state,
                             RecipeMakerControl control, IntSyncValue selSlot, IntSyncValue selSide,
                             IntSyncValue selType, IPanelHandler slotPanel) {
        ResourceLocation rl = ResourceLocation.tryParse(typeId);
        JsonObject sample = rl == null ? null : RecipeCodecReader.sample(player, rl);
        List<Alloc> allocs = rl == null ? List.of() : allocate(player, rl);

        editor.child(new TextWidget<>(Text.str(shortName(typeId))).height(9));
        if (allocs.isEmpty() || sample == null) {
            editor.child(new TextWidget<>(Text.str("no sample recipe")).height(9));
            return;
        }

        for (Alloc a : allocs) {
            String label = a.field().output() ? a.field().name() + " (out)" : a.field().name();
            switch (a.kind()) {
                case ITEM -> {
                    editor.child(new TextWidget<>(Text.str(label)).height(9));
                    CustomItemStackHandler handler = a.role() == 1 ? state.itemOut : state.itemIn;
                    editor.child(itemSlots(sm, a.role() == 1 ? "io" : "ii", handler, a.start(), a.count(), a.role(),
                            selSlot, selSide, selType, slotPanel));
                }
                case FLUID -> {
                    editor.child(new TextWidget<>(Text.str(label)).height(9));
                    FluidTank[] tanks = a.role() == 1 ? state.fluidOut : state.fluidIn;
                    editor.child(fluidSlots(sm, a.role() == 1 ? "fo" : "fi", tanks, a.start(), a.count(), a.role(),
                            selSlot, selSide, selType, slotPanel));
                }
                case BOOLEAN -> {
                    int vi = a.valIndex();
                    if (state.codecVals[vi].isEmpty()) {
                        state.codecVals[vi] = sample.has(a.field().name()) ?
                                sample.get(a.field().name()).getAsString() : "false";
                    }
                    editor.child(RecipeMakerBehavior.fieldRow(label,
                            new CycleButtonWidget().background(GTGuiTextures.BUTTON).stateCount(2)
                                    .stateOverlay(0, Text.str("false").alignment(Alignment.TopLeft).asTextIcon())
                                    .stateOverlay(1, Text.str("true").alignment(Alignment.TopLeft).asTextIcon())
                                    .value(RecipeMakerBehavior.intSync(sm, "cvb" + vi,
                                            () -> "true".equals(state.codecVals[vi]) ? 1 : 0,
                                            v -> state.codecVals[vi] = v == 1 ? "true" : "false"))
                                    .expanded().height(14)));
                }
                case FIELD -> {
                    int vi = a.valIndex();
                    if (state.codecVals[vi].isEmpty()) {
                        state.codecVals[vi] = sample.has(a.field().name()) ?
                                sample.get(a.field().name()).getAsString() : "";
                    }
                    editor.child(RecipeMakerBehavior.fieldRow(label, new TextFieldWidget()
                            .value(RecipeMakerBehavior.strSync(sm, "cv" + vi, () -> state.codecVals[vi],
                                    v -> state.codecVals[vi] = v))
                            .expanded().height(12)));
                }
                default -> editor.child(new TextWidget<>(Text.str(label + " (kept)")).height(9));
            }
        }

        control.setExporter(() -> export(player, typeId, state));
        editor.child(RecipeMakerBehavior.copyButton(control));
    }

    private static List<Alloc> allocate(Player player, ResourceLocation rl) {
        List<JsonField> fields = RecipeCodecReader.fields(player, rl);
        List<Alloc> allocs = new ArrayList<>();
        int itemIn = 0;
        int itemOut = 0;
        int fluidIn = 0;
        int fluidOut = 0;
        int val = 0;
        for (JsonField field : fields) {
            Kind kind = uiKind(field);
            int role = field.output() ? 1 : 0;
            switch (kind) {
                case ITEM -> {
                    int count = field.kind() == RecipeCodecReader.Kind.LIST ? listSlots(field) : 1;
                    int start = role == 1 ? itemOut : itemIn;
                    if (start + count <= RecipeMakerBehavior.POOL_ITEM) {
                        allocs.add(new Alloc(field, kind, role, start, count, -1));
                        if (role == 1) itemOut += count;
                        else itemIn += count;
                    }
                }
                case FLUID -> {
                    int count = field.kind() == RecipeCodecReader.Kind.LIST ? listSlots(field) : 1;
                    int start = role == 1 ? fluidOut : fluidIn;
                    if (start + count <= RecipeMakerBehavior.POOL_FLUID) {
                        allocs.add(new Alloc(field, kind, role, start, count, -1));
                        if (role == 1) fluidOut += count;
                        else fluidIn += count;
                    }
                }
                case FIELD, BOOLEAN -> {
                    if (val < RecipeMakerBehavior.POOL_CODEC_VAL) {
                        allocs.add(new Alloc(field, kind, role, -1, 0, val++));
                    }
                }
                default -> allocs.add(new Alloc(field, Kind.KEEP, role, -1, 0, -1));
            }
        }
        return allocs;
    }

    private static String export(Player player, String typeId, State state) {
        ResourceLocation rl = ResourceLocation.tryParse(typeId);
        JsonObject sample = rl == null ? null : RecipeCodecReader.sample(player, rl);
        if (sample == null) return "// no sample recipe";
        JsonObject out = sample.deepCopy();
        out.addProperty("type", typeId);
        for (Alloc a : allocate(player, rl)) {
            String name = a.field().name();
            switch (a.kind()) {
                case ITEM -> {
                    CustomItemStackHandler handler = a.role() == 1 ? state.itemOut : state.itemIn;
                    JsonElement shape = elementShape(sample.get(name));
                    if (a.field().kind() == RecipeCodecReader.Kind.LIST) {
                        JsonArray array = new JsonArray();
                        for (int j = 0; j < a.count(); j++) {
                            ItemStack stack = handler.getStackInSlot(a.start() + j);
                            String tag = a.role() == 0 ? state.inTag[a.start() + j] : "";
                            if (!stack.isEmpty()) array.add(itemElement(shape, stack, tag));
                        }
                        if (!array.isEmpty()) out.add(name, array);
                    } else {
                        ItemStack stack = handler.getStackInSlot(a.start());
                        String tag = a.role() == 0 ? state.inTag[a.start()] : "";
                        if (!stack.isEmpty()) out.add(name, itemElement(shape, stack, tag));
                    }
                }
                case FLUID -> {
                    FluidTank[] tanks = a.role() == 1 ? state.fluidOut : state.fluidIn;
                    JsonElement shape = elementShape(sample.get(name));
                    if (a.field().kind() == RecipeCodecReader.Kind.LIST) {
                        JsonArray array = new JsonArray();
                        for (int j = 0; j < a.count(); j++) {
                            FluidStack fluid = tanks[a.start() + j].getFluid();
                            if (!fluid.isEmpty()) array.add(fluidToJson(shape, fluid));
                        }
                        if (!array.isEmpty()) out.add(name, array);
                    } else {
                        FluidStack fluid = tanks[a.start()].getFluid();
                        if (!fluid.isEmpty()) out.add(name, fluidToJson(shape, fluid));
                    }
                }
                case BOOLEAN -> out.addProperty(name, "true".equals(state.codecVals[a.valIndex()]));
                case FIELD -> {
                    String text = state.codecVals[a.valIndex()].trim();
                    if (text.isEmpty()) break;
                    if (a.field().kind() == RecipeCodecReader.Kind.NUMBER) {
                        if (text.contains(".")) out.addProperty(name, Double.parseDouble(text));
                        else out.addProperty(name, Long.parseLong(text));
                    } else {
                        out.addProperty(name, text);
                    }
                }
                default -> {}
            }
        }
        String builder = buildBuilder(player, typeId, out);
        String result = builder != null ? builder : "event.custom(" + GSON.toJson(out) + ")";
        if (!state.recipeId[0].isBlank()) result += ".id('" + state.recipeId[0].trim() + "')";
        return result;
    }

    private static Flow itemSlots(PanelSyncManager sm, String key, CustomItemStackHandler handler, int start,
                                  int count, int role, IntSyncValue selSlot, IntSyncValue selSide,
                                  IntSyncValue selType, IPanelHandler slotPanel) {
        Flow column = Flow.column().coverChildren();
        Flow row = null;
        for (int j = 0; j < count; j++) {
            if (j % COLS == 0) {
                row = Flow.row().coverChildren();
                column.child(row);
            }
            int slot = start + j;
            row.child(new ConfigurableItemSlot(RecipeMakerBehavior.itemSync(sm, key, slot, handler), () -> {
                selSlot.setIntValue(slot, true, true);
                selSide.setIntValue(role, true, true);
                selType.setIntValue(0, true, true);
                slotPanel.openPanel();
            }).size(18));
        }
        return column;
    }

    private static Flow fluidSlots(PanelSyncManager sm, String key, FluidTank[] tanks, int start, int count, int role,
                                   IntSyncValue selSlot, IntSyncValue selSide, IntSyncValue selType,
                                   IPanelHandler slotPanel) {
        Flow column = Flow.column().coverChildren();
        Flow row = null;
        for (int j = 0; j < count; j++) {
            if (j % COLS == 0) {
                row = Flow.row().coverChildren();
                column.child(row);
            }
            int slot = start + j;
            row.child(new ConfigurableFluidSlot(RecipeMakerBehavior.fluidSync(sm, key, slot, tanks[slot]), () -> {
                selSlot.setIntValue(slot, true, true);
                selSide.setIntValue(role, true, true);
                selType.setIntValue(1, true, true);
                slotPanel.openPanel();
            }).size(18));
        }
        return column;
    }

    private static String buildBuilder(Player player, String typeId, JsonObject out) {
        List<RecipeSchemaReader.SchemaField> ctor = RecipeSchemaReader.constructorKeys(player, typeId);
        if (ctor.isEmpty()) return null;
        int colon = typeId.indexOf(':');
        if (colon < 0) return null;
        String ns = typeId.substring(0, colon);
        String path = typeId.substring(colon + 1);
        Set<String> ctorNames = new HashSet<>();
        List<String> args = new ArrayList<>();
        for (RecipeSchemaReader.SchemaField key : ctor) {
            ctorNames.add(key.name());
            JsonElement value = out.get(key.name());
            if (value == null) return null;
            String arg = kubeArg(value, key);
            if (arg == null) return null;
            args.add(arg);
        }
        StringBuilder sb = new StringBuilder("event.recipes.");
        sb.append(ns).append(".").append(path).append("(").append(String.join(", ", args)).append(")");
        for (RecipeSchemaReader.SchemaField key : RecipeSchemaReader.fields(player, typeId)) {
            if (ctorNames.contains(key.name())) continue;
            JsonElement value = out.get(key.name());
            if (value == null) continue;
            String arg = kubeArg(value, key);
            if (arg == null) return null;
            sb.append(".").append(key.name()).append("(").append(arg).append(")");
        }
        return sb.toString();
    }

    private static String kubeArg(JsonElement value, RecipeSchemaReader.SchemaField key) {
        if (key.list()) {
            List<String> parts = new ArrayList<>();
            if (value.isJsonArray()) {
                for (JsonElement element : value.getAsJsonArray()) {
                    String part = scalarArg(element, key.kind());
                    if (part == null) return null;
                    parts.add(part);
                }
            } else {
                String part = scalarArg(value, key.kind());
                if (part == null) return null;
                parts.add(part);
            }
            return "[" + String.join(", ", parts) + "]";
        }
        return scalarArg(value, key.kind());
    }

    private static String scalarArg(JsonElement value, RecipeSchemaReader.Kind kind) {
        return switch (kind) {
            case ITEM -> itemArg(value);
            case FLUID -> fluidArg(value);
            case NUMBER -> value.isJsonPrimitive() ? value.getAsString() : null;
            case BOOLEAN -> value.isJsonPrimitive() ? String.valueOf(value.getAsBoolean()) : null;
            case STRING -> value.isJsonPrimitive() ? "'" + value.getAsString() + "'" : null;
            default -> null;
        };
    }

    private static String itemArg(JsonElement value) {
        if (value.isJsonPrimitive()) return "'" + value.getAsString() + "'";
        if (value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if (object.has("tag")) return "'#" + object.get("tag").getAsString() + "'";
            String id = object.has("id") ? object.get("id").getAsString() :
                    object.has("item") ? object.get("item").getAsString() : null;
            if (id == null) return null;
            int count = object.has("count") ? object.get("count").getAsInt() : 1;
            return count > 1 ? "Item.of('" + id + "', " + count + ")" : "'" + id + "'";
        }
        return null;
    }

    private static String fluidArg(JsonElement value) {
        if (!value.isJsonObject()) return null;
        JsonObject object = value.getAsJsonObject();
        if (!object.has("fluid")) return null;
        int amount = object.has("amount") ? object.get("amount").getAsInt() : 1000;
        return "Fluid.of('" + object.get("fluid").getAsString() + "', " + amount + ")";
    }

    private enum Kind {
        ITEM,
        FLUID,
        BOOLEAN,
        FIELD,
        KEEP
    }

    private static Kind uiKind(JsonField field) {
        RecipeCodecReader.Kind k = field.kind() == RecipeCodecReader.Kind.LIST ? field.elementKind() : field.kind();
        return switch (k) {
            case ITEM -> Kind.ITEM;
            case FLUID -> Kind.FLUID;
            case BOOLEAN -> Kind.BOOLEAN;
            case NUMBER, STRING -> field.kind() == RecipeCodecReader.Kind.LIST ? Kind.KEEP : Kind.FIELD;
            default -> Kind.KEEP;
        };
    }

    private static int listSlots(JsonField field) {
        return Math.min(RecipeMakerBehavior.POOL_ITEM, Math.max(4, field.listMax() + 4));
    }

    private static JsonElement elementShape(JsonElement sampleValue) {
        if (sampleValue != null && sampleValue.isJsonArray() && !sampleValue.getAsJsonArray().isEmpty()) {
            return sampleValue.getAsJsonArray().get(0);
        }
        return sampleValue;
    }

    private static JsonElement itemElement(JsonElement shape, ItemStack stack, String tag) {
        if (tag != null && !tag.isEmpty()) {
            JsonObject object = new JsonObject();
            object.addProperty("tag", tag);
            return object;
        }
        return itemToJson(shape, stack);
    }

    private static JsonElement itemToJson(JsonElement shape, ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (shape != null && shape.isJsonObject()) {
            JsonObject object = shape.getAsJsonObject().deepCopy();
            if (object.has("id")) object.addProperty("id", id);
            else object.addProperty("item", id);
            if (object.has("count")) object.addProperty("count", stack.getCount());
            return object;
        }
        if (shape != null && shape.isJsonPrimitive()) {
            return new com.google.gson.JsonPrimitive(id);
        }
        JsonObject object = new JsonObject();
        object.addProperty("item", id);
        if (stack.getCount() > 1) object.addProperty("count", stack.getCount());
        return object;
    }

    private static JsonElement fluidToJson(JsonElement shape, FluidStack stack) {
        String id = BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString();
        JsonObject object = shape != null && shape.isJsonObject() ? shape.getAsJsonObject().deepCopy() :
                new JsonObject();
        object.addProperty("fluid", id);
        object.addProperty("amount", stack.getAmount());
        return object;
    }

    private static String shortName(String typeId) {
        int colon = typeId.indexOf(':');
        return colon < 0 ? typeId : typeId.substring(colon + 1);
    }
}
