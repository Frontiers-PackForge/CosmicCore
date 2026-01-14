package com.ghostipedia.cosmiccore.integration.emi.favorites;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class CosmicBookmarkKeybinds {

    public static final String CATEGORY = "key.categories.cosmiccore.emi";

    public static KeyMapping NEXT_BOOKMARK_GROUP;
    public static KeyMapping PREV_BOOKMARK_GROUP;
    public static KeyMapping CREATE_BOOKMARK_GROUP;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        NEXT_BOOKMARK_GROUP = new KeyMapping(
                "key.cosmiccore.emi.next_bookmark_group",
                KeyConflictContext.GUI,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                CATEGORY);

        PREV_BOOKMARK_GROUP = new KeyMapping(
                "key.cosmiccore.emi.prev_bookmark_group",
                KeyConflictContext.GUI,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                CATEGORY);

        CREATE_BOOKMARK_GROUP = new KeyMapping(
                "key.cosmiccore.emi.create_bookmark_group",
                KeyConflictContext.GUI,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                CATEGORY);

        event.register(NEXT_BOOKMARK_GROUP);
        event.register(PREV_BOOKMARK_GROUP);
        event.register(CREATE_BOOKMARK_GROUP);
    }

    @Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class KeyHandler {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (event.getAction() != GLFW.GLFW_PRESS) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen == null) return;

            CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

            if (NEXT_BOOKMARK_GROUP != null && NEXT_BOOKMARK_GROUP.matches(event.getKey(), event.getScanCode())) {
                manager.nextGroup();
                showGroupMessage(mc, manager);
            } else
                if (PREV_BOOKMARK_GROUP != null && PREV_BOOKMARK_GROUP.matches(event.getKey(), event.getScanCode())) {
                    manager.previousGroup();
                    showGroupMessage(mc, manager);
                } else if (CREATE_BOOKMARK_GROUP != null &&
                        CREATE_BOOKMARK_GROUP.matches(event.getKey(), event.getScanCode())) {
                            int newIndex = manager.getGroupCount() + 1;
                            manager.createGroup("Group " + newIndex);
                            manager.setActiveGroup(manager.getGroupCount() - 1);
                            showGroupMessage(mc, manager);
                        }
        }

        private static void showGroupMessage(Minecraft mc, CosmicBookmarkManager manager) {
            String groupName = manager.getActiveGroup().getName();
            int current = manager.getActiveGroupIndex() + 1;
            int total = manager.getGroupCount();
            mc.player.displayClientMessage(
                    Component.literal("§6[Bookmarks]§r " + groupName + " §7(" + current + "/" + total + ")"),
                    true);
        }
    }
}
