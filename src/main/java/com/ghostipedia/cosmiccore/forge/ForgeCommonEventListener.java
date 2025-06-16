package com.ghostipedia.cosmiccore.forge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.common.commands.WirelessEnergyCommand;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.item.behavior.EffectApplicationBehavior;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SoulHatchPartMachine;
import com.ghostipedia.cosmiccore.mixin.accessor.LivingEntityAccessor;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.ghostipedia.cosmiccore.common.item.armor.ChestSanguineWarptechSuite.SANGUINE_SHIELD_NBT_KEY;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void entityPlacementEventHandler(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().getBlock() instanceof MetaMachineBlock block &&
                block.getMachine(event.getLevel(), event.getPos()) instanceof SoulHatchPartMachine soulHatch &&
                event.getEntity() instanceof Player player) {
            soulHatch.attachSoulNetwork(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (CosmicUtils.hasTheOneRing(event.player)) {
            // forcefully get the ring's effects.
            var effects = ((EffectApplicationBehavior) CosmicItems.THE_ONE_RING.get().getComponents().get(0))
                    .getEffects();
            for (var effect : effects) {
                if (event.player.getRandom().nextFloat() < effect.getSecond()) {
                    event.player.addEffect(new MobEffectInstance(effect.getFirst()));
                }
            }
            ((LivingEntityAccessor) event.player).callRemoveEffectParticles();
        }
    }

    // Sanguine chest piece gives creative flight when equipped and powered.
    // This is handled in the ChestSanguineWarptechSuite.java
    // However, we want to take this flight away when it is taken off.
    @SubscribeEvent
    public static void onEquipChange(LivingEquipmentChangeEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (e.getSlot() != EquipmentSlot.CHEST) return;

        boolean putOn = e.getTo().is(CosmicItems.SANGUINE_WARPTECH_CHESTPLATE.get());
        boolean tookOff = e.getFrom().is(CosmicItems.SANGUINE_WARPTECH_CHESTPLATE.get()) && !putOn;

        if (tookOff && !p.isCreative() && !p.isSpectator()) {
            p.getAbilities().mayfly = false;
            p.getAbilities().flying = false;
            p.fallDistance = 0;
            p.connection.send(
                    new ClientboundPlayerAbilitiesPacket(p.getAbilities()));
            p.getPersistentData().putBoolean(SANGUINE_SHIELD_NBT_KEY, false);
        }
    }

    // Sanguine shield effect from sanguine chest piece negates all damage and defies death.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        CompoundTag tag = player.getPersistentData();
        if (tag.contains(SANGUINE_SHIELD_NBT_KEY) && tag.getBoolean(SANGUINE_SHIELD_NBT_KEY)) {
            event.setCanceled(true);
        }
    }

    // Sanguine shield effect from sanguine chest piece negates all damage and defies death.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        CompoundTag tag = player.getPersistentData();
        if (tag.contains(SANGUINE_SHIELD_NBT_KEY) && tag.getBoolean(SANGUINE_SHIELD_NBT_KEY)) {
            event.setCanceled(true); // Prevent death
            player.sendSystemMessage(
                    Component.translatable("cosmiccore.armor.sanguinewarptech.message.death_defiance"));
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        WirelessEnergyCommand.register(event.getDispatcher(), event.getBuildContext());
    }
}
