package com.ghostipedia.cosmiccore.common.mirror.deed;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DeedRegistry {

    private DeedRegistry() {}

    private static final Map<ResourceLocation, Deed> DEEDS = new LinkedHashMap<>();

    public static final Deed FIRST_FLAME = register(new Deed(CosmicCore.id("first_flame"),
            "deed.cosmiccore.first_flame", Deed.Lever.LEAF, 0, "foundation"));
    public static final Deed FIRST_MACHINE = register(new Deed(CosmicCore.id("first_machine"),
            "deed.cosmiccore.first_machine", Deed.Lever.KEY, 1, "foundation"));
    public static final Deed FIRST_DESCENT = register(new Deed(CosmicCore.id("first_descent"),
            "deed.cosmiccore.first_descent", Deed.Lever.MARK, 3, "descent"));
    public static final Deed NETHER_PERMIT = register(new Deed(CosmicCore.id("nether_permit"),
            "deed.cosmiccore.nether_permit", Deed.Lever.KEY, 0, "foundation"));
    public static final Deed CURRENT_FLOW = register(new Deed(CosmicCore.id("current_flow"),
            "deed.cosmiccore.current_flow", Deed.Lever.KEY, 1, "foundation"));
    public static final Deed THE_ADDRESS = register(new Deed(CosmicCore.id("the_address"),
            "deed.cosmiccore.the_address", Deed.Lever.KEY, 7, "address"));

    static {
        if (!FMLEnvironment.production) {
            for (int era = 1; era <= 7; era++) {
                for (int i = 1; i <= 12; i++) {
                    register(new Deed(CosmicCore.id("dev/e" + era + "_" + i), "deed.cosmiccore.dev",
                            Deed.Lever.values()[(era + i) % 4], era, "dev_era_" + era));
                }
            }
        }
    }

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
