package com.ghostipedia.cosmiccore.mixin.apothic;

import com.ghostipedia.cosmiccore.common.compat.apothic.ApothicEffectDamage;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

import com.bawnorton.mixinsquared.TargetHandler;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Stack;

@Mixin(value = LivingEntity.class, priority = 1500)
public abstract class ApothicEffectDamageOverflowFixMixin {

    @Shadow
    protected Stack<DamageContainer> damageContainers;

    @TargetHandler(mixin = "dev.shadowsoffire.apothic_attributes.mixin.LivingEntityMixin",
                   name = "apoth_sunderingApplyEffect",
                   prefix = "redirect")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void cosmiccore$repairEffectOverflow(float value, float minimum, DamageSource source, float damage,
                                                 CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        var sundering = entity.getEffect(ALObjects.MobEffects.SUNDERING);
        int sunderingAmplifier = sundering != null && !source.is(DamageTypeTags.BYPASSES_RESISTANCE) ?
                sundering.getAmplifier() : -1;
        if (!ApothicEffectDamage.needsRepair(value, damage, sunderingAmplifier)) return;

        var resistance = entity.getEffect(MobEffects.DAMAGE_RESISTANCE);
        int resistanceAmplifier = resistance == null ? -1 : resistance.getAmplifier();
        float repaired = ApothicEffectDamage.repair(damage, resistanceAmplifier, sunderingAmplifier);
        DamageContainer container = damageContainers.peek();
        if (!Float.isFinite(container.getNewDamage()) || repaired >= container.getNewDamage()) {
            container.setNewDamage(repaired);
        }
        cir.setReturnValue(Math.max(repaired, minimum));
    }
}
