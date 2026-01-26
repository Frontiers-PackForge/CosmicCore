package com.ghostipedia.cosmiccore.api.recipe.ingredient;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;

import net.minecraft.network.FriendlyByteBuf;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.With;

@With
public record SoulStack(SoulType type, int amount) {

    public static final Codec<SoulStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SoulType.CODEC.fieldOf("type").forGetter(SoulStack::type),
            Codec.INT.fieldOf("amount").forGetter(SoulStack::amount)).apply(instance, SoulStack::new));

    public static final SoulStack EMPTY = new SoulStack(SoulType.Raw, 0);

    public boolean isEmpty() {
        return this.amount <= 0;
    }

    public SoulStack add(int amount) {
        Preconditions.checkArgument(this.amount + amount >= 0, "Resulting amount must be non-negative");
        return new SoulStack(this.type, this.amount + amount);
    }

    public SoulStack sum(SoulStack a, SoulStack b) {
        Preconditions.checkArgument(a.type == b.type, "SoulStack types don't match");
        return a.add(b.amount);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeEnum(this.type);
        buf.writeVarInt(this.amount);
    }

    public static SoulStack fromNetwork(FriendlyByteBuf buf) {
        return new SoulStack(buf.readEnum(SoulType.class), buf.readVarInt());
    }
}
