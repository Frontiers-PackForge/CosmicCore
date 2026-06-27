package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarModuleReceiver;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.CosmicSounds;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage.BLACK_HOLE;
import static com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage.DEATH;

@Getter
public class IrisMultiblockMachine extends WorkableElectricMultiblockMachine implements IStellarIrisProvider {

    @Getter
    @SaveField
    private final NotifiableItemStackHandler inventory;

    @Getter
    protected boolean ignite;
    @Getter
    protected boolean isFuelable;
    protected Object workingSound;

    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private Stage stage = Stage.EMPTY;

    // 8.0.0: LDLib @UpdateListener is dead under the new sync system. @RerenderOnChanged re-renders the
    // block when `stage` syncs; @ClientFieldChangeListener fires this (client-side) for sound. The
    // setter MUST markClientSyncFieldDirty or the field never syncs (it stayed "dormant" on the client).
    @SuppressWarnings("unused")
    @ClientFieldChangeListener(fieldName = "stage")
    protected void onStageSynced() {
        this.scheduleRenderUpdate();
        soundTick();
    }

    public void setStage(Stage newStage) {
        this.stage = newStage;
        if (!isRemote()) {
            getSyncDataHolder().markClientSyncFieldDirty("stage");
        }
    }

    /**
     * 8.0.0 sync: {@code @SyncToClient} fields are NOT auto-detected — the setter must mark the field
     * dirty (server-side) for it to reach the client. Helper for all the synced state below.
     */
    private void markSynced(String field) {
        if (!isRemote()) {
            getSyncDataHolder().markClientSyncFieldDirty(field);
        }
    }

    /**
     * Custom star color (RGB, no alpha). -1 means use default stage-based color.
     * Persisted and synced to client for rendering.
     */
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private int customStarColor = -1;

    public void setCustomStarColor(int customStarColor) {
        this.customStarColor = customStarColor;
        markSynced("customStarColor");
    }

    @SaveField
    @SyncToClient
    private int lifetimePrestigePoints = 0;

    @SaveField
    @SyncToClient
    private int spendablePoints = 0;

    @SaveField
    @SyncToClient
    private int prestigeTier = 0;

    @SaveField
    @SyncToClient
    private int ascensionLevel = 0;

    @SaveField
    @SyncToClient
    private Set<StellarIrisUpgrade> unlockedUpgrades = EnumSet.noneOf(StellarIrisUpgrade.class);

    @SaveField
    @SyncToClient
    private int[] repeatableUpgradeLevels = new int[StellarIrisUpgrade.values().length];

    @SaveField
    @SyncToClient
    private int consecutivePrestiges = 0;

    @SyncToClient
    private boolean prestigeAnimationActive = false;

    @SyncToClient
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

    public IrisMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
        // 8.0.0: NotifiableItemStackHandler ctor no longer takes the machine; attach it as a trait.
        this.inventory = attachTrait(new NotifiableItemStackHandler(1, IO.NONE, IO.BOTH));
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    protected void onStatusSynced(RecipeLogic.Status newValue, RecipeLogic.Status oldValue) {
        this.scheduleRenderUpdate();
        soundTick();
    }

    @Override
    public void formStructure(@org.jetbrains.annotations.NotNull String substructureName) {
        super.formStructure(substructureName);

        // Clear old module connections
        if (connectedModules != null) {
            connectedModules.forEach(m -> m.setStellarIris(null));
        }

        // Re-derive connected modules post-formation (match-context accumulator removed in 8.0.0):
        // scan the formed pattern for IStellarModuleReceiver controllers occupying module slots.
        Set<IStellarModuleReceiver> modules = new HashSet<>();
        var level = getLevel();
        if (level != null) {
            for (var entry : getDefaultPatternState().getCache().long2ObjectEntrySet()) {
                if (MetaMachine.getMachine(level,
                        BlockPos.of(entry.getLongKey())) instanceof IStellarModuleReceiver module) {
                    modules.add(module);
                }
            }
        }

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

            if (module instanceof MetaMachine metaMachine) {
                moduleSlotPositions.add(metaMachine.getBlockPos().immutable());
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
                moduleSlotPositions.remove(metaMachine.getBlockPos());
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
        getDefaultPatternState().setError(null);
        checkAndFormStructure(); // 8.0.0: replaces removed checkPatternWithLock()
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);

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
        // Base speed multiplier from stage
        double base = switch (stage) {
            case STAR -> 1.0;
            case SUPERSTAR -> 1.5;
            case BLACK_HOLE -> 2.0;
            default -> 0.0;
        };

        // Apply upgrade bonuses
        if (hasUpgrade(StellarIrisUpgrade.TEMPORAL_ACCELERATION)) base += 0.15;
        if (hasUpgrade(StellarIrisUpgrade.STELLAR_COMPRESSION) &&
                (stage == Stage.SUPERSTAR || stage == Stage.BLACK_HOLE))
            base += 0.25;
        if (hasUpgrade(StellarIrisUpgrade.VOID_WHISPERS) && stage == Stage.BLACK_HOLE) base += 0.5;

        // Singularity Engine capstone doubles speed bonuses
        if (hasUpgrade(StellarIrisUpgrade.SINGULARITY_ENGINE)) base *= 2.0;

        return base;
    }

    @Override
    public double getEnergyDiscount() {
        // Base energy discount from stage (1.0 = no discount, lower = cheaper)
        double discount = switch (stage) {
            case STAR -> 1.0;
            case SUPERSTAR -> 0.8;
            case BLACK_HOLE -> 0.6;
            default -> 1.0;
        };

        // Apply upgrade bonuses
        if (hasUpgrade(StellarIrisUpgrade.SUPERCONDUCTING_GRID)) discount -= 0.10;
        if (hasUpgrade(StellarIrisUpgrade.MASS_EFFICIENCY)) discount -= 0.15;

        // Singularity Engine capstone
        if (hasUpgrade(StellarIrisUpgrade.SINGULARITY_ENGINE)) discount -= 0.40;

        return Math.max(0.1, discount);
    }

    @Override
    public int getParallelLimit() {
        // Base parallel limit from stage
        int base = switch (stage) {
            case STAR -> 4;
            case SUPERSTAR -> 8;
            case BLACK_HOLE -> 16;
            default -> 0;
        };

        // Graviton Lens: +1 parallel per stage
        if (hasUpgrade(StellarIrisUpgrade.GRAVITON_LENS)) {
            base += switch (stage) {
                case STAR -> 1;
                case SUPERSTAR -> 2;
                case BLACK_HOLE -> 3;
                default -> 0;
            };
        }

        // Parallel Manifold: +2 base
        if (hasUpgrade(StellarIrisUpgrade.PARALLEL_MANIFOLD)) base += 2;

        // Singularity Engine capstone doubles parallel
        if (hasUpgrade(StellarIrisUpgrade.SINGULARITY_ENGINE)) base *= 2;

        return base;
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
        if (getLevel() == null || getLevel().isClientSide) return;
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
                    getBlockPos(), stage, lastPrestigePointsEarned);
        }

        // Start the animation - UI will handle the visual sequence
        // Animation duration: 5s shrink + 3s fade = 8s total (160 ticks)
        prestigeAnimationActive = true;

        markSynced("lastPrestigePointsEarned");
        markSynced("prestigeAnimationActive");

        // Note: Stage reset and point award happen when completePrestige() is called
        // This is triggered by the UI after animation completes
    }

    /**
     * Called when the prestige animation completes (after ~8 seconds).
     * Awards points and resets the star.
     */
    public void completePrestige() {
        if (!prestigeAnimationActive) return;

        // Award the points (both lifetime and spendable)
        lifetimePrestigePoints += lastPrestigePointsEarned;
        spendablePoints += lastPrestigePointsEarned;

        // Track consecutive prestiges for momentum bonus
        consecutivePrestiges++;

        // Check for tier advancement
        int newTier = calculatePrestigeTier(lifetimePrestigePoints);
        if (newTier > prestigeTier) {
            prestigeTier = newTier;
            if (getLevel() != null && !getLevel().isClientSide) {
                com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                        "[StellarIris] PRESTIGE TIER UP! Now tier {} with {} total points",
                        prestigeTier, lifetimePrestigePoints);
            }
        }

        // Reset the star
        setStage(Stage.EMPTY);

        // End animation state
        prestigeAnimationActive = false;

        markSynced("lifetimePrestigePoints");
        markSynced("spendablePoints");
        markSynced("consecutivePrestiges");
        markSynced("prestigeTier");
        markSynced("prestigeAnimationActive");

        if (getLevel() != null && !getLevel().isClientSide) {
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                    "[StellarIris] Prestige complete. Lifetime: {}, Spendable: {}, Tier: {}",
                    lifetimePrestigePoints, spendablePoints, prestigeTier);
        }
    }

    /**
     * Calculates prestige points for consuming the current star.
     * Base points vary by stage, then multiplied by upgrades.
     */
    private int calculatePrestigePoints() {
        // Base points by stage
        int base = switch (stage) {
            case STAR -> 50;
            case SUPERSTAR -> 100;
            case BLACK_HOLE -> 200;
            default -> 25;
        };

        // Resonant Sacrifice: SUPERSTAR gives BLACK_HOLE rewards
        if (hasUpgrade(StellarIrisUpgrade.RESONANT_SACRIFICE) && stage == Stage.SUPERSTAR) {
            base = 200;
        }

        // Void Harvester: BLACK_HOLE gives 3x
        if (hasUpgrade(StellarIrisUpgrade.VOID_HARVESTER) && stage == Stage.BLACK_HOLE) {
            base *= 3;
        }

        // Apply percentage bonuses
        double multiplier = 1.0;

        // Shard Collector: +20%
        if (hasUpgrade(StellarIrisUpgrade.SHARD_COLLECTOR)) multiplier += 0.20;

        // Point Amplifier: +30%
        if (hasUpgrade(StellarIrisUpgrade.POINT_AMPLIFIER)) multiplier += 0.30;

        // Infinite Recursion capstone: 2x
        if (hasUpgrade(StellarIrisUpgrade.INFINITE_RECURSION)) multiplier *= 2.0;

        // Prestige Momentum: +10% per consecutive prestige (max 50%)
        if (hasUpgrade(StellarIrisUpgrade.PRESTIGE_MOMENTUM)) {
            multiplier += Math.min(consecutivePrestiges * 0.10, 0.50);
        }

        return (int) (base * multiplier);
    }

    /**
     * Determines prestige tier based on total accumulated points.
     * Tiers unlock upgrade rows.
     */
    private int calculatePrestigeTier(int totalPoints) {
        if (totalPoints >= 1200) return 5;
        if (totalPoints >= 600) return 4;
        if (totalPoints >= 300) return 3;
        if (totalPoints >= 150) return 2;
        if (totalPoints >= 50) return 1;
        return 0;
    }

    // -------- Upgrade System Methods --------

    /**
     * Check if a non-repeatable upgrade is owned.
     */
    public boolean hasUpgrade(StellarIrisUpgrade upgrade) {
        if (upgrade.isRepeatable()) {
            return getUpgradeLevel(upgrade) > 0;
        }
        return unlockedUpgrades.contains(upgrade);
    }

    public int getUpgradeLevel(StellarIrisUpgrade upgrade) {
        if (upgrade.isRepeatable()) {
            int ordinal = upgrade.ordinal();
            return (ordinal >= 0 && ordinal < repeatableUpgradeLevels.length) ? repeatableUpgradeLevels[ordinal] : 0;
        }
        return unlockedUpgrades.contains(upgrade) ? 1 : 0;
    }

    public Set<StellarIrisUpgrade> getUnlockedUpgrades() {
        return Collections.unmodifiableSet(unlockedUpgrades);
    }

    public int[] getRepeatableUpgradeLevels() {
        return repeatableUpgradeLevels.clone();
    }

    public boolean canUnlockUpgrade(StellarIrisUpgrade upgrade) {
        if (upgrade.isRepeatable()) {
            return getUpgradeLevel(upgrade) < upgrade.getMaxLevel();
        }
        return upgrade.canUnlock(unlockedUpgrades, prestigeTier);
    }

    public boolean tryUnlockUpgrade(StellarIrisUpgrade upgrade) {
        if (upgrade.isRepeatable()) return tryLevelUpRepeatable(upgrade);
        if (!canUnlockUpgrade(upgrade)) return false;
        if (spendablePoints < upgrade.getCost()) return false;

        spendablePoints -= upgrade.getCost();
        unlockedUpgrades.add(upgrade);
        markSynced("spendablePoints");
        markSynced("unlockedUpgrades");
        return true;
    }

    private boolean tryLevelUpRepeatable(StellarIrisUpgrade upgrade) {
        if (!upgrade.isRepeatable()) return false;

        int currentLevel = getUpgradeLevel(upgrade);
        if (currentLevel >= upgrade.getMaxLevel()) return false;

        int cost = upgrade.getCostForLevel(currentLevel + 1);
        if (spendablePoints < cost) return false;

        spendablePoints -= cost;
        int ordinal = upgrade.ordinal();
        if (ordinal >= 0 && ordinal < repeatableUpgradeLevels.length) {
            repeatableUpgradeLevels[ordinal] = currentLevel + 1;
        }
        markSynced("spendablePoints");
        markSynced("repeatableUpgradeLevels");
        return true;
    }

    public int getAscensionLevel() {
        return ascensionLevel;
    }

    public int getLifetimePrestigePoints() {
        return lifetimePrestigePoints;
    }

    public int getSpendablePoints() {
        return spendablePoints;
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
        markSynced("prestigeAnimationActive");
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
                        () -> this.shouldWorkingPlaySound() && !this.isRemoved() &&
                                this.getLevel().isLoaded(this.getBlockPos()) &&
                                MetaMachine.getMachine(this.getLevel(), this.getBlockPos()) == this,
                        RelativeDirection.offsetPos(this.getBlockPos(), getFrontFacing(), getUpwardsFacing(), isFlipped,
                                0,
                                0, -47),
                        true, 0, 1, 1);
            }

        } else if (workingSound instanceof AutoReleasedSound soundEntry) {
            soundEntry.release();
            workingSound = null;
        }
    }

    // TODO(8.0.0 MUI2): the donor (v8-dev-neocyte) drives this machine's UI via MUI2 (StellarIrisPanel /
    // buildUI(PosGuiData, PanelSyncManager, UISettings)). MUI2 (brachy.modularui / client.gui.mui) is NOT
    // available on the 1.21.1 NeoForge target yet (deferred), so the entire UI is dropped here. Re-add
    // buildUI + the StellarIrisPanel once MUI2 lands. All non-UI logic (sync, prestige, modules, sound) is intact.
    //
    // The LDLib addDisplayText(List<Component>) override point was also removed in 8.0.0; multiblock status
    // text is now provided via getWidgetsForDisplay(PanelSyncManager). DESIGN REF (pre-8.0.0) — KEEP,
    // reimplement as IWidget rows if needed:
    /*
     * @Override
     * public void addDisplayText(List<Component> textList) {
     * if (isFormed()) {
     * textList.add(Component.translatable(stage.toString()));
     * textList.add(Component.translatable("cosmiccore.multiblock.iris.star_stage_sustain"));
     * }
     * }
     */
}
