package com.ghostipedia.cosmiccore.client.firmament;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.CosmicCoreClient;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public final class FirmamentAtmosphereRenderer {

    private static final float TAU = (float) (Math.PI * 2.0);
    private static final float HALF_PI = (float) (Math.PI * 0.5);
    private static final float DOME_RADIUS = 100.0f;
    private static final int DOME_LONGITUDES = 96;
    private static final int DOME_LATITUDES = 36;
    private static final int STAR_COUNT = 640;
    private static final float SEA_HORIZON_RADIUS = 92.0f;
    private static final float SEA_HORIZON_JOIN_RADIUS = 320.0f;
    private static final float SEA_HORIZON_FAR_RADIUS = 16_384.0f;
    private static final int SEA_HORIZON_SEGMENTS = 128;
    private static final int SEA_HORIZON_RINGS = 12;
    private static final float EARTH_DISTANCE = 22.0f;
    private static final float EARTH_RADIUS = 320.0f;
    private static final float EARTH_LIMB_SLOPE = EARTH_DISTANCE / EARTH_RADIUS;
    private static final int EARTH_SEGMENTS = 128;
    private static final int EARTH_RINGS = 16;
    private static final float SUN_DISTANCE = 100.0f;
    private static final float SUN_RADIUS = 27.0f;
    private static final float SUN_HALO_DISTANCE = 99.5f;
    private static final float SUN_HALO_RADIUS = 42.0f;
    private static final long STAR_SEED = 0x6E6F6374696C756DL;
    private static final long METEOR_SEED = 0x6165746865726961L;
    private static final ResourceLocation EARTH_TEXTURE = CosmicCore.id("textures/environment/earth.png");
    private static final ResourceLocation SUN_TEXTURE = ResourceLocation
            .withDefaultNamespace("textures/environment/sun.png");
    private static final SeaHorizonLayer HORIZON_SURFACE = new SeaHorizonLayer(0.0f, 0.42f);
    private static final SeaHorizonLayer HORIZON_MIDDLE = new SeaHorizonLayer(0.5f, 0.23f);
    private static final SeaHorizonLayer HORIZON_DEEP = new SeaHorizonLayer(1.0f, 0.16f);
    private static final SeaHorizonLayer[] HORIZON_HIGH = {
            HORIZON_DEEP, HORIZON_MIDDLE, HORIZON_SURFACE
    };
    private static final SeaHorizonLayer[] HORIZON_UPPER_MIDDLE = {
            HORIZON_DEEP, HORIZON_SURFACE, HORIZON_MIDDLE
    };
    private static final SeaHorizonLayer[] HORIZON_LOWER_MIDDLE = {
            HORIZON_SURFACE, HORIZON_DEEP, HORIZON_MIDDLE
    };
    private static final SeaHorizonLayer[] HORIZON_LOW = {
            HORIZON_SURFACE, HORIZON_MIDDLE, HORIZON_DEEP
    };

    private FirmamentAtmosphereRenderer() {}

    public static boolean render(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix,
                                 Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();
        if (isFoggy || blocksSky(camera)) {
            return false;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);
        Matrix4f matrix = poseStack.last().pose();
        float solarAzimuth = ((level.getDayTime() + partialTick) % 72000L) / 72000.0f * TAU;

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        FogRenderer.setupNoFog();

        try {
            renderDome(matrix, solarAzimuth);
            renderStars(matrix);
            renderSolarHalo(matrix, solarAzimuth);
            renderEarth(matrix);
            renderFirmamentSeaHorizon(level, partialTick, matrix, camera);
        } finally {
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            setupFog.run();
        }
        return true;
    }

    public static Vec3 fogColor(float brightness) {
        double light = 0.58 + 0.42 * brightness;
        return new Vec3(0.115 * light, 0.095 * light, 0.245 * light);
    }

    public static Vec3 cloudColor(ClientLevel level, float partialTick) {
        double weather = 1.0 - level.getRainLevel(partialTick) * 0.38 - level.getThunderLevel(partialTick) * 0.22;
        return new Vec3(0.52 * weather, 0.43 * weather, 0.67 * weather);
    }

    private static boolean blocksSky(Camera camera) {
        FogType fogType = camera.getFluidInCamera();
        if (fogType == FogType.LAVA || fogType == FogType.POWDER_SNOW) {
            return true;
        }
        return camera.getEntity() instanceof LivingEntity living &&
                (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS));
    }

    private static void renderDome(Matrix4f matrix, float solarAzimuth) {
        ShaderInstance shader = CosmicCoreClient.getFirmamentAtmosphereShader();
        if (shader != null) {
            renderShaderDome(matrix, solarAzimuth, shader);
            return;
        }
        renderFallbackDome(matrix, solarAzimuth);
    }

    private static void renderShaderDome(Matrix4f matrix, float solarAzimuth, ShaderInstance shader) {
        RenderSystem.setShader(() -> shader);
        var solarUniform = shader.getUniform("SolarAzimuth");
        if (solarUniform != null) {
            solarUniform.set(solarAzimuth);
        }
        var inverseViewUniform = shader.getUniform("InverseViewMat");
        if (inverseViewUniform != null) {
            inverseViewUniform.set(new Matrix4f(matrix).invert());
        }
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
        for (int latitude = 0; latitude < DOME_LATITUDES; latitude++) {
            float lower = -HALF_PI + (float) latitude / DOME_LATITUDES * (float) Math.PI;
            float upper = -HALF_PI + (float) (latitude + 1) / DOME_LATITUDES * (float) Math.PI;
            for (int longitude = 0; longitude < DOME_LONGITUDES; longitude++) {
                float left = (float) longitude / DOME_LONGITUDES * TAU;
                float right = (float) (longitude + 1) / DOME_LONGITUDES * TAU;
                addDomePosition(buffer, matrix, lower, left);
                addDomePosition(buffer, matrix, upper, left);
                addDomePosition(buffer, matrix, upper, right);
                addDomePosition(buffer, matrix, lower, left);
                addDomePosition(buffer, matrix, upper, right);
                addDomePosition(buffer, matrix, lower, right);
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addDomePosition(BufferBuilder buffer, Matrix4f matrix, float latitude, float longitude) {
        float cosLatitude = (float) Math.cos(latitude);
        buffer.addVertex(matrix,
                cosLatitude * (float) Math.cos(longitude) * DOME_RADIUS,
                (float) Math.sin(latitude) * DOME_RADIUS,
                cosLatitude * (float) Math.sin(longitude) * DOME_RADIUS);
    }

    private static void renderFallbackDome(Matrix4f matrix, float solarAzimuth) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int latitude = 0; latitude < DOME_LATITUDES; latitude++) {
            float lower = -HALF_PI + (float) latitude / DOME_LATITUDES * (float) Math.PI;
            float upper = -HALF_PI + (float) (latitude + 1) / DOME_LATITUDES * (float) Math.PI;
            for (int longitude = 0; longitude < DOME_LONGITUDES; longitude++) {
                float left = (float) longitude / DOME_LONGITUDES * TAU;
                float right = (float) (longitude + 1) / DOME_LONGITUDES * TAU;
                addDomeVertex(buffer, matrix, lower, left, solarAzimuth);
                addDomeVertex(buffer, matrix, upper, left, solarAzimuth);
                addDomeVertex(buffer, matrix, upper, right, solarAzimuth);
                addDomeVertex(buffer, matrix, lower, left, solarAzimuth);
                addDomeVertex(buffer, matrix, upper, right, solarAzimuth);
                addDomeVertex(buffer, matrix, lower, right, solarAzimuth);
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addDomeVertex(BufferBuilder buffer, Matrix4f matrix, float latitude, float longitude,
                                      float solarAzimuth) {
        float cosLatitude = (float) Math.cos(latitude);
        float normalizedY = (float) Math.sin(latitude);
        float x = cosLatitude * (float) Math.cos(longitude) * DOME_RADIUS;
        float y = normalizedY * DOME_RADIUS;
        float z = cosLatitude * (float) Math.sin(longitude) * DOME_RADIUS;

        float red;
        float green;
        float blue;
        if (normalizedY >= 0.48f) {
            float blend = (normalizedY - 0.48f) / 0.52f;
            red = mix(0.095f, 0.018f, blend);
            green = mix(0.075f, 0.020f, blend);
            blue = mix(0.225f, 0.090f, blend);
        } else if (normalizedY >= 0.0f) {
            float blend = normalizedY / 0.48f;
            red = mix(0.315f, 0.095f, blend);
            green = mix(0.135f, 0.075f, blend);
            blue = mix(0.365f, 0.225f, blend);
        } else if (normalizedY >= -0.42f) {
            float blend = -normalizedY / 0.42f;
            red = mix(0.315f, 0.025f, blend);
            green = mix(0.135f, 0.175f, blend);
            blue = mix(0.365f, 0.620f, blend);
        } else {
            float blend = (-normalizedY - 0.42f) / 0.58f;
            red = mix(0.025f, 0.008f, blend);
            green = mix(0.175f, 0.025f, blend);
            blue = mix(0.620f, 0.155f, blend);
        }

        float horizon = square(Math.max(0.0f, 1.0f - Math.abs(normalizedY) * 3.4f));
        float solarFacing = power(Math.max(0.0f, (float) Math.cos(longitude - solarAzimuth)), 6);
        float warmth = horizon * solarFacing * 0.82f;
        red = mix(red, 1.0f, warmth);
        green = mix(green, 0.40f, warmth);
        blue = mix(blue, 0.11f, warmth);

        buffer.addVertex(matrix, x, y, z)
                .setColor(channel(red), channel(green), channel(blue), 255);
    }

    private static void renderStars(Matrix4f matrix) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Random random = new Random(STAR_SEED);
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int star = 0; star < STAR_COUNT; star++) {
            double x = random.nextDouble() * 2.0 - 1.0;
            double y = 0.10 + random.nextDouble() * 0.90;
            double z = random.nextDouble() * 2.0 - 1.0;
            double length = Math.sqrt(x * x + y * y + z * z);
            x /= length;
            y /= length;
            z /= length;

            double horizontal = Math.sqrt(x * x + z * z);
            if (horizontal < 0.0001) {
                star--;
                continue;
            }
            double tangentX = -z / horizontal;
            double tangentZ = x / horizontal;
            double bitangentX = -y * tangentZ;
            double bitangentY = horizontal;
            double bitangentZ = y * tangentX;
            double radius = 91.0;
            double size = 0.045 + random.nextDouble() * 0.105;
            int warmth = random.nextInt(4);
            int red = warmth == 0 ? 255 : 208 + random.nextInt(35);
            int green = warmth == 0 ? 232 : 220 + random.nextInt(30);
            int blue = warmth == 0 ? 190 : 245;
            int alpha = 150 + random.nextInt(106);

            addStarVertex(buffer, matrix, x, y, z, tangentX, tangentZ, bitangentX, bitangentY, bitangentZ,
                    radius, -size, -size, red, green, blue, alpha);
            addStarVertex(buffer, matrix, x, y, z, tangentX, tangentZ, bitangentX, bitangentY, bitangentZ,
                    radius, size, -size, red, green, blue, alpha);
            addStarVertex(buffer, matrix, x, y, z, tangentX, tangentZ, bitangentX, bitangentY, bitangentZ,
                    radius, size, size, red, green, blue, alpha);
            addStarVertex(buffer, matrix, x, y, z, tangentX, tangentZ, bitangentX, bitangentY, bitangentZ,
                    radius, -size, -size, red, green, blue, alpha);
            addStarVertex(buffer, matrix, x, y, z, tangentX, tangentZ, bitangentX, bitangentY, bitangentZ,
                    radius, size, size, red, green, blue, alpha);
            addStarVertex(buffer, matrix, x, y, z, tangentX, tangentZ, bitangentX, bitangentY, bitangentZ,
                    radius, -size, size, red, green, blue, alpha);
            if (star % 47 == 0) {
                addCrownStar(buffer, matrix, x, y, z, tangentX, tangentZ, bitangentX, bitangentY, bitangentZ,
                        radius - 0.1, size * 3.2, red, green, blue, Math.min(220, alpha));
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addCrownStar(BufferBuilder buffer, Matrix4f matrix, double directionX, double directionY,
                                     double directionZ, double tangentX, double tangentZ, double bitangentX,
                                     double bitangentY, double bitangentZ, double radius, double size, int red,
                                     int green, int blue, int alpha) {
        addStarQuad(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, size, size * 0.11, red, green, blue, alpha);
        addStarQuad(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, size * 0.11, size, red, green, blue, alpha);
    }

    private static void addStarQuad(BufferBuilder buffer, Matrix4f matrix, double directionX, double directionY,
                                    double directionZ, double tangentX, double tangentZ, double bitangentX,
                                    double bitangentY, double bitangentZ, double radius, double tangentSize,
                                    double bitangentSize, int red, int green, int blue, int alpha) {
        addStarVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, -tangentSize, -bitangentSize, red, green, blue, alpha);
        addStarVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, tangentSize, -bitangentSize, red, green, blue, alpha);
        addStarVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, tangentSize, bitangentSize, red, green, blue, alpha);
        addStarVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, -tangentSize, -bitangentSize, red, green, blue, alpha);
        addStarVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, tangentSize, bitangentSize, red, green, blue, alpha);
        addStarVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, -tangentSize, bitangentSize, red, green, blue, alpha);
    }

    private static void addStarVertex(BufferBuilder buffer, Matrix4f matrix, double directionX, double directionY,
                                      double directionZ, double tangentX, double tangentZ, double bitangentX,
                                      double bitangentY, double bitangentZ, double radius, double tangentOffset,
                                      double bitangentOffset, int red, int green, int blue, int alpha) {
        float x = (float) (directionX * radius + tangentX * tangentOffset + bitangentX * bitangentOffset);
        float y = (float) (directionY * radius + bitangentY * bitangentOffset);
        float z = (float) (directionZ * radius + tangentZ * tangentOffset + bitangentZ * bitangentOffset);
        buffer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
    }

    private static void renderCelestialCurrents(Matrix4f matrix) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        addCelestialCurrent(buffer, matrix, 0.08f, 1.58f, 0.58f, 0.12f, 2.2f, 0.0f,
                0.075f, 82, 190, 255, 112);
        addCelestialCurrent(buffer, matrix, 1.76f, 1.34f, 0.88f, 0.08f, 2.8f, 0.9f,
                0.050f, 130, 215, 255, 88);
        addCelestialCurrent(buffer, matrix, 3.38f, 1.82f, 0.40f, 0.14f, 1.8f, 2.1f,
                0.085f, 74, 158, 255, 102);
        addCelestialCurrent(buffer, matrix, 5.18f, 0.94f, 0.70f, 0.06f, 3.4f, 1.4f,
                0.042f, 174, 228, 255, 72);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addCelestialCurrent(BufferBuilder buffer, Matrix4f matrix, float startLongitude,
                                            float longitudeSpan, float centerLatitude, float latitudeWave,
                                            float waveFrequency, float wavePhase, float halfWidth, int red,
                                            int green, int blue, int peakAlpha) {
        int segments = 48;
        for (int segment = 0; segment < segments; segment++) {
            float progressA = (float) segment / segments;
            float progressB = (float) (segment + 1) / segments;
            float longitudeA = startLongitude + longitudeSpan * progressA;
            float longitudeB = startLongitude + longitudeSpan * progressB;
            float centerA = centerLatitude +
                    latitudeWave * (float) Math.sin(progressA * waveFrequency * TAU + wavePhase);
            float centerB = centerLatitude +
                    latitudeWave * (float) Math.sin(progressB * waveFrequency * TAU + wavePhase);
            int alphaA = Math.round((float) Math.sin(progressA * Math.PI) * peakAlpha);
            int alphaB = Math.round((float) Math.sin(progressB * Math.PI) * peakAlpha);
            addVeilSection(buffer, matrix, longitudeA, longitudeB, centerA, centerB, halfWidth,
                    red, green, blue, alphaA, alphaB);
        }
    }

    private static void addVeilSection(BufferBuilder buffer, Matrix4f matrix, float longitudeA, float longitudeB,
                                       float centerA, float centerB, float halfWidth, int red, int green, int blue,
                                       int alphaA, int alphaB) {
        float coreWidth = halfWidth * 0.22f;
        addVeilHalf(buffer, matrix, longitudeA, longitudeB, centerA - halfWidth, centerB - halfWidth,
                centerA - coreWidth, centerB - coreWidth, 0, alphaA, alphaB, 0, red, green, blue);
        addVeilHalf(buffer, matrix, longitudeA, longitudeB, centerA - coreWidth, centerB - coreWidth,
                centerA + coreWidth, centerB + coreWidth, alphaA, alphaA, alphaB, alphaB, red, green, blue);
        addVeilHalf(buffer, matrix, longitudeA, longitudeB, centerA + coreWidth, centerB + coreWidth,
                centerA + halfWidth, centerB + halfWidth, alphaA, 0, 0, alphaB, red, green, blue);
    }

    private static void addVeilHalf(BufferBuilder buffer, Matrix4f matrix, float longitudeA, float longitudeB,
                                    float latitudeLowA, float latitudeLowB, float latitudeHighA, float latitudeHighB,
                                    int lowAlphaA, int highAlphaA, int highAlphaB, int lowAlphaB, int red, int green,
                                    int blue) {
        addVeilVertex(buffer, matrix, longitudeA, latitudeLowA, red, green, blue, lowAlphaA);
        addVeilVertex(buffer, matrix, longitudeA, latitudeHighA, red, green, blue, highAlphaA);
        addVeilVertex(buffer, matrix, longitudeB, latitudeHighB, red, green, blue, highAlphaB);
        addVeilVertex(buffer, matrix, longitudeA, latitudeLowA, red, green, blue, lowAlphaA);
        addVeilVertex(buffer, matrix, longitudeB, latitudeHighB, red, green, blue, highAlphaB);
        addVeilVertex(buffer, matrix, longitudeB, latitudeLowB, red, green, blue, lowAlphaB);
    }

    private static void addVeilVertex(BufferBuilder buffer, Matrix4f matrix, float longitude, float latitude,
                                      int red, int green, int blue, int alpha) {
        float cosLatitude = (float) Math.cos(latitude);
        float radius = 95.5f;
        buffer.addVertex(matrix, cosLatitude * (float) Math.cos(longitude) * radius,
                (float) Math.sin(latitude) * radius, cosLatitude * (float) Math.sin(longitude) * radius)
                .setColor(red, green, blue, alpha);
    }

    private static void renderMeteorShower(Matrix4f matrix) {
        Random random = new Random(METEOR_SEED);
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int meteor = 0; meteor < 17; meteor++) {
            double longitude = random.nextDouble() * TAU;
            double latitude = 0.28 + random.nextDouble() * 0.92;
            double directionX = Math.cos(latitude) * Math.cos(longitude);
            double directionY = Math.sin(latitude);
            double directionZ = Math.cos(latitude) * Math.sin(longitude);
            double tangentX = -Math.sin(longitude);
            double tangentZ = Math.cos(longitude);
            double bitangentX = -Math.sin(latitude) * Math.cos(longitude);
            double bitangentY = Math.cos(latitude);
            double bitangentZ = -Math.sin(latitude) * Math.sin(longitude);
            double length = 1.3 + random.nextDouble() * 2.8;
            double width = 0.055 + random.nextDouble() * 0.085;
            int alpha = 104 + random.nextInt(92);
            addMeteor(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                    bitangentX, bitangentY, bitangentZ, length, width, alpha);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addMeteor(BufferBuilder buffer, Matrix4f matrix, double directionX, double directionY,
                                  double directionZ, double tangentX, double tangentZ, double bitangentX,
                                  double bitangentY, double bitangentZ, double length, double width, int alpha) {
        addMeteorVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, -length, 0.0, 70, 172, 255, 0);
        addMeteorVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, 0.22, -width, 170, 230, 255, alpha);
        addMeteorVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, 0.22, width, 170, 230, 255, alpha);
        addStarQuad(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, 89.8, width * 2.8, width * 2.8,
                214, 244, 255, Math.min(230, alpha + 35));
    }

    private static void addMeteorVertex(BufferBuilder buffer, Matrix4f matrix, double directionX,
                                        double directionY, double directionZ, double tangentX, double tangentZ,
                                        double bitangentX, double bitangentY, double bitangentZ,
                                        double tangentOffset, double bitangentOffset, int red, int green, int blue,
                                        int alpha) {
        double radius = 90.0;
        float x = (float) (directionX * radius + tangentX * tangentOffset + bitangentX * bitangentOffset);
        float y = (float) (directionY * radius + bitangentY * bitangentOffset);
        float z = (float) (directionZ * radius + tangentZ * tangentOffset + bitangentZ * bitangentOffset);
        buffer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
    }

    private static void renderSolarHalo(Matrix4f matrix, float solarAzimuth) {
        float directionX = (float) Math.cos(solarAzimuth);
        float directionY = -0.035f;
        float directionZ = (float) Math.sin(solarAzimuth);
        float length = (float) Math.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
        directionX /= length;
        directionY /= length;
        directionZ /= length;

        float tangentX = -directionZ;
        float tangentZ = directionX;
        float bitangentX = -tangentZ * directionY;
        float bitangentY = tangentZ * directionX - tangentX * directionZ;
        float bitangentZ = tangentX * directionY;
        float bitangentLength = (float) Math.sqrt(
                bitangentX * bitangentX + bitangentY * bitangentY + bitangentZ * bitangentZ);
        bitangentX /= bitangentLength;
        bitangentY /= bitangentLength;
        bitangentZ /= bitangentLength;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        drawHalo(matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, SUN_HALO_RADIUS, 105, 0);
        renderSun(matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ);
    }

    private static void renderSolarRays(Matrix4f matrix, float directionX, float directionY, float directionZ,
                                        float tangentX, float tangentZ, float bitangentX, float bitangentY,
                                        float bitangentZ) {
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int rays = 16;
        for (int ray = 0; ray < rays; ray++) {
            float angle = (float) ray / rays * TAU;
            float length = ray % 2 == 0 ? 26.0f : 20.0f;
            float halfAngle = ray % 2 == 0 ? 0.096f : 0.064f;
            addSolarRayVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                    bitangentX, bitangentY, bitangentZ, angle - halfAngle, 5.5f, 38);
            addSolarRayVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                    bitangentX, bitangentY, bitangentZ, angle + halfAngle, 5.5f, 38);
            addSolarRayVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                    bitangentX, bitangentY, bitangentZ, angle, length, 0);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addSolarRayVertex(BufferBuilder buffer, Matrix4f matrix, float directionX, float directionY,
                                          float directionZ, float tangentX, float tangentZ, float bitangentX,
                                          float bitangentY, float bitangentZ, float angle, float radius, int alpha) {
        float tangentOffset = (float) Math.cos(angle) * radius;
        float bitangentOffset = (float) Math.sin(angle) * radius;
        float distance = 89.1f;
        float x = directionX * distance + tangentX * tangentOffset + bitangentX * bitangentOffset;
        float y = directionY * distance + bitangentY * bitangentOffset;
        float z = directionZ * distance + tangentZ * tangentOffset + bitangentZ * bitangentOffset;
        buffer.addVertex(matrix, x, y, z).setColor(255, 206, 146, alpha);
    }

    private static void renderEarth(Matrix4f matrix) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int ring = 0; ring < EARTH_RINGS; ring++) {
            float innerProgress = (float) ring / EARTH_RINGS;
            float outerProgress = (float) (ring + 1) / EARTH_RINGS;
            for (int segment = 0; segment < EARTH_SEGMENTS; segment++) {
                float angleA = (float) segment / EARTH_SEGMENTS * TAU;
                float angleB = (float) (segment + 1) / EARTH_SEGMENTS * TAU;
                addEarthVertex(buffer, matrix, innerProgress, angleA);
                addEarthVertex(buffer, matrix, outerProgress, angleA);
                addEarthVertex(buffer, matrix, outerProgress, angleB);
                addEarthVertex(buffer, matrix, innerProgress, angleB);
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addEarthVertex(BufferBuilder buffer, Matrix4f matrix, float progress, float angle) {
        float directionX = (float) Math.cos(angle);
        float directionZ = (float) Math.sin(angle);
        float radialDistance = progress * EARTH_RADIUS;
        float projectionScale = SEA_HORIZON_RADIUS /
                (float) Math.sqrt(radialDistance * radialDistance + EARTH_DISTANCE * EARTH_DISTANCE);
        float x = directionX * radialDistance * projectionScale;
        float y = -EARTH_DISTANCE * projectionScale;
        float z = directionZ * radialDistance * projectionScale;
        float u = 0.5f + directionX * progress * 0.5f;
        float v = 0.5f + directionZ * progress * 0.5f;
        buffer.addVertex(matrix, x, y, z).setUv(u, v);
    }

    private static void renderFirmamentSeaHorizon(ClientLevel level, float partialTick, Matrix4f matrix,
                                                  Camera camera) {
        ShaderInstance shader = CosmicCoreClient.getFirmamentStormCurrentShader();
        if (shader == null) return;

        Vec3 cameraPosition = camera.getPosition();
        float passFade = FirmamentSightWallRenderer.seaVisibility(cameraPosition.y);
        if (passFade <= 0.01f) return;

        RenderSystem.setShader(() -> shader);
        var stormTimeUniform = shader.getUniform("StormTime");
        if (stormTimeUniform != null) {
            stormTimeUniform.set(FirmamentSightWallRenderer.stormTime(level.getGameTime(), partialTick));
        }
        var cameraUniform = shader.getUniform("CameraXZ");
        if (cameraUniform != null) {
            cameraUniform.set((float) cameraPosition.x, (float) cameraPosition.z);
        }
        var cameraYUniform = shader.getUniform("CameraY");
        if (cameraYUniform != null) {
            cameraYUniform.set((float) cameraPosition.y);
        }
        var edgeRadiusUniform = shader.getUniform("EdgeRadius");
        if (edgeRadiusUniform != null) {
            edgeRadiusUniform.set(SEA_HORIZON_JOIN_RADIUS);
        }
        var horizonPassUniform = shader.getUniform("HorizonPass");
        if (horizonPassUniform != null) {
            horizonPassUniform.set(1.0f);
        }

        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        SeaHorizonLayer[] layers = horizonLayers(cameraPosition.y);
        for (SeaHorizonLayer layer : layers) {
            addSeaHorizonLayer(buffer, matrix, cameraPosition, passFade, layer);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        if (horizonPassUniform != null) {
            horizonPassUniform.set(0.0f);
        }
    }

    private static SeaHorizonLayer[] horizonLayers(double cameraY) {
        return switch (FirmamentSightWallRenderer.layerOrder(cameraY)) {
            case 0 -> HORIZON_HIGH;
            case 1 -> HORIZON_UPPER_MIDDLE;
            case 2 -> HORIZON_LOWER_MIDDLE;
            default -> HORIZON_LOW;
        };
    }

    private static void addSeaHorizonLayer(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPosition,
                                           float passFade, SeaHorizonLayer layer) {
        int depth = Math.round(layer.depth() * 255.0f);
        int alpha = Math.round(layer.alpha() * passFade * 255.0f);
        for (int ring = 0; ring < SEA_HORIZON_RINGS; ring++) {
            float innerProgress = (float) ring / SEA_HORIZON_RINGS;
            float outerProgress = (float) (ring + 1) / SEA_HORIZON_RINGS;
            for (int segment = 0; segment < SEA_HORIZON_SEGMENTS; segment++) {
                float angleA = (float) segment / SEA_HORIZON_SEGMENTS * TAU;
                float angleB = (float) (segment + 1) / SEA_HORIZON_SEGMENTS * TAU;
                addSeaHorizonVertex(buffer, matrix, cameraPosition, layer.depth(), depth, alpha,
                        innerProgress, angleA);
                addSeaHorizonVertex(buffer, matrix, cameraPosition, layer.depth(), depth, alpha,
                        outerProgress, angleA);
                addSeaHorizonVertex(buffer, matrix, cameraPosition, layer.depth(), depth, alpha,
                        outerProgress, angleB);
                addSeaHorizonVertex(buffer, matrix, cameraPosition, layer.depth(), depth, alpha,
                        innerProgress, angleB);
            }
        }
    }

    private static void addSeaHorizonVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPosition,
                                            float layerDepth, int depth, int alpha, float progress, float angle) {
        float easedProgress = progress * progress * progress;
        float worldRadius = mix(SEA_HORIZON_JOIN_RADIUS, SEA_HORIZON_FAR_RADIUS, easedProgress);
        float directionX = (float) Math.cos(angle);
        float directionZ = (float) Math.sin(angle);
        float layerBase = FirmamentSightWallRenderer.layerBase(layerDepth);
        float flatRelativeY = FirmamentSightWallRenderer.SEA_Y + layerBase - (float) cameraPosition.y;
        float bend = progress * progress * (3.0f - 2.0f * progress);
        float limbRelativeY = -worldRadius * EARTH_LIMB_SLOPE;
        float relativeY = mix(flatRelativeY, limbRelativeY, bend);
        float projectionScale = SEA_HORIZON_RADIUS /
                (float) Math.sqrt(worldRadius * worldRadius + relativeY * relativeY);
        float x = directionX * worldRadius * projectionScale;
        float y = relativeY * projectionScale;
        float z = directionZ * worldRadius * projectionScale;
        float worldX = (float) cameraPosition.x + directionX * worldRadius;
        float worldZ = (float) cameraPosition.z + directionZ * worldRadius;
        buffer.addVertex(matrix, x, y, z)
                .setUv(worldX, worldZ)
                .setColor(depth, Math.round(progress * 255.0f), 255, alpha);
    }

    private static void renderFirmamentSea(Matrix4f matrix) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int segments = 128;
        float distance = 4.0f;
        for (int segment = 0; segment < segments; segment++) {
            float angleA = (float) segment / segments * TAU;
            float angleB = (float) (segment + 1) / segments * TAU;
            addSeaBand(buffer, matrix, distance, angleA, angleB, 0.0f, 32.0f,
                    15, 72, 174, 46, 24, 100, 207, 58);
            addSeaBand(buffer, matrix, distance, angleA, angleB, 32.0f, 66.0f,
                    24, 100, 207, 58, 46, 142, 230, 82);
            addSeaBand(buffer, matrix, distance, angleA, angleB, 66.0f, 95.0f,
                    46, 142, 230, 82, 91, 205, 250, 122);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        renderFirmamentSeaWall(matrix);
    }

    private static void addSeaBand(BufferBuilder buffer, Matrix4f matrix, float distance, float angleA,
                                   float angleB, float innerRadius, float outerRadius, int innerRed,
                                   int innerGreen, int innerBlue, int innerAlpha, int outerRed, int outerGreen,
                                   int outerBlue, int outerAlpha) {
        addSeaVertex(buffer, matrix, distance, angleA, innerRadius, innerRed, innerGreen, innerBlue, innerAlpha);
        addSeaVertex(buffer, matrix, distance, angleB, innerRadius, innerRed, innerGreen, innerBlue, innerAlpha);
        addSeaVertex(buffer, matrix, distance, angleB, outerRadius, outerRed, outerGreen, outerBlue, outerAlpha);
        addSeaVertex(buffer, matrix, distance, angleA, innerRadius, innerRed, innerGreen, innerBlue, innerAlpha);
        addSeaVertex(buffer, matrix, distance, angleB, outerRadius, outerRed, outerGreen, outerBlue, outerAlpha);
        addSeaVertex(buffer, matrix, distance, angleA, outerRadius, outerRed, outerGreen, outerBlue, outerAlpha);
    }

    private static void addSeaVertex(BufferBuilder buffer, Matrix4f matrix, float distance, float angle,
                                     float radius, int red, int green, int blue, int alpha) {
        buffer.addVertex(matrix, (float) Math.cos(angle) * radius, -distance, (float) Math.sin(angle) * radius)
                .setColor(red, green, blue, alpha);
    }

    private static void renderFirmamentSeaWall(Matrix4f matrix) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int segments = 128;
        float radius = 95.0f;
        for (int segment = 0; segment < segments; segment++) {
            float angleA = (float) segment / segments * TAU;
            float angleB = (float) (segment + 1) / segments * TAU;
            addSeaWallBand(buffer, matrix, angleA, angleB, radius, 8.0f, 2.0f,
                    119, 225, 255, 0, 82, 207, 255, 146);
            addSeaWallBand(buffer, matrix, angleA, angleB, radius, 2.0f, -12.0f,
                    82, 207, 255, 146, 27, 116, 218, 88);
            addSeaWallBand(buffer, matrix, angleA, angleB, radius, -12.0f, -92.0f,
                    27, 116, 218, 88, 7, 35, 112, 18);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addSeaWallBand(BufferBuilder buffer, Matrix4f matrix, float angleA, float angleB,
                                       float radius, float top, float bottom, int topRed, int topGreen, int topBlue,
                                       int topAlpha, int bottomRed, int bottomGreen, int bottomBlue, int bottomAlpha) {
        addSeaWallVertex(buffer, matrix, angleA, radius, top, topRed, topGreen, topBlue, topAlpha);
        addSeaWallVertex(buffer, matrix, angleB, radius, top, topRed, topGreen, topBlue, topAlpha);
        addSeaWallVertex(buffer, matrix, angleB, radius, bottom,
                bottomRed, bottomGreen, bottomBlue, bottomAlpha);
        addSeaWallVertex(buffer, matrix, angleA, radius, bottom,
                bottomRed, bottomGreen, bottomBlue, bottomAlpha);
    }

    private static void addSeaWallVertex(BufferBuilder buffer, Matrix4f matrix, float angle, float radius, float y,
                                         int red, int green, int blue, int alpha) {
        buffer.addVertex(matrix, (float) Math.cos(angle) * radius, y, (float) Math.sin(angle) * radius)
                .setColor(red, green, blue, alpha);
    }

    private static void renderSun(Matrix4f matrix, float directionX, float directionY, float directionZ,
                                  float tangentX, float tangentZ, float bitangentX, float bitangentY,
                                  float bitangentZ) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float radius = SUN_RADIUS;
        addCelestialTextureVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, -radius, -radius, 0.0f, 1.0f);
        addCelestialTextureVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, -radius, 1.0f, 1.0f);
        addCelestialTextureVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, radius, radius, 1.0f, 0.0f);
        addCelestialTextureVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                bitangentX, bitangentY, bitangentZ, -radius, radius, 0.0f, 0.0f);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.defaultBlendFunc();
    }

    private static void addCelestialTextureVertex(BufferBuilder buffer, Matrix4f matrix, float directionX,
                                                  float directionY, float directionZ, float tangentX, float tangentZ,
                                                  float bitangentX, float bitangentY, float bitangentZ,
                                                  float tangentOffset, float bitangentOffset, float u, float v) {
        float distance = SUN_DISTANCE;
        float x = directionX * distance + tangentX * tangentOffset + bitangentX * bitangentOffset;
        float y = directionY * distance + bitangentY * bitangentOffset;
        float z = directionZ * distance + tangentZ * tangentOffset + bitangentZ * bitangentOffset;
        buffer.addVertex(matrix, x, y, z).setUv(u, v);
    }

    private static void drawHalo(Matrix4f matrix, float directionX, float directionY, float directionZ,
                                 float tangentX, float tangentZ, float bitangentX, float bitangentY,
                                 float bitangentZ, float radius, int centerAlpha, int edgeAlpha) {
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int segments = 48;
        for (int segment = 0; segment < segments; segment++) {
            float angleA = (float) segment / segments * TAU;
            float angleB = (float) (segment + 1) / segments * TAU;
            addHaloVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                    bitangentX, bitangentY, bitangentZ, 0.0f, 0.0f, centerAlpha);
            addHaloVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                    bitangentX, bitangentY, bitangentZ, (float) Math.cos(angleA) * radius,
                    (float) Math.sin(angleA) * radius, edgeAlpha);
            addHaloVertex(buffer, matrix, directionX, directionY, directionZ, tangentX, tangentZ,
                    bitangentX, bitangentY, bitangentZ, (float) Math.cos(angleB) * radius,
                    (float) Math.sin(angleB) * radius, edgeAlpha);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addHaloVertex(BufferBuilder buffer, Matrix4f matrix, float directionX, float directionY,
                                      float directionZ, float tangentX, float tangentZ, float bitangentX,
                                      float bitangentY, float bitangentZ, float tangentOffset, float bitangentOffset,
                                      int alpha) {
        float distance = SUN_HALO_DISTANCE;
        float x = directionX * distance + tangentX * tangentOffset + bitangentX * bitangentOffset;
        float y = directionY * distance + bitangentY * bitangentOffset;
        float z = directionZ * distance + tangentZ * tangentOffset + bitangentZ * bitangentOffset;
        buffer.addVertex(matrix, x, y, z).setColor(255, 176, 104, alpha);
    }

    private static float mix(float start, float end, float amount) {
        return start + (end - start) * amount;
    }

    private static float square(float value) {
        return value * value;
    }

    private static float power(float value, int exponent) {
        float result = 1.0f;
        for (int i = 0; i < exponent; i++) {
            result *= value;
        }
        return result;
    }

    private static int channel(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255.0f)));
    }

    private record SeaHorizonLayer(float depth, float alpha) {}
}
