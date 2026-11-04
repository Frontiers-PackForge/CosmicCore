package com.ghostipedia.cosmiccore.client.aeroponics;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.block.AeroponicsCropHolderBlock.SeedlingPlants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SeedVisualRegistration extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "aeroponics_seedlings";
    public static final SeedVisualRegistration INSTANCE = new SeedVisualRegistration();

    private volatile Map<Item, Visual> overrides = Map.of();
    private volatile Map<Item, Optional<Visual>> defaults = new ConcurrentHashMap<>();

    private SeedVisualRegistration() {
        super(new GsonBuilder().create(), DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        var loaded = new HashMap<Item, Visual>();
        entries.forEach((itemId, json) -> {
            if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                CosmicCore.LOGGER.warn("Skipping seedling visual for missing item {}", itemId);
                return;
            }
            Visual.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(
                            message -> CosmicCore.LOGGER.error("Invalid seedling visual {}: {}", itemId, message))
                    .ifPresent(visual -> {
                        if (visual.model().isPresent()) {
                            var modelFile = FileToIdConverter.json("models").idToFile(visual.model().get());
                            if (resourceManager.getResource(modelFile).isEmpty()) {
                                CosmicCore.LOGGER.error("Missing seedling model {} for {}", modelFile, itemId);
                                return;
                            }
                        }
                        loaded.put(BuiltInRegistries.ITEM.get(itemId), visual);
                    });
        });
        overrides = Map.copyOf(loaded);
        defaults = new ConcurrentHashMap<>();
        CosmicCore.LOGGER.info("Loaded {} seedling visual overrides", overrides.size());
    }

    public @Nullable Visual resolve(ItemStack seed) {
        Visual override = overrides.get(seed.getItem());
        if (override != null) return override;
        return defaults.computeIfAbsent(seed.getItem(), item -> Optional.ofNullable(SeedlingPlants.defaultVisual(seed))
                .filter(state -> state.getRenderShape() == RenderShape.MODEL)
                .map(Visual::forState)).orElse(null);
    }

    public void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) return;
        defaults = new ConcurrentHashMap<>();
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().level != null) Minecraft.getInstance().levelRenderer.allChanged();
        });
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        var converter = FileToIdConverter.json("models");
        Minecraft.getInstance().getResourceManager()
                .listResources("models/" + DIRECTORY, id -> id.getPath().endsWith(".json"))
                .keySet().forEach(file -> event.register(ModelResourceLocation.standalone(converter.fileToId(file))));
    }

    public record Visual(Optional<BlockState> state, Optional<ResourceLocation> model, Optional<BlockState> upperState,
                         float scale, Vec3 offset) {

        public static final Codec<Visual> CODEC = RecordCodecBuilder.<Visual>create(instance -> instance
                .group(
                        BlockState.CODEC.optionalFieldOf("state").forGetter(Visual::state),
                        ResourceLocation.CODEC.optionalFieldOf("model").forGetter(Visual::model),
                        BlockState.CODEC.optionalFieldOf("upper_state").forGetter(Visual::upperState),
                        Codec.floatRange(0.0625F, 1.0F).optionalFieldOf("scale", 0.25F).forGetter(Visual::scale),
                        Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(Visual::offset))
                .apply(instance, Visual::new)).validate(visual -> {
                    if (visual.state.isPresent() == visual.model.isPresent()) {
                        return DataResult.error(() -> "Specify exactly one of state or model");
                    }
                    if (visual.upperState.isPresent() && visual.state.isEmpty()) {
                        return DataResult.error(() -> "An upper state requires a lower state");
                    }
                    if (visual.upperState.isPresent() &&
                            visual.upperState.get().getRenderShape() != RenderShape.MODEL) {
                        return DataResult.error(() -> "The upper seedling state must have a baked block model");
                    }
                    if (visual.state.isPresent() && visual.state.get().getRenderShape() != RenderShape.MODEL) {
                        return DataResult.error(() -> "The seedling state must have a baked block model");
                    }
                    if (visual.model.isPresent() && !visual.model.get().getPath().startsWith(DIRECTORY + "/")) {
                        return DataResult
                                .error(() -> "Custom seedling models must be inside models/" + DIRECTORY + "/");
                    }
                    if (!Float.isFinite(visual.scale) || !Double.isFinite(visual.offset.lengthSqr()) ||
                            Math.abs(visual.offset.x) > 1 || Math.abs(visual.offset.y) > 1 ||
                            Math.abs(visual.offset.z) > 1) {
                        return DataResult
                                .error(() -> "Seedling transforms must be finite, with offsets between -1 and 1");
                    }
                    return DataResult.success(visual);
                });

        public static Visual forState(BlockState state) {
            float scale = state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) ? 0.1875F : 0.25F;
            return new Visual(Optional.of(state), Optional.empty(), Optional.empty(), scale, Vec3.ZERO);
        }
    }
}
