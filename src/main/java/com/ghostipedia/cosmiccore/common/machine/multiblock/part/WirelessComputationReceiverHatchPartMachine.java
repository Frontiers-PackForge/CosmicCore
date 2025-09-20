package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.wireless.WirelessCwuStore;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

public class WirelessComputationReceiverHatchPartMachine extends MultiblockPartMachine
                                                         implements IOpticalComputationReceiver {

    public WirelessComputationReceiverHatchPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public IOpticalComputationProvider getComputationProvider() {
        var team = ((FTBOwner) getOwner()).getPlayerTeam(getOwnerUUID());
        var owner = team != null ? team.getTeamId() : getOwnerUUID();

        return WirelessCwuStore.getWirelessCwuStore(owner);
    }
}
