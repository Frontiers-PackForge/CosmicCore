package com.ghostipedia.cosmiccore.bee.feature;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.CosmicBeesHoneyComb;
import com.ghostipedia.cosmiccore.common.item.CosmicBeesItemHoneyComb;

import forestry.modules.features.FeatureItemGroup;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CosmicBeesItems {

    public static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(CosmicCore.id("core/cosmicore"));
    public static final FeatureItemGroup<CosmicBeesItemHoneyComb, CosmicBeesHoneyComb> BEE_COMBS = REGISTRY
            .itemGroup(CosmicBeesItemHoneyComb::new, "bee_comb", CosmicBeesHoneyComb.VALUES);
}
