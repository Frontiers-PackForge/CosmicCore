package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialRegistryEntry;
import com.gregtechceu.gtceu.utils.ToolItemHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ToolItemHelper.class, remap = false)
public abstract class ToolItemHelperMaterialLookupMixin {

    @ModifyArg(method = "lambda$getToolItem$0",
               at = @At(value = "INVOKE",
                        target = "Lcom/google/common/collect/Table;get(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
               index = 0,
               require = 2,
               allow = 2)
    private static Object cosmiccore$resolveToolMaterial(Object material) {
        return material instanceof MaterialRegistryEntry entry ? entry.value() : material;
    }
}
