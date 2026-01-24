package com.ghostipedia.cosmiccore.forge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.common.commands.SoulCommand;
import com.ghostipedia.cosmiccore.common.commands.VeinSurveyCommand;
import com.ghostipedia.cosmiccore.common.commands.WirelessEnergyCommand;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;
import com.ghostipedia.cosmiccore.common.item.armor.boots.ICosmicBoots;
import com.ghostipedia.cosmiccore.common.item.behavior.EffectApplicationBehavior;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.IPBF;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.SteamAssembler;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.SteamCaster;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.SteamMixer;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SoulHatchPartMachine;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCommand;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCommands;
import com.ghostipedia.cosmiccore.mixin.accessor.LivingEntityAccessor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;

import static com.ghostipedia.cosmiccore.common.item.armor.ChestSanguineWarptechSuite.SANGUINE_SHIELD_NBT_KEY;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

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
        SoulCommand.register(event.getDispatcher(), event.getBuildContext());
        ReflectionCommand.register(event.getDispatcher());
        ReflectionCommands.register(event.getDispatcher());
        VeinSurveyCommand.register(event.getDispatcher());
    }

    /**
     * Apply jump boost from Cosmic Boots when player jumps.
     * The jump power is a multiplier on the base jump velocity.
     */
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) return;

        // Check if wearing Cosmic Boots
        if (!(boots.getItem() instanceof ArmorComponentItem armorItem)) return;
        if (!(armorItem.getArmorLogic() instanceof ICosmicBoots cosmicBoots)) return;

        // Check if boots have power
        var electric = GTCapabilityHelper.getElectricItem(boots);
        if (electric == null || electric.getCharge() <= 0) return;

        // Get jump power multiplier (accounting for user's jump modifier setting)
        double jumpPower = cosmicBoots.getEffectiveJumpPower(boots);
        if (jumpPower <= 1.0) return; // No boost needed

        // Apply the jump boost by scaling the Y velocity
        // Vanilla jump gives ~0.42 Y velocity
        var motion = player.getDeltaMovement();
        double boostedY = motion.y * jumpPower;
        player.setDeltaMovement(motion.x, boostedY, motion.z);
    }

    /**
     * Cancel fall damage for players wearing Cosmic Boots with fall negation enabled.
     * This handles the actual damage cancellation that negatesFallDamage() promises.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) return;

        // Check if wearing Cosmic Boots
        if (!(boots.getItem() instanceof ArmorComponentItem armorItem)) return;
        if (!(armorItem.getArmorLogic() instanceof ICosmicBoots cosmicBoots)) return;

        // Check if boots have power
        var electric = GTCapabilityHelper.getElectricItem(boots);
        if (electric == null || electric.getCharge() <= 0) return;

        // Check if fall negation is enabled for these boots
        if (cosmicBoots.negatesFallDamage()) {
            // Cancel the fall damage entirely
            event.setCanceled(true);
            // Reset fall distance so no damage accumulates
            player.fallDistance = 0;
        }
    }

    @SubscribeEvent
    public static void remapIds(MissingMappingsEvent event) {
        // beeg machines

        remapMultiMachine(event, "steam_caster", SteamCaster.STEAM_CASTER);
        remapMultiMachine(event, "steam_mixer", SteamMixer.STEAM_MIXER);
        remapMultiMachine(event, "industrial_primitive_blast_furnace",
                IPBF.INDUSTRIAL_PRIMITIVE_BLAST_FURNACE);
        remapMultiMachine(event, "high_pressure_assembler", SteamAssembler.HIGH_PRESSURE_ASSEMBLER);
        remapMultiMachine(event, "large_combustion_engine_cc", CosmicMachines.LARGE_COMBUSTION_ENGINE);
        remapMultiMachine(event, "extreme_combustion_engine_cc", CosmicMachines.EXTREME_COMBUSTION_ENGINE);
        remapMultiMachine(event, "ludicrous_combustion_engine_cc", CosmicMachines.LUDICROUS_COMBUSTION_ENGINE);
        remapMultiMachine(event, "ultimate_combustion_engine_cc", CosmicMachines.ULTIMATE_COMBUSTION_ENGINE);

        // naq mini reactors
        for (MachineDefinition machine : CosmicMachines.NAQUAHINE_MINI_REACTOR) {
            if (machine == null) continue;
            String name = (GTValues.VN[machine.getTier()] + "_naquahine_mini_reactor");
            remapSingleBLocks(event, name, machine);
        }

        // steam sbs

        remapSingleBLocks(event, "lp_steam_wiremill", CosmicMachines.STEAM_WIREMILL.first());
        remapSingleBLocks(event, "hp_steam_wiremill", CosmicMachines.STEAM_WIREMILL.second());
        remapSingleBLocks(event, "lp_steam_wiremill", CosmicMachines.STEAM_BENDER.first());
        remapSingleBLocks(event, "hp_steam_wiremill", CosmicMachines.STEAM_BENDER.second());

        remapSingleBLocks(event, "steam_fluid_output_hatch", CosmicMachines.STEAM_EXPORT_HATCH);
        remapSingleBLocks(event, "steam_fluid_input_hatch", CosmicMachines.STEAM_IMPORT_HATCH);
    }

    private static void remapMultiMachine(MissingMappingsEvent event, String id, MultiblockMachineDefinition machine) {
        ResourceLocation resourceId = GTCEu.id(id);

        event.getMappings(Registries.ITEM, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getItem());
            }
        });

        event.getMappings(Registries.BLOCK, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getBlock());
            }
        });

        event.getMappings(Registries.BLOCK_ENTITY_TYPE, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getBlockEntityType());
            }
        });
    }

    private static void remapSingleBLocks(MissingMappingsEvent event, String id, MachineDefinition machine) {
        ResourceLocation resourceId = GTCEu.id(id);

        event.getMappings(Registries.ITEM, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getItem());
            }
        });

        event.getMappings(Registries.BLOCK, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getBlock());
            }
        });

        event.getMappings(Registries.BLOCK_ENTITY_TYPE, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getBlockEntityType());
            }
        });
    }
}
