package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.murkbloom.AbyssMachineRestrictions;

import net.minecraft.world.entity.vehicle.AbstractMinecart;

import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CartAssemblerBlockEntity.class)
public abstract class AbyssCartAssemblerMixin {

    @Inject(method = "tryAssemble", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableAbyssCartAssembly(AbstractMinecart cart, CallbackInfo ci) {
        CartAssemblerBlockEntity assembler = (CartAssemblerBlockEntity) (Object) this;
        if (AbyssMachineRestrictions.inAbyss(assembler.getLevel(), assembler.getBlockPos())) {
            ci.cancel();
        }
    }
}
