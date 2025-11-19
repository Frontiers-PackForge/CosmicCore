package com.ghostipedia.cosmiccore.common.teleporter;

import com.ghostipedia.cosmiccore.api.capability.ITeleportOrigin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

// Implementation of the teleport origin capability - Stores origin dimension, position, and rotation for teleport return trips.
public class TeleportOrigin implements ITeleportOrigin {

    @Nullable
    private ResourceKey<Level> originDimension;
    @Nullable
    private Vec3 originPosition;
    private float originYaw;
    private float originPitch;
    @Nullable
    private BlockPos escapePadPosition;

    // spotless: off
    @Override
    public void setOriginDimension(ResourceKey<Level> dimension) {
        this.originDimension = dimension;
    }

    @Override
    @Nullable
    public ResourceKey<Level> getOriginDimension() {
        return originDimension;
    }

    @Override
    public void setOriginPosition(Vec3 position) {
        this.originPosition = position;
    }

    @Override
    @Nullable
    public Vec3 getOriginPosition() {
        return originPosition;
    }

    @Override
    public void setOriginRotation(float yaw, float pitch) {
        this.originYaw = yaw;
        this.originPitch = pitch;
    }

    @Override
    public float getOriginYaw() {
        return originYaw;
    }

    @Override
    public float getOriginPitch() {
        return originPitch;
    }

    @Override
    public boolean hasValidOrigin() {
        return originDimension != null && originPosition != null;
    }

    @Override
    public void clearOriginData() {
        originDimension = null;
        originPosition = null;
        originYaw = 0;
        originPitch = 0;
        escapePadPosition = null;
    }
    // spotless: on

    // Save capability data to NBT.
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        if (originDimension != null) {
            tag.putString("OriginDimension", originDimension.location().toString());
        }

        if (originPosition != null) {
            tag.putDouble("OriginX", originPosition.x);
            tag.putDouble("OriginY", originPosition.y);
            tag.putDouble("OriginZ", originPosition.z);
        }

        tag.putFloat("OriginYaw", originYaw);
        tag.putFloat("OriginPitch", originPitch);

        if (escapePadPosition != null) {
            tag.putLong("EscapePadPos", escapePadPosition.asLong());
        }

        return tag;
    }

    // Load capability data from NBT.
    public void load(CompoundTag tag) {
        if (tag.contains("OriginDimension")) {
            ResourceLocation dimLoc = new ResourceLocation(tag.getString("OriginDimension"));
            this.originDimension = ResourceKey.create(Registries.DIMENSION, dimLoc);
        } else {
            this.originDimension = null;
        }

        if (tag.contains("OriginX")) {
            double x = tag.getDouble("OriginX");
            double y = tag.getDouble("OriginY");
            double z = tag.getDouble("OriginZ");
            this.originPosition = new Vec3(x, y, z);
        } else {
            this.originPosition = null;
        }

        this.originYaw = tag.getFloat("OriginYaw");
        this.originPitch = tag.getFloat("OriginPitch");

        if (tag.contains("EscapePadPos")) {
            this.escapePadPosition = BlockPos.of(tag.getLong("EscapePadPos"));
        } else {
            this.escapePadPosition = null;
        }
    }
}
