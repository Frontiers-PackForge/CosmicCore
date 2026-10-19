package com.ghostipedia.cosmiccore.mixin.gtfix.kjs;

import com.gregtechceu.gtceu.integration.kjs.helpers.KubeGTRegistryEventHandler;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(value = { KubeGTRegistryEventHandler.class, RegistryEventHandler.class }, remap = false)
public abstract class SharedRegistryBuilderRegistrationFixMixin {

    @WrapOperation(method = "handleRegistryEvent",
                   at = @At(value = "INVOKE",
                            target = "Lnet/neoforged/neoforge/registries/RegisterEvent;register(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;)V"))
    private static <T> void cosmiccore$registerBuilderOnce(RegisterEvent event,
                                                           ResourceKey<? extends Registry<T>> registryKey,
                                                           ResourceLocation id, Supplier<T> supplier,
                                                           Operation<Void> original, @Local BuilderBase<T> builder) {
        if (event.getRegistryKey().equals(registryKey) && builder.get() != null &&
                event.getRegistry().containsKey(id) && event.getRegistry().get(id) == builder.get()) {
            return;
        }
        original.call(event, registryKey, id, supplier);
    }
}
