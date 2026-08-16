package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.CosmicCoreAPI;
import com.ghostipedia.cosmiccore.api.block.IMagnetType;
import com.ghostipedia.cosmiccore.client.renderer.block.NebulaeCoilRenderer;
import com.ghostipedia.cosmiccore.common.block.BerryVineBlock;
import com.ghostipedia.cosmiccore.common.block.ComputationBayCasingBlock;
import com.ghostipedia.cosmiccore.common.block.LargeArcaniteClusterBlock;
import com.ghostipedia.cosmiccore.common.block.MagnetBlock;
import com.ghostipedia.cosmiccore.common.block.MurkFloraBlock;
import com.ghostipedia.cosmiccore.common.block.MurkKelpBlock;
import com.ghostipedia.cosmiccore.common.blockentity.CosmicCoilBlockEntity;
import com.ghostipedia.cosmiccore.common.data.tag.block.CosmicBlockTags;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentPortalBlock;
import com.ghostipedia.cosmiccore.common.food.hearth.HearthPlateBlock;
import com.ghostipedia.cosmiccore.ember.CosmicEmberEmitterBlock;
import com.ghostipedia.cosmiccore.ember.CosmicEmberReceptorBlock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import com.rekindled.embers.RegistryManager;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties.ACTIVE;

public class CosmicBlocks {

    static {
        REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);

    }

    public static final Map<Integer, BlockEntry<CosmicEmberEmitterBlock>> EMBER_EMITTER_BLOCKS = registerEmberEmitters();
    public static final Map<Integer, BlockEntry<CosmicEmberReceptorBlock>> EMBER_RECEPTOR_BLOCKS = registerEmberReceptors();

    public static final BlockEntry<Block> FIRMAMENT_SAPROLITE = firmamentTerrainBlock(
            "firmament_saprolite", "Firmament Saprolite", Blocks.STONE, "firmament_saprolite",
            MapColor.WOOL, SoundType.STONE, 0.5F, true, BlockTags.MINEABLE_WITH_PICKAXE);
    public static final BlockEntry<SlabBlock> FIRMAMENT_SAPROLITE_SLAB = firmamentTerrainSlab(
            "firmament_saprolite_slab", "Firmament Saprolite Slab", "firmament_saprolite", FIRMAMENT_SAPROLITE);
    public static final BlockEntry<Block> ASTRAL_REGOLITH = firmamentTerrainBlock(
            "astral_regolith", "Astral Regolith", Blocks.DIRT, "astral_regolith",
            MapColor.TERRACOTTA_CYAN, SoundType.GRAVEL, 0.2F, false,
            BlockTags.DIRT, BlockTags.MINEABLE_WITH_SHOVEL);
    public static final BlockEntry<Block> STARDUST_TURF = REGISTRATE.block("stardust_turf", Block::new)
            .lang("Stardust Turf")
            .initialProperties(() -> Blocks.GRASS_BLOCK)
            .properties(properties -> properties.mapColor(MapColor.WARPED_WART_BLOCK)
                    .strength(0.2F)
                    .sound(SoundType.GRASS))
            .blockstate((context, provider) -> provider.simpleBlock(
                    context.get(), provider.models().cubeBottomTop(
                            context.getName(), firmamentTexture("stardust_turf_side"),
                            firmamentTexture("astral_regolith"), firmamentTexture("stardust_turf_top"))))
            .loot((tables, block) -> tables.dropSelf(block))
            .tag(BlockTags.DIRT, BlockTags.MINEABLE_WITH_SHOVEL)
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<Block> SUNSCALDED_SAPROLITE = firmamentResourceBlock(
            "sunscalded_saprolite", "Sunscalded Saprolite", Blocks.DEEPSLATE, "sunscalded_saprolite",
            MapColor.TERRACOTTA_ORANGE, SoundType.STONE, 1.2F, 0);
    public static final BlockEntry<Block> HELIOSTATIC_GLASS = firmamentResourceBlock(
            "heliostatic_glass", "Heliostatic Glass", Blocks.AMETHYST_BLOCK, "heliostatic_glass",
            MapColor.GOLD, SoundType.AMETHYST, 1.8F, 4);
    public static final BlockEntry<Block> STORMGLASS = firmamentResourceBlock(
            "stormglass", "Stormglass", Blocks.AMETHYST_BLOCK, "stormglass",
            MapColor.COLOR_CYAN, SoundType.AMETHYST, 2.0F, 5);
    public static final BlockEntry<Block> TEMPEST_FULGURITE = firmamentResourceBlock(
            "tempest_fulgurite", "Tempest Fulgurite", Blocks.CALCITE, "tempest_fulgurite",
            MapColor.COLOR_LIGHT_BLUE, SoundType.CALCITE, 1.6F, 6);
    public static final BlockEntry<Block> AMMONIA_RIME = firmamentResourceBlock(
            "ammonia_rime", "Ammonia Rime", Blocks.CALCITE, "ammonia_rime",
            MapColor.COLOR_LIGHT_GREEN, SoundType.CALCITE, 0.8F, 2);
    public static final BlockEntry<Block> GRAVITIC_FAULTSTONE = firmamentResourceBlock(
            "gravitic_faultstone", "Gravitic Faultstone", Blocks.DEEPSLATE, "gravitic_faultstone",
            MapColor.COLOR_PURPLE, SoundType.DEEPSLATE, 2.4F, 4);
    public static final BlockEntry<Block> UMBRAL_CRUST = firmamentResourceBlock(
            "umbral_crust", "Umbral Crust", Blocks.TUFF, "umbral_crust",
            MapColor.TERRACOTTA_PURPLE, SoundType.TUFF, 1.4F, 1);
    public static final BlockEntry<Block> RESONANT_SPIRESTONE = firmamentResourceBlock(
            "resonant_spirestone", "Resonant Spirestone", Blocks.DEEPSLATE, "resonant_spirestone",
            MapColor.TERRACOTTA_CYAN, SoundType.DEEPSLATE, 2.2F, 3);
    public static final BlockEntry<Block> PRIMORDIAL_REMNANT = firmamentResourceBlock(
            "primordial_remnant", "Primordial Remnant", Blocks.AMETHYST_BLOCK, "primordial_remnant",
            MapColor.QUARTZ, SoundType.AMETHYST, 3.0F, 8);
    public static final BlockEntry<FirmamentPortalBlock> FIRMAMENT_PORTAL = REGISTRATE
            .block("firmament_portal", FirmamentPortalBlock::new)
            .lang("Firmament Portal")
            .initialProperties(() -> Blocks.NETHER_PORTAL)
            .addLayer(() -> RenderType::translucent)
            .blockstate(NonNullBiConsumer.noop())
            .register();

    private static Map<Integer, BlockEntry<CosmicEmberEmitterBlock>> registerEmberEmitters() {
        Map<Integer, BlockEntry<CosmicEmberEmitterBlock>> emitters = new Int2ReferenceArrayMap<>();
        for (int i = 0; i < 15; i++) {
            final int tier = i;
            final String key = (i == 0 ? "steam" : GTValues.VN[i].toLowerCase(Locale.ROOT));
            final String tierName = (i == 0 ? "Steam" : GTValues.VN[i]);
            emitters.put(i, REGISTRATE.block(key + "_ember_emitter", props -> new CosmicEmberEmitterBlock(props, tier))
                    .lang(tierName + " Ember Emitter")
                    .initialProperties(RegistryManager.EMBER_EMITTER::get)
                    .exBlockstate((ctx, prov) -> prov.directionalBlock(ctx.getEntry(),
                            prov.models().getExistingFile(CosmicCore.id(key + "_ember_emitter"))))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .item(BlockItem::new)
                    .build()
                    .register());
        }
        return emitters;
    }

    @SafeVarargs
    private static BlockEntry<Block> firmamentTerrainBlock(String name, String lang, Block source, String texture,
                                                           MapColor mapColor, SoundType sound, float strength,
                                                           boolean requiresCorrectTool,
                                                           net.minecraft.tags.TagKey<Block>... tags) {
        return REGISTRATE.block(name, Block::new)
                .lang(lang)
                .initialProperties(() -> source)
                .properties(properties -> {
                    BlockBehaviour.Properties configured = properties.mapColor(mapColor).strength(strength)
                            .sound(sound);
                    return requiresCorrectTool ? configured.requiresCorrectToolForDrops() : configured;
                })
                .blockstate((context, provider) -> provider.simpleBlock(
                        context.get(), provider.models().cubeAll(context.getName(), firmamentTexture(texture))))
                .loot((tables, block) -> tables.dropSelf(block))
                .tag(tags)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<SlabBlock> firmamentTerrainSlab(String name, String lang, String texture,
                                                              BlockEntry<? extends Block> fullBlock) {
        return REGISTRATE.block(name, SlabBlock::new)
                .lang(lang)
                .initialProperties(fullBlock)
                .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                .blockstate((context, provider) -> {
                    ResourceLocation blockTexture = firmamentTexture(texture);
                    var bottom = provider.models().slab(context.getName(), blockTexture, blockTexture, blockTexture);
                    var top = provider.models().slabTop(context.getName() + "_top", blockTexture, blockTexture,
                            blockTexture);
                    var full = provider.models().getExistingFile(CosmicCore.id("block/" + fullBlock.getId().getPath()));
                    provider.slabBlock(context.get(), bottom, top, full);
                })
                .loot((tables, block) -> tables.add(block, tables.createSlabItemTable(block)))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static ResourceLocation firmamentTexture(String name) {
        return CosmicCore.id("block/firmament/" + name);
    }

    private static BlockEntry<Block> firmamentResourceBlock(String name, String lang, Block source, String texture,
                                                            MapColor mapColor, SoundType sound, float strength,
                                                            int lightLevel) {
        return REGISTRATE.block(name, Block::new)
                .lang(lang)
                .initialProperties(() -> source)
                .properties(properties -> properties.mapColor(mapColor)
                        .strength(strength)
                        .sound(sound)
                        .lightLevel(state -> lightLevel)
                        .requiresCorrectToolForDrops())
                .blockstate((context, provider) -> provider.simpleBlock(
                        context.get(), provider.models().cubeAll(context.getName(), firmamentTexture(texture))))
                .loot((tables, block) -> tables.dropSelf(block))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE, CosmicBlockTags.FIRMAMENT_RESOURCE_BLOCKS)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static Map<Integer, BlockEntry<CosmicEmberReceptorBlock>> registerEmberReceptors() {
        Map<Integer, BlockEntry<CosmicEmberReceptorBlock>> receptors = new Int2ReferenceArrayMap<>();
        for (int i = 0; i < 15; i++) {
            final int tier = i;
            final String key = (i == 0 ? "steam" : GTValues.VN[i].toLowerCase(Locale.ROOT));
            final String tierName = (i == 0 ? "Steam" : GTValues.VN[i]);
            receptors.put(i,
                    REGISTRATE.block(key + "_ember_receptor", props -> new CosmicEmberReceptorBlock(props, tier))
                            .lang(tierName + " Ember Receptor")
                            .initialProperties(RegistryManager.EMBER_RECEIVER::get)
                            .exBlockstate((ctx, prov) -> prov.directionalBlock(ctx.getEntry(),
                                    prov.models().getExistingFile(CosmicCore.id(key + "_ember_receptor"))))
                            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                            .item(BlockItem::new)
                            .build()
                            .register());
        }
        return receptors;
    }

    // Coil Register
    // TODO(stellaris): SUN_GLOBE used Ad Astra GLOBES registry + GlobeBlock â€” dropped with Ad Astra (bead
    // cosmiccore-42.13)

    public static final BlockEntry<CoilBlock> COIL_PRISMATIC_TUNGSTENSTEEL = createCoilBlock(
            CosmicCoilBlock.CoilType.PRISMATIC_TUNGSTENSTEEL);
    public static final BlockEntry<CoilBlock> COIL_RESONANT_VIRTUE_MELD = createCoilBlock(
            CosmicCoilBlock.CoilType.RESONANT_VIRTUE_MELD);
    public static final BlockEntry<CoilBlock> COIL_NAQUADIC_SUPERALLOY = createCoilBlock(
            CosmicCoilBlock.CoilType.NAQUADIC_SUPERALLOY);
    public static final BlockEntry<CoilBlock> COIL_TRANAVINE = createCoilBlock(CosmicCoilBlock.CoilType.TRINAVINE);
    public static final BlockEntry<CoilBlock> COIL_PSIONIC_GALVORN = createCoilBlock(
            CosmicCoilBlock.CoilType.PSIONIC_GALVORN);
    public static final BlockEntry<CoilBlock> COIL_LIVING_IGNICLAD = createCoilBlock(
            CosmicCoilBlock.CoilType.LIVING_IGNICLAD);
    public static final BlockEntry<CoilBlock> COIL_PROGRAMMABLE_MATTER = createCoilBlock(
            CosmicCoilBlock.CoilType.PROGRAMMABLE_MATTER);
    public static final BlockEntry<CoilBlock> COIL_SHIMMERING_NEUTRONIUM = createCoilBlock(
            CosmicCoilBlock.CoilType.SHIMMERING_NEUTRONIUM);
    public static final BlockEntry<CoilBlock> COIL_CAUSAL_FABRIC = createCoilBlockWithEntity(
            CosmicCoilBlock.CoilType.CAUSAL_FABRIC,
            (ctx, prov) -> {
                String name = ctx.getName();
                ActiveBlock block = ctx.getEntry();
                ModelFile inactive = prov.models()
                        .cubeAll(name, CosmicCore.id("block/casings/coils/causal_fabric_off"));
                ModelFile active = prov.models()
                        .cubeAll(name + "_active", CosmicCore.id("block/casings/coils/causal_fabric"));

                prov.getVariantBuilder(block)
                        .partialState().with(GTBlockStateProperties.ACTIVE, false).modelForState().modelFile(inactive)
                        .addModel()
                        .partialState().with(GTBlockStateProperties.ACTIVE, true).modelForState().modelFile(active)
                        .addModel();
            });

    public static final BlockEntry<HearthPlateBlock> HEARTH_PLATE = REGISTRATE
            .block("hearth_plate", HearthPlateBlock::new)
            .initialProperties(() -> Blocks.STONE)
            .properties(p -> p.strength(0.5f).noOcclusion())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .blockstate(NonNullBiConsumer.noop())
            .item(BlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public static final BlockEntry<Block> BLOOMSCRAP_BLOCK = REGISTRATE
            .block("bloomscrap_block", Block::new)
            .lang("Bloomscrap Block")
            .initialProperties(() -> Blocks.DEEPSLATE)
            .properties(p -> p.isValidSpawn((state, level, pos, entity) -> false)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE_TILES))
            .addLayer(() -> RenderType::solid)
            .exBlockstate(GTModels.cubeAllModel(CosmicCore.id("block/bloomscrap_block")))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .loot((tables, block) -> tables.add(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(CosmicItems.BLOOMSCRAP.get())
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))))))
            .item(BlockItem::new)
            .build()
            .register();

    public static final BlockEntry<MurkKelpBlock> MURK_KELP = flora("murk_kelp", "Murky Kelp", 6,
            MurkKelpBlock::new);
    public static final BlockEntry<MurkFloraBlock> MURK_SEAGRASS = flora("murk_seagrass", "Murky Seagrass", 0);
    public static final BlockEntry<MurkFloraBlock> GLOOM_FAN = flora("gloom_fan", "Gloom Fan", 0);
    public static final BlockEntry<MurkFloraBlock> SHIMMER_TUFT = flora("shimmer_tuft", "Shimmer Tuft", 7);
    public static final BlockEntry<MurkFloraBlock> LANTERN_BULB = flora("lantern_bulb", "Lantern Bulb", 10);
    public static final BlockEntry<MurkFloraBlock> BLOOD_FAN = flora("blood_fan", "Blood Fan", 0);
    public static final BlockEntry<MurkFloraBlock> PALE_POLYP = flora("pale_polyp", "Pale Polyp", 0);
    public static final BlockEntry<MurkFloraBlock> ABYSS_VINE = flora("abyss_vine", "Abyss Vine", 0);
    public static final BlockEntry<MurkFloraBlock> ABYSS_VINE_TIP = flora("abyss_vine_tip", "Abyss Vine", 0);
    public static final BlockEntry<MurkFloraBlock> ANCHORED_BLOOMROT = flora("anchored_bloomrot",
            "Anchored Bloomrot", 0);
    public static final BlockEntry<MurkFloraBlock> BLOOMROT_TENDRILS = flora("bloomrot_tendrils",
            "Bloomrot Tendrils", 7);
    public static final BlockEntry<MurkFloraBlock> BLOOMROT_GROWTH = flora("bloomrot_growth", "Bloomrot Growth", 0);
    public static final BlockEntry<MurkFloraBlock> ARCANITE_CLUSTER = flora("arcanite_cluster", "Arcanite Cluster", 8);
    public static final BlockEntry<LargeArcaniteClusterBlock> LARGE_ARCANITE_CLUSTER = flora(
            "large_arcanite_cluster", "Large Arcanite Cluster", 11, LargeArcaniteClusterBlock::new);

    public static final BlockEntry<BerryVineBlock> RIPE_ABYSS_VINE = REGISTRATE
            .block("ripe_abyss_vine", BerryVineBlock::new)
            .initialProperties(() -> Blocks.SEAGRASS)
            .properties(p -> p.noCollission().instabreak().noOcclusion().lightLevel(state -> 5))
            .lang("Ripe Abyss Vine")
            .blockstate(NonNullBiConsumer.noop())
            .item(BlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    private static BlockEntry<MurkFloraBlock> flora(String name, String lang, int light) {
        return flora(name, lang, light, MurkFloraBlock::new);
    }

    private static <T extends MurkFloraBlock> BlockEntry<T> flora(String name, String lang, int light,
                                                                  NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return REGISTRATE
                .block(name, factory)
                .initialProperties(() -> Blocks.SEAGRASS)
                .properties(p -> p.noCollission().instabreak().noOcclusion().lightLevel(state -> light))
                .lang(lang)
                .blockstate(NonNullBiConsumer.noop())
                .item(BlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    // New Casings ; Several reference textures from GTOCore, make sure to give credits to them!
    public static final BlockEntry<Block> REFLECTIVE_STARMETAL_CASING = createCasingBlock("reflective_starmetal_casing",
            CosmicCore.id("block/casings/solid/reflective_starmetal_casing"));
    public static final BlockEntry<Block> TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING = createCasingBlock(
            "tritanium_lined_heavy_neutronium_casing",
            CosmicCore.id("block/casings/solid/tritanium_lined_heavy_bolted_neutronium_casing"));
    public static final BlockEntry<Block> HIGH_TOLERANCE_RHENIUM_CASING = createCasingBlock(
            "high_tolerance_rhenium_casing",
            CosmicCore.id("block/casings/solid/high_tolerance_rhenium_casing"));
    public static final BlockEntry<Block> HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING = createCasingBlock(
            "highly_flexible_reinforced_trinavine_casing",
            CosmicCore.id("block/casings/solid/highly_flexible_reinforced_trinavine_casing"));
    public static final BlockEntry<Block> CASING_DYSON_CELL = createCasingBlock("dyson_solar_cell",
            CosmicCore.id("block/casings/solid/dyson_solar_cell"));
    public static final BlockEntry<Block> STAR_LADDER_CASING = createCasingBlock("dyson_solar_cell",
            CosmicCore.id("block/casings/solid/dyson_solar_cell"));
    public static final BlockEntry<Block> NAQUADAH_PRESSURE_RESISTANT_CASING = createCasingBlock(
            "naquadah_pressure_resistant_casing",
            CosmicCore.id("block/casings/solid/naquadah_pressure_resistant_casing"));
    public static final BlockEntry<Block> RESONANTLY_TUNED_VIRTUE_MELD_CASING = createCasingBlock(
            "resonantly_tuned_virtue_meld_casing",
            CosmicCore.id("block/casings/solid/resonantly_tuned_virtue_meld_casing"));
    public static final BlockEntry<Block> STEEL_PLATED_BRONZE = createCasingBlock("steel_plated_bronze_casing",
            CosmicCore.id("block/casings/solid/steel_plated_bronze_casing"));
    public static final BlockEntry<Block> ALTERNATOR_FLUX_COILING = createCasingBlock("alternator_flux_coiling",
            CosmicCore.id("block/casings/solid/alternator_flux_coiling_copper"));
    public static final BlockEntry<Block> STEAM_GAS_TURBINE_INTEGRAL_COMPONENTS = createCasingBlock(
            "steam_gas_turbine_integral_components", "Steam/Gas Turbine Integral Components",
            CosmicCore.id("block/casings/solid/steam_gas_turbine_integral_components"));
    public static final BlockEntry<Block> COMBUSTION_INTEGRAL_COMPONENTS = createCasingBlock(
            "combustion_integral_components", "Combustion Integral Components",
            CosmicCore.id("block/casings/solid/combustion_integral_components"));
    public static final BlockEntry<Block> INDUSTRIAL_PARTWORK = createCasingBlock(
            "industrial_partwork", "Industrial Partwork",
            CosmicCore.id("block/casings/solid/industrial_partwork"));
    public static final BlockEntry<Block> INDUSTRIAL_CONVERTER_SHELL = createCasingBlock(
            "industrial_converter_shell", "Industrial Converter Shell",
            CosmicCore.id("block/casings/solid/industrial_converter_shell"));
    public static final BlockEntry<Block> LOW_VOLTAGE_STATOR_HOUSING = createCasingBlock(
            "low_voltage_stator_housing", "Low Voltage Stator Housing",
            CosmicCore.id("block/casings/solid/low_voltage_stator_housing"));
    public static final BlockEntry<Block> MEDIUM_VOLTAGE_STATOR_HOUSING = createCasingBlock(
            "medium_voltage_stator_housing", "Medium Voltage Stator Housing",
            CosmicCore.id("block/casings/solid/medium_voltage_stator_housing"));
    public static final BlockEntry<Block> HIGH_VOLTAGE_STATOR_HOUSING = createCasingBlock(
            "high_voltage_stator_housing", "High Voltage Stator Housing",
            CosmicCore.id("block/casings/solid/high_voltage_stator_housing"));
    public static final BlockEntry<Block> EXTREME_VOLTAGE_STATOR_HOUSING = createCasingBlock(
            "extreme_voltage_stator_housing", "Extreme Voltage Stator Housing",
            CosmicCore.id("block/casings/solid/extreme_voltage_stator_housing"));
    public static final BlockEntry<Block> LIGHTWEIGHT_INDUSTRIAL_CASING = createCasingBlock(
            "lightweight_industrial_casing", CosmicCore.id("block/casings/solid/lightweight_industrial_casing"));
    public static final BlockEntry<Block> LIGHTWEIGHT_MECHANICAL_PARTWORK = createCasingBlock(
            "lightweight_mechanical_partwork", CosmicCore.id("block/casings/solid/lightweight_mechanical_partwork"));
    public static final BlockEntry<Block> LIGHTWEIGHT_LOGISTIC_CASING = createCasingBlock(
            "lightweight_logistic_casing", CosmicCore.id("block/casings/solid/lightweight_logistic_casing"));
    public static final BlockEntry<Block> LIGHTWEIGHT_DARK_STEEL_CASING = createCasingBlock(
            "lightweight_dark_steel_casing", CosmicCore.id("block/casings/solid/lightweight_dark_steel_casing"));
    public static final BlockEntry<Block> LIGHTWEIGHT_STAINLESS_STEEL_CASING = createCasingBlock(
            "lightweight_stainless_steel_casing",
            CosmicCore.id("block/casings/solid/lightweight_stainless_steel_casing"));
    public static final BlockEntry<Block> REFRACTORY_STRUCTURAL_CASING = createCasingBlock(
            "refractory_structural_casing",
            CosmicCore.id("block/casings/solid/refractory_structural_casing"));
    public static final BlockEntry<Block> REFRACTORY_CONTAINMENT_CASING = createCasingBlock(
            "refractory_containment_casing",
            CosmicCore.id("block/casings/solid/refractory_containment_casing"));
    public static final BlockEntry<Block> VIBRANT_PIPE_FRAMEWORK = createCasingBlock(
            "vibrant_pipe_framework",
            CosmicCore.id("block/casings/solid/vibrant_pipe_framework"));
    public static final BlockEntry<Block> PLATED_AEROCLOUD = createCasingBlock("plated_aerocloud",
            CosmicCore.id("block/casings/solid/plated_aerocloud"));
    public static final BlockEntry<Block> SELF_HEALING_PTHANTERUM = createCasingBlock("self_healing_pthanterum_casing",
            CosmicCore.id("block/casings/solid/self_healing_pthanterum_casing"));

    public static final BlockEntry<Block> CRYOGENIC_CASING = createCasingBlock("cryogenic_casing",
            CosmicCore.id("block/casings/solid/cryogenic_casing"));

    public static final BlockEntry<Block> HEAVY_FROST_PROOF_CASING = createCasingBlock("heavy_frost_proof_casing",
            CosmicCore.id("block/casings/solid/heavy_frost_proof_casing"));
    public static final BlockEntry<Block> SOUL_STAINED_STEEL_ALU_CASING = createCasingBlock(
            "soul_stained_steel_aluminium_plated_casing",
            CosmicCore.id("block/casings/solid/soul_stained_steel_aluminium_plated_casing"));
    public static final BlockEntry<Block> LIVING_ROCK_TILES = createCasingBlock("livingrock_tiles",
            CosmicCore.id("block/casings/solid/livingrock_tiles"));

    public static final BlockEntry<Block> NEUTRONIUM_BOUEY = createCasingBlock("neutronium_buoy",
            CosmicCore.id("block/casings/solid/neutronium_buoy"));
    public static final BlockEntry<Block> PTHANTERUM_WAVE_BREAKERS_CASING = createCasingBlock(
            "pthanterum_wave_breakers",
            CosmicCore.id("block/casings/solid/pthanterum_wave_breakers"));
    public static final BlockEntry<Block> CYCLOZINE_HIGH_RIGIDITY_CASING = createCasingBlock(
            "cyclozine_high_rigidity_casing",
            CosmicCore.id("block/casings/solid/cyclozine_high_rigidity_casing"));

    public static final BlockEntry<Block> LIGHT_DAWNSTONE_CASING = createCasingBlock(
            "light_dawnstone_casing",
            CosmicCore.id("block/casings/solid/light_dawnstone_casing"));

    public static final BlockEntry<Block> REINFORCED_DAWNSTONE_CASING = createCasingBlock(
            "reinforced_dawnstone_casing",
            CosmicCore.id("block/casings/solid/reinforced_dawnstone_casing"));

    // Mechanical Alveary
    public static final BlockEntry<Block> ALVEARY_CASING = createCasingBlock(
            "alveary_casing",
            CosmicCore.id("block/casings/solid/alveary_casing"));

    // public static final BlockEntry<Block> SOMAPLASTIC_HEAVY_FRAMES = createCasingBlock("somaplastic_heavy_frames",
    // CosmicCore.id("block/casings/solid/cyclozine_high_rigidity_casing"));
    // public static final BlockEntry<Block> MOON_DIVE_CASING = createCasingBlock("moon_dive_casing",
    // CosmicCore.id("block/casings/solid/moon_dive_casing"));

    public static final BlockEntry<Block> BICHROMAL_NEVRAMITE_CASING = createCasingBlock("bichromal_nevramite_casing",
            CosmicCore.id("block/casings/solid/bichromal_nevramite_casing"));
    public static final BlockEntry<Block> FULGORINTH_PRIME_CASING = createCasingBlock("fulgorinth_prime_casing",
            CosmicCore.id("block/casings/solid/fulgorinth_prime_casing"));
    public static final BlockEntry<Block> OSCILLATING_GILDED_PTHANTERUM_CASING = createCasingBlock(
            "oscillating_gilded_pthanterum_casings",
            CosmicCore.id("block/casings/solid/oscillating_gilded_pthanterum_casings"));
    public static final BlockEntry<Block> PRESSURE_CONTAINMNET_CASING = createCasingBlock("pressure_containment_casing",
            CosmicCore.id("block/casings/solid/pressure_containment_casing"));
    public static final BlockEntry<Block> ROYAL_ICHORIUM_CASING = createCasingBlock("royal_ichorium_casing",
            CosmicCore.id("block/casings/solid/royal_ichorium_casing"));
    public static final BlockEntry<Block> VIBRANT_RUBIDIUM_CASING = createCasingBlock("vibrant_rubidium_casing",
            CosmicCore.id("block/casings/solid/vibrant_rubidium_casing"));
    public static final BlockEntry<Block> WAILING_ICHOR_CASING = createCasingBlock("wailing_ichor_casing",
            CosmicCore.id("block/casings/solid/wailing_ichor_casing"));

    public static final BlockEntry<MagnetBlock> MAGNET_HIGH_POWERED = createMagnetBlock(
            MagnetBlock.MagnetType.HIGH_POWERED);
    public static final BlockEntry<MagnetBlock> MAGNET_FUSION_GRADE = createMagnetBlock(
            MagnetBlock.MagnetType.FUSION_GRADE);
    public static final BlockEntry<MagnetBlock> MAGNET_STELLAR_GRADE = createMagnetBlock(
            MagnetBlock.MagnetType.STELLAR_NEUTRONIUM_GRADE);

    // TODO : FIGURE OUT WHY these are breaking the minable tags for pickaxe/wrench..
    public static final BlockEntry<Block> GILDED_PTHANTERUM_CASING = createCasingBlock(
            "gilded_pthanterum_casing", CosmicCore.id("block/casings/solid/gilded_pthanterum_casing"));
    public static final BlockEntry<Block> WEAR_RESISTANT_RURIDIT_CASING = createCasingBlock(
            "wear_resistant_ruridit_casing", CosmicCore.id("block/casings/solid/ruridit_casing"));
    public static final BlockEntry<Block> REINFORCED_NAQUADRIA_CASING = createCasingBlock(
            "reinforced_naquadria_casing", CosmicCore.id("block/casings/solid/reinforced_naquadria_casing"));
    public static final BlockEntry<Block> HIGH_TEMP_FISSION_CASING = createCasingBlock(
            "high_temperature_fission_casing", CosmicCore.id("block/casings/solid/high_temperature_fission_casing"));
    public static final BlockEntry<Block> CYCLOZINE_CHEMICALLY_REPELLING_CASING = createCasingBlock(
            "cyclozine_chemically_repelling_casing",
            CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"));
    public static final BlockEntry<Block> CYCLOZINE_CHEMICALLY_REPELLING_PIPE = createCasingBlock(
            "cyclozine_chemically_repelling_pipe",
            CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_pipe"));
    public static final BlockEntry<Block> MULTIPURPOSE_INTERSTELLAR_GRADE_CASING = createCasingBlock(
            "multi_purpose_interstellar_grade_casing",
            CosmicCore.id("block/casings/solid/vomahine_certified_interstellar_grade_casing"));
    public static final BlockEntry<Block> ULTRA_POWERED_CASING = createCasingBlock(
            "ultra_powered_casing", CosmicCore.id("block/casings/solid/vomahine_ultra_powered_casing"));
    public static final BlockEntry<ComputationBayCasingBlock> ME_COMPUTATION_BAY_CASING = REGISTRATE
            .block("me_computation_bay_casing", ComputationBayCasingBlock::new)
            .lang("ME Computation Bay Casing")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .addLayer(() -> RenderType::cutoutMipped)
            .exBlockstate(GTModels.cubeAllModel(
                    CosmicCore.id("block/casings/solid/compute_module_base")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<Block> HIGHLY_CONDUCTIVE_FISSION_CASING = createCasingBlock(
            "highly_conductive_fission_casing", CosmicCore.id("block/casings/solid/highly_conductive_fission_casing"));
    public static final BlockEntry<Block> GEARBOX_PTHANTERUM = createCasingBlock(
            "machine_casing_gearbox_pthanterum",
            CosmicCore.id("block/casings/gearbox/machine_casing_gearbox_pthanterum"));
    public static final BlockEntry<Block> GEARBOX_NAQUADRIA = createCasingBlock(
            "machine_casing_gearbox_naquadria",
            CosmicCore.id("block/casings/gearbox/machine_casing_gearbox_naquadria"));

    public static final BlockEntry<Block> RUST_WEAVE_CASING = createCasingBlock(
            "rust_weave_casing",
            CosmicCore.id("block/casings/solid/rust_weave_casing"));

    public static final BlockEntry<Block> RUST_STAINED_CASING = createCasingBlock(
            "rust_stained_casing",
            CosmicCore.id("block/casings/solid/rust_stained_casing"));

    public static final BlockEntry<Block> SOMARUST_CASING = createCasingBlock(
            "somarust_casing",
            CosmicCore.id("block/casings/solid/somarust_casing"));

    public static final BlockEntry<Block> SOUL_MUTED_CASING = createCasingBlock(
            "soul_muted_casing",
            CosmicCore.id("block/casings/solid/soul_muted_casing"));

    // Blood Magic replacement blocks
    public static final BlockEntry<Block> BLANK_RUNE = createCasingBlock(
            "blank_rune",
            CosmicCore.id("block/casings/solid/soul_muted_casing"));
    public static final BlockEntry<Block> RITUAL_STONE = createCasingBlock(
            "ritual_stone",
            CosmicCore.id("block/casings/solid/soul_muted_casing"));
    public static final BlockEntry<Block> LIGHT_RITUAL_STONE = createCasingBlock(
            "light_ritual_stone",
            CosmicCore.id("block/casings/solid/soul_muted_casing"));

    // Star Ladder Casings

    public static final BlockEntry<Block> SUPERHEAVY_STEEL_CASING = createCasingBlock(
            "superheavy_steel_casing",
            CosmicCore.id("block/casings/solid/superheavy_steel_casing"));

    public static final BlockEntry<Block> RIGID_HIGH_SPEED_STEEL_CASING = createCasingBlock(
            "rigid_high_speed_steel_casing",
            CosmicCore.id("block/casings/solid/rigid_high_speed_steel_casing"));

    public static final BlockEntry<Block> BOLTED_HEAVY_FRAME_CASING = createCasingBlock(
            "bolted_heavy_frame_casing",
            CosmicCore.id("block/casings/solid/bolted_heavy_frame_casing"));

    public static final BlockEntry<Block> ETHERSTEEL_PLATED_ASH_TILES = createStoneCasingBlock(
            "ethersteel_plated_ash_tiles",
            CosmicCore.id("block/casings/solid/ethersteel_plated_ash_tiles"));

    // I think i deleted the uh, yeah..
    public static final BlockEntry<ActiveBlock> CASING_HEAT_VENT = createActiveCasing("heat_fan",
            "block/variant/heat_fan");
    public static final BlockEntry<ActiveBlock> CASING_INTAKE_LUDICRIOUS = createActiveCasing("ludicrious_intake",
            "block/variant/ludicrious_intake");
    public static final BlockEntry<ActiveBlock> CASING_INTAKE_ULTIMATE = createActiveCasing("ultimate_intake",
            "block/variant/ultimate_intake");
    public static final BlockEntry<ActiveBlock> RADIOACTIVE_FILTER_CASING = createActiveCasing(
            "radioactive_filter_casing",
            "block/variant/radioactive_filter_casing");

    public static final BlockEntry<LanternBlock> STEEL_ROSE_LANTERN = createLantern("steel_rose_lantern",
            CosmicCore.id("block/lanterns/steel_rose_lantern"),
            CosmicCore.id("block/lanterns/steel_rose_lantern_hanging"));

    public static final BlockGroup STEEL_ROSE_LIGHT = createStoneBuildingBlock(
            "steel_rose_light",
            CosmicCore.id("block/casings/cosmetic/steel_rose_light"));

    public static final BlockGroup IRON_PLATED_DEEPSLATE_BLOCK = createStoneBuildingBlock(
            "iron_plated_deepslate_tile",
            CosmicCore.id("block/casings/cosmetic/iron_plated_deepslate_tile"));

    /*
     * SHELVED ember blocks â€” Embers dropped on 1.21.1 (beads cosmiccore-42.13 / 42.14)
     * public static final Map<Integer, BlockEntry<CosmicEmberEmitterBlock>> EMBER_EMITTER_BLOCKS = new
     * Int2ReferenceArrayMap<>();
     * public static final Map<Integer, BlockEntry<CosmicEmberReceptorBlock>> EMBER_RECEPTOR_BLOCKS = new
     * Int2ReferenceArrayMap<>();
     * 
     * // RECIEVERS
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_STEAM = REGISTRATE
     * .block("steam_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 0))
     * .lang("Steam Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("steam_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_LV = REGISTRATE
     * .block("lv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 1))
     * .lang("LV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("lv_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_MV = REGISTRATE
     * .block("mv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 2))
     * .lang("MV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("mv_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_HV = REGISTRATE
     * .block("hv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 3))
     * .lang("HV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("hv_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_EV = REGISTRATE
     * .block("ev_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 4))
     * .lang("EV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("ev_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_IV = REGISTRATE
     * .block("iv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 5))
     * .lang("IV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("iv_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_LUV = REGISTRATE
     * .block("luv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 6))
     * .lang("LuV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("luv_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_ZPM = REGISTRATE
     * .block("zpm_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 7))
     * .lang("ZPM Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("zpm_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_UV = REGISTRATE
     * .block("uv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 8))
     * .lang("UV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("uv_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_UHV = REGISTRATE
     * .block("uhv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 9))
     * .lang("UHV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("uhv_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_UEV = REGISTRATE
     * .block("uev_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 10))
     * .lang("UEV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("uev_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_UIV = REGISTRATE
     * .block("uiv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 11))
     * .lang("UIV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("uiv_ember_receptor")));
     * })
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_UXV = REGISTRATE
     * .block("uxv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 12))
     * .lang("UXV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("uxv_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_OPV = REGISTRATE
     * .block("opv_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 13))
     * .lang("OPV Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("opv_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberReceptorBlock> COSMIC_EMBER_RECEIVER_MAX = REGISTRATE
     * .block("max_ember_receptor", props -> new CosmicEmberReceptorBlock(props, 14))
     * .lang("MAX Ember Receptor")
     * .initialProperties(RegistryManager.EMBER_RECEIVER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("max_ember_receptor")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * // EMITTERS
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_STEAM = REGISTRATE
     * .block("steam_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 0))
     * .lang("Steam Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("steam_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_LV = REGISTRATE
     * .block("lv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 1))
     * .lang("LV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(), prov.models().getExistingFile(CosmicCore.id("lv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_MV = REGISTRATE
     * .block("mv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 2))
     * .lang("MV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(), prov.models().getExistingFile(CosmicCore.id("mv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_HV = REGISTRATE
     * .block("hv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 3))
     * .lang("HV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(), prov.models().getExistingFile(CosmicCore.id("hv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_EV = REGISTRATE
     * .block("ev_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 4))
     * .lang("EV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(), prov.models().getExistingFile(CosmicCore.id("ev_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_IV = REGISTRATE
     * .block("iv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 5))
     * .lang("IV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(), prov.models().getExistingFile(CosmicCore.id("iv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_LUV = REGISTRATE
     * .block("luv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 6))
     * .lang("LuV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("luv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_ZPM = REGISTRATE
     * .block("zpm_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 7))
     * .lang("ZPM Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("zpm_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_UV = REGISTRATE
     * .block("uv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 8))
     * .lang("UV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(), prov.models().getExistingFile(CosmicCore.id("uv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_UHV = REGISTRATE
     * .block("uhv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 9))
     * .lang("UHV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("uhv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_UEV = REGISTRATE
     * .block("uev_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 10))
     * .lang("UEV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("uev_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_UIV = REGISTRATE
     * .block("uiv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 11))
     * .lang("UIV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("uiv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_UXV = REGISTRATE
     * .block("uxv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 12))
     * .lang("UXV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("uxv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_OPV = REGISTRATE
     * .block("opv_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 13))
     * .lang("OPV Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("opv_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * public static final BlockEntry<CosmicEmberEmitterBlock> COSMIC_EMBER_EMITTER_MAX = REGISTRATE
     * .block("max_ember_emitter", props -> new CosmicEmberEmitterBlock(props, 14))
     * .lang("MAX Ember Emitter")
     * .initialProperties(RegistryManager.EMBER_EMITTER::get)
     * .exBlockstate((ctx, prov) -> {
     * prov.directionalBlock(ctx.getEntry(),
     * prov.models().getExistingFile(CosmicCore.id("max_ember_emitter")));
     * })
     * .tag(BlockTags.MINEABLE_WITH_PICKAXE)
     * .item(BlockItem::new)
     * .build()
     * .register();
     * 
     * static {
     * EMBER_EMITTER_BLOCKS.put(0, COSMIC_EMBER_EMITTER_STEAM);
     * EMBER_EMITTER_BLOCKS.put(1, COSMIC_EMBER_EMITTER_LV);
     * EMBER_EMITTER_BLOCKS.put(2, COSMIC_EMBER_EMITTER_MV);
     * EMBER_EMITTER_BLOCKS.put(3, COSMIC_EMBER_EMITTER_HV);
     * EMBER_EMITTER_BLOCKS.put(4, COSMIC_EMBER_EMITTER_EV);
     * EMBER_EMITTER_BLOCKS.put(5, COSMIC_EMBER_EMITTER_IV);
     * EMBER_EMITTER_BLOCKS.put(6, COSMIC_EMBER_EMITTER_LUV);
     * EMBER_EMITTER_BLOCKS.put(7, COSMIC_EMBER_EMITTER_ZPM);
     * EMBER_EMITTER_BLOCKS.put(8, COSMIC_EMBER_EMITTER_UV);
     * EMBER_EMITTER_BLOCKS.put(9, COSMIC_EMBER_EMITTER_UHV);
     * EMBER_EMITTER_BLOCKS.put(10, COSMIC_EMBER_EMITTER_UEV);
     * EMBER_EMITTER_BLOCKS.put(11, COSMIC_EMBER_EMITTER_UIV);
     * EMBER_EMITTER_BLOCKS.put(12, COSMIC_EMBER_EMITTER_UXV);
     * EMBER_EMITTER_BLOCKS.put(13, COSMIC_EMBER_EMITTER_OPV);
     * EMBER_EMITTER_BLOCKS.put(14, COSMIC_EMBER_EMITTER_MAX);
     * 
     * EMBER_RECEPTOR_BLOCKS.put(0, COSMIC_EMBER_RECEIVER_STEAM);
     * EMBER_RECEPTOR_BLOCKS.put(1, COSMIC_EMBER_RECEIVER_LV);
     * EMBER_RECEPTOR_BLOCKS.put(2, COSMIC_EMBER_RECEIVER_MV);
     * EMBER_RECEPTOR_BLOCKS.put(3, COSMIC_EMBER_RECEIVER_HV);
     * EMBER_RECEPTOR_BLOCKS.put(4, COSMIC_EMBER_RECEIVER_EV);
     * EMBER_RECEPTOR_BLOCKS.put(5, COSMIC_EMBER_RECEIVER_IV);
     * EMBER_RECEPTOR_BLOCKS.put(6, COSMIC_EMBER_RECEIVER_LUV);
     * EMBER_RECEPTOR_BLOCKS.put(7, COSMIC_EMBER_RECEIVER_ZPM);
     * EMBER_RECEPTOR_BLOCKS.put(8, COSMIC_EMBER_RECEIVER_UV);
     * EMBER_RECEPTOR_BLOCKS.put(9, COSMIC_EMBER_RECEIVER_UHV);
     * EMBER_RECEPTOR_BLOCKS.put(10, COSMIC_EMBER_RECEIVER_UEV);
     * EMBER_RECEPTOR_BLOCKS.put(11, COSMIC_EMBER_RECEIVER_UIV);
     * EMBER_RECEPTOR_BLOCKS.put(12, COSMIC_EMBER_RECEIVER_UXV);
     * EMBER_RECEPTOR_BLOCKS.put(13, COSMIC_EMBER_RECEIVER_OPV);
     * EMBER_RECEPTOR_BLOCKS.put(14, COSMIC_EMBER_RECEIVER_MAX);
     * }
     */

    // GLASS BLOCKS
    public static final BlockEntry<Block> ZBLAN_REINFORCED_GLASS = createGlassCasingBlock(
            "zblan_glass", CosmicCore.id("block/casings/glass/zblan_glass"), () -> RenderType::translucent);

    private static BlockEntry<Block> createGlassCasingBlock(String name, ResourceLocation texture,
                                                            Supplier<Supplier<RenderType>> type) {
        NonNullFunction<BlockBehaviour.Properties, Block> supplier = TransparentBlock::new;
        return REGISTRATE.block(name, supplier)
                .initialProperties(() -> Blocks.GLASS)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(type)
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    public record BlockGroup(BlockEntry<Block> block, BlockEntry<StairBlock> stairs, BlockEntry<SlabBlock> slab) {}

    public static BlockGroup createStoneBuildingBlock(String name, ResourceLocation texture) {
        return createStoneBuildingBlocks(name, texture, () -> Blocks.DEEPSLATE_BRICKS,
                () -> RenderType::solid);
    }

    public static BlockGroup createStoneBuildingBlocks(String name,
                                                       ResourceLocation texture,
                                                       NonNullSupplier<? extends Block> properties,
                                                       Supplier<Supplier<RenderType>> type) {
        BlockEntry<Block> fullBlock = REGISTRATE.block(name, Block::new)
                .initialProperties(properties)
                .properties(p -> p.isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                        .speedFactor(1.25f)
                        .requiresCorrectToolForDrops()
                        .strength(5, 6)
                        .sound(SoundType.NETHERITE_BLOCK))
                .addLayer(type)
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();

        BlockEntry<StairBlock> stairBlock = REGISTRATE
                .block(name + "_stairs", s -> new StairBlock(fullBlock.getDefaultState(), s))
                .initialProperties(fullBlock)
                .properties(p -> p.isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                        .speedFactor(1.25f)
                        .requiresCorrectToolForDrops()
                        .strength(5, 6)
                        .sound(SoundType.NETHERITE_BLOCK))
                .addLayer(type)
                .blockstate((ctx, prov) -> prov.stairsBlock(ctx.get(), texture))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();

        BlockEntry<SlabBlock> slabBlock = REGISTRATE.block(name + "_slab", SlabBlock::new)
                .initialProperties(fullBlock)
                .properties(p -> p.isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                        .speedFactor(1.25f)
                        .requiresCorrectToolForDrops()
                        .strength(5, 6)
                        .sound(SoundType.NETHERITE_BLOCK))
                .addLayer(type)
                .blockstate((ctx, prov) -> {
                    var slab = prov.models().slab(ctx.getName(), texture, texture, texture);
                    var slabTop = prov.models().slabTop(ctx.getName() + "_top", texture, texture, texture);
                    var fullModel = prov.models().cubeAll(name, texture);

                    prov.slabBlock((SlabBlock) ctx.get(), slab, slabTop, fullModel);
                })
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();

        return new BlockGroup(fullBlock, stairBlock, slabBlock);
    }

    public static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, Block::new, texture, () -> Blocks.IRON_BLOCK,
                () -> RenderType::cutoutMipped);
    }

    public static BlockEntry<Block> createCasingBlock(String name, String lang, ResourceLocation texture) {
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .lang(lang)
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    public static BlockEntry<Block> createCasingBlock(String name,
                                                      NonNullFunction<BlockBehaviour.Properties, Block> blockSupplier,
                                                      ResourceLocation texture,
                                                      NonNullSupplier<? extends Block> properties,
                                                      Supplier<Supplier<RenderType>> type) {
        return REGISTRATE.block(name, blockSupplier)
                .initialProperties(properties)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(type)
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<Block> createSidedCasingBlock(String name, ResourceLocation texture) {
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createSidedCasingModel(texture))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<CoilBlock> createCoilBlock(ICoilType coilType) {
        BlockEntry<CoilBlock> coilBlock = REGISTRATE
                .block("%s_coil_block".formatted(coilType.getName()), p -> new CoilBlock(p, coilType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createCoilModel(coilType))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
        GTCEuAPI.HEATING_COILS.put(coilType, coilBlock);
        return coilBlock;
    }

    private static BlockEntry<CoilBlock> createCoilBlockWithEntity(ICoilType coilType,
                                                                   NonNullBiConsumer<DataGenContext<Block, CoilBlock>, RegistrateBlockstateProvider> blockState) {
        BlockEntry<CoilBlock> coilBlock = REGISTRATE
                .block("%s_coil_block".formatted(coilType.getName()), p -> (CoilBlock) new CosmicCoilBlock(p, coilType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::translucent)
                .blockstate(blockState)
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .simpleItem()
                .blockEntity(CosmicCoilBlockEntity::new)
                .renderer(() -> NebulaeCoilRenderer::new)
                .build()
                .register();
        GTCEuAPI.HEATING_COILS.put(coilType, coilBlock);
        return coilBlock;
    }

    protected static BlockEntry<ActiveBlock> createActiveCasing(String name, String baseModelPath) {
        return REGISTRATE.block(name, ActiveBlock::new)
                .initialProperties(() -> Blocks.NETHERITE_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createActiveModel(CosmicCore.id(baseModelPath)))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), CosmicCore.id(baseModelPath)))
                .build()
                .register();
    }

    private static BlockEntry<MagnetBlock> createMagnetBlock(IMagnetType magnetType) {
        BlockEntry<MagnetBlock> magnetBlock = REGISTRATE
                .block("%s_magnet".formatted(magnetType.getName()), p -> new MagnetBlock(p, magnetType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(createMagnetModel("%s_magnet".formatted(magnetType.getName()), magnetType))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
        CosmicCoreAPI.MAGNET_COILS.put(magnetType, magnetBlock);

        return magnetBlock;
    }

    public static NonNullBiConsumer<DataGenContext<Block, MagnetBlock>, RegistrateBlockstateProvider> createMagnetModel(String name,
                                                                                                                        IMagnetType magnetType) {
        return (ctx, prov) -> {
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().cubeAll(name, magnetType.getTexture());
            ModelFile active = prov.models().withExistingParent(name + "_active", GTCEu.id("block/cube_2_layer/all"))
                    .texture("bot_all", magnetType.getTexture())
                    .texture("top_all", magnetType.getTexture().withSuffix("_bloom"));
            prov.getVariantBuilder(block)
                    .partialState().with(ACTIVE, false).modelForState().modelFile(inactive).addModel()
                    .partialState().with(ACTIVE, true).modelForState().modelFile(active).addModel();
        };
    }

    private static BlockBehaviour.Properties ironProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                .requiresCorrectToolForDrops()
                .strength(5, 6)
                .sound(SoundType.COPPER);
    }

    public static BlockEntry<LanternBlock> createLantern(String name, ResourceLocation texture,
                                                         ResourceLocation textureHanging) {
        return createLantern(
                name,
                LanternBlock::new,
                texture,
                textureHanging,
                () -> Blocks.LANTERN,
                () -> RenderType::cutout,
                15);
    }

    public static BlockEntry<LanternBlock> createLantern(String name,
                                                         NonNullFunction<BlockBehaviour.Properties, LanternBlock> blockSupplier,
                                                         ResourceLocation texture, ResourceLocation hangingTexture,
                                                         NonNullSupplier<? extends Block> properties,
                                                         java.util.function.Supplier<java.util.function.Supplier<RenderType>> type,
                                                         int lightLevel) {
        return REGISTRATE.block(name, blockSupplier)
                .initialProperties(properties)
                .properties(prop -> prop
                        .strength(3.5f)
                        .sound(SoundType.LANTERN)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()
                        .lightLevel(l -> lightLevel))
                .addLayer(type)
                .blockstate((context, provider) -> {

                    var standing = provider.models()
                            .withExistingParent(context.getName(), provider.mcLoc("block/lantern"))
                            .texture("lantern", texture)
                            .texture("particle", texture);

                    var hanging = provider.models()
                            .withExistingParent(context.getName() + "_hanging", provider.mcLoc("block/lantern_hanging"))
                            .texture("lantern", hangingTexture)
                            .texture("particle", hangingTexture);

                    provider.getVariantBuilder(context.get())
                            .forAllStates(state -> {
                                boolean isHanging = state.getValue(BlockStateProperties.HANGING);
                                return new ConfiguredModel[] {
                                        new ConfiguredModel(
                                                isHanging ? hanging : standing)
                                };
                            });
                })
                .tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .item(BlockItem::new)
                .model((context, provider) -> provider.withExistingParent(context.getName(),
                        provider.modLoc("block/" + context.getName())))
                .build()
                .register();
    }

    public static BlockEntry<Block> createStoneCasingBlock(String name,
                                                           ResourceLocation texture) {
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.DEEPSLATE)
                .properties(p -> p.isValidSpawn(((blockState, blockGetter, blockPos, entityType) -> false))
                        .requiresCorrectToolForDrops()
                        .strength(5, 6)
                        .sound(SoundType.DEEPSLATE_TILES))
                .addLayer(() -> RenderType::solid)
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();
    }

    public static void init() {}
}
