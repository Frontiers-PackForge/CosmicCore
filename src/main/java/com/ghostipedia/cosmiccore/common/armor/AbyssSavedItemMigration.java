package com.ghostipedia.cosmiccore.common.armor;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.Map;

public final class AbyssSavedItemMigration {

    private AbyssSavedItemMigration() {}

    private static final Map<String, String> IDS = Map.of(
            "stealth_coating_1", "stealth_module_1", "stealth_coating_2", "stealth_module_2",
            "stealth_coating_3", "stealth_module_3", "bronze_supply_tank", "bronze_oxygen_tank_module",
            "steel_supply_tank", "steel_oxygen_tank_module", "simple_rebreather", "simple_rebreather_module",
            "pressurized_rebreather", "advanced_rebreather_module");

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

    public static <T> Dynamic<T> migrate(Dynamic<T> input) {
        var map = input.getMapValues().result();
        if (map.isPresent()) {
            Dynamic<T> result = input;
            for (var entry : map.get().entrySet()) {
                var migrated = migrate(entry.getValue());
                if (migrated.getValue() != entry.getValue().getValue()) {
                    result = result.set(entry.getKey().asString(""), migrated);
                }
            }
            String id = result.get("id").asString("");
            if (id.isEmpty()) return result;
            String replacement = id.startsWith("cosmiccore:") ? IDS.get(id.substring("cosmiccore:".length())) : null;
            if (replacement != null) result = result.set("id", result.createString("cosmiccore:" + replacement));
            var components = result.get("components").result();
            if (components.isPresent()) {
                var custom = components.get().get("minecraft:custom_data").result();
                if (custom.isPresent() && custom.get().get("CosmicStealth").result().isPresent())
                    result = result.set("components",
                            components.get().set("minecraft:custom_data", custom.get().remove("CosmicStealth")));
            }
            return result;
        }
        var list = input.asStreamOpt().result();
        if (list.isEmpty()) return input;
        var entries = list.get().toList();
        var migrated = entries.stream().map(AbyssSavedItemMigration::migrate).toList();
        for (int index = 0; index < entries.size(); index++) {
            if (entries.get(index).getValue() != migrated.get(index).getValue())
                return input.createList(migrated.stream());
        }
        return input;
    }
}
