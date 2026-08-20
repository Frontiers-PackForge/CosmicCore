package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import org.jetbrains.annotations.Nullable;

public interface EffortlessBuildingUndoEntryExtension {

    @Nullable
    EffortlessBuildingOperationMetadata cosmiccore$getOperationMetadata();

    void cosmiccore$setOperationMetadata(EffortlessBuildingOperationMetadata metadata);
}
