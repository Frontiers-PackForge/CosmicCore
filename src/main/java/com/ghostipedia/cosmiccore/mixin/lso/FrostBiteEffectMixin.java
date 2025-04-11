package com.ghostipedia.cosmiccore.mixin.lso;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import sfiomn.legendarysurvivaloverhaul.api.ModDamageTypes;
import sfiomn.legendarysurvivaloverhaul.common.effects.FrostbiteEffect;
import sfiomn.legendarysurvivaloverhaul.registry.MobEffectRegistry;
import sfiomn.legendarysurvivaloverhaul.util.DamageSourceUtil;
import sfiomn.legendarysurvivaloverhaul.util.DamageUtil;

@Mixin(value = FrostbiteEffect.class,remap = false)
public abstract class FrostBiteEffectMixin extends MobEffect {
    protected FrostBiteEffectMixin(MobEffectCategory category, int color) {
        super(MobEffectCategory.HARMFUL, 9164281);
    }

    /**
     * @author Ghostipedia
     * @reason Replaces the Default FrostBite Effect with a scaling damage effect
     */
    @Overwrite
    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier)
    {
        if(entity instanceof Player player && !entity.hasEffect(MobEffectRegistry.COLD_IMMUNITY.get()))
        {
            Level level = entity.getCommandSenderWorld();
            float damage = 1F;
            if (DamageUtil.isModDangerous(level) && DamageUtil.healthAboveDifficulty(level, player) && !player.isSleeping())
            {
                player.hurt(DamageSourceUtil.getDamageSource(level, ModDamageTypes.HYPOTHERMIA), damage);
                damage++;
            }
        }
    }

}
