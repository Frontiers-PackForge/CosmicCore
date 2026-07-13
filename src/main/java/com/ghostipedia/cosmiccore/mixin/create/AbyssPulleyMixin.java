package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.murkbloom.AbyssMachineRestrictions;

import net.minecraft.world.level.block.entity.BlockEntity;

import com.simibubi.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ HosePulleyBlockEntity.class, ElevatorPulleyBlockEntity.class })
public abstract class AbyssPulleyMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableUndergardenPulley(CallbackInfo ci) {
        BlockEntity pulley = (BlockEntity) (Object) this;
        if (AbyssMachineRestrictions.inUndergarden(pulley.getLevel())) {
            ci.cancel();
        }
    }
}
