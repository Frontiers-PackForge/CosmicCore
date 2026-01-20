package com.ghostipedia.cosmiccore.api.capability;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// Capability for storing teleportation origin of a player.
public interface ITeleportOrigin {

    void setOriginDimension(ResourceKey<Level> dimension); // Set the origin dimension the player teleported from.

    ResourceKey<Level> getOriginDimension(); // Get the origin dimension, or null if not set.

    void setOriginPosition(Vec3 position); // Set the origin position the player teleported from.

    Vec3 getOriginPosition(); // Get the origin position, or null if not set.

    void setOriginRotation(float yaw, float pitch); // Set the player's rotation when they teleported.

    float getOriginYaw(); // Get the origin yaw rotation.

    float getOriginPitch(); // Get the origin pitch rotation.

    boolean hasValidOrigin(); // Check if this player has valid origin data.

    void clearOriginData(); // Clear all origin data.
}
