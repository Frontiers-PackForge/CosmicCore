package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import net.neoforged.neoforge.fluids.FluidStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$InternalSlot", remap = false)
public abstract class MEPatternBufferIngredientMatchingFixMixin {

    @WrapOperation(method = "add",
                   at = @At(value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/objects/Object2LongOpenHashMap;addTo(Ljava/lang/Object;J)J"))
    private long cosmiccore$mergeEquivalentFluids(Object2LongOpenHashMap<FluidStack> inventory, Object key, long amount,
                                                  Operation<Long> original) {
        FluidStack fluid = (FluidStack) key;
        for (var entry : inventory.object2LongEntrySet()) {
            if (FluidStack.isSameFluidSameComponents(entry.getKey(), fluid)) {
                long previous = entry.getLongValue();
                entry.setValue(previous + amount);
                return previous;
            }
        }
        return original.call(inventory, key, amount);
    }

    @WrapOperation(method = "deserializeNBT",
                   at = @At(value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/objects/Object2LongOpenHashMap;put(Ljava/lang/Object;J)J"))
    private long cosmiccore$mergeSavedEquivalentFluids(Object2LongOpenHashMap<FluidStack> inventory, Object key,
                                                       long amount, Operation<Long> original) {
        FluidStack fluid = (FluidStack) key;
        for (var entry : inventory.object2LongEntrySet()) {
            if (FluidStack.isSameFluidSameComponents(entry.getKey(), fluid)) {
                long previous = entry.getLongValue();
                entry.setValue(previous + amount);
                return previous;
            }
        }
        return original.call(inventory, key, amount);
    }
}
