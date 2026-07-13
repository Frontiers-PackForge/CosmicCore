package com.ghostipedia.cosmiccore.mixin.simulated;

import com.ghostipedia.cosmiccore.common.murkbloom.AbyssMachineRestrictions;

import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PhysicsAssemblerBlockEntity.class)
public abstract class AbyssPhysicsAssemblerMixin {

    @Inject(method = "assembleOrDisassemble", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableAbyssAssembly(CallbackInfo ci) {
        PhysicsAssemblerBlockEntity assembler = (PhysicsAssemblerBlockEntity) (Object) this;
        if (AbyssMachineRestrictions.inAbyss(assembler.getLevel(), assembler.getBlockPos())) {
            ci.cancel();
        }
    }
}
