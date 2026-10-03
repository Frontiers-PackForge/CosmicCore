package com.ghostipedia.cosmiccore.common.machine.vitae;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.world.phys.Vec3;

public final class VitaeRenderAnchors {

    private VitaeRenderAnchors() {}

    public static Vec3 resolve(MultiblockControllerMachine machine, double back, double up, double left) {
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        boolean flipped = machine.isFlipped();
        var b = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        var u = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);
        var l = RelativeDirection.LEFT.getRelativeFacing(front, upwards, flipped);
        return Vec3.atCenterOf(machine.getBlockPos()).add(
                back * b.getStepX() + up * u.getStepX() + left * l.getStepX(),
                back * b.getStepY() + up * u.getStepY() + left * l.getStepY(),
                back * b.getStepZ() + up * u.getStepZ() + left * l.getStepZ());
    }

    public static Vec3 transfuserCube(MultiblockControllerMachine machine) {
        return resolve(machine, 3, 3.5, 0);
    }

    public static Vec3 pylonFocus(MultiblockControllerMachine machine) {
        return resolve(machine, 2, 5, 0);
    }

    public static Vec3 pylonPool(MultiblockControllerMachine machine) {
        return resolve(machine, 2, 1.42, 0);
    }
}
