package com.ghostipedia.cosmiccore.mixin.architectury;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.network.codec.StreamCodec;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.architectury.impl.NetworkAggregator", remap = false)
public class ArchitecturyNullCodecSkipFixMixin {

    @Inject(
            method = "collectPackets(Ldev/architectury/networking/transformers/PacketSink;Ldev/architectury/networking/NetworkManager$Side;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;Lnet/minecraft/core/RegistryAccess;)V",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V"),
            cancellable = true,
            remap = false)
    private static void cosmiccore$skipNullCodec(CallbackInfo ci, @Local StreamCodec codec) {
        if (codec == null) {
            CosmicCore.LOGGER.warn(
                    "[cosmiccore] architectury NetworkAggregator skipped a packet with a null codec to avoid a world-join NPE");
            ci.cancel();
        }
    }
}
