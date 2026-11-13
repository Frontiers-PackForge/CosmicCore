package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.qualityfood.QualityFoodCompat;
import com.ghostipedia.cosmiccore.common.data.CosmicMutations;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class AeroponicsCrops extends SimpleJsonResourceReloadListener {

    public static final AeroponicsCrops INSTANCE = new AeroponicsCrops();
    private Catalog catalog = Catalog.EMPTY;
    private Sync sync = new Sync("");
    private static volatile Catalog clientCatalog = Catalog.EMPTY;
    int revision;

    private AeroponicsCrops() {
        super(new Gson(), "aeroponics");
    }

    public record Rates(double nutrient, double fertilizer, double duration, double yieldAmount) {

        static Rates read(JsonObject json) {
            return new Rates(json.get("nutrient").getAsDouble(), json.get("fertilizer").getAsDouble(),
                    json.get("duration").getAsDouble(), json.get("yieldAmount").getAsDouble());
        }

        public Rates {
            if (!Double.isFinite(nutrient) || !Double.isFinite(fertilizer) || !Double.isFinite(duration) ||
                    !Double.isFinite(yieldAmount) || nutrient < 0 || fertilizer < 0 || duration <= 0 ||
                    yieldAmount <= 0)
                throw new IllegalArgumentException("Invalid aeroponics rates");
        }

        Rates times(Rates other) {
            return new Rates(nutrient * other.nutrient, fertilizer * other.fertilizer,
                    duration * other.duration, yieldAmount * other.yieldAmount);
        }
    }

    private record Crop(Rates rates, Item primary, List<ItemStack> additional, Set<String> mutations) {}

    public record Cycle(Rates rates, List<ItemStack> harvest, List<Component> requirements) {

        public Cycle(Rates rates, List<ItemStack> harvest) {
            this(rates, harvest, List.of());
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
        var file = entries.get(CosmicCore.id("crops"));
        catalog = file == null ? Catalog.EMPTY : read(file.getAsJsonObject());
        sync = new Sync(file == null ? "" : file.toString());
        revision++;
    }

    public Sync sync() {
        return sync;
    }

    public static Catalog clientCatalog() {
        return clientCatalog;
    }

    public static void clearClientCatalog() {
        clientCatalog = Catalog.EMPTY;
    }

    public record Sync(String json) implements CustomPacketPayload {

        public static final Type<Sync> TYPE = new Type<>(CosmicCore.id("aeroponics_catalog"));
        public static final StreamCodec<ByteBuf, Sync> CODEC = ByteBufCodecs.stringUtf8(262144)
                .map(Sync::new, Sync::json);

        public void accept() {
            clientCatalog = json.isEmpty() ? Catalog.EMPTY : read(JsonParser.parseString(json).getAsJsonObject());
        }

        @Override
        public Type<Sync> type() {
            return TYPE;
        }
    }

    static Catalog read(JsonObject json) {
        var loaded = new HashMap<Item, Crop>();
        var quality = new HashMap<String, Rates>();
        var mutations = CosmicMutations.REGISTRY.keySet().stream().map(ResourceLocation::toString)
                .collect(Collectors.toUnmodifiableSet());
        var profiles = json.getAsJsonArray("profiles").asList().stream()
                .map(row -> Rates.read(row.getAsJsonObject())).toList();
        json.getAsJsonObject("quality").entrySet()
                .forEach(e -> quality.put(e.getKey(), Rates.read(e.getValue().getAsJsonObject())));
        if (!quality.keySet().containsAll(QualityFoodCompat.TIERS))
            throw new IllegalArgumentException("Missing aeroponics quality tiers");
        for (var entry : json.getAsJsonObject("plants").entrySet()) {
            var row = entry.getValue().getAsJsonObject();
            var seed = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(entry.getKey()));
            var primary = BuiltInRegistries.ITEM
                    .getOptional(ResourceLocation.parse(row.get("output").getAsString()));
            if (seed.isEmpty() || primary.isEmpty()) continue;
            var additional = new ArrayList<ItemStack>();
            if (row.has("additional")) {
                for (var element : row.getAsJsonArray("additional")) {
                    var extra = element.getAsJsonObject();
                    var item = BuiltInRegistries.ITEM
                            .getOptional(ResourceLocation.parse(extra.get("item").getAsString()));
                    if (item.isEmpty()) break;
                    int amount = extra.get("amount").getAsInt();
                    if (amount <= 0)
                        throw new IllegalArgumentException("Invalid additional yield for " + entry.getKey());
                    additional.add(new ItemStack(item.get(), amount));
                }
                if (additional.size() != row.getAsJsonArray("additional").size()) continue;
            }
            Set<String> applicable = row.has("mutations") ?
                    row.getAsJsonArray("mutations").asList().stream().map(JsonElement::getAsString)
                            .collect(Collectors.toUnmodifiableSet()) :
                    mutations;
            if (!mutations.containsAll(applicable))
                throw new IllegalArgumentException("Unknown mutation for " + entry.getKey());
            loaded.put(seed.get(), new Crop(profiles.get(row.get("profile").getAsInt()), primary.get(),
                    List.copyOf(additional), applicable));
        }
        return new Catalog(loaded, quality);
    }

    @Nullable
    Cycle resolve(ItemStack seed, AeroponicsTower machine) {
        return catalog.resolve(seed, machine);
    }

    public static final class Catalog {

        private static final Catalog EMPTY = new Catalog(Map.of(), Map.of());
        private final Map<Item, Crop> plants;
        private final Map<String, Rates> qualities;

        private Catalog(Map<Item, Crop> plants, Map<String, Rates> qualities) {
            this.plants = Map.copyOf(plants);
            this.qualities = Map.copyOf(qualities);
        }

        public Set<Item> seeds() {
            return plants.keySet();
        }

        public Set<String> applicable(Item seed) {
            return plants.get(seed).mutations;
        }

        public CosmicMutations.Mutation mutation(String id) {
            return CosmicMutations.REGISTRY.get(ResourceLocation.parse(id));
        }

        public ItemStack previewSeed(Item item, int tier, Collection<String> selected) {
            ItemStack stack = new ItemStack(item);
            QualityFoodCompat.setPreviewQuality(stack, tier);
            stack.set(CosmicMutations.SEED_MUTATIONS, selected.stream()
                    .filter(applicable(item)::contains).distinct().toList());
            return stack;
        }

        public @Nullable Cycle resolve(ItemStack seed) {
            return resolve(seed, null);
        }

        public @Nullable Cycle resolve(ItemStack seed, @Nullable AeroponicsTower machine) {
            Crop crop = plants.get(seed.getItem());
            if (crop == null) return null;
            Rates factors = qualities.get(QualityFoodCompat.TIERS.get(QualityFoodCompat.level(seed)));
            Rates rates = crop.rates.times(factors);
            var plan = new CosmicMutations.Plan(rates);
            plan.outputs.add(new CosmicMutations.Yield(new ItemStack(crop.primary), rates.yieldAmount()));
            for (var stack : crop.additional)
                plan.outputs.add(new CosmicMutations.Yield(stack, stack.getCount() * factors.yieldAmount()));
            var context = new CosmicMutations.Context(seed.copy(), crop.rates, machine);
            seed.getOrDefault(CosmicMutations.SEED_MUTATIONS, List.<String>of()).stream()
                    .filter(crop.mutations::contains).distinct().sorted()
                    .map(this::mutation).forEach(mutation -> mutation.apply(context, plan));
            var outputs = new ArrayList<ItemStack>();
            for (var output : plan.outputs) {
                int amount = Math.toIntExact(Math.round(output.amount()));
                if (amount > 0) outputs.add(output.stack().copyWithCount(amount));
            }
            for (var stack : outputs) QualityFoodCompat.copyQuality(seed, stack);
            return new Cycle(plan.rates, List.copyOf(outputs), List.copyOf(plan.requirements));
        }
    }
}
