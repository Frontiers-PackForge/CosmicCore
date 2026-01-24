package com.ghostipedia.cosmiccore.common.airControl;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class OxygenBudgetCap {

    private OxygenBudgetCap() {}

    public static final Capability<IOxygen> CAP = CapabilityManager.get(new CapabilityToken<>() {});

    public static class Provider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

        private final OxygenBudget impl = new OxygenBudget();
        private final LazyOptional<IOxygen> opt = LazyOptional.of(() -> impl);

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
        event.register(IOxygen.class);
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof net.minecraft.world.entity.player.Player)
            event.addCapability(CosmicCore.id("oxygen"), new Provider());
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(CAP).ifPresent(old -> event.getEntity().getCapability(CAP).ifPresent(now -> {
            if (now instanceof OxygenBudget newCap && old instanceof OxygenBudget oldCap) {
                newCap.loadTag(oldCap.saveTag());
            }
        }));
        event.getOriginal().invalidateCaps();
    }
}
