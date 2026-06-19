package com.ghostipedia.cosmiccore.common.item;

import com.ghostipedia.cosmiccore.utils.ItemData;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;

public class AsteroidTargetingChipItem extends Item {

    public AsteroidTargetingChipItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        CompoundTag tag = ItemData.readTag(stack);

        if (tag.isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.cosmiccore.asteroid_chip.unprogrammed")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }

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
            case "carbon", "carbonic", "carbon_asteroid", "carbon_asteroid_base" -> "Carbonic Asteroid";
            case "ferric", "ferric_asteroid" -> "Ferric Asteroid";
            case "rare", "rare_metal", "rare_metals", "rare_metal_asteroid", "exotic", "exotic_metals" -> "Rare Metals Asteroid";
            case "auric", "auric_asteroid" -> "Auric Asteroid";
            case "brimstone", "brimstone_asteroid", "sulfuric" -> "Brimstone Asteroid";
            case "lith", "lith_asteroid" -> "Lith Asteroid";
            case "mafic", "mafic_asteroid" -> "Mafic Asteroid";
            case "mossy", "mossy_asteroid" -> "Mossy Asteroid";
            case "occult", "occult_asteroid" -> "Occult Asteroid";
            case "oxide", "oxide_asteroid" -> "Oxide Asteroid";
            case "sanguine", "sanguine_asteroid" -> "Sanguine Asteroid";
            case "wasteland", "wasteland_asteroid" -> "Wasteland Asteroid";
            default -> k.isEmpty() ? "Unknown" : Character.toUpperCase(k.charAt(0)) + k.substring(1);
        };
    }
}
