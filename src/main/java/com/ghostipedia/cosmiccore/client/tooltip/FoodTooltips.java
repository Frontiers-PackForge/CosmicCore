package com.ghostipedia.cosmiccore.client.tooltip;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.food.AttributeSpec;
import com.ghostipedia.cosmiccore.common.food.BehaviorLine;
import com.ghostipedia.cosmiccore.common.food.FoodDefinition;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;

public final class FoodTooltips {

    private FoodTooltips() {}

    public static final ResourceLocation FRAME = CosmicCore.id("tooltip/food");

    private static final String G_HEALTH = "♥";
    private static final String G_REGEN = "✚";
    private static final String G_DURATION = "⌛";
    private static final String G_ATTR = "◆";

    private static final String[] ROMAN = { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };

    public static FoodTooltipComponent build(FoodDefinition def) {
        List<FoodTooltipComponent.Line> lines = new ArrayList<>();

        double hearts = def.heartBonus() / 2.0;
        if (hearts != 0) {
            lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph(G_HEALTH, 0xFFFF6B6B),
                    Component.literal("Max health"), value(signed(hearts) + " " + G_HEALTH, 0xFF8F8F)));
        }
        if (def.regenBonus() != 0) {
            lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph(G_REGEN, 0xFFFF9BCE),
                    Component.literal("Health regen"), value(signed(def.regenBonus()) + "/s", 0xFFB3DA)));
        }
        for (FoodDefinition.EffectSpec spec : def.effects()) {
            lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Effect(spec.effect()),
                    spec.effect().value().getDisplayName().copy(), value(roman(spec.amplifier() + 1), 0x8EE6B0)));
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
        lines.add(new FoodTooltipComponent.Line(new FoodTooltipComponent.Icon.Glyph(G_DURATION, 0xFFFFD166),
                Component.literal("Duration"), value(mmss(def.durationTicks() / 20), 0xFFDD8A)));

        return new FoodTooltipComponent(lines);
    }

    private static Component value(String text, int rgb) {
        return Component.literal(text).withStyle(style -> style.withColor(rgb));
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
}
