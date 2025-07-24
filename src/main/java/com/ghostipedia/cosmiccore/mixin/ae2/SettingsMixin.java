package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.api.misc.ae2.BlockingMode;
import com.ghostipedia.cosmiccore.api.misc.ae2.CosmicBlockingSettings;

import appeng.api.config.Setting;
import appeng.api.config.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Settings.class, remap = false)
public class SettingsMixin {

    @SafeVarargs
    @Shadow
    private static <T extends Enum<T>> Setting<T> register(String name, T firstOption, T... moreOptions) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void init(CallbackInfo ci) {
        CosmicBlockingSettings.BLOCKING_MODE = register("blocking_type",
                BlockingMode.ALL, BlockingMode.CONTAINS, BlockingMode.CONTAINS_SIMILAR);
    }
}
