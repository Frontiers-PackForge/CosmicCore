package com.ghostipedia.cosmiccore.integration.jade;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(CosmicCore.MOD_ID)
public class CosmicCoreJadePlugin implements IWailaPlugin {

    public static final ResourceLocation EMBER_DETAILS = CosmicCore.id("ember_details");
    public static final ResourceLocation ME_COMPUTATION_ARRAY_DETAILS = CosmicCore.id("me_computation_array_details");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CosmicEmberProvider.INSTANCE, Block.class);
        registration.registerBlockDataProvider(MEComputationArrayProvider.INSTANCE, Block.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CosmicEmberProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(MEComputationArrayProvider.INSTANCE, Block.class);
    }
}
