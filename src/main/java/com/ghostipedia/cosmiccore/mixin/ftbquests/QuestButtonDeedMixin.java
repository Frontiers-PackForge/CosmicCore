package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.client.compat.ftbquests.DeedQuestAccess;
import com.ghostipedia.cosmiccore.client.compat.ftbquests.DeedQuestInterferenceRenderer;
import com.ghostipedia.cosmiccore.client.compat.ftbquests.DeedQuestReadyRenderer;
import com.ghostipedia.cosmiccore.client.mirror.ClientDeedCache;
import com.ghostipedia.cosmiccore.client.mirror.DeedCinematic;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedTask;
import com.ghostipedia.cosmiccore.common.config.CosmicCoreConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = QuestButton.class, remap = false)
public abstract class QuestButtonDeedMixin {

    @Unique
    private static final int cosmiccore$sealed = 0;
    @Unique
    private static final int cosmiccore$primed = 1;
    @Unique
    private static final int cosmiccore$calling = 2;
    @Unique
    private static final int cosmiccore$woven = 3;

    @Shadow
    @Final
    private Quest quest;

    @Unique
    private long cosmiccore$sealedPulseUntil;

    @WrapMethod(method = "draw")
    private void cosmiccore$drawDeedQuest(GuiGraphics graphics, Theme theme, int x, int y, int width, int height,
                                          Operation<Void> original) {
        DeedTask task = cosmiccore$deedTask();
        if (task == null || cosmiccore$devVisorEditing()) {
            original.call(graphics, theme, x, y, width, height);
            return;
        }
        int state = cosmiccore$state(task);
        long now = Util.getMillis();
        if (state == cosmiccore$primed || state == cosmiccore$calling ||
                task.disclosure() == DeedTask.Disclosure.VISIBLE && state != cosmiccore$woven) {
            DeedQuestReadyRenderer.draw(graphics, x, y, width, height, now % 4_096_000L / 1000.0F);
        }
        if (cosmiccore$hidesNodeText(task, state)) {
            cosmiccore$drawDistortedSealedQuest(graphics, theme, x, y, width, height, now, original);
            cosmiccore$drawSealedInterference(graphics, x, y, width, height, now);
            if (state == cosmiccore$primed) cosmiccore$drawPrimedSigil(graphics, x, y, width, height, now);
            return;
        }
        original.call(graphics, theme, x, y, width, height);
        if (state == cosmiccore$primed) cosmiccore$drawPrimedSigil(graphics, x, y, width, height, now);
    }

    @WrapMethod(method = "addMouseOverText")
    private void cosmiccore$hideSealedQuestText(TooltipList tooltip, Operation<Void> original) {
        DeedTask task = cosmiccore$deedTask();
        if (task == null || cosmiccore$devVisorEditing()) {
            original.call(tooltip);
            return;
        }
        int state = cosmiccore$state(task);
        if (task.disclosure() == DeedTask.Disclosure.VISIBLE) {
            original.call(tooltip);
            if (state == cosmiccore$primed) {
                tooltip.add(Component.translatable("cosmiccore.ftbquests.deed.primed_visible")
                        .withColor(DeedCinematic.Voice.OVERSEER_ONE.color()));
            } else if (state != cosmiccore$woven) {
                tooltip.add(Component.translatable("cosmiccore.ftbquests.deed.visible_hint")
                        .withColor(DeedCinematic.Voice.OVERSEER_ONE.color()));
            }
            return;
        }
        boolean hidesNodeText = cosmiccore$hidesNodeText(task, state);
        if (!hidesNodeText) original.call(tooltip);
        if (state == cosmiccore$primed) {
            tooltip.add(Component.translatable("cosmiccore.ftbquests.deed.primed_sealed")
                    .withColor(DeedCinematic.Voice.OVERSEER_ONE.color()));
            return;
        }
        if (hidesNodeText) tooltip.add(DeedQuestAccess.sealedHint(task).copy().withStyle(ChatFormatting.GOLD));
    }

    @WrapMethod(method = "onClicked")
    private void cosmiccore$handleDeedQuestClick(MouseButton button, Operation<Void> original) {
        DeedTask task = cosmiccore$deedTask();
        if (task == null) {
            original.call(button);
            return;
        }
        if (ClientQuestFile.INSTANCE.canEdit()) {
            if (CosmicCoreConfig.devVisor()) original.call(button);
            return;
        }
        int state = cosmiccore$state(task);
        if (!button.isLeft()) {
            if (task.disclosure() != DeedTask.Disclosure.ASCENSION || state == cosmiccore$woven) {
                original.call(button);
            }
            return;
        }
        if (state == cosmiccore$woven) {
            original.call(button);
            return;
        }
        ((QuestButton) (Object) this).playClickSound();
        cosmiccore$sealedPulseUntil = Util.getMillis() + 420L;
        if (state == cosmiccore$primed || state == cosmiccore$calling) {
            task.requestPresentation();
        } else if (task.disclosure() != DeedTask.Disclosure.ASCENSION) {
            original.call(button);
        }
    }

    @Unique
    @Nullable
    private DeedTask cosmiccore$deedTask() {
        return DeedQuestAccess.task(quest);
    }

    @Unique
    private int cosmiccore$state(DeedTask task) {
        if (ClientDeedCache.woven().contains(task.deedId())) return cosmiccore$woven;
        if (ClientDeedCache.pending().contains(task.deedId())) return cosmiccore$calling;
        if (task.requirementsComplete(ClientQuestFile.INSTANCE.selfTeamData)) {
            return cosmiccore$primed;
        }
        return cosmiccore$sealed;
    }

    @Unique
    private boolean cosmiccore$hidesNodeText(DeedTask task, int state) {
        if (state == cosmiccore$woven || task.disclosure() == DeedTask.Disclosure.VISIBLE) return false;
        return task.disclosure() == DeedTask.Disclosure.SEALED || state == cosmiccore$sealed;
    }

    @Unique
    private boolean cosmiccore$devVisorEditing() {
        return ClientQuestFile.INSTANCE.canEdit() && CosmicCoreConfig.devVisor();
    }

    @Unique
    private int cosmiccore$jitter(long now, boolean vertical) {
        QuestButton button = (QuestButton) (Object) this;
        boolean pulse = now < cosmiccore$sealedPulseUntil;
        boolean hovered = button.isMouseOver();
        long cadence = pulse ? 45L : hovered ? 90L : 260L;
        long mixed = cosmiccore$mix(quest.id ^ now / cadence ^ (vertical ? 0x6A09E667F3BCC909L : 0L));
        if (!pulse && !hovered && (mixed & 7L) != 0L) return 0;
        int amplitude = pulse ? 2 : 1;
        return (int) Math.floorMod(mixed, amplitude * 2L + 1L) - amplitude;
    }

    @Unique
    private void cosmiccore$drawDistortedSealedQuest(GuiGraphics graphics, Theme theme, int x, int y, int width,
                                                     int height, long now, Operation<Void> original) {
        int baseX = cosmiccore$jitter(now, false);
        int baseY = cosmiccore$jitter(now, true);
        int amplitude = ((QuestButton) (Object) this).isMouseOver() || now < cosmiccore$sealedPulseUntil ? 3 : 2;
        double seed = (quest.id ^ quest.id >>> 32) * 0.000013D;
        int slices = Math.min(5, Math.max(1, height));
        for (int slice = 0; slice < slices; slice++) {
            int top = y + slice * height / slices;
            int bottom = y + (slice + 1) * height / slices;
            int tear = (int) Math.round(Math.sin(now / 520.0D + seed + slice * 1.73D) * amplitude);
            graphics.enableScissor(x - amplitude, top, x + width + amplitude, bottom);
            graphics.pose().pushPose();
            graphics.pose().translate(baseX + tear, baseY, 0);
            original.call(graphics, theme, x, y, width, height);
            graphics.flush();
            graphics.pose().popPose();
            graphics.disableScissor();
        }
    }

    @Unique
    private void cosmiccore$drawSealedInterference(GuiGraphics graphics, int x, int y, int width, int height,
                                                   long now) {
        if (width <= 1 || height <= 1) return;
        QuestButton button = (QuestButton) (Object) this;
        float pulse = Math.max(0.0F, Math.min(1.0F, (cosmiccore$sealedPulseUntil - now) / 420.0F));
        float intensity = Math.min(1.0F, 0.48F + (button.isMouseOver() ? 0.22F : 0.0F) + pulse * 0.30F);
        float seed = (float) ((quest.id ^ quest.id >>> 32) & 0xFFFFL) / 65535.0F;
        if (DeedQuestInterferenceRenderer.draw(graphics, x, y, width, height,
                now % 4_096_000L / 1000.0F, seed, intensity))
            return;
        long mixed = cosmiccore$mix(quest.id ^ now / 110L);
        int lineY = y + (int) Math.floorMod(mixed, height);
        int startX = x + (int) Math.floorMod(mixed >>> 17, Math.max(1, width / 2));
        int endX = Math.min(x + width, startX + Math.max(2, width / 2));
        int alpha = button.isMouseOver() || now < cosmiccore$sealedPulseUntil ? 90 : 46;
        graphics.fill(startX, lineY, endX, lineY + 1, alpha << 24 | 0x8F87A8);
        if (now < cosmiccore$sealedPulseUntil) {
            int secondY = y + (int) Math.floorMod(mixed >>> 29, height);
            graphics.fill(x, secondY, x + width, secondY + 1, 0x58D6B35A);
        }
    }

    @Unique
    private void cosmiccore$drawPrimedSigil(GuiGraphics graphics, int x, int y, int width, int height, long now) {
        int radius = Math.max(3, Math.min(5, Math.min(width, height) / 7));
        int centerX = x + width - radius;
        int centerY = y + radius;
        float breath = 0.5F + 0.5F * (float) Math.sin(now / 720.0D);
        int color = DeedCinematic.Voice.OVERSEER_ONE.color() & 0xFFFFFF;
        int glowAlpha = 42 + Math.round(24.0F * breath);
        int coreAlpha = 190 + Math.round(45.0F * breath);
        int outline = 0xB0181024;
        graphics.fill(centerX - 1, centerY - radius, centerX + 2, centerY + radius + 1, outline);
        graphics.fill(centerX - radius, centerY - 1, centerX + radius + 1, centerY + 2, outline);
        graphics.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 3, glowAlpha << 24 | color);
        graphics.fill(centerX, centerY - radius + 1, centerX + 1, centerY + radius, coreAlpha << 24 | color);
        graphics.fill(centerX - radius + 1, centerY, centerX + radius, centerY + 1, coreAlpha << 24 | color);
        graphics.fill(centerX, centerY, centerX + 1, centerY + 1, 0xFFF7EFD4);
    }

    @Unique
    private static long cosmiccore$mix(long value) {
        value ^= value >>> 33;
        value *= 0xFF51AFD7ED558CCDL;
        value ^= value >>> 33;
        value *= 0xC4CEB9FE1A85EC53L;
        return value ^ value >>> 33;
    }
}
