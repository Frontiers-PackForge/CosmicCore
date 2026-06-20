package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.MothCargoStation;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Moth Cargo Station - The "sender" multiblock for the Cargo Moths system.
 * <p>
 * Ships items and fluids to linked Moth Cargo Drop Off stations using moths.
 * Does NOT require power - just moths!
 * <p>
 * Features:
 * <ul>
 * <li>Cycle-based shipping (configurable via moth home tiers)</li>
 * <li>Multiple distribution modes (1:1, 1:N fill first, 1:N round robin, N:1)</li>
 * <li>Feeding bonuses from honey/oil</li>
 * <li>Same-dimension only linking</li>
 * </ul>
 */
public class MothCargoStationMachine extends LinkedWorkableMultiblockMachine {


    // ==================== Constants ====================

    /** Base items per moth per cycle (1 stack) */
    public static final int BASE_ITEMS_PER_MOTH = 64;
    /** Base fluid per moth per cycle (1000 mB) */
    public static final int BASE_FLUID_PER_MOTH = 1000;

    /** Cycle times in ticks per tier (T1=60s, T2=30s, T3=15s, T4=5s) */
    public static final int[] CYCLE_TICKS_BY_TIER = { 1200, 600, 300, 100 };
    /** Moths per home by tier (T1=1, T2=2, T3=4, T4=8) */
    public static final int[] MOTHS_PER_HOME_BY_TIER = { 1, 2, 4, 8 };
    /** Max moth homes per station */
    public static final int MAX_MOTH_HOMES = 5;

    /** Feeding multipliers */
    public static final int MULTIPLIER_REGULAR_HONEY = 2;
    public static final int MULTIPLIER_LOFTY_HONEY = 4;
    public static final int MULTIPLIER_PALE_OIL = 8;

    // ==================== Distribution Modes ====================

    public enum DistributionMode {
        /** Direct 1:1 transfer to single receiver */
        DIRECT,
        /** Fill receivers in order until full, then move to next */
        FILL_FIRST,
        /** Distribute equally across all receivers (round robin) */
        ROUND_ROBIN
    }

    // ==================== State ====================

    @Persisted
    @DescSynced
    @Getter
    @Setter
    private DistributionMode distributionMode = DistributionMode.FILL_FIRST;

    @Persisted
    @Getter
    private int mothHomeTier = 1; // 1-4

    @Persisted
    @Getter
    private int mothHomeCount = 0; // 0-5

    @Persisted
    private int ticksSinceLastCycle = 0;

    @Persisted
    private int roundRobinIndex = 0;

    @Persisted
    @DescSynced
    @Getter
    private int currentFeedingMultiplier = 1;

    private TickableSubscription shippingSubscription;

    // ==================== Constructor ====================

    public MothCargoStationMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }


    // ==================== Linking Overrides ====================

    @Override
    public LinkRole getLinkRole() {
        // Station is the CONTROLLER - it initiates transfers to Drop Offs
        return LinkRole.CONTROLLER;
    }

    @Override
    public int getMaxPartners() {
        // Can link to multiple drop-off points
        return 16;
    }

    @Override
    public boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine) {
        // Only link to Drop Off stations
        if (!(partnerMachine instanceof MothCargoDropOffMachine)) {
            return false;
        }

        // Same dimension only
        GlobalPos myPos = getGlobalPos();
        if (myPos == null) return false;

        return myPos.dimension().equals(partner.dimension());
    }

    // ==================== Lifecycle ====================

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        // Scan for moth homes in structure
        scanForMothHomes();

        subscribeToShipping();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        unsubscribeFromShipping();
        // Reset moth home stats
        mothHomeTier = 0;
        mothHomeCount = 0;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unsubscribeFromShipping();
    }

    /**
     * Scan the multiblock structure for Forestry beehive blocks (used as moth homes).
     * Sets mothHomeTier and mothHomeCount based on what's found.
     * All moth homes must be the same tier.
     *
     * Tier mapping:
     * T1: forestry:beehive_forest
     * T2: forestry:beehive_lush
     * T3: forestry:beehive_desert
     * T4: forestry:beehive_end
     */
    private void scanForMothHomes() {
        Level level = getLevel();
        if (level == null) {
            mothHomeTier = 0;
            mothHomeCount = 0;
            return;
        }

        BlockPos controllerPos = getBlockPos();
        int foundTier = 0;
        int foundCount = 0;
        boolean mixedTiers = false;

        // Scan a 7x7x7 region around the controller (covers 5x5x5 structure plus margin)
        int scanRadius = 3;
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                    net.minecraft.world.level.block.Block block = level.getBlockState(checkPos).getBlock();
                    ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

                    int tier = getBeehiveTier(blockId);
                    if (tier > 0) {
                        if (foundCount == 0) {
                            // First moth home found - set the tier
                            foundTier = tier;
                            foundCount = 1;
                        } else if (tier == foundTier) {
                            // Same tier - count it
                            foundCount++;
                        } else {
                            // Mixed tiers detected
                            mixedTiers = true;
                        }
                    }
                }
            }
        }

        // Enforce same-tier requirement
        if (mixedTiers) {
            CosmicCore.LOGGER.warn("Moth Cargo Station at {} has mixed tier moth homes - using lowest functionality",
                    controllerPos);
            // Still use what we found, but warn
        }

        // Cap at max moth homes
        foundCount = Math.min(foundCount, MAX_MOTH_HOMES);

        mothHomeTier = foundTier;
        mothHomeCount = foundCount;

        CosmicCore.LOGGER.debug("Moth Cargo Station at {} found {} T{} moth homes",
                controllerPos, mothHomeCount, mothHomeTier);
    }

    /**
     * Get the tier of a beehive block by its registry name.
     * 
     * @return tier (1-4) or 0 if not a valid beehive
     */
    private int getBeehiveTier(ResourceLocation blockId) {
        if (blockId.equals(MothCargoStation.BEEHIVE_FOREST)) return 1;
        if (blockId.equals(MothCargoStation.BEEHIVE_LUSH)) return 2;
        if (blockId.equals(MothCargoStation.BEEHIVE_DESERT)) return 3;
        if (blockId.equals(MothCargoStation.BEEHIVE_END)) return 4;
        return 0;
    }

    private void subscribeToShipping() {
        if (shippingSubscription == null) {
            shippingSubscription = subscribeServerTick(this::onShippingTick);
        }
    }

    private void unsubscribeFromShipping() {
        if (shippingSubscription != null) {
            shippingSubscription.unsubscribe();
            shippingSubscription = null;
        }
    }

    // ==================== Shipping Logic ====================

    private void onShippingTick() {
        if (!isFormed() || getLevel() == null || getLevel().isClientSide()) {
            return;
        }

        // Check if we have moths
        if (mothHomeCount <= 0) {
            return;
        }

        ticksSinceLastCycle++;

        int cycleTime = getCycleTimeTicks();
        if (ticksSinceLastCycle >= cycleTime) {
            ticksSinceLastCycle = 0;
            performShippingCycle();
        }
    }

    /**
     * Get the cycle time in ticks based on moth home tier.
     */
    public int getCycleTimeTicks() {
        int tierIndex = Math.max(0, Math.min(mothHomeTier - 1, CYCLE_TICKS_BY_TIER.length - 1));
        return CYCLE_TICKS_BY_TIER[tierIndex];
    }

    /**
     * Get total moths available for shipping.
     */
    public int getTotalMoths() {
        int tierIndex = Math.max(0, Math.min(mothHomeTier - 1, MOTHS_PER_HOME_BY_TIER.length - 1));
        return mothHomeCount * MOTHS_PER_HOME_BY_TIER[tierIndex];
    }

    /**
     * Get capacity per cycle (items or mB).
     */
    public int getCapacityPerCycle(boolean isFluid) {
        int baseCapacity = isFluid ? BASE_FLUID_PER_MOTH : BASE_ITEMS_PER_MOTH;
        return getTotalMoths() * baseCapacity * currentFeedingMultiplier;
    }

    /**
     * Perform one shipping cycle - transfer items/fluids to linked drop-offs.
     */
    private void performShippingCycle() {
        Set<GlobalPos> partners = getLinkedPartners();
        if (partners.isEmpty()) {
            return;
        }

        // Get list of valid, formed drop-off partners
        List<MothCargoDropOffMachine> dropOffs = getActiveDropOffs(partners);
        if (dropOffs.isEmpty()) {
            return;
        }

        // Calculate capacity for this cycle
        int itemCapacity = getCapacityPerCycle(false);
        int fluidCapacity = getCapacityPerCycle(true);

        // Ship items
        shipItems(dropOffs, itemCapacity);

        // Ship fluids
        shipFluids(dropOffs, fluidCapacity);

        // Consume feeding materials (TODO)
        consumeFeedingMaterials();
    }

    /**
     * Get active (formed and loaded) drop-off machines from partner list.
     */
    private List<MothCargoDropOffMachine> getActiveDropOffs(Set<GlobalPos> partners) {
        List<MothCargoDropOffMachine> result = new ArrayList<>();

        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return result;
        }

        for (GlobalPos partner : partners) {
            MetaMachine machine = LinkedMultiblockHelper.getMachine(serverLevel.getServer(), partner);
            if (machine instanceof MothCargoDropOffMachine dropOff && dropOff.isFormed()) {
                result.add(dropOff);
            }
        }

        return result;
    }

    /**
     * Ship items to drop-offs based on distribution mode.
     */
    private void shipItems(List<MothCargoDropOffMachine> dropOffs, int maxItems) {
        // Get our input items
        List<IItemHandler> inputHandlers = getItemInputHandlers();
        if (inputHandlers.isEmpty()) {
            return;
        }

        int remainingCapacity = maxItems;

        switch (distributionMode) {
            case DIRECT -> {
                // Ship to first drop-off only
                if (!dropOffs.isEmpty()) {
                    remainingCapacity = shipItemsToDropOff(dropOffs.get(0), inputHandlers, remainingCapacity);
                }
            }
            case FILL_FIRST -> {
                // Fill each drop-off in order until capacity exhausted
                for (MothCargoDropOffMachine dropOff : dropOffs) {
                    if (remainingCapacity <= 0) break;
                    remainingCapacity = shipItemsToDropOff(dropOff, inputHandlers, remainingCapacity);
                }
            }
            case ROUND_ROBIN -> {
                // Distribute items evenly starting from round robin index
                int perDropOff = Math.max(1, remainingCapacity / dropOffs.size());
                for (int i = 0; i < dropOffs.size() && remainingCapacity > 0; i++) {
                    int index = (roundRobinIndex + i) % dropOffs.size();
                    int toShip = Math.min(perDropOff, remainingCapacity);
                    int shipped = shipItemsToDropOff(dropOffs.get(index), inputHandlers, toShip);
                    remainingCapacity -= (toShip - shipped);
                }
                roundRobinIndex = (roundRobinIndex + 1) % dropOffs.size();
            }
        }
    }

    /**
     * Ship items to a single drop-off, returns remaining capacity.
     */
    private int shipItemsToDropOff(MothCargoDropOffMachine dropOff, List<IItemHandler> sources, int maxItems) {
        List<IItemHandler> destHandlers = dropOff.getItemOutputHandlers();
        if (destHandlers.isEmpty()) {
            return maxItems;
        }

        int remaining = maxItems;

        for (IItemHandler source : sources) {
            if (!(source instanceof NotifiableItemStackHandler sourceNotifiable)) {
                continue;
            }

            for (int slot = 0; slot < source.getSlots() && remaining > 0; slot++) {
                // Use internal extract to bypass IO check
                ItemStack stack = sourceNotifiable.extractItemInternal(slot, remaining, true); // Simulate
                if (stack.isEmpty()) continue;

                // Try to insert into destination using internal method
                ItemStack toInsert = stack.copy();
                int originalCount = toInsert.getCount();

                for (IItemHandler dest : destHandlers) {
                    if (dest instanceof NotifiableItemStackHandler destNotifiable) {
                        // Use insertItemInternal which bypasses the IO check
                        for (int destSlot = 0; destSlot < dest.getSlots() && !toInsert.isEmpty(); destSlot++) {
                            toInsert = destNotifiable.insertItemInternal(destSlot, toInsert, false);
                        }
                    } else {
                        // Fallback to standard insertion
                        for (int destSlot = 0; destSlot < dest.getSlots() && !toInsert.isEmpty(); destSlot++) {
                            toInsert = dest.insertItem(destSlot, toInsert, false);
                        }
                    }
                    if (toInsert.isEmpty()) break;
                }

                // Actually extract what we inserted
                int inserted = originalCount - toInsert.getCount();
                if (inserted > 0) {
                    sourceNotifiable.extractItemInternal(slot, inserted, false);
                    remaining -= inserted;
                }
            }
        }

        return remaining;
    }

    /**
     * Ship fluids to drop-offs based on distribution mode.
     */
    private void shipFluids(List<MothCargoDropOffMachine> dropOffs, int maxFluid) {
        List<IFluidHandler> inputHandlers = getFluidInputHandlers();
        if (inputHandlers.isEmpty()) {
            return;
        }

        int remainingCapacity = maxFluid;

        switch (distributionMode) {
            case DIRECT -> {
                if (!dropOffs.isEmpty()) {
                    remainingCapacity = shipFluidsToDropOff(dropOffs.get(0), inputHandlers, remainingCapacity);
                }
            }
            case FILL_FIRST -> {
                for (MothCargoDropOffMachine dropOff : dropOffs) {
                    if (remainingCapacity <= 0) break;
                    remainingCapacity = shipFluidsToDropOff(dropOff, inputHandlers, remainingCapacity);
                }
            }
            case ROUND_ROBIN -> {
                int perDropOff = Math.max(1, remainingCapacity / dropOffs.size());
                for (int i = 0; i < dropOffs.size() && remainingCapacity > 0; i++) {
                    int index = (roundRobinIndex + i) % dropOffs.size();
                    int toShip = Math.min(perDropOff, remainingCapacity);
                    int shipped = shipFluidsToDropOff(dropOffs.get(index), inputHandlers, toShip);
                    remainingCapacity -= (toShip - shipped);
                }
            }
        }
    }

    /**
     * Ship fluids to a single drop-off, returns remaining capacity.
     */
    private int shipFluidsToDropOff(MothCargoDropOffMachine dropOff, List<IFluidHandler> sources, int maxFluid) {
        List<IFluidHandler> destHandlers = dropOff.getFluidOutputHandlers();
        if (destHandlers.isEmpty()) {
            return maxFluid;
        }

        int remaining = maxFluid;

        for (IFluidHandler source : sources) {
            for (int tank = 0; tank < source.getTanks() && remaining > 0; tank++) {
                FluidStack available = source.getFluidInTank(tank);
                if (available.isEmpty()) continue;

                int toDrain = Math.min(available.getAmount(), remaining);
                FluidStack drained = source.drain(available.copyWithAmount(toDrain),
                        IFluidHandler.FluidAction.SIMULATE);
                if (drained.isEmpty()) continue;

                // Try to insert into destination
                int filled = 0;
                for (IFluidHandler dest : destHandlers) {
                    int thisFill = dest.fill(drained.copy(), IFluidHandler.FluidAction.EXECUTE);
                    filled += thisFill;
                    drained.shrink(thisFill);
                    if (drained.isEmpty()) break;
                }

                // Actually drain what we inserted
                if (filled > 0) {
                    source.drain(available.copyWithAmount(filled), IFluidHandler.FluidAction.EXECUTE);
                    remaining -= filled;
                }
            }
        }

        return remaining;
    }

    /**
     * Consume feeding materials and update multiplier.
     */
    private void consumeFeedingMaterials() {
        // TODO: Check input bus for honey/oil and consume per cycle
        // For now, default multiplier
        currentFeedingMultiplier = 1;
    }

    // ==================== Handler Access ====================

    /**
     * Get all item input handlers from the multiblock.
     */
    private List<IItemHandler> getItemInputHandlers() {
        List<IItemHandler> handlers = new ArrayList<>();

        var itemCaps = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        if (itemCaps != null) {
            for (var handler : itemCaps) {
                if (handler instanceof NotifiableItemStackHandler itemHandler) {
                    handlers.add(itemHandler);
                }
            }
        }

        return handlers;
    }

    /**
     * Get all fluid input handlers from the multiblock.
     */
    private List<IFluidHandler> getFluidInputHandlers() {
        List<IFluidHandler> handlers = new ArrayList<>();

        var fluidCaps = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        if (fluidCaps != null) {
            for (var handler : fluidCaps) {
                if (handler instanceof NotifiableFluidTank fluidHandler) {
                    handlers.add(fluidHandler);
                }
            }
        }

        return handlers;
    }

    // ==================== UI ====================

    @Override
    public void addDisplayText(List<Component> textList) {
        if (!isFormed()) {
            textList.add(Component.literal("Structure not formed")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            return;
        }

        // Moth home info
        if (mothHomeCount > 0) {
            textList.add(Component.literal("Moth Homes: " + mothHomeCount + " (T" + mothHomeTier + ")")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            textList.add(Component.literal("Total Moths: " + getTotalMoths())
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
            textList.add(Component.literal("Cycle Time: " + (getCycleTimeTicks() / 20) + "s")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
        } else {
            textList.add(Component.literal("No Moth Homes installed!")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }

        // Distribution mode
        textList.add(Component.literal("Mode: " + distributionMode.name())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

        // Linked partners
        int linkedCount = getLinkedPartners().size();
        if (linkedCount > 0) {
            textList.add(Component.literal("Linked Drop-Offs: " + linkedCount)
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        } else {
            textList.add(Component.literal("No Drop-Offs linked!")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }

        // Capacity info
        if (mothHomeCount > 0) {
            textList.add(Component
                    .literal("Capacity: " + getCapacityPerCycle(false) + " items / " + getCapacityPerCycle(true) +
                            " mB per cycle")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        }
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            // Cycle through distribution modes
            DistributionMode[] modes = DistributionMode.values();
            int nextIndex = (distributionMode.ordinal() + 1) % modes.length;
            distributionMode = modes[nextIndex];

            context.getPlayer().displayClientMessage(
                    Component.literal("Distribution Mode: " + distributionMode.name())
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)),
                    true);
        }
        return InteractionResult.SUCCESS;
    }
}
