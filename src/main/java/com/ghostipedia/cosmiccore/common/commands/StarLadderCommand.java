package com.ghostipedia.cosmiccore.common.commands;

import com.ghostipedia.cosmiccore.api.data.savedData.StarLadderSavedData;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;

import java.util.UUID;

import static net.minecraft.commands.Commands.*;

public class StarLadderCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("starladder")
                        .requires(source -> source.hasPermission(LEVEL_ADMINS))
                        .then(literal("status")
                                .then(argument("player", EntityArgument.player())
                                        .executes(ctx -> showStatus(ctx,
                                                EntityArgument.getPlayer(ctx, "player")))))
                        .then(literal("reset")
                                .then(argument("player", EntityArgument.player())
                                        .executes(ctx -> resetUplink(ctx,
                                                EntityArgument.getPlayer(ctx, "player"))))));
    }

    private static UUID getTeamId(ServerPlayer player) {
        var teamManager = FTBTeamsAPI.api().getManager();
        var team = teamManager.getTeamForPlayer(player).orElse(null);
        return team != null ? team.getTeamId() : player.getUUID();
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        UUID teamId = getTeamId(player);
        var data = StarLadderSavedData.getOrCreate(ctx.getSource().getServer());
        boolean established = data.isEstablished(teamId);

        ctx.getSource().sendSuccess(() -> Component.literal("Star Ladder uplink for ")
                .append(player.getDisplayName())
                .append(": ")
                .append(established ? Component.literal("ESTABLISHED").withStyle(s -> s.withColor(0x40CC40)) :
                        Component.literal("NOT ESTABLISHED").withStyle(s -> s.withColor(0xCC4040))),
                false);
        return 1;
    }

    private static int resetUplink(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        UUID teamId = getTeamId(player);
        var data = StarLadderSavedData.getOrCreate(ctx.getSource().getServer());
        boolean wasEstablished = data.resetEstablished(teamId);

        if (wasEstablished) {
            ctx.getSource().sendSuccess(() -> Component.literal("Reset Star Ladder uplink for ")
                    .append(player.getDisplayName()), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("Star Ladder uplink was not established for ")
                    .append(player.getDisplayName()), false);
        }
        return 1;
    }
}
