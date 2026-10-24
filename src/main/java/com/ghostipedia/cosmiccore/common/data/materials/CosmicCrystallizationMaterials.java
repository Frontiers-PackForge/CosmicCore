package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialRegistryEntry;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DISABLE_DECOMPOSITION;

public final class CosmicCrystallizationMaterials {

    public static MaterialRegistryEntry CannonseedCrystallizationMedium;

    private static final Map<MaterialRegistryEntry, MaterialRegistryEntry> ORE_SLURRIES = new LinkedHashMap<>();
    private static final Map<MaterialRegistryEntry, MaterialRegistryEntry> GEM_GROWTH_SLURRIES = new LinkedHashMap<>();

    private CosmicCrystallizationMaterials() {}

    public static void register() {
        CannonseedCrystallizationMedium = processFluid("cannonseed_crystallization_medium", () -> 0x8D6A3F);

        for (MaterialRegistryEntry bundle : CosmicBundleMaterials.bundleEntries()) {
            ORE_SLURRIES.put(bundle,
                    processFluid(bundle.getName() + "_crystallization_slurry", bundle::getMaterialRGB));
        }

        for (MaterialRegistryEntry material : gemMaterials()) {
            GEM_GROWTH_SLURRIES.put(material,
                    processFluid(material.getName() + "_gem_growth_slurry", material::getMaterialRGB));
        }
    }

    private static MaterialRegistryEntry processFluid(String name, IntSupplier color) {
        return CosmicRegistration.REGISTRATE.material(name, builder -> builder
                .liquid()
                .color(color.getAsInt())
                .flags(DISABLE_DECOMPOSITION));
    }

    private static List<MaterialRegistryEntry> gemMaterials() {
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
        return ORE_SLURRIES.entrySet().stream().filter(entry -> entry.getKey().value() == material)
                .map(entry -> entry.getValue().value()).findFirst().orElse(null);
    }

    public static Map<Material, Material> gemGrowthSlurries() {
        Map<Material, Material> materials = new LinkedHashMap<>();
        GEM_GROWTH_SLURRIES.forEach((material, slurry) -> materials.put(material.value(), slurry.value()));
        return Collections.unmodifiableMap(materials);
    }
}
