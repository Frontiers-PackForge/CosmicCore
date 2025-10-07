package com.ghostipedia.cosmiccore.common.ascension;

import com.ghostipedia.cosmiccore.common.abyss.AbyssBudgetCap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.function.BiConsumer;

import static net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES;

public class UpgradeUtils {

    public static BiConsumer<Player,Integer> attribute(ResourceLocation attrId,
                                                       AttributeModifier.Operation op,
                                                       double perRank,
                                                       UUID uuid,
                                                       String name) {
        return (player, rank) -> {
            Attribute attributeVal = ATTRIBUTES.getValue(attrId);
            if (attributeVal == null) return;
            AttributeInstance instance = player.getAttribute(attributeVal);
            if (instance == null) return;
            instance.removeModifier(uuid);
            instance.addPermanentModifier(new AttributeModifier(uuid, name, perRank * rank, op));
        };
    }

    public static BiConsumer<Player,Integer> unlockDimension(ResourceLocation dimId) {
        return (player, rank) -> player.getCapability(AscensionCap.CAP).ifPresent(cap -> cap.unlockedDims().add(dimId));
    }


    public static BiConsumer<Player,Integer> abyssSecondsPerRank(int seconds) {
        return (player, rank) -> {
             //TODO : ABYSS COMPAT
        };
    }




}
