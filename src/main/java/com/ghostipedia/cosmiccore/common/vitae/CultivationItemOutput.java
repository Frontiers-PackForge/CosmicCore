package com.ghostipedia.cosmiccore.common.vitae;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CultivationItemOutput(ResourceLocation item, int minCount, int maxCount, double chance) {

    public static final Codec<CultivationItemOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(CultivationItemOutput::item),
            Codec.INT.optionalFieldOf("min_count", 1).forGetter(CultivationItemOutput::minCount),
            Codec.INT.optionalFieldOf("max_count", 1).forGetter(CultivationItemOutput::maxCount),
            Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(CultivationItemOutput::chance))
            .apply(instance, CultivationItemOutput::new));

    public boolean isValid() {
        return minCount >= 0 && maxCount >= minCount && chance >= 0.0 && chance <= 1.0;
    }
}
