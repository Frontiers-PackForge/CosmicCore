package com.ghostipedia.cosmiccore.bee.feature;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBeesItemHiveFraming;
import com.ghostipedia.cosmiccore.common.data.CosmicGendustryUpgradeType;
import com.ghostipedia.cosmiccore.common.item.CosmicBeesHoneyComb;
import com.ghostipedia.cosmiccore.common.item.CosmicBeesItemHoneyComb;

import forestry.modules.features.*;
import thedarkcolour.gendustry.item.EliteGendustryUpgradeType;
import thedarkcolour.gendustry.item.GendustryUpgradeItem;

@FeatureProvider
public class CosmicBeesItems {

    public static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(CosmicCore.id("core/cosmicore"));
    public static final FeatureItemGroup<CosmicBeesItemHoneyComb, CosmicBeesHoneyComb> BEE_COMBS = REGISTRY
            .itemGroup(CosmicBeesItemHoneyComb::new, "bee_comb", CosmicBeesHoneyComb.VALUES);

    public static final FeatureItemGroup<GendustryUpgradeItem, CosmicGendustryUpgradeType> COSMIC_UPGRADES = REGISTRY.itemGroup(GendustryUpgradeItem::new, CosmicGendustryUpgradeType.values()).identifier("cosmic_upgrade", FeatureGroup.IdentifierType.SUFFIX).create();



    public static final FeatureItem<CosmicBeesItemHiveFraming> FRAME_DECAYING = REGISTRY.item(() -> new CosmicBeesItemHiveFraming
            .CosmicBeesItemHiveFrameBuilder(2048)
            .setAgeMult(-1000f)
            .build(), "frame_decaying");

    public static final FeatureItem<CosmicBeesItemHiveFraming> FRAME_WAILING = REGISTRY.item(() -> new CosmicBeesItemHiveFraming
            .CosmicBeesItemHiveFrameBuilder(2048)
            .setMutationMult(1000f)
            .build(), "frame_wailing");



}
