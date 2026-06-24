package com.ghostipedia.cosmiccore.common.block.debug;

import com.ghostipedia.cosmiccore.api.capability.recipe.IHeatContainer;
import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableThermiaContainer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import static com.ghostipedia.cosmiccore.common.machine.multiblock.part.ThermiaHatchPartMachine.getThermiaLimits;

public class CreativeThermiaContainerMachine extends MetaMachine implements IHeatContainer, IMuiMachine {

    // FieldHolder
    @SaveField
    private long heat = 0;
    @SaveField
    private boolean active = false;
    @SaveField
    private boolean source = true;
    private long lastAverageHeatIOPerTick = 0;
    @SaveField
    @SyncToClient
    private final NotifiableThermiaContainer thermiaContainer;

    public CreativeThermiaContainerMachine(BlockEntityCreationInfo info) {
        super(info);
        long currentTemp = 0;
        this.thermiaContainer = new NotifiableThermiaContainer(this, IO.BOTH, getThermiaLimits(GTValues.MAX),
                currentTemp);
    }

    @Override
    public long acceptHeatFromNetwork(Direction side) {
        return 0;
    }

    @Override
    public boolean inputsHeat(Direction side) {
        return false;
    }

    @Override
    public long changeHeat(long heatDifference) {
        return 0;
    }

    @Override
    public long getOverloadLimit() {
        return 0;
    }

    @Override
    public long getHeatStorage() {
        return 0;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        // TODO(8.0.0): reimplement the creative heat controller UI (heat value field +
        //  active/source toggles) in MUI2. Was LDLib ModularUI (createUI). Shelved for launch;
        //  heat logic and fields (heat/active/source) are unaffected.
        return ModularPanel.defaultPanel(getDefinition().getId().getPath(), 176, 166);
    }
}
