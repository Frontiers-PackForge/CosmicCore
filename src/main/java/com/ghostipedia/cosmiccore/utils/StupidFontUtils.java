package com.ghostipedia.cosmiccore.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StupidFontUtils {

    // The constant map directly populated with Character to Integer mappings.
    // It's declared as public static final, making it a constant accessible throughout your application.
    // Collections.unmodifiableMap ensures that its contents cannot be changed after initialization.
    public static final Map<Character, Integer> CHARACTER_VALUES;

    // A static initializer block to populate the map when the class is loaded.
    // This runs only once.
    static {
        Map<Character, Integer> tempMap = new HashMap<>();

        // Hardcoding all the character-to-integer mappings
        tempMap.put('A', 6);
        tempMap.put('B', 6);
        tempMap.put('C', 6);
        tempMap.put('D', 6);
        tempMap.put('E', 6);
        tempMap.put('F', 6);
        tempMap.put('G', 6);
        tempMap.put('H', 6);
        tempMap.put('I', 4);
        tempMap.put('J', 6);
        tempMap.put('K', 6);
        tempMap.put('L', 6);
        tempMap.put('M', 6);
        tempMap.put('N', 6);
        tempMap.put('O', 6);
        tempMap.put('P', 6);
        tempMap.put('Q', 6);
        tempMap.put('R', 6);
        tempMap.put('S', 6);
        tempMap.put('T', 6);
        tempMap.put('U', 6);
        tempMap.put('V', 6);
        tempMap.put('W', 6);
        tempMap.put('X', 6);
        tempMap.put('Y', 6);
        tempMap.put('Z', 6);

        tempMap.put('a', 6);
        tempMap.put('b', 6);
        tempMap.put('c', 6);
        tempMap.put('d', 6);
        tempMap.put('e', 6);
        tempMap.put('f', 5);
        tempMap.put('g', 6);
        tempMap.put('h', 6);
        tempMap.put('i', 2);
        tempMap.put('j', 6);
        tempMap.put('k', 5);
        tempMap.put('l', 3);
        tempMap.put('m', 6);
        tempMap.put('n', 6);
        tempMap.put('o', 6);
        tempMap.put('p', 6);
        tempMap.put('q', 6);
        tempMap.put('r', 6);
        tempMap.put('s', 6);
        tempMap.put('t', 4);
        tempMap.put('u', 6);
        tempMap.put('v', 6);
        tempMap.put('w', 6);
        tempMap.put('x', 6);
        tempMap.put('y', 6);
        tempMap.put('z', 6);

        tempMap.put('0', 6);
        tempMap.put('1', 6);
        tempMap.put('2', 6);
        tempMap.put('3', 6);
        tempMap.put('4', 6);
        tempMap.put('5', 6);
        tempMap.put('6', 6);
        tempMap.put('7', 6);
        tempMap.put('8', 6);
        tempMap.put('9', 6);

        tempMap.put(' ', 4);
        tempMap.put(',', 2);
        tempMap.put('.', 2);
        tempMap.put(':', 2);
        tempMap.put('/', 6);
        tempMap.put('[', 6);
        tempMap.put(']', 6);
        tempMap.put('+', 6);
        tempMap.put('-', 6);

        // Make the map unmodifiable to ensure it cannot be changed after this point.
        CHARACTER_VALUES = Collections.unmodifiableMap(tempMap);
    }

    int getStringWidth(String string) {
        int width = 0;
        for (var character : string.toCharArray()) {
            if (CHARACTER_VALUES.containsKey(character)) {
                width += CHARACTER_VALUES.get(character);
            } else {
                // throw new IllegalArgumentException("Character " + character + " is not supported");
            }
        }
        return width;
    }
}
