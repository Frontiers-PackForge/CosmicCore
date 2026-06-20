package com.ghostipedia.cosmiccore.common.reflection;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Threshold encounters - mandatory reflection dialogues at erosion milestones.
 * No bargain is offered - just the Reflection observing the player's degradation.
 */
public class ThresholdEncounter {

    // Number of dialogue lines per threshold (for building the list)
    private static final int[] DIALOGUE_COUNTS = { 4, 4, 4, 4, 4, 4, 3, 4, 4, 4 };

    /**
     * Get the dialogue for a specific erosion threshold.
     * 
     * @param thresholdIndex 0-9 matching ReflectionConstants.THRESHOLDS
     */
    public static List<Component> getDialogue(int thresholdIndex) {
        if (thresholdIndex < 0 || thresholdIndex >= DIALOGUE_COUNTS.length) {
            return List.of(ReflectionLang.thresholdDialogue(0, 0)); // Fallback
        }

        List<Component> dialogue = new ArrayList<>();
        int count = DIALOGUE_COUNTS[thresholdIndex];
        for (int i = 0; i < count; i++) {
            dialogue.add(ReflectionLang.thresholdDialogue(thresholdIndex, i));
        }
        return dialogue;
    }

    /**
     * Get the question/prompt for a specific threshold.
     * These are rhetorical - player can only acknowledge.
     */
    public static Component getQuestion(int thresholdIndex) {
        return ReflectionLang.thresholdQuestion(thresholdIndex);
    }

    /**
     * Get the response to the player's acknowledgment.
     */
    public static Component getAcknowledgeResponse(int thresholdIndex) {
        return ReflectionLang.thresholdResponse(thresholdIndex);
    }
}
