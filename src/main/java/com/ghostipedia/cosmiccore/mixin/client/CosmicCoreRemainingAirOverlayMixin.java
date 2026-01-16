package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.common.breath.OxygenHelper;

import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.fluids.FluidType;

import com.simibubi.create.content.equipment.armor.RemainingAirOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RemainingAirOverlay.class, remap = false)
public class CosmicCoreRemainingAirOverlayMixin {

    /**
     * Make the air overlay show when air quality is bad, even if not in fluid.
     * Redirects the fluidType.isAir() check to return false when bad air activates helmet.
     */
    @Redirect(method = "render",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraftforge/fluids/FluidType;isAir()Z"),
              remap = false)
    private boolean cosmicCore$redirectIsAir(FluidType fluidType) {
        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        // If air quality is bad, pretend we're NOT in air so overlay shows
        if (player != null && OxygenHelper.airQualityActivatesHelmet(player)) {
            return false;
        }
        return fluidType.isAir();
    }
}
