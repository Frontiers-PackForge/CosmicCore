package com.ghostipedia.cosmiccore.common.mirror.deed;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
                                        ctx.getSource().sendFailure(Component.translatableWithFallback(
                                                "command.cosmiccore.deed.unknown", "Unknown deed %s", id.toString()));
                                        return 0;
                                    }
                                    boolean granted = DeedsAPI.grantCoil(player, id);
                                    ctx.getSource().sendSuccess(() -> granted ?
                                            Component.translatableWithFallback("command.cosmiccore.deed.granted",
                                                    "Coil granted for %s", id.toString()) :
                                            Component.translatableWithFallback("command.cosmiccore.deed.duplicate",
                                                    "Deed already pending or woven: %s", id.toString()),
                                            true);
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
                                    ctx.getSource().sendSuccess(() -> echo !=
                                            null ? Component.translatableWithFallback("command.cosmiccore.deed.woven",
                                                    "Woven %s as echo #%s", id.toString(),
                                                    String.valueOf(echo.claimIndex())) :
                                                    Component.translatableWithFallback(
                                                            "command.cosmiccore.deed.already_woven",
                                                            "Already woven: %s",
                                                            id.toString()),
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
                .then(Commands.literal("fill")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("count", IntegerArgumentType.integer(0, 84))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int target = IntegerArgumentType.getInteger(ctx, "count");
                                    String teamKey = DeedTeams.teamKey(player);
                                    DeedLedger ledger = DeedLedger.get(player.getServer());
                                    int have = 0;
                                    for (DeedLedger.WovenEcho echo : ledger.wovenOf(teamKey)) {
                                        if (!echo.deedId().equals(DeedRegistry.THE_ADDRESS.id())) have++;
                                    }
                                    int added = 0;
                                    for (Deed deed : DeedRegistry.all()) {
                                        if (have + added >= target) break;
                                        if (!deed.id().getPath().startsWith("dev/")) continue;
                                        if (ledger.isWoven(teamKey, deed.id())) continue;
                                        ledger.weave(teamKey, deed.id(), player.getUUID(),
                                                player.level().getGameTime(), null);
                                        added++;
                                    }
                                    DeedsAPI.syncTeam(player.getServer(), teamKey);
                                    int result = added;
                                    int total = have + added;
                                    ctx.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                            "command.cosmiccore.deed.filled", "Dev-filled %s deeds (now %s)",
                                            String.valueOf(result), String.valueOf(total)), true);
                                    return result;
                                })))
                .then(Commands.literal("coils")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 84))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int target = IntegerArgumentType.getInteger(ctx, "count");
                                    String teamKey = DeedTeams.teamKey(player);
                                    DeedLedger ledger = DeedLedger.get(player.getServer());
                                    int added = 0;
                                    for (Deed deed : DeedRegistry.all()) {
                                        if (added >= target) break;
                                        if (!deed.id().getPath().startsWith("dev/")) continue;
                                        if (ledger.isWoven(teamKey, deed.id())) continue;
                                        if (ledger.grantCoil(teamKey, deed.id())) added++;
                                    }
                                    DeedsAPI.syncTeam(player.getServer(), teamKey);
                                    int result = added;
                                    ctx.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                            "command.cosmiccore.deed.coiled", "Granted %s dev coils",
                                            String.valueOf(result)), true);
                                    return result;
                                })))
                .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String teamKey = DeedTeams.teamKey(player);
                            DeedLedger.get(player.getServer()).reset(teamKey);
                            DeedsAPI.syncTeam(player.getServer(), teamKey);
                            ctx.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                    "command.cosmiccore.deed.reset", "Deed ledger reset for team %s", teamKey), true);
                            return 1;
                        })));
    }
}
