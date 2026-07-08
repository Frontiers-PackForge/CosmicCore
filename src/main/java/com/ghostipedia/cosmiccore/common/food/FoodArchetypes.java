package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FoodArchetypes {

    private FoodArchetypes() {}

    private static final Map<String, FoodArchetype> ARCHETYPES = new LinkedHashMap<>();
    private static final Map<String, String> ASSIGN_EXACT = new ConcurrentHashMap<>();
    private static final Map<String, String> ASSIGN_NAMESPACE = new ConcurrentHashMap<>();

    public static synchronized void register(FoodArchetype archetype) {
        ARCHETYPES.put(archetype.name(), archetype);
    }

    public static void assign(String pattern, String archetypeName) {
        if (pattern.endsWith(":*")) {
            ASSIGN_NAMESPACE.put(pattern.substring(0, pattern.length() - 2), archetypeName);
        } else {
            ASSIGN_EXACT.put(pattern, archetypeName);
        }
    }

    @Nullable
    public static synchronized FoodArchetype resolve(ResourceLocation id, FoodCategory category, int nutrition) {
        String assigned = ASSIGN_EXACT.get(id.toString());
        if (assigned == null) assigned = ASSIGN_NAMESPACE.get(id.getNamespace());
        if (assigned != null) {
            FoodArchetype archetype = ARCHETYPES.get(assigned);
            if (archetype != null) return archetype;
        }

        FoodArchetype best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (FoodArchetype archetype : ARCHETYPES.values()) {
            if (archetype.category() != category) continue;
            int distance = archetype.distance(nutrition);
            if (distance < bestDistance) {
                best = archetype;
                bestDistance = distance;
            }
        }
        return best;
    }
}
