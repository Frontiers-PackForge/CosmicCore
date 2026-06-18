package com.ghostipedia.cosmiccore.common.teleporter;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.ITeleportOrigin;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Capability provider for teleport origin data.
// Attaches to all players to track their teleportation origin for return trips.
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TeleportOriginCap {

    public static final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath("cosmiccore", "teleport_origin");
    public static final Capability<ITeleportOrigin> CAP = CapabilityManager.get(new CapabilityToken<>() {});

    public static class Provider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

        private final TeleportOrigin impl = new TeleportOrigin();
        private final LazyOptional<ITeleportOrigin> lazyOpt = LazyOptional.of(() -> impl);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                          @Nullable Direction direction) {
            return capability == CAP ? lazyOpt.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = impl.save();
            return tag != null ? tag : new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag != null) {
                impl.load(tag);
            }
        }
    }

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(ITeleportOrigin.class);
    }

    // Attach capability to all players.
    @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(KEY, new Provider());
        }
    }

    // Clone capability data on player respawn/dimension change.
    @SubscribeEvent
    public static void cloneCap(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(CAP).ifPresent(old -> {
            event.getEntity().getCapability(CAP).ifPresent(now -> {
                if (now instanceof TeleportOrigin originNow && old instanceof TeleportOrigin originOld) {
                    originNow.load(originOld.save());
                }
            });
        });
        event.getOriginal().invalidateCaps();
    }
}
