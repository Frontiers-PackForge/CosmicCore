package com.ghostipedia.cosmiccore.api.data.material.property;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet;
import com.ghostipedia.cosmiccore.common.item.behavior.HaloItemBehavior;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

@Getter
public class CCoreMaterialIconSet extends MaterialIconSet {

    private final ICustomRenderer customRender;

    public CCoreMaterialIconSet(String name, MaterialIconSet parentIconset, boolean root, IRenderer renderer) {
        this(name, parentIconset, root, renderer == null ? null : () -> renderer);
    }

    private CCoreMaterialIconSet(String name, MaterialIconSet parentIconset, boolean root, ICustomRenderer renderer) {
        super(name, parentIconset, root);
        this.customRender = renderer;
    }

    public static final CCoreMaterialIconSet VIBRANIUM = new CCoreMaterialIconSet("vibranium",
            CosmicMaterialSet.NEUTRONITE, false,
            new HaloItemBehavior(5, 0xFF1c1926, new ResourceLocation(CosmicCore.MOD_ID, "rnd/halo"), true, true));

    public static final CCoreMaterialIconSet PRISMATIC = new CCoreMaterialIconSet("prismatic", SHINY, false,
            new HaloItemBehavior(8, 0x99FFFFFF, new ResourceLocation(CosmicCore.MOD_ID, "rnd/halo"), true, false));
}
