package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.bee.CosmicBeesDefinition;
import com.ghostipedia.cosmiccore.bee.CosmicBeesTaxonomy;
import com.ghostipedia.cosmiccore.bee.feature.CosmicBeesItems;
import com.ghostipedia.cosmiccore.common.item.CosmicBeesHoneyComb;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.client.plugin.IClientRegistration;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.IGeneticRegistration;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CosmicBeesPlugin implements IForestryPlugin {

    @Override
    public ResourceLocation id() {
        return CosmicCore.id("core/cosmicore");
    }

    @Override
    public void registerApiculture(IApicultureRegistration apiculture) {
        CosmicBeesDefinition.defineBees(apiculture);
    }

    @Override
    public void registerGenetics(IGeneticRegistration genetics) {
        CosmicBeesTaxonomy.defineTaxa(genetics);
    }

    @Override
    public void registerClient(Consumer<Consumer<IClientRegistration>> registrar) {
        registrar.accept(new CosmicCoreClient.CosmicBeesClientRegistration());
    }

    private static Supplier<List<ItemStack>> getHoneyComb(CosmicBeesHoneyComb type) {
        return () -> List.of(CosmicBeesItems.BEE_COMBS.stack(type));
    }
}
