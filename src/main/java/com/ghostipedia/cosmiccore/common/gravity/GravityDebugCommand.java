package com.ghostipedia.cosmiccore.common.gravity;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.gravity.GravityApi;
import com.ghostipedia.cosmiccore.api.gravity.GravityFrame;
import com.ghostipedia.cosmiccore.api.gravity.GravityMode;
import com.ghostipedia.cosmiccore.common.firmament.FirmamentTraversalLogic;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public final class GravityDebugCommand {

    private GravityDebugCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("cosmicgravity")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset").executes(context -> reset(context.getSource())))
                .then(Commands.literal("normal").executes(context -> reset(context.getSource())));
        for (Direction down : Direction.values()) {
            root.then(Commands.literal(down.getSerializedName())
                    .executes(context -> set(context.getSource(), down)));
        }
        dispatcher.register(root);
    }

    private static int set(CommandSourceStack source, Direction down) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        GravityApi.requestFrame(player, new GravityFrame(
                GravityMode.DIRECTED,
                down,
                1.0,
                CosmicCore.id("debug/datum"),
                100,
                20,
                0.12,
                0L));
        return 1;
    }

    private static int reset(CommandSourceStack source) throws CommandSyntaxException {
        FirmamentTraversalLogic.forceReset(source.getPlayerOrException());
        return 1;
    }
}
