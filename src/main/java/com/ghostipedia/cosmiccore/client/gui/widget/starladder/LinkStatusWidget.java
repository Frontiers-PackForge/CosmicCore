package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class LinkStatusWidget extends Widget {

    private static final int[] TIER_COLORS = {
            0xFF4080C0, // T0 - Blue
            0xFF40C080, // T1 - Green
            0xFFC0A040, // T2 - Gold
            0xFFC040C0  // T3 - Purple
    };

    private final Supplier<Boolean> linkedSupplier;
    private final Supplier<Boolean> partnerOnlineSupplier;
    private final Supplier<String> partnerNameSupplier;
    private final Supplier<String> partnerLocationSupplier;
    private final java.util.function.IntSupplier tierSupplier;

    private float pulsePhase = 0f;
    private float connectionAnim = 0f;
    private float dataPacketPhase = 0f;

    public LinkStatusWidget(int x, int y, int width, int height,
                            Supplier<Boolean> linkedSupplier,
                            Supplier<Boolean> partnerOnlineSupplier,
                            Supplier<String> partnerNameSupplier,
                            Supplier<String> partnerLocationSupplier,
                            java.util.function.IntSupplier tierSupplier) {
        super(x, y, width, height);
        this.linkedSupplier = linkedSupplier;
        this.partnerOnlineSupplier = partnerOnlineSupplier;
        this.partnerNameSupplier = partnerNameSupplier;
        this.partnerLocationSupplier = partnerLocationSupplier;
        this.tierSupplier = tierSupplier;
    }

    // Legacy constructor for backwards compatibility
    public LinkStatusWidget(int x, int y, int width, int height,
                            Supplier<Boolean> linkedSupplier,
                            Supplier<Boolean> partnerOnlineSupplier,
                            Supplier<String> partnerNameSupplier,
                            Supplier<String> partnerLocationSupplier) {
        this(x, y, width, height, linkedSupplier, partnerOnlineSupplier,
                partnerNameSupplier, partnerLocationSupplier, () -> 0);
    }

    private int getTierColor() {
        int tier = tierSupplier.getAsInt();
        if (tier < 0) return TIER_COLORS[0];
        if (tier >= TIER_COLORS.length) return TIER_COLORS[TIER_COLORS.length - 1];
        return TIER_COLORS[tier];
    }

    private int adjustAlpha(int color, float factor) {
        int a = (int) (((color >> 24) & 0xFF) * factor);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        pulsePhase += 0.08f;

        boolean online = partnerOnlineSupplier.get();
        float targetAnim = online ? 1f : 0f;
        connectionAnim = Mth.lerp(0.1f, connectionAnim, targetAnim);

        // Animate data packets traveling along the line when online
        if (online) {
            dataPacketPhase += 0.04f;
            if (dataPacketPhase > 1f) {
                dataPacketPhase = 0f;
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int tierColor = getTierColor();
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, 0xA0101828);
        DrawerHelper.drawBorder(graphics, x, y, w, h, adjustAlpha(tierColor, 0.5f), 1);

        var font = Minecraft.getInstance().font;
        boolean isLinked = linkedSupplier.get();

        if (!isLinked) {
            drawUnlinkedState(graphics, x, y, w, h, font);
            return;
        }

        boolean online = partnerOnlineSupplier.get();
        String partnerName = partnerNameSupplier.get();
        String location = partnerLocationSupplier.get();

        drawConnectionLine(graphics, x, y, w, h, online);
        drawPartnerInfo(graphics, x, y, w, h, font, partnerName, location, online);
    }

    private void drawUnlinkedState(GuiGraphics graphics, int x, int y, int w, int h,
                                   net.minecraft.client.gui.Font font) {
        float pulse = Mth.sin(pulsePhase) * 0.2f + 0.8f;
        int alpha = (int) (0x80 * pulse);
        int textColor = (alpha << 24) | 0x606080;

        String text = "NO LINK";
        int textX = x + (w - font.width(text)) / 2;
        int textY = y + (h - font.lineHeight) / 2;
        graphics.drawString(font, text, textX, textY, textColor, false);

        int dotX = x + w / 2;
        int dotY = y + h - 8;
        int dotColor = (alpha << 24) | 0x404060;
        graphics.fill(dotX - 2, dotY - 2, dotX + 2, dotY + 2, dotColor);
    }

    private void drawConnectionLine(GuiGraphics graphics, int x, int y, int w, int h, boolean online) {
        int tierColor = getTierColor();
        int lineY = y + h - 6;
        int lineStartX = x + 10;
        int lineEndX = x + w - 10;
        int lineWidth = lineEndX - lineStartX;

        // Background track
        DrawerHelper.drawSolidRect(graphics, lineStartX, lineY, lineWidth, 2, 0xFF202040);

        if (connectionAnim > 0.01f) {
            int connectedWidth = (int) (lineWidth * connectionAnim);
            // Use tier color for the connection line when online
            int connColor = online ? tierColor : 0xFFFF8040;
            DrawerHelper.drawSolidRect(graphics, lineStartX, lineY, connectedWidth, 2, connColor);

            // Draw animated data packets traveling along the line when online
            if (online && connectedWidth > 20) {
                int numPackets = 3;
                for (int i = 0; i < numPackets; i++) {
                    float packetProgress = (dataPacketPhase + i * 0.33f) % 1.0f;
                    int packetX = lineStartX + (int) (connectedWidth * packetProgress);

                    // Packet glow effect
                    float packetBrightness = 0.6f + 0.4f * Mth.sin(pulsePhase * 4f + i * 2f);
                    int packetAlpha = (int) (0xFF * packetBrightness * connectionAnim);
                    int packetColor = (packetAlpha << 24) | (tierColor & 0x00FFFFFF);

                    // Draw packet as a small bright segment
                    int packetWidth = 6;
                    int drawX = Math.max(lineStartX,
                            Math.min(packetX - packetWidth / 2, lineStartX + connectedWidth - packetWidth));
                    graphics.fill(drawX, lineY - 1, drawX + packetWidth, lineY + 3, packetColor);

                    // Trailing glow
                    int trailAlpha = (int) (packetAlpha * 0.3f);
                    int trailColor = (trailAlpha << 24) | (tierColor & 0x00FFFFFF);
                    int trailX = Math.max(lineStartX, drawX - 4);
                    graphics.fill(trailX, lineY, drawX, lineY + 2, trailColor);
                }
            }

            // Pulse effect at the leading edge
            float pulse = Mth.sin(pulsePhase * 3f) * 0.3f + 0.7f;
            int pulseAlpha = (int) (0xFF * pulse * connectionAnim);
            int pulseX = lineStartX + connectedWidth - 4;
            if (pulseX > lineStartX) {
                int pulseColor = (pulseAlpha << 24) | (connColor & 0x00FFFFFF);
                graphics.fill(pulseX, lineY - 1, pulseX + 4, lineY + 3, pulseColor);
            }
        }

        // Start node (hub side) - use tier color
        int startDotColor = adjustAlpha(tierColor, 0.7f);
        graphics.fill(lineStartX - 2, lineY - 1, lineStartX + 2, lineY + 3, startDotColor);

        // End node (partner side) - brighter when connected
        int endDotAlpha = (int) (0xFF * connectionAnim);
        int endDotColor = online ? ((endDotAlpha << 24) | (tierColor & 0x00FFFFFF)) : ((endDotAlpha << 24) | 0xFF8040);
        graphics.fill(lineEndX - 2, lineY - 1, lineEndX + 2, lineY + 3, endDotColor);

        // Add a pulsing glow around end node when online
        if (online && connectionAnim > 0.5f) {
            float glowPulse = Mth.sin(pulsePhase * 2f) * 0.4f + 0.3f;
            int glowAlpha = (int) (0x60 * glowPulse * connectionAnim);
            int glowColor = (glowAlpha << 24) | (tierColor & 0x00FFFFFF);
            graphics.fill(lineEndX - 4, lineY - 3, lineEndX + 4, lineY + 5, glowColor);
        }
    }

    private void drawPartnerInfo(GuiGraphics graphics, int x, int y, int w, int h,
                                 net.minecraft.client.gui.Font font,
                                 String partnerName, String location, boolean online) {
        int statusColor = online ? 0xFF40FF80 : 0xFFFF6040;
        String statusText = online ? "ONLINE" : "OFFLINE";

        int statusX = x + 8;
        int statusY = y + 4;
        int maxTextWidth = w - 16; // Leave padding on both sides

        float pulse = online ? 1f : (Mth.sin(pulsePhase * 3f) * 0.3f + 0.7f);
        int pulseAlpha = (int) (0xFF * pulse);
        int dotColor = (pulseAlpha << 24) | (statusColor & 0x00FFFFFF);
        graphics.fill(statusX, statusY + 2, statusX + 4, statusY + 6, dotColor);

        graphics.drawString(font, statusText, statusX + 8, statusY, statusColor, false);

        int nameY = statusY + font.lineHeight + 2;
        String displayName = truncateText(font, partnerName, maxTextWidth);
        graphics.drawString(font, displayName, statusX, nameY, 0xFFB0C0D0, false);

        if (location != null && !location.isEmpty()) {
            int locY = nameY + font.lineHeight + 1;
            String displayLoc = truncateText(font, location, maxTextWidth);
            graphics.drawString(font, displayLoc, statusX, locY, 0xFF707890, false);
        }
    }

    private String truncateText(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (font.width(sb.toString() + text.charAt(i)) + ellipsisWidth > maxWidth) {
                return sb.toString() + ellipsis;
            }
            sb.append(text.charAt(i));
        }
        return sb.toString();
    }
}
