package com.ghostipedia.cosmiccore.common.machine.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

public class SensorPartMachine extends TieredPartMachine {

    private final ConditionalSubscriptionHandler signalUpdateHandler;

    public SensorPartMachine(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier);
        this.signalUpdateHandler = new ConditionalSubscriptionHandler(this, this::updateSignal, () -> true);
    }

    @Override
    public boolean canConnectRedstone(@NotNull Direction side) {
        return side == getFrontFacing();
    }

    @Override
    public void removedFromController(@NotNull MultiblockControllerMachine controller) {
        super.removedFromController(controller);
        signalUpdateHandler.updateSubscription();
    }

    @Override
    public void addedToController(@NotNull MultiblockControllerMachine controller) {
        super.addedToController(controller);
        signalUpdateHandler.updateSubscription();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    /**
     * Called to update the redstone signal output. Subclasses should override this.
     */
    protected void updateSignal() {
        // Subclasses implement actual signal logic
    }
}
