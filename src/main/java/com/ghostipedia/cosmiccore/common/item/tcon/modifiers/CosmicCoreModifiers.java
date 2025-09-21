package com.ghostipedia.cosmiccore.common.item.tcon.modifiers;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

public class CosmicCoreModifiers {

    private static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(CosmicCore.MOD_ID);

    public static final StaticModifier<WrenchModeSwitchModifier> wrenchModeSwitch = MODIFIERS.register("wrench_switch",
            WrenchModeSwitchModifier::new);

    public static void init() {
        MODIFIERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
