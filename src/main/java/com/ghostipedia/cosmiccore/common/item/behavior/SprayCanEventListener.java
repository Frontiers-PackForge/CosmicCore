package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import appeng.blockentity.networking.CableBusBlockEntity;

import java.util.Objects;

import static com.ghostipedia.cosmiccore.common.data.CosmicItems.INFINITE_SPRAY_CAN;
import static com.ghostipedia.cosmiccore.common.item.behavior.InfiniteSprayCanBehavior.ColorTag;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SprayCanEventListener {

    @SubscribeEvent
    public static void onClickEvent(InputEvent.InteractionKeyMappingTriggered event) {
        // isPickBlock() returns button == 2 which is the middle mouse click
        if (!event.isPickBlock()) {
            return;
        }

        // grabs thine game
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;

        if (player == null || level == null || mc.hitResult == null) return;

        ItemStack spraycan = player.getMainHandItem();
        if (spraycan.getItem() != INFINITE_SPRAY_CAN.get().asItem()) {
            return;

        }

        int dyeID = 0;
        ExtendedDyeColor color = null;
        // check if it gets a block
        if (mc.hitResult.getType() == HitResult.Type.BLOCK) {

            BlockHitResult blockHit = (BlockHitResult) mc.hitResult;
            BlockPos pos = blockHit.getBlockPos();

            BlockEntity entity = level.getBlockEntity(pos);
            if (entity != null) {
                if (entity instanceof ShulkerBoxBlockEntity shulker) {
                    color = ExtendedDyeColor.fromDyeColor(shulker.getColor());

                } else if (entity instanceof CableBusBlockEntity cable) {
                    var ae2dye = cable.getColor().dye;
                    if (ae2dye == null) {
                        color = ExtendedDyeColor.SOLVENT;
                    } else {
                        dyeID = ae2dye.getId();
                        color = ExtendedDyeColor.getColorFromDyeId(dyeID);
                    }
                } else if (entity instanceof IPaintable) {
                    for (DyeColor dye : DyeColor.values()) {
                        if (((IPaintable) entity).getPaintingColor() == -1) {

                            color = ExtendedDyeColor.SOLVENT;

                        }
                        if (((IPaintable) entity).getPaintingColor() == dye.getTextColor()) {
                            color = ExtendedDyeColor.getColorFromDyeId(dye.getId());
                        }
                    }
                } else if (entity instanceof MetaMachineBlockEntity meta) {

                    var Machinecolor = meta.getMetaMachine().getPaintingColor();
                    System.out.println(Machinecolor);
                    for (DyeColor dye : DyeColor.values()) {
                        if (Machinecolor == -1) {
                            color = ExtendedDyeColor.SOLVENT;
                            break;

                        } else if (Machinecolor == dye.getTextColor()) {
                            color = ExtendedDyeColor.fromDyeColor(dye);
                        }
                    }
                }
            }
            // normal blocks
            BlockState state = level.getBlockState(pos);
            MapColor mapColor = state.getMapColor(level, pos);

            // get the id of the map color
            int id = mapColor.id;

            // map id to dye
            if (id >= 15 && id <= 29) {
                dyeID = id - 14;
                color = ExtendedDyeColor.getColorFromDyeId(dyeID);
            }
            // terracotta is special
            else if (id >= 37 && id <= 51) {
                dyeID = id - 36;
                color = ExtendedDyeColor.getColorFromDyeId(dyeID);
            }
            // white maps to snow????????????????????????????
            else if (id == 8 || id == 36) {

                dyeID = 0;
                color = ExtendedDyeColor.getColorFromDyeId(dyeID);

            }

        }

        // send to spraycan when finished
        if (spraycan.getItem() instanceof ComponentItem compItem) {
            for (var component : compItem.getComponents()) {
                if (component instanceof InfiniteSprayCanBehavior behavior) {
                    if (player.isCrouching()) {

                        if (behavior.getIsLocked()){


                            player.displayClientMessage(Component.literal("Spray Can unlocked"), true);
                            behavior.setIsLocked(false);
                            event.setCanceled(true);
                        }
                        else{
                            behavior.setIsLocked(true);
                            player.displayClientMessage(Component.literal("Spray Can locked!"), true);
                            event.setCanceled(true);
                        }
                    } else {
                        if (!behavior.getIsLocked()) {
                            CompoundTag tag = spraycan.getOrCreateTag();
                            color = Objects.requireNonNullElse(color, ExtendedDyeColor.SOLVENT);
                            behavior.setColor(color);
                            behavior.sendColorToTag(player, behavior.color);
                        } else {

                            player.displayClientMessage(Component.literal("Spray Can locked!"), true);

                        }

                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
