package com.ghostipedia.cosmiccore.mixin.ebfix;

import neoforge.nl.requios.effortlessbuilding.buildmode.BuildModeEnum;
import neoforge.nl.requios.effortlessbuilding.buildmode.BuildModes;
import neoforge.nl.requios.effortlessbuilding.buildpipeline.BuildPipelineClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuildModes.class)
public abstract class BuildModesDisableSequenceMixin {

    @Shadow
    public abstract BuildModeEnum getBuildMode();

    @Inject(method = "setBuildMode", at = @At("HEAD"))
    private void cosmiccore$cancelSequenceBeforeDisabling(BuildModeEnum newMode, CallbackInfo ci) {
        if (getBuildMode() != BuildModeEnum.DISABLED && newMode == BuildModeEnum.DISABLED) {
            BuildPipelineClient.cancelCurrentSequence();
        }
    }
}
