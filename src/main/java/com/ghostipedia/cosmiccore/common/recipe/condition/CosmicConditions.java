package com.ghostipedia.cosmiccore.common.recipe.condition;

public class CosmicConditions {

    public static void register() {
        TitanCondition.register();
        DeedCondition.register();
        LinkedPartnerCondition.register();
        LinkedPartnerDimensionCondition.register();
        LinkedPartnerDimensionItemCondition.register();
        LinkedPartnerDimensionFluidCondition.register();
    }
}
