package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.TwelvefoldConductorRegistration;

import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(value = Insulation.class, remap = false)
public abstract class InsulationTwelvefoldMixin {

    @Shadow
    @Final
    @Mutable
    private static Insulation[] $VALUES;

    @Inject(method = "<clinit>", at = @At("TAIL"), require = 1)
    private static void cosmiccore$appendTwelvefoldInsulations(CallbackInfo ci) {
        int wireOrdinal = $VALUES.length;
        Insulation wire = InsulationConstructorInvoker.cosmiccore$create("WIRE_TWELVE", wireOrdinal, "twelve_wire",
                0.6875f, 12, 3, TwelvefoldConductorRegistration.wirePrefix(), -1);
        Insulation cable = InsulationConstructorInvoker.cosmiccore$create("CABLE_TWELVE", wireOrdinal + 1,
                "twelve_cable", 0.75f, 12, 1, TwelvefoldConductorRegistration.cablePrefix(), 4);
        Insulation[] extended = Arrays.copyOf($VALUES, $VALUES.length + 2);
        extended[wireOrdinal] = wire;
        extended[wireOrdinal + 1] = cable;
        $VALUES = extended;
        TwelvefoldConductorRegistration.bindInsulations(wire, cable);
    }

    @ModifyReturnValue(method = "getUninsulated()Lcom/gregtechceu/gtceu/common/pipelike/cable/Insulation;",
                       at = @At("RETURN"),
                       require = 1)
    private Insulation cosmiccore$pairTwelvefoldCable(Insulation original) {
        if ((Object) this == TwelvefoldConductorRegistration.cableInsulation()) {
            return TwelvefoldConductorRegistration.wireInsulation();
        }
        return original;
    }
}
