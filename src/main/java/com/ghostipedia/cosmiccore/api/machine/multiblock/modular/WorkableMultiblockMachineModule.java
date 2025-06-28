package com.ghostipedia.cosmiccore.api.machine.multiblock.modular;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.ModuleConnectorPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

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

//    private ConditionalSubscriptionHandler tickSubscription;

    public WorkableMultiblockMachineModule(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
//        tickSubscription = new ConditionalSubscriptionHandler(this, this::tick, this::isSubscriptionActive);
    }

    private boolean isSubscriptionActive() {
        if (!isFormed()) return false;
        if (!hasCapabilityProxies()) return false;

        for (var base : getBaseMultiBlocks()) {
            if (!base.isWorking()) return false;
        }

        return true;
    }

    private void tick() {
        if (isWorkingEnabled()) getRecipeLogic().setStatus(isSubscriptionActive() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.SUSPEND);
//        tickSubscription.updateSubscription();
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
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

    /**
     * This method is called when the module is added to a multiblock.
     * It can be used to add capabilities from the base multiblocks to this module.
     * @param capabilitiesToExtract The list of capabilities to extract from the base multiblocks.
     * empty by default
     */
    public void addCapabilitiesFromBase(List<IoRecipeCapability> capabilitiesToExtract) {}

    private void addBaseCapabilities(List<IoRecipeCapability> capabilitiesToExtract) {
        for (IModularMultiblock base : getBaseMultiBlocks()) {
            for (var ioCap : capabilitiesToExtract) {
                var handlerList = RecipeHandlerList.of(ioCap.io, base.getCapabilities(ioCap.io, ioCap.cap));
                addHandlerList(handlerList);
                traitSubscriptions.add(handlerList.subscribe(recipeLogic::updateTickSubscription));
            }
        }
    }

    public static class IoRecipeCapability {
        protected final IO io;
        protected final RecipeCapability<?> cap;

        public IoRecipeCapability(IO io, RecipeCapability<?> cap) {
            this.io = io;
            this.cap = cap;
        }
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

        // Extract requested capabilities from base Multiblocks
        var capabilitiesToExtract = new ArrayList<IoRecipeCapability>();
        addCapabilitiesFromBase(capabilitiesToExtract);
        addBaseCapabilities(capabilitiesToExtract);

//        tickSubscription.updateSubscription();
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
//        tickSubscription.updateSubscription();
        getRecipeLogic().setStatus(isSubscriptionActive() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.SUSPEND);
    }
}
