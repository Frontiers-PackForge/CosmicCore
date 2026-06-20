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
@Debug(export = true)
@Mixin(value = PowerUnits.class, remap = false)
public class EUAddPowerUnitMixin {

    @Unique
    private static PowerUnits cosCore$EU_UNIT;

    @Invoker(value = "<init>", remap = false)
    private static PowerUnits cosCore$invokeConstructor(String internalName, int ordinal, String unlocalizedName,
                                                        String textRepresentation) {
        throw new AssertionError();
    }

    @ModifyReturnValue(method = "values", at = @At("RETURN"), remap = false)
    private static PowerUnits[] cosCore$addEUToValues(PowerUnits[] original) {
        if (cosCore$EU_UNIT == null) {
            cosCore$EU_UNIT = cosCore$invokeConstructor("EU", original.length, "gui.ae2.units.eu", "EU");
            // Conversion ratio of EU to AE = EU to FE * FE to AE;
            float FEtoAE = 0.5f;
            float EUtoFE = 4f;
            cosCore$EU_UNIT.conversionRatio = EUtoFE * FEtoAE;
        }

        for (PowerUnits unit : original) {
            if (unit == cosCore$EU_UNIT) {
                return original;
            }
        }

        PowerUnits[] newArray = Arrays.copyOf(original, original.length + 1);
        newArray[original.length] = cosCore$EU_UNIT;

        return newArray;
    }
}
