package com.ghostipedia.cosmiccore.common.vitae;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpawnerMobControl {

    public static final String SPAWNER_BORN = "cosmiccore:spawner_born";
    public static final TagKey<Item> BLOCKED_DROPS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "spawner_blocked_drops"));

    private SpawnerMobControl() {}

    @SubscribeEvent(receiveCanceled = true)
    public static void onSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() == MobSpawnType.SPAWNER || event.getSpawnType() == MobSpawnType.TRIAL_SPAWNER) {
            event.getEntity().getSelfAndPassengers()
                    .forEach(entity -> entity.getPersistentData().putBoolean(SPAWNER_BORN, true));
        }
    }

    @SubscribeEvent
    public static void onConversion(LivingConversionEvent.Post event) {
        if (event.getEntity().getPersistentData().getBoolean(SPAWNER_BORN)) {
            event.getOutcome().getPersistentData().putBoolean(SPAWNER_BORN, true);
        }
    }

    @SubscribeEvent
    public static void onSplit(MobSplitEvent event) {
        if (event.getParent().getPersistentData().getBoolean(SPAWNER_BORN)) {
            event.getChildren().forEach(entity -> entity.getPersistentData().putBoolean(SPAWNER_BORN, true));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().getBoolean(SPAWNER_BORN) &&
                BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).getNamespace().equals("minecraft")) {
            event.getDrops().removeIf(drop -> drop.getItem().is(BLOCKED_DROPS));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSpawnerUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() instanceof SpawnEggItem &&
                event.getLevel().getBlockEntity(event.getPos()) instanceof SpawnerBlockEntity) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }
}
