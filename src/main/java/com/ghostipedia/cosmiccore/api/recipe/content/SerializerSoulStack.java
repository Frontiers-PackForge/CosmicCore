package com.ghostipedia.cosmiccore.api.recipe.content;

import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;

import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;

import net.minecraft.network.FriendlyByteBuf;

import com.mojang.serialization.Codec;

public class SerializerSoulStack implements IContentSerializer<SoulStack> {

    public static SerializerSoulStack INSTANCE = new SerializerSoulStack();

    private SerializerSoulStack() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, SoulStack content) {
        content.toNetwork(buf);
    }

    @Override
    public SoulStack fromNetwork(FriendlyByteBuf buf) {
        return SoulStack.fromNetwork(buf);
    }

    @Override
    public SoulStack of(Object o) {
        if (o instanceof SoulStack stack) return stack;
        else return SoulStack.EMPTY;
    }

    @Override
    public SoulStack defaultValue() {
        return SoulStack.EMPTY;
    }

    @Override
    public Class<SoulStack> contentClass() {
        return SoulStack.class;
    }

    @Override
    public Codec<SoulStack> codec() {
        return SoulStack.CODEC;
    }
}
