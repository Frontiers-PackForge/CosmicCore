package com.ghostipedia.cosmiccore.common.commands;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.function.BiFunction;

import static net.minecraft.commands.Commands.*;

public class WirelessEnergyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("wireless_energy")
                        .then(wirelessLiteral("info", LEVEL_ALL, WirelessEnergyCommand::displayPlayerInfo, WirelessEnergyCommand::displayTeamInfo))
                        .then(wirelessLiteral("clear", LEVEL_ADMINS, WirelessEnergyCommand::clearPlayerData, WirelessEnergyCommand::clearTeamData))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> wirelessLiteral(String name, int permissionLevel,
        BiFunction<CommandContext<CommandSourceStack>, ServerPlayer, Integer> playerCommand, BiFunction<CommandContext<CommandSourceStack>, Team, Integer> teamCommand) {
        return literal(name)
                .requires(source -> source.hasPermission(permissionLevel))
                    .then(literal("player").then(argument("player", EntityArgument.player())
                            .executes(ctx -> playerCommand.apply(ctx, EntityArgument.getPlayer(ctx, "player")))))
                    .then(literal("team").then(argument("team", TeamArgument.create())
                            .executes(ctx -> teamCommand.apply(ctx, TeamArgument.get(ctx, "team")))))
                    .executes(ctx -> sourceCommand(ctx, playerCommand, teamCommand));
    }

    private static int sourceCommand(CommandContext<CommandSourceStack> context, BiFunction<CommandContext<CommandSourceStack>, ServerPlayer, Integer> playerCommand, BiFunction<CommandContext<CommandSourceStack>, Team, Integer> teamCommand) {
        var owner = getPlayerOrTeam(context.getSource().getPlayer());
        if (owner instanceof ServerPlayer player) return playerCommand.apply(context, player);
        else if (owner instanceof Team team) return teamCommand.apply(context, team);
        else return -1;
    }

    private static Object getPlayerOrTeam(ServerPlayer player) {
        var team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
        return (team.isPresent() && ! team.get().isPlayerTeam()) ? team.get() : player;
    }

    // ####################################
    //            Display Info
    // ####################################

    private static int displayPlayerInfo(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        var message = generateInfoMessage(context.getSource().getLevel(), player.getUUID(), Component.literal("Player: ").append(player.getName()));
        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static int displayTeamInfo(CommandContext<CommandSourceStack> context, Team team) {
        var message = generateInfoMessage(context.getSource().getLevel(), team.getTeamId(), Component.literal("Team: ").append(team.getName()));
        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static Component generateInfoMessage(ServerLevel serverLevel, UUID owner, Component ownerName) {
        var wirelessData = WirelessEnergySavedData.getOrCreate(serverLevel);

        var message = Component.literal("Wireless Energy Network Info ");
        message.append(" (").append(ownerName).append("):\n");
        message.append("  Capacity: " + FormattingUtil.formatNumbers(wirelessData.getEnergyCapacity(owner)) + " EU\n");
        message.append("  Stored: " + FormattingUtil.formatNumbers(wirelessData.getEnergyStored(owner)) + " EU\n");
        message.append("  Input: " + FormattingUtil.formatNumbers(wirelessData.getEnergyInput(owner)) + " EU/t\n");
        message.append("  Output: " + FormattingUtil.formatNumbers(wirelessData.getEnergyOutput(owner)) + " EU/t\n");
        message.append("  Buffered: " + FormattingUtil.formatNumbers(wirelessData.getEnergyBuffered(owner)) + " EU\n");
        message.append("  Active: " + wirelessData.isActive(owner) + "\n");
        message.append("  Wireless Dimensions:");
        for (var dimension : wirelessData.getWirelessDimensions(owner)) message.append("\n    - " + dimension);

        return message;
    }

    // ####################################
    //            Clear data
    // ####################################

    private static int clearPlayerData(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        return clearData(context, player.getUUID());
    }

    private static int clearTeamData(CommandContext<CommandSourceStack> context, Team team) {
        return clearData(context, team.getTeamId());
    }

    private static int clearData(CommandContext<CommandSourceStack> context, UUID uuid) {
        var level = context.getSource().getLevel();
        var wirelessData = WirelessEnergySavedData.getOrCreate(level);
        wirelessData.clearWirelessEnergy(uuid);
        return 1;
    }
}
