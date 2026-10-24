package com.ghostipedia.cosmiccore.common.data.worldgen;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.*;
import com.ghostipedia.cosmiccore.common.murkbloom.MurkbloomServerLogic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.integration.kjs.builders.worldgen.OreVeinDefinitionBuilderJS;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CosmicOreVeins {

    private static final Map<String, Material> VEINS = new LinkedHashMap<>();
    private static final Map<Material, Shape> SHAPES = new LinkedHashMap<>();
    private static final Map<String, Material> NAME_TO_MONO = new LinkedHashMap<>();
    private static final Map<String, String> PRIMARY_TO_BUNDLE = new LinkedHashMap<>();
    private static final Map<String, Integer> COUNT = new LinkedHashMap<>();
    private static final Map<String, GTOreDefinition> SNAPSHOTS = new LinkedHashMap<>();
    private static final Map<String, int[]> HOLLOW_BANDS = new LinkedHashMap<>();

    private static final int FIELD_POCKET_CLUSTER_SIZE = 10;
    private static final float HOLLOW_DENSITY = 0.5f;

    private enum Shape {
        BRANCHING,
        CLUSTER,
        STRINGER,
        FRACTURE
    }

    public static void init() {
        VEINS.clear();
        SHAPES.clear();
        NAME_TO_MONO.clear();
        PRIMARY_TO_BUNDLE.clear();
        COUNT.clear();
        HOLLOW_BANDS.clear();

        VEINS.put("iron", CosmicBundleMaterials.Ferosine.value());
        VEINS.put("magnetite", CosmicBundleMaterials.Ferosine.value());
        VEINS.put("copper", CosmicBundleMaterials.Cuprosiva.value());
        VEINS.put("copper_tin", CosmicBundleMaterials.Cuprosiva.value());
        VEINS.put("cassiterite", CosmicBundleMaterials.Cuprosiva.value());
        VEINS.put("galena", CosmicBundleMaterials.Galenite.value());
        VEINS.put("nickel", CosmicBundleMaterials.Landisite.value());
        VEINS.put("lubricant", CosmicBundleMaterials.Landisite.value());
        VEINS.put("redstone", CosmicBundleMaterials.Redstona.value());
        VEINS.put("manganese", CosmicBundleMaterials.Redstona.value());
        VEINS.put("lapis", CosmicBundleMaterials.Lazuric.value());
        VEINS.put("sapphire", CosmicBundleMaterials.Lazuric.value());
        VEINS.put("garnet", CosmicBundleMaterials.Lazuric.value());
        VEINS.put("mica", CosmicBundleMaterials.Lazuric.value());
        VEINS.put("coal", CosmicBundleMaterials.Carbonic.value());
        VEINS.put("diamond", CosmicBundleMaterials.Carbonic.value());
        VEINS.put("oilsands", CosmicBundleMaterials.Carbonic.value());
        VEINS.put("salts", CosmicBundleMaterials.EarthenSalts.value());
        VEINS.put("apatite", CosmicBundleMaterials.EarthenSalts.value());
        VEINS.put("mineral_sand", CosmicBundleMaterials.EarthenSalts.value());
        VEINS.put("garnet_tin", CosmicBundleMaterials.EarthenSalts.value());
        VEINS.put("olivine", CosmicBundleMaterials.EarthenSalts.value());

        VEINS.put("sulfur", CosmicBundleMaterials.Pyroltic.value());
        VEINS.put("banded_iron", CosmicBundleMaterials.Pyroltic.value());
        VEINS.put("certus_quartz", CosmicBundleMaterials.Quartizine.value());
        VEINS.put("nether_quartz", CosmicBundleMaterials.Quartizine.value());
        VEINS.put("beryllium", CosmicBundleMaterials.Quartizine.value());
        VEINS.put("molybdenum", CosmicBundleMaterials.Molybite.value());
        VEINS.put("nether_manganese", CosmicBundleMaterials.Molybite.value());
        VEINS.put("tetrahedrite", CosmicBundleMaterials.Fahlorium.value());
        VEINS.put("topaz", CosmicBundleMaterials.Fahlorium.value());
        VEINS.put("nether_redstone", CosmicBundleMaterials.Fahlorium.value());
        VEINS.put("saltpeter", CosmicBundleMaterials.Fahlorium.value());
        VEINS.put("monazite", CosmicBundleMaterials.MonaziteSalts.value());

        VEINS.put("sheldonite", CosmicBundleMaterials.Agarlite.value());
        VEINS.put("pitchblende", CosmicBundleMaterials.CrudeRadionite.value());
        VEINS.put("naquadah", CosmicBundleMaterials.CrudeRadionite.value());
        VEINS.put("scheelite", CosmicBundleMaterials.CrudeRadionite.value());
        VEINS.put("end_magnetite", CosmicBundleMaterials.Vanachrome.value());
        VEINS.put("end_bauxite", CosmicBundleMaterials.Vanachrome.value());

        shape(CosmicBundleMaterials.Ferosine.value(), Shape.BRANCHING);
        shape(CosmicBundleMaterials.Cuprosiva.value(), Shape.STRINGER);
        shape(CosmicBundleMaterials.Galenite.value(), Shape.BRANCHING);
        shape(CosmicBundleMaterials.Landisite.value(), Shape.BRANCHING);
        shape(CosmicBundleMaterials.Redstona.value(), Shape.FRACTURE);
        shape(CosmicBundleMaterials.Lazuric.value(), Shape.CLUSTER);
        shape(CosmicBundleMaterials.Carbonic.value(), Shape.CLUSTER);
        shape(CosmicBundleMaterials.EarthenSalts.value(), Shape.CLUSTER);
        shape(CosmicBundleMaterials.Pyroltic.value(), Shape.FRACTURE);
        shape(CosmicBundleMaterials.Quartizine.value(), Shape.CLUSTER);
        shape(CosmicBundleMaterials.Molybite.value(), Shape.STRINGER);
        shape(CosmicBundleMaterials.Fahlorium.value(), Shape.FRACTURE);
        shape(CosmicBundleMaterials.MonaziteSalts.value(), Shape.STRINGER);
        shape(CosmicBundleMaterials.CrudeRadionite.value(), Shape.FRACTURE);

        hollow(CosmicBundleMaterials.Utherite.value(), Shape.BRANCHING, -190, -70);
        hollow(CosmicBundleMaterials.Vanachrome.value(), Shape.BRANCHING, -390, -210);
        hollow(CosmicBundleMaterials.Phycolite.value(), Shape.CLUSTER, -590, -410);
        hollow(CosmicBundleMaterials.Agarlite.value(), Shape.STRINGER, -790, -610);

        Set<String> seen = new HashSet<>();
        for (Map.Entry<String, Material> entry : VEINS.entrySet()) {
            Material mono = entry.getValue();
            String name = mono.getName();
            NAME_TO_MONO.putIfAbsent(name, mono);
            COUNT.merge(name, 1, Integer::sum);
            if (seen.add(name)) {
                PRIMARY_TO_BUNDLE.put(entry.getKey(), name);
            }
        }
    }

    private static void shape(Material mono, Shape shape) {
        SHAPES.put(mono, shape);
    }

    private static void hollow(Material mono, Shape shape, int minY, int maxY) {
        SHAPES.put(mono, shape);
        NAME_TO_MONO.putIfAbsent(mono.getName(), mono);
        HOLLOW_BANDS.put(mono.getName(), new int[] { minY, maxY });
    }

    public static void beginRebuild() {
        init();
        SNAPSHOTS.clear();
    }

    public static void capture(ResourceLocation id, GTOreDefinition vein) {
        if (!id.getNamespace().equals("gtceu")) return;
        String bundle = PRIMARY_TO_BUNDLE.get(id.getPath());
        if (bundle == null) return;
        SNAPSHOTS.put(bundle, new GTOreDefinition(vein));
    }

    public static List<String> capturedBundles() {
        Set<String> all = new LinkedHashSet<>(SNAPSHOTS.keySet());
        all.addAll(HOLLOW_BANDS.keySet());
        return List.copyOf(all);
    }

    public static void applyCaptured(OreVeinDefinitionBuilderJS dest, String bundle) {
        if (HOLLOW_BANDS.containsKey(bundle)) {
            applyHollow(dest, bundle);
            return;
        }
        GTOreDefinition src = snapshotFor(bundle);
        if (src == null) return;
        copyInto(dest, src, bundle);
    }

    private static GTOreDefinition snapshotFor(String bundle) {
        if (bundle.equals(CosmicBundleMaterials.CrudeRadionite.getName())) {
            GTOreDefinition netherProfile = SNAPSHOTS.get(CosmicBundleMaterials.Fahlorium.getName());
            if (netherProfile != null) return netherProfile;
        }
        return SNAPSHOTS.get(bundle);
    }

    private static void applyHollow(OreVeinDefinitionBuilderJS dest, String bundle) {
        Material mono = NAME_TO_MONO.get(bundle);
        int[] band = HOLLOW_BANDS.get(bundle);
        if (mono == null || band == null) return;

        dest.clusterSize(ConstantInt.of(FIELD_POCKET_CLUSTER_SIZE));
        dest.density(HOLLOW_DENSITY);
        dest.weight(0);
        dest.layer(CosmicWorldGenLayers.hollow());
        dest.dimensions(Set.of(MurkbloomServerLogic.HOLLOW_DIM));
        dest.heightRangeUniform(band[0], band[1]);
        dest.discardChanceOnAirExposure(0f);
        dest.veinGenerator(pocket(mono));
    }

    public static boolean shouldRemove(ResourceLocation id) {
        return id.getNamespace().equals("gtceu") && VEINS.containsKey(id.getPath());
    }

    private static void copyInto(OreVeinDefinitionBuilderJS dest, GTOreDefinition src, String bundle) {
        Material mono = NAME_TO_MONO.get(bundle);
        if (mono == null) return;

        IWorldGenLayer layer = src.layer();
        if (layer == WorldGenLayers.STONE.value() || layer == WorldGenLayers.DEEPSLATE.value()) {
            layer = CosmicWorldGenLayers.overworld();
        }

        dest.clusterSize(src.clusterSize());
        dest.density(src.density() * 0.8f);
        dest.weight(0);
        dest.layer(layer);
        dest.dimensions(src.dimensionFilter());
        dest.heightRange(src.heightRange());
        dest.discardChanceOnAirExposure(src.discardChanceOnAirExposure());
        dest.biomes(src.biomes());
        dest.biomeWeightModifier(src.biomeWeightModifier());
        dest.veinGenerator(pocket(mono));

        dest.clusterSize(ConstantInt.of(FIELD_POCKET_CLUSTER_SIZE));
    }

    private static VeinGenerator buildGenerator(Shape shape, Material mono) {
        return switch (shape) {
            case BRANCHING -> branching(mono);
            case CLUSTER -> cluster(mono);
            case STRINGER -> stringer(mono);
            case FRACTURE -> fracture(mono);
        };
    }

    private static VeinGenerator branching(Material mono) {
        var gen = new BranchingVeinGenerator(new ArrayList<>(), new ArrayList<>(), 4, 0.15f, 0.15f, 0.4f, 0.6f, 0.08f);
        gen.oreBlock(mono, 1);
        return gen.build();
    }

    private static VeinGenerator cluster(Material mono) {
        var gen = new ClusterVeinGenerator(new ArrayList<>(), new ArrayList<>(), 10, 0.06f, 0.025f, 0.90f, 0.06f);
        gen.oreBlock(mono, 1);
        return gen.build();
    }

    private static VeinGenerator stringer(Material mono) {
        var gen = new StringerVeinGenerator(new ArrayList<>(), new ArrayList<>(), 20, 0.35f, 0.06f, 0.7f, 0.5f, 0.04f);
        gen.oreBlock(mono, 1);
        return gen.build();
    }

    private static VeinGenerator fracture(Material mono) {
        var gen = new FractureVeinGenerator(new ArrayList<>(), new ArrayList<>(), 21.0f, 0.08f, 8, 10.5f);
        gen.oreBlock(mono, 1);
        return gen.build();
    }

    private static VeinGenerator pocket(Material mono) {
        var gen = new PocketVeinGenerator(new ArrayList<>(), 0.3f, 0.55f);
        gen.oreBlock(mono, 1);
        return gen.build();
    }

    public static boolean hasOverride(String veinId) {
        return VEINS.containsKey(veinId.toLowerCase(Locale.ROOT));
    }

    public static Set<String> getRegisteredVeinIds() {
        return Set.copyOf(VEINS.keySet());
    }
}
