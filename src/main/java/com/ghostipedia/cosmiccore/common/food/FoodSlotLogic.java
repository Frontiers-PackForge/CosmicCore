package com.ghostipedia.cosmiccore.common.food;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.SyncFoodDataPacket;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class FoodSlotLogic {

    private FoodSlotLogic() {}

    public static final double BASE_REGEN = 0.25;
    public static final int REGEN_DELAY = 200;
    public static final int REGEN_INTERVAL = 5;
    public static final int RECONCILE_INTERVAL = 40;

    private static final ResourceKey<MobEffect> FD_NOURISHMENT = ResourceKey.create(
            Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath("farmersdelight", "nourishment"));

    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItem();
        CosmicFoodData data = player.getData(CosmicAttachmentTypes.FOOD_DATA);

        if (stack.getItem() == Items.ROTTEN_FLESH) {
            data.clearActive();
            syncNow(player, data);
            return;
        }
        if (!CosmicFoodRegistry.isConsumable(stack)) return;

        data.eat(stack);
        syncNow(player, data);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isDeadOrDying()) return;

        FoodData fd = player.getFoodData();
        fd.setFoodLevel(17);
        fd.setSaturation(0);

        CosmicFoodData data = player.getData(CosmicAttachmentTypes.FOOD_DATA);
        data.tick();

        CosmicFoodModifiers.applyMaxHealth(player, data.totalHeartBonus());
        CosmicFoodModifiers.applyAttributeModifiers(player, data.allActiveAttributes(), data);
        applyEffects(player, data);

        int sinceHurt = player.tickCount - data.lastDamageTick;
        if (sinceHurt > REGEN_DELAY && player.tickCount % REGEN_INTERVAL == 0 && player.getHealth() > 0 &&
                player.getHealth() < player.getMaxHealth()) {
            float heartsPerSecond = (float) (BASE_REGEN + data.totalRegenBonus());
            player.heal(heartsPerSecond * 2f * REGEN_INTERVAL / 20f);
        }

        if (data.consumeDirty() || (data.hasActive() && player.tickCount % RECONCILE_INTERVAL == 0)) {
            CCoreNetwork.sendToPlayer(player, new SyncFoodDataPacket(data));
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().is(DamageTypes.STARVE)) {
            event.setCanceled(true);
            return;
        }
        player.getData(CosmicAttachmentTypes.FOOD_DATA).lastDamageTick = player.tickCount;
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect().is(FD_NOURISHMENT)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(CosmicAttachmentTypes.FOOD_DATA).clearActive();
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncNow(player, player.getData(CosmicAttachmentTypes.FOOD_DATA));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncNow(player, player.getData(CosmicAttachmentTypes.FOOD_DATA));
        }
    }

    private static void syncNow(ServerPlayer player, CosmicFoodData data) {
        CCoreNetwork.sendToPlayer(player, new SyncFoodDataPacket(data));
        data.consumeDirty();
    }

    private static void applyEffects(ServerPlayer player, CosmicFoodData data) {
        applyEffectsFrom(player, data.foods);
        applyEffectsFrom(player, data.brews);
    }

    private static void applyEffectsFrom(ServerPlayer player, List<ActiveFood> list) {
        for (ActiveFood af : list) {
            for (FoodDefinition.EffectSpec spec : af.def.effects()) {
                player.addEffect(new MobEffectInstance(spec.effect(), 40, spec.amplifier(), true, false, false));
            }
        }
    }
}
