package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.armor.AbyssModule;
import com.ghostipedia.cosmiccore.common.armor.OxygenTankModule;
import com.ghostipedia.cosmiccore.common.armor.UtilityArmorModule;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.world.entity.EquipmentSlot;

public final class CosmicModules {

    private CosmicModules() {}

    public static void init() {
        for (int tier = 1; tier <= 3; tier++) {
            register("stealth_module_" + tier, AbyssModule.Role.STEALTH, tier, EquipmentSlot.CHEST);
            register("pressure_module_" + tier, AbyssModule.Role.PRESSURE, tier, EquipmentSlot.CHEST);
        }
        register("simple_rebreather_module", AbyssModule.Role.REBREATHER, 1, EquipmentSlot.HEAD);
        register("advanced_rebreather_module", AbyssModule.Role.REBREATHER, 2, EquipmentSlot.HEAD);
        CosmicRegistration.REGISTRATE.simple("bronze_oxygen_tank_module", GTRegistries.Keys.ITEM_MODULE,
                () -> new OxygenTankModule(CosmicCore.id("bronze_oxygen_tank_module"), 1000, 10));
        CosmicRegistration.REGISTRATE.simple("steel_oxygen_tank_module", GTRegistries.Keys.ITEM_MODULE,
                () -> new OxygenTankModule(CosmicCore.id("steel_oxygen_tank_module"), 2500, 15));
        CosmicRegistration.REGISTRATE.simple("wireless_pda", GTRegistries.Keys.ITEM_MODULE,
                () -> new UtilityArmorModule(CosmicCore.id("wireless_pda"), UtilityArmorModule.Role.PDA));
        CosmicRegistration.REGISTRATE.simple("travelers_boots", GTRegistries.Keys.ITEM_MODULE,
                () -> new UtilityArmorModule(CosmicCore.id("travelers_boots"), UtilityArmorModule.Role.TRAVELER_BOOTS));
    }

    private static void register(String id, AbyssModule.Role role, int tier, EquipmentSlot slot) {
        CosmicRegistration.REGISTRATE.simple(id, GTRegistries.Keys.ITEM_MODULE,
                () -> new AbyssModule(CosmicCore.id(id), role, tier, slot));
    }
}
