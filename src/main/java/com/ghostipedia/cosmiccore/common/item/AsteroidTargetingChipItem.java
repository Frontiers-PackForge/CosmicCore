package com.ghostipedia.cosmiccore.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class AsteroidTargetingChipItem extends Item {

    public AsteroidTargetingChipItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getTag();

        if (tag == null || tag.isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.cosmiccore.asteroid_chip.unprogrammed")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }

        // Example NBT schema (adapt as needed)
        // String: AsteroidType = "lith|mafic|mossy|occult|oxide|sanguine|wasteland"
        // Long: TargetId
        // Int: Tier
        // Float: LockStrength (0..1)
        // String: Sector
        // String: Mode = "survey|capture|disassemble"

        String rawType = tag.getString("AsteroidType");
        String asteroidType = prettyType(rawType);
        int tier = tag.getInt("Tier");

        tooltipComponents.add(Component.translatable("tooltip.cosmiccore.asteroid_chip.type", asteroidType)
                .withStyle(ChatFormatting.YELLOW));

        if (tier > 0) {
            tooltipComponents.add(Component.translatable("tooltip.cosmiccore.asteroid_chip.tier", tier)
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    private static String prettyType(String key) {
        if (key == null || key.isBlank()) return "Unknown";
        String k = key.toLowerCase(Locale.ROOT);

        return switch (k) {
            // Carbonic
            case "carbon", "carbonic", "carbon_asteroid", "carbon_asteroid_base" -> "Carbonic Asteroid";

            // Ferric
            case "ferric", "ferric_asteroid" -> "Ferric Asteroid";

            // Exotic / Rare Metals
            case "rare", "rare_metal", "rare_metals", "rare_metals_asteroid", "exotic", "exotic_metals" -> "Exotic Metals Asteroid";

            // Auric
            case "auric", "auric_asteroid" -> "Auric Asteroid";

            // Brimstone
            case "brimstone", "brimstone_asteroid", "sulfuric" -> // Might use this for sulfur, unsure!
                    "Brimstone Asteroid";

            // Lith
            case "lith", "lith_asteroid" -> "Lith Asteroid";

            // Mafic
            case "mafic", "mafic_asteroid" -> "Mafic Asteroid";

            // Mossy
            case "mossy", "mossy_asteroid" -> "Mossy Asteroid";

            // Occult
            case "occult", "occult_asteroid" -> "Occult Asteroid";

            // Oxide
            case "oxide", "oxide_asteroid" -> "Oxide Asteroid";

            // Sanguine
            case "sanguine", "sanguine_asteroid" -> "Sanguine Asteroid";

            // Wasteland
            case "wasteland", "wasteland_asteroid" -> "Wasteland Asteroid";
            default ->
                    // Fallback: title-case single word keys like "basalt" -> "Basalt"
                    k.isEmpty() ? "Unknown" : Character.toUpperCase(k.charAt(0)) + k.substring(1);
        };
    }
}
