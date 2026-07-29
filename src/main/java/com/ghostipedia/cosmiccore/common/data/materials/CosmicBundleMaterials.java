package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.Utherium;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DISABLE_MATERIAL_RECIPES;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.crushedRefined;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustImpure;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustPure;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustSmall;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustTiny;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class CosmicBundleMaterials {

    public static Material Ferosine;
    public static Material Cuprosiva;
    public static Material Galenite;
    public static Material Landisite;
    public static Material Redstona;
    public static Material Lazuric;
    public static Material Carbonic;
    public static Material EarthenSalts;
    public static Material Pyroltic;
    public static Material Quartizine;
    public static Material Molybite;
    public static Material Fahlorium;
    public static Material MonaziteSalts;
    public static Material Agarlite;
    public static Material CrudeRadionite;
    public static Material Vanachrome;
    public static Material Emberite;
    public static Material Utherite;
    public static Material Phycolite;

    private static final Map<Material, List<Material>> OUTPUTS = new LinkedHashMap<>();
    private static final Set<Material> OUTPUT_MATERIALS = new LinkedHashSet<>();
    private static final Map<Material, MaterialStack> HAND_SORT = new LinkedHashMap<>();

    public static void register() {
        Ferosine = bundle("ferosine", 0x8B5A2B, 0x5E3A1C, MaterialIconSet.ROUGH);
        Cuprosiva = bundle("cuprosiva", 0x2EA88A, 0x176E58, MaterialIconSet.METALLIC);
        Galenite = bundle("galenite", 0x6E6E78, 0x44444C, MaterialIconSet.DULL);
        Landisite = bundle("landisite", 0x5B7FB0, 0x37527A, MaterialIconSet.METALLIC);
        Redstona = bundle("redstona", 0xC23B3B, 0x7E1E1E, MaterialIconSet.ROUGH);
        Lazuric = bundle("lazuric", 0x2A52BE, 0x16306E, MaterialIconSet.DULL);
        Carbonic = bundle("carbonic", 0x2C2C30, 0x121214, MaterialIconSet.DULL);
        EarthenSalts = bundle("earthen_salts", 0xD9CBA8, 0xA89B78, MaterialIconSet.SAND);
        Pyroltic = bundle("pyroltic", 0xC9B037, 0x8A7615, MaterialIconSet.ROUGH);
        Quartizine = bundle("quartizine", 0x9FD8C8, 0x5FA090, MaterialIconSet.QUARTZ);
        Molybite = bundle("molybite", 0x4A5A6A, 0x2A3540, MaterialIconSet.METALLIC);
        Fahlorium = bundle("fahlorium", 0x7A5C3E, 0x4E3A26, MaterialIconSet.DULL);
        MonaziteSalts = bundle("monazite_salts", 0x9A6FB0, 0x603E78, MaterialIconSet.ROUGH);
        Agarlite = bundle("agarlite", 0xCFE0E8, 0x9FB4BE, MaterialIconSet.SHINY);
        CrudeRadionite = bundle("crude_radionite", 0x3FA63F, 0x1F6B1F, MaterialIconSet.RADIOACTIVE);
        Vanachrome = bundle("vanachrome", 0x6FA0A0, 0x3F6F6F, MaterialIconSet.METALLIC);
        Utherite = bundle("utherite", 0xB0525E, 0x5E2830, MaterialIconSet.ROUGH);
        Phycolite = bundle("phycolite", 0x9FB8E8, 0x54689E, MaterialIconSet.SHINY);

        Emberite = new Material.Builder(GTCEu.id("emberite"))
                .gem()
                .ore()
                .color(0xff7300).iconSet(MaterialIconSet.CERTUS)
                .buildAndRegister();

        out(Ferosine, Magnetite, Goethite, YellowLimonite, Hematite, VanadiumMagnetite, Gold);
        out(Cuprosiva, Chalcopyrite, Cassiterite, Malachite, Pyrite, Zeolite, Realgar);
        out(Galenite, Galena, Silver);
        out(Landisite, Garnierite, Pentlandite, Cobaltite);
        out(Redstona, Redstone, Pyrolusite, Grossular, Spessartine, Tantalite, Cinnabar);
        out(Lazuric, Lazurite, Sapphire, GarnetRed, Bauxite, Kyanite, Pollucite);
        out(Carbonic, Coal, Graphite, Diamond, Oilsands);
        out(EarthenSalts, RockSalt, Apatite, Lepidolite, Spodumene, Pyrochlore, Olivine);
        out(Pyroltic, Sulfur, Gold, Pyrite, Sphalerite, Goethite, Hematite);
        out(Quartizine, CertusQuartz, NetherQuartz, Quartzite, Barite, Beryllium, Emerald);
        out(Molybite, Molybdenite, Wulfenite, Powellite, Pyrolusite, Tantalite, Grossular);
        out(Fahlorium, Tetrahedrite, Stibnite, Topaz, Chalcocite, Bornite, Saltpeter);
        out(MonaziteSalts, Bastnasite, Monazite, Emberite);
        out(Agarlite, Cooperite, Bornite);
        out(CrudeRadionite, Pitchblende, Uraninite, Naquadah, Scheelite, Tungstate);
        out(Vanachrome, Magnetite, VanadiumMagnetite, Chromite, Bauxite, Ilmenite, Gold);
        out(Utherite, Utherium, CosmicMaterials.Aphotite, CosmicMaterials.Arcanite);
        out(Phycolite, CosmicMaterials.Gloomarcine, CosmicMaterials.Veilspar, CosmicMaterials.Bathyst,
                CosmicMaterials.Nyctophyte, CosmicMaterials.Hadalite, CosmicMaterials.Abyssbloom);

        sort(Cuprosiva, Tin, 3);
        sort(Ferosine, Gold, 2);
        sort(Galenite, Silver, 2);
        sort(Pyroltic, Sphalerite, 3);
    }

    private static Material bundle(String id, int color, int secondaryColor, MaterialIconSet iconSet) {
        return new Material.Builder(CosmicCore.id(id))
                .ore()
                .color(color).secondaryColor(secondaryColor)
                .iconSet(iconSet)
                .flags(DISABLE_MATERIAL_RECIPES)
                .ignoredTagPrefixes(dust, dustSmall, dustTiny, dustImpure, dustPure, crushedRefined)
                .buildAndRegister();
    }

    private static void out(Material bundleOre, Material... minerals) {
        OUTPUTS.put(bundleOre, List.of(minerals));
        Collections.addAll(OUTPUT_MATERIALS, minerals);
    }

    private static void sort(Material bundleOre, Material output, int tinyDusts) {
        HAND_SORT.put(bundleOre, new MaterialStack(output, tinyDusts));
    }

    @Nullable
    public static MaterialStack handSortOutput(Material bundleOre) {
        return HAND_SORT.get(bundleOre);
    }

    public static List<Material> outputsOf(Material bundleOre) {
        return OUTPUTS.get(bundleOre);
    }

    public static Set<Material> bundleOres() {
        return Collections.unmodifiableSet(OUTPUTS.keySet());
    }

    public static Set<Material> outputMaterials() {
        return Collections.unmodifiableSet(OUTPUT_MATERIALS);
    }

    public static boolean isBundleOre(Material material) {
        return OUTPUTS.containsKey(material);
    }

    public static boolean isBundleOutput(Material material) {
        return OUTPUT_MATERIALS.contains(material);
    }
}
