package com.ghostipedia.cosmiccore.common.compat.gtceu.transformer;

public interface ConfigurableTransformer {

    int cosmiccore$getStepDownTransitions();

    int cosmiccore$getStepDownOutputTier();

    int cosmiccore$getStepDownOutputAmperage();

    int cosmiccore$getMaximumStepDownOutputAmperage();

    void cosmiccore$setStepDownTransitions(int transitions);

    void cosmiccore$setStepDownOutputAmperage(int amperage);
}
