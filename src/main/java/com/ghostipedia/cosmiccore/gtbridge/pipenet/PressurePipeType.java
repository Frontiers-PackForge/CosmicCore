package com.ghostipedia.cosmiccore.gtbridge.pipenet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.Statics;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public enum PressurePipeType implements IPipeType<PressurePipeData>, StringRepresentable {
    EV("ev", 0.125f, Statics.P[Statics.EV], Statics.DEFAULT_PRESSURE, 1.0D),
    IV("iv", 0.25f, Statics.P[Statics.IV], Statics.DEFAULT_PRESSURE, 1.25D),
    LUV("luv", 0.5f, Statics.P[Statics.LUV], Statics.DEFAULT_PRESSURE, 1.5D),
    ZPM("zpm", 0.625f, Statics.P[Statics.ZPM], Statics.DEFAULT_PRESSURE, 2.0D),
    UV("uv", 0.75f, Statics.P[Statics.UV], Statics.DEFAULT_PRESSURE, 3.0D),
    UHV("uhv", 0.825f, Statics.P[Statics.UHV], Statics.DEFAULT_PRESSURE, 5.0D),
    UEV("uev", 0.9f, Statics.P[Statics.UEV], Statics.DEFAULT_PRESSURE, 10.0D),
    UIV("uiv", 0.75f, Statics.DEFAULT_PRESSURE, Statics.P[Statics.UIV], 1.0D),
    UXV("uxv", 0.625f, Statics.DEFAULT_PRESSURE, Statics.P[Statics.UXV], 0.75D),
    OPV("opv", 0.5f, Statics.DEFAULT_PRESSURE, Statics.P[Statics.OPV], 0.625D),
    MAX("max", 0.375f, Statics.DEFAULT_PRESSURE, Statics.P[Statics.MAX], 0.5D);



    @Getter
    public final float thickness;
    @Getter
    public final String name;
    @Getter
    public final double maxPressure;
    @Getter
    private final double minPressure;
    @Getter
    private final double volume;

    public static final ResourceLocation TYPE_ID = CosmicCore.id("pressure");


    PressurePipeType(String name, float thickness, double minPressure, double maxPressure, double volume) {
        this.thickness = thickness;
        this.name = name;
        this.minPressure = minPressure;
        this.maxPressure = maxPressure;
        this.volume = volume;
    }

    @Nonnull
    @Override
    public PressurePipeData modifyProperties(PressurePipeData pipeData) {
        return new PressurePipeData(minPressure, maxPressure, volume, pipeData.connections);
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE_ID;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}