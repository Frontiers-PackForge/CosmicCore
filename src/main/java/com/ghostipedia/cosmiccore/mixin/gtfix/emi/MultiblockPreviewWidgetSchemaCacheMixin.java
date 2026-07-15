package com.ghostipedia.cosmiccore.mixin.gtfix.emi;

import com.ghostipedia.cosmiccore.integration.emi.MultiblockPreviewSchemaCache;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MultiblockPreviewWidget.class, remap = false)
public class MultiblockPreviewWidgetSchemaCacheMixin {

    @Shadow
    @Final
    private MultiblockMachineDefinition multiblockDefinition;

    @Shadow
    private MultiblockSchemaInfo multiblockSchemaInfo;

    @Shadow
    private boolean isFlipped;

    @Shadow
    private Direction frontFacing;

    @Shadow
    private Direction upFacing;

    @Inject(method = "refreshSchema", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$reusePreparedSchema(CallbackInfo ci) {
        if (MultiblockPreviewSchemaCache.apply(this.multiblockDefinition, this.multiblockSchemaInfo,
                this.frontFacing, this.upFacing, this.isFlipped)) {
            ci.cancel();
        }
    }
}
