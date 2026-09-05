package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.Holder;
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

    @Inject(method = "blockTag", at = @At("RETURN"))
    private void cosmiccore$allowMissingBlockTag(TagKey<Block> tag, CallbackInfoReturnable<PredicateBuilder> cir) {
        cir.getReturnValue().candidatesSupplier(() -> BuiltInRegistries.BLOCK.getTag(tag).stream()
                .flatMap(holders -> holders.stream()).map(Holder::value).map(BlockInfo::fromBlock).toList());
    }

    @Inject(method = "fluidTag", at = @At("RETURN"))
    private void cosmiccore$allowMissingFluidTag(TagKey<Fluid> tag, CallbackInfoReturnable<PredicateBuilder> cir) {
        cir.getReturnValue().candidatesSupplier(() -> BuiltInRegistries.FLUID.getTag(tag).stream()
                .flatMap(holders -> holders.stream()).map(Holder::value).map(BlockInfo::fromFluid).toList());
    }
}
