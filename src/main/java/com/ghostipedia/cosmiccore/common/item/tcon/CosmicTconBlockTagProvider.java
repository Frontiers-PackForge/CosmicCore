package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class CosmicTconBlockTagProvider extends BlockTagsProvider {

    public CosmicTconBlockTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries,
                                      ExistingFileHelper existingFileHelper) {
        super(packOutput, registries, CosmicCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {}
}
