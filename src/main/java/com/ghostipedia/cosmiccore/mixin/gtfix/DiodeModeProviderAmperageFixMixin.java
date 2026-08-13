package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.integration.jade.provider.DiodeModeProvider;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(value = DiodeModeProvider.class, remap = false)
public abstract class DiodeModeProviderAmperageFixMixin {

    @Inject(
            method = "addTooltip(Lnet/minecraft/nbt/CompoundTag;Lsnownee/jade/api/ITooltip;Lnet/minecraft/world/entity/player/Player;Lsnownee/jade/api/BlockAccessor;Lnet/minecraft/world/level/block/entity/BlockEntity;Lsnownee/jade/api/config/IPluginConfig;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$showConfiguredAmperage(CompoundTag data, ITooltip tooltip, Player player,
                                                   BlockAccessor block, BlockEntity blockEntity,
                                                   IPluginConfig config, CallbackInfo ci) {
        boolean outputFace = block.getHitResult().getDirection() ==
                Direction.from3DDataValue(data.getInt("side"));
        String rating = GTValues.VNF[data.getInt("voltage")] + " §r(" + data.getInt("amps") + "A)";
        tooltip.add(Component.translatable(
                outputFace ? "gtceu.top.transform_output" : "gtceu.top.transform_input", rating));
        ci.cancel();
    }
}
