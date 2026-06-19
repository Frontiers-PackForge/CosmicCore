package com.ghostipedia.cosmiccore.mixin.lso;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sfiomn.legendarysurvivaloverhaul.api.temperature.ITemperatureCapability;
import sfiomn.legendarysurvivaloverhaul.api.temperature.TemperatureEnum;
import sfiomn.legendarysurvivaloverhaul.common.capabilities.temperature.TemperatureCapability;

@Mixin(value = TemperatureCapability.class, remap = false)
public abstract class TemperatureCapabilityMixin implements ITemperatureCapability {

    @Unique
    private int cosmiccore$badTimeTimer = 0;

    @Inject(method = "applyDangerousEffects", at = @At("HEAD"))
    private void cosmiccore$trackBadTime(Player player, TemperatureEnum tempEnum, CallbackInfo ci) {
        // make the player's time alive worse if they're too hot or colds for too long
        cosmiccore$badTimeTimer += switch (tempEnum) {
            // Bad Timer Increases while under the effect of something deadly
            case HEAT_STROKE -> this.getTemperatureLevel() >= TemperatureEnum.HEAT_STROKE.getMiddle() ? 2 : 0;
            case FROSTBITE -> this.getTemperatureLevel() < TemperatureEnum.FROSTBITE.getMiddle() ? 2 : 0;
            case HOT, COLD -> -2; // The Player is not 'taking damage' in this state, thus we want to decay the damage
                                  // tracker.
                                  // .
            case NORMAL -> -4; // Survivable Environment, decrease bad Timer WAY FASTER to avoid getting punched with
                               // really nasty
                               // damage
        };
        cosmiccore$badTimeTimer = Math.max(cosmiccore$badTimeTimer, 0);
    }

    @ModifyExpressionValue(method = "applyDangerousEffects",
                           at = @At(value = "NEW",
                                    target = "net/minecraft/world/effect/MobEffectInstance",
                                    remap = true))
    private MobEffectInstance cosmiccore$modifyDangerousEffects(MobEffectInstance effect, Player player,
                                                                TemperatureEnum tempEnum) {
        // change this to give more/less time before the inevitable
        final int MAX_FREE_TIME_SECONDS = 60;
        int extra = cosmiccore$badTimeTimer - MAX_FREE_TIME_SECONDS;
        if (extra <= 0) {
            return effect;
        }
        // add +1 level of effect for every 10 seconds over the damage threshold
        int amplifier = extra / 10;
        return new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier() + amplifier);
    }

    // this target is funny :3
    // Don't think about it too hard, ok?
    @ModifyExpressionValue(method = "applyDangerousEffects",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z",
                                    remap = true),
                           slice = @Slice(
                                          from = @At("HEAD"),
                                          to = @At(value = "RETURN", ordinal = 1)))
    // This method patches the effect to be reapplied every time applyDangerousEffects is called.
    // It works fine, because MC doesn't error when existing effects are reapplied and instead modifies them
    // to match the new effect.
    private boolean cosmiccore$alwaysApplyEffect(boolean original) {
        // returns false because it's inverted in the if statement. And I can't change that.
        // see https://github.com/SpongePowered/Mixin/issues/365#issuecomment-539464542 for an explanation.
        return false;
    }

    // someone should PR this. Not me though :)
    @ModifyConstant(method = "writeNBT", constant = @Constant(stringValue = "ticktimer"))
    private String cosmiccore$fixSaveDataBug(String original) {
        return "tickTimer";
    }

    @Inject(method = "writeNBT", at = @At("RETURN"))
    private void cosmiccore$saveTime(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        tag.putInt("badTimeTimer", cosmiccore$badTimeTimer);
    }

    @Inject(method = "readNBT", at = @At("RETURN"))
    private void cosmiccore$loadTime(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("badTimeTimer")) {
            cosmiccore$badTimeTimer = compound.getInt("badTimeTimer");
        }
    }
}
