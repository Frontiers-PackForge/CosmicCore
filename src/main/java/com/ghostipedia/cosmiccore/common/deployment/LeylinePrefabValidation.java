package com.ghostipedia.cosmiccore.common.deployment;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public final class LeylinePrefabValidation {

    public static boolean valid(Level level, LeylinePrefab prefab) {
        return check(level, prefab).valid();
    }

    public record Result(boolean valid, List<Component> errors) {}

    public static Result check(Level level, LeylinePrefab prefab) {
        var world = new PrefabWorld(level, prefab);
        var definition = (MultiblockMachineDefinition) GTRegistries.MACHINES.get(prefab.machine());
        var machine = MetaMachine.getMachine(world, BlockPos.ZERO);
        if (!(machine instanceof MultiblockControllerMachine controller)) return new Result(false, List.of());
        var state = new PatternState();
        state.setController(controller, BlockPos.ZERO);
        boolean valid = definition.getStructurePatterns().get(MultiblockControllerMachine.DEFAULT_STRUCTURE).get()
                .checkPatternAt(world, state, BlockPos.ZERO, Direction.NORTH, Direction.UP, false) &&
                !state.hasErrors();
        var errors = new ArrayList<Component>();
        for (var error : state.getErrors().stream().limit(16).toList()) {
            if (error instanceof SinglePredicateError count) {
                var label = count.candidates.isEmpty() ? Component.translatable(count.debugName) :
                        count.candidates.getFirst().getItemStackForm().getHoverName();
                int limit = switch (count.type) {
                    case MIN_COUNT -> count.predMinCount;
                    case MAX_COUNT -> count.predMaxCount;
                    case MIN_LAYER_COUNT -> count.predMinLayerCount;
                    case MAX_LAYER_COUNT -> count.predMaxLayerCount;
                };
                errors.add(label.copy().append(": ").append(Component.translatable(
                        "gtceu.multiblock.pattern.error.limited." + count.type.getSerializedName(), limit,
                        count.actualCount)));
            } else {
                errors.add(Component.translatable("cosmiccore.leyline.invalid_at",
                        error.getPos() == null ? "?" : error.getPos().toShortString()));
                error.getCandidates().stream().flatMap(Collection::stream).limit(4)
                        .forEach(candidate -> errors.add(candidate.getItemStackForm().getHoverName()));
            }
        }
        return new Result(valid, List.copyOf(errors));
    }

    private static final class PrefabWorld extends Level {

        private final Level source;
        private final Map<BlockPos, BlockState> blocks = new HashMap<>();
        private final Map<BlockPos, BlockEntity> entities = new HashMap<>();
        private final ChunkSource chunks = new ChunkSource() {

            @Override
            public Level getLevel() {
                return PrefabWorld.this;
            }

            private final Map<Long, LevelChunk> cache = new HashMap<>();
            private final LevelLightEngine lighting = new LevelLightEngine(
                    this, false, false);

            @Override
            public ChunkAccess getChunk(int x, int z,
                                        ChunkStatus status,
                                        boolean load) {
                return cache.computeIfAbsent(ChunkPos.asLong(x, z),
                        key -> new LevelChunk(PrefabWorld.this,
                                new ChunkPos(x, z)) {

                            @Override
                            public BlockState getBlockState(BlockPos pos) {
                                return PrefabWorld.this.getBlockState(pos);
                            }

                            @Override
                            public BlockEntity getBlockEntity(BlockPos pos) {
                                return PrefabWorld.this.getBlockEntity(pos);
                            }

                            @Override
                            public BlockState setBlockState(BlockPos pos, BlockState state, boolean moving) {
                                return null;
                            }
                        });
            }

            @Override
            public boolean hasChunk(int x, int z) {
                return true;
            }

            @Override
            public void tick(BooleanSupplier time, boolean tick) {}

            @Override
            public String gatherStats() {
                return "leyline_validation";
            }

            @Override
            public int getLoadedChunksCount() {
                return cache.size();
            }

            @Override
            public LevelLightEngine getLightEngine() {
                return lighting;
            }
        };

        private PrefabWorld(Level level, LeylinePrefab prefab) {
            super((WritableLevelData) level.getLevelData(), level.dimension(),
                    level.registryAccess(), level.dimensionTypeRegistration(), level::getProfiler, true, false, 0, 0);
            source = level;
            prefab.blocks().forEach(p -> blocks.put(p.relativeOffset(), p.state()));
            blocks.forEach((pos, state) -> {
                if (state.getBlock() instanceof EntityBlock block) {
                    var entity = block.newBlockEntity(pos, state);
                    if (entity != null) {
                        entity.setLevel(this);
                        entities.put(pos, entity);
                    }
                }
            });
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return entities.get(pos);
        }

        @Override
        public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion) {
            return false;
        }

        @Override
        public void setBlockEntity(BlockEntity entity) {}

        @Override
        public void removeBlockEntity(BlockPos pos) {}

        @Override
        public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

        @Override
        public void playSeededSound(Player player, double x, double y, double z,
                                    Holder<SoundEvent> sound,
                                    SoundSource category,
                                    float volume, float pitch, long seed) {}

        @Override
        public void playSeededSound(Player player,
                                    Entity entity,
                                    Holder<SoundEvent> sound,
                                    SoundSource category,
                                    float volume, float pitch, long seed) {}

        @Override
        public String gatherChunkSourceStats() {
            return "leyline_validation";
        }

        @Override
        public Entity getEntity(int id) {
            return null;
        }

        @Override
        public TickRateManager tickRateManager() {
            return source.tickRateManager();
        }

        @Override
        public MapItemSavedData getMapData(MapId id) {
            return null;
        }

        @Override
        public void setMapData(MapId id,
                               MapItemSavedData data) {}

        @Override
        public MapId getFreeMapId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void destroyBlockProgress(int id, BlockPos pos, int progress) {}

        @Override
        public Scoreboard getScoreboard() {
            return new Scoreboard();
        }

        @Override
        public RecipeManager getRecipeManager() {
            return source.getRecipeManager();
        }

        @Override
        protected LevelEntityGetter<Entity> getEntities() {
            return new LevelEntityGetterAdapter<>(
                    new EntityLookup<>(),
                    new EntitySectionStorage<>(Entity.class,
                            key -> Visibility.HIDDEN));
        }

        @Override
        public PotionBrewing potionBrewing() {
            return source.potionBrewing();
        }

        @Override
        public void setDayTimeFraction(float value) {}

        @Override
        public float getDayTimeFraction() {
            return 0;
        }

        @Override
        public float getDayTimePerTick() {
            return 0;
        }

        @Override
        public void setDayTimePerTick(float value) {}

        @Override
        public ChunkSource getChunkSource() {
            return chunks;
        }

        @Override
        public FeatureFlagSet enabledFeatures() {
            return source.enabledFeatures();
        }

        @Override
        public LevelTickAccess<Block> getBlockTicks() {
            return BlackholeTickAccess.emptyLevelList();
        }

        @Override
        public LevelTickAccess<Fluid> getFluidTicks() {
            return BlackholeTickAccess.emptyLevelList();
        }

        @Override
        public List<Player> players() {
            return List.of();
        }

        @Override
        public Holder<Biome> getUncachedNoiseBiome(int x, int y,
                                                   int z) {
            return registryAccess().registryOrThrow(Registries.BIOME)
                    .getHolderOrThrow(Biomes.PLAINS);
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver resolver) {
            return -1;
        }

        @Override
        public float getShade(Direction direction, boolean shade) {
            return 1;
        }

        @Override
        public void levelEvent(Player player, int event, BlockPos pos, int data) {}

        @Override
        public void gameEvent(Holder<GameEvent> event,
                              Vec3 pos,
                              GameEvent.Context context) {}
    }
}
