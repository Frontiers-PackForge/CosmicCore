package com.ghostipedia.cosmiccore.common.machine.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record FoundryRenderAnchor(double right, double up, double forward) {

    public static final FoundryRenderAnchor CENTER = new FoundryRenderAnchor(0.0, 0.0, 0.0);

    public Vec3 resolve(BlockPos controller, Direction front) {
        Direction rightDirection = front.getClockWise();
        return new Vec3(
                controller.getX() + 0.5 + rightDirection.getStepX() * right + front.getStepX() * forward,
                controller.getY() + 0.5 + up,
                controller.getZ() + 0.5 + rightDirection.getStepZ() * right + front.getStepZ() * forward);
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("right", right);
        tag.putDouble("up", up);
        tag.putDouble("forward", forward);
        return tag;
    }

    static FoundryRenderAnchor load(CompoundTag tag) {
        return new FoundryRenderAnchor(tag.getDouble("right"), tag.getDouble("up"), tag.getDouble("forward"));
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(right);
        buffer.writeDouble(up);
        buffer.writeDouble(forward);
    }

    static FoundryRenderAnchor read(FriendlyByteBuf buffer) {
        return new FoundryRenderAnchor(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
}
