package com.ghostipedia.cosmiccore.common.ascension;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * @param costs cost per purchase (per rank)
 * @param apply (player, newRank)
 */
public record Upgrade(ResourceLocation id, String title, int maxRank, List<Cost> costs,
                      Predicate<IAscensionProgress> prereq, BiConsumer<Player, Integer> apply) {

    public record Cost(AscensionConsumables currency, long amount) {
    }

}
