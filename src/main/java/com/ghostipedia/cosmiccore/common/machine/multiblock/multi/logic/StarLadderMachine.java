package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.core.GlobalPos;

public class StarLadderMachine extends LinkedWorkableElectricMultiblockMachine {

    @SaveField
    private final StarLadderUplinkManager uplinkManager = new StarLadderUplinkManager(this);

    public StarLadderMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }

    public StarLadderUplinkManager getUplinkManager() {
        return uplinkManager;
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

    // TODO(8.0.0 MUI2): addDisplayText + createUIWidget/createUI (LDLib UI: research-hub link status + uplink
    // progress via StarLadderWidget/StarLadderFancyUIWidget) were removed in GTCEu 8.0.0. Rebuild on MUI2 when
    // the StarLadder UI is ported; uplinkManager / getLinkedPartners supply the data.
}
