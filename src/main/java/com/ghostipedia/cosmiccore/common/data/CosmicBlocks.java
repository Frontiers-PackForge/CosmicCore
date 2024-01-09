package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.gregtechceu.gtceu.api.block.RendererBlock;
import com.gregtechceu.gtceu.api.block.RendererGlassBlock;
import com.gregtechceu.gtceu.api.item.RendererBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistries.REGISTRATE;


public class CosmicBlocks {
    static {
        REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
    }

    // region casings
    public static final BlockEntry<Block> CASING_ALUMINIUM_AEROSPACE = createCasingBlock("aerospace_aluminium_casing", CosmicCore.id("block/casings/solid/machine_casing_aerospace"));
    public static final BlockEntry<Block> CASING_BEAM_RECEIVER = createCasingBlock("beam_receiver", CosmicCore.id("block/casings/solid/beam_receiver"));
    public static final BlockEntry<Block> CASING_SUPPORT = createCasingBlock("space_elevator_support", CosmicCore.id("block/casings/solid/space_elevator_support"));

    public static final BlockEntry<Block> CASING_DYSON_SPHERE = createCasingBlock("dyson_sphere_casing", CosmicCore.id("block/casings/solid/dyson_sphere"));
    public static final BlockEntry<Block> CASING_DYSON_CELL = createCasingBlock("dyson_solar_cell", CosmicCore.id("block/casings/solid/dyson_solar_cell"));
    public static final BlockEntry<Block> CASING_DYSON_PORT = createCasingBlock("dyson_sphere_maintenance_port", CosmicCore.id("block/casings/solid/dyson_sphere_maintenance_port"));
    public static final BlockEntry<Block> ALTERNATOR_FLUX_COILING = createCasingBlock("alternator_flux_coiling", CosmicCore.id("block/casings/solid/alternator_flux_coiling_copper"));
    public static final BlockEntry<Block> PLATED_AEROCLOUD = createCasingBlock("plated_aerocloud", CosmicCore.id("block/casings/solid/plated_aerocloud"));
    //'cosmiccore:alternator_flux_coiling'
    // endregion


    // region moon
//    public static final BlockEntry<Block> MOON_STONE = REGISTRATE
//            .block("moon_stone", Block::new)
//            .initialProperties(() -> Blocks.STONE)
//            .simpleItem()
//            .register();

//    public static final BlockEntry<FallingBlock> MOON_SAND = REGISTRATE
//            .block("moon_sand", FallingBlock::new)
//            .initialProperties(() -> Blocks.GRAVEL)
//            .simpleItem()
//            .register();

    // endregion


    // endregion

    private static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, RendererBlock::new, texture, () -> Blocks.IRON_BLOCK, () -> RenderType::cutoutMipped);
    }

    private static BlockEntry<Block> createGlassCasingBlock(String name, ResourceLocation texture, Supplier<Supplier<RenderType>> type) {
        return createCasingBlock(name, RendererGlassBlock::new, texture, () -> Blocks.GLASS, type);
    }

    private static BlockEntry<Block> createCasingBlock(String name, BiFunction<BlockBehaviour.Properties, IRenderer, ? extends RendererBlock> blockSupplier, ResourceLocation texture, NonNullSupplier<? extends Block> properties, Supplier<Supplier<RenderType>> type) {
        return REGISTRATE.block(name, p -> (Block) blockSupplier.apply(p,
                        Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                                Map.of("all", texture)) : null))
                .initialProperties(properties)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<Block> createBottomTopCasingBlock(String name, BiFunction<BlockBehaviour.Properties, IRenderer, ? extends RendererBlock> blockSupplier, ResourceLocation sideTexture, ResourceLocation topTexture, ResourceLocation bottomTexture, NonNullSupplier<? extends Block> properties, Supplier<Supplier<RenderType>> type) {
        return REGISTRATE.block(name, p -> (Block) blockSupplier.apply(p,
                        Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                                Map.of("side", sideTexture, "top", topTexture, "bottom", bottomTexture)) : null))
                .initialProperties(properties)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }
    public static void init() {

    }
}
