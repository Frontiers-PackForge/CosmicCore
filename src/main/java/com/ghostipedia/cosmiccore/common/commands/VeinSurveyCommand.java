package com.ghostipedia.cosmiccore.common.commands;

import com.ghostipedia.cosmiccore.common.worldgen.survey.VeinSurveyUtil;
import com.ghostipedia.cosmiccore.common.worldgen.survey.VeinSurveyUtil.VeinInfo;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.*;

public class VeinSurveyCommand {

    private static final int DEFAULT_RADIUS = 2000;
    private static final int MAX_RADIUS = 10000;
    private static final int MAX_RESULTS_DISPLAY = 10;

    private static final SuggestionProvider<CommandSourceStack> VEIN_SUGGESTIONS = (context, builder) -> {
        var level = context.getSource().getLevel();
        var layers = WorldGeneratorUtils.WORLD_GEN_LAYERS.values().stream()
                .filter(l -> l.isApplicableForLevel(level.dimension().location()))
                .toList();

        for (var layer : layers) {
            for (String veinType : VeinSurveyUtil.getAvailableVeinTypes(layer)) {
                if (veinType.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(veinType);
                }
            }
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("survey")
                        .requires(source -> source.hasPermission(LEVEL_ALL))
                        .executes(ctx -> surveyFromPlayer(ctx, DEFAULT_RADIUS, null))
                        .then(argument("radius", IntegerArgumentType.integer(100, MAX_RADIUS))
                                .executes(ctx -> surveyFromPlayer(ctx,
                                        IntegerArgumentType.getInteger(ctx, "radius"), null))
                                .then(argument("filter", StringArgumentType.word())
                                        .suggests(VEIN_SUGGESTIONS)
                                        .executes(ctx -> surveyFromPlayer(ctx,
                                                IntegerArgumentType.getInteger(ctx, "radius"),
                                                StringArgumentType.getString(ctx, "filter")))))
                        .then(literal("nearest")
                                .executes(ctx -> findNearest(ctx, null))
                                .then(argument("filter", StringArgumentType.word())
                                        .suggests(VEIN_SUGGESTIONS)
                                        .executes(ctx -> findNearest(ctx,
                                                StringArgumentType.getString(ctx, "filter")))))
                        .then(literal("at")
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .then(argument("radius", IntegerArgumentType.integer(100, MAX_RADIUS))
                                                .executes(ctx -> surveyFromPos(ctx,
                                                        BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                                        IntegerArgumentType.getInteger(ctx, "radius"),
                                                        null))
                                                .then(argument("filter", StringArgumentType.word())
                                                        .suggests(VEIN_SUGGESTIONS)
                                                        .executes(ctx -> surveyFromPos(ctx,
                                                                BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                                                IntegerArgumentType.getInteger(ctx, "radius"),
                                                                StringArgumentType.getString(ctx, "filter")))))))
                        .then(literal("types")
                                .executes(VeinSurveyCommand::listVeinTypes)));
    }

    private static int surveyFromPlayer(CommandContext<CommandSourceStack> ctx, int radius, String filter) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("cosmiccore.survey.command.player_only"));
            return 0;
        }
        return performSurvey(ctx, player.blockPosition(), radius, filter);
    }

    private static int surveyFromPos(CommandContext<CommandSourceStack> ctx, BlockPos pos, int radius, String filter) {
        return performSurvey(ctx, pos, radius, filter);
    }

    private static int performSurvey(CommandContext<CommandSourceStack> ctx, BlockPos center, int radius,
                                     String filter) {
        ServerLevel level = ctx.getSource().getLevel();

        ctx.getSource().sendSuccess(() -> Component.translatable("cosmiccore.survey.command.scanning", radius)
                .withStyle(ChatFormatting.YELLOW), false);

        IWorldGenLayer layer = WorldGeneratorUtils.WORLD_GEN_LAYERS.values().stream()
                .filter(l -> l.isApplicableForLevel(level.dimension().location()))
                .findFirst()
                .orElse(null);

        List<VeinInfo> veins = VeinSurveyUtil.surveyVeins(level, center, radius, layer);

        if (filter != null && !filter.isEmpty()) {
            String filterLower = filter.toLowerCase();
            veins = veins.stream()
                    .filter(v -> v.getVeinName().toLowerCase().contains(filterLower))
                    .toList();
        }

        if (veins.isEmpty()) {
            ctx.getSource()
                    .sendSuccess(() -> (filter != null ?
                            Component.translatable("cosmiccore.survey.no_veins.filtered", filter) :
                            Component.translatable("cosmiccore.survey.no_veins"))
                            .withStyle(ChatFormatting.RED), false);
            return 0;
        }

        final List<VeinInfo> finalVeins = veins;
        ctx.getSource().sendSuccess(() -> Component.literal("")
                .append(Component.translatable("cosmiccore.survey.command.results").withStyle(ChatFormatting.GOLD))
                .append("\n")
                .append(Component.translatable("cosmiccore.survey.found", finalVeins.size(), radius)
                        .withStyle(ChatFormatting.GRAY)),
                false);

        Map<String, Long> veinCounts = veins.stream()
                .collect(Collectors.groupingBy(VeinInfo::getVeinName, Collectors.counting()));

        MutableComponent typeSummary = Component.literal("\n")
                .append(Component.translatable("cosmiccore.survey.command.vein_types").withStyle(ChatFormatting.GRAY));
        boolean first = true;
        for (var entry : veinCounts.entrySet()) {
            if (!first) typeSummary.append(", ");
            first = false;
            typeSummary.append(Component.literal(entry.getKey() + " (" + entry.getValue() + ")")
                    .withStyle(ChatFormatting.WHITE));
        }
        ctx.getSource().sendSuccess(() -> typeSummary, false);

        ctx.getSource().sendSuccess(() -> Component.literal("\n")
                .append(Component.translatable("cosmiccore.survey.command.nearest_veins")
                        .withStyle(ChatFormatting.YELLOW)),
                false);

        int displayCount = Math.min(MAX_RESULTS_DISPLAY, veins.size());
        for (int i = 0; i < displayCount; i++) {
            sendVeinEntry(ctx, veins.get(i), center, i + 1);
        }

        if (veins.size() > MAX_RESULTS_DISPLAY) {
            int remaining = veins.size() - MAX_RESULTS_DISPLAY;
            ctx.getSource().sendSuccess(() -> Component.translatable("cosmiccore.survey.command.more", remaining)
                    .withStyle(ChatFormatting.GRAY), false);
        }

        return veins.size();
    }

    private static int findNearest(CommandContext<CommandSourceStack> ctx, String filter) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("cosmiccore.survey.command.player_only"));
            return 0;
        }

        ServerLevel level = ctx.getSource().getLevel();
        BlockPos center = player.blockPosition();

        IWorldGenLayer layer = WorldGeneratorUtils.WORLD_GEN_LAYERS.values().stream()
                .filter(l -> l.isApplicableForLevel(level.dimension().location()))
                .findFirst()
                .orElse(null);

        Optional<VeinInfo> nearest = VeinSurveyUtil.findNearestVein(level, center, MAX_RADIUS, layer, filter);

        if (nearest.isEmpty()) {
            ctx.getSource()
                    .sendSuccess(() -> (filter != null ?
                            Component.translatable("cosmiccore.survey.no_veins.filtered", filter) :
                            Component.translatable("cosmiccore.survey.no_veins"))
                            .withStyle(ChatFormatting.RED), false);
            return 0;
        }

        VeinInfo vein = nearest.get();
        ctx.getSource()
                .sendSuccess(() -> (filter != null ?
                        Component.translatable("cosmiccore.survey.nearest_vein.filtered", filter) :
                        Component.translatable("cosmiccore.survey.nearest_vein"))
                        .withStyle(ChatFormatting.GOLD), false);
        sendVeinEntry(ctx, vein, center, 1);

        return 1;
    }

    private static void sendVeinEntry(CommandContext<CommandSourceStack> ctx, VeinInfo vein, BlockPos from, int index) {
        BlockPos pos = vein.center();
        int distance = vein.horizontalDistanceFrom(from);
        String direction = vein.directionFrom(from);

        String tpCommand = "/tp @s " + pos.getX() + " ~ " + pos.getZ();

        MutableComponent entry = Component.literal(index + ". ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(vein.getVeinName()).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(distance + "m " + direction).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal("X:" + pos.getX() + " Z:" + pos.getZ())
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.GREEN)
                                .withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tpCommand))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("cosmiccore.survey.click_tp")))))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));

        ctx.getSource().sendSuccess(() -> entry, false);
    }

    private static int listVeinTypes(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();

        IWorldGenLayer layer = WorldGeneratorUtils.WORLD_GEN_LAYERS.values().stream()
                .filter(l -> l.isApplicableForLevel(level.dimension().location()))
                .findFirst()
                .orElse(null);

        List<String> types = VeinSurveyUtil.getAvailableVeinTypes(layer);

        if (types.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("cosmiccore.survey.command.no_veins_dimension")
                    .withStyle(ChatFormatting.RED), false);
            return 0;
        }

        ctx.getSource()
                .sendSuccess(() -> Component.translatable("cosmiccore.survey.command.available_types", types.size())
                        .withStyle(ChatFormatting.GOLD), false);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            sb.append(types.get(i));
            if ((i + 1) % 4 == 0 || i == types.size() - 1) {
                final String line = sb.toString();
                ctx.getSource().sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.WHITE), false);
                sb.setLength(0);
            } else {
                sb.append(", ");
            }
        }

        return types.size();
    }
}
