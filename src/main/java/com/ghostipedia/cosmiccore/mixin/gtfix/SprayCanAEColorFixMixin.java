package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.item.behavior.ColorSprayBehaviour;

import net.minecraft.world.item.DyeColor;

import appeng.api.util.AEColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ColorSprayBehaviour.class, remap = false)
public abstract class SprayCanAEColorFixMixin {

    @Redirect(method = "handleSpecialBlockEntities",
              at = @At(value = "INVOKE", target = "Lappeng/api/util/AEColor;values()[Lappeng/api/util/AEColor;"))
    private AEColor[] cosmiccore$mapByDye() {
        var dyes = DyeColor.values();
        var colors = new AEColor[dyes.length];
        for (var dye : dyes) colors[dye.ordinal()] = AEColor.fromDye(dye);
        return colors;
    }
}
