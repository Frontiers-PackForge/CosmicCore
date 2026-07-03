package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;

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

        public DevState() {
            reset();
        }

        public void reset() {
            litEchoes = 0;
            dimEchoes = 0;
            scorch = true;
            claimable = false;
            coils = 2;
            ceremonyActive = false;
            ceremonyProgress = 0;
            veil = 0f;
            flashTicks = 0;
            holding = false;
            holdTicks = 0;
            burstTicks = 0;
            burstSlot = -1;
        }
    }

    private final DevState state = new DevState();
    private float time = 0f;
    private float zoom = 1f;
    private float targetZoom = 1f;
    private long lastMillis = -1L;

    public MirrorScreen() {
        super(Component.literal("Sol's Mirror"));
    }

    @Override
    public void tick() {
        if (state.ceremonyActive && state.ceremonyProgress < CEREMONY_TICKS) {
            state.ceremonyProgress++;
            if (state.ceremonyProgress >= CEREMONY_TICKS) {
                state.claimable = true;
                state.flashTicks = 24;
            }
        }
        if (state.flashTicks > 0) {
            state.flashTicks--;
        }
        if (state.burstTicks > 0) {
            state.burstTicks--;
        }
        if (state.holding) {
            if (!state.claimable) {
                state.holding = false;
                state.holdTicks = 0;
            } else {
                state.holdTicks++;
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
        zoom += (targetZoom - zoom) * (1f - (float) Math.pow(0.55f, dtTicks));
        float veilTarget = (state.ceremonyActive || state.claimable) ? 1f : 0f;
        state.veil += (veilTarget - state.veil) * (1f - (float) Math.pow(0.78f, dtTicks));
        state.hoverEcho = echoAt(mouseX, mouseY);
        MirrorScene.render(guiGraphics, width, height, mouseX, mouseY, time, zoom, state);
        if (state.claimable) {
            int a = (int) (150 + 70 * Math.sin(time * 2.5f));
            String prompt = state.holding ? "lock it down." : "an echo waits. hold it to remember.";
            guiGraphics.drawCenteredString(font, prompt,
                    width / 2, height - 40, (a << 24) | 0xF0D9A8);
        } else if (state.ceremonyActive) {
            guiGraphics.drawCenteredString(font, "sol is weaving...",
                    width / 2, height - 40, 0x90E8C07A);
        }
        guiGraphics.drawString(font,
                "[E] echo  [D] dim  [S] scorch  [W] weave  [C] coil  [X] claim  [R] reset  [scroll] zoom", 8,
                height - 12, 0x40FFFFFF, false);
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
                if (!state.ceremonyActive && !state.claimable) {
                    state.litEchoes = Math.min(ECHO_CAP - state.dimEchoes, state.litEchoes + 1);
                }
                return true;
            }
            case GLFW.GLFW_KEY_D -> {
                if (!state.ceremonyActive && !state.claimable && state.litEchoes + state.dimEchoes < ECHO_CAP) {
                    state.dimEchoes++;
                }
                return true;
            }
            case GLFW.GLFW_KEY_S -> {
                state.scorch = !state.scorch;
                return true;
            }
            case GLFW.GLFW_KEY_W -> {
                if (state.claimable) {
                    performClaim();
                }
                if (!state.ceremonyActive && state.coils > 0 && state.litEchoes < ECHO_CAP) {
                    state.coils--;
                    state.ceremonyActive = true;
                    state.ceremonyProgress = 0;
                }
                return true;
            }
            case GLFW.GLFW_KEY_C -> {
                state.coils = Math.min(4, state.coils + 1);
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
        if (button == 0 && state.holding) {
            state.holding = false;
            state.holdTicks = 0;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void performClaim() {
        if (!state.claimable) return;
        int seat = Math.min(state.litEchoes, ECHO_CAP - 1);
        if (state.litEchoes < ECHO_CAP) {
            state.litEchoes++;
            state.dimEchoes = Math.max(0, state.dimEchoes - 1);
        }
        state.claimable = false;
        state.ceremonyActive = false;
        state.ceremonyProgress = 0;
        state.flashTicks = 0;
        state.holding = false;
        state.holdTicks = 0;
        state.burstTicks = 26;
        state.burstSlot = seat;
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
