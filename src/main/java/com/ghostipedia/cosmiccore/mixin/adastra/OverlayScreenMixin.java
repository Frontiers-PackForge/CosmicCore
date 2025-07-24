package com.ghostipedia.cosmiccore.mixin.adastra;

import com.ghostipedia.cosmiccore.api.item.armor.SpaceArmorComponentItem;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.api.systems.PlanetData;
import earth.terrarium.adastra.client.config.AdAstraConfigClient;
import earth.terrarium.adastra.client.screens.player.OverlayScreen;
import earth.terrarium.adastra.client.utils.ClientData;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static earth.terrarium.adastra.client.screens.player.OverlayScreen.*;

@Mixin(value = OverlayScreen.class, remap = false)
public abstract class OverlayScreenMixin {

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
                     ordinal = 0,
                     remap = true))
    private static void render(GuiGraphics graphics, float partialTick, CallbackInfo ci,
                               @Local Font font, @Local PoseStack poseStack, @Local LocalPlayer player,
                               @Local ItemStack chestStack) {
        // annoying amount of copy-pasted code, but we can't redirect casts so there's nothing we can do about it.

        if (!(chestStack.getItem() instanceof SpaceArmorComponentItem spaceSuit)) {
            return;
        }
        long amount = SpaceSuitItem.getOxygenAmount(player);
        long capacity = spaceSuit.getFluidContainer(chestStack).getTankCapacity(0);
        double ratio = (double) amount / capacity;
        int barHeight = (int) (ratio * 52);

        int x = AdAstraConfigClient.oxygenBarX;
        int y = AdAstraConfigClient.oxygenBarY;
        float scale = AdAstraConfigClient.oxygenBarScale;

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        graphics.blit(OXYGEN_TANK_EMPTY, x, y, 0, 0, 62, 52, 62, 52);
        graphics.blit(OXYGEN_TANK, x, y + 52 - barHeight, 0, 52 - barHeight, 62, barHeight, 62, 52);

        var text = String.format("%.1f%%", ratio * 100);
        int textWidth = font.width(text);
        int color = ratio <= 0 ? 0xDC143C : 0xFFFFFF;
        PlanetData localData = ClientData.getLocalData();
        if (localData != null && localData.oxygen()) {
            color = 0x55ff55;
        }
        graphics.drawString(font, text, (int) (x + (62 - textWidth) / 2f), y + 52 + 3, color);
        poseStack.popPose();
    }
}
