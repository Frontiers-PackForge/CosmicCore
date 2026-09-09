package com.ghostipedia.cosmiccore.common.compat.gtceu;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.Map;

public final class LegacyFilterComponentMigration {

    private static final String LEGACY_TAG = "gtceu:tag_filter_expression";
    private static final String SMART_FILTER = "gtceu:smart_item_filter";

    private LegacyFilterComponentMigration() {}

    public static <A> Codec<A> wrap(Codec<A> codec) {
        return new Codec<>() {

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return codec.decode(ops, migrate(new Dynamic<>(ops, input)).getValue());
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return codec.encode(input, ops, prefix);
            }
        };
    }

    public static <T> Dynamic<T> migrate(Dynamic<T> stack) {
        String item = stack.get("id").asString("");
        String tagComponent = switch (item) {
            case "gtceu:item_tag_filter" -> "gtceu:item_tag_filter";
            case "gtceu:fluid_tag_filter" -> "gtceu:fluid_tag_filter";
            default -> null;
        };
        if (tagComponent == null && !SMART_FILTER.equals(item)) return stack;

        var savedComponents = stack.get("components").result();
        if (savedComponents.isEmpty()) return stack;
        Dynamic<T> components = savedComponents.get();
        if (tagComponent != null) {
            var expression = components.get(LEGACY_TAG).asString().result();
            if (expression.isPresent()) {
                if (components.get(tagComponent).result().isEmpty() &&
                        components.get("!" + tagComponent).result().isEmpty()) {
                    components = components.set(tagComponent, stack.createMap(Map.of(
                            stack.createString("filterString"), stack.createString(expression.get()))));
                }
                components = components.remove(LEGACY_TAG);
            }
            var removedExpression = components.get("!" + LEGACY_TAG).result();
            if (removedExpression.isPresent()) {
                if (components.get(tagComponent).result().isEmpty() &&
                        components.get("!" + tagComponent).result().isEmpty()) {
                    components = components.set("!" + tagComponent, removedExpression.get());
                }
                components = components.remove("!" + LEGACY_TAG);
            }
        } else {
            var mode = components.get(SMART_FILTER).asString().result();
            if (mode.isPresent()) {
                components = components.set(SMART_FILTER, stack.createMap(Map.of(
                        stack.createString("filterMode"), stack.createString(mode.get()))));
            }
        }
        return components == savedComponents.get() ? stack : stack.set("components", components);
    }
}
