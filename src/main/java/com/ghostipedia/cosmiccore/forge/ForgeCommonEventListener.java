package com.ghostipedia.cosmiccore.forge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.common.commands.WirelessEnergyCommand;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.item.behavior.EffectApplicationBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.InfiniteSprayCanBehavior;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SoulHatchPartMachine;
import com.ghostipedia.cosmiccore.mixin.accessor.LivingEntityAccessor;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.ghostipedia.cosmiccore.common.data.CosmicItems.INFINITE_SPRAY_CAN;

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

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        WirelessEnergyCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void onClickEvent(InputEvent.InteractionKeyMappingTriggered event) {
        // isPickBlock() returns button == 2 which is the middle mouse click
        if (!event.isPickBlock()) {
            return;
        }

        // grabs thine game
        Minecraft mc = Minecraft.getInstance();

        // exit the event if no blocks clicked
        if (mc.hitResult == null) {
            return;
        }

        // check if it gets a block
        if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
            // gets the position level and player for future use
            BlockHitResult blockHit = (BlockHitResult) mc.hitResult;
            BlockPos pos = blockHit.getBlockPos();
            Level level = mc.level;
            assert level != null;
            Player player = mc.player;

            if (player != null) {
                ItemStack spraycan = player.getMainHandItem();
                if (spraycan.getItem() == INFINITE_SPRAY_CAN.get().asItem()) {

                    BlockState state = level.getBlockState(pos);
                    MapColor mapColor = state.getMapColor(level, pos);

                    int id = mapColor.id;
                    int dyeID;
                    // maps mapcolorID to Dye id
                    if (id == 8) {
                        dyeID = 0;
                    } else if (id >= 15 && id <= 29) {
                        dyeID = id - 14;
                    }
                    // daily reminder terracotta is stupid
                    else if (id == 36) {
                        dyeID = 0;
                    } else if (id > 36 && id <= 51) {
                        dyeID = id - 34;
                    } else {
                        dyeID = 0;
                        return;
                    }

                    if (spraycan.getItem() instanceof ComponentItem compItem) {

                        for (var component : compItem.getComponents()) {
                            if (component instanceof InfiniteSprayCanBehavior behavior) {
                                behavior.setColor(DyeColor.values()[dyeID]);
                            }
                        }

                    }

                }

            }
        }
    }
}
