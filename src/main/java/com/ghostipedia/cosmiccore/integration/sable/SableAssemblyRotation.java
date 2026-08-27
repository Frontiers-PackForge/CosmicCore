package com.ghostipedia.cosmiccore.integration.sable;

import com.ghostipedia.cosmiccore.mixin.sable.MachineCoverContainerSlotAccessor;
import com.ghostipedia.cosmiccore.mixin.sable.PipeCoverContainerSlotAccessor;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.pipenet.PipeCoverContainer;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;

import org.jetbrains.annotations.Nullable;

public final class SableAssemblyRotation {

    private SableAssemblyRotation() {}

    public static void rotatePipe(PipeBlockEntity<?, ?> pipe, Rotation rotation, HolderLookup.Provider lookup) {
        if (rotation == Rotation.NONE) {
            return;
        }
        rotateCovers(pipe.getCoverContainer(), rotation, lookup);
        pipe.setConnections(rotateMask(pipe.getConnections(), rotation));
        pipe.setBlockedConnections(rotateMask(pipe.getBlockedConnections(), rotation));
    }

    public static void rotateMachine(MachineCoverContainer covers, Rotation rotation, HolderLookup.Provider lookup) {
        if (rotation == Rotation.NONE) {
            return;
        }
        rotateCovers(covers, rotation, lookup);
    }

    private static int rotateMask(int mask, Rotation rotation) {
        int rotated = 0;
        for (Direction side : GTUtil.DIRECTIONS) {
            if ((mask & (1 << side.ordinal())) != 0) {
                rotated |= 1 << rotation.rotate(side).ordinal();
            }
        }
        return rotated;
    }

    private static void rotateCovers(ICoverable holder, Rotation rotation, HolderLookup.Provider lookup) {
        CoverBehavior[] snapshot = new CoverBehavior[GTUtil.DIRECTIONS.length];
        boolean any = false;
        for (Direction side : GTUtil.DIRECTIONS) {
            CoverBehavior cover = holder.getCoverAtSide(side);
            if (cover != null) {
                snapshot[side.ordinal()] = cover;
                any = true;
            }
        }
        if (!any) {
            return;
        }
        CoverBehavior[] rebuilt = new CoverBehavior[GTUtil.DIRECTIONS.length];
        Direction[] newSides = new Direction[GTUtil.DIRECTIONS.length];
        for (Direction oldSide : GTUtil.DIRECTIONS) {
            CoverBehavior old = snapshot[oldSide.ordinal()];
            if (old == null) {
                continue;
            }
            Direction newSide = rotation.rotate(oldSide);
            CoverBehavior moved = old.coverDefinition.createCoverBehavior(holder, newSide);
            if (moved == null) {
                return;
            }
            CompoundTag data = old.getSyncDataHolder().serializeNBT(lookup);
            moved.getSyncDataHolder().deserializeNBT(lookup, data);
            rebuilt[oldSide.ordinal()] = moved;
            newSides[oldSide.ordinal()] = newSide;
        }
        for (Direction side : GTUtil.DIRECTIONS) {
            setSlot(holder, side, null);
        }
        for (int i = 0; i < GTUtil.DIRECTIONS.length; i++) {
            if (rebuilt[i] != null) {
                setSlot(holder, newSides[i], rebuilt[i]);
            }
        }
    }

    private static void setSlot(ICoverable holder, Direction side, @Nullable CoverBehavior cover) {
        if (holder instanceof PipeCoverContainer pipeCovers) {
            PipeCoverContainerSlotAccessor accessor = (PipeCoverContainerSlotAccessor) pipeCovers;
            switch (side) {
                case UP -> accessor.cosmiccore$setUp(cover);
                case DOWN -> accessor.cosmiccore$setDown(cover);
                case NORTH -> accessor.cosmiccore$setNorth(cover);
                case SOUTH -> accessor.cosmiccore$setSouth(cover);
                case WEST -> accessor.cosmiccore$setWest(cover);
                case EAST -> accessor.cosmiccore$setEast(cover);
            }
        } else if (holder instanceof MachineCoverContainer machineCovers) {
            MachineCoverContainerSlotAccessor accessor = (MachineCoverContainerSlotAccessor) machineCovers;
            switch (side) {
                case UP -> accessor.cosmiccore$setUp(cover);
                case DOWN -> accessor.cosmiccore$setDown(cover);
                case NORTH -> accessor.cosmiccore$setNorth(cover);
                case SOUTH -> accessor.cosmiccore$setSouth(cover);
                case WEST -> accessor.cosmiccore$setWest(cover);
                case EAST -> accessor.cosmiccore$setEast(cover);
            }
        }
    }
}
