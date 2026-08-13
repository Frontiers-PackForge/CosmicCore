package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NotifiableEnergyContainer.class, remap = false)
public abstract class NotifiableEnergyContainerOvervoltageFixMixin {

    @Shadow
    protected long amps;

    @Inject(
            method = "acceptEnergyFromNetwork(Lnet/minecraft/core/Direction;JJ)J",
            at = @At(
                     value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/machine/MetaMachine;getTrait(Ljava/lang/Class;)Lcom/gregtechceu/gtceu/api/machine/trait/MachineTrait;"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$explodeWithoutTraitGate(Direction side, long voltage, long amperage,
                                                    CallbackInfoReturnable<Long> cir) {
        NotifiableEnergyContainer container = (NotifiableEnergyContainer) (Object) this;
        GTUtil.doExplosion(container.getLevel(), container.getBlockPos(), GTUtil.getExplosionPower(voltage));
        cir.setReturnValue(Math.min(amperage, container.getInputAmperage() - amps));
    }
}
