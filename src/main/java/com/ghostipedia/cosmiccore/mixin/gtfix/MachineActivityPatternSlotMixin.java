package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$InternalSlot", remap = false)
public abstract class MachineActivityPatternSlotMixin {

    @WrapOperation(method = { "handleItemInternal", "handleFluidInternal" },
                   at = @At(value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/objects/Object2LongMap$Entry;setValue(J)J"))
    private long cosmiccore$consume(Object2LongMap.Entry<?> entry, long value, Operation<Long> original) {
        Object key = entry.getKey();
        long previous = original.call(entry, value);
        cosmiccore$record(key, previous - value);
        return previous;
    }

    @WrapOperation(method = { "handleItemInternal", "handleFluidInternal" },
                   at = @At(value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/objects/ObjectIterator;remove()V"))
    private void cosmiccore$consumeAll(ObjectIterator<?> iterator, Operation<Void> original,
                                       @Local Object2LongMap.Entry<?> entry) {
        Object key = entry.getKey();
        long amount = entry.getLongValue();
        original.call(iterator);
        cosmiccore$record(key, amount);
    }

    private static void cosmiccore$record(Object key, long amount) {
        if (key instanceof ItemStack item) ActivityScope.item(item, amount, true);
        else if (key instanceof FluidStack fluid) ActivityScope.fluid(fluid, amount, true);
    }
}
