package com.ghostipedia.cosmiccore.common.data.worldgen;

import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.*;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.Set;

public class CosmicVeinOverrides {

    public enum VeinShape {
        BRANCHING,
        SHELL,
        CLUSTER, // Replaced LENS - multiple connected ore pockets
        STRINGER,
        FRACTURE
    }

    private static final Map<String, VeinShape> VEIN_SHAPE_OVERRIDES = new HashMap<>();
    private static final Map<String, VeinShape> MATERIAL_SHAPE_HINTS = new HashMap<>();

    // For testing: shapes in this set will use their custom generator
    // Shapes NOT in this set will use a "completed" generator (Branching) so GT veins don't show
    private static final Set<VeinShape> SHAPES_UNDER_TEST = Set.of(
            VeinShape.CLUSTER,
            VeinShape.FRACTURE);

    static {
        initMaterialHints();
        initSpecificOverrides();
    }

    private static void initMaterialHints() {
        MATERIAL_SHAPE_HINTS.put("iron", VeinShape.BRANCHING);
        MATERIAL_SHAPE_HINTS.put("magnetite", VeinShape.BRANCHING);
        MATERIAL_SHAPE_HINTS.put("hematite", VeinShape.BRANCHING);
        MATERIAL_SHAPE_HINTS.put("goethite", VeinShape.BRANCHING);

        MATERIAL_SHAPE_HINTS.put("copper", VeinShape.STRINGER);
        MATERIAL_SHAPE_HINTS.put("chalcopyrite", VeinShape.STRINGER);
        MATERIAL_SHAPE_HINTS.put("malachite", VeinShape.STRINGER);
        MATERIAL_SHAPE_HINTS.put("bornite", VeinShape.STRINGER);

        MATERIAL_SHAPE_HINTS.put("gold", VeinShape.FRACTURE);
        MATERIAL_SHAPE_HINTS.put("pyrite", VeinShape.FRACTURE);
        MATERIAL_SHAPE_HINTS.put("sphalerite", VeinShape.FRACTURE); // Zinc ore - more common
        MATERIAL_SHAPE_HINTS.put("zinc", VeinShape.FRACTURE);
        MATERIAL_SHAPE_HINTS.put("tetrahedrite", VeinShape.FRACTURE); // Copper-antimony ore

        MATERIAL_SHAPE_HINTS.put("diamond", VeinShape.SHELL);
        MATERIAL_SHAPE_HINTS.put("graphite", VeinShape.SHELL);
        MATERIAL_SHAPE_HINTS.put("kimberlite", VeinShape.SHELL);

        MATERIAL_SHAPE_HINTS.put("coal", VeinShape.CLUSTER);
        MATERIAL_SHAPE_HINTS.put("lignite", VeinShape.CLUSTER);

        MATERIAL_SHAPE_HINTS.put("salt", VeinShape.CLUSTER);
        MATERIAL_SHAPE_HINTS.put("rock_salt", VeinShape.CLUSTER);
        MATERIAL_SHAPE_HINTS.put("lepidolite", VeinShape.CLUSTER);

        MATERIAL_SHAPE_HINTS.put("bauxite", VeinShape.CLUSTER);
        MATERIAL_SHAPE_HINTS.put("aluminium", VeinShape.CLUSTER);

        MATERIAL_SHAPE_HINTS.put("sulfur", VeinShape.FRACTURE);
        MATERIAL_SHAPE_HINTS.put("cinnabar", VeinShape.FRACTURE);
        MATERIAL_SHAPE_HINTS.put("stibnite", VeinShape.FRACTURE); // Antimony ore
        MATERIAL_SHAPE_HINTS.put("realgar", VeinShape.FRACTURE); // Arsenic ore

        MATERIAL_SHAPE_HINTS.put("tin", VeinShape.STRINGER);
        MATERIAL_SHAPE_HINTS.put("cassiterite", VeinShape.STRINGER);

        MATERIAL_SHAPE_HINTS.put("lead", VeinShape.BRANCHING);
        MATERIAL_SHAPE_HINTS.put("galena", VeinShape.BRANCHING);
        MATERIAL_SHAPE_HINTS.put("silver", VeinShape.BRANCHING);

        MATERIAL_SHAPE_HINTS.put("nickel", VeinShape.SHELL);
        MATERIAL_SHAPE_HINTS.put("pentlandite", VeinShape.SHELL);
        MATERIAL_SHAPE_HINTS.put("garnierite", VeinShape.SHELL);

        MATERIAL_SHAPE_HINTS.put("redstone", VeinShape.FRACTURE);

        MATERIAL_SHAPE_HINTS.put("lapis", VeinShape.SHELL);
        MATERIAL_SHAPE_HINTS.put("lazurite", VeinShape.SHELL);
        MATERIAL_SHAPE_HINTS.put("sodalite", VeinShape.SHELL);

        MATERIAL_SHAPE_HINTS.put("uranium", VeinShape.FRACTURE);
        MATERIAL_SHAPE_HINTS.put("uraninite", VeinShape.FRACTURE);
        MATERIAL_SHAPE_HINTS.put("pitchblende", VeinShape.FRACTURE);

        MATERIAL_SHAPE_HINTS.put("olivine", VeinShape.SHELL);
        MATERIAL_SHAPE_HINTS.put("bentonite", VeinShape.CLUSTER);
        MATERIAL_SHAPE_HINTS.put("magnesite", VeinShape.CLUSTER);

        MATERIAL_SHAPE_HINTS.put("monazite", VeinShape.STRINGER);
        MATERIAL_SHAPE_HINTS.put("bastnasite", VeinShape.STRINGER);
    }

    private static void initSpecificOverrides() {
        VEIN_SHAPE_OVERRIDES.put("magnetite_vein_ow", VeinShape.BRANCHING);
        VEIN_SHAPE_OVERRIDES.put("cassiterite_vein", VeinShape.STRINGER);
        VEIN_SHAPE_OVERRIDES.put("galena_vein", VeinShape.BRANCHING);
        VEIN_SHAPE_OVERRIDES.put("salt_vein", VeinShape.CLUSTER);
        VEIN_SHAPE_OVERRIDES.put("coal_vein", VeinShape.CLUSTER);
        VEIN_SHAPE_OVERRIDES.put("diamond_vein", VeinShape.SHELL);
        VEIN_SHAPE_OVERRIDES.put("gold_vein", VeinShape.FRACTURE);
        VEIN_SHAPE_OVERRIDES.put("sphalerite_vein", VeinShape.FRACTURE); // Common zinc vein
        VEIN_SHAPE_OVERRIDES.put("tetrahedrite_vein", VeinShape.FRACTURE); // Common early game vein
        VEIN_SHAPE_OVERRIDES.put("pyrite_vein", VeinShape.FRACTURE); // Another common vein
        VEIN_SHAPE_OVERRIDES.put("lapis_vein", VeinShape.SHELL);
        VEIN_SHAPE_OVERRIDES.put("redstone_vein_ow", VeinShape.FRACTURE);
        VEIN_SHAPE_OVERRIDES.put("sulfur_vein", VeinShape.FRACTURE);
        VEIN_SHAPE_OVERRIDES.put("cinnabar_vein", VeinShape.FRACTURE); // Mercury ore
        VEIN_SHAPE_OVERRIDES.put("stibnite_vein", VeinShape.FRACTURE); // Antimony vein
        VEIN_SHAPE_OVERRIDES.put("bauxite_vein_ow", VeinShape.CLUSTER);
        VEIN_SHAPE_OVERRIDES.put("nickel_vein", VeinShape.SHELL);
        VEIN_SHAPE_OVERRIDES.put("copper_tin_vein", VeinShape.STRINGER);
        VEIN_SHAPE_OVERRIDES.put("chalcopyrite_vein", VeinShape.STRINGER);
        VEIN_SHAPE_OVERRIDES.put("iron_vein", VeinShape.BRANCHING);
        VEIN_SHAPE_OVERRIDES.put("olivine_vein", VeinShape.SHELL);
        VEIN_SHAPE_OVERRIDES.put("monazite_vein", VeinShape.STRINGER);
        VEIN_SHAPE_OVERRIDES.put("uranium_vein", VeinShape.FRACTURE);
    }

    public static void applyVeinOverrides() {
        var registry = GTRegistries.builtinRegistry().registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);
        for (var holder : registry.holders().toList()) {
            GTOreDefinition vein = holder.value();
            ResourceLocation id = holder.key().location();

            VeinShape shape = determineVeinShape(id, vein);
            if (shape != null) {
                VeinGenerator newGenerator = createGenerator(shape, vein);
                if (newGenerator != null) {
                    vein.veinGenerator(newGenerator);
                }
            }
        }
    }

    private static VeinShape determineVeinShape(ResourceLocation id, GTOreDefinition vein) {
        String path = id.getPath().toLowerCase(Locale.ROOT);

        if (VEIN_SHAPE_OVERRIDES.containsKey(path)) {
            return VEIN_SHAPE_OVERRIDES.get(path);
        }

        VeinGenerator generator = vein.veinGenerator();
        if (generator == null) return inferShapeFromVeinName(path);

        List<Material> materials = generator.getAllMaterials();
        if (materials == null) return inferShapeFromVeinName(path);

        for (Material material : materials) {
            if (material == null) continue;
            String matName = material.getName().toLowerCase(Locale.ROOT);
            if (MATERIAL_SHAPE_HINTS.containsKey(matName)) {
                return MATERIAL_SHAPE_HINTS.get(matName);
            }
        }

        return inferShapeFromVeinName(path);
    }

    private static VeinShape inferShapeFromVeinName(String path) {
        if (path.contains("iron") || path.contains("magnetite") || path.contains("hematite")) {
            return VeinShape.BRANCHING;
        }
        if (path.contains("copper") || path.contains("tin") || path.contains("cassiterite")) {
            return VeinShape.STRINGER;
        }
        if (path.contains("gold") || path.contains("uranium") || path.contains("pyrite") ||
                path.contains("sphalerite") || path.contains("tetrahedrite") || path.contains("zinc")) {
            return VeinShape.FRACTURE;
        }
        if (path.contains("diamond") || path.contains("lapis") || path.contains("nickel") || path.contains("olivine")) {
            return VeinShape.SHELL;
        }
        if (path.contains("coal") || path.contains("salt") || path.contains("bauxite")) {
            return VeinShape.CLUSTER;
        }
        if (path.contains("redstone") || path.contains("sulfur") || path.contains("cinnabar") ||
                path.contains("stibnite") || path.contains("realgar")) {
            return VeinShape.FRACTURE;
        }

        return null;
    }

    private static VeinGenerator createGenerator(VeinShape shape, GTOreDefinition vein) {
        VeinGenerator original = vein.veinGenerator();
        if (original == null) return null;

        List<Material> materials = original.getAllMaterials();
        if (materials == null || materials.isEmpty()) return null;

        Material primary = materials.get(0);
        Material secondary = materials.size() > 1 ? materials.get(1) : primary;
        Material rare = materials.size() > 2 ? materials.get(2) : null;

        // If this shape is under test, use its custom generator
        // Otherwise, use a completed generator (Branching) to prevent GT default veins
        if (!SHAPES_UNDER_TEST.contains(shape)) {
            // Use Branching for all "completed" vein types during testing
            return createBranchingGenerator(primary, secondary, rare);
        }

        return switch (shape) {
            case BRANCHING -> createBranchingGenerator(primary, secondary, rare);
            case STRINGER -> createStringerGenerator(primary, secondary, rare);
            case SHELL -> createShellGenerator(materials, rare);
            case CLUSTER -> createClusterGenerator(primary, secondary, rare);
            case FRACTURE -> createFractureGenerator(primary, secondary, rare);
        };
    }

    private static VeinGenerator createBranchingGenerator(Material primary, Material secondary, Material rare) {
        // Meandering river-like branches that snake and split recursively
        // 4 main branches, 0.15 thickness, 0.15 node size, 0.4 noise, 60% split chance
        var gen = new BranchingVeinGenerator(
                new ArrayList<>(), new ArrayList<>(),
                4, 0.15f, 0.15f, 0.4f, 0.6f, 0.08f);
        gen.oreBlock(primary, 3);
        gen.oreBlock(secondary, 2);
        if (rare != null) gen.rareBlock(rare, 1);
        return gen.build();
    }

    private static VeinGenerator createShellGenerator(List<Material> materials, Material rare) {
        // Shell veins with noisy boundaries and ore mixing for natural look
        // 0.3f inner ratio, 0.6f outer ratio, 0.25f noise intensity, 0.18f ore mixing
        var gen = new ShellVeinGenerator(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0.3f, 0.6f, 0.25f, 0.18f);

        if (materials.size() >= 3) {
            // Multiple ores in each shell for variety
            gen.coreBlock(materials.get(0), 3);
            gen.coreBlock(materials.get(1), 1); // Some inner material bleeds into core

            gen.innerBlock(materials.get(1), 3);
            gen.innerBlock(materials.get(0), 1); // Some core material in inner
            gen.innerBlock(materials.get(2), 1); // Some outer material in inner

            gen.outerBlock(materials.get(2), 3);
            gen.outerBlock(materials.get(1), 2); // Significant inner material in outer
            if (materials.size() > 3) {
                gen.outerBlock(materials.get(3), 1); // Additional variety if available
            }
        } else if (materials.size() == 2) {
            gen.coreBlock(materials.get(0), 3);
            gen.coreBlock(materials.get(1), 1);

            gen.innerBlock(materials.get(1), 2);
            gen.innerBlock(materials.get(0), 2);

            gen.outerBlock(materials.get(1), 3);
            gen.outerBlock(materials.get(0), 1);
        } else {
            gen.coreBlock(materials.get(0), 1);
            gen.innerBlock(materials.get(0), 1);
            gen.outerBlock(materials.get(0), 1);
        }

        return gen.build();
    }

    private static VeinGenerator createClusterGenerator(Material primary, Material secondary, Material rare) {
        // Large spread-out vein with distinct ore pockets connected by thin channels
        // 10 nodes, 6% node size, 2.5% channel thickness, 90% scatter for separation
        var gen = new ClusterVeinGenerator(
                new ArrayList<>(), new ArrayList<>(),
                10, 0.06f, 0.025f, 0.90f, 0.06f);
        gen.oreBlock(primary, 3);
        gen.oreBlock(secondary, 2);
        if (rare != null) gen.rareBlock(rare, 1);
        return gen.build();
    }

    private static VeinGenerator createStringerGenerator(Material primary, Material secondary, Material rare) {
        // Large blobby core with many thin tendrils - octopus/jellyfish-like
        // 20 stringers, 0.35 core (large!), 0.06 thickness (thin!), 0.7 noise (very blobby), 0.5 density
        var gen = new StringerVeinGenerator(
                new ArrayList<>(), new ArrayList<>(),
                20, 0.35f, 0.06f, 0.7f, 0.5f, 0.04f);
        gen.oreBlock(primary, 3);
        gen.oreBlock(secondary, 2);
        if (rare != null) gen.rareBlock(rare, 1);
        return gen.build();
    }

    private static VeinGenerator createFractureGenerator(Material primary, Material secondary, Material rare) {
        // "Shattered Geode" - hollow geode shell with crystal spikes pointing inward,
        // surrounded by radiating cracks extending outward like shattered glass
        // 12 block geode radius, 8% rare chance (higher in geode), 8 cracks, 6 block spikes
        var gen = new FractureVeinGenerator(
                new ArrayList<>(), new ArrayList<>(),
                12.0f, 0.08f, 8, 6.0f);
        gen.oreBlock(primary, 3);
        gen.oreBlock(secondary, 2);
        if (rare != null) gen.rareBlock(rare, 1);
        return gen.build();
    }

    public static void registerOverride(String veinPath, VeinShape shape) {
        VEIN_SHAPE_OVERRIDES.put(veinPath.toLowerCase(Locale.ROOT), shape);
    }

    public static void registerMaterialHint(String materialName, VeinShape shape) {
        MATERIAL_SHAPE_HINTS.put(materialName.toLowerCase(Locale.ROOT), shape);
    }
}
