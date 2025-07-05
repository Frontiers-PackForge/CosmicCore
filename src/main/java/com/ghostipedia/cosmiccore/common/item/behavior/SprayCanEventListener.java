package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.ghostipedia.cosmiccore.common.data.CosmicItems.INFINITE_SPRAY_CAN;

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

        // null checks the player level and if there is a blokc
        if (player == null || level == null || mc.hitResult == null) return;

        // gets the spraycan and makes sure it is a spraycan
        ItemStack spraycan = player.getMainHandItem();
        if (hasSprayCan(spraycan)) return;

        // sets an id for the dye to properly assign the color
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
        InfiniteSprayCanBehavior behavior = getSprayCanBehavior(spraycan);
        if (behavior != null) {

            // checks if it is locked first before anything
            if (!behavior.getIsLocked()) {
                color = Objects.requireNonNullElse(color, ExtendedDyeColor.SOLVENT);
                behavior.setColor(color);
                behavior.sendColorToTag(player, behavior.color);
            } else {
                player.displayClientMessage(Component.literal("Spray Can locked!"), true);
            }

            event.setCanceled(true);
        }
    }

    // this event is used here because the other one needs a b lock to be clicked on this one works in the air
    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        if (event.getButton() != 2 || event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        // null check same as above
        if (player == null || level == null || mc.hitResult == null) return;
        ItemStack spraycan = player.getMainHandItem();
        if (hasSprayCan(spraycan)) return;

        // returns if the player isn't crouching
        if (!player.isCrouching()) return;

        InfiniteSprayCanBehavior behavior = getSprayCanBehavior(spraycan);
        if (behavior == null) return;

        // if its locked invert it
        boolean nowLocked = !behavior.getIsLocked();
        behavior.setIsLocked(nowLocked);
        event.setCanceled(true);

        String message = "Spray Can " + (nowLocked ? "locked!" : "unlocked!");
        player.displayClientMessage(Component.literal(message), true);
    }

    // just pulls out some repeated code for cleanliness into this method to check if it is a spraycan
    private static boolean hasSprayCan(ItemStack stack) {
        return stack.getItem() != INFINITE_SPRAY_CAN.get().asItem();
    }

    // gets the bahavior to reduce repeated code
    private static InfiniteSprayCanBehavior getSprayCanBehavior(ItemStack stack) {
        if (!(stack.getItem() instanceof ComponentItem compItem)) return null;

        for (var component : compItem.getComponents()) {
            if (component instanceof InfiniteSprayCanBehavior behavior) {
                return behavior;
            }
        }
        return null;
    }
}
