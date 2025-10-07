package com.ghostipedia.cosmiccore.common.ascension;

import com.ghostipedia.cosmiccore.CosmicCore;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

import static com.ghostipedia.cosmiccore.common.ascension.AscensionConsumables.SOUL;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

public class AscensionUpgrades {

    public static final Upgrade IRON_SOUL = new Upgrade(
            CosmicCore.id("iron_soul"),
            "Iron Body",
            3,
            List.of(new Upgrade.Cost(SOUL, 50)),
            cap -> true,
            UpgradeUtils.attribute(
                    new ResourceLocation("minecraft","generic.max_health"),
                    ADDITION,
                    2.0, // This is effectively just 1 heart per rank
                    UUID.fromString("c71b2fb0-2b02-4977-9c2f-34e9c1e4c1e0"),
                    "Soul_IronBody"
            )
    );


}
