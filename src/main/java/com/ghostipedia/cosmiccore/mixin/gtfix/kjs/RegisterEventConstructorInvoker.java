package com.ghostipedia.cosmiccore.mixin.gtfix.kjs;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegisterEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RegisterEvent.class, remap = false)
public interface RegisterEventConstructorInvoker {

    @Invoker("<init>")
    static RegisterEvent cosmiccore$create(ResourceKey<? extends Registry<?>> key, Registry<?> registry) {
        throw new AssertionError();
    }
}
