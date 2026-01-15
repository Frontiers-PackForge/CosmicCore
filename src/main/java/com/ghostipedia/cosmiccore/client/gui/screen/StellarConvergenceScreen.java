package com.ghostipedia.cosmiccore.client.gui.screen;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarIrisUpgrade;
import com.ghostipedia.cosmiccore.client.renderer.BackgroundRenderer;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.StellarUpgradePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

/**
 * Full-screen cosmic upgrade tree for the Stellar IRIS.
 * Inspired by constellation/skill tree designs with organic node layouts.
 */
@OnlyIn(Dist.CLIENT)
public class StellarConvergenceScreen extends Screen {

    // Machine reference
    private final BlockPos machinePos;
    private int spendablePoints;
    private int lifetimePoints;
    private int tier;
    private int ascensionLevel;
    private Set<StellarIrisUpgrade> unlockedUpgrades;
    private Map<Integer, Integer> repeatableLevels;

    // Node system
    private final List<UpgradeNode> nodes = new ArrayList<>();
    private final Map<StellarIrisUpgrade, UpgradeNode> nodeMap = new HashMap<>();

    // Visual state
    private float fadeAlpha = 0f;
    private int ticks = 0;

    // Interaction
    private UpgradeNode hoveredNode = null;
    private UpgradeNode selectedNode = null;
    private float panelSlide = 0f;

    // Pan and zoom
    private float viewOffsetX = 0f;
    private float viewOffsetY = 0f;
    private float zoom = 1.0f;
    private boolean isDragging = false;
    private double lastDragX, lastDragY;

    // Particles
    private final List<CosmicParticle> particles = new ArrayList<>();
    private final Random random = new Random();

    // Constants
    private static final float MIN_ZOOM = 0.3f;
    private static final float MAX_ZOOM = 1.8f;
    private static final int PARTICLE_COUNT = 60;

    public StellarConvergenceScreen(BlockPos machinePos, int spendablePoints, int lifetimePoints,
                                    int tier, int ascensionLevel, Set<StellarIrisUpgrade> unlockedUpgrades,
                                    int[] repeatableLevels) {
        super(Component.literal("Stellar Convergence"));
        this.machinePos = machinePos;
        this.spendablePoints = spendablePoints;
        this.lifetimePoints = lifetimePoints;
        this.tier = tier;
        this.ascensionLevel = ascensionLevel;
        this.unlockedUpgrades = unlockedUpgrades != null ?
                EnumSet.copyOf(unlockedUpgrades) : EnumSet.noneOf(StellarIrisUpgrade.class);
        // Convert int[] to Map for internal use
        this.repeatableLevels = new HashMap<>();
        if (repeatableLevels != null) {
            for (int i = 0; i < repeatableLevels.length; i++) {
                if (repeatableLevels[i] > 0) {
                    this.repeatableLevels.put(i, repeatableLevels[i]);
                }
            }
        }
    }

    public static void open(IrisMultiblockMachine machine) {
        if (machine == null) return;

        Set<StellarIrisUpgrade> upgrades = machine.getUnlockedUpgrades();
        StellarConvergenceScreen screen = new StellarConvergenceScreen(
                machine.getPos(),
                machine.getSpendablePoints(),
                machine.getLifetimePrestigePoints(),
                machine.getPrestigeTier(),
                machine.getAscensionLevel(),
                upgrades.isEmpty() ? null : upgrades,
                machine.getRepeatableUpgradeLevels());
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    protected void init() {
        super.init();
        buildNodeLayout();
        initParticles();
    }

    private void buildNodeLayout() {
        nodes.clear();
        nodeMap.clear();

        int cx = width / 2;
        int cy = height / 2;

        // Create organic tree layout
        // Central core with 4 main branches spiraling outward

        for (StellarIrisUpgrade upgrade : StellarIrisUpgrade.values()) {
            float[] pos = calculateNodePosition(upgrade, cx, cy);
            UpgradeNode node = new UpgradeNode(upgrade, pos[0], pos[1]);
            nodes.add(node);
            nodeMap.put(upgrade, node);
        }
    }

    private float[] calculateNodePosition(StellarIrisUpgrade upgrade, int cx, int cy) {
        StellarIrisUpgrade.Branch branch = upgrade.getBranch();
        int row = upgrade.getRow();

        // Repeatable upgrades go in a ring around the center
        if (branch == StellarIrisUpgrade.Branch.REPEATABLE) {
            List<StellarIrisUpgrade> repeatables = new ArrayList<>(StellarIrisUpgrade.getRepeatables());
            repeatables.sort(Comparator.comparingInt(Enum::ordinal));
            int idx = repeatables.indexOf(upgrade);
            int count = repeatables.size();

            // Inner ring around center
            float radius = 55;
            float angleStep = (float) (2 * Math.PI / count);
            float angle = idx * angleStep - (float) Math.PI / 2; // Start from top

            float x = cx + Mth.cos(angle) * radius;
            float y = cy + Mth.sin(angle) * radius;
            return new float[] { x, y };
        }

        // Each branch gets a quadrant, nodes spiral outward
        float baseAngle = switch (branch) {
            case IGNITION -> (float) (-Math.PI * 0.75); // Top-left
            case FUSION -> (float) (-Math.PI * 0.25);   // Top-right
            case COLLAPSE -> (float) (Math.PI * 0.75);  // Bottom-left
            case VOID -> (float) (Math.PI * 0.25);      // Bottom-right
            case REPEATABLE -> 0; // Handled above
        };

        // Get upgrades in same branch/row for spreading
        List<StellarIrisUpgrade> sameRowBranch = new ArrayList<>();
        for (StellarIrisUpgrade u : StellarIrisUpgrade.values()) {
            if (u.getBranch() == branch && u.getRow() == row) {
                sameRowBranch.add(u);
            }
        }
        sameRowBranch.sort(Comparator.comparingInt(Enum::ordinal));

        int idx = sameRowBranch.indexOf(upgrade);
        int count = sameRowBranch.size();

        // Radial distance increases with row - much more spread out
        float radius = 95 + row * 50; // Pushed out more to make room for repeatables

        // Spread nodes in same row - wider spread
        float spreadAngle = 0.38f; // How much to spread within a row
        float nodeAngle = baseAngle;
        if (count > 1) {
            nodeAngle += (idx - (count - 1) / 2.0f) * spreadAngle;
        }

        // Add slight spiral effect
        nodeAngle += row * 0.05f;

        float x = cx + Mth.cos(nodeAngle) * radius;
        float y = cy + Mth.sin(nodeAngle) * radius;

        return new float[] { x, y };
    }

    private void initParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new CosmicParticle(width, height, random));
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticks++;

        if (fadeAlpha < 1f) {
            fadeAlpha = Math.min(1f, fadeAlpha + 0.08f);
        }

        // Update particles
        for (CosmicParticle p : particles) {
            p.tick();
            if (p.isDead()) {
                p.reset(width, height, random);
            }
        }

        // Update nodes
        for (UpgradeNode node : nodes) {
            node.tick();
        }

        // Panel animation
        if (selectedNode != null) {
            panelSlide = Math.min(1f, panelSlide + 0.12f);
        } else {
            panelSlide = Math.max(0f, panelSlide - 0.15f);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Cosmic background
        BackgroundRenderer.render(graphics.pose(), BackgroundRenderer.BackgroundType.GALAXY, fadeAlpha, width, height);

        if (fadeAlpha < 0.1f) return;

        // Particles
        for (CosmicParticle p : particles) {
            p.render(graphics, fadeAlpha);
        }

        // Vignette
        renderVignette(graphics);

        // Update node positions with view offset
        int cx = width / 2;
        int cy = height / 2;
        hoveredNode = null;

        for (UpgradeNode node : nodes) {
            float[] basePos = calculateNodePosition(node.upgrade, cx, cy);
            node.screenX = (basePos[0] - cx) * zoom + cx + viewOffsetX;
            node.screenY = (basePos[1] - cy) * zoom + cy + viewOffsetY;

            if (node.isMouseOver(mouseX, mouseY, zoom)) {
                hoveredNode = node;
            }
        }

        // Draw connections
        renderConnections(graphics);

        // Draw central core
        renderCentralCore(graphics, cx, cy);

        // Draw nodes
        for (UpgradeNode node : nodes) {
            StellarIrisUpgrade upgrade = node.upgrade;
            boolean owned;
            boolean available;
            boolean tierLocked = tier < upgrade.getRequiredTier();

            if (upgrade.isRepeatable()) {
                int level = repeatableLevels.getOrDefault(upgrade.ordinal(), 0);
                owned = level > 0;
                available = level < upgrade.getMaxLevel() &&
                        spendablePoints >= upgrade.getCostForLevel(level + 1);
            } else {
                owned = unlockedUpgrades.contains(upgrade);
                available = !owned && upgrade.canUnlock(unlockedUpgrades, tier) &&
                        spendablePoints >= upgrade.getCost();
            }

            node.render(graphics, font, fadeAlpha, zoom, ticks, partialTick,
                    owned, available, tierLocked,
                    node == hoveredNode, node == selectedNode);
        }

        // Header
        renderHeader(graphics);

        // Detail panel
        if (panelSlide > 0.01f) {
            renderDetailPanel(graphics, mouseX, mouseY);
        }

        // Tooltip for non-selected hover
        if (hoveredNode != null && hoveredNode != selectedNode) {
            renderTooltip(graphics, mouseX, mouseY);
        }

        // Controls hint
        renderControlsHint(graphics);
    }

    private void renderVignette(GuiGraphics graphics) {
        int strength = (int) (fadeAlpha * 120);
        for (int i = 0; i < 40; i += 2) {
            int alpha = (int) (strength * (1f - (float) i / 40f));
            int color = (alpha << 24);
            graphics.fill(0, i, width, i + 2, color);
            graphics.fill(0, height - i - 2, width, height - i, color);
        }
    }

    private void renderConnections(GuiGraphics graphics) {
        for (UpgradeNode node : nodes) {
            for (StellarIrisUpgrade prereq : node.upgrade.getPrerequisites()) {
                UpgradeNode prereqNode = nodeMap.get(prereq);
                if (prereqNode == null) continue;

                boolean bothOwned = unlockedUpgrades.contains(node.upgrade) &&
                        unlockedUpgrades.contains(prereq);
                boolean prereqOwned = unlockedUpgrades.contains(prereq);

                int[] color = node.getColor();
                int alpha;
                if (bothOwned) {
                    alpha = (int) (fadeAlpha * 180);
                } else if (prereqOwned) {
                    alpha = (int) (fadeAlpha * 80);
                } else {
                    alpha = (int) (fadeAlpha * 30);
                }

                int lineColor = (alpha << 24) | (color[0] << 16) | (color[1] << 8) | color[2];
                drawConnection(graphics, prereqNode, node, lineColor, bothOwned);
            }
        }
    }

    private void drawConnection(GuiGraphics graphics, UpgradeNode from, UpgradeNode to, int color, boolean animated) {
        int x1 = (int) from.screenX;
        int y1 = (int) from.screenY;
        int x2 = (int) to.screenX;
        int y2 = (int) to.screenY;

        // Draw dotted line
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps == 0) return;

        int dotSpacing = 6;
        for (int i = 0; i <= steps; i += dotSpacing) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            graphics.fill(x, y, x + 2, y + 2, color);
        }

        // Animated energy pulse on owned connections
        if (animated) {
            float t = (ticks % 40) / 40f;
            int px = (int) Mth.lerp(t, x1, x2);
            int py = (int) Mth.lerp(t, y1, y2);
            int pulseAlpha = (int) (fadeAlpha * 220);
            graphics.fill(px - 2, py - 2, px + 3, py + 3, (pulseAlpha << 24) | 0xFFFFFF);
        }
    }

    private void renderCentralCore(GuiGraphics graphics, int cx, int cy) {
        int coreX = (int) (cx + viewOffsetX);
        int coreY = (int) (cy + viewOffsetY);
        int baseRadius = (int) (45 * zoom);

        // Pulsing
        float pulse = (float) (Math.sin(ticks * 0.06) * 0.1 + 1.0);
        int radius = (int) (baseRadius * pulse);

        // Outer glow
        for (int r = radius + 25; r > radius; r -= 3) {
            float p = (float) (r - radius) / 25f;
            int alpha = (int) ((1f - p) * 40 * fadeAlpha);
            int glowColor = (alpha << 24) | 0xFFCC44;
            drawCircle(graphics, coreX, coreY, r, glowColor);
        }

        // Core
        int coreAlpha = (int) (fadeAlpha * 255);
        for (int r = radius; r > 0; r -= 2) {
            float p = (float) r / radius;
            int alpha = (int) (coreAlpha * (0.5f + 0.5f * p));
            int cr = (int) (255 * p + 200 * (1 - p));
            int cg = (int) (200 * p + 150 * (1 - p));
            int cb = (int) (100 * p + 50 * (1 - p));
            drawCircle(graphics, coreX, coreY, r, (alpha << 24) | (cr << 16) | (cg << 8) | cb);
        }

        // Ascension rings
        if (ascensionLevel > 0) {
            int ringAlpha = (int) (fadeAlpha * 150);
            for (int i = 0; i < ascensionLevel; i++) {
                int ringR = radius + 30 + i * 8;
                float rotation = ticks * 0.02f + i * 0.5f;
                for (int a = 0; a < 8; a++) {
                    float angle = rotation + a * Mth.PI / 4;
                    int rx = coreX + (int) (Mth.cos(angle) * ringR);
                    int ry = coreY + (int) (Mth.sin(angle) * ringR);
                    graphics.fill(rx - 1, ry - 1, rx + 2, ry + 2, (ringAlpha << 24) | 0xFFDD66);
                }
            }
        }
    }

    private void drawCircle(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        if (radius <= 0) return;
        for (int y = -radius; y <= radius; y += 2) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
            graphics.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 2, color);
        }
    }

    private void renderHeader(GuiGraphics graphics) {
        int alpha = (int) (fadeAlpha * 255);

        // Points display
        String pointsStr = "\u2726 " + spendablePoints;
        int pointsColor = spendablePoints > 0 ? 0xFFCC44 : 0x808080;
        graphics.drawString(font, pointsStr, 20, 20, (alpha << 24) | pointsColor, false);

        // Tier display
        String tierStr = "Tier " + tier;
        if (ascensionLevel > 0) {
            tierStr = "\u2605".repeat(ascensionLevel) + " " + tierStr;
        }
        graphics.drawString(font, tierStr, 20, 34, (alpha << 24) | 0xAAAAAA, false);

        // Title
        String title = "STELLAR CONVERGENCE";
        int titleW = font.width(title);
        graphics.drawString(font, title, (width - titleW) / 2, 15, (alpha << 24) | 0xE0E0F0, false);
    }

    private void renderDetailPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        if (selectedNode == null) return;

        StellarIrisUpgrade upgrade = selectedNode.upgrade;
        int panelW = 220;
        int panelH = 180;
        int panelX = width - (int) (panelSlide * (panelW + 15));
        int panelY = (height - panelH) / 2;

        int bgAlpha = (int) (fadeAlpha * panelSlide * 230);
        int borderAlpha = (int) (fadeAlpha * panelSlide * 255);

        // Background
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, (bgAlpha << 24) | 0x0C0C14);

        // Border
        int[] color = selectedNode.getColor();
        int borderColor = (borderAlpha << 24) | (color[0] << 16) | (color[1] << 8) | color[2];
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 2, borderColor);
        graphics.fill(panelX, panelY, panelX + 2, panelY + panelH, borderColor);
        graphics.fill(panelX + panelW - 2, panelY, panelX + panelW, panelY + panelH, borderColor);
        graphics.fill(panelX, panelY + panelH - 2, panelX + panelW, panelY + panelH, borderColor);

        int textAlpha = (int) (fadeAlpha * panelSlide * 255);
        int textPadding = 10;
        int maxTextWidth = panelW - textPadding * 2;

        // Name
        String name = Component.translatable(upgrade.getTranslationKey()).getString();
        graphics.drawString(font, name, panelX + textPadding, panelY + 10,
                (textAlpha << 24) | (color[0] << 16) | (color[1] << 8) | color[2], false);

        // Description with word wrapping
        String desc = Component.translatable(upgrade.getDescriptionKey()).getString();
        int descY = panelY + 28;
        List<String> descLines = wrapText(desc, maxTextWidth);
        for (String line : descLines) {
            graphics.drawString(font, line, panelX + textPadding, descY, (textAlpha << 24) | 0x9090A0, false);
            descY += 10;
        }

        // Dynamic Y position after description
        int infoY = descY + 6;

        // Handle repeatable vs non-repeatable differently
        if (upgrade.isRepeatable()) {
            int currentLevel = repeatableLevels.getOrDefault(upgrade.ordinal(), 0);
            int maxLevel = upgrade.getMaxLevel();
            boolean isMaxed = currentLevel >= maxLevel;

            // Level display
            String levelStr = isMaxed ?
                    "Level " + currentLevel + " / " + maxLevel + " (MAX)" :
                    "Level " + currentLevel + " / " + maxLevel;
            int levelColor = isMaxed ? 0x44FF44 : 0xAAAAAA;
            graphics.drawString(font, levelStr, panelX + textPadding, infoY, (textAlpha << 24) | levelColor, false);
            infoY += 14;

            // Cost for next level
            if (!isMaxed) {
                int nextCost = upgrade.getCostForLevel(currentLevel + 1);
                String costStr = "Next Level: " + nextCost + " pts";
                boolean canAfford = spendablePoints >= nextCost;
                int costColor = canAfford ? 0xFFCC44 : 0xFF5544;
                graphics.drawString(font, costStr, panelX + textPadding, infoY, (textAlpha << 24) | costColor, false);
                infoY += 14;

                // Status
                String status = canAfford ? "[Click to upgrade]" : "Not enough points";
                int statusColor = canAfford ? 0x88FF88 : 0xFF8844;
                graphics.drawString(font, status, panelX + textPadding, panelY + panelH - 24,
                        (textAlpha << 24) | statusColor, false);
            } else {
                graphics.drawString(font, "MAX LEVEL", panelX + textPadding, panelY + panelH - 24,
                        (textAlpha << 24) | 0x44FF44, false);
            }
        } else {
            // Non-repeatable upgrade
            // Cost
            String cost = "Cost: " + upgrade.getCost() + " pts";
            boolean canAfford = spendablePoints >= upgrade.getCost();
            int costColor = canAfford ? 0xFFCC44 : 0xFF5544;
            graphics.drawString(font, cost, panelX + textPadding, infoY, (textAlpha << 24) | costColor, false);
            infoY += 14;

            // Tier requirement
            String tierReq = "Requires Tier " + upgrade.getRequiredTier();
            boolean tierMet = tier >= upgrade.getRequiredTier();
            int tierColor = tierMet ? 0x707080 : 0xFF5544;
            graphics.drawString(font, tierReq, panelX + textPadding, infoY, (textAlpha << 24) | tierColor, false);

            // Status
            boolean owned = unlockedUpgrades.contains(upgrade);
            boolean available = !owned && upgrade.canUnlock(unlockedUpgrades, tier);

            String status;
            int statusColor;
            if (owned) {
                status = "UNLOCKED";
                statusColor = 0x44FF44;
            } else if (available && canAfford && tierMet) {
                status = "[Click to unlock]";
                statusColor = 0x88FF88;
            } else if (!tierMet) {
                status = "Tier locked";
                statusColor = 0xFF5544;
            } else if (!canAfford) {
                status = "Not enough points";
                statusColor = 0xFF8844;
            } else {
                status = "Prerequisites needed";
                statusColor = 0x888888;
            }
            graphics.drawString(font, status, panelX + textPadding, panelY + panelH - 24,
                    (textAlpha << 24) | statusColor, false);
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (font.width(testLine) <= maxWidth) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word is too long, just add it
                    lines.add(word);
                }
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredNode == null) return;

        String name = Component.translatable(hoveredNode.upgrade.getTranslationKey()).getString();
        int tw = font.width(name) + 12;
        int th = 18;
        int tx = mouseX + 12;
        int ty = mouseY - th - 5;

        if (tx + tw > width - 10) tx = width - tw - 10;
        if (ty < 10) ty = mouseY + 15;

        int alpha = (int) (fadeAlpha * 230);
        graphics.fill(tx - 2, ty - 2, tx + tw + 2, ty + th + 2, (alpha << 24) | 0x0C0C14);

        int[] color = hoveredNode.getColor();
        int textColor = (255 << 24) | (color[0] << 16) | (color[1] << 8) | color[2];
        graphics.drawString(font, name, tx + 4, ty + 4, textColor, false);
    }

    private void renderControlsHint(GuiGraphics graphics) {
        int alpha = (int) (fadeAlpha * 100);
        int color = (alpha << 24) | 0x606070;

        graphics.drawString(font, "[ESC] Close", 20, height - 25, color, false);
        graphics.drawString(font, "Scroll: Zoom | Drag: Pan", width - font.width("Scroll: Zoom | Drag: Pan") - 20,
                height - 25, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Left click
            if (hoveredNode != null) {
                if (selectedNode == hoveredNode) {
                    // Double-click to unlock
                    tryUnlock(hoveredNode.upgrade);
                } else {
                    selectedNode = hoveredNode;
                }
                return true;
            }
            selectedNode = null;
            return true;
        }

        if (button == 1) {
            // Right-click drag
            isDragging = true;
            lastDragX = mouseX;
            lastDragY = mouseY;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            viewOffsetX += (float) (mouseX - lastDragX);
            viewOffsetY += (float) (mouseY - lastDragY);
            lastDragX = mouseX;
            lastDragY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float oldZoom = zoom;
        zoom = Mth.clamp(zoom + (float) delta * 0.15f, MIN_ZOOM, MAX_ZOOM);

        // Zoom toward mouse
        if (oldZoom != zoom) {
            float ratio = zoom / oldZoom;
            float mx = (float) mouseX - width / 2f;
            float my = (float) mouseY - height / 2f;
            viewOffsetX = (viewOffsetX - mx) * ratio + mx;
            viewOffsetY = (viewOffsetY - my) * ratio + my;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            if (selectedNode != null) {
                selectedNode = null;
                return true;
            }
            onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void tryUnlock(StellarIrisUpgrade upgrade) {
        if (upgrade.isRepeatable()) {
            tryLevelUpRepeatable(upgrade);
            return;
        }

        if (unlockedUpgrades.contains(upgrade)) return;
        if (!upgrade.canUnlock(unlockedUpgrades, tier)) return;
        if (spendablePoints < upgrade.getCost()) return;

        // Send packet to server
        CCoreNetwork.sendToServer(new StellarUpgradePacket(machinePos, upgrade, false));

        // Optimistic update
        unlockedUpgrades.add(upgrade);
        spendablePoints -= upgrade.getCost();
    }

    private void tryLevelUpRepeatable(StellarIrisUpgrade upgrade) {
        if (!upgrade.isRepeatable()) return;

        int currentLevel = repeatableLevels.getOrDefault(upgrade.ordinal(), 0);
        if (currentLevel >= upgrade.getMaxLevel()) return;

        int nextLevel = currentLevel + 1;
        int cost = upgrade.getCostForLevel(nextLevel);

        if (spendablePoints < cost) return;

        // Send packet to server
        CCoreNetwork.sendToServer(new StellarUpgradePacket(machinePos, upgrade, false));

        // Optimistic update
        repeatableLevels.put(upgrade.ordinal(), nextLevel);
        spendablePoints -= cost;
    }

    private int getUpgradeLevel(StellarIrisUpgrade upgrade) {
        if (upgrade.isRepeatable()) {
            return repeatableLevels.getOrDefault(upgrade.ordinal(), 0);
        }
        return unlockedUpgrades.contains(upgrade) ? 1 : 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Node class
    private static class UpgradeNode {

        final StellarIrisUpgrade upgrade;
        float screenX, screenY;
        float pulsePhase;

        UpgradeNode(StellarIrisUpgrade upgrade, float x, float y) {
            this.upgrade = upgrade;
            this.screenX = x;
            this.screenY = y;
            this.pulsePhase = (float) (Math.random() * Math.PI * 2);
        }

        void tick() {
            pulsePhase += 0.08f;
        }

        boolean isMouseOver(int mx, int my, float zoom) {
            float r = getRadius() * zoom;
            float dx = mx - screenX;
            float dy = my - screenY;
            return dx * dx + dy * dy <= r * r * 1.5f;
        }

        float getRadius() {
            if (upgrade.isRepeatable()) return 12; // Smaller for repeatables
            if (upgrade.isCapstone()) return 18;
            if (upgrade.getRow() == 5) return 15; // T5 upgrades
            return 14;
        }

        int[] getColor() {
            return switch (upgrade.getBranch()) {
                case IGNITION -> new int[] { 255, 120, 60 };   // Fiery orange
                case FUSION -> new int[] { 80, 180, 255 };     // Electric blue
                case COLLAPSE -> new int[] { 180, 80, 220 };   // Void purple
                case VOID -> new int[] { 80, 255, 180 };       // Ethereal teal
                case REPEATABLE -> new int[] { 220, 200, 120 }; // Golden for repeatables
            };
        }

        void render(GuiGraphics graphics, net.minecraft.client.gui.Font font, float fadeAlpha, float zoom,
                    int ticks, float partialTick, boolean owned, boolean available, boolean tierLocked,
                    boolean hovered, boolean selected) {
            int[] rgb = getColor();
            float baseRadius = getRadius() * zoom;

            // Pulse for owned/available/hovered
            float pulse = 1f;
            if (owned || available || hovered || selected) {
                float smooth = pulsePhase + 0.08f * partialTick;
                pulse = 1f + Mth.sin(smooth) * 0.12f;
            }
            int radius = (int) (baseRadius * pulse);

            // Alpha based on state
            float stateAlpha;
            if (owned) stateAlpha = 1f;
            else if (available) stateAlpha = hovered ? 0.9f : 0.6f;
            else if (tierLocked) stateAlpha = 0.25f;
            else stateAlpha = 0.4f;

            int alpha = (int) (fadeAlpha * stateAlpha * 255);

            // Glow for owned/available
            if (owned || available || selected) {
                for (int r = radius + 14; r > radius; r -= 3) {
                    float p = (float) (r - radius) / 14f;
                    int glowAlpha = (int) ((1f - p) * 60 * fadeAlpha * stateAlpha);
                    int glowColor = (glowAlpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                    drawNodeCircle(graphics, (int) screenX, (int) screenY, r, glowColor);
                }
            }

            // Core - darker if locked
            int coreR = owned ? rgb[0] : (int) (rgb[0] * 0.4f);
            int coreG = owned ? rgb[1] : (int) (rgb[1] * 0.4f);
            int coreB = owned ? rgb[2] : (int) (rgb[2] * 0.4f);
            if (tierLocked) {
                coreR = coreG = coreB = 40;
            }
            int coreColor = (alpha << 24) | (coreR << 16) | (coreG << 8) | coreB;
            drawNodeCircle(graphics, (int) screenX, (int) screenY, radius, coreColor);

            // Border
            int borderAlpha = (int) (fadeAlpha * (hovered || selected ? 255 : 150));
            int borderColor;
            if (owned) {
                borderColor = (borderAlpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            } else if (available) {
                float bpulse = Mth.sin(pulsePhase) * 0.3f + 0.7f;
                int br = (int) (rgb[0] * bpulse);
                int bg = (int) (rgb[1] * bpulse);
                int bb = (int) (rgb[2] * bpulse);
                borderColor = (borderAlpha << 24) | (br << 16) | (bg << 8) | bb;
            } else {
                borderColor = (borderAlpha << 24) | 0x404050;
            }
            drawNodeRing(graphics, (int) screenX, (int) screenY, radius, borderColor);

            // Selection ring
            if (selected) {
                int ringAlpha = (int) (fadeAlpha * 200);
                float rot = ticks * 0.05f + partialTick * 0.05f;
                for (int i = 0; i < 8; i++) {
                    float angle = rot + i * Mth.PI / 4;
                    int rx = (int) (screenX + Mth.cos(angle) * (radius + 6));
                    int ry = (int) (screenY + Mth.sin(angle) * (radius + 6));
                    graphics.fill(rx - 1, ry - 1, rx + 2, ry + 2, (ringAlpha << 24) | 0xFFFFFF);
                }
            }

            // Icon/text inside
            if (owned) {
                graphics.drawString(font, "\u2713", (int) screenX - 3, (int) screenY - 4,
                        (alpha << 24) | 0xFFFFFF, false);
            } else if (tierLocked) {
                String lock = "T" + upgrade.getRequiredTier();
                graphics.drawString(font, lock, (int) screenX - font.width(lock) / 2, (int) screenY - 3,
                        (alpha << 24) | 0x505050, false);
            } else {
                String cost = String.valueOf(upgrade.getCost());
                int costColor = available ? 0xFFCC44 : 0x606060;
                graphics.drawString(font, cost, (int) screenX - font.width(cost) / 2, (int) screenY - 3,
                        (alpha << 24) | costColor, false);
            }
        }

        private void drawNodeCircle(GuiGraphics g, int cx, int cy, int radius, int color) {
            if (radius <= 0) return;
            for (int y = -radius; y <= radius; y++) {
                int halfWidth = (int) Math.sqrt(radius * radius - y * y);
                g.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 1, color);
            }
        }

        private void drawNodeRing(GuiGraphics g, int cx, int cy, int radius, int color) {
            for (int a = 0; a < 360; a += 8) {
                float rad = a * Mth.DEG_TO_RAD;
                int px = cx + (int) (Mth.cos(rad) * radius);
                int py = cy + (int) (Mth.sin(rad) * radius);
                g.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    // Particle class
    private static class CosmicParticle {

        float x, y;
        float twinkle;
        float maxAlpha;
        int life, age;

        CosmicParticle(int w, int h, Random r) {
            reset(w, h, r);
        }

        void reset(int w, int h, Random r) {
            x = r.nextFloat() * w;
            y = r.nextFloat() * h;
            twinkle = r.nextFloat() * Mth.TWO_PI;
            maxAlpha = 0.15f + r.nextFloat() * 0.3f;
            life = 150 + r.nextInt(200);
            age = 0;
        }

        void tick() {
            age++;
            twinkle += 0.06f;
        }

        boolean isDead() {
            return age >= life;
        }

        void render(GuiGraphics g, float screenAlpha) {
            float t = Mth.sin(twinkle) * 0.3f + 0.7f;
            float lifeFade = 1f;
            float p = (float) age / life;
            if (p < 0.1f) lifeFade = p / 0.1f;
            else if (p > 0.9f) lifeFade = (1f - p) / 0.1f;

            int alpha = (int) (maxAlpha * t * lifeFade * screenAlpha * 255);
            if (alpha <= 0) return;

            g.fill((int) x, (int) y, (int) x + 1, (int) y + 1, (alpha << 24) | 0xCCCCDD);
        }
    }
}
