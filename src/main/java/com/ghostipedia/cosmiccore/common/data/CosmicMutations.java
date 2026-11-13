package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.AeroponicsCrops;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.AeroponicsTower;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class CosmicMutations {

    public static final ResourceKey<Registry<Mutation>> KEY = ResourceKey.createRegistryKey(CosmicCore.id("mutation"));
    public static final DeferredRegister<Mutation> MUTATIONS = DeferredRegister.create(KEY, CosmicCore.MOD_ID);
    public static final Registry<Mutation> REGISTRY = MUTATIONS.makeRegistry(builder -> builder.sync(true));
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(
            Registries.DATA_COMPONENT_TYPE, CosmicCore.MOD_ID);
    public static final DataComponentType<List<String>> SEED_MUTATIONS = DataComponentType.<List<String>>builder()
            .persistent(ResourceLocation.CODEC.xmap(ResourceLocation::toString, ResourceLocation::parse).listOf())
            .build();

    public static final Mutation HIGH_YIELD = register("high_yield", new RateModifier(1.3, 1.2, 1.1, 2));
    public static final Mutation EFFICIENT = register("efficient", new RateModifier(0.8, 0.8, 1, 1));
    public static final Mutation FAST = register("fast", new RateModifier(1.15, 1.1, 0.75, 1));

    static {
        COMPONENTS.register("mutations", () -> SEED_MUTATIONS);
    }

    public static void init(IEventBus bus) {
        MUTATIONS.register(bus);
        COMPONENTS.register(bus);
    }

    private static Mutation register(String name, Trait... traits) {
        var mutation = new Mutation(CosmicCore.id(name), List.of(traits));
        MUTATIONS.register(name, () -> mutation);
        return mutation;
    }

    public record Context(ItemStack seed, AeroponicsCrops.Rates crop, @Nullable AeroponicsTower machine) {

        public boolean preview() {
            return machine == null;
        }
    }

    public record Yield(ItemStack stack, double amount) {

        public Yield {
            if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid mutation yield");
            stack = stack.copy();
        }
    }

    public static final class Plan {

        public AeroponicsCrops.Rates rates;
        public final List<Yield> outputs = new ArrayList<>();
        public final List<Component> requirements = new ArrayList<>();

        public Plan(AeroponicsCrops.Rates rates) {
            this.rates = rates;
        }
    }

    public interface Trait {

        void apply(Context context, Plan plan);

        default Component description() {
            return Component.empty();
        }

        default boolean requiresRefresh() {
            return false;
        }
    }

    public record RateModifier(double nutrient, double fertilizer, double duration, double yield) implements Trait {

        public RateModifier {
            new AeroponicsCrops.Rates(nutrient, fertilizer, duration, yield);
        }

        @Override
        public void apply(Context context, Plan plan) {
            var r = plan.rates;
            plan.rates = new AeroponicsCrops.Rates(r.nutrient() * nutrient, r.fertilizer() * fertilizer,
                    r.duration() * duration, r.yieldAmount() * yield);
            plan.outputs.replaceAll(output -> new Yield(output.stack(), output.amount() * yield));
        }

        @Override
        public Component description() {
            return Component.translatable("cosmiccore.aeroponics.emi.effects", nutrient, fertilizer, duration, yield);
        }
    }

    public record Mutation(ResourceLocation id, List<Trait> traits) {

        public Mutation {
            traits = List.copyOf(traits);
        }

        public void apply(Context context, Plan plan) {
            for (var trait : traits) trait.apply(context, plan);
        }

        public Component description() {
            var text = Component.translatable("mutation." + id.getNamespace() + "." + id.getPath() + ".effect");
            for (var trait : traits) {
                var detail = trait.description();
                if (!detail.getString().isEmpty()) text.append("\n").append(detail);
            }
            return text;
        }
    }
}
