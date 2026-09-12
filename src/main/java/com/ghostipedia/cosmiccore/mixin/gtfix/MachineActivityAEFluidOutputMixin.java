package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;

import appeng.api.stacks.AEFluidKey;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine$FluidStorageDelegate",
       remap = false)
public abstract class MachineActivityAEFluidOutputMixin {

    @WrapOperation(method = "fill",
                   at = @At(value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/objects/Object2LongMap;put(Ljava/lang/Object;J)J"))
    private long cosmiccore$output(Object2LongMap<Object> storage, Object key, long amount, Operation<Long> original) {
        long previous = original.call(storage, key, amount);
        if (key instanceof AEFluidKey fluid) ActivityScope.fluid(fluid.toStack(1), amount - previous, false);
        return previous;
    }
}
