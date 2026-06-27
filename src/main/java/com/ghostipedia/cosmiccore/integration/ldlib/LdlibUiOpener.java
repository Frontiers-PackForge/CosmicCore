package com.ghostipedia.cosmiccore.integration.ldlib;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.networking.s2c.SPacketUIOpen;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import brachy.modularui.core.mixins.common.ServerPlayerAccessor;
import io.netty.buffer.Unpooled;

import java.util.function.Consumer;

public final class LdlibUiOpener {

    private LdlibUiOpener() {}

    public static boolean open(ModularUI uiTemplate, ServerPlayer player, ResourceLocation uiFactoryId,
                               Consumer<RegistryFriendlyByteBuf> writeHolder) {
        uiTemplate.initWidgets();
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        ServerPlayerAccessor acc = (ServerPlayerAccessor) player;
        acc.invokeNextContainerCounter();
        int windowId = acc.getContainerCounter();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        writeHolder.accept(buf);
        ModularUIContainer container = new ModularUIContainer(uiTemplate, windowId);
        uiTemplate.mainGroup.writeInitialData(buf);
        PacketDistributor.sendToPlayer(player, new SPacketUIOpen(uiFactoryId, buf, windowId));
        acc.invokeInitMenu(container);
        player.containerMenu = container;
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
        return true;
    }
}
