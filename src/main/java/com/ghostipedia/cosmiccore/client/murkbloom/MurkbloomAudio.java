package com.ghostipedia.cosmiccore.client.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class MurkbloomAudio {

    private MurkbloomAudio() {}

    // TODO cosmiccore-100: vanilla placeholders below
    private static final ResourceLocation CLICKING = ResourceLocation.parse("block.sculk_sensor.clicking");
    private static final ResourceLocation HEARTBEAT = ResourceLocation.parse("entity.warden.heartbeat");
    private static final ResourceLocation SWELL = ResourceLocation
            .parse("ambient.underwater.loop.additions.ultra_rare");
    private static final ResourceLocation CHURN = ResourceLocation.parse("block.bubble_column.upwards_ambient");
    private static final ResourceLocation SQUELCH = ResourceLocation.parse("block.sculk.spread");
    private static final ResourceLocation SQUIRT = ResourceLocation.parse("entity.squid.squirt");
    private static final ResourceLocation SWIMMER = ResourceLocation.parse("entity.drowned.swim");
    private static final ResourceLocation MIMIC_BREAK = ResourceLocation.parse("block.stone.break");
    private static final ResourceLocation MIMIC_PLACE = ResourceLocation.parse("block.stone.place");
    private static final ResourceLocation MIMIC_EAT = ResourceLocation.parse("entity.generic.eat");
    private static final ResourceLocation MIMIC_HIT = ResourceLocation.parse("entity.player.attack.sweep");
    private static final ResourceLocation MIMIC_SONAR = ResourceLocation.parse("block.amethyst_block.resonate");

    private static final float WATER_GATE = 0.30f;
    private static final float SUIT_GATE = 0.62f;

    private static final RandomSource RANDOM = RandomSource.create();
    private static int lastStir = 0;
    private static long nextIdle = 0;
    private static long nextMimic = 0;
    private static long nextSwimmer = 0;
    private static long nextEarWhisper = 0;
    private static long nextVisor = 0;
    private static long nextBeat = 0;
    private static long nextSwell = 0;
    private static long doubleBeatAt = -1;
    private static long pendingEchoAt = -1;
    private static Vec3 pendingEchoPos = null;
    private static long lastEchoRoll = 0;
    private static long lastSquirt = 0;
    private static long postEscapeAt = -1;
    private static int lastIdleVoice = -1;

    private static ResourceLocation burstSound = null;
    private static int burstRemaining = 0;
    private static long burstNextAt = 0;
    private static Vec3 burstPos = null;
    private static float burstVol = 0;
    private static float burstPitch = 1f;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        if (!AbyssClientFog.inHollowWater(mc)) return;

        float intensity = MurkbloomClientState.intensity();
        int stir = MurkbloomClientState.stir();
        long now = MurkbloomClientState.ticks();
        Vec3 ear = mc.player.getEyePosition();

        if (stir != lastStir) {
            onTransition(mc, lastStir, stir, now);
            lastStir = stir;
        }

        tickBurst(mc, now);

        if (pendingEchoAt >= 0 && now >= pendingEchoAt) {
            play(mc, pendingEchoPos != null ? pendingEchoPos : soundPos(mc, stir, 14, 26), CLICKING,
                    0.09f, 0.55f + RANDOM.nextFloat() * 0.2f);
            pendingEchoAt = -1;
            pendingEchoPos = null;
        }

        if (postEscapeAt >= 0 && now >= postEscapeAt) {
            if (stir <= 2) {
                startBurst(CLICKING, 3, soundPos(mc, stir, 20, 34), 0.07f, 0.6f);
            }
            postEscapeAt = -1;
        }

        boolean waterVoices = stir >= MurkbloomClientState.STIRRING && stir < MurkbloomClientState.TAKEN &&
                intensity > WATER_GATE;

        float flinch = MurkbloomClientState.peekFlinch();
        if (waterVoices && flinch > 0.4f && now - lastEchoRoll > 15) {
            lastEchoRoll = now;
            if (RANDOM.nextFloat() < 0.55f) {
                pendingEchoAt = now + 30 + RANDOM.nextInt(50);
                pendingEchoPos = soundPos(mc, stir, 10, 22);
            }
        }
        if (stir >= MurkbloomClientState.RISING && flinch > 0.9f && now - lastSquirt > 40) {
            lastSquirt = now;
            play(mc, soundPos(mc, stir, 6, 14), SQUIRT, 0.22f, 0.72f + RANDOM.nextFloat() * 0.2f);
        }

        if (waterVoices) {
            if (now >= nextIdle) {
                idleWhisper(mc, stir, intensity);
                int gap = (int) Mth.lerp(intensity, 900f, 400f);
                nextIdle = now + gap + RANDOM.nextInt(300);
            }
            if (now >= nextMimic) {
                int kind = MurkbloomClientState.randomRememberedImpulse(RANDOM, 2400);
                if (kind > 0) {
                    play(mc, soundPos(mc, stir, 12, 25), mimicSound(kind), 0.15f,
                            0.5f + RANDOM.nextFloat() * 0.15f);
                }
                nextMimic = now + 600 + RANDOM.nextInt(1000);
            }
            if (now >= nextSwimmer) {
                startBurst(SWIMMER, 4 + RANDOM.nextInt(3), rearPos(mc, 15 + RANDOM.nextFloat() * 15), 0.12f,
                        0.75f + RANDOM.nextFloat() * 0.15f);
                nextSwimmer = now + 900 + RANDOM.nextInt(1500);
            }
            if (stir >= MurkbloomClientState.RISING && now >= nextEarWhisper) {
                play(mc, ear, CLICKING, 0.045f, 1.75f);
                nextEarWhisper = now + 1200 + RANDOM.nextInt(1800);
            }
        }

        if (intensity > SUIT_GATE) {
            if (now >= nextVisor) {
                if (stir >= MurkbloomClientState.TAKEN) {
                    play(mc, ear, CLICKING, 0.07f, 1.3f);
                    nextVisor = now + 60 + RANDOM.nextInt(30);
                } else if (RANDOM.nextFloat() < 0.08f) {
                    startBurst(CLICKING, 3, ear, 0.10f, 1.55f);
                    nextVisor = now + 130 + RANDOM.nextInt(60);
                } else {
                    play(mc, ear, CLICKING, 0.08f, 1.35f + RANDOM.nextFloat() * 0.1f);
                    nextVisor = now + (int) Mth.lerp(intensity, 44f, 24f);
                }
            }
            if (doubleBeatAt >= 0 && now >= doubleBeatAt) {
                play(mc, ear, HEARTBEAT, 0.10f + 0.10f * intensity, 0.52f);
                doubleBeatAt = -1;
            }
            if (now >= nextBeat) {
                play(mc, ear, HEARTBEAT, 0.08f + 0.10f * intensity, 0.5f);
                int interval = (int) Mth.lerp(intensity, 120f, 70f);
                float roll = RANDOM.nextFloat();
                if (roll < 0.06f) {
                    interval = (int) (interval * 1.8f);
                } else if (roll < 0.12f) {
                    doubleBeatAt = now + 4;
                }
                nextBeat = now + interval;
            }
        }

        if (intensity > 0.12f && stir < MurkbloomClientState.TAKEN && now >= nextSwell) {
            play(mc, ear, SWELL, 0.18f + 0.10f * intensity, 0.5f + RANDOM.nextFloat() * 0.12f);
            nextSwell = now + 500 + RANDOM.nextInt(500);
        }
    }

    private static void onTransition(Minecraft mc, int from, int to, long now) {
        if (to == MurkbloomClientState.STIRRING && from < MurkbloomClientState.STIRRING) {
            startBurst(CLICKING, 3, soundPos(mc, to, 22, 34), 0.08f, 0.6f);
        } else if (to == MurkbloomClientState.RISING && from < MurkbloomClientState.RISING) {
            play(mc, soundPos(mc, to, 8, 16), CHURN, 0.18f, 0.5f);
            pendingEchoAt = now + 12;
            pendingEchoPos = soundPos(mc, to, 6, 12);
        } else if (to == MurkbloomClientState.TAKEN) {
            burstRemaining = 0;
            pendingEchoAt = -1;
            nextVisor = now + 30;
        }
        if (from >= MurkbloomClientState.RISING && to <= MurkbloomClientState.RIPPLE) {
            postEscapeAt = now + 200 + RANDOM.nextInt(100);
        }
    }

    private static void idleWhisper(Minecraft mc, int stir, float intensity) {
        int voice;
        do {
            float roll = RANDOM.nextFloat();
            voice = roll < 0.6f ? 0 : roll < 0.85f ? 1 : 2;
        } while (voice == lastIdleVoice && RANDOM.nextBoolean());
        lastIdleVoice = voice;
        Vec3 pos = soundPos(mc, stir, 14, 30);
        switch (voice) {
            case 0 -> play(mc, pos, CLICKING, 0.07f + 0.04f * intensity, 0.55f + RANDOM.nextFloat() * 0.25f);
            case 1 -> play(mc, pos, CHURN, 0.10f + 0.05f * intensity, 0.5f + RANDOM.nextFloat() * 0.2f);
            default -> play(mc, pos, SQUELCH, 0.06f + 0.04f * intensity, 0.6f + RANDOM.nextFloat() * 0.3f);
        }
    }

    private static void tickBurst(Minecraft mc, long now) {
        if (burstRemaining <= 0 || now < burstNextAt) return;
        play(mc, burstPos, burstSound, burstVol, burstPitch + RANDOM.nextFloat() * 0.1f);
        burstRemaining--;
        burstNextAt = now + 4 + RANDOM.nextInt(4);
    }

    private static void startBurst(ResourceLocation sound, int count, Vec3 pos, float vol, float pitch) {
        burstSound = sound;
        burstRemaining = count;
        burstNextAt = 0;
        burstPos = pos;
        burstVol = vol;
        burstPitch = pitch;
    }

    private static Vec3 soundPos(Minecraft mc, int stir, float minDist, float maxDist) {
        if (stir >= MurkbloomClientState.RISING && RANDOM.nextFloat() < 0.65f) {
            return rearPos(mc, minDist + RANDOM.nextFloat() * (maxDist - minDist));
        }
        Vec3 wisp = MurkWispRenderer.randomWispPos();
        if (wisp != null) return wisp;
        return rearPos(mc, minDist + RANDOM.nextFloat() * (maxDist - minDist));
    }

    private static Vec3 rearPos(Minecraft mc, float dist) {
        float yaw = (mc.player.getYRot() + 180f + (RANDOM.nextFloat() - 0.5f) * 140f) * Mth.DEG_TO_RAD;
        double dx = -Mth.sin(yaw) * dist;
        double dz = Mth.cos(yaw) * dist;
        double dy = (RANDOM.nextFloat() - 0.5f) * dist * 0.4;
        return mc.player.getEyePosition().add(dx, dy, dz);
    }

    private static ResourceLocation mimicSound(int kind) {
        return switch (kind) {
            case 1 -> MIMIC_BREAK;
            case 2 -> MIMIC_PLACE;
            case 3 -> MIMIC_EAT;
            case 4 -> MIMIC_HIT;
            default -> MIMIC_SONAR;
        };
    }

    private static void play(Minecraft mc, Vec3 pos, ResourceLocation id, float volume, float pitch) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(id);
        if (sound == null || mc.level == null || pos == null) return;
        mc.level.playLocalSound(pos.x, pos.y, pos.z, sound, SoundSource.AMBIENT, volume, pitch, false);
    }
}
