package com.ghostipedia.cosmiccore.common.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.commands.Commands.*;

public class ExportRegistryCommand {

    private static final int ENTRIES_PER_FILE = 1000;
    private static final long CONFIRM_WINDOW_MS = 5000;
    private static final ConcurrentHashMap<UUID, Long> PENDING_CONFIRMS = new ConcurrentHashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("exportregistry")
                        .requires(source -> source.hasPermission(4))
                        .executes(ExportRegistryCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getEntity();
        UUID id = player != null ? player.getUUID() : UUID.nameUUIDFromBytes("console".getBytes());

        long now = System.currentTimeMillis();
        Long lastAttempt = PENDING_CONFIRMS.get(id);

        if (lastAttempt == null || (now - lastAttempt) > CONFIRM_WINDOW_MS) {
            PENDING_CONFIRMS.put(id, now);
            source.sendSuccess(() -> Component.literal("⚠⚠⚠ WARNING ⚠⚠⚠")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                    .append(Component.literal(
                            "\nYou are about to trigger a registry dump into a folder within your .minecraft directory of ALL items and fluids in the game. This may consume a large volume of computer storage.")
                            .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(
                            "\nThis is a developer tool for workspace integration with grep/searching tools. Use with caution")
                            .withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("\n⚠⚠⚠ WARNING ⚠⚠⚠")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .append(Component.literal("\nRun again within 5 seconds to confirm.")
                            .withStyle(ChatFormatting.GREEN)),
                    false);
            return 0;
        }

        PENDING_CONFIRMS.remove(id);
        source.sendSuccess(() -> Component.literal("Exporting registries...")
                .withStyle(ChatFormatting.GREEN), true);

        Path gameDir = source.getServer().getServerDirectory();
        Path dumpDir = gameDir.resolve("registry-dump");

        try {
            if (Files.exists(dumpDir)) {
                try (var stream = Files.walk(dumpDir)) {
                    stream.sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try {
                                    Files.delete(p);
                                } catch (IOException ignored) {}
                            });
                }
            }
            Files.createDirectories(dumpDir);

            int itemCount = exportRegistry(dumpDir, "items", collectItems());
            int fluidCount = exportRegistry(dumpDir, "fluids", collectFluids());

            int finalItemCount = itemCount;
            int finalFluidCount = fluidCount;
            source.sendSuccess(() -> Component.literal("✔ Registry dump complete: ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(finalItemCount + " items, " + finalFluidCount + " fluids")
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" → registry-dump/")
                            .withStyle(ChatFormatting.GRAY)),
                    true);

        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to export registry: " + e.getMessage()));
            return 0;
        }

        return 1;
    }

    private static List<String> collectItems() {
        List<String> entries = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
                String displayName = item.getDescription().getString();
                entries.add(id + " | " + displayName);
            }
        });
        entries.sort(String::compareTo);
        return entries;
    }

    private static List<String> collectFluids() {
        List<String> entries = new ArrayList<>();
        BuiltInRegistries.FLUID.forEach(fluid -> {
            var id = BuiltInRegistries.FLUID.getKey(fluid);
            if (id != null) {
                String displayName = fluid.getFluidType().getDescription().getString();
                entries.add(id + " | " + displayName);
            }
        });
        entries.sort(String::compareTo);
        return entries;
    }

    private static int exportRegistry(Path dir, String prefix, List<String> entries) throws IOException {
        int fileIndex = 0;
        for (int i = 0; i < entries.size(); i += ENTRIES_PER_FILE) {
            int end = Math.min(i + ENTRIES_PER_FILE, entries.size());
            List<String> chunk = entries.subList(i, end);
            Path file = dir.resolve(String.format("%s_%04d.txt", prefix, fileIndex));
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                for (String entry : chunk) {
                    writer.write(entry);
                    writer.newLine();
                }
            }
            fileIndex++;
        }
        return entries.size();
    }
}
