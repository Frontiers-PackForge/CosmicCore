package com.ghostipedia.cosmiccore.api.gravity;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

public enum GravityMode implements StringRepresentable {

    NORMAL("normal"),
    FREE_DRIFT("free_drift"),
    DIRECTED("directed");

    public static final StringRepresentable.EnumCodec<GravityMode> CODEC = StringRepresentable
            .fromEnum(GravityMode::values);

    private final String serializedName;

    GravityMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }
}
