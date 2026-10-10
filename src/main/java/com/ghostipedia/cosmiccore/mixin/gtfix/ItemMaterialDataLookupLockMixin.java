package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;

import net.minecraft.world.level.ItemLike;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(value = ItemMaterialData.class, remap = false)
public abstract class ItemMaterialDataLookupLockMixin {

    @WrapMethod(method = "registerMaterialEntry(Ljava/util/function/Supplier;Lcom/gregtechceu/gtceu/api/data/chemical/material/stack/MaterialEntry;)V",
                remap = false)
    private static void cosmiccore$lockRegistration(Supplier<? extends ItemLike> supplier, MaterialEntry entry,
                                                    Operation<Void> original) {
        synchronized (ItemMaterialData.class) {
            original.call(supplier, entry);
        }
    }

    @WrapMethod(method = "reinitializeMaterialData()V", remap = false)
    private static void cosmiccore$lockReinitialization(Operation<Void> original) {
        synchronized (ItemMaterialData.class) {
            original.call();
        }
    }
}
