package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.integration.ldlib.LdlibUiOpener;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = UIFactory.class, remap = false)
public abstract class UIFactoryServerPlayerAccessorFixMixin {

    @Shadow
    @Final
    public ResourceLocation uiFactoryId;

    @Shadow
    protected abstract ModularUI createUITemplate(Object holder, Player player);

    @Shadow
    protected abstract void writeHolderToSyncData(RegistryFriendlyByteBuf buf, Object holder);

    @Inject(method = "openUI", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$openUiViaMui2Accessor(Object holder, ServerPlayer player,
                                                  CallbackInfoReturnable<Boolean> cir) {
        ModularUI uiTemplate = createUITemplate(holder, player);
        if (uiTemplate == null) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(LdlibUiOpener.open(uiTemplate, player, uiFactoryId,
                buf -> writeHolderToSyncData(buf, holder)));
    }
}
