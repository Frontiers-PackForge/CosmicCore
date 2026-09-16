package com.ghostipedia.cosmiccore.common.machine.foundry;

public final class FoundryPatternAirspace {

    public static final char EXTERIOR_MARKER = '#';

    private FoundryPatternAirspace() {}

    public static String[][] classify(String[][] authoredSlices) {
        String[][] classified = new String[authoredSlices.length][];
        for (int slice = 0; slice < authoredSlices.length; slice++) {
            classified[slice] = new String[authoredSlices[slice].length];
            for (int row = 0; row < authoredSlices[slice].length; row++) {
                classified[slice][row] = classifyRow(authoredSlices[slice][row]);
            }
        }
        return classified;
    }

    private static String classifyRow(String authored) {
        int first = -1;
        int last = -1;
        for (int index = 0; index < authored.length(); index++) {
            if (authored.charAt(index) != ' ') {
                if (first < 0) first = index;
                last = index;
            }
        }
        char[] classified = authored.toCharArray();
        for (int index = 0; index < classified.length; index++) {
            if (classified[index] == ' ' && (first < 0 || index < first || index > last)) {
                classified[index] = EXTERIOR_MARKER;
            }
        }
        return new String(classified);
    }
}
