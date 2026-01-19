package com.ghostipedia.cosmiccore.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiFunction;

import static net.minecraft.commands.Commands.*;

public class SoulCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
            literal("wireless")
                .then(soulLiteral("info", LEVEL_ALL, SoulCommand::displayPlayerInfo, SoulCommand::displayTeamInfo))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> soulLiteral(String name, int permissionLevel,
                                                                          BiFunction<CommandContext<CommandSourceStack>, ServerPlayer, Integer> playerCommand,
                                                                          BiFunction<CommandContext<CommandSourceStack>, Team, Integer> teamCommand) {
        return literal(name)
                .requires(source -> source.hasPermission(permissionLevel))
                .then(literal("player").then(argument("player", EntityArgument.player())
                        .executes(ctx -> playerCommand.apply(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(literal("team").then(argument("team", TeamArgument.create())
                        .executes(ctx -> teamCommand.apply(ctx, TeamArgument.get(ctx, "team")))))
                .executes(ctx -> sourceCommand(ctx, playerCommand, teamCommand));
    }

    private static int sourceCommand(CommandContext<CommandSourceStack> context,
                                     BiFunction<CommandContext<CommandSourceStack>, ServerPlayer, Integer> playerCommand,
                                     BiFunction<CommandContext<CommandSourceStack>, Team, Integer> teamCommand) {
        var owner = getPlayerOrTeam(context.getSource().getPlayer());
        if (owner instanceof ServerPlayer player) return playerCommand.apply(context, player);
        else if (owner instanceof Team team) return teamCommand.apply(context, team);
        else return -1;
    }

    private static Object getPlayerOrTeam(ServerPlayer player) {
        var team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
        return (team.isPresent() && !team.get().isPlayerTeam()) ? team.get() : player;
    }

    // ####################################
    // Display Info
    // ####################################

    private static int displayPlayerInfo(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        return 1;
    }

    private static int displayTeamInfo(CommandContext<CommandSourceStack> context, Team team) {
        return 1;
    }
}
