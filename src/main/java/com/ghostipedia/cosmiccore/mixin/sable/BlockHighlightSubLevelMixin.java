package com.ghostipedia.cosmiccore.mixin.sable;

import com.ghostipedia.cosmiccore.client.renderer.SubLevelGridOverlayRenderer;

import com.gregtechceu.gtceu.client.ClientEventListener;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientEventListener.class, remap = false)
public abstract class BlockHighlightSubLevelMixin {

    @Inject(method = "onBlockHighlightEvent", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$drawSubLevelOverlay(RenderHighlightEvent.Block event, CallbackInfo ci) {
        HitResult target = event.getTarget();
        if (!(target instanceof BlockHitResult blockTarget)) {
            return;
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(level, (Vec3i) blockTarget.getBlockPos());
        if (subLevel == null) {
            return;
        }
        SubLevelGridOverlayRenderer.render(event, blockTarget, subLevel);
        ci.cancel();
    }
}
