////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by FernFlower decompiler)
////
//
//package com.ghostipedia.cosmiccore.common.commands.TMP;
//
//import com.mojang.authlib.GameProfile;
//import com.mojang.brigadier.CommandDispatcher;
//import com.mojang.brigadier.arguments.StringArgumentType;
//import com.mojang.brigadier.builder.LiteralArgumentBuilder;
//import com.mojang.brigadier.builder.RequiredArgumentBuilder;
//import com.mojang.brigadier.context.CommandContext;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import dev.architectury.platform.Platform;
//import dev.ftb.mods.ftbteams.FTBTeamsAPIImpl;
//import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
//import dev.ftb.mods.ftbteams.api.Team;
//import dev.ftb.mods.ftbteams.api.TeamRank;
//import dev.ftb.mods.ftbteams.api.event.TeamEvent;
//import dev.ftb.mods.ftbteams.api.event.TeamInfoEvent;
//import dev.ftb.mods.ftbteams.api.property.TeamPropertyArgument;
//
//import java.util.Collection;
//import java.util.UUID;
//import java.util.function.Consumer;
//import java.util.function.Predicate;
//
//import dev.ftb.mods.ftbteams.data.*;
//import net.minecraft.commands.CommandSourceStack;
//import net.minecraft.commands.Commands;
//import net.minecraft.commands.arguments.GameProfileArgument;
//import net.minecraft.network.chat.ClickEvent;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.HoverEvent;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.network.chat.HoverEvent.Action;
//import net.minecraft.server.level.ServerPlayer;
//import org.jetbrains.annotations.Nullable;
//
//import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
//
//public class FTBTeamsCommands {
//    public FTBTeamsCommands() {
//    }
//
//    private Predicate<CommandSourceStack> requiresOPorSP() {
//        return (source) -> source.getServer().isSingleplayer() || source.hasPermission(2);
//    }
//
//    private RequiredArgumentBuilder<CommandSourceStack, TeamArgumentProvider> teamArg() {
//        return Commands.argument("team", TeamArgument.create());
//    }
//
//    private String string(CommandContext<?> context, String name) {
//        return StringArgumentType.getString(context, name);
//    }
//
//    private boolean hasNoParty(CommandSourceStack source) {
//        return source.getEntity() instanceof ServerPlayer ? (Boolean) FTBTeamsAPI.api().getManager().getTeamForPlayerID(source.getEntity().getUUID()).map((team) -> !team.isPartyTeam()).orElse(false) : false;
//    }
//
//    private boolean hasParty(CommandSourceStack source, TeamRank rank) {
//        if (source.getEntity() instanceof ServerPlayer) {
//            UUID playerId = source.getEntity().getUUID();
//            return (Boolean) FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerId).map((team) -> team.isPartyTeam() && team.getRankForPlayer(playerId).isAtLeast(rank)).orElse(false);
//        } else {
//            return false;
//        }
//    }
//
//    private Team getTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
//        ServerPlayer player = (context.getSource()).getPlayerOrException();
//        return (Team) FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElseThrow(() -> TeamArgument.TEAM_NOT_FOUND.create(player.getUUID()));
//    }
//
//    private PartyTeam getPartyTeam(CommandContext<CommandSourceStack> context, TeamRank minRank) throws CommandSyntaxException {
//        ServerPlayer player = (context.getSource()).getPlayerOrException();
//        Team team = (Team) FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElseThrow(() -> TeamArgument.TEAM_NOT_FOUND.create(player.getUUID()));
//        if (team instanceof PartyTeam partyTeam) {
//            if (!partyTeam.getRankForPlayer(player.getUUID()).isAtLeast(minRank)) {
//                throw TeamArgument.CANT_EDIT.create(team.getName());
//            } else {
//                return partyTeam;
//            }
//        } else {
//            throw TeamArgument.NOT_IN_PARTY.create();
//        }
//    }
//
//    private Team teamArg(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
//        return TeamArgument.get(context, "team");
//    }
//
//    private Team teamArg(CommandContext<CommandSourceStack> context, Predicate<Team> predicate) throws CommandSyntaxException {
//        Team team = this.teamArg(context);
//        if (!predicate.test(team)) {
//            throw TeamArgument.TEAM_NOT_FOUND.create(team.getName());
//        } else {
//            return team;
//        }
//    }
//
//    private ServerTeam serverTeamArg(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
//        return (ServerTeam) this.teamArg(context, Team::isServerTeam);
//    }
//
//    private PartyTeam partyTeamArg(CommandContext<CommandSourceStack> context, TeamRank rank) throws CommandSyntaxException {
//        PartyTeam team = (PartyTeam) this.teamArg(context, Team::isPartyTeam);
//        if (rank != TeamRank.NONE && !team.getRankForPlayer((context.getSource()).getPlayerOrException().getUUID()).isAtLeast(rank)) {
//            throw TeamArgument.NOT_INVITED.create(team.getName());
//        } else {
//            return team;
//        }
//    }
//
//    private int tryCreateParty(CommandSourceStack source, String partyName) throws CommandSyntaxException {
//        if (FTBTeamsAPIImpl.INSTANCE.isPartyCreationFromAPIOnly()) {
//            throw TeamArgument.API_OVERRIDE.create();
//        } else {
//            return (Integer) TeamManagerImpl.INSTANCE.createParty(source.getPlayerOrException(), partyName).getLeft();
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
//        dispatcher.register((LiteralArgumentBuilder) literal("ftbteams")
//                .then(Commands.literal("party")
//                        .then(((LiteralArgumentBuilder) Commands.literal("create")
//                                .requires(this::hasNoParty))
//                                .then(Commands.argument("name", StringArgumentType.greedyString())
//                                        .executes((ctx) -> this.tryCreateParty(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))
//                                .executes((ctx) -> this.tryCreateParty((CommandSourceStack) ctx.getSource(), "")))
//                        .then(((LiteralArgumentBuilder) Commands.literal("join")
//                                .requires(this::hasNoParty))
//                                .then(this.teamArg().executes((ctx) -> this.partyTeamArg(ctx, TeamRank.INVITED)
//                                        .join((ctx.getSource()).getPlayerOrException()))))
//                        .then(((LiteralArgumentBuilder) Commands.literal("decline")
//                                .requires(this::hasNoParty))
//                                .then(this.teamArg()
//                                        .executes((ctx) -> this.partyTeamArg(ctx, TeamRank.INVITED)
//                                            .declineInvitation(ctx.getSource()))))
//                        .then(((LiteralArgumentBuilder) Commands.literal("leave")
//                                .requires((source) -> this.hasParty(source, TeamRank.MEMBER)))
//                                .executes((ctx) -> this.getPartyTeam(ctx, TeamRank.MEMBER)
//                                        .leave(((CommandSourceStack) ctx.getSource()).getPlayerOrException().getUUID())))
//                        .then(((LiteralArgumentBuilder) Commands.literal("invite")
//                                .requires((source) -> this.hasParty(source, TeamRank.OFFICER)))
//                                .then(Commands.argument("players", GameProfileArgument.gameProfile())
//                                        .executes((ctx) -> this.getPartyTeam(ctx, TeamRank.OFFICER)
//                                                .invite((ctx.getSource()).getPlayerOrException(), GameProfileArgument.getGameProfiles(ctx, "players")))))
//                        .then(((LiteralArgumentBuilder) Commands.literal("kick")
//                                .requires((source) -> this.hasParty(source, TeamRank.OFFICER)))
//                                .then(Commands.argument("players", GameProfileArgument.gameProfile())
//                                        .executes((ctx) -> this.getPartyTeam(ctx, TeamRank.OFFICER)
//                                                .kick(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "players")))))
//                        .then(((LiteralArgumentBuilder) Commands.literal("transfer_ownership")
//                                .requires((source) -> this.hasParty(source, TeamRank.OWNER)))
//                                .then(Commands.argument("player_id", GameProfileArgument.gameProfile())
//                                        .executes((ctx) -> this.partyTeamArg(ctx, TeamRank.OWNER)
//                                                .transferOwnership(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "player_id")))))
//                        .then(((LiteralArgumentBuilder) Commands.literal("transfer_ownership_for")
//                                .requires(this.requiresOPorSP()))
//                                .then(this.teamArg()
//                                    .then(Commands.argument("player_id", GameProfileArgument.gameProfile())
//                                            .executes((ctx) -> this.partyTeamArg(ctx, TeamRank.NONE)
//                                                    .transferOwnership(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "player_id"))))))
//                        .then(((LiteralArgumentBuilder) Commands.literal("settings")
//                                .requires((source) -> this.hasParty(source, TeamRank.OWNER)))
//                                .then(((RequiredArgumentBuilder) Commands.argument("key", TeamPropertyArgument.create())
//                                        .then(Commands.argument("value", StringArgumentType.greedyString())
//                                                .executes((ctx) -> this.getPartyTeam(ctx, TeamRank.OWNER)
//                                                        .settings(ctx.getSource(), TeamPropertyArgument.get(ctx, "key"), this.string(ctx, "value")))))
//                                        .executes((ctx) -> this.getPartyTeam(ctx, TeamRank.OWNER)
//                                                .settings((CommandSourceStack) ctx.getSource(), TeamPropertyArgument.get(ctx, "key"), ""))))
//                        .then(((LiteralArgumentBuilder) Commands.literal("settings_for")
//                                .requires(this.requiresOPorSP()))
//                                .then(this.teamArg()
//                                        .then(((RequiredArgumentBuilder) Commands.argument("key", TeamPropertyArgument.create())
//                                                .then(Commands.argument("value", StringArgumentType.greedyString())
//                                                        .executes((ctx) -> this.partyTeamArg(ctx, TeamRank.OFFICER)
//                                                                .settings(ctx.getSource(), TeamPropertyArgument.get(ctx, "key"), this.string(ctx, "value")))))
//                                                .executes((ctx) -> this.partyTeamArg(ctx, TeamRank.OFFICER)
//                                                        .settings((CommandSourceStack) ctx.getSource(), TeamPropertyArgument.get(ctx, "key"), "")))))
//                        .then(((LiteralArgumentBuilder) Commands.literal("allies")
//                                .requires((source) -> this.hasParty(source, TeamRank.MEMBER)))
//                                .then(((LiteralArgumentBuilder) Commands.literal("add")
//                                        .requires((source) -> this.hasParty(source, TeamRank.OFFICER)))
//                                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
//                                                .executes((ctx) -> this.getPartyTeam(ctx, TeamRank.OFFICER)
//                                                        .addAlly(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "player")))))
//                                .then(((LiteralArgumentBuilder) Commands.literal("remove")
//                                        .requires((source) -> this.hasParty(source, TeamRank.OFFICER)))
//                                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
//                                                .executes((ctx) -> this.getPartyTeam(ctx, TeamRank.OFFICER)
//                                                        .removeAlly(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "player")))))
//                                .then(((LiteralArgumentBuilder) Commands.literal("list")
//                                        .requires((source) -> this.hasParty(source, TeamRank.MEMBER)))
//                                        .executes((ctx) -> this.getPartyTeam(ctx, TeamRank.MEMBER)
//                                                .listAllies((CommandSourceStack) ctx.getSource())))))
//                .then(((LiteralArgumentBuilder) Commands.literal("server")
//                        .requires(this.requiresOPorSP()))
//                        .then(Commands.literal("create")
//                                .then(Commands.argument("name", StringArgumentType.greedyString())
//                                        .executes((ctx) -> (Integer) TeamManagerImpl.INSTANCE
//                                                .createServer(ctx.getSource(), this.string(ctx, "name")).getLeft())))
//                        .then(Commands.literal("delete")
//                                .then(this.teamArg()
//                                        .executes((ctx) -> this.serverTeamArg(ctx)
//                                                .delete(ctx.getSource()))))
//                        .then(Commands.literal("settings")
//                                .then(this.teamArg()
//                                        .then(((RequiredArgumentBuilder) Commands.argument("key", TeamPropertyArgument.create())
//                                                .then(Commands.argument("value", StringArgumentType.greedyString())
//                                                        .executes((ctx) -> this.serverTeamArg(ctx)
//                                                                .settings(ctx.getSource(), TeamPropertyArgument.get(ctx, "key"), this.string(ctx, "value")))))
//                                                .executes((ctx) -> this.serverTeamArg(ctx)
//                                                        .settings((CommandSourceStack) ctx.getSource(), TeamPropertyArgument.get(ctx, "key"), ""))))))
//                .then(Commands.literal("msg")
//                        .then(Commands.argument("text", StringArgumentType.greedyString())
//                                .executes((ctx) -> {
//                                    this.getTeam(ctx).sendMessage((ctx.getSource()).getPlayerOrException().getUUID(), StringArgumentType.getString(ctx, "text"));
//                                    return 1;
//                                })))
//                .then(((LiteralArgumentBuilder) Commands.literal("info")
//                        .then(Commands.literal("server_id")
//                                .executes((ctx) -> this.serverId(ctx.getSource()))))
//                        .then(this.teamArg()
//                                .executes((ctx) -> this.info(ctx.getSource(), this.teamArg(ctx))))
//                        .executes((ctx) -> this.info((CommandSourceStack) ctx.getSource(), this.getTeam(ctx))))
//                .then(((LiteralArgumentBuilder) Commands.literal("list")
//                        .executes((ctx) -> this.list(ctx.getSource(), (Predicate) null)))
//                        .then(Commands.literal("parties")
//                                .executes((ctx) -> this.list(ctx.getSource(), Team::isPartyTeam)))
//                        .then(Commands.literal("server_teams")
//                                .executes((ctx) -> this.list(ctx.getSource(), Team::isServerTeam)))
//                        .then(Commands.literal("players")
//                                .executes((ctx) -> this.list(ctx.getSource(), Team::isPlayerTeam))))
//                .then(((LiteralArgumentBuilder) Commands.literal("force-disband")
//                        .requires((source) -> source.hasPermission(2)))
//                        .then(this.teamArg()
//                                .executes((ctx) -> this.partyTeamArg(ctx, TeamRank.NONE).forceDisband(ctx.getSource())))));
//
//        if (Platform.isDevelopmentEnvironment()) {
//            dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) Commands.literal("ftbteams_add_fake_player")
//                    .requires((source) -> source.hasPermission(2)))
//                    .then(Commands.argument("profile", GameProfileArgument.gameProfile())
//                            .executes((ctx) -> this.addFakePlayer(GameProfileArgument.getGameProfiles(ctx, "profile")))));
//        }
//
//    }
//
//    private int info(CommandSourceStack source, Team team) {
//        team.getTeamInfo().forEach((line) -> source.sendSuccess(() -> line, false));
//        ((Consumer) TeamEvent.INFO.invoker()).accept(new TeamInfoEvent(team, source));
//        return 1;
//    }
//
//    private int serverId(CommandSourceStack source) {
//        UUID managerId = FTBTeamsAPI.api().getManager().getId();
//        MutableComponent component = Component.literal("Server ID: " + managerId);
//        component.withStyle((style) -> style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.literal("Click to copy"))));
//        component.withStyle((style) -> style.withClickEvent(new ClickEvent(net.minecraft.network.chat.ClickEvent.Action.COPY_TO_CLIPBOARD, managerId.toString())));
//        source.sendSuccess(() -> component, false);
//        return 1;
//    }
//
//    private int list(CommandSourceStack source, @Nullable Predicate<Team> predicate) {
//        MutableComponent list = Component.literal("");
//        boolean first = true;
//
//        for (Team team : FTBTeamsAPI.api().getManager().getTeams()) {
//            if (predicate == null || predicate.test(team)) {
//                if (first) {
//                    first = false;
//                } else {
//                    list.append(", ");
//                }
//
//                list.append(team.getName());
//            }
//        }
//
//        Component msg = Component.translatable("ftbteams.list", new Object[]{first ? Component.translatable("ftbteams.info.owner.none") : list});
//        source.sendSuccess(() -> msg, false);
//        return 1;
//    }
//
//    private int addFakePlayer(Collection<GameProfile> profiles) {
//        for (GameProfile profile : profiles) {
//            TeamManagerImpl.INSTANCE.playerLoggedIn((ServerPlayer) null, profile.getId(), profile.getName());
//        }
//
//        return 1;
//    }
//}
