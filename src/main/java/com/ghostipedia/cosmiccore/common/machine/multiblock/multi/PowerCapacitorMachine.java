package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;

import java.util.ArrayList;
import java.util.List;

public class PowerCapacitorMachine extends PowerSubstationMachine {

    public PowerCapacitorMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void formStructure(String substructureName) {
        var patternState = getPatternState(substructureName);
        if (!patternState.isFormed() && patternState.getState() == PatternState.CheckState.VALID_CACHED) {
            patternState.getCache().clear();
            checkStructurePattern(substructureName);
        }
        super.formStructure(substructureName);
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        widgets.add(Text.lang("cosmiccore.multiblock.power_capacitor.local_buffer").asWidget());
        widgets.addAll(super.getWidgetsForDisplay(syncManager));
        return widgets;
    }
}
