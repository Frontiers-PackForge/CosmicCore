package com.ghostipedia.cosmiccore.mixin.sable;

import com.gregtechceu.gtceu.api.mui.factory.MachineUIFactory;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.factory.PosGuiData;
import dev.ryanhcode.sable.Sable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MachineUIFactory.class, remap = false)
public abstract class MachineUIFactorySubLevelRangeMixin {

    @Inject(method = "canInteractWith(Lnet/minecraft/world/entity/player/Player;Lbrachy/modularui/factory/PosGuiData;)Z",
            at = @At("RETURN"),
            cancellable = true)
    private void cosmiccore$allowSubLevelInteract(Player player, PosGuiData guiData,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }
        if (player != guiData.getPlayer() || MachineUIFactory.getMachine(guiData) == null) {
            return;
        }
        if (Sable.HELPER.getContaining(guiData.getLevel(), (Vec3i) guiData.getBlockPos()) != null) {
            cir.setReturnValue(true);
        }
    }
}
