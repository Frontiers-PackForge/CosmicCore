package com.ghostipedia.cosmiccore.mixin.gttweak.armor;

import com.ghostipedia.cosmiccore.common.compat.gtceu.LegacyArmorAttributeMigration;

import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class LegacyArmorAttributeCodecMixin {

    @ModifyExpressionValue(method = "<clinit>",
                           at = @At(value = "INVOKE",
                                    target = "Lcom/mojang/serialization/Codec;lazyInitialized(Ljava/util/function/Supplier;)Lcom/mojang/serialization/Codec;",
                                    remap = false),
                           require = 2,
                           allow = 2,
                           order = 1100)
    private static Codec<ItemStack> cosmiccore$migrateArmorAttributes(Codec<ItemStack> codec) {
        return LegacyArmorAttributeMigration.wrap(codec);
    }
}
