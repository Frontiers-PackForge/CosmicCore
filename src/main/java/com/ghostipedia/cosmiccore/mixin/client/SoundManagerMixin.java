package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SoundManager.class, priority = 999)
public abstract class SoundManagerMixin {

    @Shadow
    @Final
    private SoundEngine soundEngine;

    @Inject(
            method = "play",
            at = @At("HEAD"),
            cancellable = true)
    private void onPlay(SoundInstance sound, CallbackInfo callbackInfo) {
        if (cosmicCore$checkForSpaceRadio()) {
            callbackInfo.cancel();  // Early exits Ad Astra's mixin from being called. Hopefully
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                SoundSource source = sound.getSource();
                Minecraft.getInstance().execute(() -> {
                    float volume = 1.0F;
                    SoundInstance newSound = new SimpleSoundInstance(sound.getLocation(), source, volume, 0F,
                            Minecraft.getInstance().level.random, sound.isLooping(), 0, sound.getAttenuation(),
                            sound.getX(), sound.getY(), sound.getZ(), sound.isRelative());
                    this.soundEngine.play(newSound);

                });
            }
        }
    }

    @Inject(
            method = "playDelayed",
            at = @At("HEAD"),
            cancellable = true)
    private void onPlayDelayed(SoundInstance sound, int delay, CallbackInfo callbackInfo) {
        if (cosmicCore$checkForSpaceRadio()) {
            callbackInfo.cancel();
        }
    }

    @Unique
    private boolean cosmicCore$checkForSpaceRadio() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            return CosmicUtils.hasCurio(player, "head", CosmicItems.SPACE_RADIO.asItem());
        }
        return false;
    }
}
