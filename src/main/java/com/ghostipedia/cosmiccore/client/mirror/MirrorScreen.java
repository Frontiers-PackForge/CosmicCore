package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.MirrorWeavePacket;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class MirrorScreen extends Screen {

    public static final int CEREMONY_TICKS = 160;
    public static final int ECHO_CAP = 12;
    public static final int HOLD_TICKS = 22;
    public static final int SKEIN_CAP = 6;

    public static KeyMapping OPEN;

    public static final class DevState {

        public int litEchoes;
        public int dimEchoes;
        public boolean scorch;
        public boolean claimable;
        public int coils;
        public boolean ceremonyActive;
        public int ceremonyProgress;
        public int hoverEcho = -1;
        public float veil;
        public int flashTicks;
        public boolean holding;
        public int holdTicks;
        public int burstTicks;
        public int burstSlot = -1;
        public int skeins;
        public int skeinBurstTicks;
        public float awaken;
        public int heartStage;
        public boolean heartHolding;
        public int heartHoldTicks;
        public boolean heartClaimed;
        public int heartBurstTicks;
        public int devSkeins;
        public int devDim;

        public DevState() {
            reset();
        }

        public void reset() {
            scorch = true;
            claimable = false;
            ceremonyActive = false;
            ceremonyProgress = 0;
            veil = 0f;
            flashTicks = 0;
            holding = false;
            holdTicks = 0;
            burstTicks = 0;
            burstSlot = -1;
            skeinBurstTicks = 0;
            awaken = 0f;
            heartStage = 0;
            heartHolding = false;
            heartHoldTicks = 0;
            heartBurstTicks = 0;
            devSkeins = 0;
            devDim = 0;
        }
    }

    private final DevState state = new DevState();
    private float time = 0f;
    private float zoom = 1f;
    private float targetZoom = 1f;
    private long lastMillis = -1L;
    private int lastHover = -1;
    private int lastWovenBody = -1;
    private int lastSkeins;
    private int lastLit;
    private boolean lastHeart;

    private void deriveFromLedger() {
        var woven = ClientDeedCache.woven();
        boolean heartWoven = woven.contains(DeedRegistry.THE_ADDRESS.id());
        int wovenBody = woven.size() - (heartWoven ? 1 : 0);
        int dSkeins = Math.min(SKEIN_CAP, wovenBody / ECHO_CAP);
        int dLit = Math.min(ECHO_CAP, wovenBody - dSkeins * ECHO_CAP);
        int pending = 0;
        for (var id : ClientDeedCache.pending()) {
            if (!id.equals(DeedRegistry.THE_ADDRESS.id())) pending++;
        }

        state.skeins = Math.min(SKEIN_CAP, dSkeins + state.devSkeins);
        state.litEchoes = dLit;
        state.dimEchoes = Math.min(ECHO_CAP - dLit, pending + state.devDim);
        state.coils = pending;
        state.heartClaimed = heartWoven;

        if (lastWovenBody >= 0) {
            if (dSkeins > lastSkeins) {
                state.skeinBurstTicks = 20;
                MirrorSounds.compress();
            } else if (dLit > lastLit) {
                state.burstTicks = 26;
                state.burstSlot = dLit - 1;
                MirrorSounds.claim();
            }
            if (heartWoven && !lastHeart) {
                state.heartBurstTicks = 40;
                MirrorSounds.heartClaim();
            }
        }
        lastWovenBody = wovenBody;
        lastSkeins = dSkeins;
        lastLit = dLit;
        lastHeart = heartWoven;
    }

    public MirrorScreen() {
        super(Component.translatableWithFallback("screen.cosmiccore.mirror", "Sol's Mirror"));
        MirrorSounds.open();
    }

    @Override
    public void tick() {
        if (state.ceremonyActive && state.ceremonyProgress < CEREMONY_TICKS) {
            state.ceremonyProgress++;
            if (state.ceremonyProgress >= CEREMONY_TICKS) {
                state.claimable = true;
                state.flashTicks = 24;
                MirrorSounds.weaveComplete();
            }
        }
        if (state.flashTicks > 0) {
            state.flashTicks--;
        }
        if (state.burstTicks > 0) {
            state.burstTicks--;
        }
        if (state.skeinBurstTicks > 0) {
            state.skeinBurstTicks--;
        }
        if (state.heartBurstTicks > 0) {
            state.heartBurstTicks--;
        }
        if (state.heartHolding) {
            if (!heartClaimable()) {
                state.heartHolding = false;
                state.heartHoldTicks = 0;
            } else {
                state.heartHoldTicks++;
                if (state.heartHoldTicks % 3 == 0 && state.heartHoldTicks < HOLD_TICKS) {
                    MirrorSounds.holdTick(state.heartHoldTicks / (float) HOLD_TICKS);
                }
                if (state.heartHoldTicks >= HOLD_TICKS) {
                    state.heartStage++;
                    state.heartHolding = false;
                    state.heartHoldTicks = 0;
                    if (state.heartStage >= 3) {
                        CCoreNetwork.sendToServer(new MirrorWeavePacket(true));
                    } else {
                        state.heartBurstTicks = 12;
                        MirrorSounds.heartStage(state.heartStage);
                    }
                }
            }
        }
        if (state.holding) {
            if (!state.claimable) {
                state.holding = false;
                state.holdTicks = 0;
            } else {
                state.holdTicks++;
                if (state.holdTicks % 3 == 0 && state.holdTicks < HOLD_TICKS) {
                    MirrorSounds.holdTick(state.holdTicks / (float) HOLD_TICKS);
                }
                if (state.holdTicks >= HOLD_TICKS) {
                    performClaim();
                }
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        float dtTicks = lastMillis < 0 ? 0f : Mth.clamp((now - lastMillis) / 50f, 0f, 2f);
        lastMillis = now;
        time += dtTicks / 20f;
        deriveFromLedger();
        zoom += (targetZoom - zoom) * (1f - (float) Math.pow(0.55f, dtTicks));
        float veilTarget = (state.ceremonyActive || state.claimable || heartClaimable()) ? 1f : 0f;
        state.veil += (veilTarget - state.veil) * (1f - (float) Math.pow(0.78f, dtTicks));
        float awakenTarget = state.skeins >= SKEIN_CAP ? 1f : 0f;
        state.awaken += (awakenTarget - state.awaken) * (1f - (float) Math.pow(0.99f, dtTicks));
        state.hoverEcho = echoAt(mouseX, mouseY);
        if (state.hoverEcho != lastHover && state.hoverEcho >= 0) {
            MirrorSounds.hover(state.hoverEcho);
        }
        lastHover = state.hoverEcho;
        MirrorScene.render(guiGraphics, width, height, mouseX, mouseY, time, zoom, state);
        if (heartClaimable()) {
            int a = (int) (150 + 70 * Math.sin(time * 2.5f));
            Component prompt = state.heartHolding ?
                    Component.translatableWithFallback("mirror.cosmiccore.prompt.heart_hold", "Hold on tightly...") :
                    Component.translatableWithFallback("mirror.cosmiccore.prompt.heart",
                            "Mother Moon, here I stand, on the same stage as you once again.");
            guiGraphics.drawCenteredString(font, prompt, width / 2, height - 40, (a << 24) | 0xFFF3D6);
        } else if (state.claimable) {
            int a = (int) (150 + 70 * Math.sin(time * 2.5f));
            Component prompt = state.holding ?
                    Component.translatableWithFallback("mirror.cosmiccore.prompt.lock", "Bring them back to me.") :
                    Component.translatableWithFallback("mirror.cosmiccore.prompt.waiting",
                            "An echo from the past sings, hold on and remember.");
            guiGraphics.drawCenteredString(font, prompt,
                    width / 2, height - 40, (a << 24) | 0xF0D9A8);
        } else if (state.ceremonyActive) {
            guiGraphics.drawCenteredString(font,
                    Component.translatableWithFallback("mirror.cosmiccore.prompt.weaving", "Sol starts to weave..."),
                    width / 2, height - 40, 0x90E8C07A);
        }
        guiGraphics.drawString(font,
                "[E] weave-now  [W] weave  [X] claim  [S] scar  [D] dim+  [K] skein+  [R] reset-fx  [scroll] zoom  (/deed grant feeds coils)",
                8, height - 12, 0x40FFFFFF, false);
        guiGraphics.drawString(font,
                "cache: " + ClientDeedCache.woven().size() + " woven / " + ClientDeedCache.pending().size() +
                        " pending | lit " + state.litEchoes + " skeins " + state.skeins + " coils " + state.coils,
                8, height - 24, 0x40FFFFFF, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            targetZoom = Mth.clamp(targetZoom * (scrollY > 0 ? 1.15f : 1f / 1.15f), 1f, 2.6f);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_E -> {
                if (!state.ceremonyActive && !state.claimable && state.coils > 0) {
                    CCoreNetwork.sendToServer(new MirrorWeavePacket(false));
                }
                return true;
            }
            case GLFW.GLFW_KEY_D -> {
                state.devDim = (state.devDim + 1) % (ECHO_CAP + 1);
                return true;
            }
            case GLFW.GLFW_KEY_S -> {
                state.scorch = !state.scorch;
                return true;
            }
            case GLFW.GLFW_KEY_W -> {
                if (state.claimable) {
                    performClaim();
                    return true;
                }
                if (!state.ceremonyActive && state.coils > 0 && state.litEchoes < ECHO_CAP) {
                    state.ceremonyActive = true;
                    state.ceremonyProgress = 0;
                    MirrorSounds.weaveStart();
                }
                return true;
            }
            case GLFW.GLFW_KEY_K -> {
                state.devSkeins = (state.devSkeins + 1) % (SKEIN_CAP + 1);
                return true;
            }
            case GLFW.GLFW_KEY_X -> {
                performClaim();
                return true;
            }
            case GLFW.GLFW_KEY_R -> {
                state.reset();
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (heartClaimable() && heartAt(mouseX, mouseY)) {
                state.heartHolding = true;
                state.heartHoldTicks = 0;
                return true;
            }
            int hit = echoAt(mouseX, mouseY);
            if (hit >= 0 && state.claimable && hit == Math.min(state.litEchoes, ECHO_CAP - 1)) {
                state.holding = true;
                state.holdTicks = 0;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (state.holding || state.heartHolding)) {
            state.holding = false;
            state.holdTicks = 0;
            state.heartHolding = false;
            state.heartHoldTicks = 0;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean heartClaimable() {
        return state.skeins >= SKEIN_CAP && state.litEchoes >= ECHO_CAP && !state.heartClaimed;
    }

    private boolean heartAt(double mouseX, double mouseY) {
        float[] p = MirrorScene.heartScreenPos(width, height, mouseX, mouseY, zoom);
        double dx = mouseX - p[0];
        double dy = mouseY - p[1];
        return dx * dx + dy * dy <= p[2] * p[2];
    }

    private void performClaim() {
        if (!state.claimable) return;
        state.claimable = false;
        state.ceremonyActive = false;
        state.ceremonyProgress = 0;
        state.flashTicks = 0;
        state.holding = false;
        state.holdTicks = 0;
        CCoreNetwork.sendToServer(new MirrorWeavePacket(false));
    }

    private int echoAt(double mouseX, double mouseY) {
        int total = Math.min(Math.max(state.litEchoes + state.dimEchoes,
                state.litEchoes + (state.claimable ? 1 : 0)), ECHO_CAP);
        for (int i = 0; i < total; i++) {
            float[] p = MirrorScene.echoScreenPos(i, width, height, mouseX, mouseY, zoom);
            double dx = mouseX - p[0];
            double dy = mouseY - p[1];
            if (dx * dx + dy * dy <= p[2] * p[2]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN = new KeyMapping(
                "key.cosmiccore.mirror_dev",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.cosmiccore");
        event.register(OPEN);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (OPEN == null || !OPEN.matches(event.getKey(), event.getScanCode())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) return;
        mc.setScreen(new MirrorScreen());
    }
}
