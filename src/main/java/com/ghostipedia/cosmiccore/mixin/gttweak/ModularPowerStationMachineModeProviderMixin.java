package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ModularPowerStationMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.jade.provider.MachineModeProvider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(value = MachineModeProvider.class, remap = false)
public abstract class ModularPowerStationMachineModeProviderMixin {

    @Inject(
            method = "addTooltip(Lnet/minecraft/nbt/CompoundTag;Lsnownee/jade/api/ITooltip;Lnet/minecraft/world/entity/player/Player;Lsnownee/jade/api/BlockAccessor;Lnet/minecraft/world/level/block/entity/BlockEntity;Lsnownee/jade/api/config/IPluginConfig;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$hideGenericStationMode(CompoundTag data, ITooltip tooltip, Player player,
                                                   BlockAccessor block, BlockEntity blockEntity,
                                                   IPluginConfig config, CallbackInfo ci) {
        MetaMachine machine = MetaMachine.getMachine(block.getLevel(), block.getPosition());
        if (machine instanceof ModularPowerStationMachine) {
            ci.cancel();
        }
    }
}
