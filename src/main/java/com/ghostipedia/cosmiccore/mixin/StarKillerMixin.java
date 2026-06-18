package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class StarKillerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private static ResourceLocation SUN_LOCATION;

    private static final ResourceLocation COSMIC_SUN = ResourceLocation.fromNamespaceAndPath("cosmiccore",
            "textures/environment/blackhole.png");

    // Redirect the exact moment vanilla binds a sky texture; swap only when it's the sun
    @Redirect(
              method = "renderSky",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    private void cosmiccore$swapSunTexture(int unit, ResourceLocation original) {
        // Only replace the SUN texture, leave MOON/stars/etc. alone
        if (original.equals(SUN_LOCATION) && shouldUseCosmicSun()) {
            RenderSystem.setShaderTexture(unit, COSMIC_SUN);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderSystem.defaultBlendFunc();
        } else {
            RenderSystem.setShaderTexture(unit, original);
        }
    }

    private boolean shouldUseCosmicSun() {
        Player p = minecraft.player;
        if (p == null) return false;

        ItemStack main = p.getMainHandItem();
        ItemStack off = p.getOffhandItem();
        var special = CosmicItems.ABERRANT_ESSENCE.get();

        return main.is(special) || off.is(special);
    }
}
