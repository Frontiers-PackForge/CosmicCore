package com.ghostipedia.cosmiccore.common.reflection.whisper;

import com.ghostipedia.cosmiccore.common.reflection.IReflection;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * The Whisper System - ambient comments from the reflection.
 * Event-based, contextual, scales with corruption.
 */
public final class WhisperSystem {

    private WhisperSystem() {}

    private static final Random RANDOM = new Random();

    // Cooldown tracking to prevent spam
    private static final Map<UUID, Long> lastWhisperTime = new HashMap<>();
    private static final long WHISPER_COOLDOWN = 30000L; // 30 seconds minimum between whispers

    // Style for whispers
    private static final Style WHISPER_STYLE = Style.EMPTY.withItalic(true).withColor(0x9966CC);

    /**
     * Called periodically to check for ambient whispers.
     */
    public static void tick(ServerPlayer player) {
        ReflectionCapability.get(player).ifPresent(reflection -> {
            if (!reflection.hasAwakened()) return;

            // Check various conditions for contextual whispers
            checkHealthWhisper(player, reflection);
            checkIdleWhisper(player, reflection);
            checkDimensionWhisper(player, reflection);
        });
    }

    /**
     * Trigger a whisper for a specific event.
     */
    public static void triggerEvent(ServerPlayer player, WhisperEvent event) {
        ReflectionCapability.get(player).ifPresent(reflection -> {
            if (!reflection.hasAwakened()) return;
            if (isOnCooldown(player)) return;

            List<String> lines = getEventLines(event, reflection);
            if (lines.isEmpty()) return;

            String line = lines.get(RANDOM.nextInt(lines.size()));
            sendWhisper(player, line);
        });
    }

    /**
     * Send a whisper to the player.
     */
    public static void sendWhisper(ServerPlayer player, String text) {
        if (isOnCooldown(player)) return;

        Component message = Component.literal("* " + text + " *").withStyle(WHISPER_STYLE);
        player.sendSystemMessage(message);
        lastWhisperTime.put(player.getUUID(), System.currentTimeMillis());
    }

    /**
     * Send a whisper with custom formatting.
     */
    public static void sendWhisper(ServerPlayer player, Component text) {
        if (isOnCooldown(player)) return;

        player.sendSystemMessage(text);
        lastWhisperTime.put(player.getUUID(), System.currentTimeMillis());
    }

    private static boolean isOnCooldown(Player player) {
        Long lastTime = lastWhisperTime.get(player.getUUID());
        if (lastTime == null) return false;
        return (System.currentTimeMillis() - lastTime) < WHISPER_COOLDOWN;
    }

    // ---- Condition Checks ----

    private static void checkHealthWhisper(ServerPlayer player, IReflection reflection) {
        if (player.getHealth() < player.getMaxHealth() * 0.25f) {
            if (RANDOM.nextFloat() < 0.05f) { // 5% chance per tick check
                triggerEvent(player, WhisperEvent.LOW_HEALTH);
            }
        }
    }

    private static void checkIdleWhisper(ServerPlayer player, IReflection reflection) {
        // Check if player has been standing still
        if (player.getDeltaMovement().lengthSqr() < 0.001) {
            if (RANDOM.nextFloat() < 0.01f) { // 1% chance per tick check
                triggerEvent(player, WhisperEvent.IDLE);
            }
        }
    }

    private static void checkDimensionWhisper(ServerPlayer player, IReflection reflection) {
        // Random ambient whispers based on dimension
        if (RANDOM.nextFloat() < 0.005f) { // 0.5% chance per tick check
            triggerEvent(player, WhisperEvent.AMBIENT);
        }
    }

    // ---- Line Pools ----

    private static List<String> getEventLines(WhisperEvent event, IReflection reflection) {
        int erosion = reflection.getErosion();
        int colorTier = ReflectionConstants.getSoulColorTier(erosion);

        return switch (event) {
            case DEATH -> getDeathLines(reflection, colorTier);
            case LOW_HEALTH -> getLowHealthLines(colorTier);
            case LOW_OXYGEN -> getLowOxygenLines(colorTier);
            case IDLE -> getIdleLines(colorTier);
            case ENTERED_DIMENSION -> getDimensionLines(colorTier);
            case POST_BARGAIN -> getPostBargainLines(colorTier);
            case AMBIENT -> getAmbientLines(colorTier);
            case COMBAT_KILL -> getCombatKillLines(colorTier);
        };
    }

    private static List<String> getDeathLines(IReflection reflection, int tier) {
        int deathCount = reflection.getDeathCount();

        List<String> lines = new ArrayList<>();

        // Low corruption
        if (tier <= 1) {
            lines.add("Welcome back.");
            lines.add("That one was faster than usual.");
            lines.add("I felt it too. I always do.");
            lines.add("Does it still hurt? I can never tell.");
        }

        // Mid corruption
        if (tier >= 2 && tier <= 4) {
            lines.add("Again. And again.");
            lines.add("We're getting used to this, aren't we?");
            lines.add("That's " + deathCount + " now. I've been counting.");
            lines.add("The dying is easy. It's the coming back that wears on us.");
        }

        // High corruption
        if (tier >= 5) {
            lines.add("Another one.");
            lines.add("Do you even notice anymore?");
            lines.add("We've done this " + deathCount + " times. It means nothing now.");
            lines.add("Death is just... punctuation.");
        }

        return lines;
    }

    private static List<String> getLowHealthLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 2) {
            lines.add("Careful. That looks like it hurts.");
            lines.add("You're bleeding. Well, we're bleeding.");
            lines.add("Should I be worried? Should we?");
        }

        if (tier >= 3) {
            lines.add("Pain is just information.");
            lines.add("We've felt worse.");
            lines.add("This body is temporary anyway.");
        }

        return lines;
    }

    private static List<String> getLowOxygenLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 2) {
            lines.add("Breathe. Oh wait.");
            lines.add("The air is thin here. Or is it us?");
            lines.add("Mortals panic when this happens. What do we do?");
        }

        if (tier >= 3) {
            lines.add("Still clinging to that breathing habit.");
            lines.add("We don't need air. We just think we do.");
            lines.add("Let go. It won't hurt for long.");
        }

        return lines;
    }

    private static List<String> getIdleLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 2) {
            lines.add("Thinking? Or avoiding?");
            lines.add("I'm still here, Whenever you're ready.");
            lines.add("Take your time, We have plenty.");
        }

        if (tier >= 3) {
            lines.add("You can't run from me by standing still.");
            lines.add("I'm right here, I'm always right here.");
            lines.add("The silence between us speaks volumes.");
        }

        if (tier >= 5) {
            lines.add("Are you listening? Or am I talking to myself?");
            lines.add("Sometimes I forget which one of us is which.");
            lines.add("...");
        }

        return lines;
    }

    private static List<String> getDimensionLines(int tier) {
        List<String> lines = new ArrayList<>();

        lines.add("Somewhere new. Somewhere dangerous. Good.");
        lines.add("What do you think we'll find here?");
        lines.add("This place remembers things. Be careful what you show it.");
        lines.add("The rules are different here. Can you feel it?");

        return lines;
    }

    private static List<String> getPostBargainLines(int tier) {
        List<String> lines = new ArrayList<>();

        lines.add("How does it feel?");
        lines.add("We're changing. Can you tell?");
        lines.add("No going back now. Isn't that freeing?");
        lines.add("Look at us. Look at what we're becoming.");

        return lines;
    }

    private static List<String> getAmbientLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 1) {
            lines.add("...");
            lines.add("I'm watching.");
            lines.add("Interesting choice.");
        }

        if (tier >= 2 && tier <= 4) {
            lines.add("We're doing well. Aren't we?");
            lines.add("Keep going. I want to see what happens.");
            lines.add("You're stronger than you think. We both are.");
        }

        if (tier >= 5) {
            lines.add("Beautiful, isn't it? What we've become?");
            lines.add("They wouldn't understand. Only we do.");
            lines.add("Almost there. Almost...");
        }

        return lines;
    }

    private static List<String> getCombatKillLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 2) {
            lines.add("That was necessary. Wasn't it?");
            lines.add("They're gone. We're still here.");
        }

        if (tier >= 3) {
            lines.add("More.");
            lines.add("Again.");
            lines.add("Good.");
        }

        return lines;
    }

    /**
     * Whisper event types.
     */
    public enum WhisperEvent {
        DEATH,
        LOW_HEALTH,
        LOW_OXYGEN,
        IDLE,
        ENTERED_DIMENSION,
        POST_BARGAIN,
        AMBIENT,
        COMBAT_KILL
    }
}
