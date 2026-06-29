package com.ghostipedia.cosmiccore.forge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.common.commands.ExportRegistryCommand;
import com.ghostipedia.cosmiccore.common.commands.SoulCommand;
import com.ghostipedia.cosmiccore.common.commands.StarLadderCommand;
import com.ghostipedia.cosmiccore.common.commands.VeinSurveyCommand;
import com.ghostipedia.cosmiccore.common.commands.WirelessEnergyCommand;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.item.armor.boots.ICosmicBoots;
import com.ghostipedia.cosmiccore.common.item.behavior.EffectApplicationBehavior;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCommand;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCommands;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;
import com.ghostipedia.cosmiccore.mixin.accessor.LivingEntityAccessor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static com.ghostipedia.cosmiccore.common.item.armor.ChestSanguineWarptechSuite.SANGUINE_SHIELD_NBT_KEY;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (CosmicUtils.hasTheOneRing(player)) {
            var effects = ((EffectApplicationBehavior) CosmicItems.THE_ONE_RING.get().getComponents().get(0))
                    .getEffects();
            for (var effect : effects) {
                if (player.getRandom().nextFloat() < effect.getSecond()) {
                    player.addEffect(new MobEffectInstance(effect.getFirst()));
                }
            }
            ((LivingEntityAccessor) player).callRemoveEffectParticles();
        }
    }

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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        CompoundTag tag = player.getPersistentData();
        if (tag.contains(SANGUINE_SHIELD_NBT_KEY) && tag.getBoolean(SANGUINE_SHIELD_NBT_KEY)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        CompoundTag tag = player.getPersistentData();
        if (tag.contains(SANGUINE_SHIELD_NBT_KEY) && tag.getBoolean(SANGUINE_SHIELD_NBT_KEY)) {
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth());
            player.sendSystemMessage(
                    Component.translatable("cosmiccore.armor.sanguinewarptech.message.death_defiance"));
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        WirelessEnergyCommand.register(event.getDispatcher(), event.getBuildContext());
        SoulCommand.register(event.getDispatcher(), event.getBuildContext());
        if (ReflectionConstants.ENABLED) {
            ReflectionCommand.register(event.getDispatcher());
            ReflectionCommands.register(event.getDispatcher());
        }
        VeinSurveyCommand.register(event.getDispatcher());
        ExportRegistryCommand.register(event.getDispatcher());
        StarLadderCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) return;

        if (!(boots.getItem() instanceof ArmorComponentItem armorItem)) return;
        if (!(armorItem.getArmorLogic() instanceof ICosmicBoots cosmicBoots)) return;

        var electric = GTCapabilityHelper.getElectricItem(boots);
        if (electric == null || electric.getCharge() <= 0) return;

        double jumpPower = cosmicBoots.getEffectiveJumpPower(boots);
        if (jumpPower <= 1.0) return;

        var motion = player.getDeltaMovement();
        double boostedY = motion.y * jumpPower;
        player.setDeltaMovement(motion.x, boostedY, motion.z);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) return;

        if (!(boots.getItem() instanceof ArmorComponentItem armorItem)) return;
        if (!(armorItem.getArmorLogic() instanceof ICosmicBoots cosmicBoots)) return;

        var electric = GTCapabilityHelper.getElectricItem(boots);
        if (electric == null || electric.getCharge() <= 0) return;

        if (cosmicBoots.negatesFallDamage()) {
            event.setCanceled(true);
            player.fallDistance = 0;
        }
    }
}
