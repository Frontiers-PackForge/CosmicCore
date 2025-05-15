package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.ModuleConnectorPartMachine;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.io.Serial;
import java.util.Map;

public class WorkableElectricModuleMachine extends WorkableElectricMultiblockMachine implements IMultiblockModule {

    @Getter
    @Persisted
    @DescSynced
    private @Nullable BlockPos baseMultiblockPos;

    private @Nullable IModularMultiblock baseMultiblock;
    private boolean baseMultiblockResolved = false;

    public WorkableElectricModuleMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel level) {
            level.getServer().tell(new TickTask(0, () -> setBaseMultiblock(baseMultiblockPos)));
        }
    }

    public void setBaseMultiblock(@Nullable BlockPos pos) {
        baseMultiblockResolved = true;
        var level = getLevel();
        if (level == null || pos == null) baseMultiblock = null;
        else if (MetaMachine.getMachine(level, pos) instanceof IModularMultiblock machine) {
            baseMultiblockPos = pos;
            baseMultiblock = machine;
            machine.addModule(this);
        } else baseMultiblock = null;
    }

    @Nullable
    public IModularMultiblock getBaseMultiblock() {
        if (!baseMultiblockResolved) setBaseMultiblock(baseMultiblockPos);
        return baseMultiblock;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        if (getLevel() instanceof ServerLevel level) {
            level.getServer().tell(new TickTask(0, () -> setBaseMultiblock(baseMultiblockPos)));
        }

        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            if (part instanceof ModuleConnectorPartMachine) {
                for (var controller : part.getControllers()) {
                    if (controller instanceof IModularMultiblock master) {
                        setBaseMultiblock(master.getPos());
                    }
                }
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        var base = getBaseMultiblock();
        if (base != null) {
            base.removeModule(this);
        }
    }

    @Override
    public void onMultiblockUpdate() {
        System.out.println("ModuleTest: Update notification received. IsClient: " + getLevel().isClientSide);
    }
}
