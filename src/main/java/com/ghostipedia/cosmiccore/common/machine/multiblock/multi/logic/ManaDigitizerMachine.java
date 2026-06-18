package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.CropHolderPartMachines;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ManaDigitizerMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ManaDigitizerMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    @Nullable
    protected EnergyContainerList inputEnergyContainers;
    @DescSynced
    private static final Object2IntMap<Item> validIngredients = new Object2IntOpenHashMap<>();
    @DescSynced
    private static final Object2IntMap<FluidStack> validIngredientsFluids = new Object2IntOpenHashMap<>();

    @Nullable
    protected TickableSubscription botanySubs;

    static {
        // Boosting Tiers
        validIngredients.put(Items.TNT, 1);
        validIngredientsFluids.put(GTMaterials.Argon.getFluid(FluidStorageKeys.PLASMA, 1), 3);
    }

    public ManaDigitizerMachine(BlockEntityCreationInfo holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        List<IEnergyContainer> inputEnergyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getBlockPos().asLong(), IO.IN);
            if (io == IO.NONE || io == IO.OUT) continue;
            var handlers = part.getRecipeHandlers();
            for (var handler : handlers) {
                IO handlerIO = handler.getHandlerIO();
                if (handlerIO == IO.IN) {
                    var containers = handler.getCapability(EURecipeCapability.CAP).stream()
                            .filter(IEnergyContainer.class::isInstance)
                            .map(IEnergyContainer.class::cast)
                            .toList();
                    inputEnergyContainers.addAll(containers);
                    traitSubscriptions.add(handler.subscribe(this::updateBotanySubscription));

                }
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(inputEnergyContainers);
        updateBotanySubscription();
    }

    protected void updateBotanySubscription() {
        if (isFormed) {
            botanySubs = subscribeServerTick(botanySubs, this::updateTick);
        }
    }

    protected void updateTick() {
        if (!isWorkingEnabled() || inputEnergyContainers == null) {
            return;
        }
        var cropList = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)
                .stream().filter(IRecipeHandler::shouldSearchContent)
                .filter(CropHolderPartMachines.class::isInstance)
                .map(container -> container.getContents()
                        .stream()
                        .filter(ItemStack.class::isInstance)
                        .map(ItemStack.class::cast)
                        .filter(s -> !s.isEmpty())
                        .toList())
                .toList();
        int flowers = 0;
        for (int i = 0; i < cropList.size(); i++) {
            if (!cropList.get(i).isEmpty()) {
                flowers++;
            }
        }

        if (inputEnergyContainers.getEnergyStored() > GTValues.V[6] * flowers) {
            inputEnergyContainers.removeEnergy(GTValues.V[6] * flowers);
        } else {
            // Stop Item/Fluid Generation
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && isFormed()) {
            updateBotanySubscription();
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.inputEnergyContainers = null;
        updateBotanySubscription();
    }
}
