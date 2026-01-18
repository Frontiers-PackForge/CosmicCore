package com.ghostipedia.cosmiccore.client.keybind;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.armor.boots.ICosmicBoots;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.BootsControlPacket;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

/**
 * Keybind handler for CosmicCore boots.
 * Provides controls for speed/jump modifiers, step assist, and inertia cancellation.
 */
@OnlyIn(Dist.CLIENT)
public class BootsKeybinds {

    public static final String CATEGORY = "key.categories.cosmiccore.boots";

    // Keybinds
    public static KeyMapping SPEED_INCREASE;
    public static KeyMapping SPEED_DECREASE;
    public static KeyMapping JUMP_INCREASE;
    public static KeyMapping JUMP_DECREASE;
    public static KeyMapping TOGGLE_STEP_ASSIST;
    public static KeyMapping TOGGLE_INERTIA;

    /**
     * Register all boot keybinds.
     */
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        SPEED_INCREASE = new KeyMapping(
                "key.cosmiccore.boots.speed_increase",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_ADD,
                CATEGORY);
        event.register(SPEED_INCREASE);

        SPEED_DECREASE = new KeyMapping(
                "key.cosmiccore.boots.speed_decrease",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_SUBTRACT,
                CATEGORY);
        event.register(SPEED_DECREASE);

        JUMP_INCREASE = new KeyMapping(
                "key.cosmiccore.boots.jump_increase",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_MULTIPLY,
                CATEGORY);
        event.register(JUMP_INCREASE);

        JUMP_DECREASE = new KeyMapping(
                "key.cosmiccore.boots.jump_decrease",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_DIVIDE,
                CATEGORY);
        event.register(JUMP_DECREASE);

        TOGGLE_STEP_ASSIST = new KeyMapping(
                "key.cosmiccore.boots.toggle_step",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY);
        event.register(TOGGLE_STEP_ASSIST);

        TOGGLE_INERTIA = new KeyMapping(
                "key.cosmiccore.boots.toggle_inertia",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY);
        event.register(TOGGLE_INERTIA);
    }

    /**
     * Event handler for boot keybinds.
     */
    @Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class KeyHandler {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (event.getAction() != GLFW.GLFW_PRESS) return;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.screen != null) return;

            // Check if player is wearing cosmic boots
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (!isCosmicBoots(boots)) return;

            // Handle keybinds
            if (SPEED_INCREASE != null && SPEED_INCREASE.matches(event.getKey(), event.getScanCode())) {
                CCoreNetwork.sendToServer(new BootsControlPacket(BootsControlPacket.Action.SPEED_INCREASE));
            } else if (SPEED_DECREASE != null && SPEED_DECREASE.matches(event.getKey(), event.getScanCode())) {
                CCoreNetwork.sendToServer(new BootsControlPacket(BootsControlPacket.Action.SPEED_DECREASE));
            } else if (JUMP_INCREASE != null && JUMP_INCREASE.matches(event.getKey(), event.getScanCode())) {
                CCoreNetwork.sendToServer(new BootsControlPacket(BootsControlPacket.Action.JUMP_INCREASE));
            } else if (JUMP_DECREASE != null && JUMP_DECREASE.matches(event.getKey(), event.getScanCode())) {
                CCoreNetwork.sendToServer(new BootsControlPacket(BootsControlPacket.Action.JUMP_DECREASE));
            } else if (TOGGLE_STEP_ASSIST != null &&
                    TOGGLE_STEP_ASSIST.matches(event.getKey(), event.getScanCode())) {
                        CCoreNetwork.sendToServer(new BootsControlPacket(BootsControlPacket.Action.TOGGLE_STEP));
                    } else
                if (TOGGLE_INERTIA != null && TOGGLE_INERTIA.matches(event.getKey(), event.getScanCode())) {
                    CCoreNetwork.sendToServer(new BootsControlPacket(BootsControlPacket.Action.TOGGLE_INERTIA));
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
}
