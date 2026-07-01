package com.ghostipedia.cosmiccore.common.food;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.List;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class FoodDemos {

    private FoodDemos() {}

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        CosmicFoodRegistry.register(Items.GOLDEN_APPLE, new FoodDefinition(
                FoodCategory.FOOD, 20.0, 3.0, 36000,
                List.of(
                        new FoodDefinition.EffectSpec(MobEffects.NIGHT_VISION, 0),
                        new FoodDefinition.EffectSpec(MobEffects.REGENERATION, 1),
                        new FoodDefinition.EffectSpec(MobEffects.MOVEMENT_SPEED, 1),
                        new FoodDefinition.EffectSpec(MobEffects.DAMAGE_RESISTANCE, 0),
                        new FoodDefinition.EffectSpec(MobEffects.FIRE_RESISTANCE, 0),
                        new FoodDefinition.EffectSpec(MobEffects.WATER_BREATHING, 0)),
                List.of(
                        new AttributeSpec(Attributes.ARMOR, 4.0, AttributeModifier.Operation.ADD_VALUE),
                        new AttributeSpec(Attributes.MOVEMENT_SPEED, 0.10,
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                List.of(
                        new BehaviorLine("◌", 0xFF66E0FF, "Abyssal breath", "+30% air"))));
    }
}
