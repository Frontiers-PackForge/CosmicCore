package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.deployment.LeylinePrefab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AEKeyRendering.class)
public abstract class LeylineAEIconMixin {

    @Inject(method = "drawInGui", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$prefabIcon(Minecraft minecraft, GuiGraphics graphics, int x, int y, AEKey key,
                                              CallbackInfo ci) {
        if (!(key instanceof AEItemKey item) || item.getItem() != CosmicItems.LEYLINE_PACKAGE.get()) return;
        var data = item.toStack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ResourceLocation icon = ResourceLocation.tryParse(data.getCompound(LeylinePrefab.DATA).getString("icon"));
        if (icon == null) return;
        var stack = new ItemStack(BuiltInRegistries.ITEM.get(icon));
        if (stack.isEmpty()) return;
        graphics.renderItem(stack, x, y);
        graphics.pose().pushPose();
        graphics.pose().translate(x + 9, y, 200);
        graphics.pose().scale(0.4375f, 0.4375f, 1);
        graphics.renderItem(CosmicItems.LEYLINE_PACKAGE.asStack(), 0, 0);
        graphics.pose().popPose();
        ci.cancel();
    }
}
