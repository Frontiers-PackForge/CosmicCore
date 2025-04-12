package com.ghostipedia.cosmiccore.mixin.lso;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import sfiomn.legendarysurvivaloverhaul.api.ModDamageTypes;
import sfiomn.legendarysurvivaloverhaul.common.effects.HeatStrokeEffect;
import sfiomn.legendarysurvivaloverhaul.registry.MobEffectRegistry;
import sfiomn.legendarysurvivaloverhaul.util.DamageSourceUtil;

@Mixin(value = HeatStrokeEffect.class, remap = false)
public abstract class HeatStrokeEffectMixin extends MobEffect {

    protected HeatStrokeEffectMixin(MobEffectCategory category, int color) {
        super(MobEffectCategory.HARMFUL, 16756041);
    }

    /**
     * @author Ghostipedia
     * @reasonReplaces the Default HeatStroke Effect with a Flat Damage Increase
     *                 Could someone else figure out how to make it scale, I gave up
     */
    @Overwrite
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player && !livingEntity.hasEffect(MobEffectRegistry.HEAT_IMMUNITY.get())) {
            Level level = livingEntity.getCommandSenderWorld();
            if (!player.isSleeping()) {
                player.hurt(DamageSourceUtil.getDamageSource(level, ModDamageTypes.HYPERTHERMIA), 4);
            }
        }
    }
}
