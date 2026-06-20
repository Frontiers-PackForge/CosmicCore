package com.ghostipedia.cosmiccore.common.recipe.condition;

public class CosmicConditions {

    public static void register() {
        TitanCondition.register();
        LinkedPartnerCondition.register();
        LinkedPartnerDimensionCondition.register();
        LinkedPartnerDimensionItemCondition.register();
        LinkedPartnerDimensionFluidCondition.register();
    }
}
