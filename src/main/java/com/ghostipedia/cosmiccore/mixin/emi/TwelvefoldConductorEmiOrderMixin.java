package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.common.power.TwelvefoldConductorRegistration;

import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;

import net.minecraft.world.item.BlockItem;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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
        List<EmiStack> twelvefold = EmiStackList.stacks.stream()
                .filter(TwelvefoldConductorEmiOrderMixin::cosmiccore$isTwelvefold)
                .toList();
        if (twelvefold.isEmpty()) {
            return;
        }
        EmiStackList.stacks.removeAll(twelvefold);
        for (EmiStack stack : twelvefold) {
            CableBlock conductor = cosmiccore$conductor(stack);
            if (conductor == null) {
                continue;
            }
            Insulation hex = conductor.pipeType.isCable() ? Insulation.CABLE_HEX : Insulation.WIRE_HEX;
            Insulation octal = conductor.pipeType.isCable() ? Insulation.CABLE_OCTAL : Insulation.WIRE_OCTAL;
            int insertionIndex = cosmiccore$indexOf(conductor, hex);
            if (insertionIndex < 0) {
                int octalIndex = cosmiccore$indexOf(conductor, octal);
                insertionIndex = octalIndex < 0 ? EmiStackList.stacks.size() : octalIndex + 1;
            }
            EmiStackList.stacks.add(insertionIndex, stack);
        }
    }

    private static boolean cosmiccore$isTwelvefold(EmiStack stack) {
        CableBlock conductor = cosmiccore$conductor(stack);
        return conductor != null && (conductor.pipeType == TwelvefoldConductorRegistration.wireInsulation() ||
                conductor.pipeType == TwelvefoldConductorRegistration.cableInsulation());
    }

    private static int cosmiccore$indexOf(CableBlock target, Insulation insulation) {
        for (int index = 0; index < EmiStackList.stacks.size(); index++) {
            CableBlock candidate = cosmiccore$conductor(EmiStackList.stacks.get(index));
            if (candidate != null && candidate.material == target.material && candidate.pipeType == insulation) {
                return index;
            }
        }
        return -1;
    }

    private static CableBlock cosmiccore$conductor(EmiStack stack) {
        if (stack.getKey() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CableBlock conductor) {
            return conductor;
        }
        return null;
    }
}
