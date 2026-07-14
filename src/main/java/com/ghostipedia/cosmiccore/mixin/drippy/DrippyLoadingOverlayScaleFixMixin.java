package com.ghostipedia.cosmiccore.mixin.drippy;

import net.minecraft.client.gui.screens.LoadingOverlay;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = LoadingOverlay.class, priority = 900)
public class DrippyLoadingOverlayScaleFixMixin {}
