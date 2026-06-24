package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GroupingMapRenderer.class, remap = false)
public abstract class GroupingMapRendererMixin {

    @Inject(
            method = "addMarker(Lnet/minecraft/network/chat/Component;Lnet/minecraft/resources/ResourceKey;Lcom/gregtechceu/gtceu/api/data/worldgen/ores/GeneratedVeinMetadata;Ljava/lang/String;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void cosmiccore$skipFieldMarkers(Component name, ResourceKey<Level> dim, GeneratedVeinMetadata vein,
                                             String id, CallbackInfoReturnable<Boolean> cir) {
        boolean isField = vein.definition().unwrapKey()
                .map(key -> key.location().getNamespace().equals(CosmicCore.MOD_ID))
                .orElse(false);
        if (isField) {
            cir.setReturnValue(false);
        }
    }
}
