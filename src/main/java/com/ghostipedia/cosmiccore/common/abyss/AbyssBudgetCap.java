package com.ghostipedia.cosmiccore.common.abyss;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber
public class AbyssBudgetCap {

    public static final ResourceLocation KEY = new ResourceLocation("cosmiccore", "abyss_budget");
    public static final Capability<IAbyssTimer> CAP = CapabilityManager.get(new CapabilityToken<>() {});

    public static class Provider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

        private final AbyssBudget impl = new AbyssBudget();
        private final LazyOptional<IAbyssTimer> lazyOpt = LazyOptional.of(() -> impl);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                          @Nullable Direction direction) {
            return capability == CAP ? lazyOpt.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = impl.tagSave();
            return tag != null ? tag : new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag compoundTag) {
            if (compoundTag != null) {
                impl.tagLoad(compoundTag);
            }
        }
    }

    @SubscribeEvent
    public static void registerCap(RegisterCapabilitiesEvent event) {
        event.register(IAbyssTimer.class);
    }

    @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(KEY, new Provider());
        }
    }

    @SubscribeEvent
    public static void cloneCap(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(CAP).ifPresent(old -> {
            event.getEntity().getCapability(CAP).ifPresent(now -> {
                if (now instanceof AbyssBudget timeNow && old instanceof AbyssBudget timeOld)
                    timeNow.tagLoad(timeOld.tagSave());
            });
        });
        event.getOriginal().invalidateCaps();
    }
}
