package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;

import lombok.Getter;

import java.util.List;

public class TitanFusionReactorMachine extends WorkableElectricMultiblockMachine {

    @SaveField
    @SyncToClient
    @DropSaved
    @Getter
    private long EUSpent = 0L;

    @SaveField
    @SyncToClient
    @DropSaved
    @Getter
    private int reactorTier = 3;  // To 10, 7 Upgrades

    @SaveField
    @SyncToClient
    @DropSaved
    private boolean canUpgrade = false;

    public TitanFusionReactorMachine(BlockEntityCreationInfo info) {
        super(info, new HelixFusionRecipeLogic());
    }

    long cost = upgradeCost(reactorTier);

    public void attemptUpgrade() {
        if (reactorTier >= 10) {
            cost = 0L;
            return;
        }
        long costNow = upgradeCost(reactorTier);
        if (EUSpent < costNow) {
            cost = costNow;
            return;
        }
        EUSpent -= costNow;
        reactorTier++;

        cost = upgradeCost(reactorTier);
    }

    static long upgradeCost(int reactorTier) {
        final long BASE = 8_000_000_000L;
        final long MAX = 1_000_000_000_000_000L;
        final int MIN_TIER = 3, MAX_TIER = 10;
        // Could be a clamp but the clamp was being dumb
        // Dumb solution for dumb person is this not a clamp but totally a clamp
        int t = Math.max(MIN_TIER, Math.min(MAX_TIER, reactorTier));
        if (t >= MAX_TIER) return 0L;

        double r = Math.pow((double) MAX / (double) BASE, 1.0 / (MAX_TIER - MIN_TIER));
        double raw = BASE * Math.pow(r, t - MIN_TIER);

        return Math.round(raw / 1_000_000.0) * 1_000_000L;
    }

    public void increaseEUConsumed(long EUSpent) {
        this.EUSpent += EUSpent;
    }

    // TODO(8.0.0 MUI2): custom UI shelved; default UI used (orig in git)
    // IDisplayUIMachine + LDLib createUI(Player)/createUIWidget()/addDisplayText(List<Component>) were removed in
    // 8.0.0. The shelved UI rendered the reactor tier/EU-spent/upgrade-cost readout plus an "Upgrade Reactor Tier"
    // button wired to attemptUpgrade(); attemptUpgrade() is preserved above as gameplay logic so the MUI2 rebuild
    // can re-wire the button. Default getWidgetsForDisplay(PanelSyncManager) is used in the meantime.

    public static class HelixFusionRecipeLogic extends RecipeLogic {

        public HelixFusionRecipeLogic() {
            super();
        }

        @Override
        public TitanFusionReactorMachine getMachine() {
            return (TitanFusionReactorMachine) super.getMachine();
        }

        @Override
        protected List<Class<?>> validMachineClasses() {
            return List.of(TitanFusionReactorMachine.class);
        }

        @Override
        public void onRecipeFinish() {
            if (lastRecipe != null) {
                var inputs = lastRecipe.getInputEUt();
                long totalEUt = inputs.getTotalEU() * lastRecipe.duration;
                getMachine().increaseEUConsumed(totalEUt);
            }
            super.onRecipeFinish();
        }
    }

    public static long clampLong(long v, long min, long max) {
        return Math.max(min, Math.min(v, max));
    }
}
