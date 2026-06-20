package com.ghostipedia.cosmiccore.common.reflection;

import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.BackBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.HomeBargain;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;

/**
 * Registers the /home and /back commands that are unlocked via Reflection bargains.
 */
public class ReflectionCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /home command - teleport to spawn/bed
        dispatcher.register(
                Commands.literal("home")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 0;

                            // Check if they have the bargain
                            boolean hasBargain = ReflectionCapability.get(player)
                                    .map(r -> r.hasBargain(HomeBargain.INSTANCE.getId()))
                                    .orElse(false);

                            if (!hasBargain) {
                                player.displayClientMessage(
                                        Component.literal("§7§o*You haven't bargained for this power.*"),
                                        false);
                                return 0;
                            }

                            return HomeBargain.executeHome(player) ? 1 : 0;
                        }));

        // /back command - return to last death location
        dispatcher.register(
                Commands.literal("back")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 0;

                            // Check if they have the bargain
                            boolean hasBargain = ReflectionCapability.get(player)
                                    .map(r -> r.hasBargain(BackBargain.INSTANCE.getId()))
                                    .orElse(false);

                            if (!hasBargain) {
                                player.displayClientMessage(
                                        Component.literal("§7§o*You haven't bargained for this power.*"),
                                        false);
                                return 0;
                            }

                            return BackBargain.executeBack(player) ? 1 : 0;
                        }));
    }
}
