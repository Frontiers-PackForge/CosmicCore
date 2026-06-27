package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.armor.boots.ICosmicBoots;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class BootsControlPacket implements CustomPacketPayload {

    public static final Type<BootsControlPacket> TYPE = new Type<>(CosmicCore.id("boots_control"));
    public static final StreamCodec<FriendlyByteBuf, BootsControlPacket> CODEC = StreamCodec
            .ofMember(BootsControlPacket::encode, BootsControlPacket::new);

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

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(action);
    }

    public void execute(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

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
                messageArg = enabled ? "§aON" : "§cOFF";
            }
            case TOGGLE_INERTIA -> {
                boolean enabled = ICosmicBoots.toggleInertiaCancel(boots);
                messageKey = "cosmiccore.boots.message.inertia";
                messageArg = enabled ? "§aON" : "§cOFF";
            }
        }

        if (messageKey != null) {
            player.displayClientMessage(Component.translatable(messageKey, messageArg), true);
        }
    }

    private static boolean isCosmicBoots(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ArmorComponentItem armorItem)) return false;
        return armorItem.getArmorLogic() instanceof ICosmicBoots;
    }

    @Override
    public @NotNull Type<BootsControlPacket> type() {
        return TYPE;
    }
}
