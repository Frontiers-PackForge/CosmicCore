package com.ghostipedia.cosmiccore.common.qol;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpruceSaplingGrowth {

    private SpruceSaplingGrowth() {}

    @SubscribeEvent
    public static void onGrowFeature(BlockGrowFeatureEvent event) {
        var holder = event.getFeature();
        if (holder == null || !holder.is(TreeFeatures.MEGA_PINE) ||
                !event.getLevel().getBlockState(event.getPos()).is(Blocks.SPRUCE_SAPLING)) {
            return;
        }
        var feature = holder.value();
        if (feature.feature() != Feature.TREE || !(feature.config() instanceof TreeConfiguration config)) {
            return;
        }
        var builder = new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.SPRUCE_LOG), config.trunkPlacer,
                BlockStateProvider.simple(Blocks.SPRUCE_LEAVES), config.foliagePlacer,
                config.rootPlacer, config.minimumSize)
                .dirt(config.dirtProvider)
                .decorators(config.decorators);
        if (config.ignoreVines) {
            builder.ignoreVines();
        }
        if (config.forceDirt) {
            builder.forceDirt();
        }
        event.setFeature(Holder.direct(new ConfiguredFeature<>(Feature.TREE, builder.build())));
    }
}
