package com.ghostipedia.cosmiccore.api.capability.souls;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SoulType implements StringRepresentable {
    Impure("impure", ChatFormatting.DARK_GRAY),
    Rusted("rusted", ChatFormatting.GRAY),
    Proud("proud", ChatFormatting.DARK_PURPLE),
    Greedy("greedy", ChatFormatting.YELLOW),
    Envious("envious", ChatFormatting.GREEN),
    Gluttonous("gluttonous", ChatFormatting.GOLD),
    Wrathful("wrathful", ChatFormatting.RED),
    Slothful("slothful", ChatFormatting.AQUA),
    Temporal("temporal", ChatFormatting.DARK_AQUA);


    private final String name;
    private final ChatFormatting color;

    SoulType(String name, ChatFormatting color) {
        this.name = name;
        this.color = color;
    }


    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public static final Codec<SoulType> CODEC = StringRepresentable.fromEnum(SoulType::values);

    private static final Map<String, SoulType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(SoulType::getSerializedName, Function.identity()));

    public static SoulType byName(String name) {
        return BY_NAME.get(name);
    }

    public Component toComponent(int amount) {
        return toComponent(amount, true);
    }

    public Component toComponent(int amount, boolean formatted) {
        MutableComponent nameComp = Component.translatable("cosmiccore.gui.soul." + name + ".name");
        MutableComponent amountComp = Component.literal(" : " + amount).withStyle(Style.EMPTY);
        if (formatted) {
            nameComp = nameComp.withStyle(ChatFormatting.BOLD, this.color);
            amountComp = amountComp.withStyle(ChatFormatting.RESET);
        }

        return nameComp.append(amountComp);
    }
}
