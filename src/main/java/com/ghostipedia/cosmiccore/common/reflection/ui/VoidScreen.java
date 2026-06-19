package com.ghostipedia.cosmiccore.common.reflection.ui;

import com.ghostipedia.cosmiccore.client.renderer.BackgroundRenderer;
import com.ghostipedia.cosmiccore.client.renderer.ChainRenderer;
import com.ghostipedia.cosmiccore.client.renderer.SoulAuraRenderer;
import com.ghostipedia.cosmiccore.client.renderer.SoulCoreRenderer;
import com.ghostipedia.cosmiccore.client.renderer.SoulThreadsRenderer;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.ThresholdEncounter;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain.BargainAnswer;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainRegistry;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * The Void UI - where you face your Reflection.
 *
 * A full-screen dark environment with:
 * - Animated void background with floating particles
 * - Soul orb visualization (color based on erosion, marks from bargains)
 * - Dialogue text with typewriter effect and word wrapping
 * - Stylized choice buttons for bargains
 * - Vignette effect for atmosphere
 */
@OnlyIn(Dist.CLIENT)
public class VoidScreen extends Screen {

    // State machine
    private VoidState state = VoidState.FADE_IN;
    private int ticksInState = 0;
    private int totalTicks = 0;

    // Mode determines what the screen does after dialogue
    private VoidMode mode = VoidMode.REFLECTION;

    // Dialogue system
    private final List<String> dialogueQueue = new ArrayList<>();
    private int currentDialogueIndex = 0;
    private String displayedText = "";
    private int charIndex = 0;
    private int textTickCounter = 0;

    // Current bargain being offered (if any)
    @Nullable
    private Bargain currentBargain;
    private List<BargainAnswer> currentAnswers;

    // Threshold encounter mode (no bargain, just dialogue with acknowledgment)
    private int thresholdIndex = -1;
    private boolean isThresholdEncounter = false;

    // Hub mode - browsing bargains
    private List<Bargain> availableBargains = new ArrayList<>();
    private List<Bargain> playerActiveBargains = new ArrayList<>();
    private int selectedBargainIndex = 0;
    private int bargainListScrollOffset = 0;
    @Nullable
    private Bargain viewingBargain = null; // Currently viewing details of this bargain

    // Defiance scars for display
    private Set<ResourceLocation> defianceScars = Set.of();

    // Soul shape state
    private SoulShape currentSoulShape = SoulShape.UNSHAPED;
    private boolean hasMutilator = false;
    @Nullable
    private SoulShape selectedShape = null;
    private int shapeTransformTicks = 0;
    private boolean isTransforming = false;

    // Flag to open constellation after dialogue
    private boolean pendingConstellationOpen = false;

    // Player's active bargains (for soul marks)
    private Set<ResourceLocation> activeBargains = Set.of();

    // Visual state
    private float fadeAlpha = 0f;
    private float soulPulse = 0f;
    private float soulBreath = 0f;
    private int erosion = 0;

    // Economy state
    private int shardBalance = 0;
    private int usedCapacity = 0;
    private int totalCapacity = 100;

    private final Random random = new Random();

    // Chain physics
    private final ChainRenderer chainRenderer = new ChainRenderer();

    private float lastMouseX, lastMouseY;

    // Answer button state (custom rendering)
    private final List<AnswerButton> answerButtons = new ArrayList<>();
    private int hoveredButton = -1;

    // Constants
    private static final int FADE_TICKS = 40;
    private static final int CHARS_PER_TICK = 2;
    private static final int TICKS_BETWEEN_CHARS = 1;
    private static final int MAX_LINE_WIDTH = 350;

    // Soul shape textures - using GTCEU item textures for distinct silhouettes
    private static final ResourceLocation TEXTURE_REVENANT = ResourceLocation.fromNamespaceAndPath("gtceu",
            "textures/item/material_sets/dull/dust.png");
    private static final ResourceLocation TEXTURE_HOLLOW = ResourceLocation.fromNamespaceAndPath("gtceu",
            "textures/item/material_sets/dull/ring.png");
    private static final ResourceLocation TEXTURE_ENGINE = ResourceLocation.fromNamespaceAndPath("gtceu",
            "textures/item/material_sets/dull/gear.png");
    private static final ResourceLocation TEXTURE_GLOBEDANCER = ResourceLocation.fromNamespaceAndPath("gtceu",
            "textures/item/material_sets/dull/lens.png");
    private static final ResourceLocation TEXTURE_BULWARK = ResourceLocation.fromNamespaceAndPath("gtceu",
            "textures/item/material_sets/dull/plate.png");
    private static final ResourceLocation TEXTURE_BLOODTHIRST = ResourceLocation.fromNamespaceAndPath("gtceu",
            "textures/item/material_sets/dull/crushed.png");

    public VoidScreen(int erosion) {
        super(ReflectionLang.ui("void_title"));
        this.erosion = erosion;
    }

    public VoidScreen(int erosion, Set<ResourceLocation> activeBargains) {
        this(erosion);
        this.activeBargains = activeBargains;
    }

    public static void openWithBargain(Bargain bargain, int erosion) {
        VoidScreen screen = new VoidScreen(erosion);
        screen.currentBargain = bargain;
        screen.setupBargainDialogue();
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openWithBargain(Bargain bargain, int erosion, Set<ResourceLocation> activeBargains) {
        VoidScreen screen = new VoidScreen(erosion, activeBargains);
        screen.currentBargain = bargain;
        screen.setupBargainDialogue();
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openWithBargain(Bargain bargain, int erosion, Set<ResourceLocation> activeBargains,
                                       int shardBalance, int usedCapacity, int totalCapacity) {
        VoidScreen screen = new VoidScreen(erosion, activeBargains);
        screen.currentBargain = bargain;
        screen.shardBalance = shardBalance;
        screen.usedCapacity = usedCapacity;
        screen.totalCapacity = totalCapacity;
        screen.setupBargainDialogue();
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openForReflection(int erosion) {
        VoidScreen screen = new VoidScreen(erosion);
        screen.setupReflectionDialogue();
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openForReflection(int erosion, Set<ResourceLocation> activeBargains) {
        VoidScreen screen = new VoidScreen(erosion, activeBargains);
        screen.setupReflectionDialogue();
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openForReflection(int erosion, Set<ResourceLocation> activeBargains,
                                         int shardBalance, int usedCapacity, int totalCapacity) {
        VoidScreen screen = new VoidScreen(erosion, activeBargains);
        screen.shardBalance = shardBalance;
        screen.usedCapacity = usedCapacity;
        screen.totalCapacity = totalCapacity;
        screen.setupReflectionDialogue();
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openForThreshold(int thresholdIndex, int erosion, Set<ResourceLocation> activeBargains) {
        VoidScreen screen = new VoidScreen(erosion, activeBargains);
        screen.thresholdIndex = thresholdIndex;
        screen.isThresholdEncounter = true;
        screen.setupThresholdDialogue();
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openForHub(int erosion, Set<ResourceLocation> activeBargains,
                                  Set<ResourceLocation> defianceScars) {
        VoidScreen screen = new VoidScreen(erosion, activeBargains);
        screen.mode = VoidMode.HUB;
        screen.defianceScars = defianceScars != null ? defianceScars : Set.of();
        screen.setupHubDialogue();
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openForHub(int erosion, Set<ResourceLocation> activeBargains,
                                  Set<ResourceLocation> defianceScars,
                                  int shardBalance, int usedCapacity, int totalCapacity) {
        openForHub(erosion, activeBargains, defianceScars, shardBalance, usedCapacity, totalCapacity,
                SoulShape.UNSHAPED, false);
    }

    public static void openForHub(int erosion, Set<ResourceLocation> activeBargains,
                                  Set<ResourceLocation> defianceScars,
                                  int shardBalance, int usedCapacity, int totalCapacity,
                                  SoulShape soulShape, boolean hasMutilator) {
        VoidScreen screen = new VoidScreen(erosion, activeBargains);
        screen.mode = VoidMode.HUB;
        screen.defianceScars = defianceScars != null ? defianceScars : Set.of();
        screen.shardBalance = shardBalance;
        screen.usedCapacity = usedCapacity;
        screen.totalCapacity = totalCapacity;
        screen.currentSoulShape = soulShape != null ? soulShape : SoulShape.UNSHAPED;
        screen.hasMutilator = hasMutilator;
        screen.setupHubDialogue();
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
        initChains();
    }

    private static final int PIN_SCREEN_BUFFER = 30;

    private float[] getPinPosition(ResourceLocation bargainId, int centerX, int centerY) {
        int hash = bargainId.hashCode();

        // Restrict pins to left and right zones — avoid directly above/below the orb
        // Map hash to angle within two side arcs: left (-120° to -60°) or right (60° to 120°)
        // Expressed in radians: left = π±60°, right = 0±60°
        boolean leftSide = (hash & 1) == 0;
        float spread = ((hash & 0xFF) / 255.0f - 0.5f) * (float) (Math.PI * 0.65f);
        float pinAngle = leftSide ? (float) Math.PI + spread : spread;

        float pinDist = 120 + ((hash >> 8) & 0x7F);
        float pinX = centerX + (float) Math.cos(pinAngle) * pinDist;
        float pinY = centerY + (float) Math.sin(pinAngle) * pinDist;

        pinX = Math.max(PIN_SCREEN_BUFFER, Math.min(width - PIN_SCREEN_BUFFER, pinX));
        pinY = Math.max(PIN_SCREEN_BUFFER, Math.min(height - PIN_SCREEN_BUFFER, pinY));
        return new float[] { pinX, pinY };
    }

    private void initChains() {
        chainRenderer.clear();
        if (activeBargains.isEmpty()) return;

        int centerX = width / 2;
        int centerY = height / 2 - 40;

        for (ResourceLocation bargainId : activeBargains) {
            float[] pin = getPinPosition(bargainId, centerX, centerY);
            int[] color = getBargainMarkColor(bargainId);
            chainRenderer.addChain(centerX, centerY, pin[0], pin[1], color);
        }
    }

    private void setupBargainDialogue() {
        if (currentBargain == null) return;

        dialogueQueue.clear();

        // Add the bargain's offer dialogue
        if (minecraft != null && minecraft.player != null) {
            for (Component line : currentBargain.getOfferDialogue(minecraft.player)) {
                dialogueQueue.add(line.getString());
            }
        }

        // Add the question
        dialogueQueue.add(currentBargain.getQuestion().getString());

        // Store answers for later
        currentAnswers = currentBargain.getAnswers();
    }

    private void setupReflectionDialogue() {
        dialogueQueue.clear();

        // Contextual greeting based on erosion
        int colorTier = ReflectionConstants.getSoulColorTier(erosion);

        if (erosion == 0) {
            dialogueQueue.add(ReflectionLang.ui("reflection.no_erosion.0").getString());
            dialogueQueue.add(ReflectionLang.ui("reflection.no_erosion.1").getString());
            dialogueQueue.add(ReflectionLang.ui("reflection.no_erosion.2").getString());
        } else if (colorTier <= 1) {
            dialogueQueue.add(ReflectionLang.ui("reflection.low_erosion.0").getString());
            dialogueQueue.add(ReflectionLang.ui("reflection.low_erosion.1").getString());
        } else if (colorTier <= 3) {
            dialogueQueue.add(ReflectionLang.ui("reflection.mid_erosion.0").getString());
            dialogueQueue.add(ReflectionLang.ui("reflection.mid_erosion.1").getString());
        } else if (colorTier <= 5) {
            dialogueQueue.add(ReflectionLang.ui("reflection.high_erosion.0").getString());
            dialogueQueue.add(ReflectionLang.ui("reflection.high_erosion.1").getString());
        } else {
            dialogueQueue.add(ReflectionLang.ui("reflection.extreme_erosion.0").getString());
            dialogueQueue.add(ReflectionLang.ui("reflection.extreme_erosion.1").getString());
        }

        // If they have bargains, comment on them
        if (!activeBargains.isEmpty()) {
            dialogueQueue.add(ReflectionLang.ui("reflection.has_bargains.0").getString());
            dialogueQueue.add(ReflectionLang.ui("reflection.has_bargains.1").getString());
        }
    }

    private void setupThresholdDialogue() {
        dialogueQueue.clear();

        // Get the threshold-specific dialogue
        for (Component line : ThresholdEncounter.getDialogue(thresholdIndex)) {
            dialogueQueue.add(line.getString());
        }

        // Add the rhetorical question/prompt
        String question = ThresholdEncounter.getQuestion(thresholdIndex).getString();
        if (!question.isEmpty()) {
            dialogueQueue.add(question);
        }
    }

    private void setupHubDialogue() {
        dialogueQueue.clear();

        // Contextual greeting based on player's state
        int colorTier = ReflectionConstants.getSoulColorTier(erosion);

        if (!activeBargains.isEmpty()) {
            // Has bargains - comment on them
            if (colorTier >= 4) {
                dialogueQueue.add(ReflectionLang.ui("hub.greeting.many_bargains_high.0").getString());
                dialogueQueue.add(ReflectionLang.ui("hub.greeting.many_bargains_high.1").getString());
            } else if (activeBargains.size() >= 3) {
                dialogueQueue.add(ReflectionLang.ui("hub.greeting.many_bargains.0").getString());
                dialogueQueue.add(ReflectionLang.ui("hub.greeting.many_bargains.1").getString());
            } else {
                dialogueQueue.add(ReflectionLang.ui("hub.greeting.has_bargains.0").getString());
                dialogueQueue.add(ReflectionLang.ui("hub.greeting.has_bargains.1").getString());
            }
        } else if (!defianceScars.isEmpty()) {
            // Has defied before - acknowledge the scars
            dialogueQueue.add(ReflectionLang.ui("hub.greeting.has_scars.0").getString());
            dialogueQueue.add(ReflectionLang.ui("hub.greeting.has_scars.1").getString());
            dialogueQueue.add(ReflectionLang.ui("hub.greeting.has_scars.2").getString());
        } else if (erosion > 0) {
            // Has erosion but no bargains - curious
            dialogueQueue.add(ReflectionLang.ui("hub.greeting.erosion_no_bargains.0").getString());
            dialogueQueue.add(ReflectionLang.ui("hub.greeting.erosion_no_bargains.1").getString());
            dialogueQueue.add(ReflectionLang.ui("hub.greeting.erosion_no_bargains.2").getString());
        } else {
            // Fresh - rare to hit hub with no erosion
            dialogueQueue.add(ReflectionLang.ui("hub.greeting.fresh.0").getString());
            dialogueQueue.add(ReflectionLang.ui("hub.greeting.fresh.1").getString());
        }

        dialogueQueue.add(ReflectionLang.ui("hub.greeting.question").getString());
    }

    private void setupHubMenu() {
        answerButtons.clear();

        int buttonWidth = 280;
        int buttonHeight = 28;
        int startY = height / 2 + 40;
        int spacing = 35;

        // Populate bargain lists for later
        availableBargains = BargainRegistry.getAvailable(activeBargains, defianceScars);
        playerActiveBargains = BargainRegistry.getActive(activeBargains);

        // Menu options
        List<BargainAnswer> menuOptions = new ArrayList<>();

        // Option 1: View active bargains (if any)
        if (!playerActiveBargains.isEmpty()) {
            menuOptions.add(new BargainAnswer(
                    "view_active",
                    ReflectionLang.uiReviewBargains(playerActiveBargains.size())));
        }

        // Option 2: Browse the constellation (always show - displays all bargain states)
        Component browseText = availableBargains.isEmpty() ? ReflectionLang.uiGazeConstellation() :
                ReflectionLang.uiBrowseBargains(availableBargains.size());
        menuOptions.add(new BargainAnswer(
                "browse_bargains",
                browseText));

        // Option 3: Soul mutation (only if has mutilator and not yet shaped)
        if (hasMutilator && !currentSoulShape.isShaped()) {
            menuOptions.add(new BargainAnswer(
                    "mutilate_soul",
                    ReflectionLang.ui("hub.mutilate_soul")));
        }

        // Option 4: Just reflect (always available)
        menuOptions.add(new BargainAnswer(
                "just_reflect",
                ReflectionLang.uiJustLook()));

        // Option 5: Leave
        menuOptions.add(new BargainAnswer(
                "leave",
                ReflectionLang.uiLeave()));

        for (int i = 0; i < menuOptions.size(); i++) {
            BargainAnswer option = menuOptions.get(i);
            int x = (width - buttonWidth) / 2;
            int y = startY + (i * spacing);
            answerButtons.add(new AnswerButton(x, y, buttonWidth, buttonHeight, option, i));
        }
    }

    private void setupBrowseBargains() {
        answerButtons.clear();

        int buttonWidth = 280;
        int buttonHeight = 28;
        int startY = height / 2 + 20;
        int spacing = 35;

        List<BargainAnswer> options = new ArrayList<>();

        // List available bargains
        for (Bargain bargain : availableBargains) {
            String tierStr = getTierString(bargain.getTier());
            int cost = bargain.getBaseCost();

            options.add(new BargainAnswer(
                    "view_" + bargain.getId().getPath(),
                    Component.literal(tierStr + bargain.getDisplayName().getString() + " \u00A78[" + cost + " " +
                            ReflectionLang.ui("erosion").getString() + "]"),
                    Optional.of(ReflectionLang.ui("browse.interesting_choice")),
                    false, 0,
                    bargain.getPowerDescriptions(),
                    bargain.getDrawbackDescriptions()));
        }

        // Back option
        options.add(new BargainAnswer(
                "back",
                ReflectionLang.uiBack()));

        for (int i = 0; i < options.size(); i++) {
            BargainAnswer option = options.get(i);
            int x = (width - buttonWidth) / 2;
            int y = startY + (i * spacing);
            answerButtons.add(new AnswerButton(x, y, buttonWidth, buttonHeight, option, i));
        }
    }

    private void setupViewActiveBargains() {
        answerButtons.clear();

        int buttonWidth = 280;
        int buttonHeight = 28;
        int startY = height / 2 + 60;  // Below the soul orb and orbiting marks
        int spacing = 35;

        // Calculate max visible items based on screen height
        int maxVisibleItems = Math.max(3, (height - startY - 80) / spacing);

        List<BargainAnswer> allOptions = new ArrayList<>();

        // List active bargains with defiance option
        for (Bargain bargain : playerActiveBargains) {
            int defianceCost = BargainRegistry.calculateDefianceCost(bargain);

            allOptions.add(new BargainAnswer(
                    "defy_" + bargain.getId().getPath(),
                    Component.literal("\u00A7c\u2717 " + bargain.getDisplayName().getString() + " \u00A78[" +
                            ReflectionLang.ui("defy").getString() + ": " + defianceCost + "]"),
                    Optional.of(ReflectionLang.ui("defiance.question")),
                    false, 0,
                    bargain.getPowerDescriptions(),
                    List.of(
                            ReflectionLang.uiDefianceCost(defianceCost),
                            ReflectionLang.ui("defiance.lose_power"),
                            ReflectionLang.ui("defiance.scar_remains"))));
        }

        // Back option (always at the end)
        allOptions.add(new BargainAnswer(
                "back",
                ReflectionLang.uiBack()));

        // Store total options for scroll calculation
        viewActiveAllOptions = allOptions;
        viewActiveMaxVisible = maxVisibleItems;

        // Clamp scroll offset
        int maxScroll = Math.max(0, allOptions.size() - maxVisibleItems);
        bargainListScrollOffset = Math.max(0, Math.min(bargainListScrollOffset, maxScroll));

        // Create visible buttons with scroll offset applied
        int visibleCount = Math.min(maxVisibleItems, allOptions.size() - bargainListScrollOffset);
        for (int i = 0; i < visibleCount; i++) {
            int optionIndex = i + bargainListScrollOffset;
            BargainAnswer option = allOptions.get(optionIndex);
            int x = (width - buttonWidth) / 2;
            int y = startY + (i * spacing);
            answerButtons.add(new AnswerButton(x, y, buttonWidth, buttonHeight, option, optionIndex));
        }
    }

    // Track all options for scrolling
    private List<BargainAnswer> viewActiveAllOptions = new ArrayList<>();
    private int viewActiveMaxVisible = 5;

    private void setupDefianceConfirm(Bargain bargain) {
        answerButtons.clear();

        int buttonWidth = 280;
        int buttonHeight = 28;
        int startY = height / 2 + 60;
        int spacing = 35;

        int defianceCost = BargainRegistry.calculateDefianceCost(bargain);

        // Add warning dialogue
        dialogueQueue.clear();
        dialogueQueue.add(ReflectionLang.defianceWarning1(bargain.getDisplayName().getString()).getString());
        dialogueQueue.add(ReflectionLang.defianceWarning2(defianceCost).getString());
        dialogueQueue.add(ReflectionLang.defianceWarning3().getString());
        dialogueQueue.add(ReflectionLang.defianceWarning4().getString());
        currentDialogueIndex = 0;
        charIndex = 0;
        displayedText = "";

        List<BargainAnswer> options = new ArrayList<>();

        // Confirm defiance - simple button, dialogue already explains consequences
        options.add(new BargainAnswer(
                "confirm_defiance",
                ReflectionLang.defianceConfirm()));

        // Cancel
        options.add(new BargainAnswer(
                "cancel",
                ReflectionLang.defianceCancel()));

        for (int i = 0; i < options.size(); i++) {
            BargainAnswer option = options.get(i);
            int x = (width - buttonWidth) / 2;
            int y = startY + (i * spacing);
            answerButtons.add(new AnswerButton(x, y, buttonWidth, buttonHeight, option, i));
        }
    }

    private void setupSoulShapeSelection() {
        answerButtons.clear();

        int buttonWidth = 140;
        int buttonHeight = 28;
        int startY = height / 2 + 50; // Push down to avoid header overlap
        int spacingX = 10;
        int spacingY = 35;
        int columns = 2;

        List<BargainAnswer> shapeOptions = new ArrayList<>();

        // Create a button for each soul shape (excluding UNSHAPED)
        for (SoulShape shape : SoulShape.values()) {
            if (shape == SoulShape.UNSHAPED) continue;

            // Get the shape's color code
            String colorCode = getShapeColorCode(shape);

            shapeOptions.add(new BargainAnswer(
                    "shape_" + shape.getId(),
                    Component.literal(colorCode + shape.getFormattedName().getString()),
                    Optional.of(Component.translatable("cosmiccore.soul_shape." + shape.getId() + ".tagline")),
                    false, 0,
                    List.of(shape.getDescription(),
                            Component.literal("\u00A76Super: ").append(shape.getSuperName()),
                            shape.getSuperDescription()),
                    List.of(ReflectionLang.ui("soul_shape.warning_permanent"))));
        }

        // Layout shape options in 2x3 grid
        int totalWidth = (buttonWidth * columns) + (spacingX * (columns - 1));
        int startX = (width - totalWidth) / 2;

        for (int i = 0; i < shapeOptions.size(); i++) {
            BargainAnswer option = shapeOptions.get(i);
            int col = i % columns;
            int row = i / columns;
            int x = startX + (col * (buttonWidth + spacingX));
            int y = startY + (row * spacingY);
            answerButtons.add(new AnswerButton(x, y, buttonWidth, buttonHeight, option, i));
        }

        // Back button centered below the grid
        int rows = (shapeOptions.size() + columns - 1) / columns;
        int backY = startY + (rows * spacingY) - 5; // Tighter spacing to fit on screen
        int backWidth = 120;
        BargainAnswer backOption = new BargainAnswer("back", ReflectionLang.uiBack());
        answerButtons.add(new AnswerButton((width - backWidth) / 2, backY, backWidth, buttonHeight,
                backOption, shapeOptions.size()));
    }

    private void handleSoulShapeChoice(BargainAnswer answer) {
        answerButtons.clear();

        if (answer.id().equals("back")) {
            transitionTo(VoidState.HUB_MENU);
            setupHubMenu();
            return;
        }

        if (answer.id().startsWith("shape_")) {
            String shapeId = answer.id().substring(6); // Remove "shape_"
            SoulShape shape = SoulShape.fromId(shapeId);

            if (shape != SoulShape.UNSHAPED) {
                selectedShape = shape;

                // Send the choice to the server
                VoidUIPackets.sendSoulShapeChoice(shape);

                // DON'T update currentSoulShape yet - wait until transformation completes

                // Show transformation dialogue then animate
                dialogueQueue.clear();
                dialogueQueue.add(ReflectionLang.ui("soul_shape.transforming.0").getString());
                dialogueQueue.add(ReflectionLang.ui("soul_shape.transforming.1",
                        shape.getFormattedName().getString()).getString());
                currentDialogueIndex = 0;
                charIndex = 0;
                displayedText = "";

                pendingNextState = VoidState.SOUL_TRANSFORMING;
                pendingSetup = () -> {
                    isTransforming = true;
                    shapeTransformTicks = 0;
                };
                transitionTo(VoidState.DIALOGUE);
            }
        }
    }

    private String getShapeColorCode(SoulShape shape) {
        return switch (shape.getColor()) {
            case DARK_RED -> "\u00A74";
            case DARK_PURPLE -> "\u00A75";
            case GOLD -> "\u00A76";
            case AQUA -> "\u00A7b";
            case DARK_GRAY -> "\u00A78";
            case RED -> "\u00A7c";
            default -> "\u00A7f";
        };
    }

    private String getTierString(Bargain.BargainTier tier) {
        return switch (tier) {
            case EARLY -> "\u00A77";      // Gray - early game
            case EARLY_MID -> "\u00A7f";  // White
            case MID -> "\u00A7b";        // Aqua
            case LATE -> "\u00A75";       // Purple
            case EXTREME -> "\u00A74";    // Dark red - dangerous
            case ANY -> "\u00A7e";        // Yellow - always available
        };
    }

    @Override
    public void tick() {
        super.tick();
        ticksInState++;
        totalTicks++;

        // Soul animations
        soulPulse += 0.08f;
        soulBreath += 0.03f;

        // Tick chain physics
        chainRenderer.tick(lastMouseX, lastMouseY);

        switch (state) {
            case FADE_IN -> {
                fadeAlpha = Math.min(1f, (float) ticksInState / FADE_TICKS);
                if (ticksInState >= FADE_TICKS) {
                    transitionTo(VoidState.DIALOGUE);
                }
            }
            case DIALOGUE -> {
                tickDialogue();
            }
            case AWAITING_CHOICE, HUB_MENU, BROWSE_BARGAINS, VIEW_ACTIVE, DEFIANCE_CONFIRM, SOUL_SHAPE_SELECT -> {
                // Just wait for button interaction
            }
            case SOUL_TRANSFORMING -> {
                shapeTransformTicks++;
                // Animation duration: 100 ticks (5 seconds) - longer for smoother transition
                // Phase 1 (0-30): Breaking apart
                // Phase 2 (30-60): Chaos/morphing
                // Phase 3 (60-90): Reforming
                // Phase 4 (90-100): Settling/stabilizing
                if (shapeTransformTicks >= 100) {
                    isTransforming = false;

                    // NOW update the visual shape after animation completes
                    if (selectedShape != null) {
                        currentSoulShape = selectedShape;
                    }

                    // Show completion dialogue then fade out
                    dialogueQueue.clear();
                    if (selectedShape != null) {
                        dialogueQueue.add(ReflectionLang.ui("soul_shape.complete.0",
                                selectedShape.getFormattedName().getString()).getString());
                        dialogueQueue.add(ReflectionLang.ui("soul_shape.complete.1").getString());
                    }
                    currentDialogueIndex = 0;
                    charIndex = 0;
                    displayedText = "";
                    mode = VoidMode.REFLECTION;
                    transitionTo(VoidState.DIALOGUE);
                }
            }
            case FADE_OUT -> {
                fadeAlpha = Math.max(0f, 1f - (float) ticksInState / FADE_TICKS);
                if (ticksInState >= FADE_TICKS) {
                    onClose();
                }
            }
        }
    }

    private void tickDialogue() {
        if (currentDialogueIndex >= dialogueQueue.size()) {
            // Done with dialogue - what comes next depends on mode

            // Check for pending state transition first
            if (pendingNextState != null) {
                VoidState nextState = pendingNextState;
                Runnable setup = pendingSetup;
                pendingNextState = null;
                pendingSetup = null;
                transitionTo(nextState);
                if (setup != null) {
                    setup.run();
                }
                return;
            }

            // Check if we should open the constellation browser
            if (pendingConstellationOpen) {
                pendingConstellationOpen = false;
                BargainConstellationScreen.openFromVoid(erosion, activeBargains, defianceScars,
                        shardBalance, usedCapacity, totalCapacity);
                return;
            }

            if (currentBargain != null && currentAnswers != null) {
                transitionTo(VoidState.AWAITING_CHOICE);
                setupAnswerButtons();
            } else if (isThresholdEncounter) {
                // Threshold encounter - show acknowledge button
                transitionTo(VoidState.AWAITING_CHOICE);
                setupThresholdAcknowledge();
            } else if (mode == VoidMode.HUB) {
                // Hub mode - show menu
                transitionTo(VoidState.HUB_MENU);
                setupHubMenu();
            } else {
                transitionTo(VoidState.FADE_OUT);
            }
            return;
        }

        String fullText = dialogueQueue.get(currentDialogueIndex);

        textTickCounter++;
        if (textTickCounter >= TICKS_BETWEEN_CHARS) {
            textTickCounter = 0;
            charIndex = Math.min(charIndex + CHARS_PER_TICK, fullText.length());
            displayedText = fullText.substring(0, charIndex);
        }
    }

    private void setupThresholdAcknowledge() {
        answerButtons.clear();

        int buttonWidth = 200;
        int buttonHeight = 28;
        int x = (width - buttonWidth) / 2;
        int y = height / 2 + 60;

        // Single "acknowledge" button for threshold encounters
        BargainAnswer acknowledge = new BargainAnswer(
                "acknowledge",
                ReflectionLang.uiAcknowledge());

        answerButtons.add(new AnswerButton(x, y, buttonWidth, buttonHeight, acknowledge, 0));
    }

    private void setupAnswerButtons() {
        answerButtons.clear();

        if (currentAnswers == null) return;

        int buttonWidth = 280;
        int buttonHeight = 28;
        int startY = height / 2 + 60;
        int spacing = 35;

        for (int i = 0; i < currentAnswers.size(); i++) {
            BargainAnswer answer = currentAnswers.get(i);
            int x = (width - buttonWidth) / 2;
            int y = startY + (i * spacing);

            answerButtons.add(new AnswerButton(x, y, buttonWidth, buttonHeight, answer, i));
        }
    }

    private void onAnswerSelected(BargainAnswer answer) {
        // Handle threshold acknowledgment
        if (isThresholdEncounter && answer.id().equals("acknowledge")) {
            dialogueQueue.clear();
            String response = ThresholdEncounter.getAcknowledgeResponse(thresholdIndex).getString();
            if (!response.isEmpty()) {
                dialogueQueue.add(response);
            }
            currentDialogueIndex = 0;
            charIndex = 0;
            displayedText = "";
            answerButtons.clear();
            isThresholdEncounter = false;

            if (response.isEmpty()) {
                // Silent response, just fade out
                transitionTo(VoidState.FADE_OUT);
            } else {
                transitionTo(VoidState.DIALOGUE);
            }
            return;
        }

        // Handle hub menu choices
        if (state == VoidState.HUB_MENU) {
            handleHubMenuChoice(answer);
            return;
        }

        // Handle browse bargains choices
        if (state == VoidState.BROWSE_BARGAINS) {
            handleBrowseBargainsChoice(answer);
            return;
        }

        // Handle view active choices
        if (state == VoidState.VIEW_ACTIVE) {
            handleViewActiveChoice(answer);
            return;
        }

        // Handle defiance confirmation
        if (state == VoidState.DEFIANCE_CONFIRM) {
            handleDefianceConfirmChoice(answer);
            return;
        }

        // Handle soul shape selection
        if (state == VoidState.SOUL_SHAPE_SELECT) {
            handleSoulShapeChoice(answer);
            return;
        }

        if (currentBargain == null) return;

        // Send packet to server to process the choice
        VoidUIPackets.sendBargainChoice(currentBargain.getId(), answer.id());

        // Show response dialogue
        dialogueQueue.clear();
        String response = answer.reflectionResponse()
                .map(Component::getString)
                .orElse("...");
        dialogueQueue.add(response);
        currentDialogueIndex = 0;
        charIndex = 0;
        displayedText = "";

        // Clear buttons and transition back to dialogue, then fade out
        answerButtons.clear();
        currentBargain = null;
        currentAnswers = null;

        transitionTo(VoidState.DIALOGUE);
    }

    private void handleHubMenuChoice(BargainAnswer answer) {
        answerButtons.clear();

        switch (answer.id()) {
            case "view_active" -> {
                // Transition directly to VIEW_ACTIVE
                transitionTo(VoidState.VIEW_ACTIVE);
                setupViewActiveBargains();
            }
            case "browse_bargains" -> {
                // Open the constellation browser with economy data
                BargainConstellationScreen.openFromVoid(erosion, activeBargains, defianceScars,
                        shardBalance, usedCapacity, totalCapacity);
            }
            case "mutilate_soul" -> {
                // Show soul shape selection dialogue then menu
                dialogueQueue.clear();
                dialogueQueue.add(ReflectionLang.ui("soul_shape.intro.0").getString());
                dialogueQueue.add(ReflectionLang.ui("soul_shape.intro.1").getString());
                dialogueQueue.add(ReflectionLang.ui("soul_shape.intro.2").getString());
                currentDialogueIndex = 0;
                charIndex = 0;
                displayedText = "";
                pendingNextState = VoidState.SOUL_SHAPE_SELECT;
                pendingSetup = this::setupSoulShapeSelection;
                transitionTo(VoidState.DIALOGUE);
            }
            case "just_reflect" -> {
                // Just fade out
                transitionTo(VoidState.FADE_OUT);
            }
            case "leave" -> {
                transitionTo(VoidState.FADE_OUT);
            }
        }
    }

    private void handleBrowseBargainsChoice(BargainAnswer answer) {
        answerButtons.clear();

        if (answer.id().equals("back")) {
            // Return to hub menu
            viewingBargain = null;
            transitionTo(VoidState.HUB_MENU);
            setupHubMenu();
            return;
        }

        if (answer.id().startsWith("view_")) {
            // View a specific bargain's details
            String bargainPath = answer.id().substring(5); // Remove "view_"
            for (Bargain bargain : availableBargains) {
                if (bargain.getId().getPath().equals(bargainPath)) {
                    viewingBargain = bargain;
                    currentBargain = bargain;
                    currentAnswers = bargain.getAnswers();
                    // Show the bargain's offer dialogue
                    setupBargainDialogue();
                    transitionTo(VoidState.DIALOGUE);
                    return;
                }
            }
        }
    }

    private void handleViewActiveChoice(BargainAnswer answer) {
        answerButtons.clear();

        if (answer.id().equals("back")) {
            viewingBargain = null;
            transitionTo(VoidState.HUB_MENU);
            setupHubMenu();
            return;
        }

        if (answer.id().startsWith("defy_")) {
            // Initiate defiance for a bargain
            String bargainPath = answer.id().substring(5);
            for (Bargain bargain : playerActiveBargains) {
                if (bargain.getId().getPath().equals(bargainPath)) {
                    viewingBargain = bargain;
                    transitionTo(VoidState.DEFIANCE_CONFIRM);
                    setupDefianceConfirm(bargain);
                    return;
                }
            }
        }
    }

    private void handleDefianceConfirmChoice(BargainAnswer answer) {
        answerButtons.clear();

        if (answer.id().equals("cancel")) {
            viewingBargain = null;
            transitionTo(VoidState.VIEW_ACTIVE);
            setupViewActiveBargains();
            return;
        }

        if (answer.id().equals("confirm_defiance") && viewingBargain != null) {
            // Send defiance packet to server
            VoidUIPackets.sendDefianceChoice(viewingBargain.getId());

            // Show dramatic response
            dialogueQueue.clear();
            dialogueQueue.add("So be it.");
            dialogueQueue.add("The power leaves you...");
            dialogueQueue.add("But the scar remains.");
            currentDialogueIndex = 0;
            charIndex = 0;
            displayedText = "";

            // Update local state
            activeBargains.remove(viewingBargain.getId());
            defianceScars = new java.util.HashSet<>(defianceScars);
            ((java.util.HashSet<ResourceLocation>) defianceScars).add(viewingBargain.getId());
            playerActiveBargains = BargainRegistry.getActive(activeBargains);

            viewingBargain = null;
            mode = VoidMode.REFLECTION; // Return to simple reflection mode
            transitionTo(VoidState.DIALOGUE);
        }
    }

    private void showResponseThenState(String response, VoidState nextState, Runnable setupNext) {
        dialogueQueue.clear();
        dialogueQueue.add(response);
        currentDialogueIndex = 0;
        charIndex = 0;
        displayedText = "";

        // Store what to do after dialogue
        this.pendingNextState = nextState;
        this.pendingSetup = setupNext;
        mode = VoidMode.REFLECTION; // Temporarily switch mode so dialogue finishes normally
        transitionTo(VoidState.DIALOGUE);
    }

    private void showResponseThenFadeOut(String response) {
        dialogueQueue.clear();
        dialogueQueue.add(response);
        currentDialogueIndex = 0;
        charIndex = 0;
        displayedText = "";
        mode = VoidMode.REFLECTION;
        transitionTo(VoidState.DIALOGUE);
    }

    // Pending state for after response dialogue
    @Nullable
    private VoidState pendingNextState = null;
    @Nullable
    private Runnable pendingSetup = null;

    private void transitionTo(VoidState newState) {
        state = newState;
        ticksInState = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Mystical void background shader
        BackgroundRenderer.render(graphics.pose(), BackgroundRenderer.BackgroundType.VOID, fadeAlpha, width, height);

        if (fadeAlpha < 0.1f) {
            return;
        }

        renderVignette(graphics);

        // Track mouse for chain physics
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        chainRenderer.render(graphics, fadeAlpha, mouseX, mouseY, partialTick);
        renderBargainPins(graphics);

        // Render soul orb in center
        renderSoulOrb(graphics, partialTick);

        // Render dialogue text
        if (state == VoidState.DIALOGUE || state == VoidState.AWAITING_CHOICE ||
                state == VoidState.HUB_MENU || state == VoidState.DEFIANCE_CONFIRM ||
                state == VoidState.SOUL_SHAPE_SELECT) {
            renderDialogue(graphics);
        }

        // Render transformation effect
        if (state == VoidState.SOUL_TRANSFORMING && isTransforming) {
            renderTransformationEffect(graphics);
        }

        // Render state-specific headers
        renderStateHeader(graphics);

        // Render click to continue hint
        if (state == VoidState.DIALOGUE && currentDialogueIndex < dialogueQueue.size()) {
            String fullText = dialogueQueue.get(currentDialogueIndex);
            if (charIndex >= fullText.length()) {
                renderContinueHint(graphics);
            }
        }

        // Render bargain cost preview (before buttons so tooltip can cover it)
        if (state == VoidState.AWAITING_CHOICE && currentBargain != null) {
            renderCostPreview(graphics);
        }

        // Render answer buttons for all interactive states
        if (state == VoidState.AWAITING_CHOICE || state == VoidState.HUB_MENU ||
                state == VoidState.BROWSE_BARGAINS || state == VoidState.VIEW_ACTIVE ||
                state == VoidState.DEFIANCE_CONFIRM || state == VoidState.SOUL_SHAPE_SELECT) {
            renderAnswerButtons(graphics, mouseX, mouseY);
        }

        // Render erosion indicator (subtle)
        renderErosionIndicator(graphics);
    }

    private void renderVignette(GuiGraphics graphics) {
        // Create a vignette effect using gradient bands (2px bands for smooth gradients)
        int vignetteStrength = (int) (fadeAlpha * 180);
        int bandSize = 2;

        // Top and bottom gradients
        for (int i = 0; i < 60; i += bandSize) {
            int alpha = (int) (vignetteStrength * (1f - (float) i / 60f));
            int color = (alpha << 24);
            graphics.fill(0, i, width, i + bandSize, color);
            graphics.fill(0, height - i - bandSize, width, height - i, color);
        }

        // Left and right gradients
        for (int i = 0; i < 80; i += bandSize) {
            int alpha = (int) (vignetteStrength * (1f - (float) i / 80f) * 0.7f);
            int color = (alpha << 24);
            graphics.fill(i, 0, i + bandSize, height, color);
            graphics.fill(width - i - bandSize, 0, width - i, height, color);
        }
    }

    private void renderSoulOrb(GuiGraphics graphics, float partialTick) {
        int centerX = width / 2;
        int centerY = height / 2 - 40;

        // Breathing + pulsing animation - interpolate with partialTick for smooth 60fps animation
        float smoothBreath = soulBreath + (0.03f * partialTick);
        float smoothPulse = soulPulse + (0.08f * partialTick);
        float breath = (float) Math.sin(smoothBreath) * 0.05f + 1f;
        float pulse = (float) Math.sin(smoothPulse) * 0.08f + 1f;
        int baseRadius = 35;
        int radius = (int) (baseRadius * breath * pulse);

        int auraRadius = (int) (baseRadius * 1.8f);
        SoulAuraRenderer.render(
                graphics.pose(),
                centerX, centerY,
                auraRadius,
                erosion,
                fadeAlpha * 0.8f,
                width, height);

        // Render shape-specific soul
        if (currentSoulShape.isShaped()) {
            renderShapedSoul(graphics, centerX, centerY, radius, currentSoulShape, 1.0f);
        } else {
            renderUnshapedSoul(graphics, centerX, centerY, radius);
        }
    }

    private void renderUnshapedSoul(GuiGraphics graphics, int centerX, int centerY, int radius) {
        graphics.flush();
        SoulCoreRenderer.render(
                graphics.pose(),
                centerX, centerY,
                radius,
                erosion,
                fadeAlpha,
                width, height);
        SoulThreadsRenderer.render(
                graphics.pose(),
                centerX, centerY,
                radius,
                erosion,
                fadeAlpha,
                width, height);
    }

    private void renderShapedSoul(GuiGraphics graphics, int centerX, int centerY, int radius,
                                  SoulShape shape, float shapeProgress) {
        int[] rgb = getShapeRGB(shape);
        float alpha = fadeAlpha * shapeProgress;

        // Get the texture for this shape
        ResourceLocation texture = getShapeTexture(shape);
        if (texture == null) {
            renderUnshapedSoul(graphics, centerX, centerY, radius);
            return;
        }

        // Outer glow effect (color-tinted circles)
        for (int r = radius + 30; r > radius; r -= 5) {
            float progress = (float) (r - radius) / 30f;
            int gAlpha = (int) ((1f - progress) * 40 * alpha);
            int gColor = (gAlpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            drawCircleFast(graphics, centerX, centerY, r, gColor);
        }

        // Shape-specific animations
        float scale = 1.0f;
        switch (shape) {
            case BLOODTHIRST -> scale = 1.0f + (float) Math.sin(ticksInState * 0.15f) * 0.05f; // Pulse
            default -> {}
        }

        // Render the texture with color tinting
        int size = (int) (radius * 2.2f * scale);
        int x = centerX - size / 2;
        int y = centerY - size / 2;

        // Set color tint
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f, alpha);
        graphics.blit(texture, x, y, 0, 0, size, size, size, size);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Inner core glow for depth
        int coreAlpha = (int) (alpha * 150);
        int coreColor = (coreAlpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
        drawCircleFast(graphics, centerX, centerY, radius / 3, coreColor);
    }

    private ResourceLocation getShapeTexture(SoulShape shape) {
        return switch (shape) {
            case REVENANT -> TEXTURE_REVENANT;
            case HOLLOW -> TEXTURE_HOLLOW;
            case ENGINE -> TEXTURE_ENGINE;
            case GLOBEDANCER -> TEXTURE_GLOBEDANCER;
            case BULWARK -> TEXTURE_BULWARK;
            case BLOODTHIRST -> TEXTURE_BLOODTHIRST;
            default -> null;
        };
    }

    private void renderBargainPins(GuiGraphics graphics) {
        if (activeBargains.isEmpty()) return;

        int centerX = width / 2;
        int centerY = height / 2 - 40;

        int index = 0;
        for (ResourceLocation bargainId : activeBargains) {
            float[] pin = getPinPosition(bargainId, centerX, centerY);
            float pinX = pin[0];
            float pinY = pin[1];

            int[] markColor = getBargainMarkColor(bargainId);
            int alpha = (int) (fadeAlpha * 180);

            // Check if this chain is hovered
            boolean hovered = index < chainRenderer.getChainCount() &&
                    chainRenderer.getChain(index).isHovered();

            // Outer glow
            int glowRadius = hovered ? 8 : 5;
            int glowAlpha = (int) (fadeAlpha * (hovered ? 80 : 40));
            int glowColor = (glowAlpha << 24) | (markColor[0] << 16) | (markColor[1] << 8) | markColor[2];
            drawCircle(graphics, (int) pinX, (int) pinY, glowRadius, glowColor);

            // Core pin
            int coreAlpha = (int) (fadeAlpha * (hovered ? 255 : 200));
            int coreColor = (coreAlpha << 24) | (markColor[0] << 16) | (markColor[1] << 8) | markColor[2];
            drawCircle(graphics, (int) pinX, (int) pinY, 3, coreColor);

            // Bright center
            int brightAlpha = (int) (fadeAlpha * (hovered ? 255 : 180));
            int brightR = Math.min(255, markColor[0] + 80);
            int brightG = Math.min(255, markColor[1] + 80);
            int brightB = Math.min(255, markColor[2] + 80);
            int brightColor = (brightAlpha << 24) | (brightR << 16) | (brightG << 8) | brightB;
            graphics.fill((int) pinX - 1, (int) pinY - 1, (int) pinX + 2, (int) pinY + 2, brightColor);

            index++;
        }
    }

    private int[] getBargainMarkColor(ResourceLocation id) {
        String path = id.getPath();
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
                // Hash-based unique color as fallback
                int hash = path.hashCode();
                int r = 100 + Math.abs(hash % 100);
                int g = 100 + Math.abs((hash >> 8) % 100);
                int b = 100 + Math.abs((hash >> 16) % 100);
                yield new int[] { r, g, b };
            }
        };
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
            case 0 -> new int[] { 220, 220, 235 };  // Pale white/silver
            case 1 -> new int[] { 180, 200, 255 };  // Faint blue
            case 2 -> new int[] { 140, 120, 220 };  // Deep blue/purple
            case 3 -> new int[] { 180, 80, 160 };   // Violet/crimson
            case 4 -> new int[] { 160, 50, 50 };    // Dark red
            case 5 -> new int[] { 80, 30, 30 };     // Almost black, faint red
            default -> new int[] { 20, 10, 30 };    // Void-like
        };
    }

    private void renderDialogue(GuiGraphics graphics) {
        if (displayedText.isEmpty() && state != VoidState.AWAITING_CHOICE) return;

        int textY = height / 2 + 30;
        int alpha = (int) (fadeAlpha * 255);
        int textColor = (alpha << 24) | 0xBBBBBB;

        // Word wrap the text
        List<String> lines = wrapText(displayedText, MAX_LINE_WIDTH);

        // Render each line centered
        int lineHeight = font.lineHeight + 2;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineWidth = font.width(line);
            int textX = (width - lineWidth) / 2;
            int y = textY + (i * lineHeight);

            // Italic style for reflection's voice
            graphics.drawString(font, "\u00A7o" + line, textX, y, textColor, false);
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (font.width(testLine) <= maxWidth) {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                // Handle words longer than max width
                if (font.width(word) > maxWidth) {
                    lines.add(word);
                } else {
                    currentLine.append(word);
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private void renderContinueHint(GuiGraphics graphics) {
        // Pulsing "click to continue" hint
        float pulse = (float) Math.sin(totalTicks * 0.15) * 0.3f + 0.7f;
        int alpha = (int) (fadeAlpha * pulse * 150);
        int color = (alpha << 24) | 0x888888;

        String hint = "[ Click or press Space to continue ]";
        int hintWidth = font.width(hint);
        int x = (width - hintWidth) / 2;
        int y = height / 2 + 80;

        graphics.drawString(font, hint, x, y, color, false);
    }

    private void renderAnswerButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        hoveredButton = -1;
        AnswerButton hoveredBtn = null;

        for (int i = 0; i < answerButtons.size(); i++) {
            AnswerButton button = answerButtons.get(i);
            boolean hovered = button.isMouseOver(mouseX, mouseY);
            if (hovered) {
                hoveredButton = i;
                hoveredBtn = button;
            }
            button.render(graphics, font, fadeAlpha, hovered, totalTicks);
        }

        // Render tooltip for hovered button (rendered last so it's on top)
        if (hoveredBtn != null) {
            renderAnswerTooltip(graphics, hoveredBtn, mouseX, mouseY);
        }
    }

    private void renderAnswerTooltip(GuiGraphics graphics, AnswerButton button, int mouseX, int mouseY) {
        BargainAnswer answer = button.answer;

        // Skip tooltips for simple menu items with no details
        if (answer.powerDescription().isEmpty() && answer.drawbacks().isEmpty()) {
            return;
        }

        // Collect tooltip lines
        List<Component> tooltipLines = new ArrayList<>();

        // Context-aware headers based on current state
        boolean isDefianceContext = (state == VoidState.VIEW_ACTIVE);

        // Add power description with header
        if (!answer.powerDescription().isEmpty()) {
            if (isDefianceContext) {
                tooltipLines.add(Component.literal("\u00A7a\u00A7lCURRENT POWER:\u00A7r"));
            } else {
                tooltipLines.add(Component.literal("\u00A7a\u00A7lPOWERS:\u00A7r"));
            }
            for (Component power : answer.powerDescription()) {
                tooltipLines.add(Component.literal("\u00A7a+ \u00A7f" + power.getString()));
            }
        }

        // Add spacing between power and drawbacks
        if (!answer.powerDescription().isEmpty() && !answer.drawbacks().isEmpty()) {
            tooltipLines.add(Component.literal(""));
        }

        // Add drawbacks with header
        if (!answer.drawbacks().isEmpty()) {
            if (isDefianceContext) {
                tooltipLines.add(Component.literal("\u00A7c\u00A7lDEFIANCE COST:\u00A7r"));
            } else {
                tooltipLines.add(Component.literal("\u00A7c\u00A7lDRAWBACKS:\u00A7r"));
            }
            for (Component drawback : answer.drawbacks()) {
                tooltipLines.add(Component.literal("\u00A7c- \u00A77" + drawback.getString()));
            }
        }

        // Calculate tooltip dimensions
        int tooltipWidth = 0;
        for (Component line : tooltipLines) {
            tooltipWidth = Math.max(tooltipWidth, font.width(line));
        }
        tooltipWidth += 16; // Padding

        int lineHeight = font.lineHeight + 2;
        int tooltipHeight = tooltipLines.size() * lineHeight + 8;

        // Find the topmost button to position tooltip above ALL buttons
        int topmostButtonY = button.y;
        for (AnswerButton btn : answerButtons) {
            if (btn.y < topmostButtonY) {
                topmostButtonY = btn.y;
            }
        }

        // Position tooltip above ALL buttons with some margin
        int tooltipX = (width - tooltipWidth) / 2; // Center horizontally on screen
        int tooltipY = topmostButtonY - tooltipHeight - 15; // Above the topmost button

        // Keep tooltip on screen
        if (tooltipX < 10) tooltipX = 10;
        if (tooltipX + tooltipWidth > width - 10) tooltipX = width - tooltipWidth - 10;
        if (tooltipY < 10) tooltipY = 10;

        // Render tooltip background (solid, no transparency for readability)
        int bgColor = (0xFF << 24) | 0x101018;
        int borderColor = (0xFF << 24) | 0x505080;
        int innerBorderColor = (0xFF << 24) | 0x303050;

        // Outer border
        graphics.fill(tooltipX - 6, tooltipY - 6, tooltipX + tooltipWidth + 6, tooltipY + tooltipHeight + 6,
                borderColor);
        // Inner border
        graphics.fill(tooltipX - 5, tooltipY - 5, tooltipX + tooltipWidth + 5, tooltipY + tooltipHeight + 5,
                innerBorderColor);
        // Background
        graphics.fill(tooltipX - 4, tooltipY - 4, tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 4, bgColor);

        // Render text lines
        int lineY = tooltipY;
        for (Component line : tooltipLines) {
            String text = line.getString();
            if (text.isEmpty()) {
                lineY += lineHeight / 2; // Half spacing for empty lines
            } else {
                graphics.drawString(font, line, tooltipX, lineY, 0xFFFFFFFF, false);
                lineY += lineHeight;
            }
        }
    }

    private void renderCostPreview(GuiGraphics graphics) {
        if (currentBargain == null) return;

        int alpha = (int) (fadeAlpha * 180);

        // Build cost display lines first to know how much space we need
        List<String> costLines = new ArrayList<>();
        List<Integer> costColors = new ArrayList<>();

        int shardCost = currentBargain.getShardCost();
        int weight = currentBargain.getWeight();
        int erosionCost = currentBargain.getErosionCost();

        if (shardCost > 0) {
            boolean canAfford = shardBalance >= shardCost;
            costLines.add("\u2726 " + shardCost + " shards");
            costColors.add((alpha << 24) | (canAfford ? 0x55FFFF : 0xFF5555));
        }

        if (weight > 0) {
            int remaining = totalCapacity - usedCapacity;
            boolean canFit = remaining >= weight;
            costLines.add("\u25C6 " + weight + " weight");
            costColors.add((alpha << 24) | (canFit ? 0xAA55FF : 0xFF5555));
        }

        if (erosionCost > 0) {
            costLines.add("+" + erosionCost + " erosion");
            costColors.add((alpha << 24) | 0xAA6666);
        }

        if (costLines.isEmpty()) {
            costLines.add("Free");
            costColors.add((alpha << 24) | 0x55FF55);
        }

        // Calculate total height needed for cost lines
        int lineHeight = 12;
        int totalCostHeight = costLines.size() * lineHeight;

        // Position: prefer below buttons, but clamp to stay on screen
        int buttonsEndY = height / 2 + 60 + (answerButtons.size() * 35) + 10;
        int idealY = buttonsEndY + 10;

        // Ensure we stay at least 10px from bottom of screen
        int maxY = height - totalCostHeight - 10;
        int y = Math.min(idealY, maxY);

        int centerX = width / 2;

        // Render all cost lines
        for (int i = 0; i < costLines.size(); i++) {
            String line = costLines.get(i);
            int lineWidth = font.width(line);
            graphics.drawString(font, line, centerX - lineWidth / 2, y + (i * lineHeight), costColors.get(i), false);
        }
    }

    private void renderErosionIndicator(GuiGraphics graphics) {
        // Subtle erosion display in bottom left
        int alpha = (int) (fadeAlpha * 100);
        int color = (alpha << 24) | 0x555555;

        Component text = Component.translatable("reflection.cosmiccore.ui.soul_erosion_display", erosion);
        graphics.drawString(font, text, 15, height - 25, color, false);

        // Small colored indicator
        int[] soulColor = getSoulColor();
        int indicatorColor = (alpha << 24) | (soulColor[0] << 16) | (soulColor[1] << 8) | soulColor[2];
        graphics.fill(15, height - 35, 25, height - 28, indicatorColor);

        // Render shards and capacity in top right corner
        renderEconomyDisplay(graphics);
    }

    private void renderEconomyDisplay(GuiGraphics graphics) {
        int alpha = (int) (fadeAlpha * 200);
        if (alpha < 20) return;

        int rightMargin = width - 15;
        int topY = 15;

        // Shard balance (aqua color)
        int shardColor = (alpha << 24) | 0x55FFFF;
        String shardText = "\u2726 " + shardBalance;  // Unicode diamond
        int shardWidth = font.width(shardText);
        graphics.drawString(font, shardText, rightMargin - shardWidth, topY, shardColor, false);

        // Capacity display (purple color) below shards
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
        int fillColor = fillPercent > 0.9f ? ((alpha << 24) | 0xFF5555) :  // Red when almost full
                fillPercent > 0.7f ? ((alpha << 24) | 0xFFAA55) :  // Orange when high
                        ((alpha << 24) | 0xAA55FF);                         // Purple normally
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + barHeight, fillColor);
        }
    }

    private void renderTransformationEffect(GuiGraphics graphics) {
        if (selectedShape == null) return;

        int centerX = width / 2;
        int centerY = height / 2 - 40;

        // Progress through transformation (0.0 to 1.0) over 100 ticks
        float progress = Math.min(1.0f, shapeTransformTicks / 100.0f);

        // Get the shape's color
        int[] shapeColor = getShapeRGB(selectedShape);
        int baseRadius = 35;

        // Phase 1 (0-0.25): Soul starts to distort and crack - old shape breaking apart
        // Phase 2 (0.25-0.5): Soul warps and shifts color - chaotic transition
        // Phase 3 (0.5-0.85): Soul reforms in new shape - new shape emerges
        // Phase 4 (0.85-1.0): Settling - soul stabilizes with gentle pulse

        if (progress < 0.25f) {
            // Phase 1: Distortion - old soul breaking apart
            float phase1Progress = progress / 0.25f;
            int shake = (int) (Math.sin(shapeTransformTicks * 0.8) * 5 * phase1Progress);

            // Render fading old soul (unshaped circle) with shake
            float oldSoulFade = 1.0f - phase1Progress * 0.5f;
            int[] oldRgb = getSoulColor();
            int oldAlpha = (int) (fadeAlpha * 255 * oldSoulFade);
            for (int r = baseRadius; r > 0; r -= 3) {
                float coreProgress = (float) r / baseRadius;
                int cAlpha = (int) (oldAlpha * (0.6f + 0.4f * coreProgress));
                int color = (cAlpha << 24) | (oldRgb[0] << 16) | (oldRgb[1] << 8) | oldRgb[2];
                drawCircleFast(graphics, centerX + shake, centerY, r, color);
            }

            // Draw distortion cracks radiating from center
            int crackAlpha = (int) (fadeAlpha * 200 * phase1Progress);
            int crackColor = (crackAlpha << 24) | 0x200010;

            Random crackRandom = new Random(42);
            for (int i = 0; i < 8; i++) {
                double angle = crackRandom.nextDouble() * Math.PI * 2;
                int length = (int) (50 * phase1Progress);
                int x1 = centerX + shake;
                int y1 = centerY;

                for (int j = 0; j < length; j += 4) {
                    angle += (crackRandom.nextDouble() - 0.5) * 0.6;
                    int x2 = x1 + (int) (Math.cos(angle) * 4);
                    int y2 = y1 + (int) (Math.sin(angle) * 4);
                    graphics.fill(x1, y1, x2 + 2, y2 + 2, crackColor);
                    x1 = x2;
                    y1 = y2;
                }
            }

        } else if (progress < 0.5f) {
            // Phase 2: Warp - chaotic transition, colors shifting
            float phase2Progress = (progress - 0.25f) / 0.25f;

            // Warping waves emanating from center
            for (int wave = 0; wave < 3; wave++) {
                float waveProgress = (phase2Progress + wave * 0.3f) % 1.0f;
                int radius = (int) (30 + 100 * waveProgress);
                int waveAlpha = (int) (fadeAlpha * 150 * (1.0f - waveProgress));

                // Blend between old soul color and new shape color
                int[] oldColor = getSoulColor();
                int r = (int) (oldColor[0] + (shapeColor[0] - oldColor[0]) * phase2Progress);
                int g = (int) (oldColor[1] + (shapeColor[1] - oldColor[1]) * phase2Progress);
                int b = (int) (oldColor[2] + (shapeColor[2] - oldColor[2]) * phase2Progress);

                int color = (waveAlpha << 24) | (r << 16) | (g << 8) | b;
                drawCircleFast(graphics, centerX, centerY, radius, color);
            }

            // Central chaotic vortex - flickering between shapes
            int vortexRadius = (int) (baseRadius * (1.0f - Math.abs(phase2Progress - 0.5f) * 0.5f));
            float shapeFlicker = (float) Math.sin(shapeTransformTicks * 0.5f) * 0.5f + 0.5f;

            if (shapeFlicker > 0.5f) {
                // Brief glimpse of new shape
                renderShapedSoul(graphics, centerX, centerY, vortexRadius, selectedShape,
                        (shapeFlicker - 0.5f) * 2 * phase2Progress);
            } else {
                // Old shape fading
                int vortexAlpha = (int) (fadeAlpha * 200 * (1f - phase2Progress));
                int[] oldRgb = getSoulColor();
                int vortexColor = (vortexAlpha << 24) | (oldRgb[0] << 16) | (oldRgb[1] << 8) | oldRgb[2];
                drawCircleFast(graphics, centerX, centerY, vortexRadius, vortexColor);
            }

        } else if (progress < 0.85f) {
            // Phase 3: Reform - new shape emerges and solidifies
            float phase3Progress = (progress - 0.5f) / 0.35f;

            // Glowing outer aura fading in with shape's color
            int auraRadius = (int) (60 * (2.0f - phase3Progress));
            int auraAlpha = (int) (fadeAlpha * 100 * (1.0f - phase3Progress * 0.7f));
            int auraColor = (auraAlpha << 24) | (shapeColor[0] << 16) | (shapeColor[1] << 8) | shapeColor[2];
            drawCircleFast(graphics, centerX, centerY, auraRadius, auraColor);

            // Render the new shape forming
            int formingRadius = (int) (baseRadius * phase3Progress);
            if (formingRadius > 5) {
                renderShapedSoul(graphics, centerX, centerY, formingRadius, selectedShape, phase3Progress);
            }

            // Particle burst at peak formation
            if (phase3Progress > 0.3f && phase3Progress < 0.7f) {
                float burstProgress = (phase3Progress - 0.3f) / 0.4f;
                int particleCount = 12;
                for (int i = 0; i < particleCount; i++) {
                    double angle = (i * Math.PI * 2 / particleCount) + shapeTransformTicks * 0.1f;
                    int dist = (int) (baseRadius + 30 * burstProgress);
                    int px = centerX + (int) (Math.cos(angle) * dist);
                    int py = centerY + (int) (Math.sin(angle) * dist);
                    int pAlpha = (int) (fadeAlpha * 150 * (1.0f - burstProgress));
                    int pColor = (pAlpha << 24) | (shapeColor[0] << 16) | (shapeColor[1] << 8) | shapeColor[2];
                    drawCircleFast(graphics, px, py, 3, pColor);
                }
            }
        } else {
            // Phase 4: Settling - soul stabilizes with gentle pulse, graceful fade to final state
            float phase4Progress = (progress - 0.85f) / 0.15f;

            // Render fully formed shape at full size
            renderShapedSoul(graphics, centerX, centerY, baseRadius, selectedShape, 1.0f);

            // Gentle pulsing aura that fades out
            float pulse = (float) Math.sin(shapeTransformTicks * 0.3f) * 0.5f + 0.5f;
            int auraRadius = (int) (baseRadius + 10 + 5 * pulse * (1.0f - phase4Progress));
            int auraAlpha = (int) (fadeAlpha * 80 * (1.0f - phase4Progress));
            int auraColor = (auraAlpha << 24) | (shapeColor[0] << 16) | (shapeColor[1] << 8) | shapeColor[2];
            drawCircleFast(graphics, centerX, centerY, auraRadius, auraColor);

            // Fading sparkle particles settling around the shape
            if (phase4Progress < 0.7f) {
                int particleCount = 6;
                float sparkleProgress = phase4Progress / 0.7f;
                for (int i = 0; i < particleCount; i++) {
                    double angle = (i * Math.PI * 2 / particleCount) + shapeTransformTicks * 0.05f;
                    int dist = (int) (baseRadius + 20 * (1.0f - sparkleProgress));
                    int px = centerX + (int) (Math.cos(angle) * dist);
                    int py = centerY + (int) (Math.sin(angle) * dist);
                    int pAlpha = (int) (fadeAlpha * 100 * (1.0f - sparkleProgress));
                    int pColor = (pAlpha << 24) | (shapeColor[0] << 16) | (shapeColor[1] << 8) | shapeColor[2];
                    drawCircleFast(graphics, px, py, 2, pColor);
                }
            }
        }

        // Render the shape name fading in during phase 3-4
        if (progress > 0.6f) {
            float textProgress = Math.min(1.0f, (progress - 0.6f) / 0.25f);
            int textAlpha = (int) (fadeAlpha * 255 * textProgress);
            Component shapeName = selectedShape.getFormattedName();
            int textWidth = font.width(shapeName);
            int textColor = (textAlpha << 24) | (shapeColor[0] << 16) | (shapeColor[1] << 8) | shapeColor[2];
            graphics.drawString(font, shapeName, centerX - textWidth / 2, centerY + 60, textColor, false);
        }
    }

    private int[] getShapeRGB(SoulShape shape) {
        return switch (shape) {
            case REVENANT -> new int[] { 170, 0, 0 };       // Dark red
            case HOLLOW -> new int[] { 170, 0, 170 };       // Dark purple
            case ENGINE -> new int[] { 255, 170, 0 };       // Gold
            case GLOBEDANCER -> new int[] { 85, 255, 255 }; // Aqua
            case BULWARK -> new int[] { 85, 85, 85 };       // Dark gray
            case BLOODTHIRST -> new int[] { 255, 85, 85 };  // Red
            default -> new int[] { 180, 180, 200 };         // Pale
        };
    }

    private void renderStateHeader(GuiGraphics graphics) {
        Component header = switch (state) {
            case BROWSE_BARGAINS -> ReflectionLang.uiAvailableBargains();
            case VIEW_ACTIVE -> ReflectionLang.uiYourBargains();
            case DEFIANCE_CONFIRM -> ReflectionLang.uiDefiance();
            case SOUL_SHAPE_SELECT -> ReflectionLang.ui("soul_shape.select_header");
            default -> null;
        };

        if (header == null) return;

        int alpha = (int) (fadeAlpha * 200);
        int color = (alpha << 24) | 0xAAAAAA;

        int headerWidth = font.width(header);
        int x = (width - headerWidth) / 2;
        // Position header based on state - soul shape select needs more room for 2x3 grid
        int y = (state == VoidState.SOUL_SHAPE_SELECT) ? height / 2 + 35 : height / 2 + 40;

        graphics.drawString(font, header, x, y, color, false);

        // Render scroll indicators for VIEW_ACTIVE state
        if (state == VoidState.VIEW_ACTIVE && !viewActiveAllOptions.isEmpty()) {
            int maxScroll = Math.max(0, viewActiveAllOptions.size() - viewActiveMaxVisible);
            int scrollAlpha = (int) (fadeAlpha * 150);

            // Show "scroll up" indicator if not at top
            if (bargainListScrollOffset > 0) {
                Component upHint = ReflectionLang.uiScrollUp();
                int upWidth = font.width(upHint);
                int upColor = (scrollAlpha << 24) | 0x888888;
                graphics.drawString(font, upHint, (width - upWidth) / 2, height / 2 + 52, upColor, false);
            }

            // Show "scroll down" indicator if not at bottom
            if (bargainListScrollOffset < maxScroll) {
                Component downHint = ReflectionLang.uiScrollDown();
                int downWidth = font.width(downHint);
                int downColor = (scrollAlpha << 24) | 0x888888;
                // Position below the last visible button
                int lastButtonY = height / 2 + 60 + (viewActiveMaxVisible * 35);
                graphics.drawString(font, downHint, (width - downWidth) / 2, lastButtonY + 5, downColor, false);
            }

            // Show scroll position indicator
            String posHint = (bargainListScrollOffset + 1) + "-" +
                    Math.min(bargainListScrollOffset + viewActiveMaxVisible, viewActiveAllOptions.size()) +
                    " " + ReflectionLang.ui("of").getString() + " " + viewActiveAllOptions.size();
            int posWidth = font.width(posHint);
            int posColor = ((scrollAlpha / 2) << 24) | 0x666666;
            graphics.drawString(font, posHint, (width - posWidth) / 2, y + 12, posColor, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        // Check answer buttons for all interactive states
        if (state == VoidState.AWAITING_CHOICE || state == VoidState.HUB_MENU ||
                state == VoidState.BROWSE_BARGAINS || state == VoidState.VIEW_ACTIVE ||
                state == VoidState.DEFIANCE_CONFIRM || state == VoidState.SOUL_SHAPE_SELECT) {
            for (AnswerButton answerButton : answerButtons) {
                if (answerButton.isMouseOver((int) mouseX, (int) mouseY)) {
                    onAnswerSelected(answerButton.answer);
                    return true;
                }
            }
        }

        // Click to advance dialogue
        if (state == VoidState.DIALOGUE) {
            if (currentDialogueIndex < dialogueQueue.size()) {
                String fullText = dialogueQueue.get(currentDialogueIndex);

                if (charIndex < fullText.length()) {
                    // Skip to end of current line
                    charIndex = fullText.length();
                    displayedText = fullText;
                } else {
                    // Advance to next line
                    currentDialogueIndex++;
                    charIndex = 0;
                    displayedText = "";
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC to close (with fade)
        if (keyCode == 256) { // ESCAPE
            if (state != VoidState.FADE_OUT) {
                transitionTo(VoidState.FADE_OUT);
            }
            return true;
        }

        // Space/Enter to advance dialogue
        if ((keyCode == 32 || keyCode == 257) && state == VoidState.DIALOGUE) {
            mouseClicked(0, 0, 0);
            return true;
        }

        // Number keys for quick answer selection
        if (state == VoidState.AWAITING_CHOICE && keyCode >= 49 && keyCode <= 57) {
            int index = keyCode - 49; // 1 = 0, 2 = 1, etc.
            if (index < answerButtons.size()) {
                onAnswerSelected(answerButtons.get(index).answer);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll bargain list in VIEW_ACTIVE state
        if (state == VoidState.VIEW_ACTIVE && !viewActiveAllOptions.isEmpty()) {
            int maxScroll = Math.max(0, viewActiveAllOptions.size() - viewActiveMaxVisible);
            if (scrollY > 0) {
                // Scroll up
                bargainListScrollOffset = Math.max(0, bargainListScrollOffset - 1);
            } else if (scrollY < 0) {
                // Scroll down
                bargainListScrollOffset = Math.min(maxScroll, bargainListScrollOffset + 1);
            }
            // Rebuild buttons with new scroll position
            setupViewActiveBargains();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum VoidState {
        FADE_IN,
        DIALOGUE,
        AWAITING_CHOICE,
        HUB_MENU,           // Hub menu with options
        BROWSE_BARGAINS,    // Browsing available bargains
        VIEW_ACTIVE,        // Viewing player's active bargains
        DEFIANCE_CONFIRM,   // Confirming defiance of a bargain
        SOUL_SHAPE_SELECT,  // Selecting a soul shape
        SOUL_TRANSFORMING,  // Playing transformation animation
        FADE_OUT
    }

    private enum VoidMode {
        REFLECTION,     // General reflection (no bargain)
        BARGAIN_OFFER,  // Offering a specific bargain
        THRESHOLD,      // Erosion threshold encounter
        HUB             // Mirror hub - browse/manage bargains
    }

    private class AnswerButton {

        final int x, y, width, height;
        final BargainAnswer answer;
        final int index;

        AnswerButton(int x, int y, int width, int height, BargainAnswer answer, int index) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.answer = answer;
            this.index = index;
        }

        boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }

        void render(GuiGraphics graphics, net.minecraft.client.gui.Font font, float fadeAlpha, boolean hovered,
                    int ticks) {
            int baseAlpha = (int) (fadeAlpha * (hovered ? 200 : 140));

            // Background
            int bgColor = hovered ? (baseAlpha << 24) | 0x303040 : (baseAlpha << 24) | 0x1a1a24;
            graphics.fill(x, y, x + width, y + height, bgColor);

            // Border
            int borderAlpha = (int) (fadeAlpha * (hovered ? 255 : 150));
            int borderColor = hovered ? (borderAlpha << 24) | 0x6080AA : (borderAlpha << 24) | 0x404060;

            // Top and bottom borders
            graphics.fill(x, y, x + width, y + 1, borderColor);
            graphics.fill(x, y + height - 1, x + width, y + height, borderColor);
            // Left and right borders
            graphics.fill(x, y, x + 1, y + height, borderColor);
            graphics.fill(x + width - 1, y, x + width, y + height, borderColor);

            // Text
            String text = answer.text().getString();
            int textAlpha = (int) (fadeAlpha * 255);
            int textColor = hovered ? (textAlpha << 24) | 0xDDDDEE : (textAlpha << 24) | 0x999999;

            int textWidth = font.width(text);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - font.lineHeight) / 2;

            graphics.drawString(font, text, textX, textY, textColor, false);

            // Keyboard hint
            String hint = "[" + (index + 1) + "]";
            int hintAlpha = (int) (fadeAlpha * 100);
            int hintColor = (hintAlpha << 24) | 0x666666;
            graphics.drawString(font, hint, x + 8, textY, hintColor, false);

            // Hover glow effect
            if (hovered) {
                float glowPulse = (float) Math.sin(ticks * 0.2) * 0.3f + 0.7f;
                int glowAlpha = (int) (fadeAlpha * glowPulse * 30);
                int glowColor = (glowAlpha << 24) | 0x6080AA;
                graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, glowColor);
            }
        }
    }
}
