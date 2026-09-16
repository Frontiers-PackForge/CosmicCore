package com.ghostipedia.cosmiccore.common.machine.foundry;

import java.util.ArrayList;
import java.util.List;

public final class HephaestusCauldronStructure {

    public static final TierPattern TIER_1 = new TierPattern(
            FoundryTier.TIER_1, 'G', 'B', 'F', 'H',
            new String[][] {
                    { "    ABGBA    ", "    ABBBA    ", "    A   A    ", "             ", "             ",
                            "             " },
                    { "   CCDDDCC   ", "   CCDDDCC   ", "    AEEEA    ", "    AAAAA    ", "             ",
                            "             " },
                    { "  CDDCCCDDC  ", "  CDD   DDC  ", "   DD   DD   ", "   DDFFFDD   ", "    DDDDD    ",
                            "             " },
                    { " CDCCCCCCCDC ", " CDD     DDC ", "  DD     DD  ", "  DDDFFFDDD  ", "   DD   DD   ",
                            "    D   D    " },
                    { "ACDCCCCCCCDCA", "ACD       DCA", "AAD       DAA", " ADDFFFFFDDA ", "  DD     DD  ",
                            "   D     D   " },
                    { "BDCCCCCCCCCDB", "BD         DB", " E         E ", " AFFFFFFFFFA ", "  D       D  ",
                            "             " },
                    { "BDCCCCCCCCCDB", "BD         DB", " E         E ", " AFFFFFFFFFA ", "  D   H   D  ",
                            "             " },
                    { "BDCCCCCCCCCDB", "BD         DB", " E         E ", " AFFFFFFFFFA ", "  D       D  ",
                            "             " },
                    { "ACDCCCCCCCDCA", "ACD       DCA", "AAD       DAA", " ADDFFFFFDDA ", "  DD     DD  ",
                            "   D     D   " },
                    { " CDCCCCCCCDC ", " CDD     DDC ", "  DD     DD  ", "  DDDFFFDDD  ", "   DD   DD   ",
                            "    D   D    " },
                    { "  CDDCCCDDC  ", "  CDD   DDC  ", "   DD   DD   ", "   DDFFFDD   ", "    DDDDD    ",
                            "             " },
                    { "   CCDDDCC   ", "   CCDDDCC   ", "    AEEEA    ", "    AAAAA    ", "             ",
                            "             " },
                    { "    ABBBA    ", "    ABBBA    ", "    A   A    ", "             ", "             ",
                            "             " }
            });

    public static final TierPattern TIER_2 = new TierPattern(
            FoundryTier.TIER_2, 'J', 'C', 'I', 'K',
            new String[][] {
                    { "AAA BCJCB AAA", "AAA BCCCB AAA", "AAA B   B AAA", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             " },
                    { "AAADDAAADDAAA", "AAADDAAADDAAA", "AAAEBFFFBEAAA", " A EBBBBBE A ", " A E DDD E A ",
                            " AAE DGD EAA ", "     DGD     ", "     DGD     ", "     DGD     ",
                            "     DDD     " },
                    { "AADAADDDAADAA", "AADAA   AADAA", "AAAAA   AAAAA", "  GAA   AAG  ", "  G AAAAA G  ",
                            " AAEAHHHAEAA ", "    AHHHA    ", "    AHHHA    ", "    AHHHA    ",
                            "    AHHHA    " },
                    { " DADDDDDDDAD ", " DAA     AAD ", " EAA     AAE ", " EAAA   AAAE ", " E AA   AA E ",
                            " EEEA   AEEE ", "   AA   AA   ", "   AA   AA   ", "   AAIIIAA   ",
                            "   AA   AA   " },
                    { "BDADDDDDDDADB", "BDA       ADB", "BBA       ABB", " BAA     AAB ", "  AA     AA  ",
                            "  AA     AA  ", "  AA     AA  ", "  AA     AA  ", "  AAIIIIIAA  ",
                            "  AA     AA  " },
                    { "CADDDDDDDDDAC", "CA         AC", " F         F ", " B         B ", " DA       AD ",
                            " DH       HD ", " DH       HD ", " DH       HD ", " DHIIIIIIIHD ",
                            " DH       HD " },
                    { "CADDDDDDDDDAC", "CA         AC", " F         F ", " B         B ", " DA       AD ",
                            " GH       HG ", " GH       HG ", " GH       HG ", " GHIIIIIIIHG ",
                            " DH   K   HD " },
                    { "CADDDDDDDDDAC", "CA         AC", " F         F ", " B         B ", " DA       AD ",
                            " DH       HD ", " DH       HD ", " DH       HD ", " DHIIIIIIIHD ",
                            " DH       HD " },
                    { "BDADDDDDDDADB", "BDA       ADB", "BBA       ABB", " BAA     AAB ", "  AA     AA  ",
                            "  AA     AA  ", "  AA     AA  ", "  AA     AA  ", "  AAIIIIIAA  ",
                            "  AA     AA  " },
                    { " DADDDDDDDAD ", " DAA     AAD ", " EAA     AAE ", " EAAA   AAAE ", " E AA   AA E ",
                            " EEEA   AEEE ", "   AA   AA   ", "   AA   AA   ", "   AAIIIAA   ",
                            "   AA   AA   " },
                    { "AADAADDDAADAA", "AADAA   AADAA", "AAAAA   AAAAA", "  GAA   AAG  ", "  G AAAAA G  ",
                            " AAEAHHHAEAA ", "    AHHHA    ", "    AHHHA    ", "    AHHHA    ",
                            "    AHHHA    " },
                    { "AAADDAAADDAAA", "AAADDAAADDAAA", "AAAEBFFFBEAAA", " A EBBBBBE A ", " A E DDD E A ",
                            " AAE DGD EAA ", "     DGD     ", "     DGD     ", "     DGD     ",
                            "     DDD     " },
                    { "AAA BCCCB AAA", "AAA BCCCB AAA", "AAA B   B AAA", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             " }
            });

    public static final TierPattern TIER_3 = new TierPattern(
            FoundryTier.TIER_3, 'K', 'C', 'J', 'L',
            new String[][] {
                    { "AAA BCKCB AAA", "AAA BCCCB AAA", "AAA B   B AAA", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             " },
                    { "AAADDAAADDAAA", "AAADDAAADDAAA", "AAAEBFFFBEAAA", " A EBBBBBE A ", " A EGDDDGE A ",
                            " AAEGDHDGEAA ", "    GDHDG    ", "    GDHDG    ", "    GDHDG    ",
                            "    GDDDG    ", "    GAAAG    ", "    IIIII    " },
                    { "AADAADDDAADAA", "AADAA   AADAA", "AAAAA   AAAAA", "  HAA   AAH  ", "  H AAAAA H  ",
                            " AAEAIIIAEAA ", "  IHAIIIAHI  ", "  IHAIIIAHI  ", "  IHAIIIAHI  ",
                            "  IIAIIIAII  ", "  GGGAAAGGG  ", "  IIIIIIIII  " },
                    { " DADDDDDDDAD ", " DAA     AAD ", " EAA     AAE ", " EAAA   AAAE ", " E AA   AA E ",
                            " EEEA   AEEE ", "  HAA   AAH  ", "  HAA   AAH  ", "  HAA   AAH  ",
                            "  IAA   AAI  ", "  GAAJJJAAG  ", "  III   III  " },
                    { "BDADDDDDDDADB", "BDA       ADB", "BBA       ABB", " BAA     AAB ", " GAA     AAG ",
                            " GAA     AAG ", " GAA     AAG ", " GAA     AAG ", " GAA     AAG ",
                            " GAA     AAG ", " GGAJJJJJAGG ", " III     III " },
                    { "CADDDDDDDDDAC", "CA         AC", " F         F ", " B         B ", " DA       AD ",
                            " DI       ID ", " DI       ID ", " DI       ID ", " DI       ID ",
                            " DI       ID ", " AAJJJJJJJAA ", " II       II " },
                    { "CADDDDDDDDDAC", "CA         AC", " F         F ", " B         B ", " DA       AD ",
                            " HI       IH ", " HI       IH ", " HI       IH ", " HI       IH ",
                            " DI       ID ", " AAJJJJJJJAA ", " II   L   II " },
                    { "CADDDDDDDDDAC", "CA         AC", " F         F ", " B         B ", " DA       AD ",
                            " DI       ID ", " DI       ID ", " DI       ID ", " DI       ID ",
                            " DI       ID ", " AAJJJJJJJAA ", " II       II " },
                    { "BDADDDDDDDADB", "BDA       ADB", "BBA       ABB", " BAA     AAB ", " GAA     AAG ",
                            " GAA     AAG ", " GAA     AAG ", " GAA     AAG ", " GAA     AAG ",
                            " GAA     AAG ", " GGAJJJJJAGG ", " III     III " },
                    { " DADDDDDDDAD ", " DAA     AAD ", " EAA     AAE ", " EAAA   AAAE ", " E AA   AA E ",
                            " EEEA   AEEE ", "  HAA   AAH  ", "  HAA   AAH  ", "  HAA   AAH  ",
                            "  IAA   AAI  ", "  GAAJJJAAG  ", "  III   III  " },
                    { "AADAADDDAADAA", "AADAA   AADAA", "AAAAA   AAAAA", "  HAA   AAH  ", "  H AAAAA H  ",
                            " AAEAIIIAEAA ", "  IHAIIIAHI  ", "  IHAIIIAHI  ", "  IHAIIIAHI  ",
                            "  IIAIIIAII  ", "  GGGAAAGGG  ", "  IIIIIIIII  " },
                    { "AAADDAAADDAAA", "AAADDAAADDAAA", "AAAEBFFFBEAAA", " A EBBBBBE A ", " A EGDDDGE A ",
                            " AAEGDHDGEAA ", "    GDHDG    ", "    GDHDG    ", "    GDHDG    ",
                            "    GDDDG    ", "    GAAAG    ", "    IIIII    " },
                    { "AAA BCCCB AAA", "AAA BCCCB AAA", "AAA B   B AAA", "             ", "             ",
                            "             ", "             ", "             ", "             ", "             ",
                            "             ", "             " }
            });

    public static final List<TierPattern> ASCENDING = List.of(TIER_1, TIER_2, TIER_3);
    public static final List<TierPattern> DETECTION_ORDER = List.of(TIER_3, TIER_2, TIER_1);

    private HephaestusCauldronStructure() {}

    public static TierPattern forStructureTier(int structureTier) {
        return ASCENDING.get(Math.clamp(structureTier, 0, ASCENDING.size() - 1));
    }

    public record TierPattern(FoundryTier tier, char controllerMarker, char hatchMarker, char poolMarker,
                              char sourceMarker, String[][] slices) {

        public TierPattern {
            if (slices.length == 0 || slices[0].length == 0 || slices[0][0].isEmpty()) {
                throw new IllegalArgumentException("Empty Hephaestus' Cauldron pattern");
            }
            int height = slices[0].length;
            int width = slices[0][0].length();
            for (String[] slice : slices) {
                if (slice.length != height) throw new IllegalArgumentException("Non-rectangular pattern height");
                for (String row : slice) {
                    if (row.length() != width) throw new IllegalArgumentException("Non-rectangular pattern width");
                }
            }
            if (positions(slices, controllerMarker).size() != 1) {
                throw new IllegalArgumentException("Pattern must contain exactly one controller");
            }
            if (positions(slices, sourceMarker).size() != 1) {
                throw new IllegalArgumentException("Pattern must contain exactly one source anchor");
            }
            if (positions(slices, hatchMarker).isEmpty()) {
                throw new IllegalArgumentException("Pattern must contain at least one legal hatch position");
            }
            if (positions(slices, poolMarker).isEmpty()) {
                throw new IllegalArgumentException("Pattern must contain a pool footprint");
            }
        }

        public int width() {
            return slices[0][0].length();
        }

        public int height() {
            return slices[0].length;
        }

        public int depth() {
            return slices.length;
        }

        public FoundryRenderAnchor sourceAnchor() {
            return relative(positions(sourceMarker).getFirst());
        }

        public List<FoundryRenderAnchor> poolFootprint() {
            return positions(poolMarker).stream().map(this::relative).toList();
        }

        public Bounds poolBounds() {
            List<FoundryRenderAnchor> points = poolFootprint();
            return new Bounds(
                    points.stream().mapToDouble(FoundryRenderAnchor::right).min().orElseThrow(),
                    points.stream().mapToDouble(FoundryRenderAnchor::right).max().orElseThrow(),
                    points.stream().mapToDouble(FoundryRenderAnchor::up).min().orElseThrow(),
                    points.stream().mapToDouble(FoundryRenderAnchor::up).max().orElseThrow(),
                    points.stream().mapToDouble(FoundryRenderAnchor::forward).min().orElseThrow(),
                    points.stream().mapToDouble(FoundryRenderAnchor::forward).max().orElseThrow());
        }

        public int markerCount(char marker) {
            return positions(marker).size();
        }

        private FoundryRenderAnchor relative(Position position) {
            Position controller = positions(controllerMarker).getFirst();
            return new FoundryRenderAnchor(
                    -(position.column - controller.column),
                    position.row - controller.row,
                    -(position.slice - controller.slice));
        }

        private List<Position> positions(char marker) {
            return positions(slices, marker);
        }

        private static List<Position> positions(String[][] slices, char marker) {
            List<Position> result = new ArrayList<>();
            for (int slice = 0; slice < slices.length; slice++) {
                for (int row = 0; row < slices[slice].length; row++) {
                    for (int column = 0; column < slices[slice][row].length(); column++) {
                        if (slices[slice][row].charAt(column) == marker) {
                            result.add(new Position(slice, row, column));
                        }
                    }
                }
            }
            return List.copyOf(result);
        }
    }

    public record Bounds(double minRight, double maxRight, double minUp, double maxUp, double minForward,
                         double maxForward) {}

    private record Position(int slice, int row, int column) {}
}
