package com.ghostipedia.cosmiccore.api.data.material.property;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.item.HaloItemRenderer;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet;
import com.ghostipedia.cosmiccore.utils.ColorUtil;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class CCoreMaterialIconSet extends MaterialIconSet {

    private final ICustomRenderer customRender;

    public CCoreMaterialIconSet(String name, MaterialIconSet parentIconset, boolean root, IRenderer renderer) {
        this(name, parentIconset, root, renderer == null ? null : () -> renderer);
    }

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
            HaloItemRenderer.create(5, 0xFF1c1926, new ResourceLocation(CosmicCore.MOD_ID, "rnd/halo"), true, true));

    public static final CCoreMaterialIconSet PRISMATIC = new CCoreMaterialIconSet("prismatic", SHINY, false,
            HaloItemRenderer.create(8, 0xFF1c1926, new ResourceLocation(CosmicCore.MOD_ID, "rnd/storm_halo"), true,
                    false));

    public static final CCoreMaterialIconSet VOIDSPARKICO = new CCoreMaterialIconSet("voidspark_special", CosmicMaterialSet.VOIDSPARK, false,
            HaloItemRenderer.create(8, 0xFF1c1926, new ResourceLocation(CosmicCore.MOD_ID, "rnd/storm_halo"), true,
                    false));
}
