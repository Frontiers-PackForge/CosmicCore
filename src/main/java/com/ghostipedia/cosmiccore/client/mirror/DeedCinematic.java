package com.ghostipedia.cosmiccore.client.mirror;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class DeedCinematic {

    private static final int TEMPO_SCALE = 2;
    public static final int PRELUDE_TICKS = 55 * TEMPO_SCALE;
    public static final int PHASE_TICKS = 90 * TEMPO_SCALE;
    public static final int TOTAL_TICKS = PRELUDE_TICKS + PHASE_TICKS * 3;
    private static final int TYPEWRITER_TICKS_PER_CODE_POINT = 1;
    private static final int FADE_TICKS = 18 * TEMPO_SCALE;
    private static final int MAX_FRAGMENTS_PER_PHASE = 3;
    private static final int MAX_LEGACY_FRAGMENTS = 8;

    public enum Phase {

        PRELUDE("prelude", 0, PRELUDE_TICKS),
        COIL("coil", PRELUDE_TICKS, PRELUDE_TICKS + PHASE_TICKS),
        RING("ring", PRELUDE_TICKS + PHASE_TICKS, PRELUDE_TICKS + PHASE_TICKS * 2),
        KNOT("knot", PRELUDE_TICKS + PHASE_TICKS * 2, TOTAL_TICKS);

        private final String key;
        private final int startTick;
        private final int endTick;

        Phase(String key, int startTick, int endTick) {
            this.key = key;
            this.startTick = startTick;
            this.endTick = endTick;
        }

        String key() {
            return key;
        }

        int startTick() {
            return startTick;
        }

        int endTick() {
            return endTick;
        }
    }

    public static final class Fragment {

        private final String text;
        private final float x;
        private final float y;
        private final float angle;
        private final int startTick;
        private final int endTick;

        private Fragment(String text, float x, float y, float angle, int startTick, int endTick) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.startTick = startTick;
            this.endTick = endTick;
        }

        public float x() {
            return x;
        }

        public float y() {
            return y;
        }

        public float angle() {
            return angle;
        }

        public String text() {
            return text;
        }

        public int alpha(int progress) {
            if (progress < startTick || progress >= endTick) return 0;
            int fadeStart = endTick - FADE_TICKS;
            if (progress <= fadeStart) return 255;
            return Mth.clamp(Math.round(255f * (endTick - progress) / FADE_TICKS), 0, 255);
        }

        public int visibleCodePoints(int progress) {
            int elapsed = Math.max(0, progress - startTick);
            int codePoints = text.codePointCount(0, text.length());
            return Math.min(codePoints, elapsed / TYPEWRITER_TICKS_PER_CODE_POINT);
        }
    }

    private final List<Fragment> fragments;

    private DeedCinematic(List<Fragment> fragments) {
        this.fragments = List.copyOf(fragments);
    }

    public static DeedCinematic load(ResourceLocation deedId) {
        List<Fragment> fragments = new ArrayList<>();
        String root = "deed." + deedId.getNamespace() + "." + deedId.getPath() + ".telling.";
        boolean staged = false;
        for (Phase phase : Phase.values()) {
            List<String> texts = readPhase(root, phase);
            staged |= !texts.isEmpty();
            addPhaseFragments(fragments, deedId, phase, texts);
        }
        if (!staged) {
            addLegacyFragments(fragments, deedId, root);
        }
        return new DeedCinematic(fragments);
    }

    public List<Fragment> fragments() {
        return fragments;
    }

    public int visibleCodePoints(int progress) {
        int visible = 0;
        for (Fragment fragment : fragments) {
            visible += fragment.visibleCodePoints(progress);
        }
        return visible;
    }

    public static Phase phaseAt(int progress) {
        if (progress < PRELUDE_TICKS) return Phase.PRELUDE;
        if (progress < PRELUDE_TICKS + PHASE_TICKS) return Phase.COIL;
        if (progress < PRELUDE_TICKS + PHASE_TICKS * 2) return Phase.RING;
        return Phase.KNOT;
    }

    public static float weaveProgress(int progress) {
        return Mth.clamp((progress - PRELUDE_TICKS) / (float) (TOTAL_TICKS - PRELUDE_TICKS), 0f, 1f);
    }

    private static List<String> readPhase(String root, Phase phase) {
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < MAX_FRAGMENTS_PER_PHASE; i++) {
            String key = root + phase.key() + "." + i;
            if (!I18n.exists(key)) break;
            texts.add(Component.translatable(key).getString());
        }
        return texts;
    }

    private static void addPhaseFragments(List<Fragment> fragments, ResourceLocation deedId, Phase phase,
                                          List<String> texts) {
        for (int i = 0; i < texts.size(); i++) {
            int start = phase.startTick() + 4 * TEMPO_SCALE + i * 15 * TEMPO_SCALE;
            int end = phase.endTick();
            float[] placement = placement(deedId, phase.ordinal(), i);
            fragments.add(new Fragment(texts.get(i), placement[0], placement[1], placement[2], start, end));
        }
    }

    private static void addLegacyFragments(List<Fragment> fragments, ResourceLocation deedId, String root) {
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < MAX_LEGACY_FRAGMENTS; i++) {
            String key = root + i;
            if (!I18n.exists(key)) break;
            texts.add(Component.translatable(key).getString());
        }
        if (texts.isEmpty()) return;
        int spacing = Math.max(18 * TEMPO_SCALE, (TOTAL_TICKS - 35 * TEMPO_SCALE) / texts.size());
        for (int i = 0; i < texts.size(); i++) {
            int start = 10 * TEMPO_SCALE + i * spacing;
            int end = Math.min(TOTAL_TICKS, start + Math.max(45 * TEMPO_SCALE, spacing + FADE_TICKS));
            Phase phase = phaseAt(start);
            float[] placement = placement(deedId, phase.ordinal(), i);
            fragments.add(new Fragment(texts.get(i), placement[0], placement[1], placement[2], start, end));
        }
    }

    private static float[] placement(ResourceLocation deedId, int phase, int index) {
        float[][][] placements = {
                { { 0.50f, 0.18f, -2f }, { 0.28f, 0.70f, 5f }, { 0.73f, 0.63f, -6f } },
                { { 0.25f, 0.30f, -8f }, { 0.73f, 0.68f, 7f }, { 0.31f, 0.76f, -4f } },
                { { 0.72f, 0.27f, 6f }, { 0.27f, 0.67f, -7f }, { 0.70f, 0.76f, 3f } },
                { { 0.30f, 0.25f, -5f }, { 0.69f, 0.68f, 5f }, { 0.50f, 0.78f, 0f } }
        };
        float[] base = placements[Math.floorMod(phase, placements.length)][Math.floorMod(index, 3)];
        int seed = deedId.hashCode() + phase * 97 + index * 41;
        float x = base[0] + (Math.floorMod(seed, 31) - 15) / 1000f;
        float y = base[1] + (Math.floorMod(seed * 3, 25) - 12) / 1000f;
        float angle = base[2] + (Math.floorMod(seed * 7, 21) - 10) / 10f;
        return new float[] { x, y, angle };
    }
}
