package com.ghostipedia.cosmiccore.common.data.datagen;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

public class CosmicMachineModels {

    public static MachineBuilder.ModelInitializer createSeparateControllerCasingMachineModel(ResourceLocation controllerTexture,
                                                                                             ResourceLocation baseCasingTexture,
                                                                                             ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(CUBE_ALL_SIDED_OVERLAY_MODEL))
                        .texture("all", controllerTexture);
                return addWorkableOverlays(overlays, status, model);
            });
            builder.addTextureOverride("all", baseCasingTexture);
        };
    }
}
