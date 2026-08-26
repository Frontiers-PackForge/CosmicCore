package com.ghostipedia.cosmiccore.mixin.ebfix;

import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingAE2CableCompat;
import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingPlacementHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;

import neoforge.nl.requios.effortlessbuilding.network.PacketHandler;
import neoforge.nl.requios.effortlessbuilding.network.PlaceBuildModePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketHandler.class)
public abstract class PacketHandlerPlacementMixin {

    @Inject(method = "handlePlaceBuildMode", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$placeDataSafeBlocks(
                                                       PlaceBuildModePacket packet, ServerPlayer player,
                                                       CallbackInfo ci) {
        if (!(player.getMainHandItem().getItem() instanceof BlockItem) &&
                !EffortlessBuildingAE2CableCompat.isCableItem(player.getMainHandItem()))
            return;
        EffortlessBuildingPlacementHandler.placeBlocks(packet, player);
        ci.cancel();
    }
}
