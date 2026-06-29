package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.client.dev.AbyssDevView;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightTexture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public class AbyssLightTextureMixin {

    @Redirect(method = "updateLightTexture",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private Object cosmiccore$forceBright(OptionInstance<?> instance) {
        Object value = instance.get();
        if (!AbyssDevView.stripFog) {
            return value;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isUnderWater() && instance == mc.options.gamma()) {
            return 16.0d;
        }
        return value;
    }
}
