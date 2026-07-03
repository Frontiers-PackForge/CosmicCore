package com.ghostipedia.cosmiccore.client.murkbloom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

public class MurkParticle extends TextureSheetParticle {

    private final float baseSize;
    private final float peakAlpha;
    private final float spin;

    protected MurkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                           SpriteSet sprites, float sizeMin, float sizeMax, float alphaPeak, int lifeMin,
                           int lifeSpan, int colorA, int colorB) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.baseSize = Mth.lerp(random.nextFloat(), sizeMin, sizeMax);
        this.peakAlpha = alphaPeak * (0.8f + random.nextFloat() * 0.2f);
        this.spin = (random.nextFloat() - 0.5f) * 0.06f;
        this.lifetime = lifeMin + random.nextInt(lifeSpan);
        this.gravity = 0f;
        this.friction = 0.94f;
        this.hasPhysics = false;
        this.quadSize = baseSize;
        this.alpha = 0f;
        float mix = random.nextFloat();
        setColor(
                Mth.lerp(mix, ((colorA >> 16) & 0xFF) / 255f, ((colorB >> 16) & 0xFF) / 255f),
                Mth.lerp(mix, ((colorA >> 8) & 0xFF) / 255f, ((colorB >> 8) & 0xFF) / 255f),
                Mth.lerp(mix, (colorA & 0xFF) / 255f, (colorB & 0xFF) / 255f));
        this.roll = random.nextFloat() * (float) Math.PI * 2f;
        this.oRoll = this.roll;
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += spin;
        float life = age / (float) lifetime;
        this.quadSize = baseSize * (1f + 0.7f * life);
        float in = Mth.clamp(life / 0.18f, 0f, 1f);
        float out = Mth.clamp((1f - life) / 0.35f, 0f, 1f);
        this.alpha = peakAlpha * in * out;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class BodyProvider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public BodyProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new MurkParticle(level, x, y, z, xd, yd, zd, sprites, 0.9f, 2.4f, 0.38f, 26, 18,
                    0x05080C, 0x141C26);
        }
    }

    public static class PaleProvider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public PaleProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new MurkParticle(level, x, y, z, xd, yd, zd, sprites, 0.6f, 1.6f, 0.20f, 20, 14,
                    0x4A5A64, 0x76868E);
        }
    }

    public static class MoteProvider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public MoteProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new MurkParticle(level, x, y, z, xd, yd, zd, sprites, 0.12f, 0.30f, 0.45f, 16, 12,
                    0x0B0F16, 0x1E2A38);
        }
    }
}
