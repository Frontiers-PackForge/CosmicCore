package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.common.item.armor.boots.ICosmicBoots;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Packet sent from client to server when player presses boot control keybinds.
 */
public class BootsControlPacket implements CCoreNetwork.INetPacket {

    public enum Action {
        SPEED_INCREASE,
        SPEED_DECREASE,
        JUMP_INCREASE,
        JUMP_DECREASE,
        TOGGLE_STEP,
        TOGGLE_INERTIA
    }

    private final Action action;

    public BootsControlPacket(Action action) {
        this.action = action;
    }

    public BootsControlPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(Action.class);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(action);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!isCosmicBoots(boots)) return;

        String messageKey = null;
        Object messageArg = null;

        switch (action) {
            case SPEED_INCREASE -> {
                double newValue = ICosmicBoots.changeSpeedModifier(boots, ICosmicBoots.MODIFIER_INCREMENT);
                messageKey = "cosmiccore.boots.message.speed";
                messageArg = String.format("%.0f%%", newValue * 100);
            }
            case SPEED_DECREASE -> {
                double newValue = ICosmicBoots.changeSpeedModifier(boots, -ICosmicBoots.MODIFIER_INCREMENT);
                messageKey = "cosmiccore.boots.message.speed";
                messageArg = String.format("%.0f%%", newValue * 100);
            }
            case JUMP_INCREASE -> {
                double newValue = ICosmicBoots.changeJumpModifier(boots, ICosmicBoots.MODIFIER_INCREMENT);
                messageKey = "cosmiccore.boots.message.jump";
                messageArg = String.format("%.0f%%", newValue * 100);
            }
            case JUMP_DECREASE -> {
                double newValue = ICosmicBoots.changeJumpModifier(boots, -ICosmicBoots.MODIFIER_INCREMENT);
                messageKey = "cosmiccore.boots.message.jump";
                messageArg = String.format("%.0f%%", newValue * 100);
            }
            case TOGGLE_STEP -> {
                boolean enabled = ICosmicBoots.toggleStepAssist(boots);
                messageKey = "cosmiccore.boots.message.step";
                messageArg = enabled ? "\u00a7aON" : "\u00a7cOFF";
            }
            case TOGGLE_INERTIA -> {
                boolean enabled = ICosmicBoots.toggleInertiaCancel(boots);
                messageKey = "cosmiccore.boots.message.inertia";
                messageArg = enabled ? "\u00a7aON" : "\u00a7cOFF";
            }
        }

        if (messageKey != null) {
            player.displayClientMessage(Component.translatable(messageKey, messageArg), true);
        }
    }

    /**
     * Check if the item is a CosmicCore boots item.
     */
    private static boolean isCosmicBoots(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ArmorComponentItem armorItem)) return false;
        return armorItem.getArmorLogic() instanceof ICosmicBoots;
    }
}
