package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
import com.gregtechceu.gtceu.integration.jade.provider.TransformerBlockProvider;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(value = TransformerBlockProvider.class, remap = false)
public abstract class TransformerBlockProviderConfigurationMixin {

    @Unique
    private static final String COSMICCORE_CONFIGURED_RATINGS = "cosmiccoreConfiguredRatings";

    @Inject(
            method = "write(Lcom/gregtechceu/gtceu/common/machine/electric/TransformerMachine;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At("RETURN"),
            require = 1)
    private void cosmiccore$writeConfiguredRatings(TransformerMachine transformer,
                                                   CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        tag.putBoolean(COSMICCORE_CONFIGURED_RATINGS, true);
        tag.putLong("cosmiccoreInputVoltage", transformer.energyContainer.getInputVoltage());
        tag.putLong("cosmiccoreInputAmperage", transformer.energyContainer.getInputAmperage());
        tag.putLong("cosmiccoreOutputVoltage", transformer.energyContainer.getOutputVoltage());
        tag.putLong("cosmiccoreOutputAmperage", transformer.energyContainer.getOutputAmperage());
    }

    @Inject(
            method = "addTooltip(Lnet/minecraft/nbt/CompoundTag;Lsnownee/jade/api/ITooltip;Lnet/minecraft/world/entity/player/Player;Lsnownee/jade/api/BlockAccessor;Lnet/minecraft/world/level/block/entity/BlockEntity;Lsnownee/jade/api/config/IPluginConfig;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void cosmiccore$showConfiguredRatings(CompoundTag data, ITooltip tooltip, Player player,
                                                  BlockAccessor block, BlockEntity blockEntity,
                                                  IPluginConfig config, CallbackInfo ci) {
        if (!data.getBoolean(COSMICCORE_CONFIGURED_RATINGS)) return;
        boolean transformUp = data.getBoolean("transformUp");
        long inputVoltage = data.getLong("cosmiccoreInputVoltage");
        long inputAmperage = data.getLong("cosmiccoreInputAmperage");
        long outputVoltage = data.getLong("cosmiccoreOutputVoltage");
        long outputAmperage = data.getLong("cosmiccoreOutputAmperage");
        tooltip.add(Component.translatable(
                transformUp ? "gtceu.top.transform_up" : "gtceu.top.transform_down",
                cosmiccore$rating(inputVoltage, inputAmperage) + " -> " +
                        cosmiccore$rating(outputVoltage, outputAmperage)));

        Direction front = Direction.from3DDataValue(data.getInt("side"));
        boolean frontFace = block.getHitResult().getDirection() == front;
        boolean inputFace = transformUp != frontFace;
        tooltip.add(Component.translatable(
                inputFace ? "gtceu.top.transform_input" : "gtceu.top.transform_output",
                inputFace ? cosmiccore$rating(inputVoltage, inputAmperage) :
                        cosmiccore$rating(outputVoltage, outputAmperage)));
        ci.cancel();
    }

    @Unique
    private static String cosmiccore$rating(long voltage, long amperage) {
        int tier = Mth.clamp(GTUtil.getTierByVoltage(voltage), 0, GTValues.VNF.length - 1);
        return GTValues.VNF[tier] + " §r(" + amperage + "A)";
    }
}
