package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = ToolHelper.class, remap = false)
public abstract class HardHammerDropConversionFixMixin {

    @WrapOperation(
                   method = "applyHammerDropConversion(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/List;IFLnet/minecraft/util/RandomSource;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                            ordinal = 0),
                   require = 1)
    private static boolean cosmiccore$addCopiedNonOreDrop(List<ItemStack> drops, Object value,
                                                          Operation<Boolean> original,
                                                          @Local(argsOnly = true) float dropChance,
                                                          @Local(argsOnly = true) RandomSource random) {
        if (!cosmiccore$passesDropChance(dropChance, random)) return false;
        return original.call(drops, ((ItemStack) value).copy());
    }

    @WrapOperation(
                   method = "applyHammerDropConversion(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/List;IFLnet/minecraft/util/RandomSource;)V",
                   at = { @At(
                              value = "INVOKE",
                              target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                              ordinal = 1),
                           @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2) },
                   require = 2)
    private static boolean cosmiccore$addOreDropWithChance(List<ItemStack> drops, Object value,
                                                           Operation<Boolean> original,
                                                           @Local(argsOnly = true) float dropChance,
                                                           @Local(argsOnly = true) RandomSource random) {
        if (!cosmiccore$passesDropChance(dropChance, random)) return false;
        return original.call(drops, value);
    }

    @Unique
    private static boolean cosmiccore$passesDropChance(float dropChance, RandomSource random) {
        return dropChance >= 1.0F || random.nextFloat() <= dropChance;
    }
}
