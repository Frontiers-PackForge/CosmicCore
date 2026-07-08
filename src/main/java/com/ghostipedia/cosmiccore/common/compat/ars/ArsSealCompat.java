package com.ghostipedia.cosmiccore.common.compat.ars;

import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;

import java.util.Set;

public final class ArsSealCompat {

    private ArsSealCompat() {}

    public static final Set<ResourceLocation> SEALED_BOOKS = Set.of(
            ResourceLocation.fromNamespaceAndPath("ars_nouveau", "novice_spell_book"),
            ResourceLocation.fromNamespaceAndPath("ars_nouveau", "apprentice_spell_book"),
            ResourceLocation.fromNamespaceAndPath("ars_nouveau", "archmage_spell_book"));

    public static boolean isLoaded() {
        return ModList.get().isLoaded("ars_nouveau");
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ArsSealCompat::onSpellCast);
    }

    private static void onSpellCast(SpellCastEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide || player.isCreative()) return;
        if (player.getData(CosmicAttachmentTypes.ABYSS_ATTUNED)) return;
        ItemStack tool = event.context.getCasterTool();
        if (tool.isEmpty() || !SEALED_BOOKS.contains(BuiltInRegistries.ITEM.getKey(tool.getItem()))) return;
        event.setCanceled(true);
        player.displayClientMessage(Component.translatable("cosmiccore.abyss.tome_sealed"), true);
    }
}
