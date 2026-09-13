package com.ghostipedia.cosmiccore.common.commands;

import com.ghostipedia.cosmiccore.common.network.packet.ProductionStatisticsPackets;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.mojang.brigadier.CommandDispatcher;

public final class ProductionStatisticsCommand {

    private ProductionStatisticsCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("productionstats").executes(context -> {
            ProductionStatisticsPackets.Response.open(context.getSource().getPlayerOrException());
            return 1;
        }));
    }
}
