package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.ModuleConnectorPartMachine;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.*;

public class WorkableElectricModuleMachine extends WorkableElectricMultiblockMachine implements IMultiblockModule {

    @Getter
    @DescSynced
    private final Set<BlockPos> baseMultiblockPoss = new ObjectOpenHashSet<>();
    private final Set<IModularMultiblock> baseMultiblocks = new ObjectOpenHashSet<>();
    private boolean baseMultiblockResolved = false;

    public WorkableElectricModuleMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
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

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.translatable("cosmiccore.multiblock.module.base.count", baseMultiblockPoss.size()));
    }
}
