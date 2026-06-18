package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;
import com.ghostipedia.cosmiccore.client.gui.widget.starladder.StarLadderFancyUIWidget;
import com.ghostipedia.cosmiccore.client.gui.widget.starladder.StarLadderWidget;
import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;

import java.util.List;

public class StarLadderMachine extends LinkedWorkableElectricMultiblockMachine {


    @Getter
    private final StarLadderUplinkManager uplinkManager = new StarLadderUplinkManager(this);

    public StarLadderMachine(BlockEntityCreationInfo holder, Object... args) {
        super(holder, args);
    }


    @Override
    public LinkRole getLinkRole() {
        return LinkRole.CONTROLLER;
    }

    @Override
    public int getMaxPartners() {
        return 1;
    }

    @Override
    public boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine) {
        return partnerMachine instanceof StarLadderResearchHubMachine;
    }

    public ILinkedMultiblock getLinkedPartnerMachine(GlobalPos partner) {
        return getPartnerMachine(partner);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscribeServerTick(this::tickUplink);
        }
    }

    private void tickUplink() {
        uplinkManager.tick();
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.put("uplinkManager", uplinkManager.serializeNBT());
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.contains("uplinkManager")) {
            uplinkManager.deserializeNBT(tag.getCompound("uplinkManager"));
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;

        GlobalPos hub = getLinkedPartners().stream().findFirst().orElse(null);
        if (hub == null) {
            textList.add(Component.literal("No linked Research Hub").withStyle(ChatFormatting.GRAY));
            return;
        }

        boolean online = getPartnerMachine(hub) != null;
        textList.add(Component.literal("Research Hub: " + (online ? "Online" : "Offline"))
                .withStyle(online ? ChatFormatting.GREEN : ChatFormatting.RED));
        textList.add(Component.literal("  " + LinkedMultiblockHelper.getDimensionName(hub.dimension().location()))
                .withStyle(ChatFormatting.GRAY));
        textList.add(Component.literal("  [%d, %d, %d]".formatted(
                hub.pos().getX(), hub.pos().getY(), hub.pos().getZ()))
                .withStyle(ChatFormatting.GRAY));

        StarLadderUplinkState uplinkState = uplinkManager.getState();
        if (uplinkState.isFightState()) {
            textList.add(Component.literal("Uplink: ACTIVE")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            textList.add(Component.literal("  Progress: " + (uplinkManager.getProgress() * 100 / 6000) + "%")
                    .withStyle(ChatFormatting.GOLD));
        } else if (uplinkState == StarLadderUplinkState.COMPLETED) {
            textList.add(Component.literal("Uplink: ESTABLISHED")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        }
    }

    @Override
    public Widget createUIWidget() {
        return new StarLadderWidget(() -> this);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(StarLadderWidget.WIDTH + 16, StarLadderWidget.HEIGHT + 70, this, entityPlayer)
                .widget(new StarLadderFancyUIWidget(this, StarLadderWidget.WIDTH + 16, StarLadderWidget.HEIGHT + 70,
                        () -> 0));
    }
}
