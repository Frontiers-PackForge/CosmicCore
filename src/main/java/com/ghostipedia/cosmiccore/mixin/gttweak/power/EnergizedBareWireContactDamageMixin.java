package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.common.block.CableBlock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CableBlock.class, remap = false)
public abstract class EnergizedBareWireContactDamageMixin {

    @WrapOperation(
                   method = "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/WireProperties;getLossPerBlock()I"),
                   require = 1,
                   expect = 1,
                   allow = 1)
    private int cosmiccore$useBareConductorHazard(WireProperties properties, Operation<Integer> original) {
        return properties.isSuperconductor() ? 0 : 1;
    }
}
