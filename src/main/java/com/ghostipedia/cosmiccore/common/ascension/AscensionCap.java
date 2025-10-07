package com.ghostipedia.cosmiccore.common.ascension;


import com.ghostipedia.cosmiccore.CosmicCore;
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

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AscensionCap {

    public static final Capability<IAscensionProgress> CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final ResourceLocation KEY = CosmicCore.id("ascension_progress");


    public static class Provider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
        private final IAscensionProgress impl = new AscensionProgress();
        private final LazyOptional<IAscensionProgress> opt = LazyOptional.of(() -> impl);
        @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side){
            return cap == CAP ? opt.cast() : LazyOptional.empty();
        }
        @Override public CompoundTag serializeNBT(){ return impl.save(); }
        @Override public void deserializeNBT(CompoundTag nbt){ impl.load(nbt); }
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof Player) event.addCapability(KEY, new Provider());
    }

    @SubscribeEvent public static void clone(PlayerEvent.Clone event){
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(CAP).ifPresent(old ->
                event.getEntity().getCapability(CAP).ifPresent(cur -> cur.load(old.save()))
        );
        event.getOriginal().invalidateCaps();
    }

}
