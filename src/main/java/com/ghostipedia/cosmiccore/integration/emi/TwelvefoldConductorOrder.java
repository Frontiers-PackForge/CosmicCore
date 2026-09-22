package com.ghostipedia.cosmiccore.integration.emi;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;

public final class TwelvefoldConductorOrder {

    private TwelvefoldConductorOrder() {}

    public static <T> void reorder(List<T> stacks, Function<T, Entry> classifier) {
        List<Classified<T>> classified = new ArrayList<>(stacks.size());
        IdentityHashMap<Object, MaterialFamilies<T>> materials = new IdentityHashMap<>();
        boolean hasTwelvefold = false;
        for (T stack : stacks) {
            Entry entry = classifier.apply(stack);
            classified.add(new Classified<>(stack, entry));
            if (entry == null) {
                continue;
            }
            Family<T> family = materials.computeIfAbsent(entry.material(), ignored -> new MaterialFamilies<>())
                    .family(entry.cable());
            if (entry.form() == Form.TWELVEFOLD) {
                family.twelvefold.add(stack);
                hasTwelvefold = true;
            } else if (entry.form() == Form.HEX) {
                family.hasHex = true;
            }
        }
        if (!hasTwelvefold) {
            return;
        }

        List<T> ordered = new ArrayList<>(stacks.size());
        for (Classified<T> candidate : classified) {
            Entry entry = candidate.entry();
            if (entry != null && entry.form() == Form.TWELVEFOLD) {
                continue;
            }
            if (entry != null) {
                Family<T> family = materials.get(entry.material()).family(entry.cable());
                if (entry.form() == Form.HEX) {
                    family.placeIn(ordered);
                }
                ordered.add(candidate.value());
                if (entry.form() == Form.OCTAL && !family.hasHex) {
                    family.placeIn(ordered);
                }
            } else {
                ordered.add(candidate.value());
            }
        }
        for (Classified<T> candidate : classified) {
            Entry entry = candidate.entry();
            if (entry != null && entry.form() == Form.TWELVEFOLD) {
                Family<T> family = materials.get(entry.material()).family(entry.cable());
                family.placeIn(ordered);
            }
        }

        stacks.clear();
        stacks.addAll(ordered);
    }

    public record Entry(Object material, boolean cable, Form form) {}

    public enum Form {
        OCTAL,
        TWELVEFOLD,
        HEX
    }

    private record Classified<T>(T value, Entry entry) {}

    private static final class MaterialFamilies<T> {

        private final Family<T> wire = new Family<>();
        private final Family<T> cable = new Family<>();

        private Family<T> family(boolean cable) {
            return cable ? this.cable : this.wire;
        }
    }

    private static final class Family<T> {

        private final List<T> twelvefold = new ArrayList<>();
        private boolean hasHex;
        private boolean placed;

        private void placeIn(List<T> ordered) {
            if (!placed) {
                ordered.addAll(twelvefold);
                placed = true;
            }
        }
    }
}
