package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.ghostipedia.cosmiccore.gtbridge.recipemaker.RecipeCodecReader.JsonField;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generic editor for any recipe type that has no hand-built or GregTech editor. It treats a sampled recipe (from
 * {@link RecipeCodecReader}) as a TEMPLATE: numeric/string fields are pre-filled and editable, item/fluid fields
 * get phantom slots (left empty = keep the template's value, filled = override). Export rebuilds the recipe JSON
 * from the template plus the user's overrides and emits {@code event.custom({...})}, which is valid KubeJS for any
 * type. Runs identically on both sides (codec data is available on each), so no syncing is needed.
 */
public final class CodecRecipeEditor {

    private static final int TANK_CAPACITY = 64_000;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private CodecRecipeEditor() {}

    public static void build(WidgetGroup editor, Player player, String typeId) {
        ResourceLocation rl = ResourceLocation.tryParse(typeId);
        List<JsonField> fields = rl == null ? List.of() : RecipeCodecReader.fields(player, rl);
        JsonObject sample = rl == null ? null : RecipeCodecReader.sample(player, rl);

        editor.addWidget(new LabelWidget(0, 0, shortName(typeId)));
        if (fields.isEmpty() || sample == null) {
            editor.addWidget(new LabelWidget(0, 14, "no sample recipe"));
            editor.addWidget(new LabelWidget(0, 24, "(can't auto-build)"));
            return;
        }

        String[] recipeId = { "" };
        editor.addWidget(new LabelWidget(0, 12, "id"));
        editor.addWidget(new TextFieldWidget(16, 11, 106, 12, () -> recipeId[0], s -> recipeId[0] = s));

        Map<String, CustomItemStackHandler> itemState = new LinkedHashMap<>();
        Map<String, FluidTank[]> fluidState = new LinkedHashMap<>();
        Map<String, String[]> valueState = new LinkedHashMap<>();

        DraggableScrollableWidgetGroup body = new DraggableScrollableWidgetGroup(0, 26, 128, 162);
        body.setBackground(new ColorRectTexture(0x40000000));
        int y = 2;
        for (JsonField field : fields) {
            String label = field.output() ? field.name() + " (out)" : field.name();
            Kind ui = uiKind(field);
            switch (ui) {
                case ITEM -> {
                    int slots = field.kind() == RecipeCodecReader.Kind.LIST ? listSlots(field) : 1;
                    CustomItemStackHandler handler = new CustomItemStackHandler(slots);
                    itemState.put(field.name(), handler);
                    body.addWidget(new LabelWidget(2, y, label));
                    for (int i = 0; i < slots; i++) {
                        body.addWidget(new ConfigurableItemSlot(handler, i, 2 + (i % 6) * 18, y + 10 + (i / 6) * 18));
                    }
                    y += 12 + ((slots + 5) / 6) * 18;
                }
                case FLUID -> {
                    int slots = field.kind() == RecipeCodecReader.Kind.LIST ? listSlots(field) : 1;
                    FluidTank[] tanks = new FluidTank[slots];
                    for (int i = 0; i < slots; i++) tanks[i] = new FluidTank(TANK_CAPACITY);
                    fluidState.put(field.name(), tanks);
                    body.addWidget(new LabelWidget(2, y, label));
                    for (int i = 0; i < slots; i++) {
                        body.addWidget(new ConfigurableFluidSlot(tanks[i], 2 + (i % 6) * 18, y + 10 + (i / 6) * 18));
                    }
                    y += 12 + ((slots + 5) / 6) * 18;
                }
                case BOOLEAN -> {
                    String[] value = { sample.has(field.name()) ? sample.get(field.name()).getAsString() : "false" };
                    valueState.put(field.name(), value);
                    body.addWidget(new LabelWidget(2, y, label));
                    body.addWidget(new SelectorWidget(52, y, 70, 12, List.of("false", "true"), 0)
                            .setOnChanged(v -> value[0] = v).setSupplier(() -> value[0]));
                    y += 16;
                }
                case FIELD -> {
                    String[] value = { sample.has(field.name()) ? sample.get(field.name()).getAsString() : "" };
                    valueState.put(field.name(), value);
                    body.addWidget(new LabelWidget(2, y, label));
                    body.addWidget(new TextFieldWidget(52, y, 70, 12, () -> value[0], s -> value[0] = s));
                    y += 16;
                }
                default -> {
                    body.addWidget(new LabelWidget(2, y, label + " (kept)"));
                    y += 12;
                }
            }
        }
        editor.addWidget(body);
        editor.addWidget(new ExportButtonWidget(0, 192, 120, 16, GuiTextures.VANILLA_BUTTON,
                () -> export(player, typeId, recipeId, sample, fields, itemState, fluidState, valueState)));
        editor.addWidget(new LabelWidget(34, 196, "Copy KubeJS"));
    }

    private static String export(Player player, String typeId, String[] recipeId, JsonObject sample,
                                 List<JsonField> fields, Map<String, CustomItemStackHandler> itemState,
                                 Map<String, FluidTank[]> fluidState, Map<String, String[]> valueState) {
        JsonObject out = sample.deepCopy();
        out.addProperty("type", typeId);
        for (JsonField field : fields) {
            String name = field.name();
            switch (uiKind(field)) {
                case ITEM -> {
                    CustomItemStackHandler handler = itemState.get(name);
                    JsonElement shape = elementShape(sample.get(name));
                    if (field.kind() == RecipeCodecReader.Kind.LIST) {
                        JsonArray array = new JsonArray();
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack stack = handler.getStackInSlot(i);
                            if (!stack.isEmpty()) array.add(itemToJson(shape, stack));
                        }
                        if (!array.isEmpty()) out.add(name, array);
                    } else {
                        ItemStack stack = handler.getStackInSlot(0);
                        if (!stack.isEmpty()) out.add(name, itemToJson(shape, stack));
                    }
                }
                case FLUID -> {
                    FluidTank[] tanks = fluidState.get(name);
                    JsonElement shape = elementShape(sample.get(name));
                    if (field.kind() == RecipeCodecReader.Kind.LIST) {
                        JsonArray array = new JsonArray();
                        for (FluidTank tank : tanks) {
                            if (!tank.getFluid().isEmpty()) array.add(fluidToJson(shape, tank.getFluid()));
                        }
                        if (!array.isEmpty()) out.add(name, array);
                    } else if (!tanks[0].getFluid().isEmpty()) {
                        out.add(name, fluidToJson(shape, tanks[0].getFluid()));
                    }
                }
                case BOOLEAN -> out.addProperty(name, "true".equals(valueState.get(name)[0]));
                case FIELD -> {
                    String text = valueState.get(name)[0].trim();
                    if (text.isEmpty()) break;
                    if (field.kind() == RecipeCodecReader.Kind.NUMBER) {
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
        if (!recipeId[0].isBlank()) result += ".id('" + recipeId[0].trim() + "')";
        return result;
    }

    /**
     * If the type has a KubeJS schema (i.e. a kubejs addon ships one - Create, EnderIO, Ars Nouveau, Occultism,
     * GregTech, vanilla), emit the idiomatic builder call {@code event.recipes.<ns>.<path>(args...).key(value)...}
     * by formatting the rebuilt JSON ({@code out}) against the schema's constructor + remaining keys. Returns null
     * (caller falls back to {@code event.custom}) if there's no schema or any present value can't be formatted, so a
     * recipe is never exported with silently dropped data.
     */
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
        return Math.min(64, Math.max(4, field.listMax() + 4));
    }

    private static JsonElement elementShape(JsonElement sampleValue) {
        if (sampleValue != null && sampleValue.isJsonArray() && !sampleValue.getAsJsonArray().isEmpty()) {
            return sampleValue.getAsJsonArray().get(0);
        }
        return sampleValue;
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
