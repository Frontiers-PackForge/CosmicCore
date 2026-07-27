package com.ghostipedia.cosmiccore.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CosmicCoreConfig {

    public static final ModConfigSpec CLIENT_SPEC;
    private static final ModConfigSpec.BooleanValue DEV_VISOR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        DEV_VISOR = builder.comment("§cWARNING! WARNING! WARNING!§r\n" +
                "This config enables editing aspects of the pack not normally allowed by default. This can cause " +
                "potentially damaging effects to your progression, save, team, or otherwise anything in the pack.\n" +
                "Using this means you have CLEARLY READ THIS, and ACCEPT FAULT for enabling it.\n" +
                "If you are requesting support with this config on, there is a high chance developers will not be " +
                "happy.")
                .translation("config.cosmiccore.dev_visor")
                .define("devVisor", false);
        CLIENT_SPEC = builder.build();
    }

    private CosmicCoreConfig() {}

    public static boolean devVisor() {
        return CLIENT_SPEC.isLoaded() && DEV_VISOR.get();
    }
}
