package com.ghostipedia.cosmiccore.utils;

import net.minecraft.ChatFormatting;

import static net.minecraft.ChatFormatting.*;

// CREDITS ; GTOCORE
public class StringUtil {

    private static String formatting(String input, ChatFormatting[] colours, double delay) {
        StringBuilder sb = new StringBuilder(input.length() * 3);
        if (delay <= 0.0D)
            delay = 0.001D;
        int offset = (int) Math.floor((System.currentTimeMillis() & 0x3FFFL) / delay) % colours.length;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            sb.append(colours[(colours.length + i - offset) % colours.length].toString());
            sb.append(c);
        }
        return sb.toString();
    }

    public static String midnightOscillation(String input) {
        return formatting(input, new ChatFormatting[] { DARK_PURPLE, DARK_RED }, 160.0D);
    }

    public static String rainbowDancing(String input) {
        return formatting(input, new ChatFormatting[] { RED, GOLD, YELLOW, GREEN, AQUA, BLUE, LIGHT_PURPLE }, 80.0D);
    }

    public static String goldFlicker(String input) {
        return formatting(input, new ChatFormatting[] { YELLOW, GOLD }, 240.0D);
    }

    public static String iceCold(String input) {
        return formatting(input, new ChatFormatting[] { BLUE, BLUE, BLUE, BLUE, WHITE, BLUE, WHITE, WHITE, BLUE,
                WHITE, WHITE, BLUE, AQUA, WHITE }, 360);
    }

    /**
     * Converts snake_case to Title Case (e.g., "hungering_void" -> "Hungering Void")
     */
    public static String toTitleCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) return snakeCase;

        StringBuilder result = new StringBuilder();
        for (String word : snakeCase.split("_")) {
            if (!word.isEmpty()) {
                if (result.length() > 0) result.append(" ");
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) result.append(word.substring(1));
            }
        }
        return result.toString();
    }
}
