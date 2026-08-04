package com.ghostipedia.cosmiccore.common.firmament;

import com.ghostipedia.cosmiccore.api.gravity.GravityApi;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import com.spacegravity.spacegravity.ZeroGravityPushData;
import com.spacegravity.spacegravity.ZeroGravityPushHelper;
import com.spacegravity.spacegravity.api.SpaceGravityApi;

public final class FirmamentSpaceGravityCompat {

    private static final String MOD_ID = "space_gravity";
    private static final String PLAYER_PERSISTED = "PlayerPersisted";
    private static final String ROOT_TAG = "space_gravity";
    private static final String ZERO_GRAVITY_ENABLED_TAG = "zeroGravityEnabled";
    private static final String RUNTIME_DISABLED_TAG = "spaceEngineRuntimeDisabled";
    private static boolean providerRegistered;

    private FirmamentSpaceGravityCompat() {}

    public static boolean isAvailable() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void registerPushSurfaceProvider() {
        if (!isAvailable() || providerRegistered) return;
        Loaded.registerPushSurfaceProvider();
        providerRegistered = true;
    }

    public static boolean isEnabled(ServerPlayer player) {
        return isAvailable() && SpaceGravityApi.isZeroGravityEnabled(player);
    }

    public static void setRuntimeEnabled(ServerPlayer player, boolean enabled) {
        if (!isAvailable()) return;
        clearAuthorityState(player);
        SpaceGravityApi.setSpaceEngineRuntimeEnabled(player, enabled);
    }

    public static void clearAuthorityState(ServerPlayer player) {
        if (!isAvailable()) return;
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(PLAYER_PERSISTED, Tag.TAG_COMPOUND)) return;
        CompoundTag playerPersisted = persistentData.getCompound(PLAYER_PERSISTED);
        if (!playerPersisted.contains(ROOT_TAG, Tag.TAG_COMPOUND)) return;
        CompoundTag state = playerPersisted.getCompound(ROOT_TAG);
        state.remove(ZERO_GRAVITY_ENABLED_TAG);
        state.remove(RUNTIME_DISABLED_TAG);
        playerPersisted.put(ROOT_TAG, state);
        persistentData.put(PLAYER_PERSISTED, playerPersisted);
    }

    private static final class Loaded {

        private static final ZeroGravityPushHelper.PushSurface UNAVAILABLE = new ZeroGravityPushHelper.PushSurface(
                false, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, ZeroGravityPushData.ContactLimb.NONE);

        private Loaded() {}

        private static void registerPushSurfaceProvider() {
            SpaceGravityApi.registerPushSurfaceProvider((player, orientation, bounds) -> {
                if (!player.level().dimension().equals(FirmamentDimension.KEY) ||
                        !GravityApi.isFreeDrift(player)) {
                    return UNAVAILABLE;
                }
                Vec3 anchor = player.getBoundingBox().getCenter();
                Vec3 contact = anchor.add(32.0, 0.0, 0.0);
                return new ZeroGravityPushHelper.PushSurface(
                        true, orientation.up(), contact, anchor, ZeroGravityPushData.ContactLimb.NONE);
            });
        }
    }
}
