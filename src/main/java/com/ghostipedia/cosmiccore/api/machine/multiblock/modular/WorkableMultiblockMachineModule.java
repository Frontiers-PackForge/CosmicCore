package com.ghostipedia.cosmiccore.api.machine.multiblock.modular;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.ModuleConnectorPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkableMultiblockMachineModule extends WorkableMultiblockMachine implements IMultiblockModule {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WorkableMultiblockMachineModule.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private final Set<BlockPos> baseMultiblockPoss = new ObjectOpenHashSet<>();
    private final Set<IModularMultiblock> baseMultiblocks = new ObjectOpenHashSet<>();
    private boolean baseMultiblockResolved = false;

    public WorkableMultiblockMachineModule(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void addBase(IModularMultiblock base) {
        baseMultiblockPoss.add(base.getPos());
        baseMultiblocks.add(base);
    }

    @Override
    public void removeBase(IModularMultiblock base) {
        baseMultiblockPoss.remove(base.getPos());
        baseMultiblocks.remove(base);
    }

    @Override
    public int getBaseCount() {
        return baseMultiblocks.size();
    }

    public void setBaseMultiblocks(List<BlockPos> posList) {
        baseMultiblockResolved = true;
        var level = getLevel();
        if (level == null || posList.isEmpty()) baseMultiblocks.clear();
        else {
            baseMultiblockPoss.clear();
            baseMultiblocks.clear();
            for (var pos : posList) {
                if (MetaMachine.getMachine(level, pos) instanceof IModularMultiblock machine) {
                    machine.addModule(this);
                    baseMultiblockPoss.add(pos);
                    baseMultiblocks.add(machine);
                }
            }
        }
    }

    public List<IModularMultiblock> getBaseMultiBlocks() {
        if (!baseMultiblockResolved) setBaseMultiblocks(new ArrayList<>(baseMultiblockPoss));
        return new ArrayList<>(baseMultiblocks);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        var poss = new ArrayList<BlockPos>();
        for (IMultiPart part : getParts()) {
            if (part instanceof ModuleConnectorPartMachine) {
                for (var controller : part.getControllers()) {
                    if (controller instanceof IModularMultiblock master) {
                        poss.add(master.getPos());
                    }
                }
            }
        }
        setBaseMultiblocks(poss);
        notifyBases();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        var bases = getBaseMultiBlocks();
        if (!bases.isEmpty()) {
            for (var base : bases) {
                base.removeModule(this);
            }
        }
        this.baseMultiblockPoss.clear();
        this.baseMultiblocks.clear();
    }

    @Override
    public void notifyBases() {
        for (IModularMultiblock base : getBaseMultiBlocks()) {
            base.onModuleUpdate();
        }
    }

    @Override
    public void onBaseUpdate() {
        System.out.println("ModuleTest: Update notification received. IsClient: " + getLevel().isClientSide);
    }
}
