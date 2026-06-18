package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.gui.SprayCanScreen;
import com.ghostipedia.cosmiccore.common.data.CosmicSounds;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import appeng.blockentity.networking.CableBusBlockEntity;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.ghostipedia.cosmiccore.common.item.behavior.SprayCanEventListener.getSprayCanBehavior;
import static com.ghostipedia.cosmiccore.common.item.behavior.SprayCanEventListener.hasSprayCan;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SprayCanClientHandler {

    @OnlyIn(Dist.CLIENT)
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

            /**
             * im not even gonna try to explain what this does but it grabs the block
             * and checks what state it is and gets the map color
             * and then converts it to a dye color
             * and then send that dye to the spray can but since mc is stupid it has to be like this
             */

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
                } else if (entity instanceof MetaMachine meta) {
                    var machineColor = meta.getPaintingColor();
                    for (DyeColor dye : DyeColor.values()) {
                        if (machineColor == -1) {
                            color = ExtendedDyeColor.SOLVENT;
                            break;
                        } else if (machineColor == dye.getTextColor()) {
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
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(CosmicSounds.SHAKE_CAN.getMainEvent(), 1.0f, 1.0f));
            } else {
                player.displayClientMessage(Component.translatable("cosmiccore.item.spraycan.locked"), true);
            }

            event.setCanceled(true);
        }
    }

    // this event is used here because the other one needs a b lock to be clicked on this one works in the air
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        int button = event.getButton();
        if ((button != 0 && button != 2) || event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        // null check same as above
        if (player == null || level == null || mc.hitResult == null) return;
        ItemStack spraycan = player.getMainHandItem();
        if (hasSprayCan(spraycan)) return;

        InfiniteSprayCanBehavior behavior = getSprayCanBehavior(spraycan);
        if (behavior == null) return;

        // resets the isSwinging flag
        if (button == 0) {
            behavior.isSwinging = false;
            return;
        }

        // returns if the player isn't crouching
        if (!player.isCrouching()) return;

        // if its locked invert it
        boolean nowLocked = !behavior.getIsLocked();
        behavior.setIsLocked(nowLocked);
        event.setCanceled(true);

        String langKey = nowLocked ? "cosmiccore.item.spraycan.now_locked" : "cosmiccore.item.spraycan.now_unlocked";
        player.displayClientMessage(Component.translatable(langKey), true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void openScreen(Player player, InfiniteSprayCanBehavior behavior) {
        Minecraft.getInstance().setScreen(new SprayCanScreen(player, behavior));
    }

    @OnlyIn(Dist.CLIENT)
    public static void playShakeSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(CosmicSounds.SHAKE_CAN.getMainEvent(), 1.0f, 1.0f));
    }
}
