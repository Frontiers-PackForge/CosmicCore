package com.ghostipedia.cosmiccore.client.tooltip;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public record FoodTooltipComponent(List<Line> lines) implements TooltipComponent {

    public record Line(Icon icon, Component label, Component value) {}

    public sealed interface Icon {

        record Glyph(String ch, int color) implements Icon {}

        record Effect(Holder<MobEffect> effect) implements Icon {}

        record None() implements Icon {}
    }
}
