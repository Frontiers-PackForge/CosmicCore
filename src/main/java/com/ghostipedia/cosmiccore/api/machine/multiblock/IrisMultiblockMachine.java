package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarModuleReceiver;
import com.ghostipedia.cosmiccore.client.gui.widget.stellar.StellarFancyUIWidget;
import com.ghostipedia.cosmiccore.client.gui.widget.stellar.StellarIrisWidget;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.CosmicSounds;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage.BLACK_HOLE;
import static com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage.DEATH;

@Getter
public class IrisMultiblockMachine extends WorkableElectricMultiblockMachine implements IStellarIrisProvider {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            IrisMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    private final NotifiableItemStackHandler inventory;

    @Getter
    protected boolean ignite;
    @Getter
    protected boolean isFuelable;
    protected Object workingSound;

    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onStageSynced")
    private Stage stage = Stage.EMPTY;

    /**
     * Called when the stage field is synced from server to client.
     * Parameters must match the field type (Stage).
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    protected void onStageSynced(Stage newValue, Stage oldValue) {
        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                "[IrisMultiblockMachine] CLIENT onStageSynced: {} -> {}", oldValue, newValue);
        this.scheduleRenderUpdate();
        soundTick();
    }

    /**
     * Custom setter with debug logging to track stage changes.
     */
    public void setStage(Stage newStage) {
        Stage oldStage = this.stage;
        this.stage = newStage;
        // Debug: log all stage changes with stack trace for unusual transitions
        if (oldStage != newStage) {
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                    "[IrisMultiblockMachine] setStage: {} -> {}", oldStage, newStage);
            // If transitioning TO DEATH from EMPTY, log stack trace to find the culprit
            if (oldStage == Stage.EMPTY && newStage == Stage.DEATH) {
                com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                        "[IrisMultiblockMachine] SUSPICIOUS: EMPTY->DEATH transition! Stack trace:",
                        new Exception("Stack trace"));
            }
        }
    }

    /**
     * Custom star color (RGB, no alpha). -1 means use default stage-based color.
     * Persisted and synced to client for rendering.
     */
    @Setter
    @Persisted
    @DescSynced
    private int customStarColor = -1;

    @Persisted
    @DescSynced
    private int prestigePoints = 0;

    @Persisted
    @DescSynced
    private int prestigeTier = 0;

    @DescSynced
    private boolean prestigeAnimationActive = false;

    @DescSynced
    private int lastPrestigePointsEarned = 0;

    private List<IStellarModuleReceiver> connectedModules = new ArrayList<>();
    private List<BlockPos> moduleSlotPositions = new ArrayList<>();

    public enum Stage {
        EMPTY,
        GROWING,
        STAR,
        SUPERSTAR,
        BLACK_HOLE,
        DEATH,
        DEATH_GRACEFUL;
    }

    public IrisMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
        this.inventory = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH);
        // Debug: log initial stage
        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                "[IrisMultiblockMachine] Constructor: initial stage={}", stage);
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    protected void onStatusSynced(RecipeLogic.Status newValue, RecipeLogic.Status oldValue) {
        this.scheduleRenderUpdate();
        soundTick();
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        // Clear old module connections
        if (connectedModules != null) {
            connectedModules.forEach(m -> m.setStellarIris(null));
        }

        // Get modules found during structure check (from moduleSlotPredicate)
        Set<IStellarModuleReceiver> modules = getMultiblockState().getMatchContext()
                .getOrDefault("stellarModules", Collections.emptySet());

        this.connectedModules = new ArrayList<>(modules);

        // Establish connections - tell each module about this Iris
        for (IStellarModuleReceiver module : connectedModules) {
            module.setStellarIris(this);
        }
    }

    /**
     * Registers a module with this Iris. Called by modules when they form or detect a nearby Iris.
     * 
     * @param module The module to register
     * @return true if registration was successful
     */
    public boolean registerModule(IStellarModuleReceiver module) {
        if (!isFormed() || module == null) return false;

        if (!connectedModules.contains(module)) {
            connectedModules.add(module);
            module.setStellarIris(this);

            // Store position for tracking
            if (module instanceof MetaMachine metaMachine) {
                moduleSlotPositions.add(metaMachine.getPos().immutable());
                // Debug logging
                if (getLevel() != null && !getLevel().isClientSide) {
                    com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                            "[StellarIris] Module registered: {} at {}. Total modules: {}",
                            metaMachine.getBlockState().getBlock().getDescriptionId(),
                            metaMachine.getPos(),
                            connectedModules.size());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Unregisters a module from this Iris. Called when a module is broken or invalidated.
     * 
     * @param module The module to unregister
     */
    public void unregisterModule(IStellarModuleReceiver module) {
        if (module == null) return;

        if (connectedModules.remove(module)) {
            module.setStellarIris(null);

            // Remove position tracking
            if (module instanceof MetaMachine metaMachine) {
                moduleSlotPositions.remove(metaMachine.getPos());
            }
        }
    }

    /**
     * Rescans for modules by triggering a structure recheck.
     * This is a heavier operation but ensures consistency.
     */
    public void rescanModules() {
        if (!isFormed()) return;

        // Trigger structure recheck which will re-run the predicates
        getMultiblockState().setError(null);
        checkPatternWithLock();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();

        // Sever all module connections
        if (connectedModules != null) {
            connectedModules.forEach(m -> m.setStellarIris(null));
            connectedModules.clear();
        }
    }

    // -------- IStellarIrisProvider Implementation --------

    @Override
    public int getMaxHeat() {
        // Base heat increases with stage
        return switch (stage) {
            case STAR -> 3600;
            case SUPERSTAR -> 7200;
            case BLACK_HOLE -> 12000;
            default -> 0;
        };
    }

    @Override
    public double getSpeedBonus() {
        // Speed multiplier based on stage
        return switch (stage) {
            case STAR -> 1.0;
            case SUPERSTAR -> 1.5;
            case BLACK_HOLE -> 2.0;
            default -> 0.0;
        };
    }

    @Override
    public double getEnergyDiscount() {
        // Energy discount based on stage (1.0 = no discount, lower = cheaper)
        return switch (stage) {
            case STAR -> 1.0;
            case SUPERSTAR -> 0.8;
            case BLACK_HOLE -> 0.6;
            default -> 1.0;
        };
    }

    @Override
    public int getParallelLimit() {
        // Parallel limit based on stage
        return switch (stage) {
            case STAR -> 4;
            case SUPERSTAR -> 8;
            case BLACK_HOLE -> 16;
            default -> 0;
        };
    }

    /**
     * @return list of currently connected modules (read-only view)
     */
    public List<IStellarModuleReceiver> getConnectedModules() {
        return Collections.unmodifiableList(connectedModules);
    }

    /**
     * @return number of connected modules
     */
    public int getConnectedModuleCount() {
        return connectedModules.size();
    }

    public void setStarStage() {
        Stage[] values = Stage.values();
        int nextVal = (getStage().ordinal() + 1) % values.length;
        setStage(values[nextVal]);
    }

    // -------- Prestige System Methods --------

    /**
     * Checks if the item in the star seed slot is a prestige item (Programmable Mote).
     * 
     * @return true if a prestige item is in the slot
     */
    public boolean hasPrestigeItem() {
        ItemStack stack = inventory.getStackInSlot(0);
        return !stack.isEmpty() && stack.is(CosmicItems.PROGRAMMABLE_MOTE.asItem());
    }

    /**
     * Checks if the iris has an active star (not EMPTY or DEATH states).
     * Required for prestige to be triggered.
     * 
     * @return true if there's an active star to consume
     */
    public boolean hasActiveStar() {
        return stage != Stage.EMPTY && stage != Stage.DEATH && stage != Stage.DEATH_GRACEFUL;
    }

    /**
     * Called when the prestige button is broken (after 3 cracks).
     * Starts the prestige animation sequence - star consumption happens over time.
     * The actual point award and stage reset happen when animation completes.
     */
    public void triggerPrestige() {
        if (!hasPrestigeItem() || !hasActiveStar()) {
            return;
        }

        // Consume the prestige item immediately
        inventory.getStackInSlot(0).shrink(1);

        // Calculate points to award (50 per star for now)
        lastPrestigePointsEarned = calculatePrestigePoints();

        // Log the prestige event
        if (getLevel() != null && !getLevel().isClientSide) {
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                    "[StellarIris] PRESTIGE TRIGGERED at {} - Stage: {}, Points: {}",
                    getPos(), stage, lastPrestigePointsEarned);
        }

        // Start the animation - UI will handle the visual sequence
        // Animation duration: 5s shrink + 3s fade = 8s total (160 ticks)
        prestigeAnimationActive = true;

        // Note: Stage reset and point award happen when completePrestige() is called
        // This is triggered by the UI after animation completes
    }

    /**
     * Called when the prestige animation completes (after ~8 seconds).
     * Awards points and resets the star.
     */
    public void completePrestige() {
        if (!prestigeAnimationActive) return;

        // Award the points
        prestigePoints += lastPrestigePointsEarned;

        // Check for tier advancement
        int newTier = calculatePrestigeTier(prestigePoints);
        if (newTier > prestigeTier) {
            prestigeTier = newTier;
            if (getLevel() != null && !getLevel().isClientSide) {
                com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                        "[StellarIris] PRESTIGE TIER UP! Now tier {} with {} total points",
                        prestigeTier, prestigePoints);
            }
        }

        // Reset the star
        setStage(Stage.EMPTY);

        // End animation state
        prestigeAnimationActive = false;

        if (getLevel() != null && !getLevel().isClientSide) {
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                    "[StellarIris] Prestige complete. Total points: {}, Tier: {}",
                    prestigePoints, prestigeTier);
        }
    }

    /**
     * Calculates prestige points for consuming the current star.
     * For now, flat 50 points per star (will be expanded based on star seed type later).
     * 
     * @return points to award
     */
    private int calculatePrestigePoints() {
        // Base: 50 points per consumed star
        // Future: multiply by star seed quality, stage bonuses, etc.
        return 50;
    }

    /**
     * Determines prestige tier based on total accumulated points.
     * Tiers unlock upgrades and recipe access.
     * 
     * @param totalPoints total prestige points
     * @return tier level (0 = base, higher = more unlocks)
     */
    private int calculatePrestigeTier(int totalPoints) {
        // Tier thresholds (can be adjusted for balance)
        if (totalPoints >= 1000) return 5;
        if (totalPoints >= 500) return 4;
        if (totalPoints >= 250) return 3;
        if (totalPoints >= 100) return 2;
        if (totalPoints >= 50) return 1;
        return 0;
    }

    /**
     * @return current prestige points
     */
    public int getPrestigePoints() {
        return prestigePoints;
    }

    /**
     * @return current prestige tier
     */
    public int getPrestigeTier() {
        return prestigeTier;
    }

    /**
     * @return true if prestige animation is currently playing
     */
    public boolean isPrestigeAnimationActive() {
        return prestigeAnimationActive;
    }

    /**
     * @return points earned in most recent prestige (for animation display)
     */
    public int getLastPrestigePointsEarned() {
        return lastPrestigePointsEarned;
    }

    /**
     * Manually set prestige animation state (for client sync).
     */
    public void setPrestigeAnimationActive(boolean active) {
        this.prestigeAnimationActive = active;
    }

    @Override
    public void clientTick() {
        super.clientTick();
        this.soundTick();
    }

    @OnlyIn(Dist.CLIENT)
    public void soundTick() {
        if (isFormed) {
            var sound = CosmicSounds.CHEMVAT;
            if (stage == DEATH) {
                sound = CosmicSounds.STELLAR_BODY_DYING;
            }
            if (stage == BLACK_HOLE) {
                sound = CosmicSounds.BLACK_HOLE_CRY;
            }

            if (workingSound instanceof AutoReleasedSound soundEntry) {
                if (soundEntry.soundEntry == sound && !soundEntry.isStopped()) {
                    return;
                }
                soundEntry.release();
                workingSound = null;
            }
            if (sound != null) {
                workingSound = sound.playAutoReleasedSound(
                        () -> this.shouldWorkingPlaySound() && !this.isInValid() &&
                                this.getLevel().isLoaded(this.getPos()) &&
                                MetaMachine.getMachine(this.getLevel(), this.getPos()) == this,
                        RelativeDirection.offsetPos(this.getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped, 0,
                                0, -47),
                        true, 0, 1, 1);
            }

        } else if (workingSound instanceof AutoReleasedSound soundEntry) {
            soundEntry.release();
            workingSound = null;
        }
    }

    @Override
    public Widget createUIWidget() {
        return new StellarIrisWidget(() -> this);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            textList.add(Component.translatable(stage.toString()));
            textList.add(Component.translatable("cosmiccore.multiblock.iris.star_stage_sustain"));
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .widget(new StellarFancyUIWidget(this, 176, 166, this::getStage));
    }
}
