package com.ghostipedia.cosmiccore.common.compat.ftbquests;

public interface QuestDependencyLineExtension {

    DependencyLineSettings cosmiccore$getDependencyLineSettings(long dependencyId);

    void cosmiccore$setDependencyLineSettings(long dependencyId, DependencyLineSettings settings);
}
