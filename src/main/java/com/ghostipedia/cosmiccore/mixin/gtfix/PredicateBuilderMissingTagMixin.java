package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PredicateBuilder.class, remap = false)
public abstract class PredicateBuilderMissingTagMixin {

    @Inject(method = "blockTag", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$allowMissingBlockTag(TagKey<Block> tag, CallbackInfoReturnable<PredicateBuilder> cir) {
        if (BuiltInRegistries.BLOCK.getTag(tag).isEmpty()) {
            cir.setReturnValue((PredicateBuilder) (Object) this);
        }
    }

    @Inject(method = "fluidTag", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$allowMissingFluidTag(TagKey<Fluid> tag, CallbackInfoReturnable<PredicateBuilder> cir) {
        if (BuiltInRegistries.FLUID.getTag(tag).isEmpty()) {
            cir.setReturnValue((PredicateBuilder) (Object) this);
        }
    }
}
