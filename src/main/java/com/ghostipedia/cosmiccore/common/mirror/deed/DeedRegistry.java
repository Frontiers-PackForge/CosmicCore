package com.ghostipedia.cosmiccore.common.mirror.deed;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DeedRegistry {

    private DeedRegistry() {}

    private static final Map<ResourceLocation, Deed> DEEDS = new LinkedHashMap<>();

    public static final Deed FIRST_FLAME = register(
            new Deed(CosmicCore.id("first_flame"), "deed.cosmiccore.first_flame", Deed.Lever.LEAF, 0));
    public static final Deed FIRST_MACHINE = register(
            new Deed(CosmicCore.id("first_machine"), "deed.cosmiccore.first_machine", Deed.Lever.KEY, 1));
    public static final Deed FIRST_DESCENT = register(
            new Deed(CosmicCore.id("first_descent"), "deed.cosmiccore.first_descent", Deed.Lever.MARK, 3));

    public static Deed register(Deed deed) {
        if (DEEDS.putIfAbsent(deed.id(), deed) != null) {
            throw new IllegalStateException("Duplicate deed " + deed.id());
        }
        return deed;
    }

    public static Deed put(Deed deed) {
        DEEDS.put(deed.id(), deed);
        return deed;
    }

    @Nullable
    public static Deed get(ResourceLocation id) {
        return DEEDS.get(id);
    }

    public static Collection<Deed> all() {
        return Collections.unmodifiableCollection(DEEDS.values());
    }
}
