package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.CosmicCoreAPI;
import com.ghostipedia.cosmiccore.api.block.IMagnetType;
import com.ghostipedia.cosmiccore.client.renderer.block.NebulaeCoilRenderer;
import com.ghostipedia.cosmiccore.common.block.MagnetBlock;
import com.ghostipedia.cosmiccore.common.data.recipe.RecipeTags;
import com.ghostipedia.cosmiccore.common.item.RenderBlockItem;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTModels;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.client.model.generators.ModelFile;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import java.util.Map;
import java.util.function.Supplier;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicBlocks {

    static {
        REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);

    }
    // Coil Register

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
    public static final BlockEntry<CoilBlock> COIL_CAUSAL_FABRIC = createCoilBlock(
            CosmicCoilBlock.CoilType.CAUSAL_FABRIC,
            Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                    Map.of("all", CosmicCore.id("block/casings/coils/causal_fabric_off"))) : null,
            Platform.isClient() ? new NebulaeCoilRenderer(new ResourceLocation("block/cube_all"),
                    Map.of("all", CosmicCoilBlock.CoilType.CAUSAL_FABRIC.getTexture())) : null

    );
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

    public static final BlockEntry<MagnetBlock> MAGNET_HIGH_POWERED = createMagnetBlock(
            MagnetBlock.MagnetType.HIGH_POWERED);
    public static final BlockEntry<MagnetBlock> MAGNET_FUSION_GRADE = createMagnetBlock(
            MagnetBlock.MagnetType.FUSION_GRADE);

    // TODO : FIGURE OUT WHY these are breaking the minable tags for pickaxe/wrench..
    public static final BlockEntry<Block> HIGH_TEMP_FISSION_CASING = createCasingBlock(
            "high_temperature_fission_casing", CosmicCore.id("block/casings/solid/high_temperature_fission_casing"));
    public static final BlockEntry<Block> VOMAHINE_CERTIFIED_CHEMICALLY_RESISTANT_CASING = createCasingBlock(
            "vomahine_certified_chemically_resistant_casing",
            CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"));
    public static final BlockEntry<Block> VOMAHINE_CERTIFIED_CHEMICALLY_RESISTANT_PIPE = createCasingBlock(
            "vomahine_certified_chemically_resistant_pipe",
            CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_pipe"));
    public static final BlockEntry<Block> VOMAHINE_CERTIFIED_INTERSTELLAR_GRADE_CASING = createCasingBlock(
            "vomahine_certified_interstellar_grade_casing",
            CosmicCore.id("block/casings/solid/vomahine_certified_interstellar_grade_casing"));
    public static final BlockEntry<Block> VOMAHINE_ULTRA_POWERED_CASING = createCasingBlock(
            "vomahine_ultra_powered_casing", CosmicCore.id("block/casings/solid/vomahine_ultra_powered_casing"));
    public static final BlockEntry<Block> HIGHLY_CONDUCTIVE_FISSION_CASING = createCasingBlock(
            "highly_conductive_fission_casing", CosmicCore.id("block/casings/solid/highly_conductive_fission_casing"));

    // This is a Bunch of Rendering Magic I barely understand (See: I Don't understand at all) ~Ghost
    private static BlockEntry<Block> createGlassCasingBlock(String name, ResourceLocation texture,
                                                            Supplier<Supplier<RenderType>> type) {
        return createCasingBlock(name, GlassBlock::new, texture, () -> Blocks.GLASS, type);
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
                .blockstate(GTModels.cubeAllModel(name, texture))
                .tag(RecipeTags.MINEABLE_WITH_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<Block> createSidedCasingBlock(String name, ResourceLocation texture) {
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createSidedCasingModel(name, texture))
                .tag(RecipeTags.MINEABLE_WITH_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<CoilBlock> createCoilBlock(ICoilType coilType) {
        BlockEntry<CoilBlock> coilBlock = REGISTRATE
                .block("%s_coil_block".formatted(coilType.getName()), p -> new CoilBlock(p, coilType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createCoilModel("%s_coil_block".formatted(coilType.getName()), coilType))
                .tag(RecipeTags.MINEABLE_WITH_WRENCH, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();
        GTCEuAPI.HEATING_COILS.put(coilType, coilBlock);
        return coilBlock;
    }

    private static BlockEntry<CoilBlock> createCoilBlock(ICoilType coilType, IRenderer renderer,
                                                         IRenderer activeRenderer) {
        BlockEntry<CoilBlock> coilBlock = REGISTRATE
                .block("%s_coil_block".formatted(coilType.getName()),
                        p -> (CoilBlock) new CosmicCoilBlock(p, coilType, renderer, activeRenderer))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::translucent)
                .blockstate(NonNullBiConsumer.noop())
                .tag(RecipeTags.MINEABLE_WITH_WRENCH, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RenderBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
        GTCEuAPI.HEATING_COILS.put(coilType, coilBlock);
        return coilBlock;
    }

    private static BlockEntry<MagnetBlock> createMagnetBlock(IMagnetType magnetType) {
        BlockEntry<MagnetBlock> magnetBlock = REGISTRATE
                .block("%s_magnet".formatted(magnetType.getName()), p -> new MagnetBlock(p, magnetType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(createMagnetModel("%s_magnet".formatted(magnetType.getName()), magnetType))
                .tag(RecipeTags.MINEABLE_WITH_WRENCH, BlockTags.MINEABLE_WITH_PICKAXE)
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
                    .partialState().with(ActiveBlock.ACTIVE, false).modelForState().modelFile(inactive).addModel()
                    .partialState().with(ActiveBlock.ACTIVE, true).modelForState().modelFile(active).addModel();
        };
    }

    public static void init() {}
}
