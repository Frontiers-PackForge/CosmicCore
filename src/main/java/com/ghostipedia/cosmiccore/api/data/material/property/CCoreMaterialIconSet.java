package com.ghostipedia.cosmiccore.api.data.material.property;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.item.component.HaloItemRenderData;
import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet;
import com.ghostipedia.cosmiccore.utils.ColorUtil;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class CCoreMaterialIconSet extends MaterialIconSet {

    private final ICustomRenderer customRender;

    public CCoreMaterialIconSet(String name, MaterialIconSet parentIconset, boolean root, ICustomRenderer renderer) {
        super(name, parentIconset, root);
        this.customRender = renderer;
    }

    static final Supplier<Integer> prismaticColor = () -> {
        float v = (float) ((System.currentTimeMillis() / 500) % 10) / 10;
        if (v > 0.5f)
            v = 1 - v;
        return (0xff << 24) | ColorUtil.lerpColorRGB(0xffc0cb, 0x000080, v * 2);
    };

    public static final CCoreMaterialIconSet VIBRANIUM = new CCoreMaterialIconSet("vibranium",
            CosmicMaterialSet.NEUTRONITE, false,
            new HaloItemRenderData(4, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/shifting_halo_cyan"), true,
                    false));
    public static final CCoreMaterialIconSet VIBRANIUM_NEUTRONIUM = new CCoreMaterialIconSet("vibranium_neutronium",
            CosmicMaterialSet.NEUTRONIUM_CCORE, false,
            new HaloItemRenderData(3, 0xFFFFFFFF, ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID,
                    "block/iris/rnd/compression_halo_neutronium_faded"), true,
                    false));

    public static final CCoreMaterialIconSet PRISMATIC = new CCoreMaterialIconSet("prismatic", SHINY, false,
            new HaloItemRenderData(8, 0xFF1c1926,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/storm_halo"),
                    true,
                    false));

    public static final CCoreMaterialIconSet CHRONIC = new CCoreMaterialIconSet("chronic", CosmicMaterialSet.CHRONON,
            false,
            new HaloItemRenderData(8, 0xFF1c1926,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/time_halo"),
                    true,
                    false));

    public static final CCoreMaterialIconSet VOIDSPARKICO = new CCoreMaterialIconSet("voidspark_special",
            CosmicMaterialSet.VOIDSPARK, false,
            new HaloItemRenderData(4, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false));

    public static final CCoreMaterialIconSet STARMETALICO = new CCoreMaterialIconSet("starmetal_special",
            CosmicMaterialSet.STARMETAL, false,
            new HaloItemRenderData(2, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo_glass"),
                    true,
                    true));

    public static final CCoreMaterialIconSet SOL_STEEL = new CCoreMaterialIconSet("sol_steel", CosmicMaterialSet.SOL,
            false,
            new HaloItemRenderData(4, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/compression_halo_sol"),
                    true,
                    false));
}
