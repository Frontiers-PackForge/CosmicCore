package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderMachine;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class StarLadderWidget extends WidgetGroup {

    public static final int WIDTH = 220;
    public static final int HEIGHT = 140;

    private final Supplier<StarLadderMachine> machineSupplier;

    private boolean isLinked = false;
    private boolean partnerOnline = false;
    private String partnerName = "";
    private String partnerDimension = "";
    private String partnerCoords = "";
    private int partnerTier = 0;

    private float animPhase = 0f;

    public StarLadderWidget(Supplier<StarLadderMachine> machineSupplier) {
        super(0, 0, WIDTH, HEIGHT);
        this.machineSupplier = machineSupplier;
        initWidgets();
    }

    private void initWidgets() {
        addWidget(new StarLadderBackgroundWidget(0, 0, WIDTH, HEIGHT));

        int linkStatusX = 5;
        int linkStatusY = 5;
        int linkStatusW = WIDTH - 10;
        int linkStatusH = 50;

        addWidget(new LinkStatusWidget(
                linkStatusX, linkStatusY, linkStatusW, linkStatusH,
                () -> isLinked,
                () -> partnerOnline,
                () -> partnerName,
                () -> partnerDimension + " " + partnerCoords,
                () -> partnerTier >= 0 ? partnerTier : 0));
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        syncPartnerData(buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        readPartnerData(buffer);
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

    private void readPartnerData(FriendlyByteBuf buffer) {
        isLinked = buffer.readBoolean();

        if (isLinked) {
            partnerOnline = buffer.readBoolean();
            partnerDimension = buffer.readUtf();
            partnerCoords = buffer.readUtf();
            partnerTier = buffer.readInt();
            partnerName = "Research Hub";
        } else {
            partnerOnline = false;
            partnerName = "";
            partnerDimension = "";
            partnerCoords = "";
            partnerTier = -1;
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        StarLadderMachine machine = machineSupplier.get();
        if (machine == null) return;

        GlobalPos hub = machine.getLinkedPartners().stream().findFirst().orElse(null);
        boolean newLinked = hub != null;

        if (newLinked != isLinked) {
            writeUpdateInfo(401, buf -> syncPartnerData(buf));
        } else if (newLinked) {
            var partner = machine.getLinkedPartnerMachine(hub);
            boolean newOnline = partner != null;

            if (newOnline != partnerOnline) {
                writeUpdateInfo(401, buf -> syncPartnerData(buf));
            } else if (newOnline &&
                    partner instanceof com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderResearchHubMachine researchHub) {
                        if (researchHub.getRingTier() != partnerTier) {
                            writeUpdateInfo(401, buf -> syncPartnerData(buf));
                        }
                    }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 401) {
            readPartnerData(buffer);
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.05f;
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

        drawTitle(graphics, x, y, w);
        drawPartnerTierDisplay(graphics, x, y, w, h);
    }

    private void drawTitle(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;
        String title = "STAR LADDER";
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

        String tierText = "Hub Tier: T" + partnerTier;
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
