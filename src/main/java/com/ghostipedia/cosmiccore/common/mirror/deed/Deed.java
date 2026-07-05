package com.ghostipedia.cosmiccore.common.mirror.deed;

import net.minecraft.resources.ResourceLocation;

public record Deed(ResourceLocation id, String nameKey, Lever lever, int tier, String chapter) {

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
