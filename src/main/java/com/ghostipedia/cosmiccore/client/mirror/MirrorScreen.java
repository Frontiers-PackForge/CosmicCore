package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.DeedPresentationAckPacket;
import com.ghostipedia.cosmiccore.common.network.packet.MirrorWeavePacket;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
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
        public int ceremonySlot;
        public int ceremonyCoil;
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

        public DevState() {
            reset();
        }

        public void reset() {
            scorch = true;
            claimable = false;
            ceremonyActive = false;
            ceremonyProgress = 0;
            ceremonySlot = 0;
            ceremonyCoil = 0;
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
        }
    }

    private static final Component HEART_PROMPT = Component.translatable("mirror.cosmiccore.prompt.heart");
    private static final Component HEART_HOLD_PROMPT = Component.translatable("mirror.cosmiccore.prompt.heart_hold");
    private static final Component BINDING_PROMPT = Component.translatable("mirror.cosmiccore.prompt.binding");
    private static final Component HOLD_PROMPT = Component.translatable("mirror.cosmiccore.prompt.hold");

    private static final class CinematicLine {

        private final String text;
        private final float x;
        private final float y;
        private final float angle;
        private final int startTick;
        private String visibleText = "";

        private CinematicLine(String text, float x, float y, float angle, int startTick) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.startTick = startTick;
        }

        private void update(int ceremonyProgress) {
            int elapsed = ceremonyProgress - startTick;
            int codePoints = text.codePointCount(0, text.length());
            int visible = Math.min(codePoints, Math.max(0, elapsed));
            visibleText = visible == 0 ? "" : text.substring(0, text.offsetByCodePoints(0, visible));
        }
    }

    private final DevState state = new DevState();
    private boolean playback;
    private final List<CinematicLine> cinematicLines = new ArrayList<>();
    @Nullable
    private ResourceLocation activeDeed;
    private boolean weaveSent;
    private float time;
    private float zoom = 1f;
    private float targetZoom = 1f;
    private long lastMillis = -1L;
    private int lastHover = -1;
    private int lastWovenBody = -1;
    private int lastSkeins;
    private int lastLit;
    private boolean lastHeart;

    private MirrorScreen(@Nullable ClientDeedCache.ClientPresentation presentation) {
        super(Component.translatable("screen.cosmiccore.deeds"));
        playback = presentation != null;
        if (presentation != null) {
            startPresentation(presentation.deedId());
        }
        MirrorSounds.open();
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        List<ClientDeedCache.ClientPresentation> presentations = ClientDeedCache.presentations();
        minecraft.setScreen(new MirrorScreen(presentations.isEmpty() ? null : presentations.getFirst()));
    }

    public static void openPresentation(ClientDeedCache.ClientPresentation presentation) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.screen == null) {
            minecraft.setScreen(new MirrorScreen(presentation));
        }
    }

    private void deriveFromLedger() {
        var woven = ClientDeedCache.woven();
        boolean heartWoven = woven.contains(DeedRegistry.THE_ADDRESS.id());
        int wovenBody = woven.size() - (heartWoven ? 1 : 0);
        if (playback && activeDeed != null && !activeDeed.equals(DeedRegistry.THE_ADDRESS.id()) &&
                woven.contains(activeDeed)) {
            wovenBody--;
        }
        wovenBody = Math.max(0, wovenBody);
        int derivedSkeins = Math.min(SKEIN_CAP, wovenBody / ECHO_CAP);
        int derivedLit = Math.min(ECHO_CAP, wovenBody - derivedSkeins * ECHO_CAP);
        int pending = pendingDeedCount();
        int presented = playback && activeDeed != null && !activeDeed.equals(DeedRegistry.THE_ADDRESS.id()) ? 1 : 0;

        state.skeins = derivedSkeins;
        state.litEchoes = derivedLit;
        state.dimEchoes = Math.min(ECHO_CAP - derivedLit, pending + presented);
        state.coils = Math.max(pending, state.ceremonyActive ? 1 : 0);
        state.heartClaimed = heartWoven && !(playback && DeedRegistry.THE_ADDRESS.id().equals(activeDeed));

        if (lastWovenBody >= 0) {
            if (derivedSkeins > lastSkeins) {
                state.skeinBurstTicks = 20;
                MirrorSounds.compress();
            } else if (derivedLit > lastLit) {
                state.burstTicks = 26;
                state.burstSlot = derivedLit - 1;
                MirrorSounds.claim();
            }
            if (heartWoven && !lastHeart) {
                state.heartBurstTicks = 40;
                MirrorSounds.heartClaim();
            }
        }
        lastWovenBody = wovenBody;
        lastSkeins = derivedSkeins;
        lastLit = derivedLit;
        lastHeart = heartWoven;
    }

    @Override
    public void tick() {
        deriveFromLedger();
        if (state.ceremonyActive && (playback || state.holding) && !weaveSent) {
            advanceCeremony();
        }
        if (weaveSent && activeDeed != null && ClientDeedCache.woven().contains(activeDeed)) {
            finishInteractiveWeave();
        }
        if (state.flashTicks > 0) state.flashTicks--;
        if (state.burstTicks > 0) state.burstTicks--;
        if (state.skeinBurstTicks > 0) state.skeinBurstTicks--;
        if (state.heartBurstTicks > 0) state.heartBurstTicks--;
        tickHeart();
    }

    private void advanceCeremony() {
        state.ceremonyProgress++;
        state.holdTicks = state.ceremonyProgress;
        for (CinematicLine line : cinematicLines) {
            line.update(state.ceremonyProgress);
        }
        if (state.ceremonyProgress % 12 == 0 && state.ceremonyProgress < CEREMONY_TICKS) {
            MirrorSounds.holdTick(state.ceremonyProgress / (float) CEREMONY_TICKS);
        }
        if (state.ceremonyProgress < CEREMONY_TICKS) return;
        state.ceremonyProgress = CEREMONY_TICKS;
        state.flashTicks = 24;
        MirrorSounds.weaveComplete();
        if (playback) {
            finishPresentation();
        } else if (activeDeed != null) {
            weaveSent = true;
            state.holding = false;
            CCoreNetwork.sendToServer(new MirrorWeavePacket(activeDeed, true));
        }
    }

    private void tickHeart() {
        if (!state.heartHolding) return;
        if (!heartClaimable()) {
            state.heartHolding = false;
            state.heartHoldTicks = 0;
            return;
        }
        state.heartHoldTicks++;
        if (state.heartHoldTicks % 3 == 0 && state.heartHoldTicks < HOLD_TICKS) {
            MirrorSounds.holdTick(state.heartHoldTicks / (float) HOLD_TICKS);
        }
        if (state.heartHoldTicks < HOLD_TICKS) return;
        state.heartStage++;
        state.heartHolding = false;
        state.heartHoldTicks = 0;
        if (state.heartStage >= 3) {
            CCoreNetwork.sendToServer(new MirrorWeavePacket(DeedRegistry.THE_ADDRESS.id(), true));
        } else {
            state.heartBurstTicks = 12;
            MirrorSounds.heartStage(state.heartStage);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        float deltaTicks = lastMillis < 0 ? 0f : Mth.clamp((now - lastMillis) / 50f, 0f, 2f);
        lastMillis = now;
        time += deltaTicks / 20f;
        deriveFromLedger();
        zoom += (targetZoom - zoom) * (1f - (float) Math.pow(0.55f, deltaTicks));
        float veilTarget = state.ceremonyActive || heartClaimable() ? 1f : 0f;
        state.veil += (veilTarget - state.veil) * (1f - (float) Math.pow(0.78f, deltaTicks));
        float awakenTarget = state.skeins >= SKEIN_CAP ? 1f : 0f;
        state.awaken += (awakenTarget - state.awaken) * (1f - (float) Math.pow(0.99f, deltaTicks));
        state.hoverEcho = echoAt(mouseX, mouseY);
        if (state.hoverEcho != lastHover && state.hoverEcho >= 0) {
            MirrorSounds.hover(state.hoverEcho);
        }
        lastHover = state.hoverEcho;
        MirrorScene.render(guiGraphics, width, height, mouseX, mouseY, time, zoom, state);
        renderPrompt(guiGraphics);
        renderCinematicText(guiGraphics);
    }

    private void renderPrompt(GuiGraphics guiGraphics) {
        if (heartClaimable()) {
            int alpha = (int) (150 + 70 * Math.sin(time * 2.5f));
            Component prompt = state.heartHolding ? HEART_HOLD_PROMPT : HEART_PROMPT;
            guiGraphics.drawCenteredString(font, prompt, width / 2, height - 40,
                    alpha << 24 | 0xFFF3D6);
        } else if (state.ceremonyActive) {
            int alpha = (int) (150 + 70 * Math.sin(time * 2.5f));
            Component prompt = playback ? BINDING_PROMPT : HOLD_PROMPT;
            guiGraphics.drawCenteredString(font, prompt, width / 2, height - 40,
                    alpha << 24 | 0xF0D9A8);
        }
    }

    private void renderCinematicText(GuiGraphics guiGraphics) {
        if (!state.ceremonyActive || cinematicLines.isEmpty()) return;
        for (CinematicLine line : cinematicLines) {
            if (line.visibleText.isEmpty()) continue;
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(width * line.x, height * line.y, 300);
            pose.mulPose(Axis.ZP.rotationDegrees(line.angle));
            guiGraphics.drawCenteredString(font, line.visibleText, 0, 0, 0xFFE8DFD0);
            pose.popPose();
        }
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || playback || weaveSent) return super.mouseClicked(mouseX, mouseY, button);
        if (heartClaimable() && heartAt(mouseX, mouseY)) {
            state.heartHolding = true;
            state.heartHoldTicks = 0;
            return true;
        }
        int hit = echoAt(mouseX, mouseY);
        int pendingIndex = hit - state.litEchoes;
        List<ResourceLocation> pending = pendingDeeds();
        if (pendingIndex < 0 || pendingIndex >= pending.size()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        beginHeldWeave(pending.get(pendingIndex), hit, pendingIndex);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && state.heartHolding) {
            state.heartHolding = false;
            state.heartHoldTicks = 0;
            return true;
        }
        if (button == 0 && state.holding && !weaveSent) {
            cancelHeldWeave();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void beginHeldWeave(ResourceLocation deedId, int slot, int coil) {
        activeDeed = deedId;
        state.ceremonySlot = slot;
        state.ceremonyCoil = coil;
        state.ceremonyActive = true;
        state.ceremonyProgress = 0;
        state.holding = true;
        state.holdTicks = 0;
        state.flashTicks = 0;
        prepareCinematicLines(deedId);
        MirrorSounds.weaveStart();
    }

    private void cancelHeldWeave() {
        activeDeed = null;
        state.ceremonyActive = false;
        state.ceremonyProgress = 0;
        state.holding = false;
        state.holdTicks = 0;
        state.flashTicks = 0;
        cinematicLines.clear();
    }

    private void startPresentation(ResourceLocation deedId) {
        activeDeed = deedId;
        int wovenBody = 0;
        for (ResourceLocation id : ClientDeedCache.woven()) {
            if (!id.equals(DeedRegistry.THE_ADDRESS.id()) && !id.equals(deedId)) wovenBody++;
        }
        state.ceremonySlot = wovenBody % ECHO_CAP;
        state.ceremonyCoil = 0;
        state.ceremonyActive = true;
        state.ceremonyProgress = 0;
        prepareCinematicLines(deedId);
        MirrorSounds.weaveStart();
    }

    private void finishPresentation() {
        if (activeDeed == null) return;
        ResourceLocation deedId = activeDeed;
        CCoreNetwork.sendToServer(new DeedPresentationAckPacket(deedId));
        ClientDeedCache.acknowledge(deedId);
        state.ceremonyActive = false;
        state.ceremonyProgress = 0;
        state.burstTicks = 26;
        state.burstSlot = state.ceremonySlot;
        activeDeed = null;
        playback = false;
        cinematicLines.clear();
        MirrorSounds.claim();
    }

    private void finishInteractiveWeave() {
        if (activeDeed == null) return;
        ResourceLocation deedId = activeDeed;
        CCoreNetwork.sendToServer(new DeedPresentationAckPacket(deedId));
        ClientDeedCache.acknowledge(deedId);
        state.ceremonyActive = false;
        state.ceremonyProgress = 0;
        state.burstTicks = 26;
        state.burstSlot = state.ceremonySlot;
        state.holding = false;
        state.holdTicks = 0;
        activeDeed = null;
        weaveSent = false;
        cinematicLines.clear();
    }

    private void prepareCinematicLines(ResourceLocation deedId) {
        cinematicLines.clear();
        List<String> telling = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String key = "deed." + deedId.getNamespace() + "." + deedId.getPath() + ".telling." + i;
            if (!I18n.exists(key)) break;
            telling.add(Component.translatable(key).getString());
        }
        int seed = deedId.hashCode();
        int spacing = telling.isEmpty() ? 0 : 80 / telling.size();
        for (int i = 0; i < telling.size(); i++) {
            float x = 0.18f + Math.floorMod(seed + i * 41, 620) / 1000f;
            float y = 0.16f + Math.floorMod(seed * 3 + i * 173, 570) / 1000f;
            float angle = -18f + Math.floorMod(seed * 7 + i * 67, 361) / 10f;
            cinematicLines.add(new CinematicLine(telling.get(i), x, y, angle, 10 + i * spacing));
        }
    }

    private boolean heartClaimable() {
        return state.skeins >= SKEIN_CAP && state.litEchoes >= ECHO_CAP && !state.heartClaimed;
    }

    private boolean heartAt(double mouseX, double mouseY) {
        float[] position = MirrorScene.heartScreenPos(width, height, mouseX, mouseY, zoom);
        double dx = mouseX - position[0];
        double dy = mouseY - position[1];
        return dx * dx + dy * dy <= position[2] * position[2];
    }

    private int echoAt(double mouseX, double mouseY) {
        int total = Math.min(state.litEchoes + state.dimEchoes, ECHO_CAP);
        for (int i = 0; i < total; i++) {
            float[] position = MirrorScene.echoScreenPos(i, width, height, mouseX, mouseY, zoom);
            double dx = mouseX - position[0];
            double dy = mouseY - position[1];
            if (dx * dx + dy * dy <= position[2] * position[2]) return i;
        }
        return -1;
    }

    private static List<ResourceLocation> pendingDeeds() {
        return ClientDeedCache.pending().stream()
                .filter(id -> !id.equals(DeedRegistry.THE_ADDRESS.id()))
                .toList();
    }

    private static int pendingDeedCount() {
        int count = 0;
        for (ResourceLocation id : ClientDeedCache.pending()) {
            if (!id.equals(DeedRegistry.THE_ADDRESS.id())) count++;
        }
        return count;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN = new KeyMapping(
                "key.cosmiccore.deeds.open",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.categories.cosmiccore.deeds");
        event.register(OPEN);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (OPEN == null || !OPEN.matches(event.getKey(), event.getScanCode())) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null || !ClientDeedCache.canOpen()) return;
        open();
    }
}
