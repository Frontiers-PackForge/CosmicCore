package com.ghostipedia.cosmiccore.common.data.datagen;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

public class CosmicMachineModels {

    public static MachineBuilder.ModelInitializer createSeparateControllerCasingMachineModel(ResourceLocation controllerTexture,
                                                                                             ResourceLocation baseCasingTexture,
                                                                                             ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.hasProperty(RecipeLogic.STATUS_PROPERTY) ?
                        state.getValue(RecipeLogic.STATUS_PROPERTY) :
                        RecipeLogic.Status.IDLE;

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(CUBE_ALL_SIDED_OVERLAY_MODEL))
                        .texture("all", controllerTexture);
                return ConfiguredModel.builder().modelFile(addWorkableOverlays(overlays, status, model)).build();
            });
            builder.addTextureOverride("all", baseCasingTexture);
        };
    }
}
