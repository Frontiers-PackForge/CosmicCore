package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarIrisUpgrade;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Clean, performant upgrade tree widget.
 * Displays 4 branches in a vertical layout with clear visual hierarchy.
 */
public class UpgradeTreeWidget extends Widget {

    private static final int UPDATE_ID_SYNC = 420;
    private static final int CLIENT_ACTION_UNLOCK = 1;
    private static final int CLIENT_ACTION_RESPEC = 2;

    // Clean color palette
    private static final int BG_COLOR = 0xF0101018;
    private static final int HEADER_COLOR = 0xFF181820;
    private static final int BORDER_COLOR = 0xFF3A3A50;
    private static final int BORDER_ACCENT = 0xFF5A5A80;

    private static final int[] BRANCH_COLORS = {
            0xFFFF6644, // IGNITION - orange
            0xFF44AAFF, // FUSION - blue
            0xFFAA44FF, // COLLAPSE - purple
            0xFF44FFAA, // VOID - teal
            0xFFFFCC44  // ULTIMATE - gold
    };

    private static final String[] BRANCH_NAMES = { "IGNITION", "FUSION", "COLLAPSE", "VOID" };

    private final Supplier<IrisMultiblockMachine> machineSupplier;
    private final Runnable onClose;

    private boolean visible = false;
    private float fadeAlpha = 0f;
    private int animTick = 0;

    // Cached data
    private Set<StellarIrisUpgrade> unlockedUpgrades = EnumSet.noneOf(StellarIrisUpgrade.class);
    private int spendablePoints = 0;
    private int tier = 0;
    private int ascensionLevel = 0;

    // UI state
    private StellarIrisUpgrade hoveredUpgrade = null;
    private boolean showRespecConfirm = false;
    private int unlockFlashTick = 0;
    private StellarIrisUpgrade lastUnlocked = null;

    // Precomputed node positions
    private final Map<StellarIrisUpgrade, int[]> nodePositions = new HashMap<>();

    public UpgradeTreeWidget(int x, int y, int width, int height,
                             Supplier<IrisMultiblockMachine> machineSupplier,
                             Runnable onClose) {
        super(x, y, width, height);
        this.machineSupplier = machineSupplier;
        this.onClose = onClose;
        calculateNodePositions();
    }

    private void calculateNodePositions() {
        int w = getSize().width;
        int h = getSize().height;

        // Layout: 4 branches side by side, rows going down
        // Each branch gets ~1/4 of the width
        int branchWidth = w / 4;
        int startY = 35; // Below header
        int rowHeight = 24;
        int nodeSize = 9;

        for (StellarIrisUpgrade upgrade : StellarIrisUpgrade.values()) {
            int branchIdx = upgrade.getBranch().ordinal();
            int branchCenterX = branchWidth / 2 + branchIdx * branchWidth;
            int row = upgrade.getRow();

            // Get all upgrades in same row/branch for horizontal layout
            List<StellarIrisUpgrade> sameRowBranch = new ArrayList<>();
            for (StellarIrisUpgrade u : StellarIrisUpgrade.values()) {
                if (u.getBranch() == upgrade.getBranch() && u.getRow() == row) {
                    sameRowBranch.add(u);
                }
            }
            sameRowBranch.sort(Comparator.comparingInt(Enum::ordinal));

            int col = sameRowBranch.indexOf(upgrade);
            int colCount = sameRowBranch.size();
            int spacing = 22;
            int offsetX = (colCount == 1) ? 0 : (int) ((col - (colCount - 1) / 2.0f) * spacing);

            int nx = branchCenterX + offsetX;
            int ny = startY + (row - 1) * rowHeight;

            nodePositions.put(upgrade, new int[] { nx, ny });
        }
    }

    public void show() {
        visible = true;
        animTick = 0;
        syncFromMachine();
    }

    public void hide() {
        visible = false;
        showRespecConfirm = false;
    }

    public boolean isVisible() {
        return visible;
    }

    private void syncFromMachine() {
        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine != null) {
            Set<StellarIrisUpgrade> machineUpgrades = machine.getUnlockedUpgrades();
            if (machineUpgrades.isEmpty()) {
                unlockedUpgrades = EnumSet.noneOf(StellarIrisUpgrade.class);
            } else {
                unlockedUpgrades = EnumSet.copyOf(machineUpgrades);
            }
            spendablePoints = machine.getSpendablePoints();
            tier = machine.getPrestigeTier();
            ascensionLevel = machine.getAscensionLevel();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        if (visible) {
            fadeAlpha = Math.min(fadeAlpha + 0.15f, 1f);
            animTick++;
        } else {
            fadeAlpha = Math.max(fadeAlpha - 0.2f, 0f);
        }

        if (unlockFlashTick > 0) unlockFlashTick--;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        if (fadeAlpha <= 0f) return;

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        // Push pose for z-ordering - draw on top of everything
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);

        // Solid opaque background - ensures we cover the main UI
        int bgAlpha = (int) (240 * fadeAlpha);
        graphics.fill(x, y, x + w, y + h, (bgAlpha << 24) | 0x0C0C14);

        // Header bar
        drawHeader(graphics, x, y, w);

        // Update hovered
        updateHovered(mouseX, mouseY, x, y);

        // Branch separators and labels
        drawBranchSections(graphics, x, y, w, h);

        // Draw connections
        drawConnections(graphics, x, y);

        // Draw nodes
        for (StellarIrisUpgrade upgrade : StellarIrisUpgrade.values()) {
            drawNode(graphics, x, y, upgrade, mouseX, mouseY);
        }

        // Footer with controls hint
        drawFooter(graphics, x, y + h - 14, w);

        // Draw tooltip last (on top)
        if (hoveredUpgrade != null && !showRespecConfirm) {
            drawTooltip(graphics, mouseX, mouseY);
        }

        // Respec confirmation overlay
        if (showRespecConfirm) {
            drawRespecConfirm(graphics, x, y, w, h);
        }

        // Border
        int borderAlpha = (int) (255 * fadeAlpha);
        DrawerHelper.drawBorder(graphics, x, y, w, h, (borderAlpha << 24) | 0x4A4A60, 1);

        graphics.pose().popPose();
    }

    private void drawHeader(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;
        int alpha = (int) (255 * fadeAlpha);

        // Header background
        graphics.fill(x, y, x + w, y + 20, (alpha << 24) | 0x14141C);
        graphics.fill(x, y + 19, x + w, y + 20, ((int) (alpha * 0.5) << 24) | 0x4A4A60);

        // Title
        String title = "STELLAR CONVERGENCE";
        graphics.drawString(font, title, x + 8, y + 6, (alpha << 24) | 0xE0E0F0, false);

        // Points display
        String points = "\u2726 " + spendablePoints;
        int pointsW = font.width(points);
        int pointsColor = spendablePoints > 0 ? 0xFFCC44 : 0x808090;
        graphics.drawString(font, points, x + w - pointsW - 8, y + 6, (alpha << 24) | pointsColor, false);

        // Tier/Ascension indicator
        String tierStr = "T" + tier;
        if (ascensionLevel > 0) {
            tierStr = "\u2605".repeat(Math.min(ascensionLevel, 5)) + " " + tierStr;
        }
        int tierW = font.width(tierStr);
        graphics.drawString(font, tierStr, x + (w - tierW) / 2, y + 6, (alpha << 24) | 0x909090, false);
    }

    private void drawBranchSections(GuiGraphics graphics, int x, int y, int w, int h) {
        var font = Minecraft.getInstance().font;
        int alpha = (int) (255 * fadeAlpha);
        int branchWidth = w / 4;

        for (int i = 0; i < 4; i++) {
            int bx = x + i * branchWidth;

            // Vertical separator (except first)
            if (i > 0) {
                int sepAlpha = (int) (80 * fadeAlpha);
                graphics.fill(bx, y + 22, bx + 1, y + h - 30, (sepAlpha << 24) | 0x404060);
            }

            // Branch label
            String name = BRANCH_NAMES[i];
            int labelX = bx + (branchWidth - font.width(name)) / 2;
            int labelAlpha = (int) (200 * fadeAlpha);
            int labelColor = BRANCH_COLORS[i];
            graphics.drawString(font, name, labelX, y + 24, (labelAlpha << 24) | (labelColor & 0xFFFFFF), false);
        }

        // Ultimate label at bottom
        String ultLabel = "ASCENSION";
        int ultX = x + (w - font.width(ultLabel)) / 2;
        int ultAlpha = (int) (180 * fadeAlpha);
        graphics.drawString(font, ultLabel, ultX, y + h - 40, (ultAlpha << 24) | 0xFFCC44, false);
    }

    private void drawFooter(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;
        int alpha = (int) (150 * fadeAlpha);
        String hint = "[ESC] Close    [R] Respec";
        int hintW = font.width(hint);
        graphics.drawString(font, hint, x + (w - hintW) / 2, y, (alpha << 24) | 0x606070, false);
    }

    private void updateHovered(int mouseX, int mouseY, int ox, int oy) {
        hoveredUpgrade = null;
        for (Map.Entry<StellarIrisUpgrade, int[]> entry : nodePositions.entrySet()) {
            int[] pos = entry.getValue();
            int nx = ox + pos[0];
            int ny = oy + pos[1];
            int size = getNodeSize(entry.getKey());

            if (mouseX >= nx - size && mouseX <= nx + size &&
                    mouseY >= ny - size && mouseY <= ny + size) {
                hoveredUpgrade = entry.getKey();
                break;
            }
        }
    }

    private int getNodeSize(StellarIrisUpgrade upgrade) {
        if (upgrade.isCapstone()) return 10;
        return 8;
    }

    private void drawConnections(GuiGraphics graphics, int ox, int oy) {
        for (StellarIrisUpgrade upgrade : StellarIrisUpgrade.values()) {
            int[] pos = nodePositions.get(upgrade);
            if (pos == null) continue;

            for (StellarIrisUpgrade prereq : upgrade.getPrerequisites()) {
                int[] prereqPos = nodePositions.get(prereq);
                if (prereqPos == null) continue;

                int x1 = ox + prereqPos[0];
                int y1 = oy + prereqPos[1];
                int x2 = ox + pos[0];
                int y2 = oy + pos[1];

                boolean owned = unlockedUpgrades.contains(prereq) && unlockedUpgrades.contains(upgrade);
                boolean prereqOwned = unlockedUpgrades.contains(prereq);

                int lineAlpha;
                int lineColor;

                if (owned) {
                    lineAlpha = (int) (200 * fadeAlpha);
                    lineColor = BRANCH_COLORS[upgrade.getBranch().ordinal()];
                } else if (prereqOwned) {
                    lineAlpha = (int) (100 * fadeAlpha);
                    lineColor = 0x606080;
                } else {
                    lineAlpha = (int) (40 * fadeAlpha);
                    lineColor = 0x303040;
                }

                drawLine(graphics, x1, y1, x2, y2, (lineAlpha << 24) | (lineColor & 0xFFFFFF));
            }
        }
    }

    private void drawNode(GuiGraphics graphics, int ox, int oy, StellarIrisUpgrade upgrade, int mouseX, int mouseY) {
        int[] pos = nodePositions.get(upgrade);
        if (pos == null) return;

        int nx = ox + pos[0];
        int ny = oy + pos[1];
        int size = getNodeSize(upgrade);

        boolean owned = unlockedUpgrades.contains(upgrade);
        boolean available = !owned && upgrade.canUnlock(unlockedUpgrades, tier) && spendablePoints >= upgrade.getCost();
        boolean tierLocked = tier < upgrade.getRequiredTier();
        boolean isHovered = upgrade == hoveredUpgrade;
        boolean justUnlocked = upgrade == lastUnlocked && unlockFlashTick > 0;

        int branchColor = BRANCH_COLORS[upgrade.getBranch().ordinal()];
        int alpha = (int) (255 * fadeAlpha);

        // Flash effect
        if (justUnlocked) {
            float flash = unlockFlashTick / 15f;
            int flashAlpha = (int) (150 * flash * fadeAlpha);
            int flashSize = size + (int) (8 * flash);
            graphics.fill(nx - flashSize, ny - flashSize, nx + flashSize, ny + flashSize,
                    (flashAlpha << 24) | 0xFFFFFF);
        }

        // Node background
        int bgColor;
        if (owned) {
            bgColor = darken(branchColor, 80);
        } else if (available) {
            bgColor = darken(branchColor, 140);
        } else if (tierLocked) {
            bgColor = 0x18181E;
        } else {
            bgColor = 0x202028;
        }
        graphics.fill(nx - size, ny - size, nx + size, ny + size, (alpha << 24) | bgColor);

        // Border
        int borderColor;
        if (owned) {
            borderColor = branchColor;
        } else if (available) {
            // Pulsing border for available
            float pulse = (float) (Math.sin(animTick * 0.15) * 0.3 + 0.7);
            int r = (int) (((branchColor >> 16) & 0xFF) * pulse);
            int g = (int) (((branchColor >> 8) & 0xFF) * pulse);
            int b = (int) ((branchColor & 0xFF) * pulse);
            borderColor = (r << 16) | (g << 8) | b;
        } else {
            borderColor = 0x404050;
        }
        DrawerHelper.drawBorder(graphics, nx - size, ny - size, size * 2, size * 2,
                (alpha << 24) | borderColor, 1);

        // Node content
        var font = Minecraft.getInstance().font;
        if (owned) {
            // Checkmark
            graphics.drawString(font, "\u2713", nx - 3, ny - 4, (alpha << 24) | 0xFFFFFF, false);
        } else if (tierLocked) {
            // Lock indicator
            String lock = "T" + upgrade.getRequiredTier();
            graphics.drawString(font, lock, nx - font.width(lock) / 2, ny - 3,
                    (alpha << 24) | 0x505060, false);
        } else {
            // Cost
            String cost = String.valueOf(upgrade.getCost());
            int costColor = available ? 0xFFCC44 : 0x606070;
            graphics.drawString(font, cost, nx - font.width(cost) / 2, ny - 3,
                    (alpha << 24) | costColor, false);
        }

        // Hover highlight
        if (isHovered) {
            int hoverAlpha = (int) (50 * fadeAlpha);
            graphics.fill(nx - size + 1, ny - size + 1, nx + size - 1, ny + size - 1,
                    (hoverAlpha << 24) | 0xFFFFFF);
        }
    }

    private void drawTooltip(GuiGraphics graphics, int mx, int my) {
        if (hoveredUpgrade == null) return;

        var font = Minecraft.getInstance().font;

        String name = Component.translatable(hoveredUpgrade.getTranslationKey()).getString();
        String desc = Component.translatable(hoveredUpgrade.getDescriptionKey()).getString();
        String cost = "Cost: " + hoveredUpgrade.getCost();
        String tierReq = "Requires Tier " + hoveredUpgrade.getRequiredTier();

        boolean owned = unlockedUpgrades.contains(hoveredUpgrade);
        boolean available = !owned && hoveredUpgrade.canUnlock(unlockedUpgrades, tier);
        boolean canAfford = spendablePoints >= hoveredUpgrade.getCost();

        int tw = Math.max(font.width(name), Math.max(font.width(desc), 100)) + 16;
        int th = 52;
        int tx = mx + 10;
        int ty = my - th - 5;

        // Keep tooltip on screen
        if (tx + tw > getPosition().x + getSize().width - 5) tx = mx - tw - 10;
        if (ty < getPosition().y + 20) ty = my + 15;

        int alpha = (int) (255 * fadeAlpha);
        int branchColor = BRANCH_COLORS[hoveredUpgrade.getBranch().ordinal()];

        // Background
        graphics.fill(tx, ty, tx + tw, ty + th, (alpha << 24) | 0x08080C);

        // Top accent bar
        graphics.fill(tx, ty, tx + tw, ty + 2, (alpha << 24) | branchColor);

        // Border
        DrawerHelper.drawBorder(graphics, tx, ty, tw, th, (alpha << 24) | 0x404060, 1);

        int textY = ty + 6;

        // Name
        graphics.drawString(font, name, tx + 6, textY, (alpha << 24) | 0xFFFFFF, false);
        textY += 11;

        // Description
        graphics.drawString(font, desc, tx + 6, textY, (alpha << 24) | 0x9090A0, false);
        textY += 12;

        // Cost
        int costColor = canAfford ? 0xFFCC44 : 0xFF5544;
        graphics.drawString(font, cost, tx + 6, textY, (alpha << 24) | costColor, false);
        textY += 10;

        // Tier req
        int tierColor = tier >= hoveredUpgrade.getRequiredTier() ? 0x707080 : 0xFF5544;
        graphics.drawString(font, tierReq, tx + 6, textY, (alpha << 24) | tierColor, false);

        // Status indicator
        if (owned) {
            graphics.drawString(font, "OWNED", tx + tw - font.width("OWNED") - 6, ty + 6,
                    (alpha << 24) | 0x44FF44, false);
        } else if (available && canAfford) {
            graphics.drawString(font, "Click!", tx + tw - font.width("Click!") - 6, ty + 6,
                    (alpha << 24) | 0x88FF88, false);
        }
    }

    private void drawRespecConfirm(GuiGraphics graphics, int x, int y, int w, int h) {
        var font = Minecraft.getInstance().font;
        int alpha = (int) (255 * fadeAlpha);

        // Dim background
        graphics.fill(x, y, x + w, y + h, ((int) (180 * fadeAlpha) << 24) | 0x000000);

        // Dialog box
        int bw = 160;
        int bh = 60;
        int bx = x + (w - bw) / 2;
        int by = y + (h - bh) / 2;

        graphics.fill(bx, by, bx + bw, by + bh, (alpha << 24) | 0x101018);
        DrawerHelper.drawBorder(graphics, bx, by, bw, bh, (alpha << 24) | 0xFF4444, 2);

        // Title
        String title = "Reset Upgrades?";
        graphics.drawString(font, title, bx + (bw - font.width(title)) / 2, by + 10,
                (alpha << 24) | 0xFFFFFF, false);

        // Refund amount
        int refund = 0;
        for (StellarIrisUpgrade u : unlockedUpgrades) {
            refund += u.getCost();
        }
        String refundStr = "Refund: " + refund + " pts";
        graphics.drawString(font, refundStr, bx + (bw - font.width(refundStr)) / 2, by + 24,
                (alpha << 24) | 0xFFCC44, false);

        // Buttons hint
        graphics.drawString(font, "[Y] Yes", bx + 20, by + bh - 16, (alpha << 24) | 0x88FF88, false);
        graphics.drawString(font, "[N] No", bx + bw - 50, by + bh - 16, (alpha << 24) | 0xFF8888, false);
    }

    private void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            g.fill(x1, y1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private int darken(int color, int amount) {
        int r = Math.max(0, ((color >> 16) & 0xFF) - amount);
        int g = Math.max(0, ((color >> 8) & 0xFF) - amount);
        int b = Math.max(0, (color & 0xFF) - amount);
        return (r << 16) | (g << 8) | b;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || fadeAlpha < 0.5f) return false;

        if (showRespecConfirm) return true;

        if (button == 0 && hoveredUpgrade != null) {
            boolean owned = unlockedUpgrades.contains(hoveredUpgrade);
            boolean available = !owned && hoveredUpgrade.canUnlock(unlockedUpgrades, tier);
            boolean canAfford = spendablePoints >= hoveredUpgrade.getCost();

            if (available && canAfford) {
                writeClientAction(CLIENT_ACTION_UNLOCK, buf -> buf.writeEnum(hoveredUpgrade));

                // Optimistic update
                unlockedUpgrades.add(hoveredUpgrade);
                spendablePoints -= hoveredUpgrade.getCost();
                lastUnlocked = hoveredUpgrade;
                unlockFlashTick = 15;

                playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;

        if (keyCode == 256) { // ESC
            if (showRespecConfirm) {
                showRespecConfirm = false;
            } else {
                onClose.run();
            }
            return true;
        }

        if (keyCode == 82 && !showRespecConfirm) { // R
            showRespecConfirm = true;
            return true;
        }

        if (keyCode == 89 && showRespecConfirm) { // Y
            writeClientAction(CLIENT_ACTION_RESPEC, buf -> {});
            showRespecConfirm = false;
            playSound(SoundEvents.GLASS_BREAK, 0.8f, 0.8f);

            int refund = 0;
            for (StellarIrisUpgrade u : unlockedUpgrades) {
                refund += u.getCost();
            }
            unlockedUpgrades.clear();
            spendablePoints += refund;
            return true;
        }

        if (keyCode == 78 && showRespecConfirm) { // N
            showRespecConfirm = false;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine == null) return;

        if (id == CLIENT_ACTION_UNLOCK) {
            StellarIrisUpgrade upgrade = buffer.readEnum(StellarIrisUpgrade.class);
            machine.tryUnlockUpgrade(upgrade);
        }
        // Respec is no longer supported - upgrades are permanent
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine == null) return;

        Set<StellarIrisUpgrade> currentUpgrades = machine.getUnlockedUpgrades();
        int currentPoints = machine.getSpendablePoints();
        int currentTier = machine.getPrestigeTier();
        int currentAscension = machine.getAscensionLevel();

        if (!currentUpgrades.equals(unlockedUpgrades) ||
                currentPoints != spendablePoints ||
                currentTier != tier ||
                currentAscension != ascensionLevel) {

            writeUpdateInfo(UPDATE_ID_SYNC, buf -> {
                buf.writeInt(currentPoints);
                buf.writeInt(currentTier);
                buf.writeInt(currentAscension);
                buf.writeInt(currentUpgrades.size());
                for (StellarIrisUpgrade u : currentUpgrades) {
                    buf.writeEnum(u);
                }
            });

            if (currentUpgrades.isEmpty()) {
                unlockedUpgrades = EnumSet.noneOf(StellarIrisUpgrade.class);
            } else {
                unlockedUpgrades = EnumSet.copyOf(currentUpgrades);
            }
            spendablePoints = currentPoints;
            tier = currentTier;
            ascensionLevel = currentAscension;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == UPDATE_ID_SYNC) {
            spendablePoints = buffer.readInt();
            tier = buffer.readInt();
            ascensionLevel = buffer.readInt();

            int count = buffer.readInt();
            unlockedUpgrades = EnumSet.noneOf(StellarIrisUpgrade.class);
            for (int i = 0; i < count; i++) {
                unlockedUpgrades.add(buffer.readEnum(StellarIrisUpgrade.class));
            }
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    private void playSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }
}
