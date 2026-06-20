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

    // Style for whispers — trailing thoughts, not a voice
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

        Component message = Component.literal("..." + text).withStyle(WHISPER_STYLE);
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
        List<String> lines = new ArrayList<>();

        if (tier <= 1) {
            lines.add("cold");
            lines.add("that ached");
            lines.add("the ground felt wrong");
            lines.add("something lingers");
        }

        if (tier >= 2 && tier <= 4) {
            lines.add("again");
            lines.add("familiar");
            lines.add("barely felt it");
            lines.add("the coming back is the hard part");
        }

        if (tier >= 5) {
            lines.add("nothing");
            lines.add("just punctuation");
            lines.add("barely noticed");
        }

        return lines;
    }

    private static List<String> getLowHealthLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 2) {
            lines.add("hands are shaking");
            lines.add("pulse in your ears");
            lines.add("something's wrong");
        }

        if (tier >= 3) {
            lines.add("just numbers");
            lines.add("temporary");
            lines.add("you've felt worse");
        }

        return lines;
    }

    private static List<String> getLowOxygenLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 2) {
            lines.add("chest tightening");
            lines.add("the air is thin");
            lines.add("breathe");
        }

        if (tier >= 3) {
            lines.add("still clinging to that habit");
            lines.add("do you even need it anymore?");
            lines.add("let go");
        }

        return lines;
    }

    private static List<String> getIdleLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 2) {
            lines.add("quiet");
            lines.add("what was that sound?");
            lines.add("the air feels heavy");
        }

        if (tier >= 3) {
            lines.add("something moved");
            lines.add("who were you before this?");
            lines.add("the stillness has a weight to it");
        }

        if (tier >= 5) {
            lines.add("are you listening to yourself?");
            lines.add("when did it get so quiet?");
        }

        return lines;
    }

    private static List<String> getDimensionLines(int tier) {
        List<String> lines = new ArrayList<>();

        lines.add("the rules are different here");
        lines.add("the air tastes wrong");
        lines.add("something remembers this place");
        lines.add("not home");

        return lines;
    }

    private static List<String> getPostBargainLines(int tier) {
        List<String> lines = new ArrayList<>();

        lines.add("lighter");
        lines.add("something's different");
        lines.add("the thread pulled clean");
        lines.add("can't undo that");

        return lines;
    }

    private static List<String> getAmbientLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 1) {
            lines.add("hm");
            lines.add("something flickered");
        }

        if (tier >= 2 && tier <= 4) {
            lines.add("getting used to this");
            lines.add("familiar");
            lines.add("how long has it been?");
        }

        if (tier >= 5) {
            lines.add("is this what you wanted?");
            lines.add("almost");
            lines.add("what's left?");
        }

        return lines;
    }

    private static List<String> getCombatKillLines(int tier) {
        List<String> lines = new ArrayList<>();

        if (tier <= 2) {
            lines.add("necessary");
            lines.add("gone");
        }

        if (tier >= 3) {
            lines.add("easy");
            lines.add("next");
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
