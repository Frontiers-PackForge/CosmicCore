package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ChemicalHelper.class, remap = false)
public abstract class ChemicalHelperMaterialLookupLockMixin {

    @WrapMethod(method = "getMaterialEntry(Lnet/minecraft/world/level/ItemLike;)Lcom/gregtechceu/gtceu/api/data/chemical/material/stack/MaterialEntry;",
                remap = false)
    private static MaterialEntry cosmiccore$lockItemLookup(ItemLike item, Operation<MaterialEntry> original) {
        synchronized (ItemMaterialData.class) {
            return original.call(item);
        }
    }

    @WrapMethod(method = "getMaterialEntry(Lnet/minecraft/tags/TagKey;)Lcom/gregtechceu/gtceu/api/data/chemical/material/stack/MaterialEntry;",
                remap = false)
    private static MaterialEntry cosmiccore$lockTagLookup(TagKey<Item> tag, Operation<MaterialEntry> original) {
        synchronized (ItemMaterialData.class) {
            return original.call(tag);
        }
    }
}
