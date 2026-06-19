package com.ghostipedia.cosmiccore.common.data.worldgen;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.*;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Explicit ore vein generator definitions for CosmicCore.
 * Each vein can be individually tuned with full control over generator parameters.
 *
 * Generator Types:
 * - BRANCHING: Meandering river-like branches that snake and split recursively
 * - CLUSTER: Distinct ore pockets connected by thin channels
 * - FRACTURE: "Shattered Geode" - hollow shell with inward spikes and outward cracks
 * - SHELL: Concentric layers with core/inner/outer materials
 * - STRINGER: Large blobby core with many thin tendrils (octopus-like)
 */
public class CosmicOreVeins {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmicOreVeins.class);

    private static final Map<String, Supplier<VeinGenerator>> VEIN_GENERATORS = new HashMap<>();

    // Track which veins we've registered to know which GT veins to disable
    private static final Set<String> REGISTERED_VEIN_IDS = new HashSet<>();

    public static void init() {
        // ============================================
        // OVERWORLD - STONE LAYER
        // ============================================

        // Coal - large scattered pockets
        register("coal_vein", () -> {
            var gen = new ClusterVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    12, 0.07f, 0.03f, 0.85f, 0.02f);
            gen.oreBlock(GTMaterials.Coal, 5);
            return gen.build();
        });

        // Iron - branching veins
        register("iron_vein", () -> {
            var gen = new BranchingVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    5, 0.12f, 0.18f, 0.35f, 0.55f, 0.06f);
            gen.oreBlock(GTMaterials.Goethite, 3);
            gen.oreBlock(GTMaterials.YellowLimonite, 2);
            gen.oreBlock(GTMaterials.Hematite, 2);
            gen.oreBlock(GTMaterials.Malachite, 1);
            return gen.build();
        });

        // Magnetite - branching with gold
        register("magnetite_vein_ow", () -> {
            var gen = new BranchingVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    4, 0.14f, 0.16f, 0.4f, 0.6f, 0.08f);
            gen.oreBlock(GTMaterials.Magnetite, 3);
            gen.oreBlock(GTMaterials.VanadiumMagnetite, 2);
            gen.rareBlock(GTMaterials.Gold, 1);
            return gen.build();
        });

        // Copper/Tin - stringer (octopus-like tendrils)
        register("copper_tin_vein", () -> {
            var gen = new StringerVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    18, 0.30f, 0.055f, 0.65f, 0.5f, 0.04f);
            gen.oreBlock(GTMaterials.Chalcopyrite, 3);
            gen.oreBlock(GTMaterials.Zeolite, 2);
            gen.oreBlock(GTMaterials.Cassiterite, 2);
            gen.rareBlock(GTMaterials.Realgar, 1);
            return gen.build();
        });

        // Cassiterite (Tin) - stringer
        register("cassiterite_vein", () -> {
            var gen = new StringerVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    15, 0.28f, 0.06f, 0.6f, 0.55f, 0.03f);
            gen.oreBlock(GTMaterials.Tin, 3);
            gen.oreBlock(GTMaterials.Cassiterite, 2);
            return gen.build();
        });

        // Galena (Lead/Silver) - branching
        register("galena_vein", () -> {
            var gen = new BranchingVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    4, 0.13f, 0.15f, 0.45f, 0.5f, 0.1f);
            gen.oreBlock(GTMaterials.Galena, 3);
            gen.oreBlock(GTMaterials.Silver, 2);
            gen.oreBlock(GTMaterials.Lead, 2);
            return gen.build();
        });

        // Salt - cluster pockets
        register("salts_vein", () -> {
            var gen = new ClusterVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    10, 0.065f, 0.025f, 0.9f, 0.05f);
            gen.oreBlock(GTMaterials.RockSalt, 3);
            gen.oreBlock(GTMaterials.Salt, 3);
            gen.oreBlock(GTMaterials.Lepidolite, 2);
            gen.rareBlock(GTMaterials.Spodumene, 1);
            return gen.build();
        });

        // Apatite - cluster
        register("apatite_vein", () -> {
            var gen = new ClusterVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    8, 0.06f, 0.025f, 0.85f, 0.06f);
            gen.oreBlock(GTMaterials.Apatite, 3);
            gen.oreBlock(GTMaterials.TricalciumPhosphate, 2);
            gen.rareBlock(GTMaterials.Pyrochlore, 1);
            return gen.build();
        });

        // Garnet - shell layers
        register("garnet_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.25f, 0.55f, 0.3f, 0.25f);
            gen.coreBlock(GTMaterials.GarnetRed, 3);
            gen.innerBlock(GTMaterials.GarnetYellow, 3);
            gen.outerBlock(GTMaterials.Amethyst, 2);
            gen.outerBlock(GTMaterials.Opal, 1);
            return gen.build();
        });

        // Garnet/Tin Sand - shell
        register("garnet_tin_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.25f, 0.6f, 0.3f, 0.15f);
            gen.coreBlock(GTMaterials.CassiteriteSand, 3);
            gen.innerBlock(GTMaterials.GarnetSand, 3);
            gen.outerBlock(GTMaterials.Asbestos, 2);
            gen.outerBlock(GTMaterials.Diatomite, 1);
            return gen.build();
        });

        // Lubricant materials - shell
        register("lubricant_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.3f, 0.6f, 0.2f, 0.2f);
            gen.coreBlock(GTMaterials.Soapstone, 3);
            gen.innerBlock(GTMaterials.Talc, 3);
            gen.outerBlock(GTMaterials.GlauconiteSand, 2);
            gen.outerBlock(GTMaterials.Pentlandite, 1);
            return gen.build();
        });

        // Mineral Sand - shell
        register("mineral_sand_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.25f, 0.55f, 0.3f, 0.25f);
            gen.coreBlock(GTMaterials.BasalticMineralSand, 3);
            gen.innerBlock(GTMaterials.GraniticMineralSand, 3);
            gen.outerBlock(GTMaterials.FullersEarth, 2);
            gen.outerBlock(GTMaterials.Gypsum, 1);
            return gen.build();
        });

        // Oilsands - cluster
        register("oilsands_vein", () -> {
            var gen = new ClusterVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    6, 0.08f, 0.04f, 0.7f, 0.0f);
            gen.oreBlock(GTMaterials.Oilsands, 5);
            return gen.build();
        });

        // ============================================
        // OVERWORLD - DEEPSLATE LAYER
        // ============================================

        // Copper - stringer
        register("copper_vein", () -> {
            var gen = new StringerVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    16, 0.32f, 0.055f, 0.6f, 0.5f, 0.05f);
            gen.oreBlock(GTMaterials.Chalcopyrite, 3);
            gen.oreBlock(GTMaterials.Iron, 2);
            gen.oreBlock(GTMaterials.Pyrite, 2);
            gen.oreBlock(GTMaterials.Copper, 1);
            return gen.build();
        });

        // Diamond - shell (valuable core)
        register("diamond_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.2f, 0.5f, 0.2f, 0.1f);
            gen.coreBlock(GTMaterials.Diamond, 2);
            gen.innerBlock(GTMaterials.Graphite, 3);
            gen.outerBlock(GTMaterials.Coal, 3);
            return gen.build();
        });

        // Lapis - shell
        register("lapis_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.25f, 0.55f, 0.25f, 0.15f);
            gen.coreBlock(GTMaterials.Lazurite, 3);
            gen.innerBlock(GTMaterials.Sodalite, 3);
            gen.innerBlock(GTMaterials.Lapis, 2);
            gen.outerBlock(GTMaterials.Calcite, 3);
            return gen.build();
        });

        // Redstone - fracture (shattered geode)
        register("redstone_vein_ow", () -> {
            var gen = new FractureVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    14.0f, 0.1f, 10, 7.0f);
            gen.oreBlock(GTMaterials.Redstone, 4);
            gen.oreBlock(GTMaterials.Chromite, 2);
            gen.rareBlock(GTMaterials.Cinnabar, 1);
            return gen.build();
        });

        // Sapphire - shell
        register("sapphire_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.25f, 0.55f, 0.2f, 0.12f);
            gen.coreBlock(GTMaterials.Almandine, 3);
            gen.innerBlock(GTMaterials.Pyrope, 3);
            gen.outerBlock(GTMaterials.Sapphire, 2);
            gen.outerBlock(GTMaterials.GreenSapphire, 1);
            return gen.build();
        });

        // Olivine - shell
        register("olivine_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.3f, 0.6f, 0.25f, 0.18f);
            gen.coreBlock(GTMaterials.Bentonite, 3);
            gen.innerBlock(GTMaterials.Magnesite, 3);
            gen.outerBlock(GTMaterials.Olivine, 2);
            gen.outerBlock(GTMaterials.GlauconiteSand, 1);
            return gen.build();
        });

        // Mica/Bauxite - cluster
        register("mica_vein", () -> {
            var gen = new ClusterVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    9, 0.055f, 0.025f, 0.85f, 0.08f);
            gen.oreBlock(GTMaterials.Kyanite, 3);
            gen.oreBlock(GTMaterials.Mica, 2);
            gen.oreBlock(GTMaterials.Bauxite, 2);
            gen.rareBlock(GTMaterials.Pollucite, 1);
            return gen.build();
        });

        // Manganese - branching
        register("manganese_vein_ow", () -> {
            var gen = new BranchingVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    4, 0.12f, 0.14f, 0.4f, 0.5f, 0.08f);
            gen.oreBlock(GTMaterials.Grossular, 3);
            gen.oreBlock(GTMaterials.Spessartine, 2);
            gen.oreBlock(GTMaterials.Pyrolusite, 2);
            gen.rareBlock(GTMaterials.Tantalite, 1);
            return gen.build();
        });

        // ============================================
        // NETHER
        // ============================================

        // Sulfur - fracture
        register("sulfur_vein", () -> {
            var gen = new FractureVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    12.0f, 0.08f, 9, 6.0f);
            gen.oreBlock(GTMaterials.Sulfur, 3);
            gen.oreBlock(GTMaterials.Pyrite, 2);
            gen.oreBlock(GTMaterials.Sphalerite, 2);
            return gen.build();
        });

        // Tetrahedrite - fracture
        register("tetrahedrite_vein", () -> {
            var gen = new FractureVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    11.0f, 0.1f, 8, 5.5f);
            gen.oreBlock(GTMaterials.Tetrahedrite, 3);
            gen.oreBlock(GTMaterials.Copper, 2);
            gen.rareBlock(GTMaterials.Stibnite, 1);
            return gen.build();
        });

        // Redstone (Nether) - fracture
        register("redstone_vein", () -> {
            var gen = new FractureVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    13.0f, 0.1f, 9, 6.5f);
            gen.oreBlock(GTMaterials.Redstone, 4);
            gen.oreBlock(GTMaterials.Chromite, 2);
            gen.rareBlock(GTMaterials.Cinnabar, 1);
            return gen.build();
        });

        // Banded Iron - branching
        register("banded_iron_vein", () -> {
            var gen = new BranchingVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    5, 0.14f, 0.18f, 0.35f, 0.55f, 0.1f);
            gen.oreBlock(GTMaterials.Goethite, 3);
            gen.oreBlock(GTMaterials.YellowLimonite, 2);
            gen.oreBlock(GTMaterials.Hematite, 2);
            gen.rareBlock(GTMaterials.Gold, 1);
            return gen.build();
        });

        // Beryllium - shell
        register("beryllium_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.25f, 0.55f, 0.2f, 0.15f);
            gen.coreBlock(GTMaterials.Beryllium, 3);
            gen.innerBlock(GTMaterials.Emerald, 2);
            gen.outerBlock(GTMaterials.Thorium, 2);
            return gen.build();
        });

        // Certus Quartz - cluster (GTCEu uses "certus_quartz" not "certus_quartz_vein")
        register("certus_quartz", () -> {
            var gen = new ClusterVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    8, 0.06f, 0.028f, 0.85f, 0.06f);
            gen.oreBlock(GTMaterials.Quartzite, 3);
            gen.oreBlock(GTMaterials.CertusQuartz, 2);
            gen.oreBlock(GTMaterials.Barite, 1);
            return gen.build();
        });

        // Manganese (Nether) - branching
        register("manganese_vein", () -> {
            var gen = new BranchingVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    4, 0.12f, 0.15f, 0.4f, 0.5f, 0.1f);
            gen.oreBlock(GTMaterials.Grossular, 3);
            gen.oreBlock(GTMaterials.Pyrolusite, 2);
            gen.rareBlock(GTMaterials.Tantalite, 1);
            return gen.build();
        });

        // Molybdenum - stringer
        register("molybdenum_vein", () -> {
            var gen = new StringerVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    14, 0.25f, 0.05f, 0.55f, 0.5f, 0.08f);
            gen.oreBlock(GTMaterials.Wulfenite, 3);
            gen.oreBlock(GTMaterials.Molybdenite, 2);
            gen.oreBlock(GTMaterials.Molybdenum, 2);
            gen.rareBlock(GTMaterials.Powellite, 1);
            return gen.build();
        });

        // Monazite - stringer
        register("monazite_vein", () -> {
            var gen = new StringerVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    12, 0.28f, 0.055f, 0.5f, 0.55f, 0.1f);
            gen.oreBlock(GTMaterials.Bastnasite, 3);
            gen.oreBlock(GTMaterials.Monazite, 2);
            gen.rareBlock(GTMaterials.Neodymium, 1);
            return gen.build();
        });

        // Nether Quartz - cluster
        register("nether_quartz_vein", () -> {
            var gen = new ClusterVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    10, 0.065f, 0.03f, 0.8f, 0.03f);
            gen.oreBlock(GTMaterials.NetherQuartz, 3);
            gen.oreBlock(GTMaterials.Quartzite, 2);
            return gen.build();
        });

        // Saltpeter - cluster
        register("saltpeter_vein", () -> {
            var gen = new ClusterVeinGenerator(new ArrayList<>(), new ArrayList<>(),
                    9, 0.055f, 0.025f, 0.85f, 0.06f);
            gen.oreBlock(GTMaterials.Saltpeter, 3);
            gen.oreBlock(GTMaterials.Diatomite, 2);
            gen.oreBlock(GTMaterials.Electrotine, 2);
            gen.rareBlock(GTMaterials.Alunite, 1);
            return gen.build();
        });

        // Topaz - shell
        register("topaz_vein", () -> {
            var gen = new ShellVeinGenerator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.25f, 0.55f, 0.2f, 0.15f);
            gen.coreBlock(GTMaterials.BlueTopaz, 2);
            gen.coreBlock(GTMaterials.Topaz, 2);
            gen.innerBlock(GTMaterials.Chalcocite, 3);
            gen.outerBlock(GTMaterials.Bornite, 3);
            return gen.build();
        });
    }

    // ============================================
    // REGISTRATION
    // ============================================

    private static void register(String veinId, Supplier<VeinGenerator> generatorSupplier) {
        String lowerId = veinId.toLowerCase();
        VEIN_GENERATORS.put(lowerId, generatorSupplier);
        REGISTERED_VEIN_IDS.add(lowerId);
    }

    public static void applyOverrides(RegistryAccess registryAccess) {
        int replaced = 0;
        int skipped = 0;

        Registry<GTOreDefinition> registry = registryAccess.registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);

        for (Holder.Reference<GTOreDefinition> holder : registry.holders().toList()) {
            ResourceLocation id = holder.key().location();
            if (!id.getNamespace().equals("gtceu")) continue;

            String path = id.getPath().toLowerCase();
            Supplier<VeinGenerator> generatorSupplier = VEIN_GENERATORS.get(path);

            if (generatorSupplier != null) {
                VeinGenerator generator = generatorSupplier.get();
                if (generator != null) {
                    holder.value().veinGenerator(generator);
                    replaced++;
                    LOGGER.debug("Replaced vein generator for: {}", id);
                }
            } else {
                // TODO(cosmiccore-42): GTCEu 8.0 turned ore veins into a frozen datapack registry;
                // there is no supported runtime removal (IMappedRegistryAccess#gtceu$remove is @TestOnly
                // and throws on frozen registries). Veins without a custom override can no longer be
                // removed here -- this needs a datapack/server-load hook redesign to suppress them.
                skipped++;
            }
        }

        LOGGER.info("CosmicOreVeins: Replaced {} vein generators, {} GT veins left intact (removal unsupported)",
                replaced, skipped);
    }

    public static boolean hasOverride(String veinId) {
        return VEIN_GENERATORS.containsKey(veinId.toLowerCase());
    }

    public static Set<String> getRegisteredVeinIds() {
        return Set.copyOf(REGISTERED_VEIN_IDS);
    }
}
