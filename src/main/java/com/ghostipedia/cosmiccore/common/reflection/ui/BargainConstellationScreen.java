package com.ghostipedia.cosmiccore.common.reflection.ui;

import com.ghostipedia.cosmiccore.client.renderer.BackgroundRenderer;
import com.ghostipedia.cosmiccore.client.renderer.SoulAuraRenderer;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A constellation-style visual browser for bargains.
 *
 * Bargains appear as glowing nodes orbiting the player's soul.
 * - Active bargains: Bright, pulsing with their signature color
 * - Available bargains: Dim, waiting to be claimed
 * - Scarred bargains: Cracked, dark, forever marked
 *
 * Click a node to see its details in a Thaumonomicon-style panel.
 * From there, accept or (if active) defy the bargain.
 */
@OnlyIn(Dist.CLIENT)
public class BargainConstellationScreen extends Screen {

    // Player state
    private final int erosion;
    private final Set<ResourceLocation> activeBargains;
    private final Set<ResourceLocation> defianceScars;

    // Economy state
    private int shardBalance = 0;
    private int usedCapacity = 0;
    private int totalCapacity = 100;

    // All bargains organized into nodes
    private final List<BargainNode> nodes = new ArrayList<>();

    // Visual state
    private float fadeAlpha = 0f;
    private int totalTicks = 0;
    private float soulPulse = 0f;
    private float soulBreath = 0f;

    // Interaction state
    @Nullable
    private BargainNode hoveredNode = null;
    @Nullable
    private BargainNode selectedNode = null;
    private float selectionAnimation = 0f;

    // Detail panel animation - track previous for interpolation
    private float panelSlide = 0f; // 0 = hidden, 1 = fully visible
    private float panelSlidePrev = 0f;
    private float selectionAnimationPrev = 0f;

    // Action button bounds for click detection
    private int actionButtonX, actionButtonY, actionButtonWidth, actionButtonHeight;
    private boolean actionButtonVisible = false;

    // Particles for atmosphere
    private final List<StarParticle> stars = new ArrayList<>();
    private final Random random = new Random();

    // Pan and zoom state
    private float viewOffsetX = 0f;
    private float viewOffsetY = 0f;
    private float zoom = 1.0f;
    private boolean isDragging = false;
    private double lastDragX, lastDragY;

    // Constants
    private static final int FADE_TICKS = 30;
    private static final int STAR_COUNT = 50; // Restored for better atmosphere
    private static final float ORBIT_BASE_RADIUS = 120f;
    private static final float ORBIT_RING_SPACING = 45f;
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 2.0f;

    // If true, skip fade-in (already coming from dark screen)
    private boolean skipFadeIn = false;

    public BargainConstellationScreen(int erosion, Set<ResourceLocation> activeBargains,
                                      Set<ResourceLocation> defianceScars) {
        super(ReflectionLang.ui("constellation_title"));
        this.erosion = erosion;
        this.activeBargains = activeBargains;
        this.defianceScars = defianceScars;
    }

    public static void open(int erosion, Set<ResourceLocation> activeBargains, Set<ResourceLocation> defianceScars,
                            int shardBalance, int usedCapacity, int totalCapacity) {
        openFromVoid(erosion, activeBargains, defianceScars, shardBalance, usedCapacity, totalCapacity);
    }

    public static void openFromVoid(int erosion, Set<ResourceLocation> activeBargains,
                                    Set<ResourceLocation> defianceScars,
                                    int shardBalance, int usedCapacity, int totalCapacity) {
        BargainConstellationScreen screen = new BargainConstellationScreen(erosion, activeBargains, defianceScars);
        screen.skipFadeIn = true;
        screen.fadeAlpha = 1f;
        screen.shardBalance = shardBalance;
        screen.usedCapacity = usedCapacity;
        screen.totalCapacity = totalCapacity;
        Minecraft.getInstance().setScreen(screen);
    }

    public void setEconomyData(int shardBalance, int usedCapacity, int totalCapacity) {
        this.shardBalance = shardBalance;
        this.usedCapacity = usedCapacity;
        this.totalCapacity = totalCapacity;
    }

    @Override
    protected void init() {
        super.init();

        // Initialize star particles
        stars.clear();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new StarParticle(width, height, random));
        }

        // Build bargain nodes
        buildConstellationNodes();
    }

    private void buildConstellationNodes() {
        nodes.clear();

        List<Bargain> allBargains = new ArrayList<>(BargainRegistry.getAll());

        // Organize by tier into orbital rings
        // Ring 0 (closest): EARLY, ANY
        // Ring 1: EARLY_MID
        // Ring 2: MID
        // Ring 3: LATE
        // Ring 4 (farthest): EXTREME

        int centerX = width / 2;
        int centerY = height / 2;

        // Count bargains per ring for spacing
        int[] ringCounts = new int[5];
        int[] ringIndices = new int[5];

        for (Bargain bargain : allBargains) {
            int ring = getTierRing(bargain.getTier());
            ringCounts[ring]++;
        }

        // Place each bargain
        for (Bargain bargain : allBargains) {
            int ring = getTierRing(bargain.getTier());
            float radius = ORBIT_BASE_RADIUS + (ring * ORBIT_RING_SPACING);

            // Distribute evenly around the ring
            int count = ringCounts[ring];
            int index = ringIndices[ring]++;

            // Offset each ring slightly so they don't all align
            float ringOffset = ring * 0.3f;
            float angle = (float) (2 * Math.PI * index / count) + ringOffset;

            // Determine node state
            BargainNode.NodeState state;
            if (activeBargains.contains(bargain.getId())) {
                state = BargainNode.NodeState.ACTIVE;
            } else if (defianceScars.contains(bargain.getId())) {
                state = BargainNode.NodeState.SCARRED;
            } else {
                state = BargainNode.NodeState.AVAILABLE;
            }

            nodes.add(new BargainNode(bargain, centerX, centerY, radius, angle, state));
        }
    }

    private int getTierRing(Bargain.BargainTier tier) {
        return switch (tier) {
            case EARLY, ANY -> 0;
            case EARLY_MID -> 1;
            case MID -> 2;
            case LATE -> 3;
            case EXTREME -> 4;
        };
    }

    @Override
    public void tick() {
        super.tick();
        totalTicks++;

        // Fade in
        if (fadeAlpha < 1f) {
            fadeAlpha = Math.min(1f, fadeAlpha + (1f / FADE_TICKS));
        }

        // Soul animations
        soulPulse += 0.08f;
        soulBreath += 0.03f;

        // Note: No constellation rotation - keeps nodes stationary for better UX

        // Update stars
        for (StarParticle star : stars) {
            star.tick();
            if (star.isDead()) {
                star.reset(width, height, random);
            }
        }

        // Update node animations (pulse effects only)
        for (BargainNode node : nodes) {
            node.tick();
        }

        // Panel slide animation - save previous for interpolation
        panelSlidePrev = panelSlide;
        selectionAnimationPrev = selectionAnimation;

        if (selectedNode != null) {
            panelSlide = Math.min(1f, panelSlide + 0.1f);
            selectionAnimation += 0.15f;
        } else {
            panelSlide = Math.max(0f, panelSlide - 0.15f);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Deep space galaxy background shader
        BackgroundRenderer.render(graphics.pose(), BackgroundRenderer.BackgroundType.GALAXY, fadeAlpha, width, height);

        if (fadeAlpha < 0.1f) return;

        // Render additional star particles on top of shader background
        for (StarParticle star : stars) {
            star.render(graphics, fadeAlpha, partialTick);
        }

        // Render vignette
        renderVignette(graphics);

        // Render orbital rings (faint guide lines)
        renderOrbitalRings(graphics);

        // Render connecting lines between nodes
        renderConnections(graphics);

        // Update and render all nodes
        // Update screen positions every frame for smooth dragging
        int centerX = width / 2;
        int centerY = height / 2;
        hoveredNode = null;
        for (BargainNode node : nodes) {
            node.updateScreenPosition(centerX, centerY, viewOffsetX, viewOffsetY, zoom);
            boolean hovered = node.isMouseOver(mouseX, mouseY);
            if (hovered) {
                hoveredNode = node;
            }
            node.render(graphics, font, fadeAlpha, hovered, node == selectedNode, totalTicks, zoom, partialTick);
        }

        // Render central soul
        renderSoulOrb(graphics, partialTick);

        // Render detail panel if node selected - interpolate for smooth animation
        float smoothPanelSlide = panelSlidePrev + (panelSlide - panelSlidePrev) * partialTick;
        if (smoothPanelSlide > 0.01f) {
            renderDetailPanel(graphics, mouseX, mouseY, smoothPanelSlide);
        }

        // Render hover tooltip for non-selected nodes
        if (hoveredNode != null && hoveredNode != selectedNode) {
            renderNodeTooltip(graphics, hoveredNode, mouseX, mouseY);
        }

        // Render erosion indicator
        renderErosionIndicator(graphics);

        // Render back hint
        renderBackHint(graphics);
    }

    private void renderVignette(GuiGraphics graphics) {
        int vignetteStrength = (int) (fadeAlpha * 150);
        int bandSize = 2; // 2px bands for smooth gradients

        for (int i = 0; i < 50; i += bandSize) {
            int alpha = (int) (vignetteStrength * (1f - (float) i / 50f));
            int color = (alpha << 24);
            graphics.fill(0, i, width, i + bandSize, color);
            graphics.fill(0, height - i - bandSize, width, height - i, color);
        }

        for (int i = 0; i < 70; i += bandSize) {
            int alpha = (int) (vignetteStrength * (1f - (float) i / 70f) * 0.6f);
            int color = (alpha << 24);
            graphics.fill(i, 0, i + bandSize, height, color);
            graphics.fill(width - i - bandSize, 0, width - i, height, color);
        }
    }

    private void renderOrbitalRings(GuiGraphics graphics) {
        int centerX = width / 2 + (int) viewOffsetX;
        int centerY = height / 2 + (int) viewOffsetY;
        int alpha = (int) (fadeAlpha * 25);

        for (int ring = 0; ring < 5; ring++) {
            float radius = (ORBIT_BASE_RADIUS + (ring * ORBIT_RING_SPACING)) * zoom;
            int color = (alpha << 24) | 0x404060;

            // Draw ring as series of dashed segments (fewer draws)
            int segments = 24; // Fixed low segment count for performance
            for (int i = 0; i < segments; i += 2) { // Skip every other for dashed effect
                float angle = (float) (2 * Math.PI * i / segments);
                int x = centerX + (int) (Math.cos(angle) * radius);
                int y = centerY + (int) (Math.sin(angle) * radius);
                graphics.fill(x - 1, y - 1, x + 2, y + 2, color);
            }
        }
    }

    private void renderConnections(GuiGraphics graphics) {
        // Draw faint lines from soul to each bargain (brighter for active)
        int centerX = width / 2 + (int) viewOffsetX;
        int centerY = height / 2 + (int) viewOffsetY;

        for (BargainNode node : nodes) {
            int[] color = node.getColor();
            int alpha;

            // Different visibility based on state
            switch (node.state) {
                case ACTIVE -> alpha = (int) (fadeAlpha * 60);  // Brightest
                case AVAILABLE -> alpha = (int) (fadeAlpha * 20); // Dim
                case SCARRED -> alpha = (int) (fadeAlpha * 10);   // Very faint
                default -> alpha = (int) (fadeAlpha * 15);
            }

            int lineColor = (alpha << 24) | (color[0] << 16) | (color[1] << 8) | color[2];

            // Line from center to node
            drawLine(graphics, centerX, centerY, (int) node.screenX, (int) node.screenY, lineColor);
        }
    }

    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps == 0) return;

        // Draw fewer, larger dots for performance
        int dotSpacing = Math.max(8, steps / 12); // Max ~12 dots per line
        for (int i = 0; i <= steps; i += dotSpacing) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            graphics.fill(x, y, x + 2, y + 2, color);
        }
    }

    private void renderSoulOrb(GuiGraphics graphics, float partialTick) {
        int centerX = width / 2 + (int) viewOffsetX;
        int centerY = height / 2 + (int) viewOffsetY;

        int[] rgb = getSoulColor();

        // Smooth animation with partialTick for 60fps fluidity
        float smoothBreath = soulBreath + (0.03f * partialTick);
        float smoothPulse = soulPulse + (0.08f * partialTick);
        float breath = (float) Math.sin(smoothBreath) * 0.05f + 1f;
        float pulse = (float) Math.sin(smoothPulse) * 0.08f + 1f;
        int baseRadius = (int) (30 * zoom);
        int radius = (int) (baseRadius * breath * pulse);

        int alpha = (int) (fadeAlpha * 255);

        // Render ethereal flame aura BEHIND the soul orb
        int auraRadius = (int) (baseRadius * 1.8f * zoom);
        SoulAuraRenderer.render(
                graphics.pose(),
                centerX, centerY,
                auraRadius,
                erosion,
                fadeAlpha * 0.8f,
                width, height);

        // Outer glow - use 4px steps
        for (int r = radius + 30; r > radius; r -= 4) {
            float glowProgress = (float) (r - radius) / 30f;
            int glowAlpha = (int) ((1f - glowProgress) * 35 * fadeAlpha);
            int color = (glowAlpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            drawCircleFast(graphics, centerX, centerY, r, color);
        }

        // Core - use 3px steps
        for (int r = radius; r > 0; r -= 3) {
            float coreProgress = (float) r / radius;
            int coreAlpha = (int) (alpha * (0.6f + 0.4f * coreProgress));
            int lr = Math.min(255, rgb[0] + (int) ((255 - rgb[0]) * (1f - coreProgress) * 0.3f));
            int lg = Math.min(255, rgb[1] + (int) ((255 - rgb[1]) * (1f - coreProgress) * 0.3f));
            int lb = Math.min(255, rgb[2] + (int) ((255 - rgb[2]) * (1f - coreProgress) * 0.3f));
            int color = (coreAlpha << 24) | (lr << 16) | (lg << 8) | lb;
            drawCircleFast(graphics, centerX, centerY, r, color);
        }
    }

    private void drawCircle(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
            graphics.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 1, color);
        }
    }

    private void drawCircleFast(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        if (radius <= 0) return;

        // For small radii, use precise 1px drawing
        if (radius <= 6) {
            drawCircle(graphics, cx, cy, radius, color);
            return;
        }

        // Use 2px bands for good quality
        int bandSize = 2;

        for (int y = -radius; y <= radius; y += bandSize) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
            int bandEnd = Math.min(y + bandSize, radius + 1);
            graphics.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + bandEnd, color);
        }
    }

    private int[] getSoulColor() {
        int tier = ReflectionConstants.getSoulColorTier(erosion);
        return switch (tier) {
            case 0 -> new int[] { 220, 220, 235 };
            case 1 -> new int[] { 180, 200, 255 };
            case 2 -> new int[] { 140, 120, 220 };
            case 3 -> new int[] { 180, 80, 160 };
            case 4 -> new int[] { 160, 50, 50 };
            case 5 -> new int[] { 80, 30, 30 };
            default -> new int[] { 20, 10, 30 };
        };
    }

    private void renderDetailPanel(GuiGraphics graphics, int mouseX, int mouseY, float smoothPanelSlide) {
        if (selectedNode == null) return;

        Bargain bargain = selectedNode.bargain;

        // Panel dimensions - wider to fit text better
        int panelWidth = 240;
        int maxTextWidth = panelWidth - 20; // 10px padding on each side

        // Calculate dynamic height based on content
        int contentHeight = 36; // Header space (title + tier)

        // Cost section height (variable based on what costs exist)
        // These are reused later for rendering and affordability checks
        int shardCost = bargain.getShardCost();
        int weightCost = bargain.getWeight();
        int erosionCost = bargain.getErosionCost();
        int remainingCapacity = totalCapacity - usedCapacity;
        int costLines = 0;
        if (shardCost > 0) costLines++;
        if (weightCost > 0) costLines++;
        if (erosionCost > 0) costLines++;
        if (costLines == 0) costLines = 1; // "Free" line
        contentHeight += costLines * 11 + 12; // costs + divider spacing

        for (Component line : bargain.getPowerDescriptions()) {
            contentHeight += wrapText(line.getString(), maxTextWidth).size() * 11;
        }
        contentHeight += 30; // Action hint space

        int panelHeight = Math.max(180, contentHeight);

        // Slide in from right - use interpolated value for smooth animation
        int panelX = width - (int) (smoothPanelSlide * (panelWidth + 20));
        int panelY = (height - panelHeight) / 2;

        int bgAlpha = (int) (fadeAlpha * smoothPanelSlide * 230);
        int borderAlpha = (int) (fadeAlpha * smoothPanelSlide * 255);

        // Background
        int bgColor = (bgAlpha << 24) | 0x101018;
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, bgColor);

        // Border
        int borderColor = (borderAlpha << 24) | 0x404080;
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, borderColor);
        graphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, borderColor);
        graphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, borderColor);
        graphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, borderColor);

        int textAlpha = (int) (fadeAlpha * smoothPanelSlide * 255);

        // Title
        String title = bargain.getDisplayName().getString();
        int[] nodeColor = selectedNode.getColor();
        int titleColor = (textAlpha << 24) | (nodeColor[0] << 16) | (nodeColor[1] << 8) | nodeColor[2];
        graphics.drawString(font, title, panelX + 10, panelY + 10, titleColor, false);

        // Tier
        String tierStr = bargain.getTier().name();
        int subtitleColor = (textAlpha << 24) | 0x888888;
        graphics.drawString(font, tierStr, panelX + 10, panelY + 24, subtitleColor, false);

        // Costs with affordability coloring (reusing variables from height calculation)
        int costY = panelY + 36;

        if (shardCost > 0) {
            boolean canAfford = shardBalance >= shardCost;
            int shardTextColor = (textAlpha << 24) | (canAfford ? 0x55FFFF : 0xFF5555);
            graphics.drawString(font, "\u2726 " + shardCost + " shards", panelX + 10, costY, shardTextColor, false);
            costY += 11;
        }
        if (weightCost > 0) {
            boolean canFit = remainingCapacity >= weightCost;
            int weightTextColor = (textAlpha << 24) | (canFit ? 0xAA55FF : 0xFF5555);
            graphics.drawString(font, "\u25C6 " + weightCost + " weight", panelX + 10, costY, weightTextColor, false);
            costY += 11;
        }
        if (erosionCost > 0) {
            int erosionTextColor = (textAlpha << 24) | 0xAA6666;
            graphics.drawString(font, "+" + erosionCost + " erosion", panelX + 10, costY, erosionTextColor, false);
            costY += 11;
        }
        if (shardCost == 0 && weightCost == 0 && erosionCost == 0) {
            int freeColor = (textAlpha << 24) | 0x55FF55;
            graphics.drawString(font, "Free", panelX + 10, costY, freeColor, false);
            costY += 11;
        }

        // Divider (dynamic position based on cost content)
        int dividerY = costY + 4;
        int dividerColor = ((textAlpha / 2) << 24) | 0x606080;
        graphics.fill(panelX + 10, dividerY, panelX + panelWidth - 10, dividerY + 1, dividerColor);

        // Description / power with word wrapping
        int descY = dividerY + 8;
        int descColor = (textAlpha << 24) | 0xBBBBBB;
        for (Component line : bargain.getPowerDescriptions()) {
            for (String wrappedLine : wrapText(line.getString(), maxTextWidth)) {
                graphics.drawString(font, wrappedLine, panelX + 10, descY, descColor, false);
                descY += 11;
            }
        }
        // Note: We don't render drawbacks here since cost is already shown at top

        // Action button based on state
        String actionHint;
        int hintColor;
        boolean isClickable = false;
        switch (selectedNode.state) {
            case AVAILABLE -> {
                // Check affordability (reuse variables from cost section above)
                if (shardCost > shardBalance) {
                    actionHint = "Not enough shards (" + shardBalance + "/" + shardCost + ")";
                    hintColor = 0x888888;
                    isClickable = false;
                } else if (weightCost > remainingCapacity) {
                    actionHint = "Not enough soul capacity (" + remainingCapacity + "/" + weightCost + ")";
                    hintColor = 0x888888;
                    isClickable = false;
                } else {
                    actionHint = "[Click to make bargain]";
                    hintColor = 0x80FF80;
                    isClickable = true;
                }
            }
            case ACTIVE -> {
                actionHint = "[Click to defy - costs " + BargainRegistry.calculateDefianceCost(bargain) + "]";
                hintColor = 0xFF8080;
                isClickable = true;
            }
            case SCARRED -> {
                actionHint = "Forever scarred";
                hintColor = 0x555555;
                isClickable = false;
            }
            default -> {
                actionHint = "";
                hintColor = 0;
                isClickable = false;
            }
        }

        // Store action button bounds for click detection
        actionButtonX = panelX + 10;
        actionButtonY = panelY + panelHeight - 28;
        actionButtonWidth = font.width(actionHint) + 16;
        actionButtonHeight = 20;
        actionButtonVisible = isClickable && smoothPanelSlide > 0.9f;

        if (isClickable) {
            // Draw button background
            int btnBgAlpha = (int) (fadeAlpha * smoothPanelSlide * 60);
            int btnBgColor = (btnBgAlpha << 24) | (hintColor & 0xFFFFFF);
            graphics.fill(actionButtonX - 4, actionButtonY, actionButtonX + actionButtonWidth,
                    actionButtonY + actionButtonHeight, btnBgColor);

            // Draw button border
            int btnBorderAlpha = (int) (fadeAlpha * smoothPanelSlide * 150);
            int btnBorderColor = (btnBorderAlpha << 24) | (hintColor & 0xFFFFFF);
            graphics.fill(actionButtonX - 4, actionButtonY, actionButtonX + actionButtonWidth, actionButtonY + 1,
                    btnBorderColor);
            graphics.fill(actionButtonX - 4, actionButtonY + actionButtonHeight - 1, actionButtonX + actionButtonWidth,
                    actionButtonY + actionButtonHeight, btnBorderColor);
            graphics.fill(actionButtonX - 4, actionButtonY, actionButtonX - 3, actionButtonY + actionButtonHeight,
                    btnBorderColor);
            graphics.fill(actionButtonX + actionButtonWidth - 1, actionButtonY, actionButtonX + actionButtonWidth,
                    actionButtonY + actionButtonHeight, btnBorderColor);
        }

        int textColor = (textAlpha << 24) | (hintColor & 0xFFFFFF);
        graphics.drawString(font, actionHint, actionButtonX, actionButtonY + 6, textColor, false);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        // Handle color codes - preserve them across lines
        String colorPrefix = "";
        if (text.startsWith("\u00A7") && text.length() >= 2) {
            colorPrefix = text.substring(0, 2);
        }

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
                    currentLine = new StringBuilder(colorPrefix + word);
                } else {
                    // Single word too long, just add it
                    lines.add(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private void renderNodeTooltip(GuiGraphics graphics, BargainNode node, int mouseX, int mouseY) {
        String name = node.bargain.getDisplayName().getString();
        int tooltipWidth = font.width(name) + 10;
        int tooltipHeight = 16;

        int x = mouseX + 10;
        int y = mouseY - tooltipHeight - 5;

        // Keep on screen
        if (x + tooltipWidth > width - 10) x = width - tooltipWidth - 10;
        if (y < 10) y = 10;

        int bgAlpha = (int) (fadeAlpha * 200);
        int bgColor = (bgAlpha << 24) | 0x101018;
        graphics.fill(x - 2, y - 2, x + tooltipWidth + 2, y + tooltipHeight + 2, bgColor);

        int[] nodeColor = node.getColor();
        int textColor = (255 << 24) | (nodeColor[0] << 16) | (nodeColor[1] << 8) | nodeColor[2];
        graphics.drawString(font, name, x + 3, y + 3, textColor, false);
    }

    private void renderErosionIndicator(GuiGraphics graphics) {
        int alpha = (int) (fadeAlpha * 100);
        int color = (alpha << 24) | 0x555555;

        String text = "Soul Erosion: " + erosion;
        graphics.drawString(font, text, 15, height - 25, color, false);

        // Render economy display in top right
        renderEconomyDisplay(graphics);
    }

    private void renderEconomyDisplay(GuiGraphics graphics) {
        int alpha = (int) (fadeAlpha * 200);
        if (alpha < 20) return;

        int rightMargin = width - 15;
        int topY = 15;

        // Shard balance (aqua color)
        int shardColor = (alpha << 24) | 0x55FFFF;
        String shardText = "\u2726 " + shardBalance;
        int shardWidth = font.width(shardText);
        graphics.drawString(font, shardText, rightMargin - shardWidth, topY, shardColor, false);

        // Capacity display (purple color)
        int capacityColor = (alpha << 24) | 0xAA55FF;
        String capacityText = usedCapacity + "/" + totalCapacity + " soul";
        int capacityWidth = font.width(capacityText);
        graphics.drawString(font, capacityText, rightMargin - capacityWidth, topY + 12, capacityColor, false);

        // Capacity bar
        int barWidth = 60;
        int barHeight = 4;
        int barX = rightMargin - barWidth;
        int barY = topY + 24;

        // Background
        int bgColor = (alpha << 24) | 0x222222;
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, bgColor);

        // Filled portion
        float fillPercent = totalCapacity > 0 ? (float) usedCapacity / totalCapacity : 0f;
        int fillWidth = (int) (barWidth * fillPercent);
        int fillColor = fillPercent > 0.9f ? ((alpha << 24) | 0xFF5555) :
                fillPercent > 0.7f ? ((alpha << 24) | 0xFFAA55) :
                        ((alpha << 24) | 0xAA55FF);
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + barHeight, fillColor);
        }
    }

    private void renderBackHint(GuiGraphics graphics) {
        int alpha = (int) (fadeAlpha * 120);
        int color = (alpha << 24) | 0x888888;

        String hint = "[ESC] Back";
        graphics.drawString(font, hint, 15, 15, color, false);

        // Zoom level indicator
        String zoomHint = String.format("Zoom: %.0f%%", zoom * 100);
        graphics.drawString(font, zoomHint, 15, 28, color, false);

        // Control hints - positioned below economy display (which takes ~40px)
        String controlHint = "Scroll: Zoom | Right-click drag: Pan";
        int controlWidth = font.width(controlHint);
        graphics.drawString(font, controlHint, width - controlWidth - 15, 55, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Left click - select nodes or action buttons
        if (button == 0) {
            // Check if clicking on the action button in the detail panel
            if (actionButtonVisible && selectedNode != null) {
                if (mx >= actionButtonX - 4 && mx <= actionButtonX + actionButtonWidth &&
                        my >= actionButtonY && my <= actionButtonY + actionButtonHeight) {
                    performNodeAction(selectedNode);
                    return true;
                }
            }

            // Check if clicking on a node
            for (BargainNode node : nodes) {
                if (node.isMouseOver(mx, my)) {
                    if (selectedNode == node) {
                        // Already selected - perform action
                        performNodeAction(node);
                    } else {
                        // Select this node
                        selectedNode = node;
                        selectionAnimation = 0f;
                    }
                    return true;
                }
            }

            // Clicking elsewhere deselects
            selectedNode = null;
            return true;
        }

        // Middle or right click - start dragging
        if (button == 1 || button == 2) {
            isDragging = true;
            lastDragX = mouseX;
            lastDragY = mouseY;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1 || button == 2) {
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
        // Zoom in/out with scroll wheel
        float oldZoom = zoom;
        zoom += (float) delta * 0.1f;
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));

        // Adjust offset to zoom toward mouse position
        if (oldZoom != zoom) {
            float zoomRatio = zoom / oldZoom;
            float mx = (float) mouseX - width / 2f;
            float my = (float) mouseY - height / 2f;
            viewOffsetX = (viewOffsetX - mx) * zoomRatio + mx;
            viewOffsetY = (viewOffsetY - my) * zoomRatio + my;
        }

        return true;
    }

    private void performNodeAction(BargainNode node) {
        switch (node.state) {
            case AVAILABLE -> {
                // Check if player can afford this bargain
                int shardCost = node.bargain.getShardCost();
                int weight = node.bargain.getWeight();
                int remainingCapacity = totalCapacity - usedCapacity;

                if (shardCost > shardBalance) {
                    // Can't afford - not enough shards
                    return;
                }
                if (weight > remainingCapacity) {
                    // Can't fit - not enough soul capacity
                    return;
                }

                // Open bargain offer dialogue in VoidScreen with economy data
                VoidScreen.openWithBargain(node.bargain, erosion, activeBargains,
                        shardBalance, usedCapacity, totalCapacity);
            }
            case ACTIVE -> {
                // TODO: Implement defiance from here
                // For now, go back to hub which has defiance
                onClose();
            }
            case SCARRED -> {
                // Can't do anything with scarred bargains
            }
        }
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class BargainNode {

        final Bargain bargain;
        final int centerX, centerY;
        final float orbitRadius;
        final float baseAngle;
        final NodeState state;

        float screenX, screenY;
        float pulsePhase;

        enum NodeState {
            AVAILABLE,  // Can be acquired
            ACTIVE,     // Currently owned
            SCARRED     // Defied - forever marked
        }

        BargainNode(Bargain bargain, int centerX, int centerY, float orbitRadius, float baseAngle, NodeState state) {
            this.bargain = bargain;
            this.centerX = centerX;
            this.centerY = centerY;
            this.orbitRadius = orbitRadius;
            this.baseAngle = baseAngle;
            this.state = state;
            this.pulsePhase = (float) (Math.random() * Math.PI * 2);
        }

        void tick() {
            // Only update animation phase - position is calculated in render for smooth dragging
            pulsePhase += 0.1f;
        }

        void updateScreenPosition(int viewCenterX, int viewCenterY, float offsetX, float offsetY, float zoom) {
            float scaledRadius = orbitRadius * zoom;
            screenX = viewCenterX + offsetX + (float) (Math.cos(baseAngle) * scaledRadius);
            screenY = viewCenterY + offsetY + (float) (Math.sin(baseAngle) * scaledRadius);
        }

        boolean isMouseOver(int mouseX, int mouseY) {
            float dx = mouseX - screenX;
            float dy = mouseY - screenY;
            float radius = getRadius();
            return dx * dx + dy * dy <= radius * radius * 1.5f; // Slightly generous hitbox
        }

        float getRadius(float zoom) {
            float baseRadius = switch (state) {
                case ACTIVE -> 10f;
                case AVAILABLE -> 7f;
                case SCARRED -> 6f;
            };
            return baseRadius * zoom;
        }

        float getRadius() {
            // For mouse hit detection, use a generous radius
            return switch (state) {
                case ACTIVE -> 12f;
                case AVAILABLE -> 10f;
                case SCARRED -> 8f;
            };
        }

        int[] getColor() {
            if (state == NodeState.SCARRED) {
                return new int[] { 60, 40, 50 }; // Dark, cracked
            }

            // Unique color for each bargain type
            String path = bargain.getId().getPath();
            return switch (path) {
                // EARLY tier - cool/inviting colors
                case "quake_movement" -> new int[] { 100, 200, 255 };  // Cyan - movement
                case "stride" -> new int[] { 120, 220, 180 };          // Seafoam - step assist
                case "darksight" -> new int[] { 160, 120, 255 };       // Violet - night vision
                case "swiftness" -> new int[] { 255, 200, 100 };       // Amber - speed

                // EARLY_MID tier - warmer colors
                case "home" -> new int[] { 255, 220, 100 };            // Gold - hearth
                case "back" -> new int[] { 180, 100, 220 };            // Purple - death echo
                case "vitality" -> new int[] { 255, 120, 120 };        // Coral - health
                case "violence" -> new int[] { 220, 80, 80 };          // Crimson - strength
                case "depths" -> new int[] { 80, 180, 220 };           // Ocean blue - water breathing

                // MID tier - more intense colors
                case "reach" -> new int[] { 200, 160, 255 };           // Lavender - elongated grasp
                case "soft_landing" -> new int[] { 180, 255, 180 };    // Mint - fall immunity
                case "satiated" -> new int[] { 200, 180, 120 };        // Tan - no hunger
                case "carapace" -> new int[] { 160, 160, 180 };        // Steel - armor
                case "cinder" -> new int[] { 255, 140, 60 };           // Flame orange - fire immunity

                // LATE tier - darker/ominous
                case "void_anchor" -> new int[] { 120, 60, 180 };      // Deep purple - void resistance

                default -> {
                    // Hash-based unique color as ultimate fallback
                    int hash = path.hashCode();
                    int r = 100 + Math.abs(hash % 100);
                    int g = 100 + Math.abs((hash >> 8) % 100);
                    int b = 100 + Math.abs((hash >> 16) % 100);
                    yield new int[] { r, g, b };
                }
            };
        }

        void render(GuiGraphics graphics, net.minecraft.client.gui.Font font, float fadeAlpha,
                    boolean hovered, boolean selected, int ticks, float zoom, float partialTick) {
            int[] rgb = getColor();
            float radius = getRadius(zoom);

            // Pulse effect for active/hovered - smooth with partialTick
            float pulse = 1f;
            if (state == NodeState.ACTIVE || hovered || selected) {
                float smoothPulse = pulsePhase + (0.1f * partialTick);
                pulse = 1f + (float) Math.sin(smoothPulse) * 0.15f;
            }
            int renderRadius = (int) (radius * pulse);

            // Alpha based on state
            float stateAlpha = switch (state) {
                case ACTIVE -> 1f;
                case AVAILABLE -> hovered ? 0.9f : 0.5f;
                case SCARRED -> 0.3f;
            };

            int alpha = (int) (fadeAlpha * stateAlpha * 255);

            // Outer glow - only 3 bands instead of 8 for performance
            if (state == NodeState.ACTIVE || hovered || selected) {
                for (int r = renderRadius + 8; r > renderRadius; r -= 3) {
                    float glowProgress = (float) (r - renderRadius) / 8f;
                    int glowAlpha = (int) ((1f - glowProgress) * 60 * fadeAlpha * stateAlpha);
                    int color = (glowAlpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                    drawNodeCircle(graphics, (int) screenX, (int) screenY, r, color);
                }
            }

            // Core
            int coreColor = (alpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            drawNodeCircle(graphics, (int) screenX, (int) screenY, renderRadius, coreColor);

            // Selection ring - only 8 points instead of 12, smooth with partialTick
            if (selected) {
                int ringAlpha = (int) (fadeAlpha * 200);
                int ringColor = (ringAlpha << 24) | 0xFFFFFF;
                float smoothTicks = ticks + partialTick;
                for (int i = 0; i < 8; i++) {
                    float angle = (float) (2 * Math.PI * i / 8) + smoothTicks * 0.05f;
                    int rx = (int) (screenX + Math.cos(angle) * (renderRadius + 5));
                    int ry = (int) (screenY + Math.sin(angle) * (renderRadius + 5));
                    graphics.fill(rx, ry, rx + 2, ry + 2, ringColor);
                }
            }

            // Scar cracks for defied bargains
            if (state == NodeState.SCARRED) {
                int crackAlpha = (int) (fadeAlpha * 150);
                int crackColor = (crackAlpha << 24) | 0x200010;
                Random crackRandom = new Random(bargain.getId().hashCode());
                for (int i = 0; i < 3; i++) {
                    float crackAngle = crackRandom.nextFloat() * (float) Math.PI * 2;
                    int cx = (int) (screenX + Math.cos(crackAngle) * radius * 0.5f);
                    int cy = (int) (screenY + Math.sin(crackAngle) * radius * 0.5f);
                    graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, crackColor);
                }
            }
        }

        private void drawNodeCircle(GuiGraphics graphics, int cx, int cy, int radius, int color) {
            for (int y = -radius; y <= radius; y++) {
                int halfWidth = (int) Math.sqrt(radius * radius - y * y);
                graphics.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 1, color);
            }
        }
    }

    private static class StarParticle {

        float x, y;
        float twinklePhase;
        float maxAlpha;
        int lifetime;
        int age;

        StarParticle(int screenWidth, int screenHeight, Random random) {
            reset(screenWidth, screenHeight, random);
        }

        void reset(int screenWidth, int screenHeight, Random random) {
            x = random.nextFloat() * screenWidth;
            y = random.nextFloat() * screenHeight;
            twinklePhase = random.nextFloat() * (float) Math.PI * 2;
            maxAlpha = 0.2f + random.nextFloat() * 0.4f;
            lifetime = 200 + random.nextInt(300);
            age = 0;
        }

        void tick() {
            age++;
            twinklePhase += 0.08f;
        }

        boolean isDead() {
            return age >= lifetime;
        }

        void render(GuiGraphics graphics, float screenAlpha, float partialTick) {
            // Smooth twinkle with partialTick for 60fps animation
            float smoothTwinkle = twinklePhase + (0.08f * partialTick);
            float twinkle = (float) (Math.sin(smoothTwinkle) * 0.3f + 0.7f);

            // Fade in/out
            float lifeFade = 1f;
            float progress = (float) age / lifetime;
            if (progress < 0.1f) lifeFade = progress / 0.1f;
            else if (progress > 0.9f) lifeFade = (1f - progress) / 0.1f;

            int alpha = (int) (maxAlpha * twinkle * lifeFade * screenAlpha * 255);
            if (alpha <= 0) return;

            int color = (alpha << 24) | 0xCCCCDD;
            graphics.fill((int) x, (int) y, (int) x + 1, (int) y + 1, color);
        }
    }
}
