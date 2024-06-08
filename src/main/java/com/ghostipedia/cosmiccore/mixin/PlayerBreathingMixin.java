package com.ghostipedia.cosmiccore.mixin;

import fuzs.thinair.api.v1.AirQualityLevel;
import fuzs.thinair.helper.AirQualityHelperImpl;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerBreathingMixin {

    @Shadow
    protected boolean wasUnderwater;

    @Unique
    private boolean _$didTurtleEffect = false;

    /**
     * @author MrQuentinet
     * @reason Detect when player enter and exit the water. Reset _$didTurtleEffect to false when leaving water
     */
    @Overwrite
    protected boolean updateIsUnderwater() {
        var isInWater = ((Player)(Object)this).isEyeInFluid(FluidTags.WATER);
        if (isInWater != this.wasUnderwater) {
            this.wasUnderwater = isInWater;
            if (!isInWater) _$didTurtleEffect = false;
        }
        return this.wasUnderwater;
    }

    /**
     * @author MrQuentinet
     * @reason Give Water breathing effect when wearing turtle helmet. Happen only when player enters water
     */
    @Overwrite
    private void turtleHelmetTick() {
        ItemStack itemstack = ((Player)(Object)this).getItemBySlot(EquipmentSlot.HEAD);
        if (itemstack.is(Items.TURTLE_HELMET) && this.wasUnderwater && !_$didTurtleEffect) {
            _$didTurtleEffect = true;
            ((Player)(Object)this).addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false, true));
        }
    }

}
