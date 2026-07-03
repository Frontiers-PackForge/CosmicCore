package com.ghostipedia.cosmiccore.common.mirror.deed;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;

public final class DeedCommand {

    private DeedCommand() {}

    private static final SuggestionProvider<CommandSourceStack> DEED_IDS = (ctx, builder) -> SharedSuggestionProvider
            .suggestResource(DeedRegistry.all().stream().map(Deed::id), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deed")
                .then(Commands.literal("grant")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .suggests(DEED_IDS)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                    if (DeedRegistry.get(id) == null) {
                                        ctx.getSource().sendFailure(
                                                Component.literal("Unknown deed " + id));
                                        return 0;
                                    }
                                    boolean granted = DeedsAPI.grantCoil(player, id);
                                    ctx.getSource().sendSuccess(() -> Component.literal(granted ?
                                            "Coil granted for " + id : "Deed already pending or woven: " + id), true);
                                    return granted ? 1 : 0;
                                })))
                .then(Commands.literal("weave")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .suggests(DEED_IDS)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                    DeedLedger.WovenEcho echo = DeedsAPI.weave(player, id, true);
                                    ctx.getSource().sendSuccess(() -> Component.literal(echo != null ?
                                            "Woven " + id + " as echo #" + echo.claimIndex() : "Already woven: " + id),
                                            true);
                                    return echo != null ? 1 : 0;
                                })))
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String teamKey = DeedTeams.teamKey(player);
                            DeedLedger ledger = DeedLedger.get(player.getServer());
                            var woven = ledger.wovenOf(teamKey);
                            var pending = ledger.pendingOf(teamKey);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Team " + teamKey + ": " + woven.size() + " woven, " + pending.size() + " pending"),
                                    false);
                            for (DeedLedger.WovenEcho echo : woven) {
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                        "  #" + echo.claimIndex() + " " + echo.deedId()), false);
                            }
                            for (ResourceLocation id : pending) {
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("  pending " + id), false);
                            }
                            return woven.size();
                        }))
                .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String teamKey = DeedTeams.teamKey(player);
                            DeedLedger.get(player.getServer()).reset(teamKey);
                            DeedsAPI.syncTeam(player.getServer(), teamKey);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Deed ledger reset for team " + teamKey), true);
                            return 1;
                        })));
    }
}
