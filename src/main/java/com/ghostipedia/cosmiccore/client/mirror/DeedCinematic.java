package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DeedCinematic {

    private static final Profile DEFAULT_PROFILE = Profile.builder().build();
    private static final Map<ResourceLocation, Profile> PROFILES = new HashMap<>();

    static {
        registerProfile(CosmicCore.id("current_flow"),
                Profile.builder()
                        .phaseVoice(Phase.RING, Voice.OVERSEER_ONE)
                        .automatic(true)
                        .returnQuest(0x1FECA1C0E8B233D5L)
                        .build());
    }

    public enum Phase {

        PRELUDE("prelude"),
        COIL("coil"),
        RING("ring"),
        KNOT("knot");

        private final String key;

        Phase(String key) {
            this.key = key;
        }

        String key() {
            return key;
        }
    }

    public record Voice(int color, boolean italic) {

        public static final Voice SOL = new Voice(0xE8DFD0, false);
        public static final Voice OVERSEER_ONE = new Voice(0x88CCFF, false);
    }

    public static final class Profile {

        private final Timing timing;
        private final Map<Phase, Voice> phaseVoices;
        private final Map<BeatKey, Voice> beatVoices;
        private final boolean automatic;
        private final Long returnQuestId;

        private Profile(Timing timing, Map<Phase, Voice> phaseVoices, Map<BeatKey, Voice> beatVoices,
                        boolean automatic, Long returnQuestId) {
            this.timing = timing;
            this.phaseVoices = Map.copyOf(phaseVoices);
            this.beatVoices = Map.copyOf(beatVoices);
            this.automatic = automatic;
            this.returnQuestId = returnQuestId;
        }

        public static Builder builder() {
            return new Builder();
        }

        Voice voice(Phase phase, int index) {
            return beatVoices.getOrDefault(new BeatKey(phase, index), phaseVoices.getOrDefault(phase, Voice.SOL));
        }

        public static final class Builder {

            private Timing timing = Timing.DEFAULT;
            private final Map<Phase, Voice> phaseVoices = new EnumMap<>(Phase.class);
            private final Map<BeatKey, Voice> beatVoices = new HashMap<>();
            private boolean automatic = true;
            private Long returnQuestId;

            public Builder timing(Timing timing) {
                this.timing = timing;
                return this;
            }

            public Builder phaseVoice(Phase phase, Voice voice) {
                phaseVoices.put(phase, voice);
                return this;
            }

            public Builder beatVoice(Phase phase, int index, Voice voice) {
                beatVoices.put(new BeatKey(phase, index), voice);
                return this;
            }

            public Builder automatic(boolean automatic) {
                this.automatic = automatic;
                return this;
            }

            public Builder returnQuest(long returnQuestId) {
                this.returnQuestId = returnQuestId;
                return this;
            }

            public Profile build() {
                return new Profile(timing, phaseVoices, beatVoices, automatic, returnQuestId);
            }
        }
    }

    public record Timing(int ticksPerCodePoint, int beatGapTicks, int phaseLeadTicks, int phaseHoldTicks,
                         int fadeTicks, int minimumPreludeTicks, int minimumPhaseTicks) {

        public static final Timing DEFAULT = new Timing(1, 8, 12, 28, 28, 90, 120);

        public Timing {
            if (ticksPerCodePoint < 1 || beatGapTicks < 0 || phaseLeadTicks < 0 || phaseHoldTicks < 0 ||
                    fadeTicks < 1 || minimumPreludeTicks < 1 || minimumPhaseTicks < 1) {
                throw new IllegalArgumentException("Invalid deed cinematic timing");
            }
        }
    }

    public static final class Fragment {

        private final String text;
        private final Voice voice;
        private final float x;
        private final float y;
        private final float angle;
        private final int startTick;
        private final int endTick;
        private final int ticksPerCodePoint;
        private final int fadeTicks;

        private Fragment(String text, Voice voice, float x, float y, float angle, int startTick, int endTick,
                         int ticksPerCodePoint, int fadeTicks) {
            this.text = text;
            this.voice = voice;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.startTick = startTick;
            this.endTick = endTick;
            this.ticksPerCodePoint = ticksPerCodePoint;
            this.fadeTicks = fadeTicks;
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

        public Voice voice() {
            return voice;
        }

        public int alpha(int progress) {
            if (progress < startTick || progress >= endTick) return 0;
            int fadeStart = endTick - fadeTicks;
            if (progress <= fadeStart) return 255;
            return Mth.clamp(Math.round(255f * (endTick - progress) / fadeTicks), 0, 255);
        }

        public int visibleCodePoints(int progress) {
            int elapsed = Math.max(0, progress - startTick);
            int codePoints = text.codePointCount(0, text.length());
            return Math.min(codePoints, elapsed / ticksPerCodePoint);
        }
    }

    private record BeatKey(Phase phase, int index) {}

    private record LoadedBeat(String text, Voice voice, int index) {}

    private record ScheduledBeat(LoadedBeat beat, int startTick, float[] placement) {}

    private record PhaseRange(int startTick, int endTick) {}

    private final List<Fragment> fragments;
    private final Map<Phase, PhaseRange> phaseRanges;
    private final int totalTicks;
    private final int weaveStartTick;

    private DeedCinematic(List<Fragment> fragments, Map<Phase, PhaseRange> phaseRanges, int totalTicks) {
        this.fragments = List.copyOf(fragments);
        this.phaseRanges = Map.copyOf(phaseRanges);
        this.totalTicks = totalTicks;
        weaveStartTick = phaseRanges.get(Phase.PRELUDE).endTick();
    }

    public static void registerProfile(ResourceLocation deedId, Profile profile) {
        PROFILES.put(deedId, profile);
    }

    public static boolean isAutomatic(ResourceLocation deedId) {
        return PROFILES.getOrDefault(deedId, DEFAULT_PROFILE).automatic;
    }

    public static Long returnQuestId(ResourceLocation deedId) {
        return PROFILES.getOrDefault(deedId, DEFAULT_PROFILE).returnQuestId;
    }

    public static DeedCinematic load(ResourceLocation deedId) {
        Profile profile = PROFILES.getOrDefault(deedId, DEFAULT_PROFILE);
        String root = "deed." + deedId.getNamespace() + "." + deedId.getPath() + ".telling.";
        Map<Phase, List<LoadedBeat>> beats = readStagedBeats(root, profile);
        if (beats.values().stream().allMatch(List::isEmpty)) {
            beats = readLegacyBeats(root, profile);
        }
        return schedule(deedId, profile, beats);
    }

    public List<Fragment> fragments() {
        return fragments;
    }

    public int totalTicks() {
        return totalTicks;
    }

    public int visibleCodePoints(int progress) {
        int visible = 0;
        for (Fragment fragment : fragments) {
            visible += fragment.visibleCodePoints(progress);
        }
        return visible;
    }

    public Phase phaseAt(int progress) {
        for (Phase phase : Phase.values()) {
            if (progress < phaseRanges.get(phase).endTick()) return phase;
        }
        return Phase.KNOT;
    }

    public float weaveProgress(int progress) {
        return Mth.clamp((progress - weaveStartTick) / (float) Math.max(1, totalTicks - weaveStartTick), 0f, 1f);
    }

    private static Map<Phase, List<LoadedBeat>> readStagedBeats(String root, Profile profile) {
        Map<Phase, List<LoadedBeat>> beats = emptyBeats();
        for (Phase phase : Phase.values()) {
            List<LoadedBeat> phaseBeats = beats.get(phase);
            for (int i = 0;; i++) {
                String key = root + phase.key() + "." + i;
                if (!I18n.exists(key)) break;
                phaseBeats.add(new LoadedBeat(Component.translatable(key).getString(), profile.voice(phase, i), i));
            }
        }
        return beats;
    }

    private static Map<Phase, List<LoadedBeat>> readLegacyBeats(String root, Profile profile) {
        List<String> texts = new ArrayList<>();
        for (int i = 0;; i++) {
            String key = root + i;
            if (!I18n.exists(key)) break;
            texts.add(Component.translatable(key).getString());
        }
        Map<Phase, List<LoadedBeat>> beats = emptyBeats();
        for (int i = 0; i < texts.size(); i++) {
            Phase phase = Phase.values()[Math.min(Phase.values().length - 1,
                    i * Phase.values().length / texts.size())];
            int phaseIndex = beats.get(phase).size();
            beats.get(phase).add(new LoadedBeat(texts.get(i), profile.voice(phase, phaseIndex), phaseIndex));
        }
        return beats;
    }

    private static Map<Phase, List<LoadedBeat>> emptyBeats() {
        Map<Phase, List<LoadedBeat>> beats = new EnumMap<>(Phase.class);
        for (Phase phase : Phase.values()) {
            beats.put(phase, new ArrayList<>());
        }
        return beats;
    }

    private static DeedCinematic schedule(ResourceLocation deedId, Profile profile,
                                          Map<Phase, List<LoadedBeat>> beats) {
        List<Fragment> fragments = new ArrayList<>();
        Map<Phase, PhaseRange> ranges = new EnumMap<>(Phase.class);
        int phaseStart = 0;
        for (Phase phase : Phase.values()) {
            List<ScheduledBeat> scheduled = new ArrayList<>();
            int cursor = phaseStart + profile.timing.phaseLeadTicks();
            for (LoadedBeat beat : beats.get(phase)) {
                float[] placement = placement(deedId, phase.ordinal(), beat.index());
                scheduled.add(new ScheduledBeat(beat, cursor, placement));
                int codePoints = beat.text().codePointCount(0, beat.text().length());
                cursor += Math.max(1, codePoints * profile.timing.ticksPerCodePoint()) +
                        profile.timing.beatGapTicks();
            }
            int minimumTicks = phase == Phase.PRELUDE ? profile.timing.minimumPreludeTicks() :
                    profile.timing.minimumPhaseTicks();
            int contentEnd = scheduled.isEmpty() ? phaseStart :
                    cursor - profile.timing.beatGapTicks() + profile.timing.phaseHoldTicks() +
                            profile.timing.fadeTicks();
            int phaseEnd = Math.max(phaseStart + minimumTicks, contentEnd);
            ranges.put(phase, new PhaseRange(phaseStart, phaseEnd));
            for (ScheduledBeat scheduledBeat : scheduled) {
                float[] placement = scheduledBeat.placement();
                fragments.add(new Fragment(scheduledBeat.beat().text(), scheduledBeat.beat().voice(), placement[0],
                        placement[1], placement[2], scheduledBeat.startTick(), phaseEnd,
                        profile.timing.ticksPerCodePoint(), profile.timing.fadeTicks()));
            }
            phaseStart = phaseEnd;
        }
        return new DeedCinematic(fragments, ranges, phaseStart);
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
