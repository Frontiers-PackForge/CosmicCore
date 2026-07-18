package com.ghostipedia.cosmiccore.common.block.crop;

import com.ghostipedia.cosmiccore.common.data.CosmicBotanyItemRegistration;
import com.ghostipedia.cosmiccore.common.data.CosmicCreativeModeTabs;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public final class CosmicCrops {

    static {
        REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
    }

    public static final BlockEntry<RainbowCaneBlock> RAINBOW_CANE = REGISTRATE
            .block("rainbow_cane", RainbowCaneBlock::new)
            .initialProperties(() -> Blocks.SUGAR_CANE)
            .properties(BlockBehaviour.Properties::randomTicks)
            .lang("Rainbow Cane")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> tables.dropOther(block, rainbowCaneItem()))
            .register();

    public static final BlockEntry<SoulGourdCropBlock> SOUL_GOURD_CROP = REGISTRATE
            .block("soul_gourd_crop",
                    p -> new SoulGourdCropBlock(p, () -> CosmicCrops.SOUL_GOURD_SEEDS.asItem()))
            .initialProperties(() -> Blocks.WHEAT)
            .properties(BlockBehaviour.Properties::randomTicks)
            .lang("Soul Gourd")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> tables.dropOther(block, soulGourdSeeds()))
            .register();
    public static final BlockEntry<SoulGourdBloomBlock> SOUL_GOURD_BLOOM = REGISTRATE
            .block("soul_gourd_bloom", SoulGourdBloomBlock::new)
            .initialProperties(() -> Blocks.PUMPKIN)
            .properties(p -> p.mapColor(MapColor.PLANT).instabreak().noOcclusion().pushReaction(PushReaction.DESTROY))
            .lang("Soul Gourd Bloom")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> tables.dropOther(block, soulGourd()))
            .register();
    public static final BlockEntry<SoulGourdAttachedStemBlock> SOUL_GOURD_ATTACHED_STEM = REGISTRATE
            .block("soul_gourd_attached_stem", SoulGourdAttachedStemBlock::new)
            .initialProperties(() -> Blocks.ATTACHED_PUMPKIN_STEM)
            .lang("Attached Soul Gourd Stem")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> tables.add(block, LootTable.lootTable()))
            .register();

    public static final BlockEntry<RockvineBodyBlock> CULTIVATED_ROCKVINE_BODY = REGISTRATE
            .block("cultivated_rockvine_body", RockvineBodyBlock::new)
            .initialProperties(() -> Blocks.CAVE_VINES_PLANT)
            .lang("Rockvine Plant")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> tables.add(block, LootTable.lootTable()))
            .register();
    public static final BlockEntry<RockvineBloomBlock> CULTIVATED_ROCKVINE_BLOOM = REGISTRATE
            .block("cultivated_rockvine_bloom", RockvineBloomBlock::new)
            .initialProperties(() -> Blocks.CAVE_VINES)
            .properties(BlockBehaviour.Properties::randomTicks)
            .lang("Rockvine")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> tables.add(block, LootTable.lootTable()))
            .register();

    public static final BlockEntry<SporebeanCropBlock> SPOREBEAN_CROP = REGISTRATE
            .block("sporebean_crop", p -> new SporebeanCropBlock(p, CosmicCrops::sporebeans,
                    CosmicCrops::sporebeans))
            .initialProperties(() -> Blocks.COCOA)
            .properties(BlockBehaviour.Properties::randomTicks)
            .lang("Sporebeans")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> tables.dropOther(block, sporebeans()))
            .register();

    public static final BlockEntry<DriftweedRootBlock> DRIFTWEED_ROOT = REGISTRATE
            .block("driftweed_root", DriftweedRootBlock::new)
            .initialProperties(() -> Blocks.SEAGRASS)
            .properties(BlockBehaviour.Properties::randomTicks)
            .lang("Driftweed Root")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> tables.dropOther(block, driftweedRhizome()))
            .register();
    public static final BlockEntry<DriftweedStalkBlock> DRIFTWEED_STALK = REGISTRATE
            .block("driftweed_stalk", DriftweedStalkBlock::new)
            .initialProperties(() -> Blocks.KELP_PLANT)
            .lang("Driftweed Stalk")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> tables.add(block, LootTable.lootTable()))
            .register();
    public static final BlockEntry<DriftweedBloomBlock> DRIFTWEED_BLOOM = REGISTRATE
            .block("driftweed_bloom", p -> new DriftweedBloomBlock(p,
                    CosmicBotanyItemRegistration.DRIFTWEED::asItem, () -> CosmicCrops.DRIFTWEED_RHIZOME.asItem()))
            .initialProperties(() -> Blocks.LILY_PAD)
            .properties(p -> p.randomTicks().noCollission().instabreak().noOcclusion())
            .lang("Blooming Driftweed")
            .blockstate(NonNullBiConsumer.noop())
            .loot((tables, block) -> {
                var mature = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(DriftweedBloomBlock.AGE, 3));
                tables.add(block, tables.applyExplosionDecay(block, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(CosmicBotanyItemRegistration.DRIFTWEED.get())
                                        .when(mature)
                                        .otherwise(LootItem.lootTableItem(CosmicCrops.DRIFTWEED_RHIZOME.get()))))));
            })
            .register();

    public static final ItemEntry<ItemNameBlockItem> RAINBOW_CANE_ITEM = REGISTRATE
            .item("rainbow_cane", p -> new ItemNameBlockItem(RAINBOW_CANE.get(), p))
            .lang("Rainbow Cane")
            .model(NonNullBiConsumer.noop())
            .register();
    public static final ItemEntry<ItemNameBlockItem> SOUL_GOURD = REGISTRATE
            .item("soul_gourd", p -> new ItemNameBlockItem(SOUL_GOURD_BLOOM.get(), p))
            .lang("Soul Gourd")
            .properties(p -> p.stacksTo(16))
            .model(NonNullBiConsumer.noop())
            .register();
    public static final ItemEntry<ItemNameBlockItem> SOUL_GOURD_SEEDS = REGISTRATE
            .item("soul_gourd_seeds", p -> new ItemNameBlockItem(SOUL_GOURD_CROP.get(), p))
            .lang("Soul Gourd Seeds")
            .model(NonNullBiConsumer.noop())
            .register();
    public static final ItemEntry<ItemNameBlockItem> ROCKVINE_BERRY = REGISTRATE
            .item("rockvine_berry", p -> new ItemNameBlockItem(CULTIVATED_ROCKVINE_BLOOM.get(),
                    p.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build())))
            .lang("Rockvine Berry")
            .model(NonNullBiConsumer.noop())
            .register();
    public static final ItemEntry<ItemNameBlockItem> SPOREBEANS = REGISTRATE
            .item("sporebeans", p -> new ItemNameBlockItem(SPOREBEAN_CROP.get(), p))
            .lang("Sporebeans")
            .defaultModel()
            .register();
    public static final ItemEntry<ItemNameBlockItem> DRIFTWEED_RHIZOME = REGISTRATE
            .item("driftweed_rhizome", p -> new ItemNameBlockItem(DRIFTWEED_ROOT.get(), p))
            .lang("Driftweed Rhizome")
            .model(NonNullBiConsumer.noop())
            .register();

    private CosmicCrops() {}

    private static ItemNameBlockItem rainbowCaneItem() {
        return RAINBOW_CANE_ITEM.get();
    }

    private static ItemNameBlockItem soulGourdSeeds() {
        return SOUL_GOURD_SEEDS.get();
    }

    private static ItemNameBlockItem soulGourd() {
        return SOUL_GOURD.get();
    }

    private static ItemNameBlockItem sporebeans() {
        return SPOREBEANS.get();
    }

    private static ItemNameBlockItem driftweedRhizome() {
        return DRIFTWEED_RHIZOME.get();
    }

    public static void init() {}
}
