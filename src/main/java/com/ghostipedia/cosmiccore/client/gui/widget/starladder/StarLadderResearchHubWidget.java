package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderResearchHubMachine;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class StarLadderResearchHubWidget extends WidgetGroup {

    public static final int WIDTH = 280;
    public static final int HEIGHT = 180;

    private final Supplier<StarLadderResearchHubMachine> machineSupplier;

    private int ringTier = 0;
    private boolean isLinked = false;
    private boolean partnerOnline = false;
    private String partnerDimension = "";
    private String partnerCoords = "";
    private boolean previewEnabled = false;
    private boolean canUpgrade = false;
    private Map<Block, Integer> nextTierRequirements = new HashMap<>();

    private float animPhase = 0f;

    public StarLadderResearchHubWidget(Supplier<StarLadderResearchHubMachine> machineSupplier) {
        super(0, 0, WIDTH, HEIGHT);
        this.machineSupplier = machineSupplier;
        initWidgets();
    }

    private void initWidgets() {
        addWidget(new StarLadderBackgroundWidget(0, 0, WIDTH, HEIGHT));

        // Left side: Telemetry panel (fake sci-fi data)
        int telemetryX = 5;
        int telemetryY = 5;
        int telemetryW = 120;
        int telemetryH = 100;

        addWidget(new TelemetryPanelWidget(telemetryX, telemetryY, telemetryW, telemetryH, () -> ringTier));

        // Right side top: Link status
        int linkStatusX = telemetryW + 10;
        int linkStatusY = 5;
        int linkStatusW = WIDTH - linkStatusX - 5;
        int linkStatusH = 55;

        addWidget(new LinkStatusWidget(
                linkStatusX, linkStatusY, linkStatusW, linkStatusH,
                () -> isLinked,
                () -> partnerOnline,
                () -> "Star Ladder",
                () -> partnerDimension + " " + partnerCoords,
                () -> ringTier));

        // Right side bottom: Next tier requirements (scrollable)
        int reqsX = telemetryW + 10;
        int reqsY = linkStatusY + linkStatusH + 5;
        int reqsW = WIDTH - reqsX - 5;
        int reqsH = HEIGHT - reqsY - 18; // Leave room for title at bottom

        addWidget(new NextTierRequirementsWidget(
                reqsX, reqsY, reqsW, reqsH,
                () -> ringTier,
                () -> canUpgrade,
                () -> nextTierRequirements));
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        syncAllData(buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        readAllData(buffer);
    }

    private void syncAllData(FriendlyByteBuf buffer) {
        StarLadderResearchHubMachine machine = machineSupplier.get();
        if (machine == null) {
            buffer.writeInt(0);
            buffer.writeBoolean(false);
            buffer.writeBoolean(false);
            buffer.writeBoolean(false);
            buffer.writeInt(0); // No requirements
            return;
        }

        buffer.writeInt(machine.getRingTier());
        buffer.writeBoolean(machine.isRingPreviewEnabled());
        buffer.writeBoolean(machine.canUpgrade());

        GlobalPos ladder = machine.getLinkedPartners().stream().findFirst().orElse(null);
        boolean linked = ladder != null;
        buffer.writeBoolean(linked);

        if (linked) {
            var partner = machine.getLinkedPartnerMachine(ladder);
            boolean online = partner != null;
            buffer.writeBoolean(online);
            buffer.writeUtf(LinkedMultiblockHelper.getDimensionName(ladder.dimension().location()));
            buffer.writeUtf("[%d, %d, %d]".formatted(ladder.pos().getX(), ladder.pos().getY(), ladder.pos().getZ()));
        }

        // Sync next tier requirements
        Map<Block, Integer> reqs = machine.getNextRingBlockCounts();
        buffer.writeInt(reqs.size());
        for (Map.Entry<Block, Integer> entry : reqs.entrySet()) {
            buffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(entry.getKey()));
            buffer.writeInt(entry.getValue());
        }
    }

    private void readAllData(FriendlyByteBuf buffer) {
        ringTier = buffer.readInt();
        previewEnabled = buffer.readBoolean();
        canUpgrade = buffer.readBoolean();
        isLinked = buffer.readBoolean();

        if (isLinked) {
            partnerOnline = buffer.readBoolean();
            partnerDimension = buffer.readUtf();
            partnerCoords = buffer.readUtf();
        } else {
            partnerOnline = false;
            partnerDimension = "";
            partnerCoords = "";
        }

        // Read next tier requirements
        nextTierRequirements.clear();
        int reqCount = buffer.readInt();
        for (int i = 0; i < reqCount; i++) {
            Block block = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
            int count = buffer.readInt();
            if (block != null) {
                nextTierRequirements.put(block, count);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        StarLadderResearchHubMachine machine = machineSupplier.get();
        if (machine == null) return;

        boolean needsUpdate = false;

        if (machine.getRingTier() != ringTier) needsUpdate = true;
        if (machine.isRingPreviewEnabled() != previewEnabled) needsUpdate = true;
        if (machine.canUpgrade() != canUpgrade) needsUpdate = true;

        GlobalPos ladder = machine.getLinkedPartners().stream().findFirst().orElse(null);
        boolean newLinked = ladder != null;
        if (newLinked != isLinked) needsUpdate = true;

        if (newLinked && !needsUpdate) {
            var partner = machine.getLinkedPartnerMachine(ladder);
            if ((partner != null) != partnerOnline) needsUpdate = true;
        }

        if (needsUpdate) {
            ringTier = machine.getRingTier();
            previewEnabled = machine.isRingPreviewEnabled();
            canUpgrade = machine.canUpgrade();
            isLinked = newLinked;

            writeUpdateInfo(501, buf -> syncAllData(buf));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 501) {
            readAllData(buffer);
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

        int tierColor = getTierColor(ringTier);
        int borderColor = adjustAlpha(tierColor, 0.5f);
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        if (ringTier > 0) {
            int glowColor = adjustAlpha(tierColor, 0.1f);
            DrawerHelper.drawGradientRect(graphics, x + 1, y + 1, w - 2, 15, glowColor, 0x00000000, false);
        }

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        drawTitle(graphics, x, y, w, h);
        drawTierIndicator(graphics, x, y, w, h);
        drawPreviewIndicator(graphics, x, y, w, h);
    }

    private void drawTitle(GuiGraphics graphics, int x, int y, int w, int h) {
        var font = Minecraft.getInstance().font;
        String title = "RESEARCH HUB";
        int titleX = x + (w - font.width(title)) / 2;
        int titleY = y + h - font.lineHeight - 4;
        int titleColor = getTierColor(ringTier);
        graphics.drawString(font, title, titleX, titleY, titleColor, false);
    }

    private void drawTierIndicator(GuiGraphics graphics, int x, int y, int w, int h) {
        // Draw current tier in bottom left of telemetry panel area
        var font = Minecraft.getInstance().font;
        int tierColor = getTierColor(ringTier);

        String tierText = "T" + ringTier;
        float pulse = Mth.sin(animPhase * 2f) * 0.1f + 0.9f;
        int alpha = (int) (0xFF * pulse);
        int color = (alpha << 24) | (tierColor & 0x00FFFFFF);

        graphics.drawString(font, tierText, x + 8, y + 108, color, false);
    }

    private void drawPreviewIndicator(GuiGraphics graphics, int x, int y, int w, int h) {
        if (!previewEnabled) return;

        var font = Minecraft.getInstance().font;

        float pulse = Mth.sin(animPhase * 3f) * 0.3f + 0.7f;
        int alpha = (int) (0xFF * pulse);
        int color = (alpha << 24) | 0x40FF80;

        String text = "PREVIEW ACTIVE";
        int textX = x + 5;
        int textY = y + 120;

        graphics.drawString(font, text, textX, textY, color, false);

        graphics.fill(textX + font.width(text) + 4, textY + 2,
                textX + font.width(text) + 8, textY + 6, color);
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

    private int adjustAlpha(int color, float factor) {
        int a = (int) (((color >> 24) & 0xFF) * factor);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
