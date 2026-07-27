package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.compat.ftbquests.DeedQuestReturn;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.DeedPresentationAckPacket;
import com.ghostipedia.cosmiccore.common.network.packet.MirrorWeavePacket;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
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

    public static final int ECHO_CAP = 12;
    public static final int HOLD_TICKS = 22;
    public static final int SKEIN_CAP = 6;
    private static final int CINEMATIC_TEXT_MARGIN = 24;
    private static final int CINEMATIC_TEXT_MAX_WIDTH = 320;
    private static final float CINEMATIC_TEXT_WIDTH_RATIO = 0.34f;
    private static final int WEAVE_SYNC_TIMEOUT_TICKS = 200;
    private static final int QUEST_REVEAL_TICKS = 30;

    public static KeyMapping OPEN;

    public static final class DevState {

        public int litEchoes;
        public int dimEchoes;
        public boolean scorch;
        public boolean claimable;
        public int coils;
        public boolean ceremonyActive;
        public int ceremonyProgress;
        public float ceremonyWeaveProgress;
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
            ceremonyWeaveProgress = 0f;
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

    private final DevState state = new DevState();
    private boolean playback;
    private boolean automaticWeave;
    @Nullable
    private DeedCinematic cinematic;
    private DeedCinematic.Phase cinematicPhase = DeedCinematic.Phase.PRELUDE;
    private int visibleCinematicCodePoints;
    @Nullable
    private ResourceLocation activeDeed;
    @Nullable
    private Long returnQuestId;
    private boolean weaveSent;
    private int weaveWaitTicks;
    private int questRevealTicks;
    private boolean returningToQuest;
    private float time;
    private float zoom = 1f;
    private float targetZoom = 1f;
    private long lastMillis = -1L;
    private int lastHover = -1;
    private int lastWovenBody = -1;
    private int lastSkeins;
    private int lastLit;
    private boolean lastHeart;

    private MirrorScreen(@Nullable ClientDeedCache.ClientPresentation presentation,
                         @Nullable ResourceLocation automaticDeed, @Nullable Long returnQuestId) {
        super(Component.translatable("screen.cosmiccore.deeds"));
        playback = presentation != null;
        this.returnQuestId = returnQuestId;
        MirrorSounds.open();
        if (presentation != null) {
            startPresentation(presentation.deedId());
        } else if (automaticDeed != null) {
            beginAutomaticWeave(automaticDeed);
        }
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (ClientDeedCache.pending().contains(DeedRegistry.NETHER_PERMIT.id()) &&
                !ClientDeedCache.woven().contains(DeedRegistry.NETHER_PERMIT.id())) {
            minecraft.setScreen(new MirrorScreen(null, DeedRegistry.NETHER_PERMIT.id(), null));
            return;
        }
        List<ClientDeedCache.ClientPresentation> presentations = ClientDeedCache.presentations();
        if (!presentations.isEmpty()) {
            minecraft.setScreen(new MirrorScreen(presentations.getFirst(), null, null));
            return;
        }
        for (ResourceLocation pending : pendingDeeds()) {
            if (!DeedCinematic.isAutomatic(pending)) continue;
            Long returnQuestId = DeedQuestReturn.questId(pending);
            if (returnQuestId == null) returnQuestId = DeedCinematic.returnQuestId(pending);
            minecraft.setScreen(new MirrorScreen(null, pending, returnQuestId));
            return;
        }
        minecraft.setScreen(new MirrorScreen(null, null, null));
    }

    public static void openPatientZero() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.setScreen(new MirrorScreen(null, DeedRegistry.NETHER_PERMIT.id(), null));
        }
    }

    public static void openAutomatic(ResourceLocation deedId, long returnQuestId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            DeedQuestReturn.remember(deedId, returnQuestId);
            minecraft.setScreen(new MirrorScreen(null, deedId, returnQuestId));
        }
    }

    public static void openPresentation(ClientDeedCache.ClientPresentation presentation) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.screen == null) {
            minecraft.setScreen(new MirrorScreen(presentation, null, null));
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
        if (questRevealTicks > 0) {
            questRevealTicks--;
            if (questRevealTicks == 0) {
                returnToQuest();
                return;
            }
        }
        if (state.ceremonyActive && (playback || automaticWeave || state.holding) && !weaveSent) {
            advanceCeremony();
        }
        if (weaveSent && activeDeed != null) {
            if (ClientDeedCache.woven().contains(activeDeed)) {
                finishInteractiveWeave();
            } else if (++weaveWaitTicks >= WEAVE_SYNC_TIMEOUT_TICKS) {
                abortTimedOutWeave();
            }
        }
        if (state.flashTicks > 0) state.flashTicks--;
        if (state.burstTicks > 0) state.burstTicks--;
        if (state.skeinBurstTicks > 0) state.skeinBurstTicks--;
        if (state.heartBurstTicks > 0) state.heartBurstTicks--;
        tickHeart();
    }

    private void advanceCeremony() {
        if (cinematic == null) {
            cancelWeave();
            return;
        }
        state.ceremonyProgress++;
        state.holdTicks = state.ceremonyProgress;
        state.ceremonyWeaveProgress = cinematic.weaveProgress(state.ceremonyProgress);
        DeedCinematic.Phase nextPhase = cinematic.phaseAt(state.ceremonyProgress);
        if (nextPhase != cinematicPhase) {
            cinematicPhase = nextPhase;
            MirrorSounds.weavePhase(nextPhase);
        }
        int nextVisibleCodePoints = cinematic.visibleCodePoints(state.ceremonyProgress);
        if (nextVisibleCodePoints > visibleCinematicCodePoints && state.ceremonyProgress % 3 == 0) {
            MirrorSounds.memoryGlyph(cinematicPhase);
        }
        visibleCinematicCodePoints = nextVisibleCodePoints;
        int ceremonyTicks = cinematic.totalTicks();
        if (state.ceremonyProgress % 12 == 0 && state.ceremonyProgress < ceremonyTicks) {
            MirrorSounds.holdTick(state.ceremonyWeaveProgress);
        }
        if (state.ceremonyProgress < ceremonyTicks) return;
        state.ceremonyProgress = ceremonyTicks;
        state.ceremonyWeaveProgress = 1f;
        state.flashTicks = 24;
        MirrorSounds.weaveComplete();
        if (playback) {
            finishPresentation();
        } else if (activeDeed != null) {
            weaveSent = true;
            weaveWaitTicks = 0;
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
        renderQuestRevealTail(guiGraphics);
    }

    private void renderPrompt(GuiGraphics guiGraphics) {
        if (heartClaimable()) {
            int alpha = (int) (150 + 70 * Math.sin(time * 2.5f));
            Component prompt = state.heartHolding ? HEART_HOLD_PROMPT : HEART_PROMPT;
            guiGraphics.drawCenteredString(font, prompt, width / 2, height - 40,
                    alpha << 24 | 0xFFF3D6);
        } else if (state.ceremonyActive) {
            int alpha = (int) (150 + 70 * Math.sin(time * 2.5f));
            Component prompt = playback || automaticWeave ? BINDING_PROMPT : HOLD_PROMPT;
            guiGraphics.drawCenteredString(font, prompt, width / 2, height - 40,
                    alpha << 24 | 0xF0D9A8);
        }
    }

    private void renderCinematicText(GuiGraphics guiGraphics) {
        if (!state.ceremonyActive || cinematic == null) return;
        for (DeedCinematic.Fragment fragment : cinematic.fragments()) {
            int visibleCodePoints = fragment.visibleCodePoints(state.ceremonyProgress);
            int alpha = fragment.alpha(state.ceremonyProgress);
            if (visibleCodePoints == 0 || alpha == 0) continue;
            List<String> lines = wrapCinematicText(fragment.text(), cinematicTextWidth(fragment));
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(width * fragment.x(), height * fragment.y(), 300);
            pose.mulPose(Axis.ZP.rotationDegrees(fragment.angle()));
            int top = -Math.max(0, lines.size() - 1) * 5;
            int remaining = visibleCodePoints;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                int lineCodePoints = line.codePointCount(0, line.length());
                int visibleLineCodePoints = Math.min(remaining, lineCodePoints);
                String visibleLine = visibleLineCodePoints == 0 ? "" :
                        line.substring(0, line.offsetByCodePoints(0, visibleLineCodePoints));
                Component renderedLine = Component.literal(visibleLine);
                if (fragment.voice().italic()) {
                    renderedLine = renderedLine.copy().withStyle(style -> style.withItalic(true));
                }
                guiGraphics.drawCenteredString(font, renderedLine, 0, top + i * 10,
                        alpha << 24 | fragment.voice().color());
                remaining = Math.max(0, remaining - lineCodePoints - (i + 1 < lines.size() ? 1 : 0));
            }
            pose.popPose();
        }
    }

    private void renderQuestRevealTail(GuiGraphics guiGraphics) {
        if (questRevealTicks <= 0) return;
        float progress = 1f - questRevealTicks / (float) QUEST_REVEAL_TICKS;
        float eased = progress * progress * (3f - 2f * progress);
        int alpha = Mth.clamp(Math.round(255f * eased), 0, 255);
        guiGraphics.fill(0, 0, width, height, alpha << 24);
    }

    private int cinematicTextWidth(DeedCinematic.Fragment fragment) {
        int centerX = Math.round(width * fragment.x());
        int edgeLimited = (Math.min(centerX, width - centerX) - CINEMATIC_TEXT_MARGIN) * 2;
        int preferred = Math.min(CINEMATIC_TEXT_MAX_WIDTH, Math.round(width * CINEMATIC_TEXT_WIDTH_RATIO));
        return Math.max(80, Math.min(preferred, Math.max(80, edgeLimited)));
    }

    private List<String> wrapCinematicText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" +")) {
                if (word.isEmpty()) continue;
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (font.width(candidate) <= maxWidth) {
                    line.setLength(0);
                    line.append(candidate);
                    continue;
                }
                if (!line.isEmpty()) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                wrapLongWord(lines, line, word, maxWidth);
            }
            if (!line.isEmpty()) lines.add(line.toString());
        }
        return lines;
    }

    private void wrapLongWord(List<String> lines, StringBuilder line, String word, int maxWidth) {
        int offset = 0;
        while (offset < word.length()) {
            int end = fittingPrefixEnd(word, offset, maxWidth);
            String piece = word.substring(offset, end);
            if (end < word.length()) {
                lines.add(piece);
            } else {
                line.append(piece);
            }
            offset = end;
        }
    }

    private int fittingPrefixEnd(String text, int start, int maxWidth) {
        int end = start;
        while (end < text.length()) {
            int next = text.offsetByCodePoints(end, 1);
            if (end > start && font.width(text.substring(start, next)) > maxWidth) break;
            end = next;
        }
        return end;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (questRevealTicks > 0) return true;
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
        if (button != 0 || playback || automaticWeave || weaveSent || questRevealTicks > 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
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
        ResourceLocation deedId = pending.get(pendingIndex);
        if (DeedCinematic.isAutomatic(deedId)) {
            returnQuestId = DeedQuestReturn.questId(deedId);
            if (returnQuestId == null) returnQuestId = DeedCinematic.returnQuestId(deedId);
            beginAutomaticWeave(deedId);
        } else {
            beginHeldWeave(deedId, hit, pendingIndex);
        }
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
        beginWeave(deedId, slot, coil);
        state.holding = true;
    }

    private void beginAutomaticWeave(ResourceLocation deedId) {
        deriveFromLedger();
        List<ResourceLocation> pending = pendingDeeds();
        int pendingIndex = Math.max(0, pending.indexOf(deedId));
        beginWeave(deedId, state.litEchoes + pendingIndex, pendingIndex);
        automaticWeave = true;
    }

    private void beginWeave(ResourceLocation deedId, int slot, int coil) {
        activeDeed = deedId;
        state.ceremonySlot = slot;
        state.ceremonyCoil = coil;
        state.ceremonyActive = true;
        state.ceremonyProgress = 0;
        state.ceremonyWeaveProgress = 0f;
        state.holdTicks = 0;
        state.flashTicks = 0;
        weaveWaitTicks = 0;
        questRevealTicks = 0;
        cinematic = DeedCinematic.load(deedId);
        cinematicPhase = DeedCinematic.Phase.PRELUDE;
        visibleCinematicCodePoints = 0;
        MirrorSounds.weaveStart();
    }

    private void cancelHeldWeave() {
        cancelWeave();
    }

    private void cancelWeave() {
        activeDeed = null;
        state.ceremonyActive = false;
        state.ceremonyProgress = 0;
        state.ceremonyWeaveProgress = 0f;
        state.holding = false;
        state.holdTicks = 0;
        state.flashTicks = 0;
        weaveSent = false;
        weaveWaitTicks = 0;
        automaticWeave = false;
        cinematic = null;
    }

    private void startPresentation(ResourceLocation deedId) {
        int wovenBody = 0;
        for (ResourceLocation id : ClientDeedCache.woven()) {
            if (!id.equals(DeedRegistry.THE_ADDRESS.id()) && !id.equals(deedId)) wovenBody++;
        }
        beginWeave(deedId, wovenBody % ECHO_CAP, 0);
    }

    private void finishPresentation() {
        if (activeDeed == null) return;
        ResourceLocation deedId = activeDeed;
        CCoreNetwork.sendToServer(new DeedPresentationAckPacket(deedId));
        ClientDeedCache.acknowledge(deedId);
        state.ceremonyActive = false;
        state.ceremonyProgress = 0;
        state.ceremonyWeaveProgress = 0f;
        state.burstTicks = 26;
        state.burstSlot = state.ceremonySlot;
        activeDeed = null;
        playback = false;
        cinematic = null;
        MirrorSounds.claim();
    }

    private void finishInteractiveWeave() {
        if (activeDeed == null) return;
        ResourceLocation deedId = activeDeed;
        CCoreNetwork.sendToServer(new DeedPresentationAckPacket(deedId));
        ClientDeedCache.acknowledge(deedId);
        state.ceremonyActive = false;
        state.ceremonyProgress = 0;
        state.ceremonyWeaveProgress = 1f;
        state.burstTicks = 26;
        state.burstSlot = state.ceremonySlot;
        state.holding = false;
        state.holdTicks = 0;
        activeDeed = null;
        weaveSent = false;
        weaveWaitTicks = 0;
        automaticWeave = false;
        cinematic = null;
        DeedQuestReturn.forget(deedId);
        if (returnQuestId != null) {
            questRevealTicks = QUEST_REVEAL_TICKS;
        }
    }

    private void abortTimedOutWeave() {
        cancelWeave();
        returnToQuest();
    }

    private void returnToQuest() {
        Long questId = returnQuestId;
        returnQuestId = null;
        if (questId == null) return;
        returningToQuest = true;
        if (ModList.get().isLoaded("ftbquests")) {
            DeedQuestReturn.open(questId);
        } else {
            Minecraft.getInstance().setScreen(null);
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
    public boolean shouldCloseOnEsc() {
        return !weaveSent && questRevealTicks <= 0;
    }

    @Override
    public void onClose() {
        cancelWeave();
        if (returnQuestId != null) {
            returnToQuest();
        } else {
            super.onClose();
        }
    }

    @Override
    public void removed() {
        state.heartHolding = false;
        state.heartHoldTicks = 0;
        if (!returningToQuest) {
            state.ceremonyActive = false;
            state.ceremonyProgress = 0;
            state.ceremonyWeaveProgress = 0f;
            cinematic = null;
        }
        super.removed();
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
        if (ClientDeedCache.pending().contains(DeedRegistry.NETHER_PERMIT.id()) &&
                !ClientDeedCache.woven().contains(DeedRegistry.NETHER_PERMIT.id())) {
            openPatientZero();
        } else {
            open();
        }
    }
}
