package com.ghostipedia.cosmiccore.mixin.createultimine;

import com.gregtechceu.gtceu.api.item.IGTTool;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(targets = "io.github.chaosunity.createultimine.WrenchUse", remap = false)
public abstract class GTWrenchUseFixMixin {

    @WrapOperation(
                   method = "handleRightClickBlock",
                   constant = @Constant(classValue = WrenchItem.class),
                   require = 1)
    private boolean cosmiccore$acceptGTWrenches(Object item, Operation<Boolean> original) {
        return original.call(item) || item instanceof IGTTool;
    }
}
