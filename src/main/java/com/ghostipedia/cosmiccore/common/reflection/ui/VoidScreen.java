package com.ghostipedia.cosmiccore.common.reflection.ui;

import com.ghostipedia.cosmiccore.client.renderer.BackgroundRenderer;
import com.ghostipedia.cosmiccore.client.renderer.SoulAuraRenderer;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.ThresholdEncounter;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain.BargainAnswer;
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

    // Particles
    private final List<VoidParticle> particles = new ArrayList<>();
    private final Random random = new Random();

    // Answer button state (custom rendering)
    private final List<AnswerButton> answerButtons = new ArrayList<>();
    private int hoveredButton = -1;

    // Constants
    private static final int FADE_TICKS = 40;
    private static final int CHARS_PER_TICK = 2;
    private static final int TICKS_BETWEEN_CHARS = 1;
    private static final int MAX_LINE_WIDTH = 350;
    private static final int PARTICLE_COUNT = 30; // Restored for better atmosphere

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
        VoidScreen screen = new VoidScreen(erosion, activeBargains);
        screen.mode = VoidMode.HUB;
        screen.defianceScars = defianceScars != null ? defianceScars : Set.of();
        screen.shardBalance = shardBalance;
        screen.usedCapacity = usedCapacity;
        screen.totalCapacity = totalCapacity;
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
        // Initialize particles
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new VoidParticle(width, height, random));
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

        // Option 3: Just reflect (always available)
        menuOptions.add(new BargainAnswer(
                "just_reflect",
                ReflectionLang.uiJustLook()));

        // Option 4: Leave
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

        // Update particles
        for (VoidParticle particle : particles) {
            particle.tick();
            if (particle.isDead()) {
                particle.reset(width, height, random);
            }
        }

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
            case AWAITING_CHOICE, HUB_MENU, BROWSE_BARGAINS, VIEW_ACTIVE, DEFIANCE_CONFIRM -> {
                // Just wait for button interaction
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

        // Render void particles (on top of shader background)
        renderParticles(graphics, partialTick);

        // Render vignette
        renderVignette(graphics);

        // Render soul orb in center
        renderSoulOrb(graphics, partialTick);

        // Render active bargain marks around soul
        renderBargainMarks(graphics, partialTick);

        // Render dialogue text
        if (state == VoidState.DIALOGUE || state == VoidState.AWAITING_CHOICE ||
                state == VoidState.HUB_MENU || state == VoidState.DEFIANCE_CONFIRM) {
            renderDialogue(graphics);
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
                state == VoidState.DEFIANCE_CONFIRM) {
            renderAnswerButtons(graphics, mouseX, mouseY);
        }

        // Render erosion indicator (subtle)
        renderErosionIndicator(graphics);
    }

    private void renderParticles(GuiGraphics graphics, float partialTick) {
        for (VoidParticle particle : particles) {
            particle.render(graphics, fadeAlpha);
        }
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

        // Calculate orb color based on erosion
        int[] rgb = getSoulColor();

        // Breathing + pulsing animation - interpolate with partialTick for smooth 60fps animation
        float smoothBreath = soulBreath + (0.03f * partialTick);
        float smoothPulse = soulPulse + (0.08f * partialTick);
        float breath = (float) Math.sin(smoothBreath) * 0.05f + 1f;
        float pulse = (float) Math.sin(smoothPulse) * 0.08f + 1f;
        int baseRadius = 35;
        int radius = (int) (baseRadius * breath * pulse);

        int alpha = (int) (fadeAlpha * 255);

        // Render ethereal flame aura BEHIND the soul orb
        int auraRadius = (int) (baseRadius * 1.8f); // Aura is larger than the orb
        SoulAuraRenderer.render(
                graphics.pose(),
                centerX, centerY,
                auraRadius,
                erosion,
                fadeAlpha * 0.8f, // Slightly reduced intensity
                width, height);

        // Outer glow - use 4px steps for smoother appearance
        for (int r = radius + 35; r > radius; r -= 4) {
            float glowProgress = (float) (r - radius) / 35f;
            int glowAlpha = (int) ((1f - glowProgress) * 40 * fadeAlpha);
            int color = (glowAlpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            drawCircleFast(graphics, centerX, centerY, r, color);
        }

        // Core - use 3px steps for smoother appearance
        for (int r = radius; r > 0; r -= 3) {
            float coreProgress = (float) r / radius;
            int coreAlpha = (int) (alpha * (0.6f + 0.4f * coreProgress));
            int lr = Math.min(255, rgb[0] + (int) ((255 - rgb[0]) * (1f - coreProgress) * 0.3f));
            int lg = Math.min(255, rgb[1] + (int) ((255 - rgb[1]) * (1f - coreProgress) * 0.3f));
            int lb = Math.min(255, rgb[2] + (int) ((255 - rgb[2]) * (1f - coreProgress) * 0.3f));
            int color = (coreAlpha << 24) | (lr << 16) | (lg << 8) | lb;
            drawCircleFast(graphics, centerX, centerY, r, color);
        }

        // Inner bright highlight
        int highlightRadius = radius / 4;
        int hx = centerX - radius / 3;
        int hy = centerY - radius / 3;
        int hAlpha = (int) (alpha * 0.4f);
        int hColor = (hAlpha << 24) | 0xFFFFFF;
        drawCircleFast(graphics, hx, hy, highlightRadius, hColor);

        // Erosion cracks at high levels
        if (erosion >= 100) {
            renderCracks(graphics, centerX, centerY, radius);
        }
    }

    private void renderCracks(GuiGraphics graphics, int cx, int cy, int radius) {
        int crackAlpha = (int) (fadeAlpha * Math.min(200, erosion / 5));
        int crackColor = (crackAlpha << 24) | 0x200010;

        // Draw some jagged crack lines - reduced frequency
        Random crackRandom = new Random(42); // Consistent cracks
        int numCracks = Math.min(5, erosion / 200 + 1); // Fewer cracks

        for (int i = 0; i < numCracks; i++) {
            double angle = crackRandom.nextDouble() * Math.PI * 2;
            int length = radius / 2 + crackRandom.nextInt(radius / 2);

            int x1 = cx;
            int y1 = cy;

            // Larger step for fewer draw calls
            for (int j = 0; j < length; j += 5) {
                angle += (crackRandom.nextDouble() - 0.5) * 0.5;
                int x2 = x1 + (int) (Math.cos(angle) * 5);
                int y2 = y1 + (int) (Math.sin(angle) * 5);

                graphics.fill(x1, y1, x2 + 2, y2 + 2, crackColor);
                x1 = x2;
                y1 = y2;
            }
        }
    }

    private void renderBargainMarks(GuiGraphics graphics, float partialTick) {
        if (activeBargains.isEmpty()) return;

        int centerX = width / 2;
        int centerY = height / 2 - 40;
        int orbitRadius = 55;

        int markIndex = 0;
        for (ResourceLocation bargainId : activeBargains) {
            // Position marks in orbit around the soul - smooth with partialTick
            double smoothTicks = totalTicks + partialTick;
            double angle = (smoothTicks * 0.02) + (markIndex * Math.PI * 2 / activeBargains.size());
            int mx = centerX + (int) (Math.cos(angle) * orbitRadius);
            int my = centerY + (int) (Math.sin(angle) * orbitRadius);

            // Each bargain type has a different mark color
            int[] markColor = getBargainMarkColor(bargainId);
            int alpha = (int) (fadeAlpha * 200);
            int color = (alpha << 24) | (markColor[0] << 16) | (markColor[1] << 8) | markColor[2];

            // Draw small orbiting mark as a circle
            drawCircle(graphics, mx, my, 4, color);

            // Trail - 2 fading points for smooth effect
            for (int t = 1; t <= 2; t++) {
                double trailAngle = angle - (t * 0.18);
                int tx = centerX + (int) (Math.cos(trailAngle) * orbitRadius);
                int ty = centerY + (int) (Math.sin(trailAngle) * orbitRadius);
                int trailAlpha = (int) (alpha * (1f - t * 0.35f) * 0.5f);
                int trailColor = (trailAlpha << 24) | (markColor[0] << 16) | (markColor[1] << 8) | markColor[2];
                drawCircle(graphics, tx, ty, 3 - t, trailColor);
            }

            markIndex++;
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

    private void renderStateHeader(GuiGraphics graphics) {
        Component header = switch (state) {
            case BROWSE_BARGAINS -> ReflectionLang.uiAvailableBargains();
            case VIEW_ACTIVE -> ReflectionLang.uiYourBargains();
            case DEFIANCE_CONFIRM -> ReflectionLang.uiDefiance();
            default -> null;
        };

        if (header == null) return;

        int alpha = (int) (fadeAlpha * 200);
        int color = (alpha << 24) | 0xAAAAAA;

        int headerWidth = font.width(header);
        int x = (width - headerWidth) / 2;
        int y = height / 2 + 40;  // Just above the buttons area

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
                state == VoidState.DEFIANCE_CONFIRM) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Scroll bargain list in VIEW_ACTIVE state
        if (state == VoidState.VIEW_ACTIVE && !viewActiveAllOptions.isEmpty()) {
            int maxScroll = Math.max(0, viewActiveAllOptions.size() - viewActiveMaxVisible);
            if (delta > 0) {
                // Scroll up
                bargainListScrollOffset = Math.max(0, bargainListScrollOffset - 1);
            } else if (delta < 0) {
                // Scroll down
                bargainListScrollOffset = Math.min(maxScroll, bargainListScrollOffset + 1);
            }
            // Rebuild buttons with new scroll position
            setupViewActiveBargains();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum VoidState {
        FADE_IN,
        DIALOGUE,
        AWAITING_CHOICE,
        HUB_MENU,        // Hub menu with options
        BROWSE_BARGAINS, // Browsing available bargains
        VIEW_ACTIVE,     // Viewing player's active bargains
        DEFIANCE_CONFIRM,// Confirming defiance of a bargain
        FADE_OUT
    }

    private enum VoidMode {
        REFLECTION,     // General reflection (no bargain)
        BARGAIN_OFFER,  // Offering a specific bargain
        THRESHOLD,      // Erosion threshold encounter
        HUB             // Mirror hub - browse/manage bargains
    }

    private static class VoidParticle {

        float x, y;
        float vx, vy;
        float size;
        float alpha;
        float maxAlpha;
        int lifetime;
        int age;

        VoidParticle(int screenWidth, int screenHeight, Random random) {
            reset(screenWidth, screenHeight, random);
        }

        void reset(int screenWidth, int screenHeight, Random random) {
            x = random.nextFloat() * screenWidth;
            y = random.nextFloat() * screenHeight;
            vx = (random.nextFloat() - 0.5f) * 0.5f;
            vy = (random.nextFloat() - 0.5f) * 0.3f - 0.2f; // Slight upward drift
            size = 1 + random.nextFloat() * 2;
            maxAlpha = 0.2f + random.nextFloat() * 0.3f;
            alpha = 0;
            lifetime = 100 + random.nextInt(200);
            age = 0;
        }

        void tick() {
            x += vx;
            y += vy;
            age++;

            // Fade in and out
            float progress = (float) age / lifetime;
            if (progress < 0.2f) {
                alpha = maxAlpha * (progress / 0.2f);
            } else if (progress > 0.8f) {
                alpha = maxAlpha * (1f - (progress - 0.8f) / 0.2f);
            } else {
                alpha = maxAlpha;
            }
        }

        boolean isDead() {
            return age >= lifetime;
        }

        void render(GuiGraphics graphics, float screenAlpha) {
            int a = (int) (alpha * screenAlpha * 255);
            if (a <= 0) return;

            int color = (a << 24) | 0x404050;
            int s = (int) size;
            graphics.fill((int) x, (int) y, (int) x + s, (int) y + s, color);
        }
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
