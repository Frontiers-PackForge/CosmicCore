package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.gui.SprayCanScreen;
import com.ghostipedia.cosmiccore.common.data.CosmicSounds;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.SprayCanStatePacket;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

import appeng.blockentity.networking.CableBusBlockEntity;

import java.util.Objects;

import static com.ghostipedia.cosmiccore.common.item.behavior.SprayCanEventListener.isSprayCan;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SprayCanClientHandler {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClickEvent(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;
        if (player == null || level == null || mc.hitResult == null) return;

        boolean shiftDown = mc.options.keyShift.isDown();
        if (event.isUseItem() && shiftDown && mc.hitResult.getType() != HitResult.Type.BLOCK) {
            InteractionHand useHand = findSprayCanHand(player);
            if (useHand == null) return;
            openScreen(player, useHand);
            event.setCanceled(true);
            return;
        }

        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack spraycan = player.getMainHandItem();
        if (!isSprayCan(spraycan)) return;

        if (event.isAttack()) {
            updateState(hand, spraycan, SprayCanStatePacket.Action.CYCLE, shiftDown ? -1 : 1);
            event.setCanceled(true);
            return;
        }

        if (!event.isPickBlock()) return;
        if (shiftDown) {
            updateState(hand, spraycan, SprayCanStatePacket.Action.TOGGLE_LOCK, 0);
            event.setCanceled(true);
            return;
        }

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

        color = Objects.requireNonNullElse(color, ExtendedDyeColor.SOLVENT);
        updateState(hand, spraycan, SprayCanStatePacket.Action.SET_COLOR, color.ordinal());
        event.setCanceled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void openScreen(Player player, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new SprayCanScreen(player, hand));
    }

    @OnlyIn(Dist.CLIENT)
    public static void playShakeSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(CosmicSounds.SHAKE_CAN.getMainEvent(), 1.0f, 1.0f));
    }

    private static InteractionHand findSprayCanHand(Player player) {
        if (isSprayCan(player.getMainHandItem())) return InteractionHand.MAIN_HAND;
        if (isSprayCan(player.getOffhandItem())) return InteractionHand.OFF_HAND;
        return null;
    }

    public static void updateState(InteractionHand hand, ItemStack stack, SprayCanStatePacket.Action action,
                                   int value) {
        SprayCanState before = SprayCanState.read(stack);
        SprayCanState after = SprayCanStatePacket.apply(before, action, value);
        if (after.equals(before)) {
            if (before.locked() && action != SprayCanStatePacket.Action.TOGGLE_LOCK &&
                    action != SprayCanStatePacket.Action.SET_MODE) {
                Minecraft.getInstance().player.displayClientMessage(InfiniteSprayCanBehavior.lockedMessage(), true);
            }
            return;
        }
        after.write(stack);
        if (action == SprayCanStatePacket.Action.SET_MODE) SprayCanState.clearSelection(stack);
        CCoreNetwork.sendToServer(new SprayCanStatePacket(hand, action, value));
        if (action == SprayCanStatePacket.Action.TOGGLE_LOCK) {
            Minecraft.getInstance().player.displayClientMessage(InfiniteSprayCanBehavior.lockMessage(after.locked()),
                    true);
        } else if (action == SprayCanStatePacket.Action.CYCLE || action == SprayCanStatePacket.Action.SET_COLOR) {
            InfiniteSprayCanBehavior.printColorToActionBar(Minecraft.getInstance().player, after.color());
            playShakeSound();
        }
    }
}
