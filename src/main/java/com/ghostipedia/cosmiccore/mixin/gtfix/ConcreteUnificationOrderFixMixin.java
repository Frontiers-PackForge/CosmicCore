package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(value = ChemicalHelper.class, remap = false)
public abstract class ConcreteUnificationOrderFixMixin {

    @Inject(method = "getItems", at = @At("RETURN"), cancellable = true)
    private static void cosmiccore$stableConcreteItems(MaterialEntry entry,
                                                       CallbackInfoReturnable<List<ItemLike>> cir) {
        if (entry.tagPrefix() != TagPrefix.block || entry.material() != GTMaterials.Concrete) return;
        var sorted = new ArrayList<>(cir.getReturnValue());
        sorted.sort(Comparator
                .<ItemLike>comparingInt(item -> item.asItem() == GTBlocks.LIGHT_CONCRETE.get().asItem() ? 0 : 1)
                .thenComparing(item -> BuiltInRegistries.ITEM.getKey(item.asItem()).toString()));
        cir.setReturnValue(sorted);
    }

    @Inject(method = "getBlocks", at = @At("RETURN"), cancellable = true)
    private static void cosmiccore$stableConcreteBlocks(MaterialEntry entry, CallbackInfoReturnable<List<Block>> cir) {
        if (entry.tagPrefix() != TagPrefix.block || entry.material() != GTMaterials.Concrete) return;
        var sorted = new ArrayList<>(cir.getReturnValue());
        sorted.sort(Comparator.<Block>comparingInt(block -> block == GTBlocks.LIGHT_CONCRETE.get() ? 0 : 1)
                .thenComparing(block -> BuiltInRegistries.BLOCK.getKey(block).toString()));
        cir.setReturnValue(sorted);
    }
}
