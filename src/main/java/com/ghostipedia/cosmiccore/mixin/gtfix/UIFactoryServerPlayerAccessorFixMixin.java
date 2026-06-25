package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.networking.s2c.SPacketUIOpen;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import brachy.modularui.core.mixins.common.ServerPlayerAccessor;
import io.netty.buffer.Unpooled;
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
        uiTemplate.initWidgets();
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        ServerPlayerAccessor acc = (ServerPlayerAccessor) player;
        acc.invokeNextContainerCounter();
        int windowId = acc.getContainerCounter();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        writeHolderToSyncData(buf, holder);
        ModularUIContainer container = new ModularUIContainer(uiTemplate, windowId);
        uiTemplate.mainGroup.writeInitialData(buf);
        PacketDistributor.sendToPlayer(player, new SPacketUIOpen(uiFactoryId, buf, windowId));
        acc.invokeInitMenu(container);
        player.containerMenu = container;
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
        cir.setReturnValue(true);
    }
}
