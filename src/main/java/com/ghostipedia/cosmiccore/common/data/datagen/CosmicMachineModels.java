package com.ghostipedia.cosmiccore.common.data.datagen;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import java.util.Arrays;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

public class CosmicMachineModels {

    public static MachineBuilder.ModelInitializer createSingleTextureMachineModel(ResourceLocation baseTexture) {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(
                            ResourceLocation.withDefaultNamespace("block/cube_all")))
                    .texture("all", baseTexture);
            builder.partialState().setModel(model);
        };
    }

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

    public static MachineBuilder.ModelInitializer createConfiguredWorkableCasingMachineModel(
                                                                                             ResourceLocation baseCasingTexture,
                                                                                             Property<Integer> configuration,
                                                                                             ResourceLocation... overlayDirectories) {
        if (overlayDirectories.length != configuration.getPossibleValues().size()) {
            throw new IllegalArgumentException("Overlay count does not match configuration property");
        }
        return (ctx, prov, builder) -> {
            WorkableOverlays[] overlays = Arrays.stream(overlayDirectories)
                    .map(directory -> WorkableOverlays.get(directory, prov.getExistingFileHelper()))
                    .toArray(WorkableOverlays[]::new);

            builder.forAllStatesModelsExcept(state -> {
                int selectedConfiguration = state.getValue(configuration);
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);
                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(CUBE_ALL_SIDED_OVERLAY_MODEL))
                        .texture("all", baseCasingTexture);
                return addWorkableOverlays(overlays[selectedConfiguration], status, model);
            }, IS_FORMED);
            builder.addTextureOverride("all", baseCasingTexture);
        };
    }
}
