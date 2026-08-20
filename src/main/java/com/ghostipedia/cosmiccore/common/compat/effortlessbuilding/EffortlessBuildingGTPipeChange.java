package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import org.jetbrains.annotations.Nullable;

public record EffortlessBuildingGTPipeChange(
                                             @Nullable EffortlessBuildingGTPipeSnapshot before,
                                             @Nullable EffortlessBuildingGTPipeSnapshot after) {}
