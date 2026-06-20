package com.ghostipedia.cosmiccore.common.reflection.soul;

import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Set;

/**
 * Soul Shapes - fundamental forms your soul can take.
 * Each shape defines playstyle identity with a Super, affinities, and curses.
 *
 * Affinity system:
 * - Empowered categories get 50% bonus effect
 * - Cursed categories get 50% penalty
 * - Neutral categories get no modifier
 */
public enum SoulShape {

    UNSHAPED("unshaped", ChatFormatting.GRAY,
            Set.of(),
            Set.of()),
    REVENANT("revenant", ChatFormatting.DARK_RED,
            Set.of(BargainCategory.DEATH),
            Set.of()),
    HOLLOW("hollow", ChatFormatting.DARK_PURPLE,
            Set.of(BargainCategory.SUSTENANCE),
            Set.of()),
    ENGINE("engine", ChatFormatting.GOLD,
            Set.of(BargainCategory.UTILITY),
            Set.of()),
    GLOBEDANCER("globedancer", ChatFormatting.AQUA,
            Set.of(BargainCategory.MOBILITY),
            Set.of(BargainCategory.DEFENSE)),
    BULWARK("bulwark", ChatFormatting.DARK_GRAY,
            Set.of(BargainCategory.DEFENSE),
            Set.of(BargainCategory.MOBILITY)),
    BLOODTHIRST("bloodthirst", ChatFormatting.RED,
            Set.of(BargainCategory.OFFENSE),
            Set.of(BargainCategory.DEFENSE));

    private final String id;
    private final ChatFormatting color;
    private final Set<BargainCategory> empoweredCategories;
    private final Set<BargainCategory> cursedCategories;

    SoulShape(String id, ChatFormatting color, Set<BargainCategory> empowered, Set<BargainCategory> cursed) {
        this.id = id;
        this.color = color;
        this.empoweredCategories = empowered;
        this.cursedCategories = cursed;
    }

    public String getId() {
        return id;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getNameKey() {
        return "cosmiccore.soul_shape." + id + ".name";
    }

    public String getTaglineKey() {
        return "cosmiccore.soul_shape." + id + ".tagline";
    }

    public String getDescriptionKey() {
        return "cosmiccore.soul_shape." + id + ".description";
    }

    public String getSuperNameKey() {
        return "cosmiccore.soul_shape." + id + ".super.name";
    }

    public String getSuperDescriptionKey() {
        return "cosmiccore.soul_shape." + id + ".super.description";
    }

    public MutableComponent getFormattedName() {
        return Component.translatable(getNameKey()).withStyle(color);
    }

    public MutableComponent getFormattedTagline() {
        return Component.translatable(getTaglineKey()).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
    }

    public MutableComponent getDescription() {
        return Component.translatable(getDescriptionKey());
    }

    public MutableComponent getSuperName() {
        return Component.translatable(getSuperNameKey()).withStyle(color);
    }

    public MutableComponent getSuperDescription() {
        return Component.translatable(getSuperDescriptionKey());
    }

    public static SoulShape fromId(String id) {
        for (SoulShape shape : values()) {
            if (shape.id.equals(id)) {
                return shape;
            }
        }
        return UNSHAPED;
    }

    public boolean isShaped() {
        return this != UNSHAPED;
    }

    /**
     * @return categories this shape empowers (50% bonus)
     */
    public Set<BargainCategory> getEmpoweredCategories() {
        return empoweredCategories;
    }

    /**
     * @return categories this shape curses (50% penalty)
     */
    public Set<BargainCategory> getCursedCategories() {
        return cursedCategories;
    }

    /**
     * Check if this shape empowers a category.
     */
    public boolean empowers(BargainCategory category) {
        return empoweredCategories.contains(category);
    }

    /**
     * Check if this shape curses a category.
     */
    public boolean curses(BargainCategory category) {
        return cursedCategories.contains(category);
    }

    /**
     * Get the affinity multiplier for a bargain category.
     *
     * @return 1.5 if empowered, 0.5 if cursed, 1.0 otherwise
     */
    public float getAffinityMultiplier(BargainCategory category) {
        if (empoweredCategories.contains(category)) {
            return 1.5f;
        }
        if (cursedCategories.contains(category)) {
            return 0.5f;
        }
        return 1.0f;
    }
}
