package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.tags.ItemTags.CLUSTER_MAX_HARVESTABLES;
import static slimeknights.tconstruct.common.TinkerTags.Items.*;

public class CosmicTconItemTagProvider extends ItemTagsProvider {

    public CosmicTconItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                     CompletableFuture<TagLookup<Block>> blockTagProvider,
                                     ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, CosmicCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.addTools();
    }

    private void addTools() {
        addToolTags(CosmicTinkerTools.wireCutter, MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, CustomTags.WIRE_CUTTERS,
                CustomTags.CRAFTING_WIRE_CUTTERS, MELEE_WEAPON, INTERACTABLE_RIGHT, AOE, CLUSTER_MAX_HARVESTABLES,
                SMALL_TOOLS, BONUS_SLOTS);
        addToolTags(CosmicTinkerTools.wrench, MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, CustomTags.WRENCHES,
                CustomTags.CRAFTING_WRENCHES, MELEE_WEAPON, INTERACTABLE_RIGHT, AOE, CLUSTER_MAX_HARVESTABLES,
                SMALL_TOOLS, BONUS_SLOTS);
    }

    @SafeVarargs
    private void addToolTags(ItemLike tool, TagKey<Item>... tags) {
        Item item = tool.asItem();
        for (TagKey<Item> tag : tags) {
            this.tag(tag).add(item);
        }
    }
}
