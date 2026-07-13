package com.ghostipedia.cosmiccore.common.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Set;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class AbyssMachineRestrictions {

    private static final Set<ResourceLocation> UNDERGARDEN_BLOCKED = Set.of(
            id("gtceu", "lp_steam_miner"),
            id("gtceu", "hp_steam_miner"),
            id("gtceu", "lv_miner"),
            id("gtceu", "mv_miner"),
            id("gtceu", "hv_miner"),
            id("gtceu", "ev_large_miner"),
            id("gtceu", "iv_large_miner"),
            id("gtceu", "luv_large_miner"),
            id("cosmiccore", "ore_extraction_drill_lv"),
            id("cosmiccore", "ore_extraction_drill_hv"),
            id("cosmiccore", "ore_extraction_drill_iv"),
            id("cosmiccore", "ore_extraction_drill_zpm"),
            id("create", "hose_pulley"),
            id("create", "elevator_pulley"));
    private static final ResourceLocation PHYSICS_ASSEMBLER = id("simulated", "physics_assembler");

    private AbyssMachineRestrictions() {}

    public static boolean inUndergarden(Level level) {
        return level != null && level.dimension().equals(MurkbloomServerLogic.HOLLOW_DIM);
    }

    public static boolean inAbyss(Level level, BlockPos pos) {
        return level != null && MurkbloomServerLogic.inHollow(level, pos.getY());
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) return;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock());
        boolean blocked = inUndergarden(level) && UNDERGARDEN_BLOCKED.contains(blockId);
        blocked |= inAbyss(level, event.getPos()) && blockId.equals(PHYSICS_ASSEMBLER);
        if (!blocked) return;
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("cosmiccore.abyss.machine_forbidden"), true);
        }
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
