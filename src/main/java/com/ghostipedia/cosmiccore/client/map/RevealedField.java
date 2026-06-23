package com.ghostipedia.cosmiccore.client.map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import io.netty.buffer.ByteBuf;

public record RevealedField(int x, int z, int colorARGB, String name, byte tier, int radius) {

    public static final StreamCodec<ByteBuf, RevealedField> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RevealedField::x,
            ByteBufCodecs.VAR_INT, RevealedField::z,
            ByteBufCodecs.INT, RevealedField::colorARGB,
            ByteBufCodecs.STRING_UTF8, RevealedField::name,
            ByteBufCodecs.BYTE, RevealedField::tier,
            ByteBufCodecs.VAR_INT, RevealedField::radius,
            RevealedField::new);

    public int colorRGB() {
        return colorARGB & 0xFFFFFF;
    }

    public String displayName() {
        return Component.translatable("ore_vein.cosmiccore." + name).getString();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("z", z);
        tag.putInt("color", colorARGB);
        tag.putString("name", name);
        tag.putByte("tier", tier);
        tag.putInt("radius", radius);
        return tag;
    }

    public static RevealedField fromTag(CompoundTag tag) {
        return new RevealedField(tag.getInt("x"), tag.getInt("z"), tag.getInt("color"),
                tag.getString("name"), tag.getByte("tier"), tag.getInt("radius"));
    }
}
