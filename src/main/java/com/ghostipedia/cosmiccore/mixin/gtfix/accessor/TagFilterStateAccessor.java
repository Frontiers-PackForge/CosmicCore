package com.ghostipedia.cosmiccore.mixin.gtfix.accessor;

import com.gregtechceu.gtceu.api.cover.filter.TagFilter;

import net.minecraft.tags.TagKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;
import java.util.stream.Stream;

@Mixin(value = TagFilter.class, remap = false)
public interface TagFilterStateAccessor<T, S> {

    @Accessor("tagHolderObject")
    Function<T, S> cosmiccore$getTagHolderObject();

    @Accessor("tagsSupplier")
    Function<T, Stream<TagKey<S>>> cosmiccore$getTagsSupplier();
}
