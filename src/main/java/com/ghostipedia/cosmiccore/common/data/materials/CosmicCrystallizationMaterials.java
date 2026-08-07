package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DISABLE_DECOMPOSITION;

public final class CosmicCrystallizationMaterials {

    public static Material CannonseedCrystallizationMedium;

    private static final Map<Material, Material> ORE_SLURRIES = new LinkedHashMap<>();
    private static final Map<Material, Material> GEM_GROWTH_SLURRIES = new LinkedHashMap<>();

    private CosmicCrystallizationMaterials() {}

    public static void register() {
        CannonseedCrystallizationMedium = processFluid("cannonseed_crystallization_medium", 0x8D6A3F);

        for (Material bundle : CosmicBundleMaterials.bundleOres()) {
            ORE_SLURRIES.put(bundle,
                    processFluid(bundle.getName() + "_crystallization_slurry", bundle.getMaterialRGB()));
        }

        for (Material material : gemMaterials()) {
            GEM_GROWTH_SLURRIES.put(material,
                    processFluid(material.getName() + "_gem_growth_slurry", material.getMaterialRGB()));
        }
    }

    private static Material processFluid(String name, int color) {
        return new Material.Builder(CosmicCore.id(name))
                .liquid()
                .color(color)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();
    }

    private static List<Material> gemMaterials() {
        return List.of(
                GTMaterials.Almandine,
                GTMaterials.Amethyst,
                GTMaterials.Andradite,
                GTMaterials.Apatite,
                GTMaterials.BlueTopaz,
                GTMaterials.CertusQuartz,
                GTMaterials.Cinnabar,
                GTMaterials.Diamond,
                GTMaterials.Emerald,
                GTMaterials.GreenSapphire,
                GTMaterials.Grossular,
                GTMaterials.Lapis,
                GTMaterials.Lazurite,
                GTMaterials.Malachite,
                GTMaterials.Monazite,
                GTMaterials.NetherQuartz,
                GTMaterials.Olivine,
                GTMaterials.Opal,
                GTMaterials.Pyrope,
                GTMaterials.Quartzite,
                GTMaterials.Realgar,
                GTMaterials.GarnetRed,
                GTMaterials.RockSalt,
                GTMaterials.Ruby,
                GTMaterials.Rutile,
                GTMaterials.Salt,
                GTMaterials.Sapphire,
                GTMaterials.Sodalite,
                GTMaterials.Spessartine,
                GTMaterials.Topaz,
                GTMaterials.Uvarovite,
                GTMaterials.GarnetYellow,
                CosmicBundleMaterials.Emberite,
                CosmicMaterials.Utherium,
                CosmicMaterials.Arcanite,
                CosmicMaterials.Veilspar,
                CosmicMaterials.Hadalite);
    }

    @Nullable
    public static Material oreSlurry(Material material) {
        return ORE_SLURRIES.get(material);
    }

    public static Map<Material, Material> gemGrowthSlurries() {
        return Collections.unmodifiableMap(GEM_GROWTH_SLURRIES);
    }
}
