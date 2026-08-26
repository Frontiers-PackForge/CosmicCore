package com.ghostipedia.cosmiccore.forge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.api.gravity.GravityApi;
import com.ghostipedia.cosmiccore.client.map.RevealedField;
import com.ghostipedia.cosmiccore.common.commands.ExportRegistryCommand;
import com.ghostipedia.cosmiccore.common.commands.SoulCommand;
import com.ghostipedia.cosmiccore.common.commands.StarLadderCommand;
import com.ghostipedia.cosmiccore.common.commands.VeinSurveyCommand;
import com.ghostipedia.cosmiccore.common.commands.WirelessEnergyCommand;
import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingGTPipeRenderSync;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.FieldDiscoveryData;
import com.ghostipedia.cosmiccore.common.flight.FlightDiffuserBehavior;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodCommand;
import com.ghostipedia.cosmiccore.common.gravity.GravityDebugCommand;
import com.ghostipedia.cosmiccore.common.item.armor.boots.TravelerBootsLogic;
import com.ghostipedia.cosmiccore.common.item.behavior.EffectApplicationBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.WirelessPDABehavior;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedCommand;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.EffortlessBuildingAE2CountQueryPacket;
import com.ghostipedia.cosmiccore.common.network.packet.RevealFieldsPacket;
import com.ghostipedia.cosmiccore.mixin.accessor.LivingEntityAccessor;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

import static com.ghostipedia.cosmiccore.common.item.armor.ChestSanguineWarptechSuite.SANGUINE_SHIELD_NBT_KEY;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        FlightDiffuserBehavior.clear(player);
        TravelerBootsLogic.clearStepAssist(player);
        FieldDiscoveryData data = FieldDiscoveryData.get(player.getServer());
        String teamKey = DeedTeams.teamKey(player);
        for (String dimensionId : data.dimensionsFor(teamKey)) {
            ResourceLocation dimensionLoc = ResourceLocation.parse(dimensionId);
            List<RevealedField> fields = data.get(teamKey, dimensionLoc);
            if (fields.isEmpty()) continue;
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionLoc);
            CCoreNetwork.sendToPlayer(player, new RevealFieldsPacket(dimension, fields));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FlightDiffuserBehavior.clear(player);
            GravityApi.reset(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FlightDiffuserBehavior.clear(player);
            EffortlessBuildingAE2CountQueryPacket.clearPlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FlightDiffuserBehavior.clear(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTickPre(final PlayerTickEvent.Pre event) {
        TravelerBootsLogic.updateStepAssist(event.getEntity());
    }

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
        if (player instanceof ServerPlayer serverPlayer) {
            FlightDiffuserBehavior.tick(serverPlayer);
            WirelessPDABehavior.syncHud(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        EffortlessBuildingGTPipeRenderSync.flush(event.getServer());
    }

    @SubscribeEvent
    public static void onEquipChange(LivingEquipmentChangeEvent e) {
        if (e.getEntity() instanceof Player player && e.getSlot() == EquipmentSlot.FEET) {
            TravelerBootsLogic.clearStepAssist(player);
        }

        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (e.getSlot() != EquipmentSlot.CHEST) return;

        boolean putOn = e.getTo().is(CosmicItems.SANGUINE_WARPTECH_CHESTPLATE.get());
        boolean tookOff = e.getFrom().is(CosmicItems.SANGUINE_WARPTECH_CHESTPLATE.get()) && !putOn;

        if (tookOff && !p.isCreative() && !p.isSpectator()) {
            if (!FlightDiffuserBehavior.claimActiveFlight(p)) {
                p.getAbilities().mayfly = false;
                p.getAbilities().flying = false;
                p.fallDistance = 0;
                p.connection.send(
                        new ClientboundPlayerAbilitiesPacket(p.getAbilities()));
            }
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
        VeinSurveyCommand.register(event.getDispatcher());
        ExportRegistryCommand.register(event.getDispatcher());
        StarLadderCommand.register(event.getDispatcher());
        CosmicFoodCommand.register(event.getDispatcher());
        DeedCommand.register(event.getDispatcher());
        GravityDebugCommand.register(event.getDispatcher());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player instanceof ServerPlayer serverPlayer &&
                FlightDiffuserBehavior.consumeLandingProtection(serverPlayer)) {
            event.setCanceled(true);
            player.fallDistance = 0;
            return;
        }
        if (!TravelerBootsLogic.isWearing(player)) return;
        event.setCanceled(true);
        player.fallDistance = 0;
    }
}
