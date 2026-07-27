package com.ghostipedia.cosmiccore.common.mirror.deed;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record Deed(ResourceLocation id, String nameKey, Lever lever, int tier, String chapter,
                   Map<String, String> enUs) {

    public Deed(ResourceLocation id, String nameKey, Lever lever, int tier, String chapter) {
        this(id, nameKey, lever, tier, chapter, Map.of());
    }

    public Deed {
        enUs = Map.copyOf(enUs);
    }

    public enum Lever {
        KEY,
        MARK,
        GRACE,
        LEAF
    }

    public String tellingKey() {
        return nameKey + ".telling";
    }
}
