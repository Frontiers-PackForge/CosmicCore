package com.ghostipedia.cosmiccore.common.machine.foundry;

import java.util.ArrayDeque;

public final class FoundryPatternAirspace {

    public static final char EXTERIOR_MARKER = '#';

    private FoundryPatternAirspace() {}

    public static String[][] classify(String[][] authoredSlices) {
        int depth = authoredSlices.length;
        int height = authoredSlices[0].length;
        int width = authoredSlices[0][0].length();
        String[][] classified = new String[depth][height];
        for (int row = 0; row < height; row++) {
            boolean[][] exterior = new boolean[depth][width];
            var pending = new ArrayDeque<Integer>();
            for (int slice = 0; slice < depth; slice++) {
                for (int column = 0; column < width; column++) {
                    if (slice == 0 || slice == depth - 1 || column == 0 || column == width - 1) {
                        pending.add(slice * width + column);
                    }
                }
            }
            while (!pending.isEmpty()) {
                int position = pending.removeFirst();
                int slice = position / width;
                int column = position % width;
                if (exterior[slice][column] || authoredSlices[slice][row].charAt(column) != ' ') continue;
                exterior[slice][column] = true;
                if (slice > 0) pending.add(position - width);
                if (slice + 1 < depth) pending.add(position + width);
                if (column > 0) pending.add(position - 1);
                if (column + 1 < width) pending.add(position + 1);
            }
            for (int slice = 0; slice < depth; slice++) {
                classified[slice][row] = classifyRow(authoredSlices[slice][row], exterior[slice]);
            }
        }
        return classified;
    }

    private static String classifyRow(String authored, boolean[] exterior) {
        char[] classified = authored.toCharArray();
        for (int index = 0; index < classified.length; index++) {
            if (exterior[index]) {
                classified[index] = EXTERIOR_MARKER;
            }
        }
        return new String(classified);
    }
}
