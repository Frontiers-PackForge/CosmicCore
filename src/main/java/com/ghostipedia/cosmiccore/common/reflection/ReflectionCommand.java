package com.ghostipedia.cosmiccore.common.reflection;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainRegistry;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementBargain;
import com.ghostipedia.cosmiccore.common.reflection.network.SyncQuakeMovementPacket;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;
import com.ghostipedia.cosmiccore.common.reflection.ui.VoidUIPackets;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

/**
 * Debug/test commands for the Reflection system.
 * /reflection status - Show current erosion, deaths, bargains
 * /reflection add_erosion <amount> - Add erosion
 * /reflection accept <bargain_id> - Accept a bargain
 * /reflection defy <bargain_id> - Defy a bargain
 * /reflection awaken - Force awakening
 * /reflection reset - Reset all reflection data
 */
public class ReflectionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("reflection")
                        .requires(source -> source.hasPermission(2)) // Op level 2
                        .then(Commands.literal("status")
                                .executes(ctx -> showStatus(ctx.getSource())))
                        .then(Commands.literal("add_erosion")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000))
                                        .executes(ctx -> addErosion(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount")))))
                        .then(Commands.literal("accept")
                                .then(Commands.argument("bargain", StringArgumentType.string())
                                        .executes(ctx -> acceptBargain(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "bargain")))))
                        .then(Commands.literal("defy")
                                .then(Commands.argument("bargain", StringArgumentType.string())
                                        .executes(ctx -> defyBargain(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "bargain")))))
                        .then(Commands.literal("awaken")
                                .executes(ctx -> forceAwaken(ctx.getSource())))
                        .then(Commands.literal("reset")
                                .executes(ctx -> resetData(ctx.getSource())))
                        .then(Commands.literal("list_bargains")
                                .executes(ctx -> listBargains(ctx.getSource())))
                        .then(Commands.literal("mirror")
                                .executes(ctx -> openMirror(ctx.getSource()))
                                .then(Commands.argument("bargain", StringArgumentType.string())
                                        .executes(ctx -> openMirrorWithBargain(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "bargain")))))
                        .then(Commands.literal("soulshape")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("shape", StringArgumentType.string())
                                                .executes(ctx -> setSoulShape(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "shape")))))
                                .then(Commands.literal("reset")
                                        .executes(ctx -> resetSoulShape(ctx.getSource())))));
    }

    private static int showStatus(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ReflectionCapability.get(player).ifPresentOrElse(reflection -> {
            source.sendSuccess(() -> Component.literal("=== Reflection Status ==="), false);
            source.sendSuccess(() -> Component.literal("Erosion: " + reflection.getErosion()), false);
            source.sendSuccess(() -> Component.literal("Deaths: " + reflection.getDeathCount()), false);
            source.sendSuccess(() -> Component.literal("Awakened: " + reflection.hasAwakened()), false);
            source.sendSuccess(() -> Component.literal("Threshold: " + reflection.getHighestThresholdSeen() +
                    " (current: " + ReflectionConstants.getThresholdIndex(reflection.getErosion()) + ")"), false);
            source.sendSuccess(
                    () -> Component
                            .literal("Color Tier: " + ReflectionConstants.getSoulColorTier(reflection.getErosion())),
                    false);
            SoulShape shape = reflection.getSoulShape();
            source.sendSuccess(() -> Component.literal("Soul Shape: ").append(shape.getFormattedName()), false);

            source.sendSuccess(() -> Component.literal("Active Bargains:"), false);
            if (reflection.getActiveBargains().isEmpty()) {
                source.sendSuccess(() -> Component.literal("  (none)"), false);
            } else {
                for (ResourceLocation id : reflection.getActiveBargains()) {
                    source.sendSuccess(() -> Component.literal("  - " + id), false);
                }
            }

            if (!reflection.getDefianceScars().isEmpty()) {
                source.sendSuccess(() -> Component.literal("Defiance Scars:"), false);
                for (ResourceLocation id : reflection.getDefianceScars()) {
                    source.sendSuccess(() -> Component.literal("  - " + id), false);
                }
            }

            // Command usage info
            source.sendSuccess(() -> Component.literal("Command Costs:"), false);
            int homeCost = ReflectionConstants.getCommandCost(reflection, "home");
            int backCost = ReflectionConstants.getCommandCost(reflection, "back");
            int homeUsage = reflection.getCommandUsageCount("home");
            int backUsage = reflection.getCommandUsageCount("back");
            source.sendSuccess(() -> Component.literal("  /home: " + homeCost + " erosion (used " + homeUsage + "x)"),
                    false);
            source.sendSuccess(() -> Component.literal("  /back: " + backCost + " erosion (used " + backUsage + "x)"),
                    false);
        }, () -> source.sendFailure(Component.literal("No reflection data found")));

        return 1;
    }

    private static int addErosion(CommandSourceStack source, int amount) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ReflectionCapability.get(player).ifPresentOrElse(reflection -> {
            int oldErosion = reflection.getErosion();
            reflection.addErosion(amount);
            int newErosion = reflection.getErosion();

            source.sendSuccess(() -> Component.literal("Added " + amount + " erosion. Total: " + newErosion), false);

            if (ReflectionConstants.crossedNewThreshold(oldErosion, newErosion)) {
                int threshold = ReflectionConstants.getThresholdIndex(newErosion);
                source.sendSuccess(() -> Component.literal("Crossed threshold " + threshold + "!"), false);
            }
        }, () -> source.sendFailure(Component.literal("No reflection data found")));

        return 1;
    }

    private static int acceptBargain(CommandSourceStack source, String bargainId) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cosmiccore", bargainId);

        return BargainRegistry.get(id).map(bargain -> {
            ReflectionCapability.get(player).ifPresentOrElse(reflection -> {
                if (reflection.hasBargain(id)) {
                    source.sendFailure(Component.literal("Already have this bargain"));
                    return;
                }

                // Calculate and apply cost
                int cost = BargainRegistry.calculateCost(player, bargain);
                reflection.addErosion(cost);
                reflection.acceptBargain(id);

                // Call the bargain's accept handler
                bargain.onAccept(player, bargain.getAnswers().get(0)); // Use first answer for testing

                // Sync to client if this is the quake movement bargain
                if (id.equals(QuakeMovementBargain.INSTANCE.getId())) {
                    CCoreNetwork.sendToPlayer(player, new SyncQuakeMovementPacket(true));
                }

                source.sendSuccess(() -> Component.literal("Accepted bargain: " + bargain.getName().getString() +
                        " (cost: " + cost + " erosion)"), false);
            }, () -> source.sendFailure(Component.literal("No reflection data found")));
            return 1;
        }).orElseGet(() -> {
            source.sendFailure(Component.literal("Unknown bargain: " + id));
            return 0;
        });
    }

    private static int defyBargain(CommandSourceStack source, String bargainId) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cosmiccore", bargainId);

        return BargainRegistry.get(id).map(bargain -> {
            ReflectionCapability.get(player).ifPresentOrElse(reflection -> {
                if (!reflection.hasBargain(id)) {
                    source.sendFailure(Component.literal("Don't have this bargain"));
                    return;
                }

                // Calculate and apply defiance cost
                int cost = BargainRegistry.calculateDefianceCost(player, bargain);
                reflection.addErosion(cost);
                reflection.defy(id);

                // Call the bargain's defy handler
                bargain.onDefy(player);

                // Sync to client if this is the quake movement bargain
                if (id.equals(QuakeMovementBargain.INSTANCE.getId())) {
                    CCoreNetwork.sendToPlayer(player, new SyncQuakeMovementPacket(false));
                }

                source.sendSuccess(() -> Component.literal("Defied bargain: " + bargain.getName().getString() +
                        " (cost: " + cost + " erosion, debuff remains)"), false);
            }, () -> source.sendFailure(Component.literal("No reflection data found")));
            return 1;
        }).orElseGet(() -> {
            source.sendFailure(Component.literal("Unknown bargain: " + id));
            return 0;
        });
    }

    private static int forceAwaken(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ReflectionCapability.get(player).ifPresentOrElse(reflection -> {
            reflection.setAwakened(true);
            source.sendSuccess(() -> Component.literal("Reflection awakened."), false);
        }, () -> source.sendFailure(Component.literal("No reflection data found")));

        return 1;
    }

    private static int resetData(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ReflectionCapability.get(player).ifPresentOrElse(reflection -> {
            // Reset by loading empty data
            if (reflection instanceof ReflectionData data) {
                data.loadTag(new net.minecraft.nbt.CompoundTag());
            }

            // Sync all bargain states to client (all should now be false)
            CCoreNetwork.sendToPlayer(player, new SyncQuakeMovementPacket(false));
            // Add more bargain syncs here as they're implemented

            source.sendSuccess(() -> Component.literal("Reflection data reset."), false);
        }, () -> source.sendFailure(Component.literal("No reflection data found")));

        return 1;
    }

    private static int listBargains(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== Available Bargains ==="), false);

        var allBargains = BargainRegistry.getAll();
        source.sendSuccess(() -> Component.literal("Total registered: " + allBargains.size()), false);

        for (Bargain bargain : allBargains) {
            // Capture in final variable for lambda
            final Bargain b = bargain;
            source.sendSuccess(() -> Component.literal("- " + b.getId().getPath() +
                    " (" + b.getName().getString() + ") [" + b.getTier().name() + "]"), false);
        }

        return 1;
    }

    private static int openMirror(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        // Force awaken if not already
        ReflectionCapability.get(player).ifPresent(reflection -> {
            if (!reflection.hasAwakened()) {
                reflection.setAwakened(true);
            }
        });

        VoidUIPackets.sendOpenVoidScreen(player);
        return 1;
    }

    private static int openMirrorWithBargain(CommandSourceStack source, String bargainId) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cosmiccore", bargainId);

        return BargainRegistry.get(id).map(bargain -> {
            // Force awaken if not already
            ReflectionCapability.get(player).ifPresent(reflection -> {
                if (!reflection.hasAwakened()) {
                    reflection.setAwakened(true);
                }
            });

            VoidUIPackets.sendOpenVoidScreen(player, id);
            source.sendSuccess(() -> Component.literal("Opened mirror with bargain: " + bargain.getName().getString()),
                    false);
            return 1;
        }).orElseGet(() -> {
            source.sendFailure(Component.literal("Unknown bargain: " + id));
            return 0;
        });
    }

    private static int setSoulShape(CommandSourceStack source, String shapeId) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        SoulShape shape = SoulShape.fromId(shapeId);
        ReflectionCapability.get(player).ifPresentOrElse(reflection -> {
            reflection.setSoulShape(shape);
            source.sendSuccess(() -> Component.literal("Soul shape set to: ")
                    .append(shape.getFormattedName()), false);
        }, () -> source.sendFailure(Component.literal("No reflection data found")));

        return 1;
    }

    private static int resetSoulShape(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ReflectionCapability.get(player).ifPresentOrElse(reflection -> {
            reflection.setSoulShape(SoulShape.UNSHAPED);
            source.sendSuccess(() -> Component.literal("Soul shape reset to Unshaped."), false);
        }, () -> source.sendFailure(Component.literal("No reflection data found")));

        return 1;
    }
}
