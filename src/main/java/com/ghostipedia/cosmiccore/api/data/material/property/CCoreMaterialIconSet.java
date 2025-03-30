package com.ghostipedia.cosmiccore.api.data.material.property;

import com.ghostipedia.cosmiccore.client.renderer.item.HaloRenders;
import com.ghostipedia.cosmiccore.client.renderer.item.RadianceItemRenderer;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import lombok.Getter;


@Getter
public class CCoreMaterialIconSet extends MaterialIconSet {

    private final ICustomRenderer customRender;
    public CCoreMaterialIconSet(String name, MaterialIconSet parentIconset, boolean root, IRenderer renderer) {
        this(name, parentIconset, root, renderer == null ? null : () -> renderer);
    }

    private CCoreMaterialIconSet(String name, MaterialIconSet parentIconset, boolean root, ICustomRenderer renderer) {
        super (name, parentIconset, root);
        this.customRender = renderer;
    }


    public static final CCoreMaterialIconSet VIBRANIUM = new CCoreMaterialIconSet("vibranium", CosmicMaterialSet.NEUTRONITE, false, RadianceItemRenderer.INSTANCE);
    public static final CCoreMaterialIconSet PRISMATIC = new CCoreMaterialIconSet("prismatic", SHINY, false, HaloRenders.PRISMATIC_TUNGSTEN_HALO);

}
