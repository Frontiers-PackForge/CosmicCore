package com.ghostipedia.cosmiccore.mixin.ebfix;

import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingOperationMetadata;
import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingUndoEntryExtension;

import neoforge.nl.requios.effortlessbuilding.utilities.UndoManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = UndoManager.UndoEntry.class, remap = false)
public abstract class UndoEntryOperationMetadataMixin implements EffortlessBuildingUndoEntryExtension {

    @Unique
    @Nullable
    private EffortlessBuildingOperationMetadata cosmiccore$operationMetadata;

    @Override
    @Nullable
    public EffortlessBuildingOperationMetadata cosmiccore$getOperationMetadata() {
        return cosmiccore$operationMetadata;
    }

    @Override
    public void cosmiccore$setOperationMetadata(EffortlessBuildingOperationMetadata metadata) {
        cosmiccore$operationMetadata = metadata;
    }
}
