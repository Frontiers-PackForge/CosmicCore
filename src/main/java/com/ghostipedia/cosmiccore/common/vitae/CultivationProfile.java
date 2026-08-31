package com.ghostipedia.cosmiccore.common.vitae;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record CultivationProfile(
                                 ResourceLocation entity,
                                 CultivationTier tier,
                                 int experienceMin,
                                 int experienceMax,
                                 CultivationYieldBand vitae,
                                 CultivationYieldBand spiritus,
                                 List<CultivationItemOutput> itemOutputs,
                                 String sourceFingerprint) {

    public static final Codec<CultivationProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity").forGetter(CultivationProfile::entity),
            CultivationTier.CODEC.fieldOf("cultivation_tier").forGetter(CultivationProfile::tier),
            Codec.INT.optionalFieldOf("experience_min", 0).forGetter(CultivationProfile::experienceMin),
            Codec.INT.optionalFieldOf("experience_max", 0).forGetter(CultivationProfile::experienceMax),
            CultivationYieldBand.CODEC.fieldOf("vitae").forGetter(CultivationProfile::vitae),
            CultivationYieldBand.CODEC.fieldOf("spiritus").forGetter(CultivationProfile::spiritus),
            CultivationItemOutput.CODEC.listOf().optionalFieldOf("item_outputs", List.of())
                    .forGetter(CultivationProfile::itemOutputs),
            Codec.STRING.optionalFieldOf("source_fingerprint", "unreviewed")
                    .forGetter(CultivationProfile::sourceFingerprint))
            .apply(instance, CultivationProfile::new));

    public boolean isValid() {
        return experienceMin >= 0 && experienceMax >= experienceMin && itemOutputs.stream()
                .allMatch(CultivationItemOutput::isValid);
    }
}
