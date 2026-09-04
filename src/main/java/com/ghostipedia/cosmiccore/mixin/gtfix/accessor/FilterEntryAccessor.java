package com.ghostipedia.cosmiccore.mixin.gtfix.accessor;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.gregtechceu.gtceu.api.cover.filter.Filters$FilterEntry", remap = false)
public interface FilterEntryAccessor {

    @Accessor("dataComponentType")
    Holder<DataComponentType<?>> cosmiccore$getDataComponentType();
}
