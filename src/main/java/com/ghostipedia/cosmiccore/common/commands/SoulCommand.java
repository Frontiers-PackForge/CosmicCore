package com.ghostipedia.cosmiccore.common.commands;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetworkSavedData;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import com.ghostipedia.cosmiccore.common.commands.argument.SoulTypeArgument;
import com.ghostipedia.cosmiccore.common.item.SoulNetworkReaderItem;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbteams.data.TeamArgument;

import java.util.UUID;

import static net.minecraft.commands.Commands.*;

public class SoulCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("soul")
                        .requires(source -> source.hasPermission(LEVEL_ADMINS))
                        .then(literal("player")
                                .then(argument("player", EntityArgument.player())
                                        .then(literal("info").executes(ctx -> displayInfo(ctx,
                                                EntityArgument.getPlayer(ctx, "player").getUUID())))
                                        .then(literal("reset").executes(ctx -> resetNetwork(ctx,
                                                EntityArgument.getPlayer(ctx, "player").getUUID())))
                                        .then(literal("add").then(argument("type", SoulTypeArgument.soulType())
                                                .then(argument("amount", IntegerArgumentType.integer())
                                                        .executes(ctx -> addSouls(ctx,
                                                                EntityArgument.getPlayer(ctx, "player").getUUID(),
                                                                SoulTypeArgument.get(ctx, "type"),
                                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                                        .then(literal("syphon").then(argument("type", SoulTypeArgument.soulType())
                                                .then(argument("amount", IntegerArgumentType.integer())
                                                        .executes(ctx -> syphon(ctx,
                                                                EntityArgument.getPlayer(ctx, "player").getUUID(),
                                                                SoulTypeArgument.get(ctx, "type"),
                                                                IntegerArgumentType.getInteger(ctx, "amount"))))))))
                        .then(literal("team")
                                .then(argument("team", TeamArgument.create())
                                        .then(literal("info").executes(
                                                ctx -> displayInfo(ctx, TeamArgument.get(ctx, "team").getTeamId())))
                                        .then(literal("reset").executes(
                                                ctx -> resetNetwork(ctx, TeamArgument.get(ctx, "team").getTeamId())))
                                        .then(literal("add").then(argument("type", SoulTypeArgument.soulType())
                                                .then(argument("amount", IntegerArgumentType.integer())
                                                        .executes(ctx -> addSouls(ctx,
                                                                TeamArgument.get(ctx, "team").getTeamId(),
                                                                SoulTypeArgument.get(ctx, "type"),
                                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                                        .then(literal("syphon").then(argument("type", SoulTypeArgument.soulType())
                                                .then(argument("amount", IntegerArgumentType.integer())
                                                        .executes(ctx -> syphon(ctx,
                                                                TeamArgument.get(ctx, "team").getTeamId(),
                                                                SoulTypeArgument.get(ctx, "type"),
                                                                IntegerArgumentType.getInteger(ctx, "amount")))))))));
    }

    private static int displayInfo(CommandContext<CommandSourceStack> context, UUID owner) {
        var network = SoulNetworkSavedData.getSoulNetwork(context.getSource().getLevel(), owner);
        context.getSource().sendSuccess(() -> SoulNetworkReaderItem.displaySoulNetworkInfo(network), false);
        return 1;
    }

    private static int resetNetwork(CommandContext<CommandSourceStack> context, UUID owner) {
        var network = SoulNetworkSavedData.getSoulNetwork(context.getSource().getLevel(), owner);
        network.reset();
        context.getSource().sendSuccess(() -> Component.translatable("gui.cosmiccore.soul.reset"), false);
        return 1;
    }

    private static int addSouls(CommandContext<CommandSourceStack> context, UUID owner, SoulType type, int amount) {
        var network = SoulNetworkSavedData.getSoulNetwork(context.getSource().getLevel(), owner);
        network.add(new SoulStack(type, amount), Integer.MAX_VALUE, Integer.MAX_VALUE, false);
        context.getSource().sendSuccess(
                () -> Component.translatable("gui.cosmiccore.soul.add", amount, type.getSerializedName()), false);
        return 1;
    }

    private static int syphon(CommandContext<CommandSourceStack> context, UUID owner, SoulType type, int amount) {
        var network = SoulNetworkSavedData.getSoulNetwork(context.getSource().getLevel(), owner);
        network.syphon(new SoulStack(type, amount), false);
        context.getSource().sendSuccess(
                () -> Component.translatable("gui.cosmiccore.soul.remove", amount, type.getSerializedName()), false);
        return 1;
    }
}
