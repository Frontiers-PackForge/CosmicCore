package com.ghostipedia.cosmiccore.api.machine.multiblock.modular;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.ModuleConnectorPartMachine;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;

import java.util.*;

public class WorkableModularMultiblockMachine extends WorkableMultiblockMachine implements IModularMultiblock {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WorkableModularMultiblockMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private final Set<BlockPos> modulesPoss = new ObjectOpenHashSet<>();
    private final Set<IMultiblockModule> moduleMachines = new ObjectOpenHashSet<>();
    private boolean modulesResolved = false;

    public WorkableModularMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void addModule(IMultiblockModule module) {
        modulesPoss.add(module.getPos());
        moduleMachines.add(module);
    }

    @Override
    public void removeModule(IMultiblockModule module) {
        modulesPoss.remove(module.getPos());
        moduleMachines.remove(module);
    }

    @Override
    public int getModuleCount() {
        return modulesPoss.size();
    }

    public void setModules(List<BlockPos> posList) {
        modulesResolved = true;
        var level = getLevel();
        if (level == null || posList.isEmpty()) moduleMachines.clear();
        else {
            modulesPoss.clear();
            moduleMachines.clear();
            for (var pos : posList) {
                if (MetaMachine.getMachine(level, pos) instanceof IMultiblockModule module) {
                    module.addBase(this);
                    modulesPoss.add(pos);
                    moduleMachines.add(module);
                }
            }
        }
    }

    public Set<IMultiblockModule> getModulesPoss() {
        if (!modulesResolved) setModules(new ArrayList<>(modulesPoss));
        return Collections.unmodifiableSet(moduleMachines);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        var poss = new ArrayList<BlockPos>();
        for (IMultiPart part : getParts()) {
            if (part instanceof ModuleConnectorPartMachine) {
                for (var controller : part.getControllers()) {
                    if (controller instanceof IMultiblockModule master) {
                        poss.add(master.getPos());
                    }
                }
            }
        }
        setModules(poss);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        var modules = getModulesPoss();
        if (!modules.isEmpty()) {
            for (var module : modules) {
                module.removeBase(this);
            }
        }
        this.modulesPoss.clear();
        this.moduleMachines.clear();
    }

    @Override
    public void notifyModules() {
        for (IMultiblockModule module : getModulesPoss()) {
            module.onBaseUpdate();
        }
    }

    @Override
    public void onModuleUpdate() {
        System.out.println("ModularMulti: Update notification received. IsClient: " + getLevel().isClientSide);
    }
}
