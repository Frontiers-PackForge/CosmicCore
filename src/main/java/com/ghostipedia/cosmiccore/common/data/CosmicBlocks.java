package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.CosmicCoreAPI;
import com.ghostipedia.cosmiccore.api.block.IMagnetType;
import com.ghostipedia.cosmiccore.client.renderer.block.NebulaeCoilRenderer;
import com.ghostipedia.cosmiccore.common.block.MagnetBlock;
import com.ghostipedia.cosmiccore.common.blockentity.CosmicCoilBlockEntity;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.model.generators.ModelFile;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import earth.terrarium.adastra.common.blocks.GlobeBlock;

import java.util.function.Supplier;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties.ACTIVE;
import static earth.terrarium.adastra.common.registry.ModBlocks.GLOBES;

public class CosmicBlocks {

    static {
        REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);

    }
    // Coil Register
    public static final RegistryEntry<Block> SUN_GLOBE = GLOBES.register("sun_globe",
            () -> new GlobeBlock(ironProperties().noOcclusion()));

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
    public static final BlockEntry<Block> PLATED_AEROCLOUD = createCasingBlock("plated_aerocloud",
            CosmicCore.id("block/casings/solid/plated_aerocloud"));
    public static final BlockEntry<Block> SELF_HEALING_PTHANTERUM = createCasingBlock("self_healing_pthanterum_casing",
            CosmicCore.id("block/casings/solid/self_healing_pthanterum_casing"));

    public static final BlockEntry<Block> NEUTRONIUM_BOUEY = createCasingBlock("neutronium_buoy",
            CosmicCore.id("block/casings/solid/neutronium_buoy"));
    public static final BlockEntry<Block> PTHANTERUM_WAVE_BREAKERS_CASING = createCasingBlock("pthanterum_wave_breakers",
            CosmicCore.id("block/casings/solid/pthanterum_wave_breakers"));
    public static final BlockEntry<Block> CYCLOZINE_HIGH_RIGIDITY_CASING = createCasingBlock("cyclozine_high_rigidity_casing",
            CosmicCore.id("block/casings/solid/cyclozine_high_rigidity_casing"));
//    public static final BlockEntry<Block> SOMAPLASTIC_HEAVY_FRAMES = createCasingBlock("somaplastic_heavy_frames",
//            CosmicCore.id("block/casings/solid/cyclozine_high_rigidity_casing"));
//    public static final BlockEntry<Block> MOON_DIVE_CASING = createCasingBlock("moon_dive_casing",
//            CosmicCore.id("block/casings/solid/moon_dive_casing"));

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
    public static final BlockEntry<Block> HIGHLY_CONDUCTIVE_FISSION_CASING = createCasingBlock(
            "highly_conductive_fission_casing", CosmicCore.id("block/casings/solid/highly_conductive_fission_casing"));
    public static final BlockEntry<Block> GEARBOX_PTHANTERUM = createCasingBlock(
            "machine_casing_gearbox_pthanterum",
            CosmicCore.id("block/casings/gearbox/machine_casing_gearbox_pthanterum"));
    public static final BlockEntry<Block> GEARBOX_NAQUADRIA = createCasingBlock(
            "machine_casing_gearbox_naquadria",
            CosmicCore.id("block/casings/gearbox/machine_casing_gearbox_naquadria"));
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

    // GLASS BLOCKS
    public static final BlockEntry<Block> ZBLAN_REINFORCED_GLASS = createGlassCasingBlock(
            "zblan_glass", CosmicCore.id("block/casings/glass/zblan_glass"), () -> RenderType::translucent);

    // This is a Bunch of Rendering Magic I barely understand (See: I Don't understand at all) ~Ghost
    private static BlockEntry<Block> createGlassCasingBlock(String name, ResourceLocation texture,
                                                            Supplier<Supplier<RenderType>> type) {
        NonNullFunction<BlockBehaviour.Properties, Block> supplier = GlassBlock::new;
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

    public static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, Block::new, texture, () -> Blocks.IRON_BLOCK,
                () -> RenderType::cutoutMipped);
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

    public static void init() {}
}
