package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MultiblockControllerMachine.class, remap = false)
public abstract class TieredMultiblockControllerMixin {

    @Shadow
    public abstract MultiblockMachineDefinition getDefinition();

    @ModifyReturnValue(method = "getDefaultStructurePattern", at = @At("RETURN"))
    private IBlockPattern cosmiccore$selectDefaultTierPattern(IBlockPattern original) {
        if ((Object) this instanceof ITieredMultiblockMachine tiered &&
                TieredMultiblockPatterns.isTiered(getDefinition())) {
            return TieredMultiblockPatterns.pattern(getDefinition(), tiered.getStructureTier());
        }
        return original;
    }

    @ModifyReturnValue(method = "getSubstructurePattern", at = @At("RETURN"))
    private IBlockPattern cosmiccore$selectCheckedTierPattern(IBlockPattern original, String name) {
        if (MultiblockControllerMachine.DEFAULT_STRUCTURE.equals(name) &&
                (Object) this instanceof ITieredMultiblockMachine tiered &&
                TieredMultiblockPatterns.isTiered(getDefinition())) {
            return TieredMultiblockPatterns.pattern(getDefinition(), tiered.getStructureTier());
        }
        return original;
    }

    @ModifyExpressionValue(
                           method = "onUse",
                           at = @At(value = "INVOKE",
                                    target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
                           require = 1)
    private Object cosmiccore$selectWorldPreviewTierPattern(Object original) {
        if ((Object) this instanceof ITieredMultiblockMachine tiered &&
                TieredMultiblockPatterns.isTiered(getDefinition())) {
            return TieredMultiblockPatterns.pattern(getDefinition(), tiered.getStructureTier());
        }
        return original;
    }
}
