package com.ghostipedia.cosmiccore.mixin.client;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = PlanetsScreen.class, remap = false)
public class AdAstraPlanetsScreenMixin {

    @Unique
    Set<ResourceKey<Level>> noLand = Set.of(); //Add the planets where the land button should not show up, ex: Planet.MOON

    @Shadow
    private Planet selectedPlanet;

    @Inject(method = "createSelectedPlanetButtons", at = @At("HEAD"), cancellable = true)
    private void createSelectedPlanetButtons(CallbackInfo ci) {
        if (this.selectedPlanet == null) return;
        if (noLand.contains(selectedPlanet.dimension())) ci.cancel();
    }
}
