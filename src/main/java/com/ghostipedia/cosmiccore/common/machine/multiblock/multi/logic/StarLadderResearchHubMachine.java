package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;
import com.ghostipedia.cosmiccore.client.gui.widget.starladder.StarLadderFancyUIWidget;
import com.ghostipedia.cosmiccore.client.gui.widget.starladder.StarLadderResearchHubWidget;
import com.ghostipedia.cosmiccore.client.renderer.RingUpgradePreviewRenderer;
import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.StarLadderResearchHub;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StarLadderResearchHubMachine extends LinkedWorkableElectricMultiblockMachine {


    private static final ResourceKey<Level> REQUIRED_DIMENSION = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("ad_astra", "earth_orbit"));

    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onRingTierSynced")
    private int ringTier = 0;

    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onRingPreviewSynced")
    private boolean ringPreviewEnabled = false;

    @Persisted
    @DescSynced
    private int partialRingIndex = 0;

    public StarLadderResearchHubMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }

    public int getRingTier() {
        return ringTier;
    }

    public boolean isRingPreviewEnabled() {
        return ringPreviewEnabled;
    }

    public int getPartialRingIndex() {
        return partialRingIndex;
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    protected void onRingPreviewSynced(boolean newValue, boolean oldValue) {
        if (newValue) {
            RingUpgradePreviewRenderer.enablePreview(getBlockPos(), getFrontFacing(), ringTier);
        } else {
            RingUpgradePreviewRenderer.disablePreview(getBlockPos());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    protected void onRingTierSynced(int newValue, int oldValue) {
        if (ringPreviewEnabled) {
            RingUpgradePreviewRenderer.updatePreview(getBlockPos(), getFrontFacing(), newValue);
        }
    }

    public void toggleRingPreview() {
        // ringTier is 0-3, max tier is 3
        if (!isFormed() || ringTier >= 3) {
            ringPreviewEnabled = false;
            return;
        }
        ringPreviewEnabled = !ringPreviewEnabled;
    }

    public boolean canUpgrade() {
        // Can upgrade from 0 (to build T1) up through 2 (to build T3)
        return isFormed() && ringTier < 3;
    }

    public net.minecraft.world.level.block.Block getNextRingBlock() {
        if (!canUpgrade()) return null;
        return RingUpgradePreviewRenderer.getRingBlock(ringTier + 1);
    }

    public int getNextRingBlockCount() {
        if (!canUpgrade()) return 0;
        return RingUpgradePreviewRenderer.getRingBlockCount(ringTier + 1);
    }

    public Set<BlockPos> getNextRingPositions() {
        if (!canUpgrade()) return Set.of();
        return RingUpgradePreviewRenderer.calculateRingPositions(getBlockPos(), getFrontFacing(), ringTier + 1);
    }

    public Map<BlockPos, net.minecraft.world.level.block.Block> getNextRingPositionsWithBlocks() {
        if (!canUpgrade()) return Map.of();
        return RingUpgradePreviewRenderer.calculateRingPositionsWithBlocks(getBlockPos(), getFrontFacing(), ringTier + 1);
    }

    public int autoBuildNextRing(Player player) {
        if (!canUpgrade() || getLevel() == null) return 0;

        Map<BlockPos, net.minecraft.world.level.block.Block> positionsWithBlocks = getNextRingPositionsWithBlocks();
        if (positionsWithBlocks.isEmpty()) return 0;

        boolean isCreative = player.isCreative();
        int placed = 0;

        for (Map.Entry<BlockPos, net.minecraft.world.level.block.Block> entry : positionsWithBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            net.minecraft.world.level.block.Block targetBlock = entry.getValue();

            if (!getLevel().isEmptyBlock(pos)) continue;

            if (!isCreative) {
                // Find and consume a matching block from player inventory
                ItemStack consumed = consumeBlockFromInventory(player, targetBlock);
                if (consumed.isEmpty()) continue; // Try next position, might have different block type
            }

            // Place the block
            getLevel().setBlock(pos, targetBlock.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);

            // Play place sound
            SoundType soundType = targetBlock.defaultBlockState().getSoundType();
            getLevel().playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

            placed++;
        }

        return placed;
    }

    private ItemStack consumeBlockFromInventory(Player player, net.minecraft.world.level.block.Block targetBlock) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() == targetBlock) {
                    ItemStack consumed = stack.split(1);
                    if (stack.isEmpty()) {
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                    return consumed;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public int countBlocksInInventory(Player player, net.minecraft.world.level.block.Block targetBlock) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() == targetBlock) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    public int countEmptyRingPositions() {
        if (!canUpgrade() || getLevel() == null) return 0;

        Map<BlockPos, net.minecraft.world.level.block.Block> positions = getNextRingPositionsWithBlocks();
        int empty = 0;
        for (BlockPos pos : positions.keySet()) {
            if (getLevel().isEmptyBlock(pos)) {
                empty++;
            }
        }
        return empty;
    }

    public Map<net.minecraft.world.level.block.Block, Integer> countEmptyRingPositionsByBlock() {
        if (!canUpgrade() || getLevel() == null) return Map.of();

        Map<BlockPos, net.minecraft.world.level.block.Block> positions = getNextRingPositionsWithBlocks();
        Map<net.minecraft.world.level.block.Block, Integer> counts = new java.util.HashMap<>();
        for (Map.Entry<BlockPos, net.minecraft.world.level.block.Block> entry : positions.entrySet()) {
            if (getLevel().isEmptyBlock(entry.getKey())) {
                counts.merge(entry.getValue(), 1, Integer::sum);
            }
        }
        return counts;
    }

    public Map<net.minecraft.world.level.block.Block, Integer> getNextRingBlockCounts() {
        if (!canUpgrade()) return Map.of();
        return RingUpgradePreviewRenderer.getDeltaBlockCounts(ringTier);
    }


    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        // Use the tier from the pattern that matched during checkPattern()
        this.ringTier = Math.max(0, lastMatchedTier);
        this.partialRingIndex = 0; // No partial rings with strict pattern matching

        // Disable preview if max tier reached (T3 is max)
        if (ringPreviewEnabled && ringTier >= 3) {
            ringPreviewEnabled = false;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.ringTier = 0;
        this.partialRingIndex = 0;
        this.ringPreviewEnabled = false;
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            Player playerIn = context.getPlayer();
            if (playerIn.isShiftKeyDown()) {
                // Shift+Screwdriver = Auto-build
                return handleAutoBuild(playerIn);
            } else {
                // Normal Screwdriver = Toggle preview
                return handlePreviewToggle(playerIn);
            }
        }
        return super.onScrewdriverClick(context);
    }

    @Override
    protected InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        Player playerIn = context.getPlayer();
        if (!isRemote()) {
            if (playerIn.isShiftKeyDown()) {
                return handleDebugBuildT0(playerIn);
            }
        }
        return super.onSoftMalletClick(context);
    }

    private InteractionResult handlePreviewToggle(Player player) {
        if (canUpgrade()) {
            toggleRingPreview();

            if (ringPreviewEnabled) {
                int totalCount = getNextRingBlockCount();
                player.displayClientMessage(
                        Component.literal("Ring Preview: ON")
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(" (" + totalCount + " blocks needed)")
                                        .withStyle(ChatFormatting.GRAY)),
                        true);
            } else {
                player.displayClientMessage(
                        Component.literal("Ring Preview: OFF").withStyle(ChatFormatting.RED),
                        true);
            }
            return InteractionResult.SUCCESS;
        } else if (ringTier >= 3) {
            player.displayClientMessage(
                    Component.literal("Maximum tier reached!").withStyle(ChatFormatting.GOLD),
                    true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private InteractionResult handleAutoBuild(Player player) {
        if (!canUpgrade()) {
            if (ringTier >= 3) {
                player.displayClientMessage(
                        Component.literal("Maximum tier reached!").withStyle(ChatFormatting.GOLD),
                        true);
            }
            return InteractionResult.SUCCESS;
        }

        int needed = countEmptyRingPositions();

        if (needed == 0) {
            player.displayClientMessage(
                    Component.literal("Ring already complete! Break and rebuild structure to upgrade tier.")
                            .withStyle(ChatFormatting.YELLOW),
                    true);
            return InteractionResult.SUCCESS;
        }

        // Check if player has any of the required blocks (skip for creative)
        if (!player.isCreative()) {
            Map<net.minecraft.world.level.block.Block, Integer> neededByBlock = countEmptyRingPositionsByBlock();
            boolean hasAnyBlocks = false;
            for (net.minecraft.world.level.block.Block block : neededByBlock.keySet()) {
                if (countBlocksInInventory(player, block) > 0) {
                    hasAnyBlocks = true;
                    break;
                }
            }

            if (!hasAnyBlocks) {
                player.displayClientMessage(
                        Component.literal("Missing blocks for next tier!").withStyle(ChatFormatting.RED),
                        true);
                return InteractionResult.SUCCESS;
            }
        }

        int placed = autoBuildNextRing(player);
        int remaining = countEmptyRingPositions();

        if (remaining == 0) {
            player.displayClientMessage(
                    Component.literal("Ring complete! ").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal("Placed " + placed + " blocks.")
                                    .withStyle(ChatFormatting.WHITE)),
                    true);
            // Play a completion sound
            getLevel().playSound(null, getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.0F);

            // Force structure re-evaluation by invalidating first, then rechecking
            // This ensures onStructureFormed() is called again with the new tier
            onStructureInvalid();
            getMultiblockState().setError(null);
            if (checkPattern()) {
                onStructureFormed();
            }
        } else {
            player.displayClientMessage(
                    Component.literal("Placed " + placed + " blocks. ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(remaining + " positions remaining.")
                                    .withStyle(ChatFormatting.GRAY)),
                    true);
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Auto-build T0 structure for easier testing.
     * Only works in creative mode. Use Shift+Soft Mallet to trigger.
     */
    private InteractionResult handleDebugBuildT0(Player player) {
        if (!player.isCreative()) {
            player.displayClientMessage(
                    Component.literal("Creative mode only!").withStyle(ChatFormatting.RED),
                    true);
            return InteractionResult.SUCCESS;
        }

        if (getLevel() == null) return InteractionResult.PASS;

        Map<BlockPos, net.minecraft.world.level.block.Block> t0Positions = RingUpgradePreviewRenderer.calculateRingPositionsWithBlocks(
                getBlockPos(), getFrontFacing(), 0);

        if (t0Positions.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("No T0 positions found!").withStyle(ChatFormatting.RED),
                    true);
            return InteractionResult.SUCCESS;
        }

        int placed = 0;
        int skipped = 0;
        for (Map.Entry<BlockPos, net.minecraft.world.level.block.Block> entry : t0Positions.entrySet()) {
            BlockPos pos = entry.getKey();
            net.minecraft.world.level.block.Block targetBlock = entry.getValue();

            if (!getLevel().isEmptyBlock(pos)) {
                skipped++;
                continue;
            }

            getLevel().setBlock(pos, targetBlock.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);

            SoundType soundType = targetBlock.defaultBlockState().getSoundType();
            getLevel().playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

            placed++;
        }

        player.displayClientMessage(
                Component.literal("Built T0: ").withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(placed + " blocks placed, " + skipped + " skipped")
                                .withStyle(ChatFormatting.WHITE)),
                true);

        getLevel().playSound(null, getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.2F);

        // Force structure re-evaluation
        onStructureInvalid();
        getMultiblockState().setError(null);
        if (checkPattern()) {
            onStructureFormed();
        }

        return InteractionResult.SUCCESS;
    }

    // Cache of tier patterns for checkPattern - built lazily
    private static BlockPattern[] tierPatterns;

    // Tracks which tier pattern matched during the last successful checkPattern()
    // This is used by onStructureFormed() to set the correct tier
    private int lastMatchedTier = -1;

    private static BlockPattern[] getTierPatterns() {
        if (tierPatterns == null) {
            tierPatterns = new BlockPattern[] {
                    StarLadderResearchHub.buildT0Pattern(),
                    StarLadderResearchHub.buildT1Pattern(),
                    StarLadderResearchHub.buildT2Pattern(),
                    StarLadderResearchHub.buildT3Pattern()
            };
        }
        return tierPatterns;
    }

    @Override
    public boolean checkPattern() {
        // TODO: Re-enable dimension check after testing
        // Check dimension requirement
        // if (getLevel() != null && !getLevel().dimension().equals(REQUIRED_DIMENSION)) {
        // return false;
        // }

        // Try each tier pattern from T3 to T0 (highest first)
        // The highest tier that matches determines the structure's tier
        var state = getMultiblockState();

        for (int tier = 3; tier >= 0; tier--) {
            BlockPattern pattern = getTierPatterns()[tier];
            if (pattern.checkPatternAt(state, false)) {
                lastMatchedTier = tier;
                return true;
            }
            state.setError(null);
        }

        lastMatchedTier = -1;
        return false;
    }

    @Override
    public LinkRole getLinkRole() {
        return LinkRole.REMOTE;
    }

    @Override
    public int getMaxPartners() {
        return 1;
    }

    @Override
    public boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine) {
        return partnerMachine instanceof StarLadderMachine;
    }

    public ILinkedMultiblock getLinkedPartnerMachine(GlobalPos partner) {
        return getPartnerMachine(partner);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;

        // Display tier (0-3)
        textList.add(Component.literal("Tier: T" + ringTier)
                .withStyle(ChatFormatting.AQUA));

        // Display upgrade info if not at max tier
        if (canUpgrade()) {
            // Show what's needed for next tier
            Map<net.minecraft.world.level.block.Block, Integer> neededByBlock = countEmptyRingPositionsByBlock();
            if (!neededByBlock.isEmpty()) {
                textList.add(Component.literal("Next Tier (T" + (ringTier + 1) + ") Needs:")
                        .withStyle(ChatFormatting.GRAY));
                for (Map.Entry<net.minecraft.world.level.block.Block, Integer> entry : neededByBlock.entrySet()) {
                    textList.add(Component.literal("  " + entry.getValue() + "x ")
                            .withStyle(ChatFormatting.WHITE)
                            .append(entry.getKey().getName().copy().withStyle(ChatFormatting.YELLOW)));
                }
            }

            if (ringPreviewEnabled) {
                textList.add(Component.literal("  [Preview: ON]")
                        .withStyle(ChatFormatting.GREEN));
            } else {
                textList.add(Component.literal("  [Screwdriver: preview | Shift: build]")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else if (ringTier >= 3) {
            textList.add(Component.literal("Maximum tier reached!")
                    .withStyle(ChatFormatting.GOLD));
        }

        GlobalPos ladder = getLinkedPartners().stream().findFirst().orElse(null);
        if (ladder == null) {
            textList.add(Component.literal("Not linked to Star Ladder").withStyle(ChatFormatting.GRAY));
            return;
        }

        boolean online = getPartnerMachine(ladder) != null;
        textList.add(Component.literal("Star Ladder: " + (online ? "Online" : "Offline"))
                .withStyle(online ? ChatFormatting.GREEN : ChatFormatting.RED));
        textList.add(Component.literal("  " + LinkedMultiblockHelper.getDimensionName(ladder.dimension().location()))
                .withStyle(ChatFormatting.GRAY));
        textList.add(Component.literal("  [%d, %d, %d]".formatted(
                ladder.pos().getX(), ladder.pos().getY(), ladder.pos().getZ()))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Widget createUIWidget() {
        return new StarLadderResearchHubWidget(() -> this);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(StarLadderResearchHubWidget.WIDTH + 16, StarLadderResearchHubWidget.HEIGHT + 70, this,
                entityPlayer)
                .widget(new StarLadderFancyUIWidget(this, StarLadderResearchHubWidget.WIDTH + 16,
                        StarLadderResearchHubWidget.HEIGHT + 70, this::getRingTier));
    }
}
