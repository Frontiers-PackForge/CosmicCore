package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.PCBFoundryMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.integration.jade.provider.ParallelProvider;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ParallelProvider.class, remap = false)
public abstract class ParallelProviderMixin {

    // GTCEu 8.0 replaced Jade's appendServerData(CompoundTag, BlockAccessor) with
    // MachineInfoProvider#write(MetaMachine) -> CompoundTag (the server-data producer). To hide parallel info
    // for our custom machines we just return an empty tag from write() for them, so addTooltip finds no
    // "parallel" key and renders nothing. This collapses the old 4 surgical injects into one HEAD cancel.
    @Inject(method = "write", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmicCore$skipParallelInfo(MetaMachine machine, CallbackInfoReturnable<CompoundTag> cir) {
        if (machine instanceof StellarBaseModule ||
                machine instanceof PCBFoundryMachine ||
                cosmicCore$isPCBFoundryParallelHatch(machine)) {
            cir.setReturnValue(new CompoundTag());
        }
    }

    @Unique
    private boolean cosmicCore$isPCBFoundryParallelHatch(MetaMachine machine) {
        if (machine instanceof ParallelHatchPartMachine hatch) {
            if (hatch.getControllers().size() == 1) {
                return hatch.getControllers().first() instanceof PCBFoundryMachine;
            }
        }
        return false;
    }
}
