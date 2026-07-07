package com.ghostipedia.cosmiccore.common.compat.aethercurios;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class AetherCuriosEvents {

    private AetherCuriosEvents() {}

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        Item zaniteRing = aetherItem("zanite_ring");
        Item zanitePendant = aetherItem("zanite_pendant");
        if (zaniteRing == Items.AIR) return;

        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            float speed = event.getNewSpeed();
            boolean changed = false;
            for (SlotResult result : inventory.findCurios(zaniteRing)) {
                speed = zaniteBoost(speed, result.stack());
                changed = true;
            }
            List<SlotResult> pendants = inventory.findCurios(zanitePendant);
            if (!pendants.isEmpty()) {
                speed = zaniteBoost(speed, pendants.get(0).stack());
                changed = true;
            }
            if (changed) {
                event.setNewSpeed(speed);
            }
        });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (event.getState().getDestroySpeed(player.level(), event.getPos()) <= 0) return;
        Item zaniteRing = aetherItem("zanite_ring");
        Item zanitePendant = aetherItem("zanite_pendant");
        if (zaniteRing == Items.AIR) return;

        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            for (SlotResult result : inventory.findCurios(zaniteRing)) {
                damageWithChance(player, result.stack());
            }
            for (SlotResult result : inventory.findCurios(zanitePendant)) {
                damageWithChance(player, result.stack());
            }
        });
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            for (SlotResult result : inventory.findCurios(stack -> isAetherGlove(stack), false, "hands")) {
                result.stack().hurtAndBreak(1, (ServerLevel) player.level(), player, item -> {});
            }
        });
    }

    @SubscribeEvent
    public static void onHotFloorDamage(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(DamageTypes.HOT_FLOOR)) return;
        Item iceRing = aetherItem("ice_ring");
        Item icePendant = aetherItem("ice_pendant");
        if (iceRing == Items.AIR) return;

        CuriosApi.getCuriosInventory(event.getEntity()).ifPresent(inventory -> {
            if (inventory.isEquipped(iceRing) || inventory.isEquipped(icePendant)) {
                event.setCanceled(true);
            }
        });
    }

    private static float zaniteBoost(float speed, ItemStack stack) {
        return speed * (1.4f + stack.getDamageValue() / (stack.getMaxDamage() * 3.0f));
    }

    private static void damageWithChance(ServerPlayer player, ItemStack stack) {
        if (player.getRandom().nextInt(6) == 0) {
            stack.hurtAndBreak(1, (ServerLevel) player.level(), player, item -> {});
        }
    }

    private static boolean isAetherGlove(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return "aether".equals(id.getNamespace()) && id.getPath().endsWith("_gloves");
    }

    private static Item aetherItem(String path) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("aether", path))
                .orElse(Items.AIR);
    }
}
