package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.RegistryReferenceTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RegistryReferenceTransformer.class, remap = false)
public abstract class LegacyNullMaterialReferenceFixMixin {

    @Inject(method = "deserializeNBT", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$readLegacyNullMaterial(Tag tag, ValueTransformer.TransformerContext<?> context,
                                                   CallbackInfoReturnable<Object> cir) {
        var transformer = (RegistryReferenceTransformer<?>) (Object) this;
        if (transformer.registryKey().equals(GTRegistries.Keys.MATERIAL) &&
                tag instanceof StringTag value && "gtceu:null".equals(value.getAsString())) {
            cir.setReturnValue(null);
        }
    }
}
