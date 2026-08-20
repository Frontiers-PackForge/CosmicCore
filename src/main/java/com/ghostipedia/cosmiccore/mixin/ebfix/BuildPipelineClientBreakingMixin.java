package com.ghostipedia.cosmiccore.mixin.ebfix;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import neoforge.nl.requios.effortlessbuilding.buildmode.BuildModeEnum;
import neoforge.nl.requios.effortlessbuilding.buildmode.BuildModes;
import neoforge.nl.requios.effortlessbuilding.buildpipeline.BuildPipeline;
import neoforge.nl.requios.effortlessbuilding.buildpipeline.BuildPipelineClient;
import neoforge.nl.requios.effortlessbuilding.buildpipeline.ConstraintSystem;
import neoforge.nl.requios.effortlessbuilding.config.ServerConfig;
import neoforge.nl.requios.effortlessbuilding.utilities.BlockEntry;
import neoforge.nl.requios.effortlessbuilding.utilities.BlockSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BuildPipelineClient.class)
public abstract class BuildPipelineClientBreakingMixin {

    @Inject(method = "shouldInterceptBreaking", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$allowRejectedTargetsToUseVanillaMining(CallbackInfoReturnable<Boolean> cir) {
        if (BuildModes.CLIENT.getBuildMode() == BuildModeEnum.DISABLED) {
            cir.setReturnValue(false);
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            cir.setReturnValue(false);
            return;
        }
        if (!player.getAbilities().instabuild && !ServerConfig.INSTANCE.survivalAllowBreaking) {
            cir.setReturnValue(false);
            return;
        }
        if (BuildPipelineClient.getBuildState() != null || player.getAbilities().instabuild) {
            cir.setReturnValue(true);
            return;
        }
        if (minecraft.level != null && minecraft.hitResult instanceof BlockHitResult hit) {
            BlockPos target = hit.getBlockPos();
            BlockSet blocks = new BlockSet();
            blocks.add(new BlockEntry(target));
            ConstraintSystem.INSTANCE.processBlocks(blocks, player, BuildPipeline.BuildState.BREAKING);
            BlockEntry entry = blocks.get(target);
            if (entry != null && !entry.isValid()) {
                cir.setReturnValue(false);
                return;
            }
        }
        cir.setReturnValue(true);
    }
}
