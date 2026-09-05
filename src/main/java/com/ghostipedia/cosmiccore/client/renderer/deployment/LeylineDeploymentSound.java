package com.ghostipedia.cosmiccore.client.renderer.deployment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class LeylineDeploymentSound extends AbstractTickableSoundInstance {

    private final ClientLevel level;
    private final AABB bounds;
    private final float gain;

    private LeylineDeploymentSound(SoundEvent sound, Vec3 position, AABB bounds, float gain, float pitch,
                                   boolean looping) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.level = Minecraft.getInstance().level;
        this.bounds = bounds;
        this.gain = gain;
        this.pitch = pitch;
        this.looping = looping;
        this.attenuation = Attenuation.NONE;
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        tick();
    }

    static LeylineDeploymentSound play(SoundEvent sound, Vec3 position, AABB bounds, float gain, float pitch,
                                       boolean looping) {
        var instance = new LeylineDeploymentSound(sound, position, bounds, gain, pitch, looping);
        Minecraft.getInstance().getSoundManager().play(instance);
        return instance;
    }

    @Override
    public void tick() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != level || minecraft.player == null) {
            stop();
            return;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        double distance = camera.distanceTo(new Vec3(Mth.clamp(camera.x, bounds.minX, bounds.maxX),
                Mth.clamp(camera.y, bounds.minY, bounds.maxY), Mth.clamp(camera.z, bounds.minZ, bounds.maxZ)));
        double range = (minecraft.options.getEffectiveRenderDistance() + 1) * 16.0;
        volume = gain * (float) Math.pow(Math.max(0, 1 - distance / range), 0.5);
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    void release() {
        stop();
    }
}
