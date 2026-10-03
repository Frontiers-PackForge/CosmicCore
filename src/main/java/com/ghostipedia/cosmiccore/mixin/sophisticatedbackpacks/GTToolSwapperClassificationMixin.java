package com.ghostipedia.cosmiccore.mixin.sophisticatedbackpacks;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.p3pp3rf1y.sophisticatedbackpacks.upgrades.toolswapper.ToolSwapperUpgradeWrapper", remap = false)
public class GTToolSwapperClassificationMixin {

    @ModifyReturnValue(method = "canPerformToolAction", at = @At("RETURN"))
    private static boolean cosmiccore$recognizeGTUtilityTools(boolean original, ItemStack stack) {
        var toolTypes = ToolHelper.getToolTypes(stack);
        return original || toolTypes.contains(GTToolType.WRENCH) || toolTypes.contains(GTToolType.WIRE_CUTTER);
    }
}
