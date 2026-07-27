package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedSelectorConfig;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftblibrary.config.EnumConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EnumConfig.class, remap = false)
public abstract class EnumConfigDeedSelectorMixin {

    @ModifyExpressionValue(
                           method = "onClicked",
                           at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int cosmiccore$alwaysOpenDeedSelector(int size) {
        return (Object) this instanceof DeedSelectorConfig ? Math.max(size, 17) : size;
    }
}
