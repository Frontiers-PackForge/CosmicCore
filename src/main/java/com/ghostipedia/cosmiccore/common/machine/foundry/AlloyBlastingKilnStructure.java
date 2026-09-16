package com.ghostipedia.cosmiccore.common.machine.foundry;

import java.util.ArrayList;
import java.util.List;

public final class AlloyBlastingKilnStructure {

    public static final char CONTROLLER_MARKER = 'G';
    public static final char HATCH_MARKER = 'B';
    public static final char POOL_MARKER = 'F';
    public static final char SOURCE_MARKER = 'H';
    public static final String[][] SLICES = {
            { "AAAAAAA", "AABGBAA", "AABBBAA", "CAAAAAC", " C   C ", " C   C ", "       ", "       ",
                    "       ", "       " },
            { "AAAAAAA", "ACCCCCA", "ACCCCCA", "ADCCCDA", "CDCCCDC", "CDCECDC", " DCECD ", " DCECD ",
                    " DCCCD ", " AAAAA " },
            { "AAAAAAA", "BCCCCCB", "BC   CB", "AC   CA", " C   C ", " C   C ", " C   C ", " C   C ",
                    " CFFFC ", " A   A " },
            { "AAAAAAA", "BCCCCCB", "BC   CB", "AC   CA", " C   C ", " E   E ", " E   E ", " E   E ",
                    " CFFFC ", " A H A " },
            { "AAAAAAA", "BCCCCCB", "BC   CB", "AC   CA", " C   C ", " C   C ", " C   C ", " C   C ",
                    " CFFFC ", " A   A " },
            { "AAAAAAA", "ACCCCCA", "ACCCCCA", "ADCCCDA", "CDCCCDC", "CDCECDC", " DCECD ", " DCECD ",
                    " DCCCD ", " AAAAA " },
            { "AAAAAAA", "AABBBAA", "AABBBAA", "CAAAAAC", " C   C ", " C   C ", "       ", "       ",
                    "       ", "       " }
    };

    static {
        validate();
    }

    private AlloyBlastingKilnStructure() {}

    public static int width() {
        return SLICES[0][0].length();
    }

    public static int height() {
        return SLICES[0].length;
    }

    public static int depth() {
        return SLICES.length;
    }

    public static int markerCount(char marker) {
        return positions(marker).size();
    }

    public static FoundryRenderAnchor receivingAnchor() {
        return relative(positions(SOURCE_MARKER).getFirst());
    }

    public static List<FoundryRenderAnchor> poolFootprint() {
        return positions(POOL_MARKER).stream().map(AlloyBlastingKilnStructure::relative).toList();
    }

    public static HephaestusCauldronStructure.Bounds poolBounds() {
        List<FoundryRenderAnchor> points = poolFootprint();
        return new HephaestusCauldronStructure.Bounds(
                points.stream().mapToDouble(FoundryRenderAnchor::right).min().orElseThrow(),
                points.stream().mapToDouble(FoundryRenderAnchor::right).max().orElseThrow(),
                points.stream().mapToDouble(FoundryRenderAnchor::up).min().orElseThrow(),
                points.stream().mapToDouble(FoundryRenderAnchor::up).max().orElseThrow(),
                points.stream().mapToDouble(FoundryRenderAnchor::forward).min().orElseThrow(),
                points.stream().mapToDouble(FoundryRenderAnchor::forward).max().orElseThrow());
    }

    private static void validate() {
        if (SLICES.length == 0 || SLICES[0].length == 0 || SLICES[0][0].isEmpty()) {
            throw new IllegalArgumentException("Empty Alloy Blasting Kiln pattern");
        }
        int height = height();
        int width = width();
        for (String[] slice : SLICES) {
            if (slice.length != height) throw new IllegalArgumentException("Non-rectangular pattern height");
            for (String row : slice) {
                if (row.length() != width) throw new IllegalArgumentException("Non-rectangular pattern width");
            }
        }
        if (positions(CONTROLLER_MARKER).size() != 1) {
            throw new IllegalArgumentException("Pattern must contain exactly one controller");
        }
        if (positions(SOURCE_MARKER).size() != 1) {
            throw new IllegalArgumentException("Pattern must contain exactly one receiving anchor");
        }
        if (positions(HATCH_MARKER).isEmpty()) {
            throw new IllegalArgumentException("Pattern must contain at least one legal hatch position");
        }
        if (positions(POOL_MARKER).isEmpty()) {
            throw new IllegalArgumentException("Pattern must contain a pool footprint");
        }
    }

    private static FoundryRenderAnchor relative(Position position) {
        Position controller = positions(CONTROLLER_MARKER).getFirst();
        return new FoundryRenderAnchor(
                -(position.column - controller.column),
                position.row - controller.row,
                -(position.slice - controller.slice));
    }

    private static List<Position> positions(char marker) {
        List<Position> result = new ArrayList<>();
        for (int slice = 0; slice < SLICES.length; slice++) {
            for (int row = 0; row < SLICES[slice].length; row++) {
                for (int column = 0; column < SLICES[slice][row].length(); column++) {
                    if (SLICES[slice][row].charAt(column) == marker) {
                        result.add(new Position(slice, row, column));
                    }
                }
            }
        }
        return List.copyOf(result);
    }

    private record Position(int slice, int row, int column) {}
}
