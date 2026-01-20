package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;

import java.util.List;

public class StarLadderMachine extends LinkedWorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            StarLadderMachine.class,
            LinkedWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public StarLadderMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
    }
}
