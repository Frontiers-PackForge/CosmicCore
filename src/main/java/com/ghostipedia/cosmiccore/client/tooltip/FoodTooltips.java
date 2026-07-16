package com.ghostipedia.cosmiccore.client.tooltip;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.lso.LsoFoodCompat;
import com.ghostipedia.cosmiccore.common.compat.qualityfood.QualityFoodCompat;
import com.ghostipedia.cosmiccore.common.food.AttributeSpec;
import com.ghostipedia.cosmiccore.common.food.BehaviorLine;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodRegistry;
import com.ghostipedia.cosmiccore.common.food.FoodDefinition;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class FoodTooltips {

    private FoodTooltips() {}

    public static final ResourceLocation FRAME = CosmicCore.id("tooltip/food");

    private static final String G_HEALTH = "❤";
    private static final String G_REGEN = "✚";
    private static final String G_DURATION = "⧖";
    private static final String G_ATTR = "◆";
    private static final String G_QUALITY = "✦";

    private static final String[] ROMAN = { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };

    public static FoodTooltipComponent buildVile() {
        return new FoodTooltipComponent(List.of(
                new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph("☠", 0xFFCF6679),
                        Component.translatable("cosmiccore.tooltip.food.vile"),
                        value(Component.translatable("cosmiccore.tooltip.food.vile_desc"), 0xE58A93)),
                new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph("⧖", 0xFFCF6679),
                        Component.translatable("cosmiccore.tooltip.food.vile_hunger"), value("", 0xC8A8AC))));
    }

    public static FoodTooltipComponent build(ItemStack stack, FoodDefinition def) {
        List<FoodTooltipComponent.Line> lines = new ArrayList<>();

        String family = CosmicFoodRegistry.archetypeNameFor(stack);
        Component familyLabel = switch (family) {
            case "defined" -> Component.translatable("cosmiccore.food.family.defined");
            case "auto" -> Component.translatable("cosmiccore.food.family.auto");
            default -> Component.translatableWithFallback("cosmiccore.food.family." + family,
                    Character.toUpperCase(family.charAt(0)) + family.substring(1));
        };
        Component role = CosmicFoodRegistry.plateRole(stack, family).label();
        lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph("✦", 0xFFB9A5E3),
                familyLabel, value(role, 0xCFC0EE)));

        int quality = QualityFoodCompat.level(stack);
        double qualityMultiplier = QualityFoodCompat.multiplier(quality);
        if (quality > 0) {
            int color = qualityColor(quality);
            double bonus = (qualityMultiplier - 1.0) * 100.0;
            lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph(G_QUALITY, color),
                    Component.translatable("cosmiccore.tooltip.food.quality"), value(signed(bonus) + "%", color)));
        }

        double hearts = FoodDefinition.heartsFromHealth(def.heartBonus() * qualityMultiplier);
        if (hearts != 0) {
            lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph(G_HEALTH, 0xFFFF6B6B),
                    Component.translatable("cosmiccore.tooltip.food.max_health"),
                    value(signed(hearts) + " " + G_HEALTH, 0xFF8F8F)));
        }
        double regen = def.regenBonus() * qualityMultiplier;
        if (regen != 0) {
            lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph(G_REGEN, 0xFFFF9BCE),
                    Component.translatable("cosmiccore.tooltip.food.regen"),
                    value(signed(regen) + "/s", 0xFFB3DA)));
        }
        for (FoodDefinition.EffectSpec spec : def.effects()) {
            lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Effect(spec.effect()),
                    spec.effect().value().getDisplayName().copy(), value(roman(spec.amplifier() + 1), 0x8EE6B0)));
        }
        for (FoodDefinition.ConsumeEffectSpec spec : def.consumeEffects()) {
            boolean harmful = spec.effect().value().getCategory() == MobEffectCategory.HARMFUL;
            boolean beneficial = spec.effect().value().getCategory() == MobEffectCategory.BENEFICIAL;
            int effectTicks = beneficial ? QualityFoodCompat.scaleDuration(spec.durationTicks(), quality) :
                    spec.durationTicks();
            lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Effect(spec.effect()),
                    spec.effect().value().getDisplayName().copy(),
                    value(roman(spec.amplifier() + 1) + " (" + mmss(effectTicks / 20) + ")",
                            harmful ? 0xFF8A80 : 0xC8C8C8)));
        }
        for (AttributeSpec spec : def.attributes()) {
            lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph(G_ATTR, 0xFF7FB2FF),
                    Component.translatable(spec.attribute().value().getDescriptionId()),
                    value(attrValue(spec), 0xA7CCFF)));
        }
        for (BehaviorLine behavior : def.behaviors()) {
            lines.add(new FoodTooltipComponent.Line(
                    new FoodTooltipComponent.Icon.Glyph(behavior.glyph(), behavior.color()),
                    Component.literal(behavior.label()), value(behavior.value(), behavior.color() & 0xFFFFFF)));
        }
        LsoFoodCompat.ConsumableTemp temp = LsoFoodCompat.temperature(stack);
        if (temp != null) {
            boolean warming = temp.level() > 0;
            String degrees = String.format("%+d°C (", (int) (temp.level() * LsoFoodCompat.DEGREES_PER_LEVEL)) +
                    mmss(temp.durationTicks() / 20) + ")";
            lines.add(new FoodTooltipComponent.Line(
                    new FoodTooltipComponent.Icon.Glyph(warming ? "♨" : "❆", warming ? 0xFFFF9E64 : 0xFF7FD4FF),
                    Component.translatable(
                            warming ? "cosmiccore.tooltip.food.warming" : "cosmiccore.tooltip.food.cooling"),
                    value(degrees, warming ? 0xFFB27F : 0xA8E4FF)));
        }
        lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph(G_DURATION, 0xFFFFD166),
                Component.translatable("cosmiccore.tooltip.food.duration"),
                value(mmss(QualityFoodCompat.scaleDuration(def.durationTicks(), quality) / 20), 0xFFDD8A)));

        return new FoodTooltipComponent(lines);
    }

    private static Component value(String text, int rgb) {
        return value(Component.literal(text), rgb);
    }

    private static Component value(Component text, int rgb) {
        return text.copy().withStyle(style -> style.withColor(rgb));
    }

    private static String signed(double v) {
        return v == Math.floor(v) ? String.format("%+d", (long) v) : String.format("%+.1f", v);
    }

    private static String attrValue(AttributeSpec spec) {
        if (spec.operation() == AttributeModifier.Operation.ADD_VALUE) return signed(spec.amount());
        return signed(spec.amount() * 100) + "%";
    }

    private static String mmss(int seconds) {
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    private static String roman(int n) {
        return n >= 0 && n < ROMAN.length ? ROMAN[n] : String.valueOf(n);
    }

    private static int qualityColor(int quality) {
        return switch (quality) {
            case 1 -> 0xFFC0C0C0;
            case 2 -> 0xFFFFD85A;
            case 3 -> 0xFF55FFFF;
            default -> 0xFFFFFFFF;
        };
    }
}
