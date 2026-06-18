package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderUplinkState;
import com.ghostipedia.cosmiccore.common.network.packet.StarLadderUplinkPackets;
import com.ghostipedia.cosmiccore.common.network.packet.StarLadderUplinkPackets.StarLadderUplinkClientState;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class StarLadderWidget extends WidgetGroup {

    public static final int WIDTH = 220;
    public static final int HEIGHT = 140;

    private static final int BUTTON_W = 140;
    private static final int BUTTON_H = 20;
    private static final int CONFIRM_BUTTON_W = 120;
    private static final int CONFIRM_BUTTON_H = 24;
    private static final int ABORT_BUTTON_W = 50;
    private static final int ABORT_BUTTON_H = 14;
    private static final int WARNING_COLOR = 0xFFFF4444;
    private static final int HOLD_TICKS_REQUIRED = 30;

    private final Supplier<StarLadderMachine> machineSupplier;

    private boolean isLinked = false;
    private boolean partnerOnline = false;
    private String partnerName = "";
    private String partnerDimension = "";
    private String partnerCoords = "";
    private int partnerTier = 0;

    private StarLadderUplinkState uplinkState = StarLadderUplinkState.IDLE;
    private float animPhase = 0f;

    private boolean isHoldingInitiate = false;
    private boolean isHoldingConfirm = false;
    private int holdTicks = 0;

    private LinkStatusWidget linkStatusWidget;
    private UplinkOverlayWidget uplinkOverlay;

    public StarLadderWidget(Supplier<StarLadderMachine> machineSupplier) {
        super(0, 0, WIDTH, HEIGHT);
        this.machineSupplier = machineSupplier;
        initWidgets();
    }

    private void initWidgets() {
        addWidget(new StarLadderBackgroundWidget(0, 0, WIDTH, HEIGHT));

        linkStatusWidget = new LinkStatusWidget(
                5, 5, WIDTH - 10, 50,
                () -> isLinked,
                () -> partnerOnline,
                () -> partnerName,
                () -> partnerDimension + " " + partnerCoords,
                () -> partnerTier >= 0 ? partnerTier : 0);
        addWidget(linkStatusWidget);

        uplinkOverlay = new UplinkOverlayWidget(0, 0, WIDTH, HEIGHT);
        addWidget(uplinkOverlay);
    }

    // ---- Data Sync ----

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        syncPartnerData(buffer);
        syncUplinkState(buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        readPartnerData(buffer);
        readUplinkState(buffer);
    }

    private void syncPartnerData(FriendlyByteBuf buffer) {
        StarLadderMachine machine = machineSupplier.get();
        if (machine == null) {
            buffer.writeBoolean(false);
            return;
        }

        GlobalPos hub = machine.getLinkedPartners().stream().findFirst().orElse(null);
        boolean linked = hub != null;
        buffer.writeBoolean(linked);

        if (linked) {
            var partner = machine.getLinkedPartnerMachine(hub);
            boolean online = partner != null;
            buffer.writeBoolean(online);

            buffer.writeUtf(LinkedMultiblockHelper.getDimensionName(hub.dimension().location()));
            buffer.writeUtf("[%d, %d, %d]".formatted(hub.pos().getX(), hub.pos().getY(), hub.pos().getZ()));

            if (online &&
                    partner instanceof com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderResearchHubMachine researchHub) {
                buffer.writeInt(researchHub.getRingTier());
            } else {
                buffer.writeInt(-1);
            }
        }
    }

    private void syncUplinkState(FriendlyByteBuf buffer) {
        StarLadderMachine machine = machineSupplier.get();
        if (machine == null) {
            buffer.writeEnum(StarLadderUplinkState.IDLE);
            return;
        }
        buffer.writeEnum(machine.getUplinkManager().getState());
    }

    private void readPartnerData(FriendlyByteBuf buffer) {
        isLinked = buffer.readBoolean();

        if (isLinked) {
            partnerOnline = buffer.readBoolean();
            partnerDimension = buffer.readUtf();
            partnerCoords = buffer.readUtf();
            partnerTier = buffer.readInt();
            partnerName = Component.translatable("cosmiccore.star_ladder.hub_name").getString();
        } else {
            partnerOnline = false;
            partnerName = "";
            partnerDimension = "";
            partnerCoords = "";
            partnerTier = -1;
        }
    }

    private void readUplinkState(FriendlyByteBuf buffer) {
        uplinkState = buffer.readEnum(StarLadderUplinkState.class);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        StarLadderMachine machine = machineSupplier.get();
        if (machine == null) return;

        GlobalPos hub = machine.getLinkedPartners().stream().findFirst().orElse(null);
        boolean newLinked = hub != null;

        if (newLinked != isLinked) {
            writeUpdateInfo(401, this::syncPartnerData);
        } else if (newLinked) {
            var partner = machine.getLinkedPartnerMachine(hub);
            boolean newOnline = partner != null;

            if (newOnline != partnerOnline) {
                writeUpdateInfo(401, this::syncPartnerData);
            } else if (newOnline &&
                    partner instanceof com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderResearchHubMachine researchHub) {
                        if (researchHub.getRingTier() != partnerTier) {
                            writeUpdateInfo(401, this::syncPartnerData);
                        }
                    }
        }

        StarLadderUplinkState newUplinkState = machine.getUplinkManager().getState();
        if (newUplinkState != uplinkState) {
            uplinkState = newUplinkState;
            writeUpdateInfo(402, buf -> buf.writeEnum(newUplinkState));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 401) {
            readPartnerData(buffer);
        } else if (id == 402) {
            uplinkState = buffer.readEnum(StarLadderUplinkState.class);
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    // ---- Update & Draw ----

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.05f;

        StarLadderUplinkState clientState = StarLadderUplinkClientState.getState();
        if (clientState != StarLadderUplinkState.IDLE) {
            uplinkState = clientState;
        }

        boolean showLink = uplinkState == StarLadderUplinkState.IDLE || uplinkState == StarLadderUplinkState.COMPLETED;
        linkStatusWidget.setVisible(showLink);
        linkStatusWidget.setActive(showLink);

        if (isHoldingInitiate || isHoldingConfirm) {
            holdTicks++;
            if (holdTicks >= HOLD_TICKS_REQUIRED) {
                StarLadderMachine machine = machineSupplier.get();
                if (machine != null) {
                    if (isHoldingInitiate) {
                        StarLadderUplinkPackets.sendInitiate(machine.getPos());
                        playUISound(SoundEvents.UI_BUTTON_CLICK.value(), 0.7f, 0.6f);
                    } else {
                        StarLadderUplinkPackets.sendConfirm(machine.getPos());
                        playUISound(SoundEvents.BEACON_ACTIVATE, 0.5f, 1.5f);
                    }
                }
                isHoldingInitiate = false;
                isHoldingConfirm = false;
                holdTicks = 0;
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int borderColor = isLinked && partnerOnline ? 0x804080C0 : 0x80404060;
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        switch (uplinkState) {
            case IDLE -> {
                drawTitle(graphics, x, y, w);
                drawPartnerTierDisplay(graphics, x, y, w, h);
                if (isLinked && partnerOnline) {
                    drawInitiateButton(graphics, x, y, w, h, mouseX, mouseY);
                }
            }
            case AWAITING_CONFIRMATION -> drawConfirmationScreen(graphics, x, y, w, h, mouseX, mouseY);
            case COMPLETED -> {
                drawTitle(graphics, x, y, w);
                drawCompletedDisplay(graphics, x, y, w, h);
            }
            default -> {
                drawTitle(graphics, x, y, w);
                if (uplinkState.isFightState()) {
                    drawAbortButton(graphics, x, y, w, h, mouseX, mouseY);
                }
            }
        }
    }

    // ---- Click Handling ----

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        if (uplinkState == StarLadderUplinkState.IDLE && isLinked && partnerOnline) {
            int btnX = x + (w - BUTTON_W) / 2;
            int btnY = y + h - BUTTON_H - 20;
            if (isInRect(mouseX, mouseY, btnX, btnY, BUTTON_W, BUTTON_H)) {
                isHoldingInitiate = true;
                holdTicks = 0;
                playUISound(SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, 0.5f, 0.8f);
                return true;
            }
        }

        if (uplinkState == StarLadderUplinkState.AWAITING_CONFIRMATION) {
            int btnX = x + (w - CONFIRM_BUTTON_W) / 2;
            int btnY = y + h - CONFIRM_BUTTON_H - 15;
            if (isInRect(mouseX, mouseY, btnX, btnY, CONFIRM_BUTTON_W, CONFIRM_BUTTON_H)) {
                isHoldingConfirm = true;
                holdTicks = 0;
                playUISound(SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, 0.5f, 0.6f);
                return true;
            }
        }

        if (uplinkState.isFightState()) {
            int abortX = x + w - ABORT_BUTTON_W - 5;
            int abortY = y + h - ABORT_BUTTON_H - 4;
            if (isInRect(mouseX, mouseY, abortX, abortY, ABORT_BUTTON_W, ABORT_BUTTON_H)) {
                StarLadderMachine machine = machineSupplier.get();
                if (machine != null) {
                    StarLadderUplinkPackets.sendAbort(machine.getPos());
                    StarLadderUplinkClientState.reset();
                    uplinkState = StarLadderUplinkState.IDLE;
                    playUISound(SoundEvents.BEACON_DEACTIVATE, 0.5f, 1.2f);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (isHoldingInitiate || isHoldingConfirm)) {
            isHoldingInitiate = false;
            isHoldingConfirm = false;
            holdTicks = 0;
            playUISound(SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, 0.4f, 0.6f);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isInRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    // ---- Drawing ----

    private void drawTitle(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;
        String title = Component.translatable("cosmiccore.star_ladder.title").getString();
        int titleX = x + (w - font.width(title)) / 2;
        int titleY = y + HEIGHT - font.lineHeight - 4;
        graphics.drawString(font, title, titleX, titleY, 0xFF6080B0, false);
    }

    private void drawPartnerTierDisplay(GuiGraphics graphics, int x, int y, int w, int h) {
        if (!isLinked || partnerTier < 0) return;

        var font = Minecraft.getInstance().font;
        int panelY = y + 60;
        int panelH = 34;

        DrawerHelper.drawSolidRect(graphics, x + 5, panelY, w - 10, panelH, 0x40102030);

        String tierText = Component.translatable("cosmiccore.star_ladder.hub_tier", partnerTier).getString();
        int tierColor = getTierColor(partnerTier);
        graphics.drawString(font, tierText, x + 10, panelY + 6, tierColor, false);

        int barY = panelY + 18;
        int barW = w - 20;
        int barH = 10;

        DrawerHelper.drawSolidRect(graphics, x + 10, barY, barW, barH, 0xFF202030);

        float fillProgress = (partnerTier + 1) / 4f;
        int fillW = (int) (barW * fillProgress);
        if (fillW > 0) {
            DrawerHelper.drawGradientRect(graphics, x + 10, barY, fillW, barH,
                    darkenColor(tierColor, 0.6f), tierColor, true);
        }

        for (int i = 1; i <= 3; i++) {
            int markerX = x + 10 + (barW * i) / 4;
            graphics.fill(markerX, barY, markerX + 1, barY + barH, 0x80000000);
        }
    }

    private void drawInitiateButton(GuiGraphics graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;

        int btnX = x + (w - BUTTON_W) / 2;
        int btnY = y + h - BUTTON_H - 20;
        boolean hovered = isInRect(mouseX, mouseY, btnX, btnY, BUTTON_W, BUTTON_H);

        float pulse = Mth.sin(animPhase * 2f) * 0.15f + 0.85f;
        int bgAlpha = hovered ? 0x80 : (int) (0x50 * pulse);
        DrawerHelper.drawSolidRect(graphics, btnX, btnY, BUTTON_W, BUTTON_H, (bgAlpha << 24) | 0x402040);

        int borderColor = hovered ? 0xC08040C0 : 0x806040A0;
        DrawerHelper.drawBorder(graphics, btnX, btnY, BUTTON_W, BUTTON_H, borderColor, 1);

        String text = Component.translatable("cosmiccore.star_ladder.initiate").getString();
        int textColor = hovered ? 0xFFDDCCFF : 0xFFA080C0;
        int textX = btnX + (BUTTON_W - font.width(text)) / 2;
        int textY = btnY + (BUTTON_H - font.lineHeight) / 2;
        graphics.drawString(font, text, textX, textY, textColor, false);

        if (isHoldingInitiate) {
            drawHoldBar(graphics, btnX, btnY, BUTTON_W, BUTTON_H, 0xFFA060D0);
        }
    }

    private void drawConfirmationScreen(GuiGraphics graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;

        DrawerHelper.drawSolidRect(graphics, x + 5, y + 5, w - 10, h - 10, 0xC0080810);

        String title = Component.translatable("cosmiccore.star_ladder.interrupted").getString();
        float titlePulse = Mth.sin(animPhase * 3f) * 0.3f + 0.7f;
        int titleAlpha = (int) (255 * titlePulse);
        graphics.drawString(font, title, x + (w - font.width(title)) / 2, y + 15,
                (titleAlpha << 24) | 0xFF4444, false);

        int lineColor = 0xFFA0A0B0;
        graphics.drawString(font, Component.translatable("cosmiccore.star_ladder.resisting").getString(),
                x + 15, y + 35, lineColor, false);
        graphics.drawString(font, Component.translatable("cosmiccore.star_ladder.demands_soul").getString(),
                x + 15, y + 50, lineColor, false);
        graphics.drawString(font, Component.translatable("cosmiccore.star_ladder.drain_rate", 5000).getString(),
                x + 15, y + 65, WARNING_COLOR, false);

        int btnX = x + (w - CONFIRM_BUTTON_W) / 2;
        int btnY = y + h - CONFIRM_BUTTON_H - 15;
        boolean hovered = isInRect(mouseX, mouseY, btnX, btnY, CONFIRM_BUTTON_W, CONFIRM_BUTTON_H);

        float btnPulse = Mth.sin(animPhase * 4f) * 0.2f + 0.8f;
        int btnBgAlpha = hovered ? 0xA0 : (int) (0x60 * btnPulse);
        DrawerHelper.drawSolidRect(graphics, btnX, btnY, CONFIRM_BUTTON_W, CONFIRM_BUTTON_H,
                (btnBgAlpha << 24) | 0x801020);

        DrawerHelper.drawBorder(graphics, btnX, btnY, CONFIRM_BUTTON_W, CONFIRM_BUTTON_H,
                hovered ? 0xFFCC4444 : 0xC0993333, 1);

        String confirmText = Component.translatable("cosmiccore.star_ladder.confirm").getString();
        graphics.drawString(font, confirmText,
                btnX + (CONFIRM_BUTTON_W - font.width(confirmText)) / 2,
                btnY + (CONFIRM_BUTTON_H - font.lineHeight) / 2,
                hovered ? 0xFFFFAAAA : 0xFFCC6666, false);

        if (isHoldingConfirm) {
            drawHoldBar(graphics, btnX, btnY, CONFIRM_BUTTON_W, CONFIRM_BUTTON_H, 0xFFCC4444);
        }
    }

    private void drawCompletedDisplay(GuiGraphics graphics, int x, int y, int w, int h) {
        var font = Minecraft.getInstance().font;

        String text = Component.translatable("cosmiccore.star_ladder.established").getString();
        float pulse = Mth.sin(animPhase) * 0.2f + 0.8f;
        int alpha = (int) (255 * pulse);
        graphics.drawString(font, text, x + (w - font.width(text)) / 2,
                y + h / 2 - font.lineHeight / 2, (alpha << 24) | 0x40CC40, false);
    }

    private void drawAbortButton(GuiGraphics graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;

        int btnX = x + w - ABORT_BUTTON_W - 5;
        int btnY = y + h - ABORT_BUTTON_H - 4;
        boolean hovered = isInRect(mouseX, mouseY, btnX, btnY, ABORT_BUTTON_W, ABORT_BUTTON_H);

        int bg = hovered ? 0x80401010 : 0x40301010;
        DrawerHelper.drawSolidRect(graphics, btnX, btnY, ABORT_BUTTON_W, ABORT_BUTTON_H, bg);
        DrawerHelper.drawBorder(graphics, btnX, btnY, ABORT_BUTTON_W, ABORT_BUTTON_H,
                hovered ? 0xC0CC4444 : 0x60993333, 1);

        String text = Component.translatable("cosmiccore.star_ladder.abort").getString();
        int textColor = hovered ? 0xFFFF6666 : 0xFF884444;
        graphics.drawString(font, text,
                btnX + (ABORT_BUTTON_W - font.width(text)) / 2,
                btnY + (ABORT_BUTTON_H - font.lineHeight) / 2,
                textColor, false);
    }

    // ---- Utility ----

    private void drawHoldBar(GuiGraphics graphics, int btnX, int btnY, int btnW, int btnH, int color) {
        float progress = (float) holdTicks / HOLD_TICKS_REQUIRED;
        int barH = 2;
        int barY = btnY + btnH - barH;
        int fillW = (int) (btnW * progress);
        if (fillW > 0) {
            DrawerHelper.drawSolidRect(graphics, btnX, barY, fillW, barH, color);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playUISound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    private int getTierColor(int tier) {
        return switch (tier) {
            case 0 -> 0xFF4080C0;
            case 1 -> 0xFF40C080;
            case 2 -> 0xFFC0A040;
            case 3 -> 0xFFC040C0;
            default -> 0xFF606080;
        };
    }

    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
