package com.ghostipedia.cosmiccore.mixin.ebfix;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import neoforge.nl.requios.effortlessbuilding.screen.RadialMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RadialMenu.class)
public abstract class RadialMenuSingleplayerConfigMixin {

    @Redirect(
              method = "render",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z"))
    private boolean cosmiccore$enableServerConfigForSingleplayer(LocalPlayer player, int permissionLevel) {
        return Minecraft.getInstance().isSingleplayer() || player.hasPermissions(permissionLevel);
    }
}
