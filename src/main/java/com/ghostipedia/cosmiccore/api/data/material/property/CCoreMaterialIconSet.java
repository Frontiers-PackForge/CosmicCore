package com.ghostipedia.cosmiccore.api.data.material.property;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.item.HaloItemRenderer;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet;
import com.ghostipedia.cosmiccore.utils.ColorUtil;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

import java.util.function.IntSupplier;

public class CCoreMaterialIconSet extends MaterialIconSet {

    @Getter
    private final IRenderer renderer;

    public CCoreMaterialIconSet(String name, MaterialIconSet parentIconset, boolean root, IRenderer renderer) {
        super(name, parentIconset, root);
        this.renderer = renderer;
    }

    static final IntSupplier prismaticColor = () -> {
        float v = (float) ((System.currentTimeMillis() / 500) % 10) / 10;
        if (v > 0.5f)
            v = 1 - v;
        return (0xff << 24) | ColorUtil.lerpColorRGB(0xffc0cb, 0x000080, v * 2);
    };

    public static final CCoreMaterialIconSet VIBRANIUM = new CCoreMaterialIconSet("vibranium",
            CosmicMaterialSet.NEUTRONITE, false,
            HaloItemRenderer.create(4, 0xFF489BC3, CosmicCore.id("block/iris/rnd/halo"), true,
                    false));
    public static final CCoreMaterialIconSet VIBRANIUM_NEUTRONIUM = new CCoreMaterialIconSet("vibranium_neutronium",
            CosmicMaterialSet.NEUTRONIUM_CCORE, false,
            HaloItemRenderer.create(3, 0xFFFFFFFF, new ResourceLocation(CosmicCore.MOD_ID,
                    "block/iris/rnd/compression_halo_neutronium_faded"), true,
                    false));

    // public static final CCoreMaterialIconSet VIBRANIUM_NEUTRONIUM = new CCoreMaterialIconSet("vibranium_neutronium",
    // CosmicMaterialSet.NEUTRONIUM_CCORE, false, LensRender::new);

    public static final CCoreMaterialIconSet PRISMATIC = new CCoreMaterialIconSet("prismatic", SHINY, false,
            HaloItemRenderer.create(8, 0xFF1c1926, CosmicCore.id("block/iris/rnd/storm_halo"),
                    true,
                    false));

    public static final CCoreMaterialIconSet VOIDSPARKICO = new CCoreMaterialIconSet("voidspark_special",
            CosmicMaterialSet.VOIDSPARK, false,
            HaloItemRenderer.create(4, 0xFFFFFFFF,
                    CosmicCore.id("block/iris/rnd/compression_halo_cyan_faded"), true,
                    false));

    public static final CCoreMaterialIconSet SOL_STEEL = new CCoreMaterialIconSet("sol_steel", CosmicMaterialSet.SOL,
            false,
            HaloItemRenderer.create(4, 0xFFFFFFFF,
                    CosmicCore.id("block/iris/rnd/compression_halo_sol"),
                    true,
                    false));
}
