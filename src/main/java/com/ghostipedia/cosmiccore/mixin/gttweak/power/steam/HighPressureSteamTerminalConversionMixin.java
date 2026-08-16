package com.ghostipedia.cosmiccore.mixin.gttweak.power.steam;

import com.ghostipedia.cosmiccore.common.power.steam.HPBoilerRates;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NotifiableFluidTank.class, remap = false)
public abstract class HighPressureSteamTerminalConversionMixin {

    @Inject(
            method = "fill(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)I",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$expandHighPressureSteam(FluidStack resource, FluidAction action,
                                                    CallbackInfoReturnable<Integer> cir) {
        NotifiableFluidTank tank = (NotifiableFluidTank) (Object) this;
        if (tank.handlerIO != IO.IN || !HPBoilerRates.isHighPressureSteam(resource)) return;

        MetaMachine machine = tank.getMachine();
        if (!(machine instanceof SteamMachine) && !(machine instanceof SteamHatchPartMachine)) return;

        int offered = Math.min(resource.getAmount(),
                Integer.MAX_VALUE / HPBoilerRates.COMPACT_RATE);
        if (offered <= 0) {
            cir.setReturnValue(0);
            return;
        }

        int simulatedSteam = tank.fillInternal(HPBoilerRates.expandedSteam(offered), FluidAction.SIMULATE);
        int accepted = simulatedSteam / HPBoilerRates.COMPACT_RATE;
        if (accepted > 0 && action.execute()) {
            tank.fillInternal(HPBoilerRates.expandedSteam(accepted), FluidAction.EXECUTE);
        }
        cir.setReturnValue(accepted);
    }
}
