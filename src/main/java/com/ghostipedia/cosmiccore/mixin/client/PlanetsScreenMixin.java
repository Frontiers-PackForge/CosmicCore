package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.misc.PlanetKeys;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = PlanetsScreen.class, remap = false)
public abstract class PlanetsScreenMixin extends AbstractContainerScreen<PlanetsMenu> {

    // Add the planets where the land button should not show up
    // ex: PlanetKeys.MOON
    @Unique
    private static final Set<ResourceKey<Level>> CCORE$NO_LAND = Set.of(PlanetKeys.SUN, PlanetKeys.SATURN,
            PlanetKeys.JUPITER);
    @Unique
    private static final ResourceLocation SELECTION_MENU_NOLAND = CosmicCore
            .id("textures/gui/sprites/planets/selection_menu_noland.png");

    @Shadow
    private Planet selectedPlanet;

    public PlanetsScreenMixin(PlanetsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "createSelectedPlanetButtons", at = @At("HEAD"), cancellable = true)
    private void createSelectedPlanetButtons(CallbackInfo ci) {
        if (this.selectedPlanet == null) return;
        if (CCORE$NO_LAND.contains(selectedPlanet.dimension())) ci.cancel();
    }

    @ModifyArg(method = "renderBg",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V",
                        ordinal = 0),
               index = 0)
    private ResourceLocation cosmicCore$changeSelectionMenuBackground(ResourceLocation original) {
        if (CCORE$NO_LAND.contains(selectedPlanet.dimension())) {
            return SELECTION_MENU_NOLAND;
        } else {
            return original;
        }
    }
}
