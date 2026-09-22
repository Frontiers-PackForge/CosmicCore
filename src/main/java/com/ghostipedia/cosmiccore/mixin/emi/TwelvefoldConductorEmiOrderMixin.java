package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.common.power.TwelvefoldConductorRegistration;
import com.ghostipedia.cosmiccore.integration.emi.TwelvefoldConductorOrder;

import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;

import net.minecraft.world.item.BlockItem;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiStackList.class, remap = false)
public abstract class TwelvefoldConductorEmiOrderMixin {

    @Inject(
            method = "bake()V",
            at = @At(
                     value = "INVOKE",
                     target = "Ljava/util/List;stream()Ljava/util/stream/Stream;",
                     ordinal = 0,
                     shift = At.Shift.BEFORE),
            require = 1)
    private static void cosmiccore$orderTwelvefoldConductors(CallbackInfo ci) {
        TwelvefoldConductorOrder.reorder(EmiStackList.stacks, TwelvefoldConductorEmiOrderMixin::cosmiccore$classify);
    }

    private static TwelvefoldConductorOrder.Entry cosmiccore$classify(EmiStack stack) {
        CableBlock conductor = cosmiccore$conductor(stack);
        if (conductor == null) {
            return null;
        }
        Insulation insulation = conductor.pipeType;
        if (insulation == TwelvefoldConductorRegistration.wireInsulation()) {
            return new TwelvefoldConductorOrder.Entry(conductor.material, false,
                    TwelvefoldConductorOrder.Form.TWELVEFOLD);
        }
        if (insulation == TwelvefoldConductorRegistration.cableInsulation()) {
            return new TwelvefoldConductorOrder.Entry(conductor.material, true,
                    TwelvefoldConductorOrder.Form.TWELVEFOLD);
        }
        if (insulation == Insulation.WIRE_OCTAL || insulation == Insulation.WIRE_HEX) {
            TwelvefoldConductorOrder.Form form = insulation == Insulation.WIRE_HEX ?
                    TwelvefoldConductorOrder.Form.HEX : TwelvefoldConductorOrder.Form.OCTAL;
            return new TwelvefoldConductorOrder.Entry(conductor.material, false, form);
        }
        if (insulation == Insulation.CABLE_OCTAL || insulation == Insulation.CABLE_HEX) {
            TwelvefoldConductorOrder.Form form = insulation == Insulation.CABLE_HEX ?
                    TwelvefoldConductorOrder.Form.HEX : TwelvefoldConductorOrder.Form.OCTAL;
            return new TwelvefoldConductorOrder.Entry(conductor.material, true, form);
        }
        return null;
    }

    private static CableBlock cosmiccore$conductor(EmiStack stack) {
        if (stack.getKey() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CableBlock conductor) {
            return conductor;
        }
        return null;
    }
}
