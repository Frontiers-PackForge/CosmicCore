package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import earth.terrarium.adastra.common.constants.ConstantComponents;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static earth.terrarium.adastra.client.screens.PlanetsScreen.SELECTION_MENU;
import static earth.terrarium.adastra.client.screens.PlanetsScreen.SMALL_SELECTION_MENU;

@Mixin(value = PlanetsScreen.class, remap = false)
public class AdAstraPlanetsScreenMixin extends AbstractContainerScreen<PlanetsMenu> {

    @Unique
    Set<ResourceKey<Level>> noLand = Set.of(); // Add the planets where the land button should not show up
                                               // ex: Planet.MOON

    @Unique
    private static final ResourceLocation SELECTION_MENU_NOLAND = CosmicCore
            .id("textures/gui/sprites/planets/selection_menu_noland.png");
    @Shadow
    private Planet selectedPlanet;
    @Shadow
    private int pageIndex;
    @Shadow
    private @Nullable ResourceLocation selectedSolarSystem;

    public AdAstraPlanetsScreenMixin(PlanetsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "createSelectedPlanetButtons", at = @At("HEAD"), cancellable = true)
    private void createSelectedPlanetButtons(CallbackInfo ci) {
        if (this.selectedPlanet == null) return;
        if (noLand.contains(selectedPlanet.dimension())) ci.cancel();
    }

    /**
     * @author Kolja
     * @reason Removes the outline of the land button where it should not show up
     */
    @Overwrite(remap = false)
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (this.pageIndex == 2) {
            if (noLand.contains(selectedPlanet.dimension())) {
                graphics.blit(SELECTION_MENU_NOLAND, 7, this.height / 2 - 88, 0.0F, 0.0F, 209, 177, 209, 177);
                graphics.drawCenteredString(this.font, ConstantComponents.SPACE_STATION, 163, this.height / 2 - 15,
                        16777215);
            } else {
                graphics.blit(SELECTION_MENU, 7, this.height / 2 - 88, 0.0F, 0.0F, 209, 177, 209, 177);
                graphics.drawCenteredString(this.font, ConstantComponents.SPACE_STATION, 163, this.height / 2 - 15,
                        16777215);
            }
        } else {
            graphics.blit(SMALL_SELECTION_MENU, 7, this.height / 2 - 88, 0.0F, 0.0F, 105, 177, 105, 177);
        }

        if (this.pageIndex == 2 && this.selectedPlanet != null) {
            MutableComponent title = Component.translatableWithFallback(
                    "planet.%s.%s".formatted(this.selectedPlanet.dimension().location().getNamespace(),
                            this.selectedPlanet.dimension().location().getPath()),
                    this.title(this.selectedPlanet.dimension().location().getPath()));
            graphics.drawCenteredString(this.font, title, 57, this.height / 2 - 60, 16777215);
        } else if (this.pageIndex == 1 && this.selectedSolarSystem != null) {
            MutableComponent title = Component
                    .translatableWithFallback(
                            "solar_system.%s.%s".formatted(this.selectedSolarSystem.getNamespace(),
                                    this.selectedSolarSystem.getPath()),
                            this.title(this.selectedSolarSystem.getPath()));
            graphics.drawCenteredString(this.font, title, 57, this.height / 2 - 60, 16777215);
        } else {
            graphics.drawCenteredString(this.font, ConstantComponents.CATALOG, 57, this.height / 2 - 60, 16777215);
        }
    }

    @Unique
    private String title(String string) {
        return WordUtils.capitalizeFully(string.replace("_", " "));
    }
}
