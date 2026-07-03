package com.ghostipedia.cosmiccore.client.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class MurkbloomClientState {

    private MurkbloomClientState() {}

    public static final int DORMANT = 0;
    public static final int RIPPLE = 1;
    public static final int STIRRING = 2;
    public static final int RISING = 3;
    public static final int TAKEN = 4;

    private static int stirLevel = DORMANT;
    private static float intensity = 0f;
    private static float bloomDensity = 0f;
    private static float lastLoudYaw = 0f;
    private static float flinchPulse = 0f;
    private static long clientTicks = 0;
    private static boolean serverDriven = false;

    private static final int MEMORY_SIZE = 8;
    private static final byte[] impulseMemory = new byte[MEMORY_SIZE];
    private static final long[] impulseMemoryAt = new long[MEMORY_SIZE];
    private static int impulseMemoryIdx = 0;
    private static long lullUntil = -1;
    private static long nextLullRoll = 0;

    public static void applySync(int stir, float density, float loudYaw) {
        stirLevel = Mth.clamp(stir, DORMANT, TAKEN);
        bloomDensity = Mth.clamp(density, 0f, 1f);
        lastLoudYaw = loudYaw;
        serverDriven = true;
    }

    public static void setStir(int stir) {
        stirLevel = Mth.clamp(stir, DORMANT, TAKEN);
        serverDriven = false;
    }

    public static void flinch(float strength) {
        flinchPulse = Math.max(flinchPulse, strength);
    }

    public static int stir() {
        return stirLevel;
    }

    public static float intensity() {
        return intensity;
    }

    public static float bloomDensity() {
        return bloomDensity;
    }

    public static float lastLoudYaw() {
        return lastLoudYaw;
    }

    public static float consumeFlinch() {
        float f = flinchPulse;
        flinchPulse = 0f;
        return f;
    }

    public static void recordImpulse(int kind) {
        impulseMemory[impulseMemoryIdx] = (byte) kind;
        impulseMemoryAt[impulseMemoryIdx] = clientTicks;
        impulseMemoryIdx = (impulseMemoryIdx + 1) % MEMORY_SIZE;
    }

    public static int randomRememberedImpulse(RandomSource random, long maxAge) {
        int tries = MEMORY_SIZE;
        while (tries-- > 0) {
            int i = random.nextInt(MEMORY_SIZE);
            if (impulseMemory[i] > 0 && clientTicks - impulseMemoryAt[i] < maxAge) {
                return impulseMemory[i];
            }
        }
        return 0;
    }

    public static float peekFlinch() {
        return flinchPulse;
    }

    public static long ticks() {
        return clientTicks;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        clientTicks++;
        float target = serverDriven ? bloomDensity : stirLevel / 4f;
        float wobble = (float) Math.sin(clientTicks * 0.011) * 0.03f;

        if (stirLevel == RISING && clientTicks >= nextLullRoll) {
            nextLullRoll = clientTicks + 1200 + (long) (Math.random() * 1400);
            if (Math.random() < 0.45) {
                lullUntil = clientTicks + 70 + (long) (Math.random() * 50);
            }
        }
        if (lullUntil > 0 && clientTicks >= lullUntil) {
            lullUntil = -1;
            flinchPulse = Math.max(flinchPulse, 1.0f);
        }
        if (lullUntil > 0) {
            target *= 0.62f;
        }

        intensity = Mth.lerp(0.04f, intensity, Mth.clamp(target + wobble, 0f, 1f));
        flinchPulse *= 0.90f;
    }
}
