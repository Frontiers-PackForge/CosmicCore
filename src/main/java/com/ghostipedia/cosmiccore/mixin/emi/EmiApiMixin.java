package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.utils.FluidUtilities;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
// Credits ; GTOCore - Thank you for this amazing mixin!

@Mixin(value = EmiApi.class, remap = false)
public abstract class EmiApiMixin {

    @ModifyVariable(method = "displayUses",
                    at = @At(value = "INVOKE",
                             target = "Ldev/emi/emi/api/stack/EmiIngredient;isEmpty()Z"),
                    remap = false,
                    argsOnly = true)
    private static EmiIngredient modifyDisplayUses(EmiIngredient stack) {
        return stack.isEmpty() ? stack : cosmicCore$getBucketFluid(stack);
    }

    @ModifyVariable(method = "displayRecipes",
                    at = @At(value = "INVOKE",
                             target = "Ljava/util/List;size()I"),
                    remap = false,
                    argsOnly = true)
    private static EmiIngredient modifyDisplayRecipes(EmiIngredient stack) {
        return stack.getEmiStacks().size() != 1 ? stack : cosmicCore$getBucketFluid(stack);
    }

    @Unique
    private static EmiIngredient cosmicCore$getBucketFluid(EmiIngredient stack) {
        if (stack instanceof EmiStack emiStack) {
            Fluid fluid = Fluids.EMPTY;
            if (emiStack.getKey() instanceof BucketItem bucketItem) {
                fluid = bucketItem.getFluid();
            } else if (emiStack.getKey() instanceof ComponentItem && emiStack.hasNbt()) {
                CompoundTag nbt = emiStack.getNbt();
                if (nbt.contains("Fluid", Tag.TAG_COMPOUND)) {
                    var fluidTag = nbt.getCompound("Fluid");
                    var fluidName = fluidTag.getString("FluidName");
                    fluid = FluidUtilities.getFluid(fluidName);
                }
            }
            return fluid == Fluids.EMPTY ? stack : EmiStack.of(fluid);
        }
        return stack;
    }
}
