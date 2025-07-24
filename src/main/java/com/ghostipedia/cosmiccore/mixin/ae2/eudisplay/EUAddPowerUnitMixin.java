package com.ghostipedia.cosmiccore.mixin.ae2.eudisplay;

import appeng.api.config.PowerUnits;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

// Reasoning: The Power Unit enum is used to store
// the different power units and their lang/conversion
// factors in AE2. We want to add EU to this.
@Mixin(value = PowerUnits.class, remap = false)
public class EUAddPowerUnitMixin {

    @Unique
    private static PowerUnits cosmicCore$EU_UNIT;

    @Invoker(value = "<init>", remap = false)
    private static PowerUnits cosmicCore$invokeConstructor(String internalName, int ordinal, String unlocalizedName,
                                                           String textRepresentation) {
        throw new AssertionError();
    }

    @ModifyReturnValue(method = "values", at = @At("RETURN"), remap = false)
    private static PowerUnits[] cosmicCore$addEUToValues(PowerUnits[] original) {
        if (cosmicCore$EU_UNIT == null) {
            cosmicCore$EU_UNIT = cosmicCore$invokeConstructor("EU", original.length, "gui.ae2.units.eu", "EU");
            // Conversion ratio of EU to AE = EU to FE * FE to AE;
            float FEtoAE = 0.5f;
            float EUtoFE = 4f;
            cosmicCore$EU_UNIT.conversionRatio = EUtoFE * FEtoAE;
        }

        for (PowerUnits unit : original) {
            if (unit == cosmicCore$EU_UNIT) {
                return original;
            }
        }

        PowerUnits[] newArray = Arrays.copyOf(original, original.length + 1);
        newArray[original.length] = cosmicCore$EU_UNIT;

        return newArray;
    }
}
