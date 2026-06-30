package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(SimpleWorldGenLayer.class)
public interface SimpleWorldGenLayerLevelsAccessor {

    @Mutable
    @Accessor("levels")
    void cosmiccore$setLevels(Set<ResourceKey<Level>> levels);
}
