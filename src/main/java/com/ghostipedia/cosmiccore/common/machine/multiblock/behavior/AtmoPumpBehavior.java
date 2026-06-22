package com.ghostipedia.cosmiccore.common.machine.multiblock.behavior;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;

public class AtmoPumpBehavior extends WorkableElectricMultiblockMachine {

    @NotNull
    private AABB killzone = new AABB(BlockPos.ZERO);
    private TickableSubscription hurtSub;
    private int Damage = 1;

    public AtmoPumpBehavior(BlockEntityCreationInfo holder) {
        super(holder);
    }

    private void updateBounds(int multiplier) {
        var flt = RelativeDirection.offsetPos(getBlockPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 3, 14,
                -14);
        var brb = RelativeDirection.offsetPos(getBlockPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), -14,
                -14,
                14);
        killzone = AABB.encapsulatingFullBlocks(flt, brb);
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        unsubscribe(hurtSub);
        hurtSub = null;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        unsubscribe(hurtSub);
        hurtSub = null;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        hurtSub = subscribeServerTick(this::suffocatePlayer);
        updateBounds(1);
    }

    private void suffocatePlayer() {
        if (getOffsetTimer() % 35 != 0) return;
        if (!this.isActive()) return;
        if (isRemote() || getLevel() == null) return;
        for (Entity entity : getLevel().getEntities(null, killzone)) {
            if (entity instanceof Player player) {
                player.hurt(player.level().damageSources().inWall(), this.Damage);
            }
        }
    }
}
