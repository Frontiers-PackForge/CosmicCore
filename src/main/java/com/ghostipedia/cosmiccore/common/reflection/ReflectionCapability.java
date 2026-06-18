package com.ghostipedia.cosmiccore.common.reflection;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Capability provider for the Reflection system.
 * Handles attachment to players and persistence across death/respawn.
 */
@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class ReflectionCapability {

    private ReflectionCapability() {}

    public static final Capability<IReflection> CAP = CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * Get the reflection data for a player.
     */
    public static Optional<IReflection> get(Player player) {
        return player.getCapability(CAP).resolve();
    }

    /**
     * Get the reflection data, or throw if not present.
     */
    public static IReflection getOrThrow(Player player) {
        return get(player).orElseThrow(() -> new IllegalStateException("Player missing Reflection capability"));
    }

    public static class Provider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

        private final ReflectionData impl = new ReflectionData();
        private final LazyOptional<IReflection> opt = LazyOptional.of(() -> impl);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            return cap == CAP ? opt.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = impl.saveTag();
            return tag != null ? tag : new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt != null) {
                impl.loadTag(nbt);
            }
        }
    }

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(IReflection.class);
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(CosmicCore.id("reflection"), new Provider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        // Preserve reflection data across death/respawn and dimension changes
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(CAP).ifPresent(old -> event.getEntity().getCapability(CAP).ifPresent(now -> {
            if (now instanceof ReflectionData newCap && old instanceof ReflectionData oldCap) {
                newCap.loadTag(oldCap.saveTag());
            }
        }));
        event.getOriginal().invalidateCaps();
    }
}
