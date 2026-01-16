package com.ghostipedia.cosmiccore.common.reflection.bargain;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.ArmorBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.BackBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.DepthsBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.FallImmunityBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.FireImmunityBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.HealthBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.HomeBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.HungerBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.NightVisionBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.ReachBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.StepAssistBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.StrengthBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.SwiftnessBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.VoidResistanceBargain;

/**
 * Registers all bargains for the Reflection system.
 * Called during mod initialization.
 */
public final class CosmicBargains {

    private CosmicBargains() {}

    public static void init() {
        CosmicCore.LOGGER.info("Registering Reflection bargains...");

        // EARLY TIER - Gateway bargains (0-100 erosion)
        safeRegister("QuakeMovementBargain", () -> BargainRegistry.register(QuakeMovementBargain.INSTANCE));
        safeRegister("StepAssistBargain", () -> BargainRegistry.register(StepAssistBargain.INSTANCE));
        safeRegister("NightVisionBargain", () -> BargainRegistry.register(NightVisionBargain.INSTANCE));
        safeRegister("SwiftnessBargain", () -> BargainRegistry.register(SwiftnessBargain.INSTANCE));

        // EARLY_MID TIER - Building addiction (0-300)
        safeRegister("HomeBargain", () -> BargainRegistry.register(HomeBargain.INSTANCE));
        safeRegister("BackBargain", () -> BargainRegistry.register(BackBargain.INSTANCE));
        safeRegister("HealthBargain", () -> BargainRegistry.register(HealthBargain.INSTANCE));
        safeRegister("StrengthBargain", () -> BargainRegistry.register(StrengthBargain.INSTANCE));
        safeRegister("DepthsBargain", () -> BargainRegistry.register(DepthsBargain.INSTANCE));

        // MID TIER - Significant commitment (100-500)
        safeRegister("ReachBargain", () -> BargainRegistry.register(ReachBargain.INSTANCE));
        safeRegister("FallImmunityBargain", () -> BargainRegistry.register(FallImmunityBargain.INSTANCE));
        safeRegister("HungerBargain", () -> BargainRegistry.register(HungerBargain.INSTANCE));
        safeRegister("ArmorBargain", () -> BargainRegistry.register(ArmorBargain.INSTANCE));
        safeRegister("FireImmunityBargain", () -> BargainRegistry.register(FireImmunityBargain.INSTANCE));

        // LATE TIER - Deep corruption (300-750)
        safeRegister("VoidResistanceBargain", () -> BargainRegistry.register(VoidResistanceBargain.INSTANCE));

        CosmicCore.LOGGER.info("Registered {} bargains", BargainRegistry.getAll().size());
    }

    private static void safeRegister(String name, Runnable registrar) {
        try {
            registrar.run();
        } catch (Throwable t) {
            CosmicCore.LOGGER.error("Failed to register bargain: {}", name, t);
        }
    }
}
