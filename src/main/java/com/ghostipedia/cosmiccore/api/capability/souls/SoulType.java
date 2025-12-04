package com.ghostipedia.cosmiccore.api.capability.souls;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SoulType implements StringRepresentable {
    Impure("Impure"),
    Rusted("Rusted"),
    Proud("Proud"),
    Greedy("Greedy"),
    Envious("Envious"),
    Gluttonous("Gluttonous"),
    Wrathful("Wrathful"),
    Slothful("Slothful"),
    Temporal("Temporal");

    private final String name;

    SoulType(String name) {
        this.name = name;
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
}
