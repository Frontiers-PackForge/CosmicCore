package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSoulContainer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.TickTask;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SoulHatchPartMachine extends TieredIOPartMachine {

    @Persisted
    @DescSynced
    private final NotifiableSoulContainer soulContainer;

    public SoulHatchPartMachine(BlockEntityCreationInfo holder, int tier, IO io) {
        super(holder, tier, io);
        this.soulContainer = new NotifiableSoulContainer(this, io, getMaxConsumption(tier), getMaxCapacity(tier));
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller, String substructureName) {
        super.addedToController(controller, substructureName);
        var level = controller.self().getLevel();
        if (level != null && level.getServer() != null) {
            level.getServer().tell(new TickTask(0, this::invalidateIfDuplicate));
        }
    }

    private void invalidateIfDuplicate() {
        for (var controller : getControllers()) {
            for (var part : controller.getParts()) {
                if (part == this) continue;
                if (part instanceof SoulHatchPartMachine soulHatch && soulHatch.io == this.io) {
                    controller.invalidateStructure();
                }
            }
        }
    }

    // TODO(8.0.0 MUI2): the LDLib createUIWidget surface (owner + soul-network contents readout via
    //  ComponentPanelWidget) was removed in GTCEu 8.0.0. Rebuild on IItemUIHolder/buildUI when the soul UI
    //  is ported; soulContainer.getStacks() supplies the display data. Non-UI logic preserved.

    public static int getMaxConsumption(int tier) {
        return switch (tier) {
            case GTValues.IV -> 10_000;
            case GTValues.LuV -> 50_000;
            case GTValues.ZPM -> 5_000_000;
            case GTValues.UV -> 10_000_000;
            case GTValues.UHV -> 25_000_000;
            case GTValues.UEV -> 50_000_000;
            case GTValues.UIV -> 125_000_000;
            case GTValues.UXV -> 250_000_000;
            case GTValues.OpV -> 500_000_000;
            case GTValues.MAX -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    public static int getMaxCapacity(int tier) {
        return switch (tier) {
            case GTValues.IV -> 1_000_000;
            case GTValues.LuV -> 10_000_000;
            case GTValues.ZPM -> 50_000_000;
            case GTValues.UV -> 100_000_000;
            case GTValues.UHV -> 250_000_000;
            case GTValues.UEV -> 500_000_000;
            case GTValues.UIV -> 1_000_000_000;
            case GTValues.UXV -> 1_500_000_000;
            case GTValues.OpV -> 2_000_000_000;
            case GTValues.MAX -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    @Override
    public int tintColor(int index) {
        return (index == 2) ? GTValues.VC[getTier()] : super.tintColor(index);
    }
}
