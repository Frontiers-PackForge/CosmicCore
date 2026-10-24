package com.ghostipedia.cosmiccore.api.data.material.property;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.item.component.HaloItemRenderData;
import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet;
import com.ghostipedia.cosmiccore.utils.ColorUtil;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@Getter
public class CCoreMaterialIconSet extends MaterialIconSet {

    private final ICustomRenderer customRender;

    public CCoreMaterialIconSet(String name, Holder<MaterialIconSet> parentIconset, boolean root,
                                ICustomRenderer renderer) {
        super(GTCEu.id(name), parentIconset, root);
        this.customRender = renderer;
    }

    private static Holder<MaterialIconSet> register(String name, Holder<MaterialIconSet> parentIconset, boolean root,
                                                    ICustomRenderer renderer) {
        return REGISTRATE.simple(name, GTRegistries.Keys.MATERIAL_ICON_SET,
                () -> new CCoreMaterialIconSet(name, parentIconset, root, renderer));
    }

    public static void init() {}

    static final Supplier<Integer> prismaticColor = () -> {
        float v = (float) ((System.currentTimeMillis() / 500) % 10) / 10;
        if (v > 0.5f)
            v = 1 - v;
        return (0xff << 24) | ColorUtil.lerpColorRGB(0xffc0cb, 0x000080, v * 2);
    };

    public static final Holder<MaterialIconSet> VIBRANIUM = register("vibranium",
            CosmicMaterialSet.NEUTRONITE, false,
            new HaloItemRenderData(4, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/shifting_halo_cyan"), true,
                    false));
    public static final Holder<MaterialIconSet> VIBRANIUM_NEUTRONIUM = register("vibranium_neutronium",
            CosmicMaterialSet.NEUTRONIUM_CCORE, false,
            new HaloItemRenderData(3, 0xFFFFFFFF, ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID,
                    "block/iris/rnd/compression_halo_neutronium_faded"), true,
                    false));

    public static final Holder<MaterialIconSet> PRISMATIC = register("prismatic", SHINY, false,
            new HaloItemRenderData(8, 0xFF1c1926,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/storm_halo"),
                    true,
                    false));

    public static final Holder<MaterialIconSet> CHRONIC = register("chronic", CosmicMaterialSet.CHRONON,
            false,
            new HaloItemRenderData(8, 0xFF1c1926,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/time_halo"),
                    true,
                    false));

    public static final Holder<MaterialIconSet> VOIDSPARKICO = register("voidspark_special",
            CosmicMaterialSet.VOIDSPARK, false,
            new HaloItemRenderData(4, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false));

    public static final Holder<MaterialIconSet> STARMETALICO = register("starmetal_special",
            CosmicMaterialSet.STARMETAL, false,
            new HaloItemRenderData(2, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo_glass"),
                    true,
                    true));

    public static final Holder<MaterialIconSet> SOL_STEEL = register("sol_steel", CosmicMaterialSet.SOL,
            false,
            new HaloItemRenderData(4, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/compression_halo_sol"),
                    true,
                    false));
}
