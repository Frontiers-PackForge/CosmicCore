package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Set;

public class ModularWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine implements IModularMultiblock {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ModularWorkableElectricMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private final Set<BlockPos> modules = new ObjectOpenHashSet<>();
    private final Set<IMultiblockModule> moduleMachines = new ObjectOpenHashSet<>();


    public ModularWorkableElectricMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        notifyModules();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void addModule(IMultiblockModule module) {
        modules.add(module.getPos());
        moduleMachines.add(module);
    }

    @Override
    public void removeModule(IMultiblockModule module) {
        modules.remove(module.getPos());
        moduleMachines.remove(module);
    }

    @UnmodifiableView
    public Set<IMultiblockModule> getModules() {
        if (moduleMachines.size() != modules.size()) {
            moduleMachines.clear();
            for (var pos : modules) {
                if (MetaMachine.getMachine(getLevel(), pos) instanceof IMultiblockModule module) {
                    moduleMachines.add(module);
                }
            }
        }
        return Collections.unmodifiableSet(moduleMachines);
    }



    public void notifyModules() {
        for (IMultiblockModule module : getModules()) {
            module.onMultiblockUpdate();
        }
    }
}
