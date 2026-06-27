package com.ghostipedia.cosmiccore.mixin.sable;

import com.gregtechceu.gtceu.api.mui.factory.CoverUIFactory;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.factory.SidedPosGuiData;
import dev.ryanhcode.sable.Sable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CoverUIFactory.class, remap = false)
public abstract class CoverUIFactorySubLevelRangeMixin {

    @Inject(
            method = "canInteractWith(Lnet/minecraft/world/entity/player/Player;Lbrachy/modularui/factory/SidedPosGuiData;)Z",
            at = @At("RETURN"),
            cancellable = true)
    private void cosmiccore$allowSubLevelInteract(Player player, SidedPosGuiData guiData,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }
        if (Sable.HELPER.getContaining(guiData.getLevel(), (Vec3i) guiData.getBlockPos()) != null) {
            cir.setReturnValue(true);
        }
    }
}
