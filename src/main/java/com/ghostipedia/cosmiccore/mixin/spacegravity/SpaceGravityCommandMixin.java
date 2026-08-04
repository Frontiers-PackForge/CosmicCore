package com.ghostipedia.cosmiccore.mixin.spacegravity;

import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.CommandDispatcher;
import com.spacegravity.spacegravity.SpaceGravityCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpaceGravityCommand.class, remap = false)
public final class SpaceGravityCommandMixin {

    private SpaceGravityCommandMixin() {}

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$disable(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo callback) {
        callback.cancel();
    }
}
