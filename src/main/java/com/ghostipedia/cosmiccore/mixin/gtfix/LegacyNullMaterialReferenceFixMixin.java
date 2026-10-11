package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.CodecTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CodecTransformer.class, remap = false)
public abstract class LegacyNullMaterialReferenceFixMixin {

    @Inject(method = "deserializeNBT", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$readLegacyNullMaterial(Tag tag, ValueTransformer.TransformerContext<?> context,
                                                   CallbackInfoReturnable<Object> cir) {
        if ((Object) this == ValueTransformers.get(Material.class) &&
                tag instanceof StringTag value && "gtceu:null".equals(value.getAsString())) {
            cir.setReturnValue(null);
        }
    }
}
