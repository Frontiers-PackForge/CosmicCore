package com.ghostipedia.cosmiccore.integration.kjs;

import com.ghostipedia.cosmiccore.common.mirror.deed.Deed;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DeedBuilder {

    private static final int MAX_PHASE_LINES = 32;

    private final ResourceLocation id;
    private final String root;
    private final Map<String, String> enUs = new LinkedHashMap<>();
    private final Map<String, Integer> phaseLines = new HashMap<>();
    private boolean built;

    DeedBuilder(String id) {
        this.id = ResourceLocation.parse(id);
        root = "deed." + this.id.getNamespace() + "." + this.id.getPath().replace('/', '.');
    }

    public DeedBuilder name(String text) {
        return scalar(root, text);
    }

    public DeedBuilder subtitle(String text) {
        return scalar(root + ".subtitle", text);
    }

    public DeedBuilder sealedHint(String text) {
        return scalar(root + ".sealed_hint", text);
    }

    public DeedBuilder prelude(String text) {
        return phase("prelude", text);
    }

    public DeedBuilder coil(String text) {
        return phase("coil", text);
    }

    public DeedBuilder ring(String text) {
        return phase("ring", text);
    }

    public DeedBuilder knot(String text) {
        return phase("knot", text);
    }

    public DeedBuilder post(String text) {
        return scalar(root + ".post", text);
    }

    public DeedBuilder unlock(String text) {
        return scalar(root + ".unlock", text);
    }

    public Deed build() {
        ensureMutable();
        if (!enUs.containsKey(root)) {
            throw new IllegalStateException("Deed " + id + " is missing name(...)");
        }
        built = true;
        return DeedRegistry.put(new Deed(id, root, Deed.Lever.KEY, 0, "unclassified", enUs));
    }

    private DeedBuilder scalar(String key, String text) {
        ensureMutable();
        String previous = enUs.putIfAbsent(key, requireText(text));
        if (previous != null) {
            throw new IllegalStateException("Deed " + id + " defines " + key + " more than once");
        }
        return this;
    }

    private DeedBuilder phase(String phase, String text) {
        ensureMutable();
        int index = phaseLines.getOrDefault(phase, 0);
        if (index >= MAX_PHASE_LINES) {
            throw new IllegalStateException("Deed " + id + " has more than " + MAX_PHASE_LINES + " " + phase +
                    " lines");
        }
        enUs.put(root + ".telling." + phase + "." + index, requireText(text));
        phaseLines.put(phase, index + 1);
        return this;
    }

    private String requireText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Deed " + id + " text cannot be blank");
        }
        return text;
    }

    private void ensureMutable() {
        if (built) throw new IllegalStateException("Deed " + id + " has already been built");
    }
}
