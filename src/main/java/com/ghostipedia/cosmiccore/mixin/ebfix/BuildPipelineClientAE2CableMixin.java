package com.ghostipedia.cosmiccore.mixin.ebfix;

import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingAE2CableCompat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import neoforge.nl.requios.effortlessbuilding.buildpipeline.BuildPipeline;
import neoforge.nl.requios.effortlessbuilding.buildpipeline.BuildPipelineClient;
import neoforge.nl.requios.effortlessbuilding.utilities.BlockSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuildPipelineClient.class)
public abstract class BuildPipelineClientAE2CableMixin {

    @Inject(method = "updateDisplayTrackers", at = @At("TAIL"))
    private static void cosmiccore$trackAE2CableMaterials(Player player, BlockSet blocks, CallbackInfo ci) {
        if (BuildPipelineClient.getBuildState() == BuildPipeline.BuildState.BREAKING) return;
        ItemStack held = player.getMainHandItem();
        if (!EffortlessBuildingAE2CableCompat.isCableItem(held)) return;
        BuildPipelineClient.ITEM_USAGE.compute(
                player, blocks.validPositions(), held.getItem(), player.getAbilities().instabuild);
    }
}
